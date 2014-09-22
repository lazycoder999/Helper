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
		final Thread connectionChecker = new Thread(new Runnable() {
			@Override
			public void run() {
				while (true) {
					try {
						Thread.sleep(1000);
					} catch (final InterruptedException e) {
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
						} catch (final SQLException e) {
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
		final String url = "jdbc:mysql://78.84.107.167:3366/";
		final String dbName = "skudra";
		final String driver = "com.mysql.jdbc.Driver";
		final String userName = "gunars1";
		final String password = "tests1";
		//mysql-connector-java-5.1.31-bin.jar
		try {
			Class.forName(driver).newInstance();
		} catch (final InstantiationException e) {
			e.printStackTrace();
		} catch (final IllegalAccessException e) {
			e.printStackTrace();
		} catch (final ClassNotFoundException e) {
			e.printStackTrace();
		}
		
		try {
			conn = DriverManager.getConnection(url + dbName, userName, password);
		} catch (final SQLException e) {
			e.printStackTrace();
		}
		
		Glog.prnt(conn.toString());
		
		try {
			st = conn.createStatement();
		} catch (final SQLException e) {
			Glog.prnte("connectDb");
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
	
	public void updateTradeField(final long tradeId, final String fieldName, final String fieldValue) {
		final Thread startInThread = new Thread(new Runnable() {
			@Override
			public void run() {
				
				try {
					final String statement = "UPDATE skudra.trades SET " + fieldName + "='" + fieldValue
							+ "', lastTableUpdate=NOW() WHERE tradeId = " + tradeId + ";";
					final int result = st.executeUpdate(statement);
					if (result == 0) {
						Glog.prnt(result + " " + statement);
					}
				} catch (final SQLException e) {
					Glog.prnte("updateTradeField");
					e.printStackTrace();
				}
				
			}
		});
		startInThread.setPriority(Thread.MIN_PRIORITY);
		startInThread.start();
	}
	
	public void insertNewTrade(final long tradeId) {
		
		Glog.prnt("nbr = " + tradeId);
		try {
			//st.execute("INSERT INTO skudra.trades(tradeId, tradeSide, requestedPrice, filledPrice) VALUES (4, 'buy', 1.12345, 1.12344)");
			final String statement = "INSERT INTO skudra.trades(tradeId, dateTime) VALUES (" + tradeId + ", NOW())";
			final int result = st.executeUpdate(statement);
			if (result == 0) {
				Glog.prnt(result + " " + statement);
			}
		} catch (final SQLException e) {
			Glog.prnte("insertNewTrade");
			e.printStackTrace();
		}
		
	}
	
	public void insertNewPerformance(final String whoWasIt, final double miliseconds) {
		final Thread startInThread = new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					//st.execute("INSERT INTO skudra.trades(tradeId, tradeSide, requestedPrice, filledPrice) VALUES (4, 'buy', 1.12345, 1.12344)");
					final String statement = "INSERT INTO skudra.performance(lastTableUpdate, whoWasIt, miliseconds) VALUES (NOW(), '" + whoWasIt
							+ "'," + miliseconds + ")";
					final int result = st.executeUpdate(statement);
					if (result == 0) {
						Glog.prnt(result + " " + statement);
					}
				} catch (final SQLException e) {
					Glog.prnte("insertNewPerformance");
					e.printStackTrace();
				}
				
			}
		});
		startInThread.setPriority(Thread.MIN_PRIORITY);
		startInThread.start();
	}
}
