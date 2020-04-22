package com.example.weather;

import androidx.appcompat.app.AppCompatActivity;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import interfaces.heweather.com.interfacesmodule.bean.weather.forecast.Forecast;
import interfaces.heweather.com.interfacesmodule.bean.weather.forecast.ForecastBase;
import interfaces.heweather.com.interfacesmodule.bean.Code;
import interfaces.heweather.com.interfacesmodule.bean.Lang;
import interfaces.heweather.com.interfacesmodule.bean.Unit;
import interfaces.heweather.com.interfacesmodule.view.HeConfig;
import interfaces.heweather.com.interfacesmodule.view.HeWeather;

import java.lang.ref.WeakReference;
import java.util.ArrayList;

import com.google.gson.Gson;
import com.heweather.plugin.view.HeContent;
import com.heweather.plugin.view.HeWeatherConfig;
import com.heweather.plugin.view.RightLargeView;

public class MainActivity extends AppCompatActivity {
    private String id = "HE2004101743231681";
    private String key = "3930614036664920b938cb85206f363b";
    private ArrayList<ForecastBase> info;
    private LoadingView loadingView;
    private ListView forecastListView;
    private UIHandler uiHandler;
    private RightLargeView rlView;
    private ForecastAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        String cityName = getIntent().getStringExtra("cityName");
        HeConfig.init(id, key);
        HeConfig.switchToFreeServerNode();
        HeWeatherConfig.init(id, cityName);

        rlView = findViewById(R.id.rlView);
        LinearLayout rightLayout = rlView.getRightLayout();
        LinearLayout leftTopLayout = rlView.getLeftTopLayout();
        LinearLayout leftBottomLayout = rlView.getLeftBottomLayout();
        rlView.setClickable(false);
        rlView.setDefaultBack(false);
        rlView.setStroke(5, R.color.colorPrimary, 1, Color.WHITE);
        rlView.addLocation(leftTopLayout, 14, Color.WHITE);
        rlView.addAqiText(leftTopLayout, 14);
        rlView.addAqiQlty(leftTopLayout, 14);
        rlView.addAqiNum(leftTopLayout, 14);
        rlView.addAlarmIcon(leftTopLayout, 14);
        rlView.addAlarmTxt(leftTopLayout, 14);
        rlView.addWeatherIcon(leftTopLayout, 14);
        rlView.addRainIcon(leftBottomLayout, 14);
        rlView.addRainDetail(leftBottomLayout, 14, Color.WHITE);
        rlView.addWindIcon(leftBottomLayout, 14);
        rlView.addWind(leftBottomLayout, 14, Color.WHITE);
        rlView.addCond(leftBottomLayout, 14, Color.WHITE);
        rlView.addTemp(rightLayout, 40, Color.WHITE);
        rlView.setViewGravity(HeContent.GRAVITY_RIGHT);
        rlView.show();

        loadingView = new LoadingView(this, R.style.CustomDialog);

