package util;

/**
 * Created by caihua2300 on 24/12/2015.
 */
public interface HttpCallbackListener {
    void onFinish(String response);
    void onError(Exception e);
}
