import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * Central controller for the Home Guardian server.
 *
 * - Manages:
 *   - Devices (Device + subclasses)
 *   - Users (HomeAdmin, HomeGuest, etc.)
 *   - Activity logs
 *   - Notifications
 *
 * - Also exposes high-level, type-safe methods that the Server can call
 *   in response to client commands (toggleLight, setLightBrightness, etc.).
 */
public class HGController {

    //fields

    private final List<ActivityLog> activityLogs;
    private final List<Notification> notifications;
    private final List<Device> deviceList;
    private final List<User> userList;

    //constructor

    public HGController() {
        this.activityLogs = new ArrayList<>();
        this.notifications = new ArrayList<>();
        this.deviceList = new ArrayList<>();
        this.userList = new ArrayList<>();
    }

    // =====================================================
    // ================ DEVICE REGISTRATION ================
    // =====================================================

    /**
     * Adds a new device if not already present.
     */
    public void addDevice(Device device) {
        if (device != null && !deviceList.contains(device)) {
            deviceList.add(device);
            logActivity("New device added: " + device.getDeviceName()
                    + " (" + device.getDeviceID() + ")");
        }
    }

    /**
     * Generic device control using a string command.
     * Used by the existing String-based protocol: ON, OFF, LOCK, UNLOCK, etc.
     */
    public boolean controlDevice(String deviceID, String command) {
        Optional<Device> deviceOpt = findDeviceByID(deviceID);
        if (deviceOpt.isEmpty()) {
            logActivity("Device with ID " + deviceID + " can't be found.");
            return false;
        }

        Device device = deviceOpt.get();
        boolean success = device.handleCommand(command);

        String statusMessage = success
                ? "Command '" + command + "' executed on device " + deviceID
                : "Failed to execute command '" + command + "' on device " + deviceID;

        logActivity(statusMessage);
        return success;
    }

    /**
     * Helper: find a Device by its ID.
     */
    private Optional<Device> findDeviceByID(String deviceID) {
        return deviceList.stream()
                .filter(d -> d.getDeviceID().equals(deviceID))
                .findFirst();
    }


    //user mgmt

    public void addUser(User user) {
        if (user != null && !userList.contains(user)) {
            userList.add(user);
        }
    }

    public boolean removeUser(User user) {
        if (user == null) return false;
        return userList.remove(user);
    }

    public List<User> getAllUsers() {
        return Collections.unmodifiableList(userList);
    }

    //Finds the first user whose username matches (case-sensitive).
   
    public User findUserByUsername(String username) {
        if (username == null) return null;
        for (User u : userList) {
            if (username.equals(u.getUsername())) {   // <--- use login username
                return u;
            }
        }
        return null;
    }



    public boolean isUsernameTaken(String username) {
        return findUserByUsername(username) != null;
    }


    public User authenticateUser(String username, String password) {
        if (username == null || password == null) return null;

        for (User u : userList) {
            // compare against stored login username
            if (username.equals(u.getUsername())) {
                // for now we treat passwordHash as plain text password
                if (password.equals(u.getPasswordHash())) {
                    logActivity("User '" + username + "' authenticated successfully.");
                    return u;
                } else {
                    logActivity("Failed login attempt for user '" + username + "': wrong password.");
                    return null;
                }
            }
        }

        logActivity("Failed login attempt for unknown username '" + username + "'.");
        return null;
    }


    public HomeGuest registerGuest(String name, String email, String username, String password) {
        if (name == null || email == null || username == null || password == null) {
            throw new IllegalArgumentException("All signup fields must be provided.");
        }

        if (isUsernameTaken(username)) {
            throw new IllegalArgumentException("Username '" + username + "' is already taken.");
        }

        // password is stored as passwordHash for now (no real hashing yet)
        HomeGuest guest = new HomeGuest(name, username, email, password);
        guest.signup(this); // existing pattern: user calls signup(controller)
        logActivity("New guest user registered: " + username + " (" + email + ")");
        return guest;
    }

    public void logActivity(String message) {
        ActivityLog log = new ActivityLog(message);
        activityLogs.add(log);
        System.out.println("[ACTIVITY] " + message);
    }

    public List<ActivityLog> getAllLogs() {
        return Collections.unmodifiableList(activityLogs);
    }


    public void notifyUser(User user, String message) {
        if (user == null || message == null || message.isBlank()) return;

        Notification notification = new Notification(user, message);
        notifications.add(notification);

        logActivity("Notification queued for user "
                + user.getUserName() + ": " + message);
    }

    public void notifyEmergencyServices(String message) {
        Notification emergencyNotification =
                new Notification(null, "[EMERGENCY] " + message);
        notifications.add(emergencyNotification);
        logActivity("Emergency services notified: " + message);
        System.out.println("Emergency services notified: " + message);
    }

    public List<Notification> getAllNotifications() {
        return Collections.unmodifiableList(notifications);
    }

    public List<Device> getAllDevices() {
        return Collections.unmodifiableList(deviceList);
    }


