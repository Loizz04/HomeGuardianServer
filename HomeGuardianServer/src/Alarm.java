public class Alarm extends Device {

    private int id;                     // 1, 2, 3
    private boolean armed;
    private int volume;                 // 0–100
    private String tone;                // can be any string or enum
    private boolean linkedToMotion;
    private boolean linkedToLight;
    private boolean linkedToLock;

    public Alarm(int id, String name) {
        // FIX: Device(String deviceID, String deviceName)
        super("alarm" + id, name);

        this.id = id;
        this.armed = false;
        this.volume = 50;
        this.tone = "default";
        this.linkedToMotion = false;
        this.linkedToLight = false;
        this.linkedToLock = false;
    }

    public int getId() { return id; }
    public boolean isArmed() { return armed; }
    public int getVolume() { return volume; }
    public String getTone() { return tone; }

    public boolean isLinkedToMotion() { return linkedToMotion; }
    public boolean isLinkedToLight() { return linkedToLight; }
    public boolean isLinkedToLock() { return linkedToLock; }

    public void arm()  { armed = true; }
    public void disarm() { armed = false; }

    public void setVolume(int volume) {
        this.volume = Math.max(0, Math.min(100, volume));
    }

    public void setTone(String tone) {
        this.tone = tone;
    }

    public void toggleMotionLink() { linkedToMotion = !linkedToMotion; }
    public void toggleLightLink()  { linkedToLight  = !linkedToLight; }
    public void toggleLockLink()   { linkedToLock   = !linkedToLock; }
}
