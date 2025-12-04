
import java.util.List;
import java.util.ArrayList;

public class Device {
	
    /**
     * Base class for all devices in the Home Guardian system.
     *
     * ATTRIBUTES
     *  - deviceID: unique identifier for the device
     *  - deviceName: human-readable name
     *  - connected: basic ON/OFF or connection status
     *  - deviceLogs: list of ActivityLog entries for this device
     *
     * METHODS
     *  - getDeviceID(), getDeviceName()
     *  - connectionStatus()
     *  - addLog()
     *  - notifyEvents()
     *  - handleCommand()
     *  - getDeviceLogs()
     */

    private final String deviceID;
    private final String deviceName;
    private boolean connected;
    private final List<ActivityLog> deviceLogs;

    public Device(String deviceID, String deviceName) {
        this.deviceID = deviceID;
        this.deviceName = deviceName;
        // default: device starts disconnected / OFF
        this.connected = false;
        this.deviceLogs = new ArrayList<>();
    }

    // ---------- GETTERS ----------

    public String getDeviceID() {
        return deviceID;
    }

    public String getDeviceName() {
        return deviceName;
    }

    // Returns the current connection / ON-OFF status
    public boolean connectionStatus() {
        return connected;
    }

    // ---------- LOGGING & EVENTS ----------

    // Adds a new log entry to the device's internal log history
    public void addLog(String message) {
        ActivityLog log = new ActivityLog(deviceName, message);
        deviceLogs.add(log);

        System.out.println("[LOG][DEVICE] " + message);
    }

    // Sends notifications for important device events (currently console + log)
    public void notifyEvents(String eventMessage) {
        System.out.println("[DEVICE EVENT][" + deviceName + "] " + eventMessage);
        addLog("Event: " + eventMessage);
    }

    // ---------- GENERIC COMMAND HANDLER ----------

    /**
     * Basic handler for simple commands.
     * Child classes can override this if they need more specific behavior.
     */
    public boolean handleCommand(String command) {
        if (command == null) {
            notifyEvents("Received null command");
            return false;
        }

        switch (command.toUpperCase()) {
            case "ON":
                connected = true;
                notifyEvents("Device turned ON");
                return true;

            case "OFF":
                connected = false;
                notifyEvents("Device turned OFF");
                return true;

            case "LOCK":
                notifyEvents("Lock activated");
                return true;

            case "UNLOCK":
                notifyEvents("Lock released");
                return true;

            default:
                notifyEvents("Unknown command: " + command);
                return false;
        }
    }

    public List<ActivityLog> getDeviceLogs() {
        return deviceLogs;
    }
}