    private SmartLight getLightById(String deviceId) {
        Optional<Device> opt = findDeviceByID(deviceId);
        if (opt.isEmpty() || !(opt.get() instanceof SmartLight)) return null;
        return (SmartLight) opt.get();
    }

    private SmartLock getLockById(String deviceId) {
        Optional<Device> opt = findDeviceByID(deviceId);
        if (opt.isEmpty() || !(opt.get() instanceof SmartLock)) return null;
        return (SmartLock) opt.get();
    }

    private Alarm getAlarmById(String deviceId) {
        Optional<Device> opt = findDeviceByID(deviceId);
        if (opt.isEmpty() || !(opt.get() instanceof Alarm)) return null;
        return (Alarm) opt.get();
    }

    private SecurityCamera getCameraById(String deviceId) {
        Optional<Device> opt = findDeviceByID(deviceId);
        if (opt.isEmpty() || !(opt.get() instanceof SecurityCamera)) return null;
        return (SecurityCamera) opt.get();
    }


    //LIGHT COMMANDS

    public boolean toggleLight(String deviceId, boolean on) {
        SmartLight light = getLightById(deviceId);
        if (light == null) {
            logActivity("toggleLight failed – no SmartLight with ID " + deviceId);
            return false;
        }

        if (on) {
            light.turnOn();
        } else {
            light.turnOff();
        }

        logActivity("Light " + deviceId + " set to " + (on ? "ON" : "OFF"));
        return true;
    }

    public boolean setLightBrightness(String deviceId, int value) {
        SmartLight light = getLightById(deviceId);
        if (light == null) {
            logActivity("setLightBrightness failed – no SmartLight with ID " + deviceId);
            return false;
        }
        light.setBrightness(value);
        logActivity("Light " + deviceId + " brightness set to " + value);
        return true;
    }

    public boolean setLightColor(String deviceId, int r, int g, int b) {
        SmartLight light = getLightById(deviceId);
        if (light == null) {
            logActivity("setLightColor failed – no SmartLight with ID " + deviceId);
            return false;
        }

        light.setColor(r, g, b);
        logActivity("Light " + deviceId + " colour set to RGB(" + r + "," + g + "," + b + ")");
        return true;
    }

    /**
     * Your SmartLight class doesn't have timeout support.
     * We keep this method for protocol compatibility, but log it as a no-op.
     */
    public boolean setLightTimeout(String deviceId, int minutes) {
        SmartLight light = getLightById(deviceId);
        if (light == null) {
            logActivity("setLightTimeout failed – no SmartLight with ID " + deviceId);
            return false;
        }
        logActivity("setLightTimeout called for " + deviceId +
                " with " + minutes + " minutes (no timeout field implemented).");
        return true;
    }

    /**
     * Link/unlink light to the MotionSensor.
     * On = link via MotionSensor.linkLight; Off = unlink.
     */
    public boolean toggleLightMotionLink(String deviceId, boolean on) {
        SmartLight light = getLightById(deviceId);
        if (light == null) {
            logActivity("toggleLightMotionLink failed – no SmartLight with ID " + deviceId);
            return false;
        }
        light.toggleMotionLink();

        logActivity("Light " + deviceId + " motion link toggled to " 
                + (on ? "ON" : "OFF") + " (handled locally on the light).");
        return true;
    }


    public boolean setMotionSensitivity(int value) {
        // clamp to 0–100 or whatever range your slider uses
        int clamped = clamp(value, 0, 100);

        boolean anyLight = false;
        for (Device d : deviceList) {
            if (d instanceof SmartLight) {
                SmartLight light = (SmartLight) d;
                light.setMotionSensitivity(clamped);   // <-- per-light field
                anyLight = true;
            }
        }

        if (!anyLight) {
            logActivity("setMotionSensitivity failed – no SmartLights in device list.");
            return false;
        }

        logActivity("Motion sensitivity set to " + clamped + " for all SmartLights.");
        return true;
    }


    //LOCK COMMANDS

    public boolean toggleLock(String deviceId, boolean engaged) {
        SmartLock lock = getLockById(deviceId);
        if (lock == null) {
            logActivity("toggleLock failed – no SmartLock with ID " + deviceId);
            return false;
        }

        if (engaged) {
            lock.lock();
        } else {
            lock.unlock();
        }

        logActivity("Lock " + deviceId + " set to " + (engaged ? "LOCKED" : "UNLOCKED"));
        return true;
    }

    /**
     * Your SmartLock model has no duration; keep method for protocol but log as no-op.
     */
    public boolean setLockDuration(String deviceId, int minutes) {
        SmartLock lock = getLockById(deviceId);
        if (lock == null) {
            logActivity("setLockDuration failed – no SmartLock with ID " + deviceId);
            return false;
        }
        logActivity("setLockDuration called for " + deviceId +
                " with " + minutes + " minutes (no duration field implemented).");
        return true;
    }

