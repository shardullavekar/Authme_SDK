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

import android.os.AsyncTask;
import android.util.Log;

import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;

import java.io.IOException;
import java.util.concurrent.TimeUnit;


public class PostData {
    public static final MediaType JSON
            = MediaType.parse("application/json; charset=utf-8");
    private static final String AUTHMEIO = "AUTHMEIO";
    private static final String API_KEY_HEADER = "X-Api-Key";

    OkHttpClient client;
    Callback callback;
    String apiKey;
    public PostData(Callback callback, String apiKey) {
        this.callback = callback;
        client = new OkHttpClient();
        client.setConnectTimeout(5, TimeUnit.MINUTES);
        client.setReadTimeout(5, TimeUnit.MINUTES);
        client.setWriteTimeout(5, TimeUnit.MINUTES);
        this.apiKey = apiKey;
    }

    public void runPost(final String url, final String json) throws IOException {
        post(url, json);
    }

    class PostRequestExecutor extends AsyncTask<io.authme.sdk.server.PostRequest, String, String> {

        @Override
        protected String doInBackground(io.authme.sdk.server.PostRequest... uri) {


            RequestBody body = RequestBody.create(JSON, uri[0].getBody().getBytes());
            Request request = new Request.Builder()
                    .url(uri[0].getUrl())
                    .addHeader(API_KEY_HEADER, apiKey)
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
        AsyncTask<io.authme.sdk.server.PostRequest, String, String> postRequestExecutor = new PostRequestExecutor();
        io.authme.sdk.server.PostRequest post = new io.authme.sdk.server.PostRequest(url, "POST");
        post.setBody(json);
        postRequestExecutor.execute(post);
    }

}
