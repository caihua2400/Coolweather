package util;

import android.os.Handler;
import android.os.Message;
import android.util.Log;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by caihua2300 on 15/12/2015.
 */
public class HttpUtil {
    static String result;


    public static void sendHttpRequest(final String address,final HttpCallbackListener listener){

        new Thread(
                new Runnable() {
                    @Override
                    public void run() {
                        Log.d("HttpUtil21","success");
                        HttpURLConnection connection=null;
                        try{
                            URL url=new URL(address);
                            connection=(HttpURLConnection)url.openConnection();
                            connection.setRequestMethod("GET");
                            connection.setConnectTimeout(8000);
                            connection.setReadTimeout(8000);
                            InputStream in=connection.getInputStream();
                            BufferedReader reader=new BufferedReader(new InputStreamReader(in));
                            StringBuilder response=new StringBuilder();
                            String line;
                            while((line=reader.readLine())!=null){
                                response.append(line);
                                line.toString();
                                Log.d("line: ",line);
                            }
                            if(listener!=null){
                                Log.d("response:",response.toString());
                                listener.onFinish(response.toString());
                            }
                            result=response.toString();






                        }catch (Exception e){

                            if(listener!=null){
                                listener.onError(e);
                            }
                        }finally {
                            if(connection!=null){
                                connection.disconnect();
                            }
                        }
                    }
                }
        ).start();

//       Log.d("result out thread",result);

    }
}
