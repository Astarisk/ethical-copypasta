package haven.purus.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class Database {

	public static Connection connection;

	static {
		try {
			connection = DriverManager.getConnection("jdbc:sqlite:pasta.db");
			Config.init();
			Credentials.init();
		} catch(SQLException throwables) {
			throwables.printStackTrace();
		}
	}
}
