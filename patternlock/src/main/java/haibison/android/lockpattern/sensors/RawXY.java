package haibison.android.lockpattern.sensors;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by shardul on 28-09-2015.
 */
public class RawXY {
    private float x , y;
    private long timestamp;
    public RawXY(float x, float y, long timestamp) {
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
