
/**
 * Author: Rawan Genina
 * Student Number: 1196208
 * 
 * Class: ConnectionToClient
 * 
 * Description:
 * This class represents a single client connection to the server.
 * It handles receiving messages from the client, sending messages to the client,
 * and notifying the server of client events like connect, disconnect, or exceptions.
 * Implements Runnable so each client can run in its own thread.
 */

import java.io.*;
import java.net.Socket;

public class ConnectionToClient implements Runnable {

    // ---- CONNECTION PROPERTIES ----
    private Socket socket;                 // Client socket
    private ObjectInputStream in;          // Input stream from client
    private ObjectOutputStream out;        // Output stream to client
    private AbstractServer server;         // Reference to the server
    private boolean running = true;        // True while connection is active

    /**
     * Constructor initializes the connection with the socket and server reference
     * @param socket The client socket
     * @param server The server handling this client
     * @throws IOException If there is an error creating input/output streams
     */
    public ConnectionToClient(Socket socket, AbstractServer server) throws IOException {
        this.socket = socket;
        this.server = server;

        // Java requires ObjectOutputStream to be created first
        this.out = new ObjectOutputStream(socket.getOutputStream());
        this.out.flush();
        this.in = new ObjectInputStream(socket.getInputStream());
    }

    /**
     * Main client loop - listens for messages from client and sends them to the server
     */
    @Override
    public void run() {
        server.clientConnected(this); // Callback: client connected

        try {
            while (running) {
                Object msg = in.readObject(); // Wait for a message from client
                server.handleMessageFromClient(msg, this); // Pass message to server
            }
        } catch (Exception e) {
            running = false;
            server.clientException(this, e); // Callback: client exception
        } finally {
            server.clientDisconnected(this); // Callback: client disconnected
            close(); // Ensure connection is closed
        }
    }

    /**
     * Send a message to the client
     * @param msg The message object to send
     */
    public void sendToClient(Object msg) {
        try {
            out.writeObject(msg);
            out.flush();
        } catch (IOException e) {
            server.clientException(this, e); // Callback: client exception
        }
    }

    /**
     * Close this client connection
     */
    public void close() {
        running = false;
        try {
            socket.close(); // Close underlying socket
        } catch (IOException ignore) {}
    }
}
