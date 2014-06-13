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

	private Gh gh = new Gh();
	private Socket socket = null;

	private OutputStreamWriter outputStreamWriter = null;
	private BufferedOutputStream bufferedOutputStream = null;

	private BufferedReader bufferedReader = null;

	public int serverPort;
	private boolean isConnectedToF = false;
	private long lastHbReceivedTms = 0;
	private int receiveHbTimeotTms = 6000;
	private int sendHbEachTms = 2000;
	private int sendPingEachTms = 1000;
	private long lastPingSentTns = 0;

// unique	

	private ServerSocket serverSocket = null;

	public byte srvId = 0;
	private float latencySum = 0;
	private int pongCount = 0;
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
						reconnecterThread.setPriority(Thread.MAX_PRIORITY);
						reconnecterThread.start();
					}
				}
			}
		});
		receiveHbThr.setPriority(Thread.MAX_PRIORITY);
		receiveHbThr.setName("receiveHbThr");
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
		sendHbThr.setPriority(Thread.MAX_PRIORITY);
		sendHbThr.setName("sendHbThr");
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
		//012 21
		//021 21
		//102 21
		//120 21
		//201 21
		//210 21
		try {
			serverSocket = new ServerSocket(serverPort);
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
		if (isConnectedToF) {
			if (outputStreamWriter != null) {
				try {
					text = text + '\n';
					outputStreamWriter.write(text, 0, text.length());
					outputStreamWriter.flush();
				} catch (IOException e) {
					Gh.prnte(serverPort + " sendMsg error sening to Client");
				}
			} else {
				Gh.prnte(serverPort + " sendMsg outputStreamWriter==null");
			}
		} else {
			Gh.prnte(serverPort + " sendMsg cannot send msg=" + text + " because isConnectedToF = false");
		}
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
						pongCount++;
						float latency = (System.nanoTime() - lastPingSentTns);
						latency = latency / 1000000;
						latencySum = latencySum + latency;
						Gh.prnt("PONG avg = " + gh.gRound(latencySum / pongCount, 5) + "ms / now=" + gh.gRound(latency, 5) + "ms");
						//Gh.prnt("received PONG = " + gh.gRound(latency, 5) + "ms");
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
