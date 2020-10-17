package haven.purus.database;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

public class Config {
	public static void init() throws SQLException {
		Database.connection.prepareStatement("CREATE TABLE IF NOT EXISTS Config (" +
				"absolute_path TEXT," +
				"key TEXT," +
				"value TEXT)").execute();
		Database.connection.prepareStatement("CREATE UNIQUE INDEX IF NOT EXISTS path_key_idx ON Config (absolute_path, key)").execute();
	}

	public static void setKey(String key, String value, String absolutePath) {
		try {
			PreparedStatement ps = Database.connection.prepareStatement("INSERT INTO Config (absolute_path, key, value) VALUES(?,?,?) ON CONFLICT(key, absolute_path) DO UPDATE SET value=? WHERE absolute_path=? AND key=?");
			ps.setString(1, absolutePath);
			ps.setString(2, key);
			ps.setString(3, value);
			ps.setString(4, value);
			ps.setString(5, absolutePath);
			ps.setString(6, key);
			ps.execute();
		} catch(SQLException throwables) {
			throwables.printStackTrace();
		}
	}

	public static String getValue(String key, String absolutePath) {
		try {
			PreparedStatement ps = Database.connection.prepareStatement("SELECT value FROM Config WHERE absolute_path=? AND key=?");
			ps.setString(1, absolutePath);
			ps.setString(2, key);
			ResultSet rs = ps.executeQuery();
			if(rs.next())
				return rs.getString(1);
			else
				return null;
		} catch(SQLException throwables) {
			throwables.printStackTrace();
			return null;
		}
	}

	public static void removeEntry(String key, String absolutePath) {
		try {
		PreparedStatement ps = Database.connection.prepareStatement("DELETE FROM Config WHERE absolute_path=? AND key=?");
		ps.setString(1, absolutePath);
		ps.setString(2, key);
		ps.execute();
		} catch(SQLException throwables) {
			throwables.printStackTrace();
		}
	}

	public static String[] keys(String absolutePath) {
		try {
			PreparedStatement ps = Database.connection.prepareStatement("SELECT key FROM Config WHERE absolute_path=?");
			ps.setString(1, absolutePath);
			ResultSet rs = ps.executeQuery();
			ArrayList<String> l = new ArrayList<>();
			while(rs.next()) {
				l.add(rs.getString(1));
			}
			return (String[]) l.stream().toArray();
		} catch (SQLException throwables) {
			throwables.printStackTrace();
			return new String[0];
		}
	}

	public static void removeKeys(String absolutePath) {
		try {
			PreparedStatement ps = Database.connection.prepareStatement("DELETE FROM Config WHERE absolute_path=?");
			ps.setString(1, absolutePath);
			ps.execute();
		} catch(SQLException throwables) {
			throwables.printStackTrace();
		}
	}
}

