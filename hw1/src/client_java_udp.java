import javax.xml.crypto.Data;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.*;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collections;

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
    private boolean debug = true;

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
        System.out.println("sent command");

        //boolean saveFileSuccess = saveFileFromServer(parseFileName(command));
    }

    private boolean saveFileFromServer(String filename) throws IOException {
        // set up
        byte[] receiveData = new byte[1024];
        byte[] sendData = new byte[1024];
        socket.setSoTimeout(0); // infinite timeout so the server can do its work

        // get file length
        DatagramPacket res = new DatagramPacket(receiveData, receiveData.length);
        socket.receive(res);
        int expectedLength = ByteBuffer.wrap(res.getData()).getInt();
        int bytesReceived = 0;
        sendAck(res);

        // receive incoming packets
        socket.setSoTimeout(500);
        ArrayList<Byte> data = new ArrayList<>();
        while (bytesReceived < expectedLength) {
            res = new DatagramPacket(new byte[1024], 1024);
            socket.receive(res);
            for (int i=0; i<res.getData().length; i++) {
                data.add(res.getData()[i]);
            }
            bytesReceived += res.getData().length;
        }
        sendAck(res);




        // save file

        return true;
    }

    private void sendAck(DatagramPacket received) throws IOException {
        String response = new String(received.getData());
        response = "ACK: "+response;
        byte[] sendData = response.getBytes();
        DatagramPacket send = new DatagramPacket(sendData, sendData.length, received.getAddress(), received.getPort());
        socket.send(send);
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

    /**
     * Sends a packet containing the length of the data to send, then the data, then waits for ACK
     * Tries up to 3 times
     * @param message the message to send
     * @return whether sending was successful (if ACK was received)
     * @throws IOException
     */
    private boolean send(String message) throws IOException { return send(message, 0); }
    private boolean send(String message, int tries) throws IOException {
        if (tries > 2) return false;
        byte[] receiveData = new byte[1024];  // *
        byte[] sendData = message.getBytes();  // *
        byte[] length = ByteBuffer.allocate(4).putInt(sendData.length).array();  // from https://stackoverflow.com/a/2183279

        // send length then data
        DatagramPacket lengthPacket = new DatagramPacket(length, length.length, ip, port);
        DatagramPacket packet = new DatagramPacket(sendData, sendData.length, ip, port);  // *
        socket.send(lengthPacket);
        socket.send(packet);  // *

        // wait for ACK
        DatagramPacket receipt = new DatagramPacket(receiveData, receiveData.length);  // *
        socket.setSoTimeout(1000);
        try {
            socket.receive(receipt);
        } catch (Exception se) {
            System.out.println("failed, retrying...");
            return send(message, tries+1);
        }

        return true;
    }

    private boolean validateIp(String address) {
        return (address.startsWith("localhost"));
    }

    private boolean validatePort(int port) {
        return true;
    }

    private String parseFileName(String command) throws IOException {
        String[] pieces = command.split(">");
        String filename = null;
        if (pieces.length > 1) {
            filename = pieces[pieces.length - 1].trim();
        }
        if (filename == null) throw new IOException(); // just in case
        return filename;
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
