
	
	/**
	 * just a push test 
	 * Impliment the following into the class 
	 * ATTRIBUTES 
	 * deviceName: String
	 * ( Not included into the class diagram but included in description ) logID: unique identifier for the log entry
	 * ( Not included into the class diagram but included in description )user: identifies the used who performed the action
	 * timeStamp: Date
	 * actionType: String 
	 * deviceID : String
	 * 
	 * METHODS
	 * +logActivity()
	 * +getAllLogs()
	 **/
	
	/*private final String message;
    private final long timestamp;

    public ActivityLog(String message) {
        this.message = message;
        this.timestamp = System.currentTimeMillis();
    }

    public String getMessage() {
        return message;
    }

    public long getTimestamp() {
        return timestamp;
        
        BUT:
 *  - The client GUI is expected to use ONLY:
 *      - deviceName
 *      - actionType
 *      - date & time (formatted)
 *  - Use:
 *      - getDeviceName()
 *      - getActionType()
 *      - getFormattedDateTime()
 *    when sending data to the client.
    }*/
	
	

	import java.util.ArrayList;
	import java.util.Date;
	import java.util.List;
	import java.text.SimpleDateFormat; // Added so the client can receive a date and time message
	/**
	 * Represents a single activity entry within the Home Guardian system.
	 * Attributes: logID, user, actionType, deviceName, deviceID, message, timestamp.
	 */
	public class ActivityLog {

	    // -------------------------
	    // STATIC STORAGE
	    // -------------------------
	    private static int nextLogId = 1;              // Auto-incremented ID
	    private static final List<ActivityLog> logStore = new ArrayList<>(); // Stores all logs

	    // -------------------------
	    // ATTRIBUTES
	    // -------------------------
	    private final String logID;
	    private final String user;
	    private final String actionType;
	    private final String deviceName;
	    private final String deviceID;
	    private final String message;
	    private final Date timeStamp;     // Human-readable
	    private final long timestamp;     // Milliseconds

	    // -------------------------
	    // CONSTRUCTORS
	    // -------------------------
	    public ActivityLog(String user, String actionType, String deviceName, String deviceID, String message) {
	        this.logID = "LOG: " + nextLogId++;
	        this.user = user;
	        this.actionType = actionType;
	        this.deviceName = deviceName;
	        this.deviceID = deviceID;
	        this.message = message;
	        this.timeStamp = new Date();
	        this.timestamp = this.timeStamp.getTime();

	        logActivity(this); // Automatically store the log
	    }

	   
	    
	    /**
	     * Convenience constructor – used by HGController and others
	     * when only a message is given.
	     *
	     * Defaults:
	     *  user       = "SYSTEM/UNKNOWN"
	     *  actionType = "Basic Log"
	     *  deviceName = "N/A"
	     *  deviceID   = "N/A"
	     */
	    public ActivityLog(String message) {
	        this("SYSTEM/UNKNOWN", "Basic Log", "N/A", "N/A", message);
	    }
	    
	 // -------------------------
	    // GETTERS
	    // -------------------------

	    public String getLogID() {
	        return logID;
	    }

	    public String getUser() {
	        return user;
	    }

	    /**
	     * This is one of the key fields the client GUI cares about.
	     */
	    public String getActionType() {
	        return actionType;
	    }

	    /**
	     * This is one of the key fields the client GUI cares about.
	     */
	    public String getDeviceName() {
	        return deviceName;
	    }

	    public String getDeviceID() {
	        return deviceID;
	    }

	    public String getMessage() {
	        return message;
	    }

	    public Date getTimeStamp() {
	        return timeStamp;
	    }

	    public long getTimestamp() {
	        return timestamp;
	    }

	    /**
	     * Formatted date & time string for the GUI.
	     * Example: "2025-12-02 14:35:10"
	     */
	    public String getFormattedDateTime() {
	        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	        return sdf.format(timeStamp);
	    }

	    // -------------------------
	    // STATIC OPERATIONS
	    // -------------------------

	    /**
	     * Store a log entry in the central store and print a concise line.
	     *
	     * NOTE: Printing is also restricted to what the client essentially cares about:
	     *   - Device name
	     *   - Action type
	     *   - Date & time
	     */
	    public static void logActivity(ActivityLog logEntry) {
	        logStore.add(logEntry);

	        System.out.println(
	            "LOGGING -> Device: " + logEntry.getDeviceName()
	            + " | Action: " + logEntry.getActionType()
	            + " | DateTime: " + logEntry.getFormattedDateTime()
	        );
	    }

	    /**
	     * Return all logs as an unmodifiable copy.
	     * The server can then map each ActivityLog into the three fields
	     * the GUI needs:
	     *   - getDeviceName()
	     *   - getActionType()
	     *   - getFormattedDateTime()
	     */
	    public static List<ActivityLog> getAllLogs() {
	        return List.copyOf(logStore);
	    }
	}
/**
	    // -------------------------
	    // GETTERS
	    // -------------------------
	    public String getLogID() { return logID; }
	    public String getUser() { return user; }
	    public String getActionType() { return actionType; }
	    public String getDeviceName() { return deviceName; }
	    public String getDeviceID() { return deviceID; }
	    public String getMessage() { return message; }
	    public Date getTimeStamp() { return timeStamp; }
	    public long getTimestamp() { return timestamp; }

	    // -------------------------
	    // STATIC OPERATIONS
	    // -------------------------

	    // Store a log entry and print to console 
	    public static void logActivity(ActivityLog logEntry) {
	        logStore.add(logEntry);
	        System.out.println("LOGGING: [" + logEntry.getTimeStamp() + "] " + logEntry.getLogID() +
	                " | User: " + logEntry.getUser() +
	                " | Action: " + logEntry.getActionType() +
	                " | Device: " + logEntry.getDeviceName() +
	                " | Message: " + logEntry.getMessage());
	    }

	    // Return all logs as an unmodifiable list 
	    public static List<ActivityLog> getAllLogs() {
	        return List.copyOf(logStore);
	    }
	}
*/
	    
	    

