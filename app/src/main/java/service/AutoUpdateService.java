package service;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.os.SystemClock;
import android.preference.PreferenceManager;

import receiver.AutoUpdateReceiver;
import util.CityNameUtil;
import util.HttpCallbackListener;
import util.HttpUtil;
import util.Utility;

public class AutoUpdateService extends Service {
    public static final String GETWEATHER= "http://op.juhe.cn/onebox/weather/query?dtype=json&key=b33bbe2433b1e559f980de073bb66fae&cityname=";
    public AutoUpdateService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }
    public int onStartCommand(Intent i,int flags,int startId){
        new Thread(new Runnable() {
            @Override
            public void run() {
                updateWeather();
            }
        }).start();
        AlarmManager manager=(AlarmManager)getSystemService(ALARM_SERVICE);
        int anHour=8*60*60*1000;
        long triggerTime= SystemClock.elapsedRealtime()+anHour;
        Intent intent=new Intent(this, AutoUpdateReceiver.class);
        PendingIntent pi=PendingIntent.getBroadcast(this,0,intent,0);
        manager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP,triggerTime,pi);

        return super.onStartCommand(i,flags,startId);

    }
    private void updateWeather(){
        SharedPreferences preferences= PreferenceManager.getDefaultSharedPreferences(this);
        String weatherCityName=preferences.getString("city_name","");
        String selectedCity= CityNameUtil.SelectCity(weatherCityName,null,null);
        String address=GETWEATHER+selectedCity;
        HttpUtil.sendHttpRequest(address,new HttpCallbackListener() {
            @Override
            public void onFinish(String response) {
                Utility.handleWeatherResponse(AutoUpdateService.this,response);
            }

            @Override
            public void onError(Exception e) {
                e.printStackTrace();
            }
        });



    }
}
