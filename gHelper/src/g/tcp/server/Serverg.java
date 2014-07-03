package g.tcp.server;

import helper.Glog;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;

public class Serverg implements Runnable {

	private String serverName = "";
	private Integer serverPort;
	private ServerSocket in;
	private ServerSocket clientSocket;

	private byte srvId = 0;
	private ServerListener srvListener;
	private ServerSocket srvSocket = null;
	private boolean runningToF = true;

	public void setServerListener(ServerListener srvListener) {
		this.srvListener = srvListener;
	}

	public void setServerName(String text) {
		serverName = text;
	}

	public void setServerPort(Integer port) {
		serverPort = port;
	}

	public void setServerId(byte id) {
		srvId = id;
	}

	@Override
	public void run() {
		Glog.prnt(serverName + " thread started");
		newConnection();
		Glog.prnt(serverName + " thread end");
	}

	private void newConnection() {
		Glog.prnt(serverName + " newConnection start");
		srvSocket = null;

		try {
			srvSocket = new ServerSocket(serverPort);
		} catch (IOException e) {
			Glog.prnte(serverName + " Could not listen on port: " + serverPort);
		}

		Glog.prnt(serverName + " newConnection end");
		gServerInside();
	}

	private void gServerInside() {
		Glog.prnt(serverName + " gServerInside start");

		Socket clientSocket = null;
		Glog.prnt(serverName + " waiting for connection... on port: " + serverPort);

		if (srvSocket == null) {
			Glog.prnte("srvSocket == null. serverName=" + serverName);
		}

		try {
			clientSocket = srvSocket.accept();
			Glog.prnt(serverName + " accepted");
		} catch (IOException e) {
			Glog.prnte(serverName + " Accept failed. e=" + e.getMessage());
		}

		Glog.prnt(serverName + " Connection successful");
		Glog.prnt(serverName + " Waiting for input.....");

		BufferedReader in = null;

		if (clientSocket != null) {
			try {
				in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
			} catch (IOException e1) {
				e1.printStackTrace();
				Glog.prnte(serverName + " BufferedReader, e: " + e1.getMessage());
			}
		} else {
			Glog.prnte(serverName + " clientSocket == null");
		}

		String inputLine = null;

		Glog.prnt(serverName + " before while");

		if (in != null) {
			try {
				while (runningToF) {
					inputLine = in.readLine();

					if (srvListener != null && inputLine != null) {
						if (srvId == 1) {
							srvListener.incomingMessage1(inputLine);
						} else if (srvId == 2) {
							srvListener.incomingMessage2(inputLine);
						} else if (srvId == 3) {
							srvListener.incomingMessage3(inputLine);
						} else if (srvId == 4) {
							srvListener.incomingMessage4(inputLine);
						} else if (srvId == 5) {
							srvListener.incomingMessage5(inputLine);
						}
					} else {
						Glog.prnte(serverName + " srvListener=null or inputLine = null");
						try {
							Thread.sleep(1000);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
					}

				}
			} catch (SocketException e2) {
				Glog.prnte(serverName + " e2: " + e2.getMessage());
				if (e2.getMessage().toString().equals("Connection reset")) {
					Glog.prnte(serverName + " konnekcion reset");
				}
			} catch (IOException e) {
				e.printStackTrace();
				Glog.prnte(serverName + " while, e: " + e.getMessage());
			}
		} else {
			Glog.prnte(serverName + " in == null");
		}
		Glog.prnt(serverName + " after while");

		Glog.prnt(serverName + " gServerInside end");
		//closeAll();
		//newConnection();
	}

	public void closeAll() {
		Glog.prnt(serverName + " closeAll start");
		runningToF = false;

		if (in != null) {
			try {
				Glog.prnt(serverName + " in.close: " + in);
				in.close();
			} catch (IOException e) {
				e.printStackTrace();
				Glog.prnte(serverName + " in.close() e: " + e.getMessage());
			}
		}

		if (clientSocket != null) {
			try {
				Glog.prnt(serverName + " clientSocket: " + clientSocket);
				clientSocket.close();
			} catch (IOException e) {
				e.printStackTrace();
				Glog.prnte(serverName + " clientSocket.close() e: " + e.getMessage());
			}
		}

		if (srvSocket != null) {
			try {
				Glog.prnt(serverName + " srvSocket: " + srvSocket);
				srvSocket.close();
			} catch (IOException e) {
				e.printStackTrace();
				Glog.prnte(serverName + " srvSocket.close() e: " + e.getMessage());
			}
		}
		Glog.prnt(serverName + " closeAll end");
	}

}
