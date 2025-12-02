

/**
 * Author: Rawan Genina
 * Student Number: 1196208
 * 
 * Class: HomeAdmin
 * 
 * Description:
 * The HomeAdmin class represents an administrative user within the HomeGuardian
 * smart home system. An admin has elevated privileges compared to regular users 
 * and is responsible for managing guests, devices, and security access.
 * 
 * This class extends the User base class and introduces additional 
 * administrative functionality, including:
 * 
 *  - Creating guest accounts (HomeGuest objects)
 *  - Assigning devices to guest users
 *  - Revoking device access from guests
 *  - Generating or updating a guest-specific lock passcode
 *  - Tracking actions through the inherited user activity log
 * 
 * HomeAdmin acts as the main authority for user management and access control
 * within the system.
 */
import java.util.List;

public class HomeAdmin extends User {

    // -------------------------
    // ATTRIBUTES
    // -------------------------
    private boolean isPrimaryAdmin;

    // -------------------------
    // CONSTRUCTOR
    // -------------------------
    public HomeAdmin(String name, String username, String email, String passwordHash, boolean isPrimaryAdmin) {
        super(name, username, email, passwordHash);
        this.isPrimaryAdmin = isPrimaryAdmin;
    }

    // -------------------------
    // OPERATIONS
    // -------------------------

    public HomeGuest createGuest(String name, String username, String email, String passwordHash) {
        return new HomeGuest(name, username, email, passwordHash);
    }

    public void assignDevice(HGController controller, HomeGuest guest, Device device) {
        guest.addAccessibleDevice(device);
        addUserLog("Assigned device " + device.getDeviceName() + " to guest " + guest.getUsername());
    }

    public void revokeAccess(HGController controller, HomeGuest guest, Device device) {
        guest.removeAccessibleDevice(device);
        addUserLog("Revoked access to device " + device.getDeviceName() + " from guest " + guest.getUsername());
    }

    public void createGuestLockPasscode(HomeGuest guest, String passcode) {
        guest.setGuestLockPasscode(passcode);
        addUserLog("Set guest lock passcode for " + guest.getUsername());
    }

    // -------------------------
    // GETTERS / SETTERS
    // -------------------------
    public boolean isPrimaryAdmin() {
        return isPrimaryAdmin;
    }

    public void setPrimaryAdmin(boolean primaryAdmin) {
        isPrimaryAdmin = primaryAdmin;
    }

    @Override
    public String getRole() {
        return "Admin";
    }
}

