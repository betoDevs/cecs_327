import java.net.*;
import java.io.*;

public class Client {
    private Socket clientSocket;
    private PrintWriter out;
    private BufferedReader in;
    private String path_to_dir;
    private int my_id;
 
    public void startConnection(String ip, int port, String path_to_dir) throws Exception{
        clientSocket = new Socket(ip, port);
        my_id = null;
        this.path_to_dir = path_to_dir;
        out = new PrintWriter(clientSocket.getOutputStream(), true);
        in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
    }
 
    public String sendMessage(String msg) throws Exception {
        out.println(msg);
        String resp = in.readLine();
        return resp;
    }
 
    public void stopConnection() throws IOException {
        in.close();
        out.close();
        clientSocket.close();
    }

    public void set_id(int id) { my_id = id; }
    public int get_id() {return my_id; }
}