        info = new ArrayList<>();
        adapter = new ForecastAdapter(this, info);
        forecastListView = findViewById(R.id.forecastListView);
        forecastListView.setAdapter(adapter);
        uiHandler = new UIHandler(this);
        fetchWeather(cityName);
    }

    @Override
    protected void onStop() {
        super.onStop();
        loadingView.dismiss();
    }

    protected void fetchWeather(String cityName) {
        loadingView.show();
        HeWeather.getWeatherForecast(MainActivity.this, cityName, Lang.CHINESE_SIMPLIFIED, Unit.METRIC, new HeWeather.OnResultWeatherForecastBeanListener() {
            @Override
            public void onError(Throwable throwable) {
                loadingView.dismiss();
                Toast.makeText(MainActivity.this, "网络错误，请重试！", Toast.LENGTH_SHORT).show();
                Log.d("info_fetch", "Weather Now onError: ", throwable);
            }

            @Override
            public void onSuccess(Forecast forecast) {
                Log.i("info_fetch", " Weather Now onSuccess: " + new Gson().toJson(forecast));
                if ( Code.OK.getCode().equalsIgnoreCase(forecast.getStatus()) ){
                    Message msg = Message.obtain();
                    msg.what = 1;
                    msg.obj = (ArrayList<ForecastBase>) forecast.getDaily_forecast();
                    uiHandler.sendMessage(msg);
                } else {
                    loadingView.dismiss();
                    Toast.makeText(MainActivity.this, "无效城市名!", Toast.LENGTH_SHORT).show();
                    String status = forecast.getStatus();
                    Code code = Code.toEnum(status);
                    Log.i("info_fetch", "failed code: " + code);
                }
            }
        });
    }

    private static class UIHandler extends Handler {
        private final WeakReference<Activity> mActivityReference;

        UIHandler(Activity activity) {
            this.mActivityReference = new WeakReference<Activity>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            MainActivity activity = (MainActivity) mActivityReference.get();
            super.handleMessage(msg);
            activity.loadingView.dismiss();
            if (msg.what == 1) {
                try {
                    Log.i("info", activity.info.toString());
                    activity.adapter.addAll((ArrayList<ForecastBase>)msg.obj);

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

    public static class LoadingView extends ProgressDialog {
        public LoadingView(Context context) {
            super(context);
        }

        public LoadingView(Context context, int theme) {
            super(context, theme);
        }
        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            init(getContext());
        }
        private void init(Context context) {
            setCancelable(true);
            setCanceledOnTouchOutside(false);
            setContentView(R.layout.loading);
            WindowManager.LayoutParams params = getWindow().getAttributes();
            params.width = WindowManager.LayoutParams.WRAP_CONTENT;
            params.height = WindowManager.LayoutParams.WRAP_CONTENT;
            getWindow().setAttributes(params);
        }
        @Override
        public void show() {
            super.show();
        }
        @Override
        public void dismiss() {
            super.dismiss();
        }
    }

    public class ForecastAdapter extends ArrayAdapter<ForecastBase> {
        // View lookup cache
        private class ViewHolder {
            TextView date;
            TextView weather;
            TextView temperature;
            ImageView icon;
            TextView wind;
        }

        public ForecastAdapter(Context context, ArrayList<ForecastBase> forecastBases) {
            super(context, R.layout.item_forecast, forecastBases);
        }

        @SuppressLint("SetTextI18n")
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            // Get the data item for this position
            ForecastBase forecastBase = getItem(position);
            // Check if an existing view is being reused, otherwise inflate the view
            ViewHolder viewHolder; // view lookup cache stored in tag
            if (convertView == null) {
                // If there's no view to re-use, inflate a brand new view for row
                viewHolder = new ViewHolder();
                LayoutInflater inflater = LayoutInflater.from(getContext());
                convertView = inflater.inflate(R.layout.item_forecast, parent, false);
                viewHolder.date = convertView.findViewById(R.id.tvDate);
                viewHolder.weather = convertView.findViewById(R.id.tvWeather);
                viewHolder.temperature = convertView.findViewById(R.id.tvTemperature);
                viewHolder.icon = convertView.findViewById(R.id.tvIcon);
                viewHolder.wind = convertView.findViewById(R.id.tvWind);
                // Cache the viewHolder object inside the fresh view
                convertView.setTag(viewHolder);
            } else {
                // View is being recycled, retrieve the viewHolder object from tag
                viewHolder = (ViewHolder) convertView.getTag();
            }
            // Populate the data from the data object via the viewHolder object
            // into the template view.
            viewHolder.date.setText(forecastBase.getDate().substring(5));
            viewHolder.weather.setText(forecastBase.getCond_txt_d());
            viewHolder.temperature.setText(forecastBase.getTmp_min() + "℃ ~ " + forecastBase.getTmp_max() + "℃");
            int id = convertView.getResources().getIdentifier("icon" + forecastBase.getCond_code_d(), "drawable", "com.example.weather");
            viewHolder.icon.setImageResource(id);
            viewHolder.wind.setText(forecastBase.getWind_dir());
            // Return the completed view to render on screen
            return convertView;
        }
    }
}
