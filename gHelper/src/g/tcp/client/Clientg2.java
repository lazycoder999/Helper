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

public class Clientg2 {
	
	private Socket					socket					= null;						// writing
// && reading
																							
	private OutputStreamWriter		outputStreamWriter		= null;						// writing
	private BufferedOutputStream	bufferedOutputStream	= null;						// writing
	private BufferedReader			bufferedReader			= null;						// reading
																							
	public String					ip						= "";
	public Integer					port					= 0;
	
	private boolean					isConnectedToF			= false;
	private boolean					reconnecterCalled		= false;
	private long					lastPingReceivedTms		= System.currentTimeMillis();
	private int						receivePingTimeotTms	= 5000;
	
	public void startClientg2() {
		Runtime.getRuntime().addShutdownHook(new Thread() {
			public void run() {
				closeConn();
			}
		});
		
		Thread listenerThread = new Thread(new Runnable() {
			public void run() {
				while (true) {
					try {
						Thread.sleep(200);
					} catch (InterruptedException e1) {
						e1.printStackTrace();
					}
					serverListener();
				}
			}
		});
		listenerThread.setPriority(Thread.MIN_PRIORITY);
		listenerThread.start();
		
		Thread receivePingThread = new Thread(new Runnable() {
			public void run() {
				while (true) {
					try {
						Thread.sleep(500);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					
					if ((System.currentTimeMillis() - lastPingReceivedTms) > receivePingTimeotTms) {
						Gh.prnt("Client ping not reveiced over " + receivePingTimeotTms + "ms");
						lastPingReceivedTms = System.currentTimeMillis();
						reconnecter("ping not received");
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
						Thread.sleep(2000);
					} catch (InterruptedException e) {
						Gh.prnte("Client sendPing thread sleep error, e=" + e.getMessage());
					}
					
					try {
						sendMsg("ping");
					} catch (Exception e) {
						Gh.prnte("Client sendMsg(ping) error, e=" + e.getMessage());
					}
				}
			}
		});
		sendPingThread.setPriority(Thread.MIN_PRIORITY);
		sendPingThread.start();
	}
	
	public void reconnecter(final String caller) {
		Gh.prnt("Client reconnecter called=" + caller);
		if (ip != null && !"".equals(ip)) {
			if (!reconnecterCalled) {
				reconnecterCalled = true;
				Gh.prnt("Client reconnecter launched caller=" + caller);
				
				//sendMsg("reastart");
				closeConn();
				boolean chck2 = createConn();
				
				if (chck2) {
					Gh.prnt("Client reconnecter connected to ip=" + socket.getInetAddress() + " port=" + socket.getPort());
					reconnecterCalled = false;
				} else {
					Gh.prnte("Client reconnecter, chck2=false createConn=false");
					reconnecterCalled = false;
				}
			} else {
				Gh.prnte("Client reconnecter cannot be called because reconnecterCalled=" + reconnecterCalled + "(already called)");
			}
		} else {
			Gh.prnte("Client reconnecter failed to launch because ip=" + ip + " caller=" + caller);
			isConnectedToF = false;
		}
	}
	
	public void sendMsg(String text) {
		if (isConnectedToF && text != null && !"".equals(text) && text.length() > 1) {
			try {
				//Gh.prnt("Client sendMsg text=" + text);
				outputStreamWriter.write(text + '\n');
				outputStreamWriter.flush();
			} catch (IOException e) {
				Gh.prnte("Client sendMsg exception: " + e.getMessage());
				if (e.getMessage().contains("Connection reset by peer")) {
					Gh.prnt("Client detected connection reset");
					isConnectedToF = false;
					// reconnecter("sendMsg 1");
				} else {
					Gh.prnte("Client error sendMsg unexpected error, e:" + e.getMessage());
					isConnectedToF = false;
					// reconnecter("sendmsg 2");
				}
			}
		} else {
			Gh.prnte("Client sendMsg not sending because isConnectedTof == false");
			// reconnecter("sendmsg 3");
		}
	}
	
// private
	
	private boolean createSocketConnection() {
		//Gh.prnt("Client createSocketConnection Start");
		//Gh.prnt("Client createSocketConnection on, ip=" + ip + " port=" + port);
		
		if (ip == null || "".equals(ip)) {
			//Gh.prnte("Client createSocketConnection ip not set, ip=" + ip + " port=" + port);
			return false;
		}
		
		Integer zero = new Integer(0);
		
		if (port == null || zero.equals(port)) {
			//Gh.prnte("Client createSocketConnection port not set, ip=" + ip + " port=" + port);
			return false;
		}
		
		socket = null;
		
		try {
			socket = new Socket(ip, port);
			//Gh.prnt("Client createSocketConnection clientSocket succesfull");
			//Gh.prnt("Client createSocketConnection End");
			return true;
		} catch (UnknownHostException e) {
			//Gh.prnte("Client createSocketConnection clientSocket failed, e=" + e.getMessage());
			//Gh.prnt("Client createSocketConnection End");
			socket = null;
			return false;
		} catch (IOException e) {
			//Gh.prnte("Client createSocketConnection clientSocket failed, e=" + e.getMessage());
			//Gh.prnt("Client createSocketConnection End");
			socket = null;
			return false;
		}
	}
	
	private boolean createWriter() {
		bufferedOutputStream = null;
		outputStreamWriter = null;
		
		if (socket != null) {
			try {
				bufferedOutputStream = new BufferedOutputStream(socket.getOutputStream());
				//Gh.prnt("Client createWriter bufferedOutputStream succesfull");
			} catch (IOException e) {
				//Gh.prnte("Client createWriter bufferedOutputStream failed, e=" + e.getMessage());
				bufferedOutputStream = null;
			}
		} else {
			//Gh.prnte("Client createWriter bufferedOutputStream failed because clientSocket==null");
		}
		
		if (bufferedOutputStream != null) {
			try {
				outputStreamWriter = new OutputStreamWriter(bufferedOutputStream, "US-ASCII");
				//Gh.prnt("Client createWriter bufferedOutputStream succesfull");
				return true;
			} catch (UnsupportedEncodingException e) {
				//Gh.prnte("Client createWriter bufferedOutputStream failed, e=" + e.getMessage());
				outputStreamWriter = null;
				return false;
			}
		} else {
			return false;
		}
	}
	
	private boolean createReader() {
		bufferedReader = null;
		
		if (socket != null) {
			try {
				bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
				//Gh.prnt("Client createReader BufferedReader succesfull");
				return true;
			} catch (IOException e) {
				e.printStackTrace();
				//Gh.prnte("Client createReader BufferedReader failed, e=" + e.getMessage());
				bufferedReader = null;
				return false;
			}
		} else {
			//Gh.prnte("Client createReader bufferedReader cannot be create because socket==null");
			return false;
		}
	}
	
	private boolean createConn() {
		//Gh.prnt("Client createConn Start");
		
		boolean chck1 = false;
		boolean chck2 = false;
		boolean chck3 = false;
		
		chck1 = createSocketConnection();
		
		if (chck1) {
			chck2 = createWriter();
		}
		if (chck2) {
			chck3 = createReader();
		}
		
//		if (!chck1) {
//			Gh.prnte("Client createConn chck1==false");
//		}
//		if (!chck2) {
//			Gh.prnte("Client createConn chck2==false");
//		}
//		if (!chck3) {
//			Gh.prnte("Client createConn chck3==false");
//		}
		
		if (chck1 && chck2 && chck3) {
//			Gh.prnt("Client createConn succesfull");
//			Gh.prnt("Client createConn End");
			isConnectedToF = true;
			return true;
		} else {
//			Gh.prnte("Client createConn failed");
//			Gh.prnt("Client createConn End");
			isConnectedToF = false;
			return false;
		}
	}
	
	private void closeConn() {
		//Gh.prnt("Client closeConn Start");
		if (outputStreamWriter != null) {
			try {
				// osw.flush();
				outputStreamWriter.close();
				isConnectedToF = false;
			} catch (IOException e1) {
				//Gh.prnte("closeConn, e: " + e1.getMessage());
			}
		} else {
			isConnectedToF = false;
		}
		
		if (socket != null) {
			try {
				socket.close();
				isConnectedToF = false;
			} catch (IOException e) {
				//Gh.prnte("Client closeConn, e: " + e.getMessage());
			}
		} else {
			isConnectedToF = false;
		}
		
		//Gh.prnt("Client closeConn End");
	}
	
	private void serverListener() {
		String receivedText = "";
		
		// Gh.prnt("Client serverListener looping");
		if (bufferedReader != null && socket != null) {
			try {
				// Gh.prnt("Client serverListener before readLine");
				receivedText = bufferedReader.readLine();
				if (receivedText != null) {
					// Gh.prnt("Client serverListener received text=" +
// receivedText);
					if ("ping".equals(receivedText)) {
						//Gh.prnt("Client serverListener received ping");
						lastPingReceivedTms = System.currentTimeMillis();
					} else if ("restart".equals(receivedText)) {
						reconnecter("received over TCP");
					} else {
						Gh.prnte("Client serverListener something wrong received, receivedText=" + receivedText);
					}
				}
			} catch (IOException e) {
				Gh.prnte("Client serverListener error on readLine, e=" + e.getMessage());
			}
			
		} else {
			// Gh.prnte("Client serverListener text receiver text=" +
// receivedText + " bufferedReader=null or socket=null");
		}
		
	}
	
}