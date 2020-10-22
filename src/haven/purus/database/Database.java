package haven.purus.database;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class Database {

	public static Connection connection;

	static {
		try {
			File dbFile = new File(System.getProperty("user.home"), ".haven");
			dbFile.mkdirs();
			dbFile = new File(dbFile, "pasta.db");

			connection = DriverManager.getConnection("jdbc:sqlite:" + dbFile.getAbsolutePath());
			Config.init();
			Credentials.init();
		} catch(SQLException throwables) {
			throwables.printStackTrace();
		}
	}
}
