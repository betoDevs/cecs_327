import java.net.*;
import java.io.*;
import java.nio.file.Paths;
import java.nio.file.Files;

/*
* This class will be a thread that starts from 'Client.java'.
* This class' purpose is for P2P communication
* Client starts it after it successfully connects to Server and
* this class will wait in 'listen()' for another peer to connect and
* ask for a file.
*
* Class is closed by'Client.java', when the connection to server is terminated.
*/
public class Connection implements Runnable {
	private ServerSocket seed_socket;
	private Socket leech_socket;
	private PrintWriter out;
	private BufferedReader in;
	private String path_to_dir;
	private File[] files;

	public Connection(int seed_id, String path) throws IOException{
		// Create open connections that waits
		// for clients to message me
		seed_socket = new ServerSocket(seed_id);
		path_to_dir = path;
		files = new File(path_to_dir).listFiles();
	}

	// Thread starts here
	public void run() {
		// Send to Controller
		try {
			Controller(); 
		} catch(Exception e) {
			System.out.println(e);
		}
		System.out.println("Exiting Connection.java");
	}

	// control flow
	public void Controller() throws Exception{
		String request;
		while(true) {
			// accept connection and get request
			request = listen();

			// '-1' is sent by its' own Client when the connection wants to be ended.
			if(request.equals("-1")){
				close();
				return;
			}
			
			// send info back 
			send(request);

			// close out P2P connection
			close();
		}
	}

	// Listen for connection and request
	// return request
	public String listen() throws Exception {
		leech_socket = seed_socket.accept();
		out = new PrintWriter(leech_socket.getOutputStream(), true);
		in = new BufferedReader(new InputStreamReader(leech_socket.getInputStream()));
		return in.readLine();
	}

	public void send(String request) throws Exception{
		// Get the file
		for(File f : files){
			if(f.getName().equals(request)){
				// file found. Send as a string
				out.println(new String(Files.readAllBytes(Paths.get(path_to_dir+request))));
				return;
			}
		}
		// file not found send error;
		out.println("error");
	}

	// closes the connection to requesting peer
	public void close() throws IOException {
		in.close();
		out.close();
		leech_socket.close();
	}
}