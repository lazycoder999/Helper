package g.tcp.server;

import helper.pack.Gh;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;

public class Serverg implements Runnable {
	
	private String			serverName	= "";
	private Integer			serverPort;
	private ServerSocket	in;
	private ServerSocket	clientSocket;
	
	private byte			srvId		= 0;
	private ServerListener	srvListener;
	private ServerSocket	srvSocket	= null;
	private boolean			runningToF	= true;
	
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
		Gh.prnt(serverName + " thread started");
		newConnection();
		Gh.prnt(serverName + " thread end");
	}
	
	private void newConnection() {
		Gh.prnt(serverName + " newConnection start");
		srvSocket = null;
		
		try {
			srvSocket = new ServerSocket(serverPort);
		} catch (IOException e) {
			Gh.prnte(serverName + " Could not listen on port: " + serverPort);
		}
		
		Gh.prnt(serverName + " newConnection end");
		gServerInside();
	}
	
	private void gServerInside() {
		Gh.prnt(serverName + " gServerInside start");
		
		Socket clientSocket = null;
		Gh.prnt(serverName + " waiting for connection... on port: " + serverPort);
		
		if (srvSocket == null) {
			Gh.prnte("srvSocket == null. serverName=" + serverName);
		}
		
		try {
			clientSocket = srvSocket.accept();
			Gh.prnt(serverName + " accepted");
		} catch (IOException e) {
			Gh.prnte(serverName + " Accept failed. e=" + e.getMessage());
		}
		
		Gh.prnt(serverName + " Connection successful");
		Gh.prnt(serverName + " Waiting for input.....");
		
		BufferedReader in = null;
		
		if (clientSocket != null) {
			try {
				in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
			} catch (IOException e1) {
				e1.printStackTrace();
				Gh.prnte(serverName + " BufferedReader, e: " + e1.getMessage());
			}
		} else {
			Gh.prnte(serverName + " clientSocket == null");
		}
		
		String inputLine = null;
		
		Gh.prnt(serverName + " before while");
		
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
						Gh.prnte(serverName + " srvListener=null or inputLine = null");
						try {
							Thread.sleep(1000);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
					}
					
				}
			} catch (SocketException e2) {
				Gh.prnte(serverName + " e2: " + e2.getMessage());
				if (e2.getMessage().toString().equals("Connection reset")) {
					Gh.prnte(serverName + " konnekcion reset");
				}
			} catch (IOException e) {
				e.printStackTrace();
				Gh.prnte(serverName + " while, e: " + e.getMessage());
			}
		} else {
			Gh.prnte(serverName + " in == null");
		}
		Gh.prnt(serverName + " after while");
		
		Gh.prnt(serverName + " gServerInside end");
		//closeAll();
		//newConnection();
	}
	
	public void closeAll() {
		Gh.prnt(serverName + " closeAll start");
		runningToF = false;
		
		if (in != null) {
			try {
				Gh.prnt(serverName + " in.close: " + in);
				in.close();
			} catch (IOException e) {
				e.printStackTrace();
				Gh.prnte(serverName + " in.close() e: " + e.getMessage());
			}
		}
		
		if (clientSocket != null) {
			try {
				Gh.prnt(serverName + " clientSocket: " + clientSocket);
				clientSocket.close();
			} catch (IOException e) {
				e.printStackTrace();
				Gh.prnte(serverName + " clientSocket.close() e: " + e.getMessage());
			}
		}
		
		if (srvSocket != null) {
			try {
				Gh.prnt(serverName + " srvSocket: " + srvSocket);
				srvSocket.close();
			} catch (IOException e) {
				e.printStackTrace();
				Gh.prnte(serverName + " srvSocket.close() e: " + e.getMessage());
			}
		}
		Gh.prnt(serverName + " closeAll end");
	}
	
}
