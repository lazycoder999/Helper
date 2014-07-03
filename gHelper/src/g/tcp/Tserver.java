package g.tcp;

import g.tcp.server.ServerListener;
import g.tcp.server.Serverg3;
import helper.Glog;

public class Tserver implements ServerListener {

	public static void main(String[] args) {
		Tserver tst = new Tserver();
		tst.starto();
	}

	public void starto() {
		Glog.runPrintLogToConsole();

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

	}

	@Override
	public void incomingMessage1(String line) {
		Glog.prnt("server receveived " + line);
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
