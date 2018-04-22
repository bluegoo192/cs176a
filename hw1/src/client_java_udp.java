import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.*;
import java.nio.ByteBuffer;

/**
 * Created by Arthur on 4/22/18.
 * Lines marked '*' are taken from course lecture slides
 */
public class client_java_udp {

    private BufferedReader input;
    private DatagramSocket socket;
    private int port;
    private InetAddress ip;
    private String command;

    public client_java_udp() {
        input = new BufferedReader(new InputStreamReader(System.in));  // *
    }

    public void run() throws IOException {
        boolean connected = connect();
        if (!connected) return;

        // get command from user
        System.out.println("Enter command: ");
        command = input.readLine();
        System.out.println();

        boolean sendCommandSuccess = send(command);
        if (!sendCommandSuccess) {
            System.out.println("Failed to send command. Terminating.");
            return;
        }
    }

    /**
     * Gather connection info from user and establish socket connection
     * @return whether or not connection was successful
     * @throws IOException if cannot read input from user (does NOT throw exception for connection failure)
     */
    private boolean connect() throws IOException {
        // get hostname
        System.out.println("Enter server name or IP address:  ");
        String hostname = input.readLine();
        if (!validateIp(hostname)) {
            System.err.println("Could not connect to server.");
            return false;
        }

        // get port
        System.out.println("Enter port:  ");
        String portString = input.readLine();
        try {
            port = Integer.parseInt(portString);
        } catch (NumberFormatException e) {
            System.err.println("Invalid port number.");
            return false;
        }
        if (!validatePort(port)) {
            System.err.println("Invalid port number.");
            return false;
        }

        // connect
        try {
            socket = new DatagramSocket(); // * client socket for server connection
            ip = InetAddress.getByName(hostname);
        } catch (IOException e) {
            System.err.println("Could not connect to server.");
            return false;
        }

        return true;
    }

    private boolean send(String message) throws IOException {
        byte[] receiveData = new byte[1024];  // *
        byte[] sendData = message.getBytes();  // *
        byte[] length = ByteBuffer.allocate(4).putInt(sendData.length).array();  // from https://stackoverflow.com/a/2183279

        DatagramPacket lengthPacket = new DatagramPacket(length, length.length, ip, port);
        DatagramPacket packet = new DatagramPacket(sendData, sendData.length, ip, port);  // *
        socket.send(lengthPacket);
        socket.send(packet);  // *

        // receive a packet in return
        DatagramPacket receipt = new DatagramPacket(receiveData, receiveData.length);  // *
        boolean success = receivePacket(receipt, 1);
        if (!success) return false;

        System.out.println("Received packet from server: ");
        System.out.println(new String(receipt.getData()));

        return true;
    }

    private boolean receivePacket(DatagramPacket receipt, int tries) {
        try {
            socket.setSoTimeout(1000);  // set a 1-second timeout for our ACK
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Failed to set socket timeout?!?!");
            return false;
        }

        try {
            socket.receive(receipt);
        } catch (Exception se) {
            System.out.println("Failed, retrying...");
            if (tries > 2) return false;
            return receivePacket(receipt, tries+1);
        }
        return true;
    }

    private boolean validateIp(String address) {
        return (address.startsWith("localhost"));
    }

    private boolean validatePort(int port) {
        return true;
    }

    public static void main(String[] args) {
        client_java_udp client = new client_java_udp();
        try {
            client.run();
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Could not fetch file.");
        }
    }

}
