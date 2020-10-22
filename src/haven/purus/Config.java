package haven.purus;

import java.util.prefs.Preferences;

public class Config {
	public static Preferences pref = SQLPreferences.factory.userRoot().node("puruspasta");

	public static class Setting<T> {
		public T val;
		public String name;

		Setting(String name, T defaultVal) {
			this.name = name;
			this.val = defaultVal;
			getVal();
		}

		private void getVal() {
			if(val instanceof Boolean) {
				this.val = (T) val.getClass().cast(pref.getBoolean(name, (Boolean) val));
			} else if(val instanceof String) {
				this.val = (T) val.getClass().cast(pref.get(name, (String) val));
			} else if(val instanceof Integer) {
				this.val = (T) val.getClass().cast(pref.getInt(name, (Integer) val));
			} else if(val instanceof Float) {
				this.val = (T) val.getClass().cast(pref.getFloat(name, (Float) val));
			} else {
				throw(new RuntimeException("Cannot get unknown type " + val.getClass() + " to config!"));
			}
		}

		public void setVal(T val) {
			this.val = val;
			if(val instanceof Boolean) {
				pref.putBoolean(name, (Boolean) val);
			} else if(val instanceof String) {
				pref.put(name, (String) val);
			} else if(val instanceof Integer) {
				pref.putInt(name, (Integer) val);
			} else if(val instanceof Float) {
				pref.putFloat(name, (Float) val);
			} else {
				throw(new RuntimeException("Cannot set unknown type " + val.getClass() + " to config!"));
			}
			this.val = val;
		}
	}

	// Name of the variable and preference key should always be the same
	public static Setting<Boolean> toggleTracking = new Setting<>("toggleTracking", true);
	public static Setting<Boolean> toggleCriminalacts = new Setting<>("toggleCriminalacts", false);
	public static Setting<Boolean> toggleSiege = new Setting<>("toggleSiege", true);
	public static Setting<Boolean> hwcursor = new Setting<>("hwcursor", true);
	public static Setting<Boolean> debugRescode = new Setting<>("debugRescode", false);
	public static Setting<Boolean> displayQuality = new Setting<>("displayQuality", true);
	public static Setting<Float> cameraScrollSensitivity = new Setting<>("cameraScrollSensitivity", 1.0f);
}
