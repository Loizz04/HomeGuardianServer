import java.util.ArrayList;
import java.util.List;

public class SmartLight extends Device {

    private int id;             // 1, 2, 3
    private boolean isOn;
    private int brightness;     // 0–100
    private int red, green, blue;
    private boolean linkedToMotion;

    public SmartLight(int id, String name) {
        // FIX: Device requires TWO STRINGS: (deviceID, deviceName)
        super("light" + id, name);
        
        this.id = id;
        this.isOn = false;
        this.brightness = 100;
        this.red = 255;
        this.green = 255;
        this.blue = 255;
        this.linkedToMotion = false;
    }

    public int getId() { return id; }
    public boolean isOn() { return isOn; }
    public int getBrightness() { return brightness; }
    public int getRed() { return red; }
    public int getGreen() { return green; }
    public int getBlue() { return blue; }
    public boolean isLinkedToMotion() { return linkedToMotion; }

    public void turnOn() { isOn = true; }
    public void turnOff() { isOn = false; }

    public void setBrightness(int brightness) {
        this.brightness = Math.max(0, Math.min(100, brightness));
    }

    public void setColor(int r, int g, int b) {
        this.red = Math.max(0, Math.min(255, r));
        this.green = Math.max(0, Math.min(255, g));
        this.blue = Math.max(0, Math.min(255, b));
    }

    public void toggleMotionLink() {
        linkedToMotion = !linkedToMotion;
    }
}
