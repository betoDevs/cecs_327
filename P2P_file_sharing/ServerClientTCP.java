import java.net.*;
import java.io.*;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class ServerClientTCP extends Server implements Runnable{
	/*
	* clientSocket: socket of peer which a TCP connection will be established
	* out/in: write to and recieve String data from and to client/peer
	* my_id: client's id of which this thread is connected to
	* choice: used for controlling the flow of the client
	* connection_status: used to control when this thread should be closed
	*/
	private Socket clientSocket;
	private PrintWriter out;
	private BufferedReader in;
	private int my_id;
	private String choice;
	private int connection_status;

	/*
	* establish connection and set connection_status to 1, (open).
	*/
	public ServerClientTCP(Socket clientSocket, int id) throws Exception{
		this.clientSocket = clientSocket;
		my_id = id;
		out = new PrintWriter(clientSocket.getOutputStream(), true);
		in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
		connection_status = 1;
	}

	// Thread starts here when opened from 'Server.java'
	public void run() {
		try{
			Join();
		} catch(Exception e){
			System.out.println("Problem at starting Join: " + e);
		}
		try{
			Publish();
		} catch(Exception e){
			System.out.println("Problem at starting Publish: " + e);
		}
		try{
			Controller();
		} catch(Exception e){
			System.out.println("Problem at starting Controller: " + e);
		}
		System.out.println("ServerClientTCP closed. ID: " + my_id);
	}

	// Join the server and greet it. If greet successful then move onto 'Publish()'
	public void Join() throws Exception{
		String choice, greeting;
		// give client its' Server given id
		greeting = in.readLine();

		// In the case the client disconects early.
		if(greeting == null) { throw new IOException(); }
		out.println(my_id);

		// greet and display to client's console
		greeting = in.readLine();
		System.out.println(greeting);
		out.println("Join Successful! Welcome to P2P, your id is: " + my_id);

		// return to 'run()' and go to 'Publish()'
		return;
	}

	/*
	* Get the number of files that client has available for sharing. 
	* Read in the titles of the number and add them to Server file index at
	* client_files from parent class 'Server.java'
	*/
	public void Publish() throws IOException {
		String name;
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
			if(name != null) Add(name);
		}
		name = in.readLine();
		out.println("Sucess.");
		// Server will display the added files
		Show();

		// return to 'run()' and go to 'Controller()'
		return;
	}

	// determines wether to search or close connection
	public void Controller() throws Exception{
		// while connections_status is open
		while(connection_status == 1){
			choice = in.readLine();

			// if client disconnects before giving an answer
			if(choice == null){
				closeConnection();
				return;
			}
			if(choice.equals("1") || choice.equals("2"))
				Search();
			else{
				closeConnection();
				return;
			}
		}
	}

	// Search client_files for the client with wanted file
	public void Search() throws Exception {
		String response = "-1";
		String file_name = in.readLine();
		if(file_name == null) {
			closeConnection();
			return;
		}
		for(int i : client_files.keySet()){
			if(client_files.get(i).contains(file_name))
				response = Integer.toString(i);
		}
		
		// If i contain the file let the client know by outputting "0"
		if(response.equals(Integer.toString(my_id))) out.println("0");

		// else send the client that contains the file
		else out.println(response);
	}

	// for using after a fetch to update the list
	public void Add(String file_name){
		if(!client_files.containsKey(my_id)) 
			client_files.put(my_id, new HashSet<String>());
		client_files.get(my_id).add(file_name);
	}

	public void Show(){
		Set<String> t = client_files.get(my_id);
		System.out.println("Displaying elements just added for client with id: " + my_id);
		for(String element : t){
			System.out.println("File name: " + element);
		}
		return;
	}

	// last thing to be called
	// remove client then close connection.
	public void closeConnection() throws IOException{
		client_files.remove(my_id);
		out.close();
		in.close();
		clientSocket.close();
		connection_status = 0;
	}
}