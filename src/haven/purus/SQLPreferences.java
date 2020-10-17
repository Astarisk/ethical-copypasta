package haven.purus;

import haven.purus.database.Config;

import java.util.concurrent.ConcurrentHashMap;
import java.util.prefs.AbstractPreferences;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;
import java.util.prefs.PreferencesFactory;

public class SQLPreferences extends AbstractPreferences {

	public static class XMLPreferencesFactory implements PreferencesFactory {

		@Override
		public Preferences systemRoot() {
			return new SQLPreferences(null, "");
		}

		@Override
		public Preferences userRoot() {
			return new SQLPreferences(null, "");
		}
	}

	public static XMLPreferencesFactory factory = new XMLPreferencesFactory();

	private final ConcurrentHashMap<String, AbstractPreferences> childNodes = new ConcurrentHashMap<>();

	protected SQLPreferences(AbstractPreferences parent, String name) {
		super(parent, name);
	}

	@Override
	protected void putSpi(String s, String s1) {
		Config.setKey(s, s1, absolutePath());
	}

	@Override
	protected String getSpi(String s) {
		return Config.getValue(s, absolutePath());
	}

	@Override
	protected void removeSpi(String s) {
		Config.removeEntry(s, absolutePath());
	}

	@Override
	protected void removeNodeSpi() throws BackingStoreException {
		Config.removeKeys(absolutePath());
	}

	@Override
	protected String[] keysSpi() throws BackingStoreException {
		return Config.keys(absolutePath());
	}

	@Override
	protected String[] childrenNamesSpi() throws BackingStoreException {

		return childNodes.keySet().toArray(new String[0]);
	}

	@Override
	protected AbstractPreferences childSpi(String s) {
		AbstractPreferences pref = new SQLPreferences(this, s);
		AbstractPreferences prev = childNodes.putIfAbsent(s, pref);
		if(prev == null)
			return pref;
		else
			return prev;
	}

	@Override
	protected void syncSpi() throws BackingStoreException {
	}

	@Override
	protected void flushSpi() throws BackingStoreException {
	}
}
