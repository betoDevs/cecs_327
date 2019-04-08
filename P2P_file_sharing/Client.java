import java.net.*;
import java.io.*;

public class Client {
	// for connection to server
    private Socket clientSocket;
    private PrintWriter out;
    private BufferedReader in;
    // for asking for files
    private Socket client_leech;
    private PrintWriter out_file;
    private BufferedReader in_file;
    private String path_to_dir;
    private String my_id;
    private String ip;
 
 	public Client(String path_to_dir){
 		this.path_to_dir = path_to_dir;
 	}
    public void startConnection(String ip, int port) throws Exception{
    	this.ip = ip;
        clientSocket = new Socket(ip, port);
        out = new PrintWriter(clientSocket.getOutputStream(), true);
        in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
        setId(sendMessage("Request ID"));

        // Sets up a recieving interface at port 'my_id'
        Thread connection = new Thread(new Connection(Integer.parseInt(my_id), path_to_dir));
        connection.start();

        // Sets up a UDP that sends every 20 seconds to server
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

    public void sendInfo(String data){
    	out.println(data);
    }

    public String publishFiles() throws Exception {
    	String status;
    	File[] files = new File(path_to_dir).listFiles();
    	System.out.println("Files length: " + files.length);
    	status = sendMessage(""+files.length);
    	if("ready".equals(status))
    		System.out.println("Server ready to recieve: " + files.length + " files.");
    	else
    		return status;
    	for(File file : files) {
    		sendInfo(file.getName());
    	}
    	status = sendMessage("What is your status");
    	return status;
    }

    public String searchFiles(String file) throws Exception {
    	String result = sendMessage(file);
    	if (result.equals("-1"))
    		return "-1";
    	else
    		return result;
    }

    public String fetchFile(String file, int seed_id) throws Exception {
    	String file_in;
    	File file_to_be_added;
    	// Establish connetion with seeder
    	client_leech = new Socket(ip, seed_id);
    	out_file = new PrintWriter(client_leech.getOutputStream(), true);
        in_file = new BufferedReader(new InputStreamReader(client_leech.getInputStream()));

        // Send request and recieve answer
        out_file.println(file);
        file_in = in_file.readLine();

        // check if we got "-1"
        if(file_in.equals("-1"))
        	return "Not found";

        // else add to our dir
        return file_in;

    }

    public void setId(String id) { my_id = id; }
    public String getId() {return my_id; }
}