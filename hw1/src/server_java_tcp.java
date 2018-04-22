import java.io.*;
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
            // get command
            Socket connection = socket.accept();
            BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            DataOutputStream out = new DataOutputStream(connection.getOutputStream());
            input = in.readLine();

            // run command
            Runtime rt = Runtime.getRuntime(); // copied from https://stackoverflow.com/a/8496537
            Process p = rt.exec(input); // run and send output to file
            BufferedReader commandOutput = new BufferedReader(new InputStreamReader(p.getInputStream()));

            // get file name
            String[] pieces = input.split(">");
            String filename = null;
            if (pieces.length > 1) {
                filename = pieces[pieces.length - 1].trim();
            }

            if (filename == null) {
                throw new IOException(); // just in case
            }

            // write file
            File f = new File(filename);
            BufferedWriter writer = new BufferedWriter(new FileWriter(f));

            String line = commandOutput.readLine();
            while (line != null) {
                writer.write(line);
                System.out.println(line);
                line = commandOutput.readLine();
            }
            writer.close();

            System.out.println("Received input: "+input);
            out.writeBytes("You said "+input + "\n");
            out.writeBytes("wrote to "+filename+"\n");


        }
        // END
    }
}
