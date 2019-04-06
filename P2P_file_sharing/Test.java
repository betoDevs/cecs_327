import java.util.concurrent.TimeUnit;

public class Test{
	public static void main(String[] args) throws Exception{
		String response;
		Client client = new Client();
		client.startConnection("127.0.0.1", 6666);
		for(int i = 0; i < 5; i ++){
			response = client.sendMessage("hello server");
			System.out.println(response);
			System.out.println("Seconds left: " + i);
			TimeUnit.SECONDS.sleep(1);
		}
	}
}