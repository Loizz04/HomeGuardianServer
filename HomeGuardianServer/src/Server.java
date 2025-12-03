import java.util.ArrayList;

/**
 * Author: Rawan Genina
 * Student Number: 1196208
 * 
 * Class: Server
 * 
 * Description:
 * This class extends AbstractServer and implements a concrete server
 * for handling smart home device commands. It interacts with HGController
 * to control devices and manage client connections.
 */
public class Server extends AbstractServer {

    private HGController controller; // Reference to the controller managing devices

    public Server(int port, HGController controller) {
        super(port);
        this.controller = controller;
    }

    public void startServer() {
        try {
            listen();
        } catch (Exception e) {
            System.out.println("Error starting server: " + e.getMessage());
        }
    }

    public void stopServer() {
        close();
    }

    @Override
    protected void handleMessageFromClient(Object msg, ConnectionToClient client) {
        try {
            if (msg instanceof String) {
                handleLegacyStringMessage((String) msg, client);
                return;
            }

            if (msg instanceof ArrayList<?>) {
                handleListCommand((ArrayList<?>) msg, client);
                return;
            }

            System.out.println("Received unsupported message type from client: " + msg);
            controller.logActivity("Unsupported message type received from client: " + msg);

        } catch (Exception e) {
            e.printStackTrace();
            controller.logActivity("Error while handling message from client: " + e.getMessage());
        }
    }

    // ===========================================================================================
    //                               NEW LOGIN + SIGNUP HANDLING
    // ===========================================================================================

    @SuppressWarnings("rawtypes")
    private void handleLoginCommand(ArrayList list, ConnectionToClient client) {

        if (list.size() < 3) {
            sendLoginResult(client, "error", "Invalid login message.");
            return;
        }

        String username = String.valueOf(list.get(1));
        String password = String.valueOf(list.get(2));

        User user = controller.authenticateUser(username, password);

        if (user != null) {
            sendLoginResult(client, "success",
                    "Login successful. Welcome, " + user.getUserName() + "!");
        } else {
            sendLoginResult(client, "error", "Invalid username or password.");
        }
    }


