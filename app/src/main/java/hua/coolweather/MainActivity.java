package hua.coolweather;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.StrictMode;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import db.CoolweatherDB;
import model.City;
import model.County;
import model.Province;
import util.HttpCallbackListener;
import util.Utility;
import util.HttpUtil;


public class MainActivity extends ActionBarActivity {
    public static final String GETWEATHER= "http://op.juhe.cn/onebox/weather/query?dtype=json&key=6d7d3974b7d12efac480f7ae4522dc63&cityname=";
    public static final String GETLOCATION= "http://AreaData.api.juhe.cn/AreaHandler.ashx?action=getArea&key=6d7d3974b7d12efac480f7ae4522dc63&areaID=";
    public static final String GETPROVINCE="0";

    public static final int LEVEL_PROVINCE=0;
    public static final int LEVEL_CITY=1;
    public static final int LEVEL_COUNTY=2;
    private ProgressDialog progressDialog;
    private TextView titleText;
    private ListView listView;
    private ArrayAdapter<String> adapter;
    private CoolweatherDB coolweatherDB;
    private List<String> dataList=new ArrayList<String>();
    /*
    省列表
     */
    private List<Province> provinceList;
    /*
    市列表
     */
    private List<City> cityList;
    /*
    县列表
     */
    private List<County> countyList;
    /*
    选中的省份
     */
    private Province selectedProvince;
    /*
    选中的城市
     */
    private City selectedCity;
    /*
    选中的级别
     */
    private int currentLevel;
    private boolean result;
    /*
    是否从weatherActivity中跳转过来。
     */
    private boolean isFromWeatherActivity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState);


        setContentView(R.layout.choose_area);
        isFromWeatherActivity = getIntent().getBooleanExtra("from_weather_activity", false);
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        if(prefs.getBoolean("city_selected", false)&& !isFromWeatherActivity){
            Intent intent = new Intent(this,WeatherActivity.class);
            startActivity(intent);
            finish();
            return;
        }
        //requestWindowFeature(Window.FEATURE_NO_TITLE);
        listView=(ListView)findViewById(R.id.list_view);
        titleText=(TextView)findViewById(R.id.title_text);
        adapter=new ArrayAdapter<String>(MainActivity.this,android.R.layout.simple_list_item_1,dataList);
        listView.setAdapter(adapter);
        coolweatherDB=CoolweatherDB.getInstance(this);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if(currentLevel==LEVEL_PROVINCE){
                    selectedProvince=provinceList.get(position);
                    queryCities();
                }else if(currentLevel==LEVEL_CITY){
                    selectedCity=cityList.get(position);
                    queryCounties();
                }else if(currentLevel==LEVEL_COUNTY){
                    String countyName=countyList.get(position).getCountyName();
                    Intent intent=new Intent(MainActivity.this,WeatherActivity.class);
                    intent.putExtra("county_name", countyName);
                    Log.d("countyName",countyName);
                    intent.putExtra("selectedCity", selectedCity.getCityName());
                    intent.putExtra("selectedProvince", selectedProvince.getProvinceName());
                    startActivity(intent);
                    finish();
                    return;
                }
            }
        });
        queryProvinces();
    }

    /*
    查询全国所有的省，优先从数据库查起，如果没有再到服务器上去查询
     */
    private void queryProvinces(){
        provinceList=coolweatherDB.loadProvinces();
        if(provinceList.size()>0){
            dataList.clear();
            for(Province p:provinceList){
                dataList.add(p.getProvinceName());
            }
            adapter.notifyDataSetChanged();
            listView.setSelection(0);
            titleText.setText("China");
            currentLevel=LEVEL_PROVINCE;

        }else{
            queryFromServer("0","province");
            Log.d("Main113","success");
        }
    }
    /*
    查询全省所有的市，优先从数据库查起，如果没有再到服务器上去查询。
     */
    private void queryCities(){
        cityList=coolweatherDB.loadCities(Integer.parseInt(selectedProvince.getProvinceCode()));
        Log.d("provinceCode",selectedProvince.getProvinceCode());
        Log.d("citylist",cityList.toString());
        if(cityList.size()>0){
            dataList.clear();
            for(City city: cityList){
                dataList.add(city.getCityName());
            }
            adapter.notifyDataSetChanged();
            listView.setSelection(0);
            titleText.setText(selectedProvince.getProvinceName());
            currentLevel = LEVEL_CITY;

        }else{
            queryFromServer(selectedProvince.getProvinceCode(),"city");
        }

    }
    /*
    查询全市所有的县，优先从数据库查起，如果没有再到服务器上去查询
     */
    private void queryCounties(){
        countyList = coolweatherDB.loadCounties(Integer.parseInt(selectedCity.getCityCode()));
        if(countyList.size()>0){
            dataList.clear();
            for(County c : countyList){
                dataList.add(c.getCountyName());
            }
            adapter.notifyDataSetChanged();
            listView.setSelection(0);
            titleText.setText(selectedCity.getCityName());
            currentLevel = LEVEL_COUNTY;
        }else{
            queryFromServer(selectedCity.getCityCode(),"county");
        }
    }

    private void queryFromServer(final String code,final String type){
        String address=null;
        //The core part for this project
        //String jsonData = Utility.getInfoBylocCode(code);
        //Log.d("MainActivity158",jsonData);
        if(!TextUtils.isEmpty(code)){
            address=GETLOCATION+code;
        }
        HttpUtil.sendHttpRequest(address,new HttpCallbackListener() {
            @Override
            public void onFinish(String response) {
               boolean result=false;
                if("province".equals(type)){
                    result=Utility.handleProvincesResponse(coolweatherDB,response);
                }else if("city".equals(type)){
                    //Log.d("cityresponse",response.toString());
                    result=Utility.handleCitiesResponse(coolweatherDB,response,Integer.parseInt(selectedProvince.getProvinceCode()));
                }else if("county".equals(type)){

                    result=Utility.handleCountiesResponse(coolweatherDB,response,Integer.parseInt(selectedCity.getCityCode()));
                }
                if(result){
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if("province".equals(type)){
                                queryProvinces();
                            }else if("city".equals(type)){
                                queryCities();
                            }else if("county".equals(type)){
                                queryCounties();
                            }
                        }
                    });
                }
            }

            @Override
            public void onError(Exception e) {
              runOnUiThread(new Runnable() {
                  @Override
                  public void run() {
                      Toast.makeText(MainActivity.this," failed",Toast.LENGTH_SHORT).show();
                  }
              });
            }
        });



    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
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
    public void onBackPressed() {
        if(currentLevel==LEVEL_COUNTY){
            queryCities();
        }else if(currentLevel==LEVEL_CITY){
            queryProvinces();
        }else{
            if(isFromWeatherActivity){
                Intent intent = new Intent(this, WeatherActivity.class);
                startActivity(intent);
            }
            finish();
        }
    }
}
