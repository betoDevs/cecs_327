import java.net.*;
import java.util.TimerTask;
import java.io.*;
import java.util.Timer;

/*
* This class is created from 'Test.java'
* 'Client.java' will connect to the server and 
* handle requests to and from other Clients
*
* Two connections are opened to Server, a TCP and UDP.
*   TCP is used for requestion and publishing information
*   UDP is used as a pinging mechanism to inform server that 
*   'I' am still connected.
*
* Client to client communications (P2P), is handled thru a TCP
* connection that start on request and closes after request is done.  
*/
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

    // For sending udp messages
    private Timer timer = new Timer();
    private final int timer_value = 4500;
 
    // A path to the local folder of files available for sharing
 	public Client(String path_to_dir){
 		this.path_to_dir = path_to_dir;
 	}

    /* 
    * Connect to Server, recieve id, and open up a 'Connection.java' thread
    * Which will handle P2P communications
    * 
    *@params:
    * ip : String, the ip address of Server. In this case local machine
    * port : int, my port to handle P2P events.
    */
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
    }
 
    /*
    * function to facilitate the send message and get response mechanism
    * also the main form of communication across all P2P and Client/Server.
    *
    *@params
    * msg: String, message to send the server or other Peer.
    *
    *@return
    * resp: String, What the server responded.
    */
    public String sendMessage(String msg) throws Exception {
        out.println(msg);
        String resp = in.readLine();
        return resp;
    }
 
    /* 
    * First: close down connection to servers. 
    * Second: close down our 'Connection.java', used for P2P
    * Third close: the timer thread, which is used for UDP messaging
    *   wait for 'Connection.java' to finish and then return, thus ending 
    *   the service.
    */
    public void stopConnection() throws Exception {
    	// shut down sever connection
        client_leech = new Socket(ip, Integer.parseInt(my_id));
        out_file = new PrintWriter(client_leech.getOutputStream(), true);
        in_file = new BufferedReader(new InputStreamReader(client_leech.getInputStream()));
        out_file.println("-1");

        // and close my socket (P2P)
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

    // secondary form of communication. Message sent but expect no response
    public void sendInfo(String data){
    	out.println(data);
    }

    /* 
    * This function is called after a successful greet from server
    * Will tell the server which files we have ready for sharing
    *
    * @return
    * status: String. If the publishing of files was successful or not.
    */
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


    /* 
    * Used to issue a 'search' request to Server. 
    * 
    *@params
    * file: String, name of the file we are looking for
    *
    *@return
    * result: String, -1 if file wasn't found, else the id of 
    *         the client which has the file
    */
    public String searchFiles(String file) throws Exception {
    	String result = sendMessage(file);
    	if (result.equals("-1"))
    		return "-1";
    	else
    		return result;
    }

    /* 
    * For continously ping the server via UDP
    */
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
        System.out.println("Pinged the server");
    }
    
    //send Hello to server every 'timer_value' seconds
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
            }
        };
        timer.schedule(y,0, timer_value);
	}
    
    /* 
    * fetch desired file from another client.
    * When fetch is done, a 'search()' will first be performed on 
    * Server. Then, if the file is found, a fetch request will be done 
    * to the client's socket, which has the file.
    * 
    *@params
    * file: String, name of requesting file
    * seed_id: int, the port at which the client, who has the file, is waiting for 
    *          requests.
    *@return 
    * answer: String, wether the file was fetched or not found.
    */
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

    // Pass the contents of the file after recieving it from 
    // the other client. then return wether the operation was successful or not
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

    // setters and getter for id
    public void setId(String id) { my_id = id; }
    public String getId() {return my_id; }
}