package hua.coolweather;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import service.AutoUpdateService;
import util.CityNameUtil;
import util.HttpCallbackListener;
import util.HttpUtil;
import util.Utility;


public class WeatherActivity extends ActionBarActivity implements View.OnClickListener{
    public static final String GETWEATHER= "http://op.juhe.cn/onebox/weather/query?dtype=json&key=b33bbe2433b1e559f980de073bb66fae&cityname=";
    private LinearLayout weatherInfoLayout;
    private TextView cityName;


    private TextView publishText;

    private TextView weatherDespText;

    private TextView tempText;

    private TextView currentDateText;
    /*
    切换城市按钮
     */

    private Button switchCity;
    /*
    更新天气按钮
     */


    private Button refreshWeather;


    private ImageView currentImg;



    private String selectedProvinceName;


    private String selectedCityName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState);

        setContentView(R.layout.weather_layout);
        Intent intent = getIntent();
        selectedCityName = intent.getStringExtra("selectedCity");
        selectedProvinceName = intent.getStringExtra("selectedProvince");
        weatherInfoLayout=(LinearLayout)findViewById(R.id.weather_info_layout);
        cityName=(TextView)findViewById(R.id.city_name);
        publishText=(TextView)findViewById(R.id.publish_text);
        weatherDespText=(TextView)findViewById(R.id.weather_desp);
        tempText=(TextView)findViewById(R.id.current_temp);
        currentDateText=(TextView)findViewById(R.id.current_date);
        switchCity=(Button)findViewById(R.id.switch_city);
        refreshWeather=(Button)findViewById(R.id.refresh_weather);
        switchCity.setOnClickListener(this);
        refreshWeather.setOnClickListener(this);


        String countyName=getIntent().getStringExtra("county_name");
        //Log.d("weatherActivity-1",countyName);
        if(!TextUtils.isEmpty(countyName)){
            publishText.setText("synconrizing...");
            weatherInfoLayout.setVisibility(View.INVISIBLE);
            cityName.setVisibility(View.INVISIBLE);
            queryWeatherInfo(countyName);

        }else{
            showWeather();
        }


    }
   /*
   根据县级名称获得查询名称
    */
    /*
    遇见区返回上一级，街道返回上一级菜单
     */


    private void queryWeatherInfo(String countyName){
       String selectedCity = CityNameUtil.SelectCity(countyName, selectedCityName, selectedProvinceName);
        Log.d("queryWeatherInfo",countyName+"countyName:");
        Log.d("queryWeatherInfo",selectedCityName+"selectedCityName:");
        Log.d("queryWeatherInfo",selectedProvinceName+"selectedProvinceName:");
        String address=GETWEATHER+selectedCity;
        Log.d("weatheraddress:",address);
        queryFromServer(address,countyName);


    }
    private void queryFromServer(final String address,final String type){
        HttpUtil.sendHttpRequest(address,new HttpCallbackListener() {
            @Override
            public void onFinish(String response) {
                if(!TextUtils.isEmpty(response)){
                    Utility.handleWeatherResponse(WeatherActivity.this,response);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            showWeather();
                        }
                    });
                }
            }

            @Override
            public void onError(Exception e) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        publishText.setText("synchronizing error");
                    }
                });
            }
        });

    }
    private void showWeather(){
        SharedPreferences preferences= PreferenceManager.getDefaultSharedPreferences(this);
        cityName.setText(preferences.getString("city_name",""));
        tempText.setText(preferences.getString("temp", ""));
        weatherDespText.setText(preferences.getString("weather_desp", ""));
        publishText.setText("今天" + preferences.getString("publish_time", "") + "发布");
        currentDateText.setText(preferences.getString("current_date", ""));
        Resources res=getResources();
        int img_id=res.getIdentifier(preferences.getString("imgNum", "d00"), "drawable", getPackageName());
//        currentImg.setImageResource(img_id);
        weatherInfoLayout.setVisibility(View.VISIBLE);
        cityName.setVisibility(View.VISIBLE);
        Intent intent=new Intent(this, AutoUpdateService.class);
        startService(intent);

    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_weather, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.switch_city:
                Intent intent=new Intent(this,MainActivity.class);
                intent.putExtra("from_weather_activity",true);
                startActivity(intent);
                finish();
                break;
            case R.id.refresh_weather:
                publishText.setText("synchronizing...");
                SharedPreferences preferences=PreferenceManager.getDefaultSharedPreferences(this);
                String weatherName=preferences.getString("city_name","");
                Log.d("onClick:refresh",weatherName);
                if(!TextUtils.isEmpty(weatherName)){
                    queryWeatherInfo(weatherName);
                }
                break;
            default:
                break;

        }
    }
}
