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
    private String hostname;
    private Socket socket;
    private boolean debug = true;

    public client_java_tcp() {
        input = new BufferedReader(new InputStreamReader(System.in));  // *
    }

    public void connect() throws IOException {
        hostname = promptIp();
        int port = 3300;
        socket = new Socket(hostname, port); // * client socket for server connection
        if (debug) System.out.println("Connected!");
    }

    public String sendCommand() throws IOException {
        DataOutputStream out = new DataOutputStream(socket.getOutputStream()); // *
        BufferedReader response = new BufferedReader(new InputStreamReader(socket.getInputStream()));  // *

        System.out.println("Enter command: ");
        String command = input.readLine();

        out.writeBytes(command+"\n");  // *
        if (debug) System.out.println("sent bytes");
        String res = response.readLine();
        System.out.println("response: "+res);
        socket.close();
        return res;
    }

    private boolean validateIp(String address) {
        return (address.startsWith("localhost"));
    }

    /**
     * Ask user for server's IP address
     * @return valid IP address
     */
    private String promptIp() throws IOException {
        System.out.println("Enter server name or IP address:  ");
        String userInput = input.readLine();
        boolean valid = validateIp(userInput);
        while (!valid) {
            System.out.println("Sorry, "+userInput+" doesn't appear to be a valid IP.  Please try again:  ");
            userInput = input.readLine();
            valid = validateIp(userInput);
        }
        return userInput;
    }


    public static void main(String args[]) throws IOException {
        client_java_tcp client = new client_java_tcp();
        client.connect();
        client.sendCommand();
    }
}