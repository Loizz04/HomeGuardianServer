
public class HomeGuardianServerMain {

    public static void main(String[] args) {

        System.out.println("=== Home Guardian Server Starting ===");

        //Create the controller
        HGController controller = new HGController();

        //Create and register devices
        //Match constructors
        SmartLight livingLight = new SmartLight(1, "Living Room Light");
        SmartLock doorLock     = new SmartLock(1, "Front Door Lock");
        Alarm alarm            = new Alarm(1, "Home Alarm");
        SecurityCamera camera  = new SecurityCamera(1, "Door Camera");

        controller.addDevice(livingLight);
        controller.addDevice(doorLock);
        controller.addDevice(alarm);
        controller.addDevice(camera);

        System.out.println("[SETUP] All devices registered.");

        //Create and register users
        HomeAdmin admin = new HomeAdmin(
                "Admin User", "admin1", "admin@example.com", "hash123", true);

        HomeGuest guest = new HomeGuest(
                "Guest User", "guest1", "guest@example.com", "hash456");

        admin.signup(controller);
        guest.signup(controller);

        System.out.println("[SETUP] Users registered.");

        //Start the server
        int PORT = 12345;
        Server server = new Server(PORT, controller);

        System.out.println("[SERVER] Starting server on port " + PORT + "...");
        server.startServer();

        System.out.println("=== Home Guardian Server is now running ===");
    }
}
