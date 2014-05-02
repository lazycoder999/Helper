package g.tcp.client;

import helper.pack.Gh;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.Socket;
import java.net.UnknownHostException;

public class Clientg {
	
	private Socket				clientSocket		= null;
	private OutputStreamWriter	osw;
	
	public String				ip					= "";
	public Integer				port				= 0;
	
	private boolean				isConnectedToF		= false;
	private boolean				reconnecterCalled	= false;
	
	public Clientg() {
		Runtime.getRuntime().addShutdownHook(new Thread() {
			public void run() {
				closeConn();
			}
		});
	}
	
	private void createConn(String ip, Integer port) {
		Gh.prnt("createConn Start");
		
		this.ip = ip;
		this.port = port;
		
		Gh.prnt("createConn on ip=" + ip + " port=" + port);
		
		boolean a = false, b = false, c = false;
		
		try {
			clientSocket = new Socket(ip, port);
			a = true;
			Gh.prnt("a = true;");
		} catch (UnknownHostException e) {
			Gh.prnte("2 " + e.getMessage());
			// e.printStackTrace();
		} catch (IOException e) {
			Gh.prnte("3 " + e.getMessage());
			// e.printStackTrace();
		}
		
		BufferedOutputStream bos = null;
		
		if (a == true) {
			try {
				bos = new BufferedOutputStream(clientSocket.getOutputStream());
				Gh.prnt("b = true;");
				b = true;
			} catch (IOException e) {
				Gh.prnte("5" + e.getMessage());
				// e.printStackTrace();
			}
		}
		
		if (b == true) {
			try {
				osw = new OutputStreamWriter(bos, "US-ASCII");
				Gh.prnt("c = true;");
				c = true;
			} catch (UnsupportedEncodingException e) {
				// e.printStackTrace();
				Gh.prnte("7" + e.getMessage());
			}
		}
		
		if (a == true && b == true && c == true) {
			Gh.prnt("createConn. setting isConnectedToF = true;");
			isConnectedToF = true;
		} else {
			Gh.prnte("createConn. setting isConnectedToF = false;");
			isConnectedToF = false;
		}
		Gh.prnt("createConn End");
	}
	
	public void sendMsg(String text) {
		if (isConnectedToF == true) {
			try {
				Gh.prnt("sendMsg text start=" + text);
				osw.write(text + '\n');
				osw.flush();
				Gh.prnt("[debug] sendMsg text end=" + text);
			} catch (IOException e) {
				Gh.prnte("sendMsg exception: " + e.getMessage());
				if (e.getMessage().contains("Connection reset by peer")) {
					Gh.prnt("detected connection reset");
					isConnectedToF = false;
					reconnecter("sendMsg 1");
				} else {
					Gh.prnte("error sendMsg unexpected error, e:" + e.getMessage());
					isConnectedToF = false;
					// e.printStackTrace();
					reconnecter("sendmsg 2");
				}
			}
		} else {
			Gh.prnte("sendMsg not sending because isConnectedTof == false");
			reconnecter("sendmsg 3");
		}
	}
	
	public Boolean isConnected() {
		
		if (osw == null) {
			Gh.prnte("TcpClient osw == null");
			isConnectedToF = false;
			return false;
		} else {
			try {
				osw.write("[sys]connection check message;0;0;0;0" + '\n');
				osw.flush();
				isConnectedToF = true;
				return true;
			} catch (Exception e) {
				Gh.prnte("TcpClient not connected.ip=" + clientSocket.getInetAddress() + " port=" + clientSocket.getPort());
				isConnectedToF = false;
				return false;
			}
		}
	}
	
	public void reconnecter(final String caller) {
		if (!reconnecterCalled) {
			reconnecterCalled = true;
			Gh.prnt("reconnecter caller=" + caller);
			Thread t1 = new Thread(new Runnable() {
				@Override
				public void run() {
					while (true) {
						reconnecterCalled = true;
						try {
							Thread.sleep(1000);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
						Gh.prnt("reconnecter looping caller=" + caller);
						closeConn();
						createConn(ip, port);
						reconnecterCalled = true;
						if (isConnected()) {
							Gh.prnt("breaking out of reconnecter, already connected to ip=" + clientSocket.getInetAddress() + " port="
									+ clientSocket.getPort() + "  set reconnecterCalled = false");
							reconnecterCalled = false;
							break;
						}
					}
				}
			});
			t1.start();
		} else {
			Gh.prnte("reconnecter cannot be called because reconnecterCalled=" + reconnecterCalled + "(already called)");
		}
	}
	
	private void closeConn() {
		Gh.prnt("closeConn Start");
		try {
			// osw.flush();
			if (osw != null) {
				osw.close();
			}
		} catch (IOException e1) {
			Gh.prnte("closeConn, e: " + e1.getMessage());
			// e1.printStackTrace();
		}
		
		try {
			if (clientSocket != null)
				clientSocket.close();
		} catch (IOException e) {
			Gh.prnte("closeConn, e: " + e.getMessage());
			// e.printStackTrace();
		}
		Gh.prnt("closeConn End");
	}
}