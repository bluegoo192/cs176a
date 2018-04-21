import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;

/**
 * Created by Arthur on 4/21/18.
 */
public class client_java_tcp {

    public static void main(String args[]) throws IOException {
        // Following code (until 'END') adapted from course lecture slides
        BufferedReader input = new BufferedReader(new InputStreamReader(System.in));
        System.out.println("Attempting to connect to "+args[0]);
        Socket socket = new Socket(args[0], 3300); // client socket for connecting to server
        System.out.println("Connected!");

        DataOutputStream out = new DataOutputStream(socket.getOutputStream());

        BufferedReader response = new BufferedReader(new InputStreamReader(socket.getInputStream()));

        String sentence = input.readLine();
        out.writeBytes(sentence+"\n");

        System.out.println(response.readLine());

        socket.close();

        // END
    }
}
