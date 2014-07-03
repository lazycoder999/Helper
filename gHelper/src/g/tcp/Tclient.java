package g.tcp;

import g.tcp.client.Clientg3;
import helper.Glog;

public class Tclient {

	public static void main(String[] args) {
		Glog.runPrintLogToConsole();
		Clientg3 client = new Clientg3();

		client.clientPort = 5566;
		client.ip = "127.0.0.1";
		Thread clientThread = new Thread(client);
		clientThread.setName("clientThread");
		clientThread.start();
	}

}
