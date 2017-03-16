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
import android.text.TextUtils;
import android.widget.Toast;

import org.jboss.aerogear.security.otp.Totp;

import java.util.UUID;

public class Config {
    public static final int LOGIN_PATTERN = 2, SIGNUP_PATTERN = 1,
            INVALID_CONFIG = 3, RESULT_FAILED = 5, RESET_PATTERN = 6;

    public static final String PROD_HOST = "https://api.authme.authme.host/";
    public static final String PROD_SERVER_URL = PROD_HOST;

    public static final String SANDBOX_HOST = "https://api.authme.authme.xyz/";
    public static final String SANDBOX_SERVER_URL = SANDBOX_HOST;

    public static final String STORED_VALUES = "STORED_VALUES";
    public static final String API_KEY = "API_KEY",
            BYTE_ARRAY = "BYTE_ARRAY", EMAIL= "emailId",
            DEVICE_ID = "deviceId", ENVIRONMENT = "environment",
            PRODUCTION = "PROD", SANDBOX = "TEST",
            SECRET_KEY = "secretkey";
    private SharedPreferences userpreference;
    private SharedPreferences.Editor editor;
    private Activity activity;
    private String emailId, deviceId;


    public String getEmailId() {
        return userpreference.getString(EMAIL, null);
    }

    public void setEmailId(String emailId) {
        this.emailId = emailId;
        editor.putString(EMAIL, emailId);
        editor.apply();
        editor.commit();
    }

    public String getDeviceId() {
        String deviceId = userpreference.getString(DEVICE_ID, null);
        if (TextUtils.isEmpty(deviceId)) {
            return String.valueOf(UUID.randomUUID());
        }
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
        editor.putString(DEVICE_ID, deviceId);
        editor.apply();
        editor.commit();
    }

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

    public void setEnvironment(String environment) {
        editor.putString(ENVIRONMENT, environment);
        editor.apply();
        editor.commit();
    }

    public String getServerURL() {
        String environment = userpreference.getString(ENVIRONMENT, null);

        if (TextUtils.equals(environment, PRODUCTION)) {
            return PROD_SERVER_URL;
        }
        else {
            return SANDBOX_SERVER_URL;
        }
    }

    public void setSecretKey(String secretKey) {
        editor.putString(SECRET_KEY, secretKey);
        editor.apply();
        editor.commit();
    }

    public String getOTP() {

        String totpKey = userpreference.getString(SECRET_KEY, null);

        if (TextUtils.isEmpty(totpKey)) {
            return null;
        }

        Totp totpGenerator = new Totp(totpKey);
        return totpGenerator.now();
    }

    public final static boolean isValidEmail(CharSequence target) {
        if (TextUtils.isEmpty(target)) {
            return false;
        } else {
            return android.util.Patterns.EMAIL_ADDRESS.matcher(target).matches();
        }
    }

    public boolean isValidConfig() {
        String email = getEmailId();
        String API_Key = getApiKey();
        String environment = userpreference.getString(ENVIRONMENT, null);

        if (!TextUtils.equals(environment, PRODUCTION) && !TextUtils.equals(environment, SANDBOX)) {
            Toast.makeText(this.activity, "Invalid Environment", Toast.LENGTH_LONG)
                    .show();
            return false;
        }

        if (!isValidEmail(email)) {
            Toast.makeText(this.activity, "Invalid Email", Toast.LENGTH_LONG)
                    .show();
            return false;
        }

        if (!TextUtils.equals("k", API_Key.substring(0,1))) {
            Toast.makeText(this.activity, "Invalid API KEY", Toast.LENGTH_LONG)
                    .show();
            return false;
        }

        return true;
    }
}