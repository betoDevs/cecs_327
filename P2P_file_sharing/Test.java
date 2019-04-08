import java.util.concurrent.TimeUnit;

public class Test{
	public static void main(String[] args) throws Exception{
		String response, path_to_dir;
		path_to_dir = args[0];
		Client client = new Client(path_to_dir);
		client.startConnection("127.0.0.1", 6666);
		System.out.println("Connection Successful. My id is: " + client.getId());
		response = client.sendMessage("Hello Server");
		System.out.println(response);
		response = client.publishFiles();
		while(true) {
			System.out.println("1. for search, 2. for fetch");
			client.sendInfo("1");
			response = client.searchFiles("Iron Man.txt");
			System.out.println(response);
			System.out.println(client.fetchFile("Iron Man.txt", Integer.parseInt(response)));
			TimeUnit.SECONDS.sleep(5);
		}
	}
}