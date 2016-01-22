package util;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import db.CoolweatherDB;
import model.City;
import model.County;
import model.Province;

/**
 * Created by caihua2300 on 15/12/2015.
 */
public class Utility {
    /*
    解析和处理服务器返回的省级数据
     */
    public static final String GETWEATHER= "http://op.juhe.cn/onebox/weather/query?dtype=json&key=6d7d3974b7d12efac480f7ae4522dc63&cityname=";
    public static final String GETLOCATION= "http://AreaData.api.juhe.cn/AreaHandler.ashx?action=getArea&key=6d7d3974b7d12efac480f7ae4522dc63&areaID=";
    public static final String GETPROVINCE="0";



    public synchronized static boolean handleProvincesResponse(CoolweatherDB coolweatherDB,String response){
        if(!TextUtils.isEmpty(response)){
            try{
                JSONObject obj= new JSONObject(response);
                response=obj.getString("str");
                JSONObject obj2=new JSONObject(response);
                response=obj2.getString("regions");

                JSONObject object=new JSONObject();
                JSONArray array=new JSONArray(response);
                for(int i=0;i<array.length();i++){
                    object=array.getJSONObject(i);
                    String provinceCode=object.getString("id");
                    String provinceName=object.getString("name");
                    Province p=new Province();
                    p.setProvinceCode(provinceCode);
                    p.setProvinceName(provinceName);
                    coolweatherDB.saveProvince(p);

                }
                return true;
            }catch(
                    Exception e
                    ){
                e.printStackTrace();
            }
        }

       return false;
    }
    /*
    解析处理返回来的市级数据
     */
    public synchronized static boolean handleCitiesResponse(CoolweatherDB coolweatherDB,String response,int parentId){
        if(!TextUtils.isEmpty(response)){
            try{
                JSONObject obj= new JSONObject(response);
                response=obj.getString("str");
                JSONObject obj2=new JSONObject(response);
                JSONObject object=new JSONObject();
                response=obj2.getString("regions");
                JSONArray array=new JSONArray(response);
                Log.d("Cityresponse",array.toString());
                for(int i=0;i<array.length();i++){
                    object=array.getJSONObject(i);
                    Log.d("object",object.toString());
                    String CityCode=object.getString("id");
                    String CityName=object.getString("name");
                    City city=new City();
                    city.setCityCode(CityCode);
                    city.setCityName(CityName);
                    city.setProvinceId(parentId);
                    coolweatherDB.saveCity(city);
                    Log.d("CAT",city.toString());
                    //return true;


                }
                return true;
            }catch(Exception e){
                e.printStackTrace();
            }

        }
        return false;
    }
    /*
    解析和处理返回来的县级数据
     */
    public synchronized static boolean handleCountiesResponse(CoolweatherDB coolweatherDB,String response,int parentId){
        if(!TextUtils.isEmpty(response)){
            try{

                JSONObject obj= new JSONObject(response);
                response=obj.getString("str");
                JSONObject obj2=new JSONObject(response);
                JSONObject object=new JSONObject();
                response=obj2.getString("regions");
                JSONArray array=new JSONArray(response);
                Log.d("Countyresponse",array.toString());
                for(int i=0;i<array.length();i++){
                    object=array.getJSONObject(i);
                    String CountyCode=object.getString("id");
                    String CountyName=object.getString("name");
                    County county=new County();
                    county.setCountyCode(CountyCode);
                    county.setCountyName(CountyName);
                    county.setCityId(parentId);
                    coolweatherDB.saveCounty(county);
                    //return true;
                }
                return true;
            }catch(Exception e){

            }

        }
        return false;
    }
    /*
    解析处理器返回的JSON数据，并将解析出的数据保存到本地
     */
    public static void handleWeatherResponse(Context context,String response){
        try{
            JSONObject jsonObject=new JSONObject(response);
            String obj=jsonObject.getString("result");
            JSONObject jsonObject1 =new JSONObject(obj);
            String obj2=jsonObject1.getString("data");
            JSONObject jsonObject2=new JSONObject(obj2);
            String obj3=jsonObject2.getString("realtime");
            JSONObject jsonObject3=new JSONObject(obj3);
            String obj4=jsonObject3.getString("weather");
            JSONObject jsonObject4=new JSONObject(obj4);
            String temp =jsonObject4.getString("temperature");
            String cityName=jsonObject3.getString("city_name");
            String desc=jsonObject4.getString("info");
            String imgNum;
            if(jsonObject4.getString("img").length()==1){
                imgNum="0"+jsonObject4.getString("img");
            }else{
                imgNum=jsonObject4.getString("img");
            }
            String publishTime=jsonObject3.getString("time");
            saveWeatherInfo(context,cityName,temp,desc,imgNum,publishTime);




        }catch(Exception e){
            e.printStackTrace();
        }

    }
    public static void saveWeatherInfo(Context context,String cityName,String temp,String desc,String imgNum,String publishTime){
        SimpleDateFormat sdf=new SimpleDateFormat("yyyy-mm-dd", Locale.CHINA);
        SharedPreferences.Editor editor= PreferenceManager.getDefaultSharedPreferences(context).edit();
        editor.putBoolean("city_selected",true);
        editor.putString("city_name", cityName);
        editor.putString("temp", temp);
        editor.putString("imgNum", "d"+imgNum);
        editor.putString("weather_desp", desc);
        editor.putString("publish_time", publishTime);
        editor.putString("current_date", sdf.format(new Date()));
        editor.commit();


    }

}