    /**
     * Link/unlink lock to an alarm.
     * Your SmartLock only has a boolean linkedToAlarm; we toggle that, but
     * we don't store exactly *which* alarm. We still log the targeted alarmId.
     */
    public boolean linkLockToAlarm(String lockId, String alarmId, boolean linked) {
        SmartLock lock = getLockById(lockId);
        Alarm alarm = getAlarmById(alarmId);
        if (lock == null || alarm == null) {
            logActivity("linkLockToAlarm failed – lock or alarm missing (lock=" +
                    lockId + ", alarm=" + alarmId + ")");
            return false;
        }

        // Just reflect the link status on the lock
        if (linked) {
            if (!lock.isLinkedToAlarm()) {
                lock.toggleAlarmLink();
            }
            logActivity("Lock " + lockId + " linked logically to Alarm " + alarmId);
        } else {
            if (lock.isLinkedToAlarm()) {
                lock.toggleAlarmLink();
            }
            logActivity("Lock " + lockId + " unlinked logically from Alarm " + alarmId);
        }
        return true;
    }

    //CAMERA COMMANDS

    public boolean toggleCamera(String deviceId, boolean on) {
        SecurityCamera cam = getCameraById(deviceId);
        if (cam == null) {
            logActivity("toggleCamera failed – no SecurityCamera with ID " + deviceId);
            return false;
        }

        if (on) {
            cam.turnOn();
        } else {
            cam.turnOff();
        }

        logActivity("Camera " + deviceId + " set to " + (on ? "ON" : "OFF"));
        return true;
    }

    public boolean toggleCameraRecording(String deviceId, boolean on) {
        SecurityCamera cam = getCameraById(deviceId);
        if (cam == null) {
            logActivity("toggleCameraRecording failed – no SecurityCamera with ID " + deviceId);
            return false;
        }

        if (on) {
            cam.startRecording();
        } else {
            cam.stopRecording();
        }

        logActivity("Camera " + deviceId + " recording " + (on ? "STARTED" : "STOPPED"));
        return true;
    }

    /**
     * In your simplified model, camera just has a motionTriggered flag.
     * We map this to toggleMotionTrigger().
     */
    public boolean toggleCameraMotion(String deviceId, boolean on) {
        SecurityCamera cam = getCameraById(deviceId);
        if (cam == null) {
            logActivity("toggleCameraMotion failed – camera missing (cam=" + deviceId + ")");
            return false;
        }

        boolean current = cam.isMotionTriggered();
        if (current != on) {
            cam.toggleMotionTrigger();
        }

        logActivity("Camera " + deviceId + " motion trigger set to " + on);
        return true;
    }

    /**
     * Placeholder: server-side handler for "getCameraFootage".
     * For now just logs the request – actual media handling is beyond scope.
     */
    public void requestCameraFootage(String deviceId, String timeRangeLabel) {
        SecurityCamera cam = getCameraById(deviceId);
        if (cam == null) {
            logActivity("requestCameraFootage failed – no SecurityCamera with ID " + deviceId);
            return;
        }
        logActivity("Footage requested from camera " + deviceId +
                " for range: " + timeRangeLabel + " (not implemented).");
    }

    //ALARM COMMANDS

    public boolean toggleAlarmWithString(String alarmId, boolean on) {
        Alarm alarm = getAlarmById(alarmId);
        if (alarm == null) {
            logActivity("toggleAlarmWithString failed – no Alarm with ID " + alarmId);
            return false;
        }

        if (on) {
            alarm.arm();
        } else {
            alarm.disarm();
        }

        logActivity("Alarm " + alarmId + " set to " + (on ? "ARMED" : "DISARMED"));
        return true;
    }

    public boolean toggleAlarmMotion(String alarmId, boolean on) {
        Alarm alarm = getAlarmById(alarmId);
        if (alarm == null) {
            logActivity("toggleAlarmMotion failed – no Alarm with ID " + alarmId);
            return false;
        }

        // Alarm already has isLinkedToMotion() + toggleMotionLink()
        boolean current = alarm.isLinkedToMotion();
        if (current != on) {
            alarm.toggleMotionLink();
        }

        logActivity("Alarm " + alarmId + " motion link set to " + on);
        return true;
    }


    /**
     * Link/unlink alarm to record on camera when activated.
     * Your current Alarm model has no cameraId storage; we only log this.
     */
    public boolean toggleAlarmRecordOnCam(String alarmId, String cameraId, boolean on) {
        Alarm alarm = getAlarmById(alarmId);
        SecurityCamera cam = getCameraById(cameraId);
        if (alarm == null || cam == null) {
            logActivity("toggleAlarmRecordOnCam failed – alarm or camera missing (alarm=" +
                    alarmId + ", cam=" + cameraId + ")");
            return false;
        }

        logActivity("toggleAlarmRecordOnCam called for alarm=" + alarmId +
                ", camera=" + cameraId + ", on=" + on +
                " (link not modeled in Alarm class).");
        return true;
    }

//util helper
    private int clamp(int val, int min, int max) {
        return Math.max(min, Math.min(max, val));
    }
}
