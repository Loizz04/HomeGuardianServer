

import java.util.ArrayList;
import java.util.List;

public class HomeGuest extends User {

    // -------------------------
    // ATTRIBUTES
    // -------------------------
    private final List<Device> accessibleDevices; // Devices this guest can use
    private String guestLockPasscode;            // Optional passcode for SmartLocks

    // -------------------------
    // CONSTRUCTOR
    // -------------------------
    public HomeGuest(String name, String username, String email, String passwordHash) {
        super(name, username, email, passwordHash);
        this.accessibleDevices = new ArrayList<>();
    }

    // -------------------------
    // OPERATIONS
    // -------------------------
    public boolean loginGuest(String username, String passwordHash) {
        if (login(username, passwordHash)) {
            addUserLog("Guest logged in.");
            return true;
        } else {
            addUserLog("Guest login failed.");
            return false;
        }
    }

    public void addAccessibleDevice(Device device) {
        if (!accessibleDevices.contains(device)) {
            accessibleDevices.add(device);
        }
    }

    public void removeAccessibleDevice(Device device) {
        accessibleDevices.remove(device);
    }

    public void setGuestLockPasscode(String passcode) {
        this.guestLockPasscode = passcode;
    }

    // -------------------------
    // GETTERS
    // -------------------------
    public List<Device> getAccessibleDevices() {
        return accessibleDevices;
    }

    public String getGuestLockPasscode() {
        return guestLockPasscode;
    }

    @Override
    public String getRole() {
        return "Guest";
    }
}

