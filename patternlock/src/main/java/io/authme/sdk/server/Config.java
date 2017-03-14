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
import android.content.SharedPreferences;

public class Config {

    public static final String PROD_HOST = "http://authme.io";
    public static final String PROD_SERVER_URL = PROD_HOST + ":3000/";

    public static final String SANDBOX_HOST = "";
    public static final String SANDBOX_SERVER_URL = SANDBOX_HOST + ":3000/";

    private static final String STORED_VALUES = "STORED_VALUES";
    private static final String API_KEY = "API_KEY", BYTE_ARRAY = "BYTE_ARRAY", USER_ID = "USERID";
    private SharedPreferences userpreference;
    private SharedPreferences.Editor editor;
    private Activity activity;

    public Config(Activity activity) {
        this.activity = activity;
        userpreference = this.activity.getSharedPreferences(STORED_VALUES, 0);
        editor = userpreference.edit();
    }

    public void setAPIKey(String key) {
        editor.putString(API_KEY, key);
        editor.apply();
        editor.commit();
    }

    public String getApiKey() {
        return userpreference.getString(API_KEY, null);
    }

    public void setByteArray(char[] pattern) {
        String arrayString = new String(pattern);
        editor.putString(BYTE_ARRAY, arrayString);
        editor.apply();
        editor.commit();
    }

    public String getPatternString() {
        String stringArray = userpreference.getString(BYTE_ARRAY, null);
        return stringArray;
    }

    public void setUserId(String userId) {
        editor.putString(USER_ID, userId);
        editor.apply();
        editor.commit();
    }

    public String getUserId() {
        return userpreference.getString(USER_ID, null);
    }

}