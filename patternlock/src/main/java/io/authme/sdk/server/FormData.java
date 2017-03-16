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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import io.authme.sdk.sensors.Accelerometer;
import io.authme.sdk.sensors.Gyroscope;
import io.authme.sdk.sensors.Magnetic;
import io.authme.sdk.sensors.Orientation;
import io.authme.sdk.sensors.RawXY;
import io.authme.sdk.sensors.Velocity;


public class FormData {

    private ArrayList<Accelerometer> accelList;
    private ArrayList<Magnetic> magnetics;
    private ArrayList<Gyroscope> gyroscopes;
    private ArrayList<RawXY> xy;
    private ArrayList<Velocity> velocityList;
    private ArrayList<Orientation> orientationArrayList;
    private JSONObject biggerJSon;

    private JSONArray acceloArray, magneticArray, gyroArray, xyArray, velocityArray, orientArary;

    public FormData(ArrayList accelList,
                    ArrayList orientations, ArrayList gyroscopes,
                    ArrayList xy, ArrayList velocityList,
                    ArrayList orientationList) {
        this.accelList = accelList;
        this.magnetics = orientations;
        this.gyroscopes = gyroscopes;
        this.xy = xy;
        this.orientationArrayList = orientationList;
        this.velocityList = velocityList;
        acceloArray = new JSONArray();
        magneticArray = new JSONArray();
        gyroArray = new JSONArray();
        xyArray = new JSONArray();
        velocityArray = new JSONArray();
        orientArary = new JSONArray();
        biggerJSon = new JSONObject();
    }

    public JSONObject formJson() {
        int co_ordinate = 0, velo_cordi = 0;
        for (Accelerometer i : accelList) {
            JSONObject acclelo = new JSONObject();
            try {
                acclelo.put("x", i.getX());
                acclelo.put("y", i.getY());
                acclelo.put("z", i.getZ());
                acclelo.put("t", i.getTimestamp());
                acceloArray.put(acclelo);
            } catch (JSONException e) {
                e.printStackTrace();
            }

        }

        for (Magnetic i : magnetics) {
            JSONObject orio = new JSONObject();
            try {
                orio.put("x", i.getX());
                orio.put("y", i.getY());
                orio.put("z", i.getZ());
                orio.put("t", i.getTimestamp());
                magneticArray.put(orio);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        for (Gyroscope i : gyroscopes) {
            JSONObject gyroobj = new JSONObject();
            try {
                gyroobj.put("x", i.getX());
                gyroobj.put("y", i.getY());
                gyroobj.put("z", i.getZ());
                gyroobj.put("t", i.getTimestamp());
            } catch (JSONException e) {
                e.printStackTrace();
            }
            gyroArray.put(gyroobj);
        }

        for (RawXY i : xy) {
            JSONObject rawxyObj = new JSONObject();
            try {
                rawxyObj.put("x", i.getX());
                rawxyObj.put("y", i.getY());
                rawxyObj.put("t", i.getTimestamp());
            } catch (JSONException e) {
                e.printStackTrace();
            }
            xyArray.put(rawxyObj);
        }

        for (Orientation i : orientationArrayList) {
            JSONObject orientObj = new JSONObject();
            try {
                orientObj.put("x", i.getX());
                orientObj.put("y", i.getY());
                orientObj.put("z", i.getZ());
                orientObj.put("t", i.getTimestamp());
            } catch (JSONException e) {
                e.printStackTrace();
            }
            orientArary.put(orientObj);
        }

        for (Velocity v : velocityList) {
            JSONObject veloObj = new JSONObject();
            try {
                veloObj.put("x", v.getX());
                veloObj.put("y", v.getY());
                veloObj.put("t", v.getTimestamp());
            } catch (JSONException e) {
                e.printStackTrace();
            }
            velocityArray.put(veloObj);
        }
        JSONObject sensorsIp = new JSONObject();
        try {
            sensorsIp.put("magnetic_sensor", magneticArray);
            sensorsIp.put("accel_sensor", acceloArray);
            sensorsIp.put("gyro_sensor", gyroArray);
            sensorsIp.put("co_ordinates", xyArray);
            sensorsIp.put("velocity", velocityArray);
            sensorsIp.put("orient_sensor", orientArary);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        try {
            biggerJSon.put("PatternLength", 5);
            biggerJSon.put("Sensors", sensorsIp);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return biggerJSon;
    }


}
