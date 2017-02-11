package haibison.android.pattern.sensors;

public class Magnetic {
    private float x, y, z;
    private long timestamp;

    public Magnetic(float x, float y, float z, long timestamp) {
        this.timestamp = timestamp;
        this.x = x;
        this.y = y;
        this.z = z;
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
