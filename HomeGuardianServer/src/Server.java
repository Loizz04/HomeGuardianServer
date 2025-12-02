
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

    /**
     * Constructor initializes the server with a port and controller
     * @param port Port number for server to listen on
     * @param controller Reference to HGController for device management
     */
    public Server(int port, HGController controller) {
        super(port);          // Call parent constructor
        this.controller = controller;
    }

    /**
     * Start the server and begin listening for client connections
     */
    public void startServer() {
        try {
            listen(); // AbstractServer method that starts the listener thread
        } catch (Exception e) {
            System.out.println("Error starting server: " + e.getMessage());
        }
    }

    /**
     * Stop the server and close all client connections
     */
    public void stopServer() {
        close(); // AbstractServer method to stop and clean up clients
    }

    /**
     * Handle messages received from clients
     * @param msg Message from the client
     * @param client ConnectionToClient object representing the sender
     */
    @Override
    protected void handleMessageFromClient(Object msg, ConnectionToClient client) {
        try {
            // Support old String-based protocol if you still use it anywhere
            if (msg instanceof String) {
                handleLegacyStringMessage((String) msg, client);
                return;
            }

            // New protocol: ArrayList<Object> from the JavaFX client
            if (msg instanceof ArrayList<?>) {
                handleListCommand((ArrayList<?>) msg, client);
                return;
            }

            // Unknown message type
            System.out.println("Received unsupported message type from client: " + msg);
            controller.logActivity("Unsupported message type received from client: " + msg);

        } catch (Exception e) {
            e.printStackTrace();
            controller.logActivity("Error while handling message from client: " + e.getMessage());
        }
    }

    /**
     * Handles the new structured ArrayList-based protocol from the JavaFX client.
     *
     * Expected general format:
     *   [String COMMAND, ...params]
     */
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
        Object response = null;  // you can use this to send a detailed reply

        try {
            switch (command) {

                // ------------- LIGHTS -------------

                case "TOGGLE_LIGHT": {
                    int lightId = (int) list.get(1);
                    boolean on = (boolean) list.get(2);
                    String deviceId = "light" + lightId;
                    success = controller.toggleLight(deviceId, on);
                    break;
                }

                case "SET_LIGHT_BRIGHTNESS": {
                    int lightId = (int) list.get(1);
                    int value = (int) list.get(2);
                    String deviceId = "light" + lightId;
                    success = controller.setLightBrightness(deviceId, value);
                    break;
                }

                case "SET_LIGHT_COLOR": {
                    int lightId = (int) list.get(1);
                    int r = (int) list.get(2);
                    int g = (int) list.get(3);
                    int b = (int) list.get(4);
                    String deviceId = "light" + lightId;
                    success = controller.setLightColor(deviceId, r, g, b);
                    break;
                }

                case "SET_LIGHT_TIMEOUT": {
                    int lightId = (int) list.get(1);
                    int minutes = (int) list.get(2);
                    String deviceId = "light" + lightId;
                    success = controller.setLightTimeout(deviceId, minutes);
                    break;
                }

                case "SET_LIGHT_MOTION_LINK": {
                    int lightId = (int) list.get(1);
                    boolean link = (boolean) list.get(2);
                    String deviceId = "light" + lightId;
                    success = controller.toggleLightMotionLink(deviceId, link);
                    break;
                }

                // ------------- LOCKS -------------

                case "TOGGLE_LOCK": {
                    int lockId = (int) list.get(1);
                    boolean locked = (boolean) list.get(2);
                    String deviceId = "lock" + lockId;
                    success = controller.toggleLock(deviceId, locked);
                    break;
                }

                case "SET_LOCK_DURATION": {
                    int lockId = (int) list.get(1);
                    int minutes = (int) list.get(2);
                    String deviceId = "lock" + lockId;
                    success = controller.setLockDuration(deviceId, minutes);
                    break;
                }

                case "LINK_LOCK_TO_ALARM": {
                    int lockId = (int) list.get(1);
                    int alarmId = (int) list.get(2);
                    boolean link = (boolean) list.get(3);
                    String lockDeviceId = "lock" + lockId;
                    String alarmDeviceId = "alarm" + alarmId;
                    success = controller.linkLockToAlarm(lockDeviceId, alarmDeviceId, link);
                    break;
                }

                // ------------- CAMERAS -------------

                case "TOGGLE_CAMERA": {
                    int camId = (int) list.get(1);
                    boolean on = (boolean) list.get(2);
                    String deviceId = "camera" + camId;
                    success = controller.toggleCamera(deviceId, on);
                    break;
                }

                case "TOGGLE_CAMERA_RECORDING": {
                    int camId = (int) list.get(1);
                    boolean on = (boolean) list.get(2);
                    String deviceId = "camera" + camId;
                    success = controller.toggleCameraRecording(deviceId, on);
                    break;
                }

                case "TOGGLE_CAMERA_MOTION": {
                    int camId = (int) list.get(1);
                    boolean on = (boolean) list.get(2);
                    String deviceId = "camera" + camId;
                    success = controller.toggleCameraMotion(deviceId, on);
                    break;
                }

                case "REQUEST_CAMERA_FOOTAGE": {
                    int camId = (int) list.get(1);
                    String range = (String) list.get(2);
                    String deviceId = "camera" + camId;
                    controller.requestCameraFootage(deviceId, range);
                    success = true;
                    break;
                }

                // ------------- ALARMS -------------

                case "TOGGLE_ALARM": {
                    int alarmId = (int) list.get(1);
                    boolean on = (boolean) list.get(2);
                    String deviceId = "alarm" + alarmId;
                    success = controller.toggleAlarmWithString(deviceId, on);
                    break;
                }

                case "TOGGLE_ALARM_MOTION": {
                    int alarmId = (int) list.get(1);
                    boolean on = (boolean) list.get(2);
                    String deviceId = "alarm" + alarmId;
                    success = controller.toggleAlarmMotion(deviceId, on);
                    break;
                }

                case "TOGGLE_ALARM_RECORD_ON_CAM": {
                    int alarmId = (int) list.get(1);
                    int camId = (int) list.get(2);
                    boolean on = (boolean) list.get(3);
                    String alarmDeviceId = "alarm" + alarmId;
                    String camDeviceId = "camera" + camId;
                    success = controller.toggleAlarmRecordOnCam(alarmDeviceId, camDeviceId, on);
                    break;
                }

                // ------------- MOTION SENSOR -------------

                case "SET_MOTION_SENSITIVITY": {
                    int value = (int) list.get(1);
                    success = controller.setMotionSensitivity(value);
                    break;
                }

                // ------------- ACTIVITY LOGS -------------

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

        // ------------- Send response back to this client -------------

        try {
            if (response != null) {
                client.sendToClient(response);
            } else {
                client.sendToClient(success ? "OK" : "ERROR");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Handles old string-based commands (backwards compatibility).
     * Example: "light1 ON"
     */
    private void handleLegacyStringMessage(String msg, ConnectionToClient client) {
        System.out.println("Legacy message from client: " + msg);

        // Very simple example: "deviceId COMMAND"
        String[] parts = msg.trim().split("\\s+");
        if (parts.length < 2) {
            controller.logActivity("Invalid legacy command: " + msg);
            return;
        }

        String deviceId = parts[0];   // e.g. "light1"
        String command = parts[1];    // e.g. "ON", "OFF", "LOCK", etc.

        boolean success = controller.controlDevice(deviceId, command);
        try {
            client.sendToClient(success ? "OK" : "ERROR");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Called when server has successfully started
     */
    @Override
    protected void serverStarted() {
        System.out.println("Server started on port " + getPort());
    }

    /**
     * Called when server has stopped
     */
    @Override
    protected void serverStopped() {
        System.out.println("Server stopped.");
    }
}
