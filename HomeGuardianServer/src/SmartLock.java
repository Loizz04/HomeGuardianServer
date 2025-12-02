public class SmartLock extends Device {

    private int id;                 // 1,2
    private boolean isLocked;
    private boolean linkedToAlarm;
    private boolean linkedToMotion;

    public SmartLock(int id, String name) {
        super("lock" + id, name);   // ✅ matches Device(String deviceID, String deviceName)
        this.id = id;
        this.isLocked = true;
    }

    public int getId() { return id; }
    public boolean isLocked() { return isLocked; }
    public boolean isLinkedToAlarm() { return linkedToAlarm; }
    public boolean isLinkedToMotion() { return linkedToMotion; }

    public void lock()  { isLocked = true; }
    public void unlock(){ isLocked = false; }

    public void toggleAlarmLink() {
        linkedToAlarm = !linkedToAlarm;
    }

    public void toggleMotionLink() {
        linkedToMotion = !linkedToMotion;
    }
}
