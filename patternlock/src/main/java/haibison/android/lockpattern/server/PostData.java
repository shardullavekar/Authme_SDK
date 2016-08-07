package haibison.android.lockpattern.server;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;

import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.concurrent.TimeUnit;

import haibison.android.lockpattern.Config;


public class PostData {
    public static final MediaType JSON
            = MediaType.parse("application/json; charset=utf-8");
    private static final String AUTHMEIO = "AUTHMEIO";

    OkHttpClient client;
    private static Handler handler = new Handler();
    Runnable postrunner;
    Callback callback;
    JSONObject keys;
    Activity activity;

    public PostData(Callback callback) {
        this.callback = callback;
        client = new OkHttpClient();
        client.setConnectTimeout(5, TimeUnit.MINUTES);
        client.setReadTimeout(5, TimeUnit.MINUTES);
        client.setWriteTimeout(5, TimeUnit.MINUTES);
    }

    public PostData(Callback callback, Activity activity) {
        this(callback);
        this.activity = activity;
    }

    public void runPost(final String url, final String json) throws IOException {
        post(url, json);
    }
    class GetRequestTest extends AsyncTask<String, String, String> {

        @Override
        protected String doInBackground(String... uri) {

            URLConnection connection;
            String content;
            InputStream s;
            try {
                connection = new URL(uri[0]).openConnection();
                connection.connect();
                s = (InputStream) connection.getContent();
            } catch (IOException e) {
                Log.e(AUTHMEIO, "Failed to get content : " + uri[0], e);
                return "";
            }
            return InputSteamToString(s);
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            callback.onTaskExecuted(s);
        }
    }

    public String InputSteamToString(InputStream inputStream) {
        BufferedReader r = new BufferedReader(new InputStreamReader(inputStream));
        StringBuilder total = new StringBuilder();
        String line;
        try {
            while ((line = r.readLine()) != null) {
                total.append(line);
            }
            return total.toString();
        } catch (IOException e) {
            Log.e(AUTHMEIO, "Failed to read input stream: ", e);
            return null;
        }

    }

    class PostRequestExecutor extends AsyncTask<haibison.android.lockpattern.server.PostRequest, String, String> {

        @Override
        protected String doInBackground(haibison.android.lockpattern.server.PostRequest... uri) {

            Config config = new Config();
            RequestBody body = RequestBody.create(JSON, uri[0].getBody().getBytes());
            Request request = new Request.Builder()
                    .url(uri[0].getUrl())
                    .addHeader("X-API-KEY", config.getApi_key())
                    .post(body)
                    .build();
            Response response = null;
            try {
                response = client.newCall(request).execute();
            } catch (IOException e) {
                Log.e(AUTHMEIO, "Failed to execute post: ", e);
                return "";
            }
            try {
                return response.body().string();
            } catch (IOException e) {
                Log.e(AUTHMEIO, "Failed to get body post: ", e);
                return "";
            }
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            callback.onTaskExecuted(s);
        }
    }

    void post(String url, String json) throws IOException {
        AsyncTask<PostRequest, String, String> postRequestExecutor = new PostRequestExecutor();
        PostRequest post = new PostRequest(url, "POST");
        post.setBody(json);
        postRequestExecutor.execute(post);
    }

    private class ExecuteGet extends AsyncTask<Void, Void, String> {
        ProgressDialog progressDialog;
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog = new ProgressDialog(activity);
            progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            progressDialog.setTitle("Creating Account");
            progressDialog.setMessage("Generating your secret keys.. This usually takes about 3-5 minutes");
            progressDialog.show();
        }
        @Override
        protected String doInBackground(Void... params) {
            Request request = new Request.Builder()
                    .url(Config.SERVER_URL + "key/new")
                    .build();
            Response response = null;
            try {
                response = client.newCall(request).execute();
            } catch (IOException e) {
                e.printStackTrace();
                return "ioException";
            }
            try {
                keys = new JSONObject(response.body().string());
            } catch (JSONException e) {
                e.printStackTrace();
                return "jsonException";
            } catch (IOException e) {
                e.printStackTrace();
                return "ioException";
            }

            return "ok";
        }

        @Override
        protected void onPostExecute(String s) {
            progressDialog.dismiss();
            if (TextUtils.equals(s, "ok")) {
                callback.onTaskExecuted(keys.toString());
            }
            else {
                callback.onTaskExecuted("error");
            }
        }
    }
}
