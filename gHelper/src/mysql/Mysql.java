package mysql;

import helper.Gh;
import helper.Glog;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class Mysql {
	
	public static void main(String[] args) {
		Glog.runPrintLogToConsole();
		Gh gh = new Gh();
		Glog.prnt("start");
		Mysql dao = new Mysql();
		dao.connectDb();
		//dao.updateActorName(7);
		//System.out.println("actor name=" + dao.requestActorName(7));
		
//		Runnable r1 = new Runnable() {
//			public void run() {
//				for (int i = 0; i < 100; i++) {
//					dao.inertNewTrade();
//				}
//				Glog.prnt("end inserting");
//			}
//		};
//		
//		Thread thr1 = new Thread(r1);
//		thr1.start();
//		Glog.prnt("end");
		
		dao.inertNewTrade(gh.getUniq());
		dao.updateTradeField("2871821404408764774", "tradeSide", "buy");
		
	}
	
	private Connection			conn				= null;
	private Statement			st					= null;
	private PreparedStatement	preparedStatement	= null;
	private ResultSet			resultSet			= null;
	
	public void connectDb() {
		
		String url = "jdbc:mysql://78.84.107.167:3366/";
		String dbName = "skudra";
		String driver = "com.mysql.jdbc.Driver";
		String userName = "gunars1";
		String password = "tests1";
		
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
		
	}
	
	public String requestActorName(Integer actorId) {
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
	}
	
	public void updateTradeField(String tradeId, String fieldName, String fieldValue) {
		try {
			st.executeUpdate("UPDATE skudra.trades SET " + fieldName + "='" + fieldValue + "', lastTableUpdate=NOW() WHERE tradeId = " + tradeId
					+ ";");
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	public void inertNewTrade(String tradeId) {
		Glog.prnt("nbr = " + tradeId);
		try {
			//st.execute("INSERT INTO skudra.trades(tradeId, tradeSide, requestedPrice, filledPrice) VALUES (4, 'buy', 1.12345, 1.12344)");
			st.execute("INSERT INTO skudra.trades(tradeId, dateTime) VALUES ( " + tradeId + ", NOW())");
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
}
