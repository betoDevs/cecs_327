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
	//accessible from child class w/o having to write getter and setter
	protected static HashMap<Integer,Set<String>> client_files; 
	protected static HashMap<Integer,String> actives = new HashMap<>();
	protected static HashMap<Integer,Timer> timers= new HashMap<>();

	/*
	* serverSocket: will listen to peers when they want to connect and publish
	* port: socket will be on port 6666
	* client_count: will server as the id's given out to each client in order to 
					be accesed from other peers
	* clientSocket: bind the peer's connections to a socket and start TCP with it
	* out/in: write to and recieve String data from clients
	*/
	private static ServerSocket serverSocket;
	private static int port;
	private int client_count;
	private Socket clientSocket;
	private PrintWriter out;
	private BufferedReader in;

	
	public Server(){
		this.port = 6666;
		client_count = 6667;
	}

	// bind the socket to the port and go to listen()
	public void start() throws Exception {
		serverSocket = new ServerSocket(port);
		client_files = new HashMap<Integer,Set<String>>();
		listen();
		
	}

	// closes server connections and shuts it down
	public void stop() throws IOException{
		in.close();
		out.close();
		clientSocket.close();
		serverSocket.close();
	}

	// constantly listen to peers trying to connect. Both a tcp and udp is made for each peer
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