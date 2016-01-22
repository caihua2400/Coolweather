package db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import model.City;
import model.County;
import model.Province;

/**
 * Created by caihua2300 on 12/12/2015.
 */
public class CoolweatherDB {
    /*
    数据库名
     */
    public static final String DB_NAME="cool_weather";
    public static final int VERSION=1;
    private static CoolweatherDB coolweatherDB;
    private SQLiteDatabase db;
    public static final String query="select * from City where province_id= ";
    private CoolweatherDB(Context context){
        CoolWeatherOpenHelper dbHelper=new CoolWeatherOpenHelper(context,DB_NAME,null,VERSION);
        db=dbHelper.getWritableDatabase();

    }
    /*
    获取CoolwheraterDB的实例
     */
    public synchronized static CoolweatherDB getInstance(Context context){
        if(coolweatherDB==null){
            coolweatherDB=new CoolweatherDB(context);
        }
        return coolweatherDB;

    }
    /*
    将province实例存储到数据库
     */
     public void saveProvince(Province province){
         if(province!=null){
             ContentValues values=new ContentValues();
             values.put("province_name",province.getProvinceName());
             values.put("province_code",province.getProvinceCode());
             db.insert("Province",null,values);
         }

     }
    /*
    从数据库读取全国所有省份信息
     */
    public List<Province> loadProvinces(){
        List<Province> list=new ArrayList<Province>();
        Cursor cursor=db.query("Province",null,null,null,null,null,null);
        if(cursor.moveToNext()){
            do{
                Province province=new Province();
                province.setId(cursor.getInt(cursor.getColumnIndex("id")));
                province.setProvinceName(cursor.getString(cursor.getColumnIndex("province_name")));
                province.setProvinceCode(cursor.getString(cursor.getColumnIndex("province_code")));
                list.add(province);
            }while (cursor.moveToNext());


        }
        if(cursor!=null){
            cursor.close();
        }
        return list;
    }
    /*
    将City实例储存到数据库
     */
    public void saveCity(City city){
        if(city!=null){
            ContentValues values=new ContentValues();
            values.put("city_name",city.getCityName());
            values.put("city_code",city.getCityCode());
            values.put("province_id",city.getProvinceId());
            db.insert("City",null,values);
        }

    }
    /*
    从数据库哪里读取一个省所有的城市信息
     */
    public List<City> loadCities(int provinceId){
        List<City> list=new ArrayList<City>();
        Cursor cursor=db.query("City",null, "province_id= ?",new String[]{ String.valueOf(provinceId) }, null, null, null);

        Log.d("cursor size:",cursor.getCount()+" tt");
        //Log.d("cursorinload:",cursor.toString());
        if(cursor.moveToNext()){
            do{
                City city=new City();

                city.setId(cursor.getInt(cursor.getColumnIndex("id")));
                Log.d("cityid:",city.getId()+"");
                city.setCityName(cursor.getString(cursor.getColumnIndex("city_name")));
                city.setCityCode(cursor.getString(cursor.getColumnIndex("city_code")));
                city.setProvinceId(provinceId);
                list.add(city);
            }while(cursor.moveToNext());

        }
        if(cursor!=null){
            cursor.close();
        }
        Log.d("listinload:",list.toString());
        return  list;


    }
    /*
    将County实例存储到数据库
     */
    public void saveCounty(County county){
        if(county!=null){
            ContentValues values=new ContentValues();
            values.put("county_name",county.getCountyName());
            values.put("county_code",county.getCountyCode());
            values.put("city_id",county.getCityId());
            db.insert("County",null,values);
        }

    }
    /*
    从数据库读取某城市下所有的县信息
     */
    public List<County> loadCounties(int cityId){
        List<County> list=new ArrayList<County>();
        Cursor cursor=db.query("County",null,"city_id= ?",new String[]{String.valueOf(cityId)},null,null,null);
        Log.d("cursorinload:",cursor.toString());
        Log.d("cityId",String.valueOf(cityId));
        if(cursor.moveToNext()){
            do{
                County county=new County();
                county.setId(cursor.getInt(cursor.getColumnIndex("id")));
                county.setCountyName(cursor.getString(cursor.getColumnIndex("county_name")));
                county.setCountyCode(cursor.getString(cursor.getColumnIndex("county_code")));
                county.setCityId(cityId);
                list.add(county);
            }while(cursor.moveToNext());
        }
        if(cursor!=null){
            cursor.close();
        }
        return list;
    }
}
