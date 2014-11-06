package g.tcp.server;

import helper.Gh;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.ServerSocket;
import java.net.Socket;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Serverg3 implements Runnable {
	
	private Socket					socket					= null;
	
	private OutputStreamWriter		outputStreamWriter		= null;
	private BufferedOutputStream	bufferedOutputStream	= null;
	private BufferedReader			bufferedReader			= null;
	
	public Integer					srvPort					= 0;
	private final int				connectionTimeout		= 30000;
	
	private final int				sendPingEachTms			= 5000;
	private long					lastPingSentTns			= 0;
	
// unique
	
	private ServerSocket			serverSocket			= null;
	private final Gh				gh						= new Gh();
	public byte						srvId					= 0;
	private ServerListener			srvListener;
	public boolean					isConnected				= false;
	
	private static final Logger		log						= LogManager.getLogger(Serverg3.class.getName());
	
// unique
	
	public void setServerListener(final ServerListener srvListener) {
		this.srvListener = srvListener;
	}
	
// common
	@Override
	public void run() {
		
		Runtime.getRuntime().addShutdownHook(new Thread() {
			@Override
			public void run() {
				closeAll();
			}
		});
		
		reconnecter("start server");
		
		final Thread sendPingThr = new Thread(new Runnable() {
			@Override
			public void run() {
				while (true) {
					try {
						Thread.sleep(sendPingEachTms);
					} catch (final InterruptedException e) {
						log.error("Serverg3:" + srvPort + " sendPing thread sleep error, e=" + e.getMessage());
					}
					
					try {
						lastPingSentTns = System.nanoTime();
						sendMsg("PING");
					} catch (final Exception e) {
						log.error("Serverg3:" + srvPort + " sendMsg(PING) error, e=" + e.getMessage());
					}
				}
			}
		});
		sendPingThr.setPriority(Thread.MAX_PRIORITY);
		sendPingThr.setName("Serverg3: sendPingThr");
		sendPingThr.start();
	}
	
	private boolean createConnection() {
		serverSocket = null;
		try {
			serverSocket = new ServerSocket(srvPort);
			serverSocket.setSoTimeout(connectionTimeout);
			serverSocket.setPerformancePreferences(2, 0, 1);
		} catch (final IOException e) {
			log.error("Serverg3:" + srvPort + " createServerSocketConnection new serverSocket failed on port=" + srvPort);
			serverSocket = null;
			return false;
		}
		
		if (serverSocket != null) {
			try {
				log.info("Serverg3:" + srvPort + " createSocketConnection clientSocket = serverSocket.accept() waiting on incomming connection...");
				socket = serverSocket.accept();
				socket.setTcpNoDelay(true);
				socket.setSoTimeout(connectionTimeout);
				socket.setPerformancePreferences(2, 0, 1);
				log.info("Serverg3:" + srvPort + " createSocketConnection clientSocket succesful");
			} catch (final IOException e) {
				log.error("Serverg3:" + srvPort + " createSocketConnection clientSocket failed, e=" + e.getMessage());
				socket = null;
				return false;
			}
		} else {
			log.error("Serverg3:" + srvPort + " createSocketConnection serverSocket == null");
			return false;
		}
		
		if (socket != null) {
			try {
				bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			} catch (final IOException e1) {
				e1.printStackTrace();
				bufferedReader = null;
				log.error("Serverg3:" + srvPort + " createReader BufferedReader, e: " + e1.getMessage());
				return false;
			}
			
			try {
				bufferedOutputStream = new BufferedOutputStream(socket.getOutputStream());
				// log.info("Client createWriter bufferedOutputStream succesfull");
			} catch (final IOException e) {
				log.error("Serverg3: createWriter bufferedOutputStream failed, e=" + e.getMessage());
				bufferedOutputStream = null;
				return false;
			}
		} else {
			log.error("Serverg3:" + srvPort + " createReader clientSocket == null");
			return false;
		}
		
		if (bufferedOutputStream != null) {
			try {
				outputStreamWriter = new OutputStreamWriter(bufferedOutputStream, "US-ASCII");
				// log.info("Client createWriter bufferedOutputStream succesfull");
				return true;
			} catch (final UnsupportedEncodingException e) {
				log.error("Serverg3: createWriter bufferedOutputStream failed, e=" + e.getMessage());
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
			} catch (final IOException e) {
				log.error("Serverg3:" + srvPort + " sendMsg error = " + e.getMessage());
			}
		} else {
			log.error("Serverg3:" + srvPort + " sendMsg outputStreamWriter=" + outputStreamWriter);
		}
	}
	
	private void reconnecter(final String calletId) {
		final Thread serverReconnecterThr = new Thread(new Runnable() {
			@Override
			public void run() {
				isConnected = false;
				log.info("Serverg3:" + srvPort + " reconnecter called callerId=" + calletId);
				closeAll();
				
				while (!createConnection()) {
					closeAll();
					try {
						Thread.sleep(1000);
					} catch (final InterruptedException e) {
						e.printStackTrace();
					}
				}
				
				final Thread serverListenerThr = new Thread(new Runnable() {
					@Override
					public void run() {
						serverListener();
					}
				});
				serverListenerThr.setPriority(Thread.MAX_PRIORITY);
				serverListenerThr.setName("Serverg3: serverListenerThr");
				serverListenerThr.start();
			}
		});
		serverReconnecterThr.setPriority(Thread.MAX_PRIORITY);
		serverReconnecterThr.setName("Serverg3: serverReconnecterThr");
		serverReconnecterThr.start();
	}
	
	private void serverListener() {
//		log.info(serverName + " serverListener Start");
		
		log.info("Serverg3:" + srvPort + " serverListener start listening (before while)");
		String receivedText = "";
		while (true) {
			if (bufferedReader != null) {
				receivedText = "";
				try {
					receivedText = bufferedReader.readLine();
				} catch (final Exception e) {
					log.error("Serverg3:" + srvPort + " br.readLine exception=" + e.getMessage());
					receivedText = "";
					break;
				}
				
			} else {
				receivedText = null;
				break;
			}
			//log.info(serverName + " serverListener received inputLine=" + receivedText);
			if (receivedText != null) {
				if ("PONG".equals(receivedText)) {
					isConnected = true;
					float latency = (System.nanoTime() - lastPingSentTns);
					latency = latency / 1000000;
					if (latency > 1) {
						log.info("Serverg3: PONG now=" + gh.gRound(latency, 5) + "ms");
					}
					//log.info("received PONG = " + gh.gRound(latency, 5) + "ms");
				} else {
					if (srvListener != null) {
						if (srvId == 1) {
							srvListener.onSocketMsg1(receivedText);
						} else if (srvId == 2) {
							srvListener.onSocketMsg2(receivedText);
						} else if (srvId == 3) {
							srvListener.onSocketMsg3(receivedText);
						} else if (srvId == 4) {
							srvListener.onSocketMsg4(receivedText);
						} else if (srvId == 5) {
							srvListener.onSocketMsg5(receivedText);
						} else if (srvId == 6) {
							srvListener.onSocketMsg6(receivedText);
						} else if (srvId == 7) {
							srvListener.onSocketMsg7(receivedText);
						} else if (srvId == 8) {
							srvListener.incomingMessage8(receivedText);
						} else if (srvId == 9) {
							srvListener.incomingMessage9(receivedText);
						} else if (srvId == 10) {
							srvListener.incomingMessage10(receivedText);
						} else if (srvId == 11) {
							srvListener.incomingMessage11(receivedText);
						} else if (srvId == 12) {
							srvListener.incomingMessage12(receivedText);
						} else if (srvId == 13) {
							srvListener.incomingMessage12(receivedText);
						} else if (srvId == 14) {
							srvListener.incomingMessage12(receivedText);
						} else if (srvId == 15) {
							srvListener.incomingMessage12(receivedText);
						} else if (srvId == 16) {
							srvListener.incomingMessage12(receivedText);
						} else if (srvId == 17) {
							srvListener.incomingMessage12(receivedText);
						} else if (srvId == 18) {
							srvListener.incomingMessage12(receivedText);
						} else if (srvId == 19) {
							srvListener.incomingMessage12(receivedText);
						} else if (srvId == 20) {
							srvListener.incomingMessage12(receivedText);
						} else if (srvId == 21) {
							srvListener.incomingMessage12(receivedText);
						} else if (srvId == 22) {
							srvListener.incomingMessage12(receivedText);
						} else if (srvId == 23) {
							srvListener.incomingMessage12(receivedText);
						} else if (srvId == 24) {
							srvListener.incomingMessage12(receivedText);
						} else if (srvId == 25) {
							srvListener.incomingMessage12(receivedText);
						} else if (srvId == 26) {
							srvListener.incomingMessage12(receivedText);
						} else if (srvId == 27) {
							srvListener.incomingMessage12(receivedText);
						} else if (srvId == 28) {
							srvListener.incomingMessage12(receivedText);
						} else if (srvId == 29) {
							srvListener.incomingMessage12(receivedText);
						} else if (srvId == 30) {
							srvListener.incomingMessage12(receivedText);
						}
						
						else {
							
						}
						
					} else {
						log.error("Serverg3:" + srvPort + " serverListener srvListener=null");
						break;
					}
				}
			} else {
				log.error("Serverg3:" + srvPort + " serverListener receivedText=null breaking out of while listener");
				break;
			}
		}
		
		log.info("Serverg3:" + srvPort + " serverListener Ending all process");
		reconnecter("server listener");
	}
	
	private void closeAll() {
//		log.info("closeAll start");
		
		if (outputStreamWriter != null) {
			try {
				outputStreamWriter.close();
			} catch (final IOException e) {
				e.printStackTrace();
			}
		} else {
			log.info("Serverg3:" + srvPort + " closeAll outputStreamWriter == null nothing to close");
		}
		
		if (bufferedOutputStream != null) {
			try {
				bufferedOutputStream.close();
			} catch (final IOException e) {
				e.printStackTrace();
			}
		} else {
			log.info("Serverg3:" + srvPort + " closeAll bufferedOutputStream == null nothing to close");
		}
		
		if (serverSocket != null) {
			try {
				serverSocket.close();
				//log.info(serverName + " closeAll serverSocket close successful");
			} catch (final IOException e) {
				log.error("Serverg3:" + srvPort + " closeAll serverSocket close failed, e=" + e.getMessage());
			}
		} else {
			//log.info(serverName + " closeAll serverSocket == null nothing to close");
		}
		
		if (socket != null) {
			try {
				socket.close();
				//log.info(serverName + " closeAll clientSocket close succesfull");
			} catch (final IOException e) {
				log.error("Serverg3:" + srvPort + " closeAll clientSocket close failed, e=" + e.getMessage());
			}
		} else {
			//log.info(serverPort + " closeAll clientSocket == null nothing to close");
		}
		
//		log.info("closeAll end");
	}
	
}
