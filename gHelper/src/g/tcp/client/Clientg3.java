package g.tcp.client;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.Socket;
import java.net.UnknownHostException;

import org.apache.log4j.Logger;

public class Clientg3 implements Runnable {
	
	private Socket					socket					= null;
	
	private OutputStreamWriter		outputStreamWriter		= null;
	private BufferedOutputStream	bufferedOutputStream	= null;
	private BufferedReader			bufferedReader			= null;
	
	public Integer					clientPort				= 0;
	private final int				connectionTimeout		= 30000;
	
	private final int				receivePingTimeotTms	= 10000;
	private long					lastPingReceivedTms		= 0;
	
// unique
	public String					ip						= "";
	private boolean					reconnecterCalled		= false;
	private String					name;
	
	private static final Logger		log						= Logger.getLogger(Clientg3.class.getName());
	
// common
	@Override
	public void run() {
		
		Runtime.getRuntime().addShutdownHook(new Thread() {
			@Override
			public void run() {
				closeAll();
			}
		});
		
		final Thread receivePingThr = new Thread(new Runnable() {
			@Override
			public void run() {
				while (true) {
					try {
						Thread.sleep(2000);
					} catch (final InterruptedException e) {
						e.printStackTrace();
					}
					
					if ((System.currentTimeMillis() - lastPingReceivedTms) > receivePingTimeotTms) {
						log.info("Clientg3:" + name + "ping not reveiced over " + receivePingTimeotTms + "ms");
						lastPingReceivedTms = System.currentTimeMillis();
						reconnecter("ping not received");
					}
				}
			}
		});
		receivePingThr.setPriority(Thread.MAX_PRIORITY);
		receivePingThr.setName("Clientg3: receivePingThr");
		receivePingThr.start();
		
		reconnecter("server listener");
	}
	
	private boolean createConnection() {
		name = ip + ":" + clientPort + " ";
		//log.info(ip + ":" + port + " createSocketConnection Start");
		
		if (ip == null || "".equals(ip)) {
			log.error("Clientg3: Client createSocketConnection ip not set, ip=" + ip + " clientPort=" + clientPort);
			return false;
		}
		
		final Integer zero = new Integer(0);
		
		if (clientPort == null || zero.equals(clientPort)) {
			log.error("Clientg3: Client createSocketConnection port not set, ip=" + ip + " clientPort=" + clientPort);
			return false;
		}
		
		socket = null;
		
		try {
			socket = new Socket(ip, clientPort);
			socket.setSoTimeout(connectionTimeout);
			socket.setTcpNoDelay(true);
			socket.setPerformancePreferences(2, 0, 1);
		} catch (final UnknownHostException e) {
			log.error("Clientg3:" + name + "createSocketConnection clientSocket failed, e=" + e.getMessage());
			socket = null;
			return false;
		} catch (final IOException e) {
			log.error("Clientg3:" + name + "createSocketConnection clientSocket failed, e=" + e.getMessage());
			socket = null;
			return false;
		}
		
		bufferedOutputStream = null;
		outputStreamWriter = null;
		
		if (socket != null) {
			try {
				bufferedOutputStream = new BufferedOutputStream(socket.getOutputStream());
			} catch (final IOException e) {
				log.error("Clientg3:" + name + "createWriter bufferedOutputStream failed, e=" + e.getMessage());
				bufferedOutputStream = null;
			}
			
			if (bufferedOutputStream != null) {
				try {
					outputStreamWriter = new OutputStreamWriter(bufferedOutputStream, "US-ASCII");
				} catch (final UnsupportedEncodingException e) {
					e.printStackTrace();
					log.error("Clientg3:" + name + "createWriter bufferedOutputStream failed, e=" + e.getMessage());
					outputStreamWriter = null;
					return false;
				}
			} else {
				return false;
			}
			
			try {
				bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
				return true;
			} catch (final IOException e) {
				e.printStackTrace();
				log.error("Clientg3:" + name + "createReader BufferedReader failed, e=" + e.getMessage());
				bufferedReader = null;
				return false;
			}
		} else {
			log.error("Clientg3:" + name + "socket==null");
			return false;
		}
		
	}
	
	public void sendMsg(String text) {
		if (outputStreamWriter != null && text != null && !"".equals(text) && text.length() > 1) {
			try {
				//log.info("Client sendMsg text=" + text);
				text = text + '\n';
				outputStreamWriter.write(text, 0, text.length());
				outputStreamWriter.flush();
			} catch (final IOException e) {
				log.error("Clientg3:" + name + "sendMsg exception: " + e.getMessage());
			}
		} else {
			log.error("Clientg3:" + name + "sendMsg not sending text=" + text);
		}
	}
	
	private void reconnecter(final String caller) {
		log.info("Clientg3:" + name + "reconnecter called=" + caller);
		if (ip != null && !"".equals(ip)) {
			if (!reconnecterCalled) {
				reconnecterCalled = true;
				
				while (!createConnection()) {
					closeAll();
					try {
						Thread.sleep(1000);
					} catch (final InterruptedException e) {
						e.printStackTrace();
					}
				}
				reconnecterCalled = false;
				serverListener();
			} else {
				log.error("Clientg3:" + name + "reconnecter cannot be called because reconnecterCalled=" + reconnecterCalled + "(already called)");
			}
		} else {
			log.error("Clientg3:" + name + "reconnecter failed to launch because ip=" + ip + " caller=" + caller);
		}
	}
	
	private void serverListener() {
		log.info("Clientg3:" + name + "called serverListener");
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
							log.error("Clientg3:" + name + "serverListener something wrong received, receivedText=" + receivedText);
						}
					} else {
						log.error("Clientg3:" + name + "receivedText=" + receivedText);
						break;
					}
				} catch (final IOException e) {
					log.error("Clientg3:" + name + "serverListener error on readLine, e=" + e.getMessage());
					break;
				}
			} else {
				log.error("Clientg3:" + name + "serverListener bufferedReader=" + bufferedReader + " socket=" + socket);
				break;
			}
		}
		log.info("Clientg3:" + name + "exiting serverListener");
		reconnecter("server listener");
	}
	
	public void closeAll() {
		//log.info("Client closeConn Start");
		if (outputStreamWriter != null) {
			try {
				// osw.flush();
				outputStreamWriter.close();
			} catch (final IOException e1) {
				//log.error("closeConn, e: " + e1.getMessage());
			}
		} else {
			
		}
		
		if (socket != null) {
			try {
				socket.close();
			} catch (final IOException e) {
				//log.error("Client closeConn, e: " + e.getMessage());
			}
		} else {
			
		}
		
		//log.info("Client closeConn End");
	}
	
}