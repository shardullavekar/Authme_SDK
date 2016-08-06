package haibison.android.lockpattern.sensors;

public class Accelerometer {
    private float x, y, z;
    private long timestamp;

    public Accelerometer(float x, float y, float z, long timestamp) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.timestamp = timestamp;
    }

    public float getX() {
        return this.x;
    }

    public float getY() {
        return this.y;
    }

    public float getZ() {
        return this.z;
    }

    public long getTimestamp() {
        return this.timestamp;
    }
}
