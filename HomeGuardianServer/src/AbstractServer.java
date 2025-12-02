

/**
 * Author: Rawan Genina
 * Student Number: 1196208
 * 
 * Class: AbstractServer
 * 
 * Description:
 * This is an abstract base class for a server that listens for client connections.
 * It handles accepting clients, managing client connections, sending messages
 * to all clients, and provides callbacks for subclasses to handle client messages
 * and server events.
 * 
 * Subclasses must implement handleMessageFromClient() to define specific
 * server behavior for incoming messages.
 */

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public abstract class AbstractServer {

    // ---- SERVER PROPERTIES ----
    private int port;                     // Port number server listens on
    private ServerSocket serverSocket;    // Server socket object
    private boolean listening = false;    // True if server is currently listening

    // List of connected clients
    protected final List<ConnectionToClient> clients = new ArrayList<>();

    /**
     * Constructor to initialize the server with a port
     */
    public AbstractServer(int port) {
        this.port = port;
    }

    // NEW: allow subclasses (like Server) to access the port
    public int getPort() { return port; }

    // ---- SERVER LISTENING METHODS ----

    /**
     * Start listening for incoming client connections
     */
    public void listen() throws IOException {
        if (listening) return; // Already listening

        serverSocket = new ServerSocket(port);
        listening = true;
        serverStarted(); // Callback: server started

        // Thread to accept incoming connections continuously
        Thread acceptThread = new Thread(() -> {
            try {
                while (listening) {
                    Socket clientSocket = serverSocket.accept(); // Wait for client
                    ConnectionToClient client = new ConnectionToClient(clientSocket, this);

                    // Add client to list
                    synchronized (clients) {
                        clients.add(client);
                    }

                    // Start a thread for handling this client
                    new Thread(client).start();
                }
            } catch (Exception e) {
                if (listening) listeningException(e); // Callback: error while listening
            }
        });

        acceptThread.start();
    }

    /**
     * Stop listening for new connections
     */
    public void stopListening() {
        listening = false;
        try { serverSocket.close(); } catch (IOException ignore) {}
        serverStopped(); // Callback: server stopped
    }

    /**
     * Close the server and all client connections
     */
    public void close() {
        stopListening();

        synchronized (clients) {
            for (ConnectionToClient c : clients) {
                c.close(); // Close individual client connections
            }
            clients.clear();
        }

        serverClosed(); // Callback: server fully closed
    }

    // ---- CLIENT COMMUNICATION ----

    /**
     * Send a message to all connected clients
     */
    public void sendToAllClients(Object msg) {
        synchronized (clients) {
            for (ConnectionToClient c : clients) {
                c.sendToClient(msg);
            }
        }
    }

    /**
     * Get a specific client connection by index
     */
    public ConnectionToClient getClientConnection(int index) {
        synchronized (clients) {
            return clients.get(index);
        }
    }

    // ---- ABSTRACT CALLBACK TO IMPLEMENT IN SUBCLASS ----

    /**
     * Must be implemented by subclass to handle messages from clients
     */
    protected abstract void handleMessageFromClient(Object msg, ConnectionToClient client);

    // ---- OPTIONAL CALLBACKS ----
    protected void clientConnected(ConnectionToClient client) {}
    protected void clientDisconnected(ConnectionToClient client) {}
    protected void clientException(ConnectionToClient client, Exception e) {}

    protected void serverStarted() {}
    protected void serverStopped() {}
    protected void serverClosed() {}
    protected void listeningException(Exception e) {}
}
