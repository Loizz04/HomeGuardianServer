
import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

/**
 * Name: Nosizo Mabuza
 * Revised by: Rawan Genina
 * Date Implemented: Nov 1st, 2025
 * Date Revised: Dec 1st, 2025
 *
 * Description:
 * Represents a system notification or alert intended for a specific user
 * or a general system/emergency event. Tracks:
 *  - target user (or system/global)
 *  - message content
 *  - timestamp
 *  - user email for delivery
 *  - whether notifications are enabled
 *  - unique notification ID
 *
 * --ATTRIBUTES (Design Document)
 * notificationID: String
 * userID: String
 * timeStamp: LocalDateTime
 * isEnabled: boolean
 * userEmail: String
 * recipient: User
 * message: String
 *
 * --METHODS
 * Notification(recipient, message, isEnabled, userEmail)
 * Notification(recipient, message)
 * isEnabled()
 * sendAlert()
 * getEmail()
 * setEmail()
 * getRecipient()
 * getMessage()
 */

public class Notification implements Serializable {

    private static final long serialVersionUID = 1L;

    // -------------------------
    // ATTRIBUTES
    // -------------------------
    private final String notificationID;
    private final String userID;
    private final LocalDateTime timeStamp;

    private boolean isEnabled;
    private String userEmail;

    private final User recipient;   // Can be null (SYSTEM/global alert)
    private final String message;

    // -------------------------
    // FULL CONSTRUCTOR
    // -------------------------
    /**
     * Full constructor (design-specified)
     */
    public Notification(User recipient, String message, boolean isEnabled, String userEmail) {
        this.recipient = recipient;
        this.message = message;

        this.notificationID = UUID.randomUUID().toString();
        this.timeStamp = LocalDateTime.now();
        this.isEnabled = isEnabled;
        this.userEmail = userEmail;

        // userID = username OR “SYSTEM”
        this.userID = (recipient != null) ? recipient.getUsername() : "SYSTEM";
    }

    // -------------------------
    // CONVENIENCE CONSTRUCTOR
    // -------------------------
    /**
     * Simple constructor used by controller (enabled + placeholder email)
     */
    public Notification(User recipient, String message) {
        this(recipient, message, true, "unknown@example.com");
    }

    // -------------------------
    // METHODS
    // -------------------------

    /** Checks if notification is enabled */
    public boolean isEnabled() {
        return isEnabled;
    }

    /**
     * Simulates sending an alert.
     * Returns true if sent, false if disabled.
     */
    public boolean sendAlert() {
        if (!isEnabled) {
            System.out.println("[" + getTimestampFormatted() + "] ALERT DISABLED for User "
                    + userID + ": " + message);
            return false;
        }

        String recipientInfo = (recipient != null) ? recipient.getName() : "System/Global";

        System.out.println("--- ALERT SENT ---");
        System.out.println("ID: " + notificationID);
        System.out.println("To: " + recipientInfo + " | Email: " + userEmail);
        System.out.println("Time: " + getTimestampFormatted());
        System.out.println("Message: " + message);
        System.out.println("------------------");

        return true;
    }

    /** Returns the current email on file */
    public String getEmail() {
        return userEmail;
    }

    /** Updates the user's email string */
    public void setEmail(String email) {
        if (email != null && !email.trim().isEmpty()) {
            this.userEmail = email;
            System.out.println("Notification email updated to: " + email);
        }
    }

    // -------------------------
    // GETTERS (Design-Specified)
    // -------------------------

    public String getNotificationID() {
        return notificationID;
    }

    public String getUserID() {
        return userID;
    }

    public LocalDateTime getTimeStamp() {
        return timeStamp;
    }

    public String getTimestampFormatted() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        return timeStamp.format(formatter);
    }

    public User getRecipient() {
        return recipient;
    }

    public String getMessage() {
        return message;
    }
}