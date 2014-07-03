package mysql;

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
		Glog.prnt("start");
		Mysql dao = new Mysql();
		dao.connectDb();
		//dao.updateActorName(7);
		//System.out.println("actor name=" + dao.requestActorName(7));

		Runnable r1 = new Runnable() {
			public void run() {
				for (int i = 0; i < 100; i++) {
					dao.insertInto();
				}
				Glog.prnt("end inserting");
			}
		};

		Thread thr1 = new Thread(r1);
		thr1.start();
		Glog.prnt("end");

	}

	private Connection conn = null;
	private Statement st = null;
	private PreparedStatement preparedStatement = null;
	private ResultSet resultSet = null;

	public void connectDb() {

		String url = "jdbc:mysql://localhost:3306/";
		String dbName = "sakila";
		String driver = "com.mysql.jdbc.Driver";
		String userName = "user1";
		String password = "qaz123";

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

	public void updateActorName(Integer actorId) {
		try {
			st.executeUpdate("UPDATE sakila.actor SET first_name='janis7' WHERE actor_id = " + actorId + ";");
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public void insertInto() {
		try {
			st.execute("INSERT INTO sakila.actor(first_name, last_name) VALUES ('namaaae', 'ddddd')");
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

}
