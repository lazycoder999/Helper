package mysql;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Mysql {
	
	private Connection			conn	= null;
	private Statement			st		= null;
	private static final Logger	LOG		= LogManager.getLogger(Mysql.class.getName());
	
	//private ResultSet	resultSet	= null;
	
	public void launchDbConnection() {
		final Thread connectionChecker = new Thread(new Runnable() {
			@Override
			public void run() {
				while (true) {
					try {
						Thread.sleep(1000);
					} catch (final InterruptedException e) {
						LOG.error(e);
					}
					
					if (conn != null) {
						try {
							if (conn.isClosed()) {
								LOG.info("conn.isClosed() == true");
								conn.close();
								((Mysql) conn).connectDb();
							} else {
								//LOG.info("conn.isClosed() = false");
							}
						} catch (final SQLException e) {
							LOG.error(e);
						}
					} else {
						LOG.error("conn == null");
						connectDb();
					}
				}
			}
		});
		connectionChecker.setPriority(Thread.MIN_PRIORITY);
		connectionChecker.setName("connectionChecker-" + System.nanoTime());
		connectionChecker.start();
	}
	
	private void closeAll() {
		
		if (conn != null) {
			try {
				conn.close();
			} catch (final SQLException e) {
				LOG.error(e);
			}
		}
		
		if (conn != null) {
			conn = null;
		}
	}
	
	private void connectDb() {
		LOG.info("Start connectDb");
		final String url = "jdbc:mysql://78.84.107.167:3366/";
		final String dbName = "skudra";
		final String driver = "com.mysql.jdbc.Driver";
		final String userName = "gunars1";
		final String password = "tests1";
		//mysql-connector-java-5.1.31-bin.jar
		
		try {
			Class.forName(driver).newInstance();
		} catch (final InstantiationException e) {
			LOG.error(e);
			//e.printStackTrace();
		} catch (final IllegalAccessException e) {
			LOG.error(e);
		} catch (final ClassNotFoundException e) {
			LOG.error(e);
		}
		
		try {
			conn = DriverManager.getConnection(url + dbName, userName, password);
		} catch (final SQLException e) {
			LOG.error(e);
		}
		
		if (conn != null) {
			LOG.info(conn.toString());
			
			try {
				st = conn.createStatement();
			} catch (final SQLException e) {
				LOG.error("connectDb", e);
			}
		} else {
			LOG.error("conn=null");
		}
		LOG.info("End connectDb");
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
		final Thread updateTradeFieldThr = new Thread(new Runnable() {
			@Override
			public void run() {
				
				try {
					final String statement = "UPDATE skudra.trades SET " + fieldName + "='" + fieldValue
							+ "', lastTableUpdate=NOW() WHERE tradeId = " + tradeId + ";";
					final int result = st.executeUpdate(statement);
					if (result == 0) {
						LOG.info(result + " " + statement);
					} else {
						//LOG.info("result=" + result);
					}
				} catch (final SQLException e) {
					LOG.error("updateTradeField", e);
					closeAll();
				}
				
			}
		});
		updateTradeFieldThr.setPriority(Thread.MIN_PRIORITY);
		updateTradeFieldThr.setName("updateTradeFieldThr-" + System.nanoTime());
		updateTradeFieldThr.start();
	}
	
	public void insertNewTrade(final long tradeId) {
		
		LOG.info("nbr = " + tradeId);
		try {
			//st.execute("INSERT INTO skudra.trades(tradeId, tradeSide, requestedPrice, filledPrice) VALUES (4, 'buy', 1.12345, 1.12344)");
			final String statement = "INSERT INTO skudra.trades(tradeId, dateTime) VALUES (" + tradeId + ", NOW())";
			final int result = st.executeUpdate(statement);
			if (result == 0) {
				LOG.info(result + " " + statement);
			}
		} catch (final SQLException e) {
			LOG.error("insertNewTrade", e);
			closeAll();
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
					LOG.info(statement);
					if (st != null) {
						final int result = st.executeUpdate(statement);
						if (result == 0) {
							LOG.info(result + " " + statement);
						}
					} else {
						LOG.error("st = null");
					}
					
				} catch (final SQLException e) {
					LOG.error("insertNewPerformance", e);
					closeAll();
				}
				
			}
		});
		startInThread.setPriority(Thread.MIN_PRIORITY);
		startInThread.setName("insertNewPerformance-thr");
		startInThread.start();
	}
}
