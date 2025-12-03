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

    // -------------------- FIELDS --------------------

    private final List<ActivityLog> activityLogs;
    private final List<Notification> notifications;
    private final List<Device> deviceList;
    private final List<User> userList;

    // -------------------- CONSTRUCTOR --------------------

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

    // -------------------- DEVICE REMOVAL --------------------

    public boolean removeDevice(String deviceID) {
        Optional<Device> deviceOpt = findDeviceByID(deviceID);
        if (deviceOpt.isEmpty()) {
            logActivity("Device with ID " + deviceID + " not found for removal.");
            return false;
        }
        Device device = deviceOpt.get();
        deviceList.remove(device);
        logActivity("Device removed: " + device.getDeviceName()
                + " (" + deviceID + ")");
        return true;
    }

    // =====================================================
    // ================== USER MANAGEMENT ==================
    // =====================================================

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

    // ---------- NEW: helper to find user by username ----------

    /**
     * Finds the first user whose username matches (case-sensitive).
     */
    public User findUserByUsername(String username) {
        if (username == null) return null;
        for (User u : userList) {
            if (username.equals(u.getUsername())) {   // <--- use login username
                return u;
            }
        }
        return null;
    }


    /**
     * Checks if a username is already taken.
     */
    public boolean isUsernameTaken(String username) {
        return findUserByUsername(username) != null;
    }

    /**
     * Authenticate a user by username + password.
     *
     * NOTE: This assumes your User class (or subclasses) exposes a
     * getPassword() method. If yours is called getPasswordHash()
     * or something else, just update the call below.
     *
     * @return the matching User if credentials are valid, otherwise null.
     */
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


    /**
     * Registers a new guest user.
     *
     * This assumes HomeGuest has a constructor:
     *   HomeGuest(String name, String username, String email, String password)
     * matching how it's used in HomeGuardianServerMain.
     *
     * It throws IllegalArgumentException if the username is already taken.
     */
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

    // =====================================================
    // ================== ACTIVITY LOGGING =================
    // =====================================================

    public void logActivity(String message) {
        ActivityLog log = new ActivityLog(message);
        activityLogs.add(log);
        System.out.println("[ACTIVITY] " + message);
    }

    public List<ActivityLog> getAllLogs() {
        return Collections.unmodifiableList(activityLogs);
    }

    // =====================================================
    // ================== NOTIFICATIONS ====================
    // =====================================================

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

    // =====================================================
    // ========== HIGH-LEVEL DEVICE OPERATIONS =============
    // ======== (USED BY HomeGuardianClient/Server) ========
    // =====================================================

    // ---------- Helper getters for typed devices ----------

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

    private MotionSensor getMotionSensor() {
        for (Device d : deviceList) {
            if (d instanceof MotionSensor) {
                return (MotionSensor) d;
            }
        }
        return null;
    }

    // -----------------------------------------------------
    // LIGHT COMMANDS
    // -----------------------------------------------------

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
        MotionSensor sensor = getMotionSensor();
        if (light == null || sensor == null) {
            logActivity("toggleLightMotionLink failed – light or sensor missing (lightID=" + deviceId + ")");
            return false;
        }

        if (on) {
            sensor.linkLight(light);
            light.toggleMotionLink(); // update light’s own flag
            logActivity("Light " + deviceId + " linked to MotionSensor.");
        } else {
            sensor.unlinkLight(light);
            light.toggleMotionLink(); // flip back
            logActivity("Light " + deviceId + " unlinked from MotionSensor.");
        }
        return true;
    }

    /**
     * MotionSensor currently has no sensitivity field; this is a no-op.
     */
    public boolean setMotionSensitivity(int value) {
        MotionSensor sensor = getMotionSensor();
        if (sensor == null) {
            logActivity("setMotionSensitivity failed – no MotionSensor in device list.");
            return false;
        }
        logActivity("setMotionSensitivity called with value " + value +
                " (not implemented in MotionSensor model).");
        return true;
    }

    // -----------------------------------------------------
    // LOCK COMMANDS
    // -----------------------------------------------------

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

    // -----------------------------------------------------
    // CAMERA COMMANDS
    // -----------------------------------------------------

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

    // -----------------------------------------------------
    // ALARM COMMANDS
    // -----------------------------------------------------

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
        MotionSensor sensor = getMotionSensor();
        if (alarm == null || sensor == null) {
            logActivity("toggleAlarmMotion failed – alarm or sensor missing (alarm=" + alarmId + ")");
            return false;
        }

        boolean current = alarm.isLinkedToMotion();

        if (on && !current) {
            alarm.toggleMotionLink();
            sensor.linkAlarm(alarm);
            logActivity("Alarm " + alarmId + " linked to MotionSensor.");
        } else if (!on && current) {
            alarm.toggleMotionLink();
            sensor.unlinkAlarm(alarm);
            logActivity("Alarm " + alarmId + " unlinked from MotionSensor.");
        } else {
            logActivity("toggleAlarmMotion called but state already " + on + " for alarm " + alarmId);
        }
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

    // =====================================================
    // ==================== UTIL HELPERS ===================
    // =====================================================

    private int clamp(int val, int min, int max) {
        return Math.max(min, Math.min(max, val));
    }
}
