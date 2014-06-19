package g.tcp;

import g.tcp.client.Clientg2;
import g.tcp.server.Serverg2;
import helper.pack.Gh;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class Test {

	public static void main(String[] args) {
		Gh.runPrintLogToConsole();

		Serverg2 server = new Serverg2();
		Clientg2 client = new Clientg2();

		/*		Thread servThr = new Thread(new Runnable() {
					public void run() {
						server.srvId = (byte) 1;
						server.serverPort = 5566;
						server.startServerg2();
					}
				});
				servThr.setPriority(Thread.MAX_PRIORITY);
				servThr.setName("servThr");
				servThr.start();*/

		//MyRunner myRunner = new MyRunner();
		server.srvId = (byte) 1;
		server.serverPort = 5566;
		//server.startServerg2();
		Thread myThread = new Thread(server);
		myThread.setName("myThread");
		myThread.start();

		Thread clientThr = new Thread(new Runnable() {
			public void run() {
				client.clientPort = 5566;
				client.ip = "127.0.0.1";
				client.startClientg2();
			}
		});
		clientThr.setPriority(Thread.MAX_PRIORITY);
		clientThr.setName("clientThr");
		clientThr.start();

		try {
			Thread.sleep(782);
		} catch (InterruptedException e1) {

			e1.printStackTrace();
		}

		Thread consoleListenerThread = new Thread(new Runnable() {
			public void run() {
				while (true) {
					BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
					String line = "";

					while (line.equalsIgnoreCase("quit") == false) {
						try {
							line = in.readLine();
						} catch (IOException e) {
							e.printStackTrace();
						}

						if (line.equals("cclose")) {
							client.closeAll();
						}

					}

					try {
						in.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
		});
		consoleListenerThread.setPriority(Thread.MIN_PRIORITY);
		consoleListenerThread.start();
	}
}
