import java.text.SimpleDateFormat;
import java.util.Date;

public class ActivityLog {

    private final String device;
    private final String activity;
    private final String dateTime;

    // Main constructor used by HGController and devices
    public ActivityLog(String device, String activity) {
        this.device = device;
        this.activity = activity;
        this.dateTime =
                new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
                        .format(new Date());
    }

    // Simple 1-argument constructor used by User, HGController, devices
    public ActivityLog(String message) {
        this("SYSTEM", message);
    }

    // Getters for the ActivityLogPageController
    public String getDevice() { return device; }
    public String getActivity() { return activity; }
    public String getDateTime() { return dateTime; }
}
