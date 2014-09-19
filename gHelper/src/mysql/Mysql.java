package mysql;

import helper.Glog;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class Mysql {
	
	private Connection	conn	= null;
	private Statement	st		= null;
	
	//private ResultSet	resultSet	= null;
	
	public void launchDbConnection() {
		Thread connectionChecker = new Thread(new Runnable() {
			public void run() {
				while (true) {
					try {
						Thread.sleep(1000);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					
					if (conn != null) {
//						try {
//							Glog.prnt("1 conn.isClosed()=" + conn.isClosed());
//						} catch (SQLException e) {
//							e.printStackTrace();
//						}
						try {
							if (conn.isClosed()) {
								Glog.prnt("conn.isClosed() == true");
								conn.close();
								((Mysql) conn).connectDb();
							}
						} catch (SQLException e) {
							e.printStackTrace();
						}
					} else {
						Glog.prnte("conn == null");
						connectDb();
					}
				}
			}
		});
		connectionChecker.setPriority(Thread.MIN_PRIORITY);
		connectionChecker.start();
	}
	
	private void connectDb() {
		Glog.prnt("Start connectDb");
		String url = "jdbc:mysql://78.84.107.167:3366/";
		String dbName = "skudra";
		String driver = "com.mysql.jdbc.Driver";
		String userName = "gunars1";
		String password = "tests1";
		//mysql-connector-java-5.1.31-bin.jar
		try {
			Class.forName(driver).newInstance();
		} catch (InstantiationException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		
		try {
			conn = DriverManager.getConnection(url + dbName, userName, password);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		Glog.prnt(conn.toString());
		
		try {
			st = conn.createStatement();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		Glog.prnt("End connectDb");
	}
	
/*	public String requestActorName(Integer actorId) {
		try {
			resultSet = st.executeQuery("SELECT first_name FROM sakila.actor WHERE actor_id = " + actorId);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		String msg = null;
		
		try {
			while (resultSet.next()) {
				//int id = resultSet.getInt("actor_id");
				msg = resultSet.getString("first_name");
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		return msg;
	}*/	
	
	public void updateTradeField(long tradeId, String fieldName, String fieldValue) {
		Thread startInThread = new Thread(new Runnable() {
			public void run() {
				
				try {
					String statement = "UPDATE skudra.trades SET " + fieldName + "='" + fieldValue + "', lastTableUpdate=NOW() WHERE tradeId = "
							+ tradeId + ";";
					int result = st.executeUpdate(statement);
					if (result == 0) {
						Glog.prnt(result + " " + statement);
					}
				} catch (SQLException e) {
					e.printStackTrace();
				}
				
			}
		});
		startInThread.setPriority(Thread.MIN_PRIORITY);
		startInThread.start();
	}
	
	public void insertNewTrade(long tradeId) {
		
		Glog.prnt("nbr = " + tradeId);
		try {
			//st.execute("INSERT INTO skudra.trades(tradeId, tradeSide, requestedPrice, filledPrice) VALUES (4, 'buy', 1.12345, 1.12344)");
			String statement = "INSERT INTO skudra.trades(tradeId, dateTime) VALUES (" + tradeId + ", NOW())";
			int result = st.executeUpdate(statement);
			if (result == 0) {
				Glog.prnt(result + " " + statement);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
	}
	
	public void insertNewPerformance(String whoWasIt, double miliseconds) {
		Thread startInThread = new Thread(new Runnable() {
			public void run() {
				try {
					//st.execute("INSERT INTO skudra.trades(tradeId, tradeSide, requestedPrice, filledPrice) VALUES (4, 'buy', 1.12345, 1.12344)");
					String statement = "INSERT INTO skudra.performance(lastTableUpdate, whoWasIt, miliseconds) VALUES (NOW(), '" + whoWasIt + "',"
							+ miliseconds + ")";
					int result = st.executeUpdate(statement);
					if (result == 0) {
						Glog.prnt(result + " " + statement);
					}
				} catch (SQLException e) {
					e.printStackTrace();
				}
				
			}
		});
		startInThread.setPriority(Thread.MIN_PRIORITY);
		startInThread.start();
	}
}
