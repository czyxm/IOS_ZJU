package com.example.weather;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.gson.Gson;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;

import interfaces.heweather.com.interfacesmodule.bean.Code;
import interfaces.heweather.com.interfacesmodule.bean.Lang;
import interfaces.heweather.com.interfacesmodule.bean.Unit;
import interfaces.heweather.com.interfacesmodule.bean.weather.now.Now;
import interfaces.heweather.com.interfacesmodule.view.HeConfig;
import interfaces.heweather.com.interfacesmodule.view.HeWeather;

public class CityActivity extends AppCompatActivity {
    private String id = "HE2004101743231681";
    private String key = "3930614036664920b938cb85206f363b";
    private ListView cityView;
    private MainActivity.LoadingView loadingView;
    private FloatingActionButton addCityFab;
    private ArrayList<Now> info;
    private HashMap<String, Now> cityMap;
    private CityAdapter cityAdapter;
    private UIHandler uiHandler;
    private final String CITY_FILE = "city_list.txt";
    private final int REQUEST_CODE = 20;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_city);
        HeConfig.init(id, key);
        HeConfig.switchToFreeServerNode();

        cityView = findViewById(R.id.cityView);
        addCityFab = findViewById(R.id.addCityFab);
        loadingView = new MainActivity.LoadingView(this, R.style.CustomDialog);

        info = new ArrayList<>();
        cityMap = new HashMap<>();
        cityAdapter = new CityAdapter(this, info);
        cityView.setAdapter(cityAdapter);
        uiHandler = new UIHandler(this);

        BufferedReader input = null;
        try {
            input = new BufferedReader(new InputStreamReader(openFileInput(CITY_FILE)));
            String line;
            StringBuffer buffer = new StringBuffer();
            while ((line = input.readLine()) != null) {
                buffer.append(line + "\n");
            }
            String [] cities = buffer.toString().split("\n");
            for (String city : cities) {
                addCity(city);
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        addCityFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(CityActivity.this, AddActivity.class);
                startActivityForResult(intent, REQUEST_CODE);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && requestCode == REQUEST_CODE) {
            addCity(data.getStringExtra("cityName"));
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        try {
            FileOutputStream fos = openFileOutput(CITY_FILE, MODE_WORLD_WRITEABLE);
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(fos));
            for (String city : cityMap.keySet()) {
                writer.write(city + "\n");
            }
            writer.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static class UIHandler extends Handler {
        private final WeakReference<Activity> mActivityReference;

        UIHandler(Activity activity) {
            this.mActivityReference = new WeakReference<Activity>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            CityActivity activity = (CityActivity) mActivityReference.get();
            activity.loadingView.dismiss();
            if (msg.what == 1) {
                try {
                    Log.i("info", activity.info.toString());
                    Now now = (Now)msg.obj;
                    activity.cityMap.put(now.getBasic().getLocation(), now);
                    activity.cityAdapter.add(now);
                    Log.i("add-1", "add to map and info:\n Map:: " + activity.cityMap.toString());

                } catch(NullPointerException e) {
                    Log.i("info", "info is null");
                } catch (ClassCastException e) {
                    Log.i("info", "info is null");
                }
            } else {
                Log.i("info", "message what error");
            }
        }
    }

    public void fetchWeather(final String cityName) {
        loadingView.show();
        HeWeather.getWeatherNow(CityActivity.this, cityName, Lang.CHINESE_SIMPLIFIED , Unit.METRIC , new HeWeather.OnResultWeatherNowBeanListener() {
            @Override
            public void onError(Throwable e) {
                loadingView.dismiss();
                Toast.makeText(CityActivity.this, "网络错误，请重试！", Toast.LENGTH_SHORT).show();
                Log.d("info_fetch", "Weather Now onError: ", e);
            }

            @Override
            public void onSuccess(Now dataObject) {
                Log.d("info_fetch", " Weather Now onSuccess: " + new Gson().toJson(dataObject));
                if ( Code.OK.getCode().equalsIgnoreCase(dataObject.getStatus()) ){
                    Message msg = Message.obtain();
                    msg.what = 1;
                    msg.obj = dataObject;
                    uiHandler.sendMessage(msg);
                } else {
                    loadingView.dismiss();
                    Toast.makeText(CityActivity.this, "无效城市名!", Toast.LENGTH_SHORT).show();
                    String status = dataObject.getStatus();
                    Code code = Code.toEnum(status);
                    Log.d("info_fetch", "failed code: " + code);
                }
            }
        });
    }

    public void addCity(String cityName) {
        if (!cityName.isEmpty() && !cityMap.containsKey(cityName)) {
            Log.i("add-1", "To add " + cityName);
            fetchWeather(cityName);
        } else {
            Log.i("add-1", cityName + " exits");
        }
    }

    public class CityAdapter extends ArrayAdapter<Now> {
        // View lookup cache
        private class ViewHolder {
            TextView name;
            TextView weather;
            TextView temperature;
            ImageView icon, detail;
        }

        public CityAdapter(Context context, ArrayList<Now> cityBases) {
            super(context, R.layout.item_city, cityBases);
        }

        @SuppressLint("SetTextI18n")
        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            // Get the data item for this position
            final Now now = getItem(position);
            // Check if an existing view is being reused, otherwise inflate the view
            final CityActivity.CityAdapter.ViewHolder viewHolder; // view lookup cache stored in tag
            if (convertView == null) {
                // If there's no view to re-use, inflate a brand new view for row
                viewHolder = new CityActivity.CityAdapter.ViewHolder();
                LayoutInflater inflater = LayoutInflater.from(getContext());
                convertView = inflater.inflate(R.layout.item_city, parent, false);
                viewHolder.name = convertView.findViewById(R.id.tvCityName);
                viewHolder.weather = convertView.findViewById(R.id.tvCityWeather);
                viewHolder.temperature = convertView.findViewById(R.id.tvCityTemperature);
                viewHolder.icon = convertView.findViewById(R.id.tvCityIcon);
                viewHolder.detail = convertView.findViewById(R.id.detailImageView);
                // Cache the viewHolder object inside the fresh view
                convertView.setTag(viewHolder);
            } else {
                // View is being recycled, retrieve the viewHolder object from tag
                viewHolder = (CityActivity.CityAdapter.ViewHolder) convertView.getTag();
            }
            // Populate the data from the data object via the viewHolder object
            // into the template view.
            final String city = now.getBasic().getLocation();
            viewHolder.name.setText(city);
            viewHolder.weather.setText(now.getNow().getCond_txt());
            viewHolder.temperature.setText(now.getNow().getTmp() + "℃");
            int id = convertView.getResources().getIdentifier("icon" + now.getNow().getCond_code(), "drawable", "com.example.weather");
            viewHolder.icon.setImageResource(id);
            viewHolder.detail.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(CityActivity.this, MainActivity.class);
                    intent.putExtra("cityName", city);
                    startActivity(intent);
                }
            });
            convertView.setOnTouchListener(new OnSwipeTouchListener(CityActivity.this) {
                @Override
                public void onSwipeLeft() {
                    cityAdapter.remove(cityMap.get(city));
                    cityMap.remove(city);
                    Toast.makeText(CityActivity.this, city + " 删除成功!", Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onSwipeRight() {
                    String city = now.getBasic().getLocation();
                    cityAdapter.remove(cityMap.get(city));
                    cityMap.remove(city);
                    Toast.makeText(CityActivity.this, city + " 删除成功!", Toast.LENGTH_SHORT).show();
                }
            });
            // Return the completed view to render on screen
            return convertView;
        }
    }

    public class OnSwipeTouchListener implements View.OnTouchListener {

        private GestureDetector gestureDetector;

        public OnSwipeTouchListener(Context c) {
            gestureDetector = new GestureDetector(c, new GestureListener());
        }

        public boolean onTouch(final View view, final MotionEvent motionEvent) {
            return gestureDetector.onTouchEvent(motionEvent);
        }

        private final class GestureListener extends GestureDetector.SimpleOnGestureListener {

            private static final int SWIPE_THRESHOLD = 100;
            private static final int SWIPE_VELOCITY_THRESHOLD = 100;

            @Override
            public boolean onDown(MotionEvent e) {
                return true;
            }

            // Determines the fling velocity and then fires the appropriate swipe event accordingly
            @Override
            public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
                boolean result = false;
                try {
                    float diffY = e2.getY() - e1.getY();
                    float diffX = e2.getX() - e1.getX();
                    if (Math.abs(diffX) > Math.abs(diffY)) {
                        if (Math.abs(diffX) > SWIPE_THRESHOLD && Math.abs(velocityX) > SWIPE_VELOCITY_THRESHOLD) {
                            if (diffX > 0) {
                                onSwipeRight();
                            } else {
                                onSwipeLeft();
                            }
                        }
                    } else {
                        if (Math.abs(diffY) > SWIPE_THRESHOLD && Math.abs(velocityY) > SWIPE_VELOCITY_THRESHOLD) {
                            if (diffY > 0) {
                                onSwipeDown();
                            } else {
                                onSwipeUp();
                            }
                        }
                    }
                } catch (Exception exception) {
                    exception.printStackTrace();
                }
                return result;
            }
        }

        public void onSwipeRight() {
        }

        public void onSwipeLeft() {
        }

        public void onSwipeUp() {
        }

        public void onSwipeDown() {
        }
    }
}
