import java.io.*;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.nio.ByteBuffer;

/**
 * Created by Arthur on 4/22/18.
 * Lines marked '*' are taken from lecture slides
 */
public class server_java_udp {

    private DatagramSocket socket;

    public void run() throws IOException {
        try {
            socket = new DatagramSocket(3300);  // *
        } catch (SocketException e) {
            e.printStackTrace();
            return;
        }

        byte[] receiveData = new byte[1024];  // *
        byte[] sendData = new byte[1024];  // *

        while (true) {
            // get length of command packet from client
            DatagramPacket receipt = new DatagramPacket(receiveData, receiveData.length);  // *
            socket.receive(receipt);  // *
            int expectedLength = ByteBuffer.wrap(receipt.getData()).getInt();

            // get command
            socket.setSoTimeout(500);
            receipt = new DatagramPacket(new byte[1024], 1024);
            try {
                socket.receive(receipt);
                if (receipt.getLength() != expectedLength) throw new SocketTimeoutException();
            } catch (SocketTimeoutException e) {
                System.out.println("Failed getting instructions from the client.");
                continue;
            }
            sendAck(receipt);

            // get file name
            String[] pieces = new String(receipt.getData()).split(">");
            String filename = null;
            if (pieces.length > 1) {
                filename = pieces[pieces.length - 1].trim();
            }

            if (filename == null) {
                throw new IOException(); // just in case
            }

            // reconstruct original command
            String command = pieces[0];
            for (int i=1; i<pieces.length - 2; i++) {
                command = command + ">" + pieces[i]; // original command COULD contain >'s
            }

            // run command
            Runtime rt = Runtime.getRuntime(); // copied from https://stackoverflow.com/a/8496537
            Process p = rt.exec(command); // run and send output to file
            BufferedReader commandOutput = new BufferedReader(new InputStreamReader(p.getInputStream()));

            // write file
            File f = new File(filename);
            FileWriter writer = new FileWriter(f);

            String line = commandOutput.readLine();
            while (line != null) {
                writer.write(line+"\n");
                System.out.println(line);
                line = commandOutput.readLine();
            }
            writer.close();

            // send size of file
            byte[] size = ByteBuffer.allocate(8).putLong(f.length()).array();  // from https://stackoverflow.com/a/2183279
            DatagramPacket sizePacket = new DatagramPacket(size, size.length, receipt.getAddress(), receipt.getPort());
            socket.send(sizePacket);
            // wait for ACK
            socket.setSoTimeout(500);




            socket.setSoTimeout(0); // reset timeout
        }
    }

    private void sendAck(DatagramPacket received) throws IOException {
        String response = new String(received.getData());
        response = "ACK: "+response;
        byte[] sendData = response.getBytes();
        DatagramPacket send = new DatagramPacket(sendData, sendData.length, received.getAddress(), received.getPort());
        socket.send(send);
    }

    public static void main(String[] args) {
        server_java_udp server = new server_java_udp();
        try {
            server.run();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
