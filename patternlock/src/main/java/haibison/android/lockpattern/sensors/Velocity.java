package haibison.android.lockpattern.sensors;


import org.json.JSONException;
import org.json.JSONObject;

public class Velocity {
    private float x , y;
    private long timestamp;
    public Velocity(float x, float y, long timestamp) {
        this.x = x;
        this.y = y;
        this.timestamp = timestamp;
    }

    public float getX() {
        return this.x;
    }

    public float getY() {
        return this.y;
    }

    public long getTimestamp() {
        return this.timestamp;
    }
}
