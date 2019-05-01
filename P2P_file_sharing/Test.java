import java.util.concurrent.TimeUnit;
import java.util.Scanner;

public class Test{
	public static void main(String[] args) throws Exception{
		if(args.length != 1){
			System.out.println("Please enter only the directory for your shared folder");
			return;
		}
		String response, path_to_dir, choice;
		Scanner in = new Scanner(System.in);
		path_to_dir = args[0];

		// Initialize my client and the client-to-client, Connection, threads
		Client client = new Client(path_to_dir);
		client.startConnection("127.0.0.1", 6666);

		// Report if success, say hello to server
		System.out.println("Connection Successful. My id is: " + client.getId());
		response = client.sendMessage("Hello Server");
		client.sendingHello();
		System.out.println(response);

		// Publish files that are available for sharing
		response = client.publishFiles();
		if(response == null){
			System.out.println("Problem Publishing files.\nExiting.");
			client.stopConnection();
			return;
		}
		System.out.println("Files Sent: " + response);

		// Start the interaction with server
		while(true) {
			// Search, Fetch or Disconnect?
			System.out.println("1. for search, 2. for fetch, 3. for exit.");
			choice = in.nextLine();

			// make sure appropiate choice was inputted in
			if(!validate(choice)){
				System.out.println("Please enter an appropiate choice");
				continue;
			}

			// search
			if(choice.equals("1")){
				// Let server know we want to search
				client.sendInfo("1");
				System.out.println("What would you like to search for?");

				// get the id of the user with the desired file
				response = client.searchFiles(in.nextLine());
				if (response.equals("-1"))
					System.out.println("File not found");
				else
					System.out.println("File found! At user with id : " + response);
			}

			else if(choice.equals("2")){
				// Let server know we want to fetch
				client.sendInfo("2");
				System.out.println("What would you like to fetch?");
				choice = in.nextLine();

				// Look and fetch for the file
				response = client.searchFiles(choice);
				// If i already have the file the answer will be 0
				if(response.equals("0")){
					System.out.println("File already in your folder.");
					continue;
				}
				if(response.equals("-1"))
					System.out.println("File not found");
				else {
					client.fetchFile(choice, Integer.parseInt(response));
					System.out.println("File added to shared folder at " + path_to_dir);
				}
			}

			// exit
			else {
				client.stopConnection();
				break;
			}

			// sleep a second for easier reading
			TimeUnit.SECONDS.sleep(1);
		}
		in.close();
		System.out.println("Exiting Test.java");
	}

	public static boolean validate(String s){
		if(s == null || s.length() != 1) return false;
		char c = s.charAt(0);
		if(!Character.isDigit(c) || c < '1' || c > '3') return false;
		return true;
	}
}