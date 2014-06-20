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

public class Serverg3 implements Runnable {
	
	private Gh						gh						= new Gh();
	private Socket					socket					= null;
	
	private OutputStreamWriter		outputStreamWriter		= null;
	private BufferedOutputStream	bufferedOutputStream	= null;
	
	private BufferedReader			bufferedReader			= null;
	
	public int						serverPort;
	
	private int						sendPingEachTms			= 5000;
	private long					lastPingSentTns			= 0;
	private int						connectionTimeout		= 30000;
	
// unique	
	
	private ServerSocket			serverSocket			= null;
	
	public byte						srvId					= 0;
	private ServerListener			srvListener;
	
// unique
	
	public void setServerListener(ServerListener srvListener) {
		this.srvListener = srvListener;
	}
	
// common
	private void startServerg2() {
		Runtime.getRuntime().addShutdownHook(new Thread() {
			public void run() {
				closeAll();
			}
		});
		
		reconnecter("start server");
		
		Thread sendPingThr = new Thread(new Runnable() {
			public void run() {
				while (true) {
					try {
						Thread.sleep(sendPingEachTms);
					} catch (InterruptedException e) {
						Gh.prnte(serverPort + " sendPing thread sleep error, e=" + e.getMessage());
					}
					
					try {
						lastPingSentTns = System.nanoTime();
						sendMsg("PING");
					} catch (Exception e) {
						Gh.prnte(serverPort + " sendMsg(PING) error, e=" + e.getMessage());
					}
				}
			}
		});
		sendPingThr.setPriority(Thread.MAX_PRIORITY);
		sendPingThr.setName("sendPingThr");
		sendPingThr.start();
	}
	
	private boolean createConnection() {
		serverSocket = null;
		try {
			serverSocket = new ServerSocket(serverPort);
			serverSocket.setSoTimeout(connectionTimeout);
			serverSocket.setPerformancePreferences(2, 0, 1);
		} catch (IOException e) {
			Gh.prnte(serverPort + " createServerSocketConnection new serverSocket failed on port=" + serverPort);
			serverSocket = null;
			return false;
		}
		
		if (serverSocket != null) {
			try {
				Gh.prnt(serverPort + " createSocketConnection clientSocket = serverSocket.accept() waiting on incomming connection...");
				socket = serverSocket.accept();
				socket.setTcpNoDelay(true);
				socket.setSoTimeout(connectionTimeout);
				socket.setPerformancePreferences(2, 0, 1);
				Gh.prnt(serverPort + " createSocketConnection clientSocket succesful");
			} catch (IOException e) {
				Gh.prnte(serverPort + " createSocketConnection clientSocket failed, e=" + e.getMessage());
				socket = null;
				return false;
			}
		} else {
			Gh.prnte(serverPort + " createSocketConnection serverSocket == null");
			return false;
		}
		
		if (socket != null) {
			try {
				bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			} catch (IOException e1) {
				e1.printStackTrace();
				bufferedReader = null;
				Gh.prnte(serverPort + " createReader BufferedReader, e: " + e1.getMessage());
				return false;
			}
			
			try {
				bufferedOutputStream = new BufferedOutputStream(socket.getOutputStream());
				// Gh.prnt("Client createWriter bufferedOutputStream succesfull");
			} catch (IOException e) {
				Gh.prnte("Client createWriter bufferedOutputStream failed, e=" + e.getMessage());
				bufferedOutputStream = null;
				return false;
			}
		} else {
			Gh.prnte(serverPort + " createReader clientSocket == null");
			return false;
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
	
	public void sendMsg(String text) {
		if (outputStreamWriter != null) {
			try {
				text = text + '\n';
				outputStreamWriter.write(text, 0, text.length());
				outputStreamWriter.flush();
			} catch (IOException e) {
				Gh.prnte(serverPort + " sendMsg error sening to Client" + e.getMessage());
				//e.printStackTrace();
			}
		} else {
			Gh.prnte(serverPort + " sendMsg outputStreamWriter==null");
		}
	}
	
	private void reconnecter(String calletId) {
		Thread startThr = new Thread(new Runnable() {
			public void run() {
				Gh.prnt(serverPort + " reconnecter called callerId=" + calletId);
				closeAll();
				
				while (!createConnection()) {
					closeAll();
				}
				
				Thread serverListenerThr = new Thread(new Runnable() {
					public void run() {
						serverListener();
					}
				});
				serverListenerThr.setPriority(Thread.MAX_PRIORITY);
				serverListenerThr.setName("startThr");
				serverListenerThr.start();
			}
		});
		startThr.setPriority(Thread.MAX_PRIORITY);
		startThr.setName("startThr");
		startThr.start();
	}
	
	private void serverListener() {
//		Gh.prnt(serverName + " serverListener Start");
		
		Gh.prnt(serverPort + " serverListener start listening (before while)");
		String receivedText = "";
		while (true) {
			if (bufferedReader != null) {
				receivedText = "";
				try {
					receivedText = bufferedReader.readLine();
				} catch (Exception e) {
					Gh.prnte(serverPort + " br.readLine exception=" + e.getMessage());
					receivedText = "";
					break;
				}
				
			} else {
				receivedText = null;
				break;
			}
			//Gh.prnt(serverName + " serverListener received inputLine=" + receivedText);
			if (receivedText != null) {
				if ("PONG".equals(receivedText)) {
					float latency = (System.nanoTime() - lastPingSentTns);
					latency = latency / 1000000;
					if (latency > 1) {
						Gh.prnt("PONG now=" + gh.gRound(latency, 5) + "ms");
					}
					Gh.prnt("received PONG = " + gh.gRound(latency, 5) + "ms");
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
						Gh.prnte(serverPort + " serverListener srvListener=null");
						break;
					}
				}
			} else {
				Gh.prnte(serverPort + " serverListener receivedText=null breaking out of while listener");
				break;
			}
		}
		
		Gh.prnt(serverPort + " serverListener Ending all process");
		reconnecter("server listener");
	}
	
	private void closeAll() {
//		Gh.prnt("closeAll start");
		
		if (outputStreamWriter != null) {
			try {
				outputStreamWriter.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		} else {
			Gh.prnt(serverPort + " closeAll outputStreamWriter == null nothing to close");
		}
		
		if (bufferedOutputStream != null) {
			try {
				bufferedOutputStream.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		} else {
			Gh.prnt(serverPort + " closeAll bufferedOutputStream == null nothing to close");
		}
		
		if (serverSocket != null) {
			try {
				serverSocket.close();
				//Gh.prnt(serverName + " closeAll serverSocket close successful");
			} catch (IOException e) {
				Gh.prnte(serverPort + " closeAll serverSocket close failed, e=" + e.getMessage());
			}
		} else {
			//Gh.prnt(serverName + " closeAll serverSocket == null nothing to close");
		}
		
		if (socket != null) {
			try {
				socket.close();
				//Gh.prnt(serverName + " closeAll clientSocket close succesfull");
			} catch (IOException e) {
				Gh.prnte(serverPort + " closeAll clientSocket close failed, e=" + e.getMessage());
			}
		} else {
			//Gh.prnt(serverPort + " closeAll clientSocket == null nothing to close");
		}
		
//		Gh.prnt("closeAll end");
	}
	
	@Override
	public void run() {
		startServerg2();
	}
	
}
