import java.util.ArrayList;

public class MotionSensor extends Device {

    private boolean motionDetected;
    private ArrayList<SmartLight> linkedLights;
    private ArrayList<Alarm> linkedAlarms;

    public MotionSensor(int id, String name) {
        // FIX: Device requires (deviceID, deviceName)
        super("motion" + id, name);

        this.motionDetected = false;
        this.linkedLights = new ArrayList<>();
        this.linkedAlarms = new ArrayList<>();
    }

    public boolean isMotionDetected() { 
        return motionDetected; 
    }

    public void detectMotion() {
        motionDetected = true;

        // Trigger linked lights
        for (SmartLight light : linkedLights) {
            light.turnOn();
        }

        // Trigger linked alarms
        for (Alarm alarm : linkedAlarms) {
            alarm.arm();
        }
    }

    public void clearMotion() {
        motionDetected = false;
    }

    public void linkLight(SmartLight light) {
        if (!linkedLights.contains(light)) {
            linkedLights.add(light);
        }
    }

    public void linkAlarm(Alarm alarm) {
        if (!linkedAlarms.contains(alarm)) {
            linkedAlarms.add(alarm);
        }
    }

    public void unlinkLight(SmartLight light) {
        linkedLights.remove(light);
    }

    public void unlinkAlarm(Alarm alarm) {
        linkedAlarms.remove(alarm);
    }
}
