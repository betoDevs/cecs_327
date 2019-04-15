import java.net.*;
import java.io.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.Timer;
import java.util.TimerTask;

public class Server implements Runnable {
	// static cuz same amongst all clients
	private static ServerSocket serverSocket;
	private static HashMap<Integer,HashMap<String, String>> client_files; // here maybe a semaphor?
	private static HashMap<Integer,String> actives = new HashMap<>();
	private static HashMap<Integer,Timer> timers= new HashMap<>();
	private static int client_count;
	private static int port;

	// These are private to each client/thread 
	private Socket clientSocket;
	private PrintWriter out;
	private BufferedReader in;
	private int my_id;

	//timer to count to 200 seconds
	private static Timer timer;
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

	// contacting peer receives requested info
	// from peer at index
	public void Fetch(int index){

	}

	// check to see if a client is still active
	public int UDPHello() throws Exception {
		DatagramSocket udpSocket = new DatagramSocket(1234); 
        byte[] receive = new byte[1024]; 
        DatagramPacket udpReceive = null;
        int uid =0;
        while(true)
        {
        udpReceive = new DatagramPacket(receive, receive.length);
        udpSocket.receive(udpReceive);
        
        String parsing = convertToString(receive).toString();
        String sArray[] = parsing.split("/");
        
        uid = Integer.parseInt(sArray[0]);
        System.out.println("received hello from" + sArray[0]);
        String message = sArray[1];
        if(uid !=0)
        	udpSocket.close();
        		break;
        }
        receive = new byte[1024];
        return uid;
	}

	//thread to check active
	public void checkActive() throws Exception
	{
			Thread t = new Thread(new Runnable()
			{
				@Override
				public void run()
				{
					while(true)
					{
					try{
						//get the id of the message
						int id = UDPHello();
						
						//if the id is not existing, add one
						if(!actives.containsKey(id))
						{
							actives.put(id, "Available");
							System.out.println("added client");
							
							//start the count down after added
							removeInactive(id);
						}
						else
						{
						//restart the count down
						resetTimer(id);
						}
						
					} catch(Exception e) {
						System.out.println(e );
					};
				}	
				}
			});
			t.start();
	}
	
	//Thread to remove inactive
	public void removeInactive(int x) throws Exception
	{
		startTimer(x);
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

	public void removeClient() throws IOException {
		in.close();
		out.close();
		clientSocket.close();
		client_files.remove(my_id);
		return;
	}

	public void Controller() throws Exception{
		String choice;
		while(true){
			choice = in.readLine();
			if(choice == null){
				System.out.println("Client with id: " + my_id + " dc'd.");
				// need to remove clients and it's files
				removeClient();
			if(choice.equals("1"))
				Search();
			else if(choice.equals("2")){
				System.out.println("User watch to Fetch");
				Search();
			}
			else{
				System.out.println("here");
				removeClient();
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
	
	//convert byte to string
	public static StringBuilder convertToString(byte[] s)
	{
		if(s == null)
			return null;
		StringBuilder finString = new StringBuilder();
		int x = 0;
		while (s[x] != 0)
		{
			finString.append((char) s[x]);
			x++;
		}
		return finString;
	}
	
	//Start timer of the ID x
	public void startTimer(int x)
	{
		TimerTask y = new TimerTask() {

            @Override
            public void run() {
            	if(actives.containsKey(x))
        			actives.remove(x);
        			System.out.println("Remove client: " + Integer.toString(x));
            }
        };
        //make a timer that remove inactive after 200 seconds
        Timer newTimer = new Timer();
        timers.put(x, newTimer);
        timers.get(x).schedule(y, 2000);
	}
	
	//reset a timer of the ID x
	public void resetTimer(int x)
	{
		//timerTask to remove inactive clients
		TimerTask timerTask = new TimerTask() {
			@Override
            public void run() {
				if(actives.containsKey(x))
        			actives.remove(x);
        			System.out.println("Remove client: " + Integer.toString(x));
            }
		};
		
		//cancel the current timer and reschedule a new one
		timers.get(x).cancel();
		Timer newTimer = new Timer();
		timers.put(x, newTimer);
        timers.get(x).schedule(timerTask, 2000);
        System.out.println(Integer.toString(x) + " timer is reset");
	}
	
	public static void main(String[] args) throws Exception{
		Server server = new Server();
		server.checkActive();
		server.start();
	}
}