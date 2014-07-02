package g.tcp.client;

import helper.pack.Glog;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.Socket;
import java.net.UnknownHostException;

public class Clientg3 implements Runnable {

	private Socket socket = null;

	private OutputStreamWriter outputStreamWriter = null;
	private BufferedOutputStream bufferedOutputStream = null;
	private BufferedReader bufferedReader = null;

	public Integer clientPort = 0;
	private int connectionTimeout = 30000;

	private int receivePingTimeotTms = 10000;
	private long lastPingReceivedTms = 0;

// unique	
	public String ip = "";
	private boolean reconnecterCalled = false;
	private String name;

// common
	@Override
	public void run() {
		Glog.runPrintLogToConsole();

		Runtime.getRuntime().addShutdownHook(new Thread() {
			public void run() {
				closeAll();
			}
		});

		Thread receivePingThr = new Thread(new Runnable() {
			public void run() {
				while (true) {
					try {
						Thread.sleep(2000);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}

					if ((System.currentTimeMillis() - lastPingReceivedTms) > receivePingTimeotTms) {
						Glog.prnt(name + "ping not reveiced over " + receivePingTimeotTms + "ms");
						lastPingReceivedTms = System.currentTimeMillis();
						reconnecter("ping not received");
					}
				}
			}
		});
		receivePingThr.setPriority(Thread.MAX_PRIORITY);
		receivePingThr.setName("receivePingThr");
		receivePingThr.start();

		reconnecter("server listener");
	}

	private boolean createConnection() {
		name = ip + ":" + clientPort + " ";
		//Glog.prnt(ip + ":" + port + " createSocketConnection Start");		

		if (ip == null || "".equals(ip)) {
			Glog.prnte("Client createSocketConnection ip not set, ip=" + ip + " clientPort=" + clientPort);
			return false;
		}

		Integer zero = new Integer(0);

		if (clientPort == null || zero.equals(clientPort)) {
			Glog.prnte("Client createSocketConnection port not set, ip=" + ip + " clientPort=" + clientPort);
			return false;
		}

		socket = null;

		try {
			socket = new Socket(ip, clientPort);
			socket.setSoTimeout(connectionTimeout);
			socket.setTcpNoDelay(true);
			socket.setPerformancePreferences(2, 0, 1);
		} catch (UnknownHostException e) {
			Glog.prnte(name + "createSocketConnection clientSocket failed, e=" + e.getMessage());
			socket = null;
			return false;
		} catch (IOException e) {
			Glog.prnte(name + "createSocketConnection clientSocket failed, e=" + e.getMessage());
			socket = null;
			return false;
		}

		bufferedOutputStream = null;
		outputStreamWriter = null;

		if (socket != null) {
			try {
				bufferedOutputStream = new BufferedOutputStream(socket.getOutputStream());
			} catch (IOException e) {
				Glog.prnte(name + "createWriter bufferedOutputStream failed, e=" + e.getMessage());
				bufferedOutputStream = null;
			}

			if (bufferedOutputStream != null) {
				try {
					outputStreamWriter = new OutputStreamWriter(bufferedOutputStream, "US-ASCII");
				} catch (UnsupportedEncodingException e) {
					e.printStackTrace();
					Glog.prnte(name + "createWriter bufferedOutputStream failed, e=" + e.getMessage());
					outputStreamWriter = null;
					return false;
				}
			} else {
				return false;
			}

			try {
				bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
				return true;
			} catch (IOException e) {
				e.printStackTrace();
				Glog.prnte(name + "createReader BufferedReader failed, e=" + e.getMessage());
				bufferedReader = null;
				return false;
			}
		} else {
			Glog.prnte(name + "socket==null");
			return false;
		}

	}

	public void sendMsg(String text) {
		if (outputStreamWriter != null && text != null && !"".equals(text) && text.length() > 1) {
			try {
				//Glog.prnt("Client sendMsg text=" + text);	
				text = text + '\n';
				outputStreamWriter.write(text, 0, text.length());
				outputStreamWriter.flush();
			} catch (IOException e) {
				Glog.prnte(name + "sendMsg exception: " + e.getMessage());
			}
		} else {
			Glog.prnte(name + "sendMsg not sending text=" + text);
		}
	}

	private void reconnecter(final String caller) {
		Glog.prnt(name + "reconnecter called=" + caller);
		if (ip != null && !"".equals(ip)) {
			if (!reconnecterCalled) {
				reconnecterCalled = true;

				while (!createConnection()) {
					closeAll();
					try {
						Thread.sleep(1000);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
				reconnecterCalled = false;
				serverListener();
			} else {
				Glog.prnte(name + "reconnecter cannot be called because reconnecterCalled=" + reconnecterCalled + "(already called)");
			}
		} else {
			Glog.prnte(name + "reconnecter failed to launch because ip=" + ip + " caller=" + caller);
		}
	}

	private void serverListener() {
		Glog.prnt(name + "called serverListener");
		String receivedText = "";
		while (true) {
			if (bufferedReader != null && socket != null) {
				try {
					receivedText = bufferedReader.readLine();
					if (receivedText != null) {
						if ("PING".equals(receivedText)) {
							lastPingReceivedTms = System.currentTimeMillis();
							sendMsg("PONG");
						} else {
							Glog.prnte(name + "serverListener something wrong received, receivedText=" + receivedText);
						}
					} else {
						Glog.prnte(name + "receivedText=" + receivedText);
						break;
					}
				} catch (IOException e) {
					Glog.prnte(name + "serverListener error on readLine, e=" + e.getMessage());
					break;
				}
			} else {
				Glog.prnte(name + "serverListener bufferedReader=" + bufferedReader + " socket=" + socket);
				break;
			}
		}
		Glog.prnt(name + "exiting serverListener");
		reconnecter("server listener");
	}

	public void closeAll() {
		//Glog.prnt("Client closeConn Start");
		if (outputStreamWriter != null) {
			try {
				// osw.flush();
				outputStreamWriter.close();
			} catch (IOException e1) {
				//Glog.prnte("closeConn, e: " + e1.getMessage());
			}
		} else {

		}

		if (socket != null) {
			try {
				socket.close();
			} catch (IOException e) {
				//Glog.prnte("Client closeConn, e: " + e.getMessage());
			}
		} else {

		}

		//Glog.prnt("Client closeConn End");
	}

}