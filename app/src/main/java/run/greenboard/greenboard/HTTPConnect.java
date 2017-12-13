package run.greenboard.greenboard;

import android.os.StrictMode;

import java.io.IOException;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class HTTPConnect {
    OkHttpClient client;
    public HTTPConnect()
    {
        //function used to call synchronus tasks to the database by passing through the URL end point with parameters already in it
        client = new OkHttpClient();
        int SDK_INT = android.os.Build.VERSION.SDK_INT;
         if (SDK_INT > 8){
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
    }
}

    public String getSynchronusRequest(String url)
    {
        String responseBody = null;
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder().url(url).get().build();

        Response response = null;
        try {
            response = client.newCall(request).execute();
            responseBody = response.body().string();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return responseBody;
    }
}
