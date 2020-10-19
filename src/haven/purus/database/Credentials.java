package haven.purus.database;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Credentials {
	public static void init() throws SQLException {
		Database.connection.prepareStatement("CREATE TABLE IF NOT EXISTS Credentials (" +
				"username TEXT UNIQUE," +
				"password TEXT)").execute();
	}

	public static void setCredential(String username, String password) {
		try {
			PreparedStatement ps = Database.connection.prepareStatement("INSERT INTO Credentials (username, password) VALUES(?,?) ON CONFLICT(username) DO UPDATE SET password=? WHERE username=?");
			ps.setString(1, username);
			ps.setString(2, password);
			ps.setString(3, password);
			ps.setString(4, username);
			ps.execute();
		} catch(SQLException throwables) {
			throwables.printStackTrace();
		}
	}

	public static void removeCredential(String username) {
		try {
			PreparedStatement ps = Database.connection.prepareStatement("DELETE FROM Credentials WHERE username=?");
			ps.setString(1, username);
			ps.execute();
		} catch(SQLException throwables) {
			throwables.printStackTrace();
		}
	}

	public static List<String> getUsernames() {
		try {
			ArrayList<String> ret = new ArrayList<>();
			PreparedStatement ps = Database.connection.prepareStatement("SELECT username FROM Credentials ORDER BY ROWID");
			ResultSet rs = ps.executeQuery();
			while(rs.next()) {
				ret.add(rs.getString(1));
			}
			return ret;
		} catch(SQLException throwables) {
			throwables.printStackTrace();
		}
		return Collections.emptyList();
	}

	public static String getPassword(String username) {
		try {
			PreparedStatement ps = Database.connection.prepareStatement("SELECT password FROM Credentials WHERE username=?");
			ps.setString(1, username);
			ResultSet rs = ps.executeQuery();
			if(rs.next())
				return rs.getString(1);
			else
				return "";
		} catch(SQLException throwables) {
			throwables.printStackTrace();
		}
		return "";
	}
}
