package com.example.weather;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.lang.ref.WeakReference;
import java.util.ArrayList;

import interfaces.heweather.com.interfacesmodule.bean.Code;
import interfaces.heweather.com.interfacesmodule.bean.Lang;
import interfaces.heweather.com.interfacesmodule.bean.basic.Basic;
import interfaces.heweather.com.interfacesmodule.bean.search.Search;
import interfaces.heweather.com.interfacesmodule.view.HeConfig;
import interfaces.heweather.com.interfacesmodule.view.HeWeather;

public class AddActivity extends AppCompatActivity {
    private TextView cityNameText;
    private ImageView searchIV;
    private ListView candidatesTV;
    private ArrayList<Basic> info;
    private CandidateAdapter candidateAdapter;
    private UIHandler uiHandler;
    private MainActivity.LoadingView loadingView;
    private String cityName;
    private String id = "HE2004101743231681";
    private String key = "3930614036664920b938cb85206f363b";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add);
        HeConfig.init(id, key);
        HeConfig.switchToFreeServerNode();

        cityNameText = findViewById(R.id.cityNameText);
        searchIV = findViewById(R.id.searchImageView);
        info = new ArrayList<>();
        candidateAdapter = new CandidateAdapter(this, info);
        candidatesTV = findViewById(R.id.candidateListView);
        candidatesTV.setAdapter(candidateAdapter);
        loadingView = new MainActivity.LoadingView(this, R.style.CustomDialog);
        uiHandler = new UIHandler(this);

        searchIV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                cityName = cityNameText.getText().toString().trim().toLowerCase();
                if (!cityName.isEmpty()) {
                    candidateAdapter.clear();
                    searchCity(cityName);
                } else {
                    Toast.makeText(AddActivity.this, "无效的输入!", Toast.LENGTH_SHORT).show();
                }
            }
        });

        candidatesTV.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Basic basic = (Basic)candidatesTV.getItemAtPosition(position);
                Intent intent = new Intent(AddActivity.this, CityActivity.class);
                intent.putExtra("cityName", basic.getLocation());
                setResult(RESULT_OK, intent);
                finish();
            }
        });
    }

    public void searchCity(String location) {
        loadingView.show();
        HeWeather.getSearch(AddActivity.this, location, "cn", 5, Lang.CHINESE_SIMPLIFIED, new HeWeather.OnResultSearchBeansListener() {
            @Override
            public void onError(Throwable throwable) {
                Log.i("City_Search", "City Search onError: ", throwable);
                loadingView.dismiss();
                Toast.makeText(AddActivity.this, "网络错误，请重试！", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onSuccess(Search search) {
                Message msg = Message.obtain();
                if (Code.OK.getCode().equalsIgnoreCase(search.getStatus())) {
                    msg.what = 1;
                    msg.obj = search.getBasic();
                    Log.d("Search", search.getBasic().toString());
                } else {
                    msg.what = 0;
                }
                uiHandler.sendMessage(msg);
            }
        });
    }

    private class UIHandler extends Handler {
        private final WeakReference<Activity> mActivityReference;

        UIHandler(Activity activity) {
            this.mActivityReference = new WeakReference<Activity>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            AddActivity activity = (AddActivity) mActivityReference.get();
            activity.loadingView.dismiss();
            if (msg.what == 1) {
                activity.candidateAdapter.addAll((ArrayList<Basic>)msg.obj);
            } else {
                Toast.makeText(AddActivity.this, "未找到任何相关城市!", Toast.LENGTH_SHORT).show();
                activity.cityNameText.setText("");
            }
        }
    }

    public class CandidateAdapter extends ArrayAdapter<Basic> {
        // View lookup cache
        private class ViewHolder {
            TextView location;
            TextView country;
            TextView area;
            TextView lonlat;
        }

        public CandidateAdapter(Context context, ArrayList<Basic> candidates) {
            super(context, R.layout.item_candidate, candidates);
        }

        @SuppressLint("SetTextI18n")
        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            // Get the data item for this position
            final Basic basic = getItem(position);
            // Check if an existing view is being reused, otherwise inflate the view
            final AddActivity.CandidateAdapter.ViewHolder viewHolder; // view lookup cache stored in tag
            if (convertView == null) {
                // If there's no view to re-use, inflate a brand new view for row
                viewHolder = new AddActivity.CandidateAdapter.ViewHolder();
                LayoutInflater inflater = LayoutInflater.from(getContext());
                convertView = inflater.inflate(R.layout.item_candidate, parent, false);
                viewHolder.location = convertView.findViewById(R.id.locationTextView);
                viewHolder.country = convertView.findViewById(R.id.countryTextView);
                viewHolder.area = convertView.findViewById(R.id.areaTextView);
                viewHolder.lonlat = convertView.findViewById(R.id.lonlatTextView);
                // Cache the viewHolder object inside the fresh view
                convertView.setTag(viewHolder);
            } else {
                // View is being recycled, retrieve the viewHolder object from tag
                viewHolder = (AddActivity.CandidateAdapter.ViewHolder) convertView.getTag();
            }
            // Populate the data from the data object via the viewHolder object
            // into the template view.
            viewHolder.location.setText(basic.getLocation());
            viewHolder.country.setText(basic.getCnty());
            viewHolder.area.setText(basic.getAdmin_area());
            viewHolder.lonlat.setText("Lon: " + basic.getLon().substring(0, 5) + "/Lat: " + basic.getLat().substring(0, 5));
            // Return the completed view to render on screen
            return convertView;
        }
    }
}
