import java.net.*;
import java.io.*;
import java.util.HashMap;
import java.util.List;

public class Server implements Runnable {
	// static cuz same amongst all clients
	private static ServerSocket serverSocket;
	private static HashMap<Integer,List<String>> client_files; // here maybe a semaphor?
	private static int client_count;
	private static int port;

	// These are private to each client/thread 
	private Socket clientSocket;
	private PrintWriter out;
	private BufferedReader in;
	private int my_id;


	// Main thread server will be initialized with this
	public Server(){
		this.port = 6666;
		client_count = 0;
	}

	// Multiple threads start with this
	public Server(Socket clientSocket, int id) throws Exception{
		this.clientSocket = clientSocket;
		my_id = id;
		out = new PrintWriter(clientSocket.getOutputStream(), true);
		in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
	}

	public void start() throws Exception {
		serverSocket = new ServerSocket(port);
		client_files = new HashMap<Integer,List<String>>();
		listen();
	}

	public void stop() throws IOException{
		in.close();
		out.close();
		clientSocket.close();
		serverSocket.close();
	}

	public void listen() throws Exception {
		while(true){
			clientSocket = serverSocket.accept();
			Thread t = new Thread(new Server(clientSocket, client_count));
			client_count++;
			t.start();
			clientSocket = new Socket();
		}
	}

	// Come here after creating a thread
	public void run() {
		while(true){
			try{
				Join();
			} catch(Exception e) {
				System.out.println(e);
			}
		}
	}

	public void Join() throws Exception{
		String choice;
		// Greet and give id
		String greeting = in.readLine();
		System.out.println(greeting);
		out.println("Join Successful!\nWelcome to P2P, your id is: " + my_id);

		// Ask for what the user wants to do
		while(true) {
			out.println("What would you like to do?");
			out.println("1. Publish\n2. Search\n3. Fetch");
			choice = in.readLine();
		}
	}

	// information about the connected client
	// gets added to server
	public void Publish() {

	}

	// return index of a servant having the required
	// file, else -1
	public void Search(String file_name) {

	}

	// contacting peer recieves requested info
	// from peer at index
	public void Fetch(int index){

	}

	// check to see if a client is still active
	public void UDPHello() {

	}

	public static void main(String[] args) throws Exception{
		Server server = new Server();
		server.start();
	}
}