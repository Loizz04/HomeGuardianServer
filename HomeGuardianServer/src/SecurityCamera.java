public class SecurityCamera extends Device {

    private int id;                 // 1, 2, 3
    private boolean isOn;
    private boolean isRecording;
    private boolean motionTriggered;

    public SecurityCamera(int id, String name) {
        // FIX: Device requires (deviceID, deviceName)
        super("camera" + id, name);

        this.id = id;
        this.isOn = false;
        this.isRecording = false;
        this.motionTriggered = false;
    }

    public int getId() { return id; }
    public boolean isOn() { return isOn; }
    public boolean isRecording() { return isRecording; }
    public boolean isMotionTriggered() { return motionTriggered; }

    public void turnOn()  { isOn = true; }
    public void turnOff() { isOn = false; }

    public void startRecording() { isRecording = true; }
    public void stopRecording()  { isRecording = false; }

    public void toggleMotionTrigger() {
        motionTriggered = !motionTriggered;
    }
}
