import java.net.*;
import java.io.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class Server implements Runnable {
	// static cuz same amongst all clients
	private static ServerSocket serverSocket;
	private static HashMap<Integer,HashMap<String, String>> client_files; // here maybe a semaphor?
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
		client_count = 6667;
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
		client_files = new HashMap<Integer,HashMap<String, String>>();
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
			try{
				Join();
			} catch(Exception e) {
				System.out.println(e);
			}
			return;
	}

	public void Join() throws Exception{
		String choice, greeting;
		// give id
		greeting = in.readLine();
		out.println(my_id);
		// greet
		greeting = in.readLine();
		System.out.println(greeting);
		out.println("Join Successful! Welcome to P2P, your id is: " + my_id);

		// Ask for what the user wants to do
		Publish();
		return;
	}

	// information about the connected client
	// gets added to server
	public void Publish() throws IOException {
		String name, path;
		int num_of_files;
		num_of_files = Integer.parseInt(in.readLine());
		if(num_of_files != -1)
			out.println("ready");
		else {
			out.println("Error recieving number of files");
			return; 
		}
		for(int k = 0; k < num_of_files; k++){
			name = in.readLine();
			if(name != null)
				Add(name, "path");
		}
		name = in.readLine();
		out.println("Success");
		Show();
		try {
			Controller();
		}catch(Exception e){
			System.out.println(e);
		}
		return;
	}

	// return index of a servant having the required
	// file, else -1
	public void Search() throws Exception {
		String response = "-1";
		String file_name = in.readLine();
		System.out.println("I am looking for: " + file_name);
		for(int i : client_files.keySet()){
			for(Map.Entry element : client_files.get(i).entrySet()){
				if(element.getKey().equals(file_name)){
					response = Integer.toString(i);
				}
			}
		}
		//System.out.println("Done searching");
		out.println(response);
		return;
	}

	// contacting peer recieves requested info
	// from peer at index
	public void Fetch(int index){

	}

	// check to see if a client is still active
	public void UDPHello() {

	}

	public void Show(){
		HashMap<String, String> t = client_files.get(my_id);
		System.out.println("Displaying elements just added for client with id: " + my_id);
		for(Map.Entry element : t.entrySet()){
			System.out.println("File name: " + element.getKey() +
							  " Path name: " + element.getValue());
		}
		return;
	}

	public void RemoveClient() {

	}

	public void Controller() throws Exception{
		String choice;
		while(true){
			choice = in.readLine();
			if(choice.equals("1"))
				Search();
			else if(choice.equals("2")){
				System.out.println("User watch to Fetch");
				Search();
			}
			else{
				System.out.println("here");
				RemoveClient();
				return;
			}
		}
	}

	// Add to register
	public void Add(String file_name, String path){
		HashMap<String, String> tmp;
		if(client_files.containsKey(my_id)) {
			tmp = client_files.get(my_id);
			tmp.put(file_name, path);
		} else {
			tmp = new HashMap<String, String>();
			tmp.put(file_name, path);
			client_files.put(my_id, tmp);
		}
	}

	public static void main(String[] args) throws Exception{
		Server server = new Server();
		server.start();
	}
}