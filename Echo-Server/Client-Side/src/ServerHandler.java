import java.io.*;
import java.net.Socket;

public class ServerHandler implements Runnable {

    private int port;
    private DataInputStream in;
    private DataOutputStream out;
    private Socket server;

    public ServerHandler(int port) {
        this.port = port;
    }

    @Override
    public void run() {
        try {
            server = new Socket("", port);
            in = new DataInputStream(server.getInputStream());
            out = new DataOutputStream(server.getOutputStream());
            BufferedReader br = new BufferedReader(new InputStreamReader(System.in));

            while (!server.isInputShutdown()) {
                if (br.ready()) {
                    String query = br.readLine();
                    out.writeUTF(query);
                    out.flush();
                }
                if (in.available() > 0) {
                    System.out.println(in.readUTF());
                }
            }

            br.close();
            in.close();
            out.close();
            server.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
