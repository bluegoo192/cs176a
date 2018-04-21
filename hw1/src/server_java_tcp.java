import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Created by Arthur on 4/21/18.
 */
public class server_java_tcp {

    public static void main(String args[]) throws IOException {
        // Following code (until 'END') taken from course lecture slides
        ServerSocket socket = new ServerSocket(3300);
        String input;

        boolean keepRunning = true;

        while (keepRunning) {
            Socket connection = socket.accept();
            BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            DataOutputStream out = new DataOutputStream(connection.getOutputStream());

            input = in.readLine();
            System.out.println("Received input: "+input);
            out.writeBytes("You said "+input + "\n");
            out.writeBytes("test\n");
        }
        // END
    }
}
