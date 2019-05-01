import java.net.*;
import java.io.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.Timer;
import java.util.TimerTask;

public class Server {
	// static cuz same amongst all clients
	private static ServerSocket serverSocket;
	private static int port;

	//accessible from child class w/o having to write getter and setter
	protected static HashMap<Integer,Set<String>> client_files; // here maybe a semaphor?
	protected static HashMap<Integer,String> actives = new HashMap<>();
	protected static HashMap<Integer,Timer> timers= new HashMap<>();

	// These are private to each client/thread 
	private int client_count;
	private Socket clientSocket;
	private PrintWriter out;
	private BufferedReader in;
	private int my_id;

	//timer to count to 200 seconds
	//private static Timer timer;
	// Main thread server will be initialized with this
	public Server(){
		this.port = 6666;
		client_count = 6667;
	}

	public void start() throws Exception {
		serverSocket = new ServerSocket(port);
		client_files = new HashMap<Integer,Set<String>>();
		listen();
		
	}

	public void stop() throws IOException{
		in.close();
		out.close();
		clientSocket.close();
		serverSocket.close();
	}

	public void listen() throws Exception {
		Thread udp = new Thread(new ServerClientUDP());
		udp.start();
		while(true){
			this.client_count+=1;
			clientSocket = serverSocket.accept();
			Thread t = new Thread(new ServerClientTCP(clientSocket, client_count));
			t.start();
			clientSocket = new Socket();
		}
	}
	
	public static void main(String[] args) throws Exception{
		Server server = new Server();
		server.start();
	}
}