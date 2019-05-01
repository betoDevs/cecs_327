import java.net.*;
import java.util.TimerTask;
import java.io.*;
import java.util.Timer;

public class Client {
	// for connection to server
    private Socket clientSocket;
    private PrintWriter out;
    private BufferedReader in;
    private Thread connection;
    private Connection c;
    // for asking for files
    private Socket client_leech;
    private PrintWriter out_file;
    private BufferedReader in_file;
    private String path_to_dir;
    private String my_id;
    private String ip;
    private Timer timer = new Timer();
    private final int timer_value = 4500;
 
 	public Client(String path_to_dir){
 		this.path_to_dir = path_to_dir;
 	}
    public void startConnection(String ip, int port) throws Exception{
    	this.ip = ip;
        clientSocket = new Socket(ip, port);
        out = new PrintWriter(clientSocket.getOutputStream(), true);
        in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
        setId(sendMessage("Request ID"));

        // Sets up a receiving interface at port 'my_id'
        c = new Connection(Integer.parseInt(my_id), path_to_dir);
        connection = new Thread(c);
        connection.start();

        // Sets up a UDP that sends every 20 seconds to server
    }
 
    public String sendMessage(String msg) throws Exception {
        out.println(msg);
        String resp = in.readLine();
        return resp;
    }
 
    public void stopConnection() throws Exception {
    	// shut down connection
        client_leech = new Socket(ip, Integer.parseInt(my_id));
        out_file = new PrintWriter(client_leech.getOutputStream(), true);
        in_file = new BufferedReader(new InputStreamReader(client_leech.getInputStream()));
        out_file.println("-1");
        //System.out.println(in_file.readLine());

        // and close my sockets
        out.close();
        out_file.close();
        in.close();
        in_file.close();
        client_leech.close();
        clientSocket.close();
        timer.cancel();

        //wait until thread is done and return
        connection.join();
        System.out.println("Exiting Client.java");
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
    	status = sendMessage("status");
    	return status;
    }

    public String searchFiles(String file) throws Exception {
    	String result = sendMessage(file);
    	if (result.equals("-1"))
    		return "-1";
    	else
    		return result;
    }

    //sendHello to udp
    public void sendHello() throws Exception
    {
    		//join the UDP
    		DatagramSocket udpSocket = new DatagramSocket(); 
    		InetAddress ip = InetAddress.getLocalHost(); 
    		
    		//buffer to hold message
        byte buffer[] = null; 
        String hello = getId().toString() + "/" + "Hello";
        
        //convert to byte to send
        buffer = hello.getBytes();
        DatagramPacket udpSend = new DatagramPacket(buffer, buffer.length, ip, 1234);
        udpSocket.send(udpSend);
        //System.out.println(getId().toString() +"Hello is sended");
    }
    
    //send Hello to server every 200 seconds
    public void sendingHello() throws Exception
	{
    	TimerTask y = new TimerTask() {

            @Override
            public void run() {
            	try{
					sendHello();
				} catch(Exception e) {
					System.out.println(e);
				};
                //System.out.println("Sending Hello");
            }
        };
        
        //modify this to change the seconds period
        timer.schedule(y,0, timer_value);
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

        // close P2P connection
        out_file.close();
        in_file.close();
        client_leech.close();

        // check if we got "-1"
        if(file_in.equals("-1"))
        	return "Not found";

        // else add to our dir
        else {
            return createFile(file_in, file);
        }
    }

    public String createFile(String contents, String file_name){
        try{
            File destinaton = new File(path_to_dir+file_name);
            Writer writer = new FileWriter(destinaton);
            writer.write(contents);
            writer.close();
            return "Sucess!";
        } catch(Exception e) {
            return "Error writing to dir: " + e;
        }
    }
    public void setId(String id) { my_id = id; }
    public String getId() {return my_id; }
}