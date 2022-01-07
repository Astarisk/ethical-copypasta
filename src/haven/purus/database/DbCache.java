package haven.purus.database;

import haven.ResCache;

import java.io.*;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class DbCache implements ResCache {
	static {
		try {
			Database.connection.prepareStatement("CREATE TABLE IF NOT EXISTS ResCache (" +
					"name TEXT UNIQUE," +
					"value BLOB)").execute();
			Database.connection.prepareStatement("CREATE UNIQUE INDEX IF NOT EXISTS name_idx ON ResCache (name)").execute();
		} catch(SQLException e) {
			e.printStackTrace();
		}
	}

	@Override
	public OutputStream store(String name) throws IOException {
		return new ByteArrayOutputStream() {
			@Override
			public void close() throws IOException {
				try {
					PreparedStatement ps = Database.connection.prepareStatement("INSERT INTO ResCache(name, value) VALUES(?,?) ON CONFLICT(name) DO UPDATE SET value=? WHERE name=?");
					ps.setString(1, name);
					ps.setBinaryStream(2, new ByteArrayInputStream(buf), count);
					ps.setBinaryStream(3, new ByteArrayInputStream(buf), count);
					ps.setString(4, name);
					ps.execute();
				} catch(SQLException e) {
					throw new RuntimeException(e);
				}
				super.close();
			}
		};
	}

	@Override
	public InputStream fetch(String name) throws IOException {
		try {
			PreparedStatement ps = Database.connection.prepareStatement("SELECT value FROM ResCache WHERE name=?");
			ps.setString(1, name);
			ResultSet rs = ps.executeQuery();
			if(rs.next())
				return rs.getBinaryStream(1);
			else
				throw(new FileNotFoundException(name));
		} catch(SQLException e) {
			throw new RuntimeException(e);
		}
	}
}
