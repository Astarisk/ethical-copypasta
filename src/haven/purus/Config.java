package haven.purus;

import java.util.prefs.Preferences;

public class Config {
	public static Preferences pref = SQLPreferences.factory.userRoot().node("puruspasta");

	// Name of the variable and preference key should always be the same
	public static boolean toggleTracking = pref.getBoolean("toggleTracking", true);
	public static boolean toggleCriminalacts = pref.getBoolean("toggleCriminalacts", true);
	public static boolean toggleSiege = pref.getBoolean("toggleSiege", true);
	public static boolean hwcursor = pref.getBoolean("hwcursor", false);
	public static boolean debugRescode = pref.getBoolean("debugRescode", false);
}
