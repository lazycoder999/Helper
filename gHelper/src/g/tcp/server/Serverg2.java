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

	private Socket socket = null;

	private OutputStreamWriter outputStreamWriter = null;
	private BufferedOutputStream bufferedOutputStream = null;

	private BufferedReader bufferedReader = null;

	public Integer serverPort;
	private boolean isConnectedToF = false;
	private long lastHbReceivedTms = 0;
	private int receiveHbTimeotTms = 10000;
	private int sendHbEachTms = 2000;
	private int sendPingEachTms = 2000;
	private long lastPingSentTms = 0;

// unique	

	private ServerSocket serverSocket = null;

	public byte srvId = 0;

	private ServerListener srvListener;

// unique

	public void setServerListener(ServerListener srvListener) {
		this.srvListener = srvListener;
	}

// common
	public void startServerg2() {
		Runtime.getRuntime().addShutdownHook(new Thread() {
			public void run() {
				closeAll();
			}
		});

		Thread receiveHbThr = new Thread(new Runnable() {
			public void run() {
				while (true) {
					try {
						Thread.sleep(500);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}

					if (System.currentTimeMillis() - lastHbReceivedTms > receiveHbTimeotTms) {
						Gh.prnt(serverPort + " HB not reveiced over " + receiveHbTimeotTms + "ms");
						lastHbReceivedTms = System.currentTimeMillis();
						Thread reconnecterThread = new Thread(new Runnable() {
							public void run() {
								reconnecter("HB not received");
							}
						});
						reconnecterThread.setPriority(Thread.MIN_PRIORITY);
						reconnecterThread.start();
					}
				}
			}
		});
		receiveHbThr.setPriority(Thread.MIN_PRIORITY);
		receiveHbThr.start();

		Thread sendHbThr = new Thread(new Runnable() {
			public void run() {
				while (true) {
					try {
						Thread.sleep(sendHbEachTms);
					} catch (InterruptedException e) {
						Gh.prnte(serverPort + " sendHb thread sleep error, e=" + e.getMessage());
					}

					try {
						sendMsg("HB");
					} catch (Exception e) {
						Gh.prnte(serverPort + " sendMsg(HB) error, e=" + e.getMessage());
					}
				}
			}
		});
		sendHbThr.setPriority(Thread.MIN_PRIORITY);
		sendHbThr.start();

		Thread sendPingThr = new Thread(new Runnable() {
			public void run() {
				while (true) {
					try {
						Thread.sleep(sendPingEachTms);
					} catch (InterruptedException e) {
						Gh.prnte(serverPort + " sendPing thread sleep error, e=" + e.getMessage());
					}

					try {
						lastPingSentTms = System.currentTimeMillis();
						sendMsg("PING");
					} catch (Exception e) {
						Gh.prnte(serverPort + " sendMsg(PING) error, e=" + e.getMessage());
					}
				}
			}
		});
		sendPingThr.setPriority(Thread.MIN_PRIORITY);
		sendPingThr.start();
	}

	private boolean createConnection() {
		serverSocket = null;

		try {
			serverSocket = new ServerSocket(serverPort);
			serverSocket.setPerformancePreferences(0, 0, 0);
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
				socket.setPerformancePreferences(0, 0, 0);
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
		// Gh.prnt(serverName + " sendMsg Start");
		if (isConnectedToF) {
			if (outputStreamWriter != null) {
				try {
					outputStreamWriter.write(text + '\n');
					outputStreamWriter.flush();
				} catch (IOException e) {
					Gh.prnte(serverPort + " sendMsg error sening to Client");
					// reconnecter("sendMsg");
				}
			} else {
				Gh.prnte(serverPort + " sendMsg outputStreamWriter==null");
				// reconnecter("sendMsg");
			}
		} else {
			Gh.prnte(serverPort + " sendMsg cannot send msg=" + text + " because isConnectedToF = false");
			// reconnecter("sendMsg");
		}
		// Gh.prnt(serverName + " sendMsg End");
	}

	private void reconnecter(String calletId) {
		Gh.prnt(serverPort + " reconnecter called callerId=" + calletId);
		closeAll();
		if (!isConnectedToF) { // && !isListeningToF
			//Gh.prnt(serverPort + " reconnecter launched callerId=" + calletId);

			isConnectedToF = createConnection();
			if (isConnectedToF) {
				serverListener();
			} else {
				Gh.prnte(serverPort + " cannot start serverListener because isConnectedToF=" + isConnectedToF + " callerId=" + calletId);
			}

		} else {
			Gh.prnte(serverPort + " cannot start reconnecter because isConnectedToF=" + isConnectedToF);
		}
	}

	private void serverListener() {
//		Gh.prnt(serverName + " serverListener Start");

		if (bufferedReader != null) {

			Gh.prnt(serverPort + " serverListener start listening (before while) final isConnectedToF=" + isConnectedToF);
			String receivedText = "";
			while (isConnectedToF) {
				if (bufferedReader != null) {
					receivedText = "";
					try {
						receivedText = bufferedReader.readLine();
					} catch (Exception e) {
						Gh.prnte(serverPort + " br.readLine exception");
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
					if ("HB".equals(receivedText)) {
						//Gh.prnt(serverPort + " serverListener checker received HB");
						lastHbReceivedTms = System.currentTimeMillis();
					} else if ("restart".equals(receivedText)) {
						reconnecter("received over TCP");
					} else if ("PONG".equals(receivedText)) {
						Gh.prnt("received PONG = " + (System.currentTimeMillis() - lastPingSentTms) + "ms");
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
							try {
								Thread.sleep(1000);
							} catch (InterruptedException e1) {
								e1.printStackTrace();
							}
						}
					}
				} else {
					Gh.prnte(serverPort + " serverListener receivedText=null breaking out of while listener");
					try {
						Thread.sleep(1000);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
//					isListeningToF = false;
//					break;
				}
			}

		} else {
			Gh.prnte("serverListener br == null");
		}

		Gh.prnt(serverPort + " serverListener Ending all process");
	}

	private void closeAll() {
		//Gh.prnt(serverName + " closeAll start");

//		if (outputStreamWriter != null) {
//			try {
//				outputStreamWriter.close();
//			} catch (IOException e) {
//				e.printStackTrace();
//			}
//		} else {
//			Gh.prnt(serverPort + " closeAll outputStreamWriter == null nothing to close");
//		}
//
//		if (bufferedOutputStream != null) {
//			try {
//				bufferedOutputStream.close();
//			} catch (IOException e) {
//				e.printStackTrace();
//			}
//		} else {
//			Gh.prnt(serverPort + " closeAll bufferedOutputStream == null nothing to close");
//		}

		if (serverSocket != null) {
			try {
				serverSocket.close();
				isConnectedToF = false;
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
				isConnectedToF = false;
				//Gh.prnt(serverName + " closeAll clientSocket close succesfull");
			} catch (IOException e) {
				Gh.prnte(serverPort + " closeAll clientSocket close failed, e=" + e.getMessage());
			}
		} else {
			//Gh.prnt(serverPort + " closeAll clientSocket == null nothing to close");
		}

		//Gh.prnt(serverName + " closeAll end");
	}
}