    private void sendLoginResult(ConnectionToClient client, String status, String message) {
        try {
            ArrayList<Object> reply = new ArrayList<>();
            reply.add("loginResult");
            reply.add(status);
            reply.add(message);
            client.sendToClient(reply);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @SuppressWarnings("rawtypes")
    private void handleSignupCommand(ArrayList list, ConnectionToClient client) {

        if (list.size() < 5) {
            sendSignupResult(client, "error", "Invalid signup message.");
            return;
        }

        String name     = String.valueOf(list.get(1));
        String email    = String.valueOf(list.get(2));
        String username = String.valueOf(list.get(3));
        String password = String.valueOf(list.get(4));

        try {
            HomeGuest guest = controller.registerGuest(name, email, username, password);
            sendSignupResult(client, "success",
                    "Signup successful. Welcome, " + guest.getUserName() + "!");
        } catch (IllegalArgumentException ex) {
            sendSignupResult(client, "error", ex.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            sendSignupResult(client, "error", "Signup failed on server.");
        }
    }


    private void sendSignupResult(ConnectionToClient client, String status, String message) {
        try {
            ArrayList<Object> reply = new ArrayList<>();
            reply.add("signupResult");
            reply.add(status);
            reply.add(message);
            client.sendToClient(reply);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // ===========================================================================================
    //                               DEVICE COMMAND HANDLING
    // ===========================================================================================

    @SuppressWarnings("rawtypes")
    private void handleListCommand(ArrayList list, ConnectionToClient client) {

        if (list.isEmpty()) {
            controller.logActivity("Empty command list received from client.");
            return;
        }

        Object cmdObj = list.get(0);
        if (!(cmdObj instanceof String)) {
            controller.logActivity("First element of command list is not a String: " + cmdObj);
            return;
        }

        String command = ((String) cmdObj).toUpperCase();
        System.out.println("Command from client: " + command + "  | full: " + list);

        boolean success = false;
        Object response = null;

        try {
            switch (command) {

                // ---------------- AUTH ----------------
                case "LOGIN":
                    handleLoginCommand(list, client);
                    return;

                case "SIGNUP":
                    handleSignupCommand(list, client);
                    return;

                // ---------------- LIGHTS ----------------
                case "TOGGLE_LIGHT": {
                    int id = (int) list.get(1);
                    boolean on = (boolean) list.get(2);
                    success = controller.toggleLight("light" + id, on);
                    break;
                }

                case "SET_LIGHT_BRIGHTNESS": {
                    int id = (int) list.get(1);
                    int value = (int) list.get(2);
                    success = controller.setLightBrightness("light" + id, value);
                    break;
                }

                case "SET_LIGHT_COLOR": {
                    int id = (int) list.get(1);
                    int r = (int) list.get(2);
                    int g = (int) list.get(3);
                    int b = (int) list.get(4);
                    success = controller.setLightColor("light" + id, r, g, b);
                    break;
                }

                case "SET_LIGHT_TIMEOUT": {
                    int id = (int) list.get(1);
                    int minutes = (int) list.get(2);
                    success = controller.setLightTimeout("light" + id, minutes);
                    break;
                }

                case "SET_LIGHT_MOTION_LINK": {
                    int id = (int) list.get(1);
                    boolean link = (boolean) list.get(2);
                    success = controller.toggleLightMotionLink("light" + id, link);
                    break;
                }

                // ---------------- LOCKS ----------------
                case "TOGGLE_LOCK": {
                    int id = (int) list.get(1);
                    boolean locked = (boolean) list.get(2);
                    success = controller.toggleLock("lock" + id, locked);
                    break;
                }

                case "SET_LOCK_DURATION": {
                    int id = (int) list.get(1);
                    int minutes = (int) list.get(2);
                    success = controller.setLockDuration("lock" + id, minutes);
                    break;
                }

                case "LINK_LOCK_TO_ALARM": {
                    int lockId = (int) list.get(1);
                    int alarmId = (int) list.get(2);
                    boolean link = (boolean) list.get(3);
                    success = controller.linkLockToAlarm("lock" + lockId, "alarm" + alarmId, link);
                    break;
                }

                // ---------------- CAMERAS ----------------
                case "TOGGLE_CAMERA": {
                    int id = (int) list.get(1);
                    boolean on = (boolean) list.get(2);
                    success = controller.toggleCamera("camera" + id, on);
                    break;
                }

                case "TOGGLE_CAMERA_RECORDING": {
                    int id = (int) list.get(1);
                    boolean on = (boolean) list.get(2);
                    success = controller.toggleCameraRecording("camera" + id, on);
                    break;
                }

                case "TOGGLE_CAMERA_MOTION": {
                    int id = (int) list.get(1);
                    boolean on = (boolean) list.get(2);
                    success = controller.toggleCameraMotion("camera" + id, on);
                    break;
                }

                case "REQUEST_CAMERA_FOOTAGE": {
                    int id = (int) list.get(1);
                    String range = (String) list.get(2);
                    controller.requestCameraFootage("camera" + id, range);
                    success = true;
                    break;
                }

                // ---------------- ALARMS ----------------
                case "TOGGLE_ALARM": {
                    int id = (int) list.get(1);
                    boolean on = (boolean) list.get(2);
                    success = controller.toggleAlarmWithString("alarm" + id, on);
                    break;
                }

                case "TOGGLE_ALARM_MOTION": {
                    int id = (int) list.get(1);
                    boolean on = (boolean) list.get(2);
                    success = controller.toggleAlarmMotion("alarm" + id, on);
                    break;
                }

                case "TOGGLE_ALARM_RECORD_ON_CAM": {
                    int aId = (int) list.get(1);
                    int cId = (int) list.get(2);
                    boolean on = (boolean) list.get(3);
                    success = controller.toggleAlarmRecordOnCam("alarm" + aId, "camera" + cId, on);
                    break;
                }

                // ---------------- MOTION SENSOR ----------------
                case "SET_MOTION_SENSITIVITY": {
                    int value = (int) list.get(1);
                    success = controller.setMotionSensitivity(value);
                    break;
                }

                // ---------------- ACTIVITY LOGS ----------------
                case "GET_LOGS": {
                    response = controller.getAllLogs();
                    success = true;
                    break;
                }

                default:
                    controller.logActivity("Unknown command from client: " + command);
                    success = false;
            }

        } catch (Exception e) {
            e.printStackTrace();
            controller.logActivity("Exception in handleListCommand for command " + command + ": " + e);
            success = false;
        }

        try {
            if (response != null)
                client.sendToClient(response);
            else
                client.sendToClient(success ? "OK" : "ERROR");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // ===========================================================================================

    private void handleLegacyStringMessage(String msg, ConnectionToClient client) {
        System.out.println("Legacy message from client: " + msg);
        String[] parts = msg.trim().split("\\s+");
        if (parts.length < 2) {
            controller.logActivity("Invalid legacy command: " + msg);
            return;
        }

        String deviceId = parts[0];
        String command = parts[1];

        boolean success = controller.controlDevice(deviceId, command);
        try {
            client.sendToClient(success ? "OK" : "ERROR");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void serverStarted() {
        System.out.println("Server started on port " + getPort());
    }

    @Override
    protected void serverStopped() {
        System.out.println("Server stopped.");
    }
}
