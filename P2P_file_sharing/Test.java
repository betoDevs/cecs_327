import java.util.concurrent.TimeUnit;
import java.util.Scanner;

public class Test{
	public static void main(String[] args) throws Exception{
		String response, path_to_dir, choice;
		Scanner in = new Scanner(System.in);
		path_to_dir = args[0];
		Client client = new Client(path_to_dir);
		client.startConnection("127.0.0.1", 6666);
		System.out.println("Connection Successful. My id is: " + client.getId());
		response = client.sendMessage("Hello Server");
		System.out.println(response);
		response = client.publishFiles();
		while(true) {
			// Search, Fetch or Disconnect?
			System.out.println("1. for search, 2. for fetch");
			choice = in.nextLine();

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
	}
}