
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

/**
 * Author: Rawan Genina
 * Student Number: 1196208
 * 
 * Class: User
 * 
 * Description:
 * The User class is the abstract base representation of any account within the
 * HomeGuardian smart home system. It is extended by HomeAdmin and HomeGuest to
 * provide role-specific behavior.
 * 
 * This class manages:
 *   - Core identity fields (name, username, email)
 *   - Secure password hash storage
 *   - Basic authentication methods (login, logout, signup)
 *   - User verification via email
 *   - A personal activity log recording all user actions
 *
 */
public abstract class User {

    // -------------------------
    // ATTRIBUTES
    // -------------------------
    protected String name;
    protected String username;
    protected String email;
    protected String passwordHash;

    // Each user has a personal activity log list
    protected List<ActivityLog> userLogs;

    // -------------------------
       // CONSTRUCTOR
    // -------------------------
    public User(String name, String username, String email, String passwordHash) {
        this.name = name;
        this.username = username;
        this.email = email;
        this.passwordHash = passwordHash;
        this.userLogs = new ArrayList<>();
    }

    // -------------------------
    // CORE METHODS
    // -------------------------

    public boolean login(String enteredUsername, String enteredPasswordHash) {
        if (this.username.equals(enteredUsername) &&
            this.passwordHash.equals(enteredPasswordHash)) {

            addUserLog("Login successful.");
            return true;
        }
        addUserLog("Login failed.");
        return false;
    }

    public void logout() {
        addUserLog("User logged out.");
    }

    public boolean signup(HGController controller) {
        controller.addUser(this);
        addUserLog("User signed up.");
        return true;
    }

    public boolean verifyUser(String email) {
        return this.email.equals(email);
    }
    
   
    public String getUserId() {
        return username; // or some other ID logic
    }

    public String getUserName() {
        return name;     // or combine name + username, etc.
    }
    // -------------------------
    // LOGGING SUPPORT
    // -------------------------
    public void addUserLog(String message) {
        ActivityLog log = new ActivityLog(message);
        userLogs.add(log);
    }

    public List<ActivityLog> getUserLogs() {
        return userLogs;
    }

    // -------------------------
    // GETTERS
    // -------------------------
    public String getName() { return name; }
    public String getUsername() { return username; }
    public String getEmail() { return email; }

    // Every subclass MUST say their role
    public abstract String getRole();
}
