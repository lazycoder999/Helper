package g.tcp;

import g.tcp.client.Clientg3;
import helper.pack.Gh;

public class Tclient {
	
	public static void main(String[] args) {
		Gh.runPrintLogToConsole();
		Clientg3 client = new Clientg3();
		
		client.clientPort = 5566;
		client.ip = "127.0.0.1";
		Thread myThread2 = new Thread(client);
		myThread2.setName("myThread2");
		myThread2.start();
	}
	
}
