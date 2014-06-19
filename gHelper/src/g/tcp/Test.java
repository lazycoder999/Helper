package g.tcp;

import g.tcp.server.ServerListener;
import g.tcp.server.Serverg3;
import helper.pack.Gh;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class Test implements ServerListener {

	public static void main(String[] args) {
		Test tst = new Test();
		tst.starto();
	}

	public void starto() {
		Gh.runPrintLogToConsole();

		Serverg3 server = new Serverg3();

		server.srvId = (byte) 1;
		server.serverPort = 5566;
		server.setServerListener(this);
		Thread myThread = new Thread(server);
		myThread.setName("myThread");
		myThread.start();

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

//						if (line.equals("cclose")) {
//							client.closeAll();
//						}

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

	@Override
	public void incomingMessage1(String line) {
		// TODO Auto-generated method stub

	}

	@Override
	public void incomingMessage2(String line) {
		// TODO Auto-generated method stub

	}

	@Override
	public void incomingMessage3(String line) {
		// TODO Auto-generated method stub

	}

	@Override
	public void incomingMessage4(String line) {
		// TODO Auto-generated method stub

	}

	@Override
	public void incomingMessage5(String line) {
		// TODO Auto-generated method stub

	}

	@Override
	public void incomingMessage6(String line) {
		// TODO Auto-generated method stub

	}

	@Override
	public void incomingMessage7(String line) {
		// TODO Auto-generated method stub

	}
}
