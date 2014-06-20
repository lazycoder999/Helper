package g.tcp.client;

import helper.pack.Gh;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.Socket;
import java.net.UnknownHostException;

public class Clientg3 implements Runnable {
	
	private Socket					socket					= null;
	
	private OutputStreamWriter		outputStreamWriter		= null;
	private BufferedOutputStream	bufferedOutputStream	= null;
	
	private BufferedReader			bufferedReader			= null;
	
	public Integer					clientPort				= 0;
	private long					lastPingReceivedTms		= 0;
	private int						receivePingTimeotTms	= 10000;
	
// unique	
	public String					ip						= "";
	private boolean					reconnecterCalled		= false;
	
// common
	private void startClientg2() {
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
						Gh.prnt(ip + ":" + clientPort + " ping not reveiced over " + receivePingTimeotTms + "ms");
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
		//Gh.prnt(ip + ":" + port + " createSocketConnection Start");
		//Gh.prnt("Client createSocketConnection on, ip=" + ip + " port=" + port);
		
		if (ip == null || "".equals(ip)) {
			//Gh.prnte("Client createSocketConnection ip not set, ip=" + ip + " port=" + port);
			return false;
		}
		
		Integer zero = new Integer(0);
		
		if (clientPort == null || zero.equals(clientPort)) {
			//Gh.prnte("Client createSocketConnection port not set, ip=" + ip + " port=" + port);
			return false;
		}
		
		socket = null;
		
		try {
			socket = new Socket(ip, clientPort);
			socket.setTcpNoDelay(true);
			socket.setPerformancePreferences(2, 0, 1);
			
			//Gh.prnt(ip + ":" + port + " createSocketConnection clientSocket succesfull");
		} catch (UnknownHostException e) {
			Gh.prnte(ip + ":" + clientPort + " createSocketConnection clientSocket failed, e=" + e.getMessage());
			socket = null;
			return false;
		} catch (IOException e) {
			Gh.prnte(ip + ":" + clientPort + " createSocketConnection clientSocket failed, e=" + e.getMessage());
			socket = null;
			return false;
		}
		
		bufferedOutputStream = null;
		outputStreamWriter = null;
		
		if (socket != null) {
			try {
				bufferedOutputStream = new BufferedOutputStream(socket.getOutputStream());
				//Gh.prnt(ip + ":" + port + " createWriter bufferedOutputStream succesfull");
			} catch (IOException e) {
				Gh.prnte(ip + ":" + clientPort + " createWriter bufferedOutputStream failed, e=" + e.getMessage());
				bufferedOutputStream = null;
			}
			
			if (bufferedOutputStream != null) {
				try {
					outputStreamWriter = new OutputStreamWriter(bufferedOutputStream, "US-ASCII");
					//Gh.prnt(ip + ":" + port + " createWriter bufferedOutputStream succesfull");
				} catch (UnsupportedEncodingException e) {
					Gh.prnte(ip + ":" + clientPort + " createWriter bufferedOutputStream failed, e=" + e.getMessage());
					outputStreamWriter = null;
					return false;
				}
			} else {
				return false;
			}
			
			try {
				bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
				//Gh.prnt(ip + ":" + port + " createReader BufferedReader succesfull");
				return true;
			} catch (IOException e) {
				e.printStackTrace();
				Gh.prnte(ip + ":" + clientPort + " createReader BufferedReader failed, e=" + e.getMessage());
				bufferedReader = null;
				return false;
			}
		} else {
			//Gh.prnte("Client createWriter bufferedOutputStream failed because clientSocket==null");
			return false;
		}
		
	}
	
	public void sendMsg(String text) {
		//Gh.prnt("client send");
		if (outputStreamWriter != null && text != null && !"".equals(text) && text.length() > 1) {
			try {
				//Gh.prnt("Client sendMsg text=" + text);	
				text = text + '\n';
				outputStreamWriter.write(text, 0, text.length());
				outputStreamWriter.flush();
			} catch (IOException e) {
				Gh.prnte(ip + ":" + clientPort + " sendMsg exception: " + e.getMessage());
				//e.printStackTrace();
			}
		} else {
			Gh.prnte(ip + ":" + clientPort + " sendMsg not sending text=" + text);
		}
	}
	
	private void reconnecter(final String caller) {
		Gh.prnt(ip + ":" + clientPort + " reconnecter called=" + caller);
		if (ip != null && !"".equals(ip)) {
			if (!reconnecterCalled) {
				reconnecterCalled = true;
				//Gh.prnt(ip + ":" + port + " reconnecter launched caller=" + caller);
				
				while (!createConnection()) {
					closeAll();
				}
				
				Gh.prnt(ip + ":" + clientPort + " reconnecter,  createConn=true");
				//Gh.prnt(ip + ":" + port + " reconnecter connected to ip=" + socket.getInetAddress() + " port=" + socket.getPort());					
				reconnecterCalled = false;
				serverListener();
			} else {
				Gh.prnte(ip + ":" + clientPort + " reconnecter cannot be called because reconnecterCalled=" + reconnecterCalled + "(already called)");
			}
		} else {
			Gh.prnte(ip + ":" + clientPort + " reconnecter failed to launch because ip=" + ip + " caller=" + caller);
		}
	}
	
	private void serverListener() {
		Gh.prnt(ip + ":" + clientPort + " called serverListener");
		String receivedText = "";
		while (true) {
			// Gh.prnt("Client serverListener looping");
			if (bufferedReader != null && socket != null) {
				try {
					//Gh.prnt(ip + ":" + port + " bufferedReader.readLine()...");
					receivedText = bufferedReader.readLine();
					if (receivedText != null) {
//						Gh.prnt("Client serverListener received text=" + receivedText);
						if ("PING".equals(receivedText)) {
							//Gh.prnt(ip + ":" + clientPort + " ping received");
							lastPingReceivedTms = System.currentTimeMillis();
							sendMsg("PONG");
						} else {
							Gh.prnte(ip + ":" + clientPort + " serverListener something wrong received, receivedText=" + receivedText);
						}
					} else {
						Gh.prnte("receivedText=" + receivedText);
						break;
					}
				} catch (IOException e) {
					Gh.prnte(ip + ":" + clientPort + " serverListener error on readLine, e=" + e.getMessage());
					//e.printStackTrace();
					break;
				}
			} else {
				Gh.prnte(ip + ":" + clientPort + " serverListener text receiver text=" + receivedText + " bufferedReader=null or socket=null");
				break;
			}
		}
		Gh.prnt(ip + ":" + clientPort + " exiting serverListener");
		reconnecter("server listener");
	}
	
	public void closeAll() {
		//Gh.prnt("Client closeConn Start");
		if (outputStreamWriter != null) {
			try {
				// osw.flush();
				outputStreamWriter.close();
			} catch (IOException e1) {
				//Gh.prnte("closeConn, e: " + e1.getMessage());
			}
		} else {
			
		}
		
		if (socket != null) {
			try {
				socket.close();
			} catch (IOException e) {
				//Gh.prnte("Client closeConn, e: " + e.getMessage());
			}
		} else {
			
		}
		
		//Gh.prnt("Client closeConn End");
	}
	
	@Override
	public void run() {
		startClientg2();
	}
}