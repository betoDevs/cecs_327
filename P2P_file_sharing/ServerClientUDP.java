import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;

/*
* This class is created in a thread from 'Server.java'
* Will always be listening and when a datagram packet is recieved
* from a client, it will add that client to actives and reset the timer
* of that client.
*
* If a client doesn't ping in the required time, the connection is then terminated
* and the files are taken out of 'client_files' in 'Server.java'.
*/
public class ServerClientUDP implements Runnable{
	private static HashMap<Integer,String> actives = new HashMap<>();
	private static HashMap<Integer,Timer> timers= new HashMap<>();
	private boolean open;
	private final int timer_value = 5000;
	
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
        if(uid !=0)
        	udpSocket.close();
        		break;
        }
        receive = new byte[1024];
        return uid;
	}

	public void run() {
		try{
			checkActive();
		} catch(Exception e){
			System.out.println(e);
		}
	}

	//thread to check active
	public void checkActive() throws Exception
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
					System.out.println("added client " + id);
					
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
			}
		}	
				
	}
	
	//Thread to remove inactive
	public void removeInactive(int x) throws Exception
	{
		startTimer(x);
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
        timers.get(x).schedule(y, timer_value);
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
        timers.get(x).schedule(timerTask, timer_value);
        System.out.println(Integer.toString(x) + " Pinged.");
	}
}
