import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;

/**
 * Created by Arthur on 4/21/18.
 * Lines with a '*' comment have been adapted from course lecture slides
 */
public class client_java_tcp {

    private BufferedReader input;
    private Socket socket;
    private boolean debug = true;

    public client_java_tcp() {
        input = new BufferedReader(new InputStreamReader(System.in));  // *
    }

    /**
     * Gather connection info from user and establish socket connection
     * @return whether or not connection was successful
     * @throws IOException if cannot read input from user (does NOT throw exception for connection failure)
     */
    public boolean connect() throws IOException {
        // get hostname
        System.out.println("Enter server name or IP address:  ");
        String hostname = input.readLine();
        if (!validateIp(hostname)) {
            System.err.println("Could not connect to server");
            return false;
        }

        // get port
        System.out.println("Enter port:  ");
        String portString = input.readLine();
        int port;
        try {
            port = Integer.parseInt(portString);
        } catch (NumberFormatException e) {
            System.err.println("Invalid port number");
            return false;
        }
        if (!validatePort(port)) {
            System.err.println("Invalid port number");
            return false;
        }

        // connect
        try {
            socket = new Socket(hostname, port); // * client socket for server connection
        } catch (IOException e) {
            System.err.println("Could not connect to server");
            return false;
        }

        if (debug) System.out.println("  > Connected!");
        return true;
    }

    /**
     * Get command from user, send to server, and print response
     * @throws IOException if anything goes wrong
     */
    public void sendCommand() throws IOException {
        DataOutputStream out = new DataOutputStream(socket.getOutputStream()); // *
        BufferedReader response = new BufferedReader(new InputStreamReader(socket.getInputStream()));  // *

        // get command from user
        System.out.println("Enter command: ");
        String command = input.readLine();

        // send it
        out.writeBytes(command+"\n");  // *
        if (debug) System.out.println("  > sent bytes");

        // print response
        String line = response.readLine();
        StringBuilder res =  new StringBuilder();
        while (line != null) {
            System.out.println(line);
            res.append(line);
            line = response.readLine();
        }
        socket.close(); // close connection
    }

    private boolean validateIp(String address) {
        return (address.startsWith("localhost"));
    }

    private boolean validatePort(int port) {
        return true;
    }

    public static void main(String args[]) throws IOException {
        client_java_tcp client = new client_java_tcp();
        boolean connected = client.connect();
        if (!connected) return;
        try {
            client.sendCommand();
        } catch (IOException e) {
            System.err.println("Failed to send command.  Terminating.");
        }
    }
}