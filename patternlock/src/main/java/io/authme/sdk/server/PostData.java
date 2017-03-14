package io.authme.sdk.server;
/*
   Copyright 2017 Authme ID Services Pvt. Ltd.
   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at
     http://www.apache.org/licenses/LICENSE-2.0
   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
*/

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;

import com.squareup.okhttp.FormEncodingBuilder;
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

    class PostRequestExecutor extends AsyncTask<io.authme.sdk.server.PostRequest, String, String> {

        @Override
        protected String doInBackground(io.authme.sdk.server.PostRequest... uri) {


            RequestBody body = RequestBody.create(JSON, uri[0].getBody().getBytes());
            Request request = new Request.Builder()
                    .url(uri[0].getUrl())
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



    public void runGet(final String url) {
        AsyncTask<String, String, String> getRequestTest = new GetRequestTest();
        getRequestTest.execute(url);
    }

    void post(String url, String json) throws IOException {
        AsyncTask<io.authme.sdk.server.PostRequest, String, String> postRequestExecutor = new PostRequestExecutor();
        io.authme.sdk.server.PostRequest post = new io.authme.sdk.server.PostRequest(url, "POST");
        post.setBody(json);
        postRequestExecutor.execute(post);
    }

    public void postStatus(String url, String status) {
        RequestBody formBody = new FormEncodingBuilder()
                               .add("status", status)
                               .build();
        final Request request = new Request.Builder()
                .url(url)
                .post(formBody)
                .build();

        Runnable postrunner = new Runnable() {
            @Override
            public void run() {
                try {
                    Response response = client.newCall(request).execute();
                    Log.d("StatusResponse", response.body().string());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        };
        Thread t = new Thread(postrunner);
        t.start();

    }

    public void resetStatus(String url) {
        RequestBody formBody = new FormEncodingBuilder()
                .build();
        final Request request = new Request.Builder()
                .url(url)
                .post(formBody)
                .build();

        Runnable postrunner = new Runnable() {
            @Override
            public void run() {
                try {
                    Response response = client.newCall(request).execute();
                    Log.d("ResetResult", response.body().string());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        };
        Thread t = new Thread(postrunner);
        t.start();

    }

    public void getKeys() {
        new ExecuteGet().execute();
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
                    .url(Config.PROD_SERVER_URL + "key/new")
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
