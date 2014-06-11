package g.tcp.server;

import helper.pack.Gh;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.ServerSocket;
import java.net.Socket;

public class Serverg2 {
	
	private Socket					socket					= null;
	private BufferedReader			br						= null;
	private ServerSocket			serverSocket			= null;
	
	private OutputStreamWriter		outputStreamWriter		= null;						// writing
	private BufferedOutputStream	bufferedOutputStream	= null;						// writing
																							
	private String					serverName				= "";
	private Integer					serverPort;
	private byte					srvId					= 0;
	
	private boolean					isConnectedToF			= false;
	private boolean					isListeningToF			= false;
	
	private int						sendPingEachTms			= 2000;
	private int						receivePingTimeotTms	= 10000;
	
	private ServerListener			srvListener;
	private long					lastPingReceivedTms		= System.currentTimeMillis();
	
	public void startServerg2() {
		Runtime.getRuntime().addShutdownHook(new Thread() {
			public void run() {
				closeAll();
			}
		});
		
		Thread receivePingThread = new Thread(new Runnable() {
			public void run() {
				while (true) {
					try {
						Thread.sleep(500);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					
					if (System.currentTimeMillis() - lastPingReceivedTms > receivePingTimeotTms) {
						Gh.prnt(serverName + " ping not reveiced over " + receivePingTimeotTms + "ms");
						lastPingReceivedTms = System.currentTimeMillis();
						Thread reconnecterThread = new Thread(new Runnable() {
							public void run() {
								reconnecter("ping not received");
							}
						});
						reconnecterThread.setPriority(Thread.MIN_PRIORITY);
						reconnecterThread.start();
					}
				}
			}
		});
		receivePingThread.setPriority(Thread.MIN_PRIORITY);
		receivePingThread.start();
		
		Thread sendPingThread = new Thread(new Runnable() {
			public void run() {
				while (true) {
					try {
						Thread.sleep(sendPingEachTms);
					} catch (InterruptedException e) {
						Gh.prnte(serverName + " sendPing thread sleep error, e=" + e.getMessage());
					}
					
					try {
						sendMsg("ping");
					} catch (Exception e) {
						Gh.prnte(serverName + " sendMsg(ping) error, e=" + e.getMessage());
					}
				}
			}
		});
		sendPingThread.setPriority(Thread.MIN_PRIORITY);
		sendPingThread.start();
	}
	
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
	
	public void sendMsg(String text) {
		// Gh.prnt(serverName + " sendMsg Start");
		if (isConnectedToF) {
			if (outputStreamWriter != null) {
				try {
					outputStreamWriter.write(text + '\n');
					outputStreamWriter.flush();
				} catch (IOException e) {
					Gh.prnte(serverName + " sendMsg error sening to Client");
					// reconnecter("sendMsg");
				}
			} else {
				Gh.prnte(serverName + " sendMsg outputStreamWriter==null");
				// reconnecter("sendMsg");
			}
		} else {
			Gh.prnte(serverName + " sendMsg cannot send msg=" + text + " because isConnectedToF = false");
			// reconnecter("sendMsg");
		}
		// Gh.prnt(serverName + " sendMsg End");
	}
	
// private
	
	private boolean createServerSocketConnection() {
		serverSocket = null;
		
		try {
			serverSocket = new ServerSocket(serverPort);
			// Gh.prnt(serverName +
// " createServerSocketConnection new serverSocket successful on port=" +
// serverPort);
			return true;
		} catch (IOException e) {
			Gh.prnte(serverName + " createServerSocketConnection new serverSocket failed on port=" + serverPort);
			serverSocket = null;
			return false;
		}
	}
	
	private boolean createSocketConnection() {
		socket = null;
		if (serverSocket != null) {
			try {
				Gh.prnt(serverName + " createSocketConnection clientSocket = serverSocket.accept() waiting on incomming connection...");
				socket = serverSocket.accept();
				Gh.prnt(serverName + " createSocketConnection clientSocket succesful port=" + serverPort);
				return true;
			} catch (IOException e) {
				Gh.prnte(serverName + " createSocketConnection clientSocket failed port=" + serverPort + ", e=" + e.getMessage());
				socket = null;
				return false;
			}
		} else {
			Gh.prnte(" createSocketConnection serverSocket == null. serverName=" + serverName + ", port=" + serverPort);
			return false;
		}
	}
	
	private boolean createReader() {
		if (socket != null) {
			try {
				br = new BufferedReader(new InputStreamReader(socket.getInputStream()));
				return true;
			} catch (IOException e1) {
				e1.printStackTrace();
				br = null;
				Gh.prnte(serverName + " createReader BufferedReader, e: " + e1.getMessage());
				return false;
			}
		} else {
			Gh.prnte(serverName + " createReader clientSocket == null");
			return false;
		}
	}
	
	private boolean createWriter() {
		bufferedOutputStream = null;
		outputStreamWriter = null;
		
		if (socket != null) {
			try {
				bufferedOutputStream = new BufferedOutputStream(socket.getOutputStream());
				// Gh.prnt("Client createWriter bufferedOutputStream succesfull");
			} catch (IOException e) {
				Gh.prnte("Client createWriter bufferedOutputStream failed, e=" + e.getMessage());
				bufferedOutputStream = null;
			}
		} else {
			Gh.prnte("Client createWriter bufferedOutputStream failed because clientSocket==null");
		}
		
		if (bufferedOutputStream != null) {
			try {
				outputStreamWriter = new OutputStreamWriter(bufferedOutputStream, "US-ASCII");
				// Gh.prnt("Client createWriter bufferedOutputStream succesfull");
				return true;
			} catch (UnsupportedEncodingException e) {
				Gh.prnte("Client createWriter bufferedOutputStream failed, e=" + e.getMessage());
				outputStreamWriter = null;
				return false;
			}
		} else {
			return false;
		}
	}
	
	private boolean createConn() {
		//Gh.prnt(serverName + "Server createConn Start");
		
		boolean chck1 = false;
		boolean chck2 = false;
		boolean chck3 = false;
		boolean chck4 = false;
		
		chck1 = createServerSocketConnection();
		
		if (chck1) {
			chck2 = createSocketConnection();
		}
		if (chck2) {
			chck3 = createReader();
		}
		if (chck3) {
			chck4 = createWriter();
		}
		
		if (!chck1) {
			//Gh.prnte(serverName + " createConn chck1==false");
		}
		if (!chck2) {
			//Gh.prnte(serverName + " createConn chck2==false");
		}
		if (!chck3) {
			//Gh.prnte(serverName + " createConn chck3==false");
		}
		if (!chck4) {
			//Gh.prnte(serverName + " createConn chck4==false");
		}
		
		if (chck1 && chck2 && chck3 && chck4) {
			//Gh.prnt(serverName + " createConn succesfull");
			//Gh.prnt(serverName + " createConn End");
			return true;
		} else {
			//Gh.prnte(serverName + " createConn not succesfull");
			//Gh.prnt(serverName + " createConn End");
			return false;
		}
	}
	
	private void serverListener() {
//		Gh.prnt(serverName + " serverListener Start");
		
		if (br != null) {
			
			Gh.prnt(serverName + " serverListener start listening (before while) final isConnectedToF=" + isConnectedToF);
			String receivedText = "";
			while (isConnectedToF) {
				isListeningToF = true;
				if (br != null) {
					receivedText = "";
					try {
						receivedText = br.readLine();
					} catch (Exception e) {
						Gh.prnte("br.readLine exception");
						receivedText = "";
						try {
							Thread.sleep(1000);
						} catch (InterruptedException e1) {
							e1.printStackTrace();
						}
					}
					
				} else {
					receivedText = null;
				}
				//Gh.prnt(serverName + " serverListener received inputLine=" + receivedText);
				if (receivedText != null) {
					if ("ping".equals(receivedText)) {
						//Gh.prnt(serverName + " serverListener checker received ping");
						lastPingReceivedTms = System.currentTimeMillis();
					} else if ("restart".equals(receivedText)) {
						reconnecter("received over TCP");
					} else {
						if (srvListener != null) {
							if (srvId == 1) {
								srvListener.incomingMessage1(receivedText);
							} else if (srvId == 2) {
								srvListener.incomingMessage2(receivedText);
							} else if (srvId == 3) {
								srvListener.incomingMessage3(receivedText);
							} else if (srvId == 4) {
								srvListener.incomingMessage4(receivedText);
							} else if (srvId == 5) {
								srvListener.incomingMessage5(receivedText);
							} else {
								
							}
							
						} else {
							Gh.prnte(serverName + " serverListener srvListener=null");
							try {
								Thread.sleep(1000);
							} catch (InterruptedException e1) {
								e1.printStackTrace();
							}
						}
					}
				} else {
					Gh.prnte(serverName + " serverListener receivedText=null breaking out of while listener");
					isListeningToF = false;
					break;
				}
				
//				try {
//					Thread.sleep(1);
//				} catch (InterruptedException e) {
//					e.printStackTrace();
//				}
			}
			
		} else {
			isListeningToF = false;
			Gh.prnte("serverListener br == null");
		}
		
		isListeningToF = false;
		Gh.prnt(serverName + " serverListener Ending all process");
	}
	
	private void closeAll() {
		//Gh.prnt(serverName + " closeAll start");
		
		if (serverSocket != null) {
			try {
				serverSocket.close();
				isConnectedToF = false;
				//Gh.prnt(serverName + " closeAll serverSocket close successful");
			} catch (IOException e) {
				//Gh.prnte(serverName + " closeAll serverSocket close failed, e=" + e.getMessage());
			}
		} else {
			//Gh.prnt(serverName + " closeAll serverSocket == null nothing to close");
		}
		
		if (socket != null) {
			try {
				socket.close();
				isConnectedToF = false;
				//Gh.prnt(serverName + " closeAll clientSocket close succesfull");
			} catch (IOException e) {
				//Gh.prnte(serverName + " closeAll clientSocket close failed, e=" + e.getMessage());
			}
		} else {
			//Gh.prnt(serverName + " closeAll clientSocket == null nothing to close");
		}
		
		//Gh.prnt(serverName + " closeAll end");
	}
	
	private void reconnecter(String calletId) {
		Gh.prnt(serverName + " reconnecter called callerId=" + calletId);
		closeAll();
		if (!isConnectedToF) { // && !isListeningToF
			Gh.prnt(serverName + " reconnecter launched callerId=" + calletId);
			
			isConnectedToF = createConn();
			if (isConnectedToF) {
				serverListener();
			} else {
				Gh.prnte(serverName + " cannot start serverListener because isConnectedToF=" + isConnectedToF + " callerId=" + calletId);
			}
			
		} else {
			Gh.prnte(serverName + " cannot start reconnecter because isConnectedToF=" + isConnectedToF + " isListeningToF=" + isListeningToF);
		}
	}
	
}
