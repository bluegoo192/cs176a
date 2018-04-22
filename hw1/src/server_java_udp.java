import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;

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
            DatagramPacket receipt = new DatagramPacket(receiveData, receiveData.length);  // *
            socket.receive(receipt);  // *

            String response = new String(receipt.getData());
            response = "You said: "+response;
            sendData = response.getBytes();

            DatagramPacket sendPacket =
                    new DatagramPacket(sendData, sendData.length, receipt.getAddress(), receipt.getPort());

            socket.send(sendPacket);
        }
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
