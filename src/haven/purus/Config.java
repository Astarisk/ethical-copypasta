package haven.purus;

import haven.KeyBinding;
import haven.KeyMatch;
import haven.purus.timer.TimerWnd;

import java.awt.event.KeyEvent;
import java.io.*;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;
import java.util.prefs.Preferences;

public class Config {
	public static final boolean iswindows = System.getProperty("os.name").startsWith("Windows");
	public static Preferences pref = SQLPreferences.factory.userRoot().node("puruspasta");

	public static class Setting<T> {
		public T val;
		public String name;

		public Setting(String name, T defaultVal) {
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
			} else if(val instanceof Serializable) {
				try {
					byte[] arr = pref.getByteArray(name, null);
					if(arr == null)
						return;
					ByteArrayInputStream bis = new ByteArrayInputStream(arr);
					ObjectInputStream ois = new ObjectInputStream(bis);
					this.val = (T) val.getClass().cast(ois.readObject());
				} catch(IOException | ClassNotFoundException e) {
					e.printStackTrace();
				}
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
			} else if(val instanceof Serializable) {
				try {
					ByteArrayOutputStream bos = new ByteArrayOutputStream();
					ObjectOutputStream oos = new ObjectOutputStream(bos);
					oos.writeObject(val);
					pref.putByteArray(name, bos.toByteArray());
				} catch(IOException e) {
					e.printStackTrace();
				}
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
	public static Setting<Boolean> debugWdgmsg = new Setting<>("debugWdgmsg", false);
	public static Setting<Boolean> displayQuality = new Setting<>("displayQuality", true);
	public static Setting<Float> cameraScrollSensitivity = new Setting<>("cameraScrollSensitivity", 1.0f);
	public static Setting<Boolean> tileGrid = new Setting<>("tileGrid", false);
	public static Setting<Boolean> showGobDecayNum = new Setting<>("showGobDecayNum", true);
	public static Setting<Boolean> growthStages = new Setting<>("growthStages", false);
	public static Setting<Integer> speedOnLogin = new Setting<>("speedOnLogin", 2);
	public static Setting<Float> flowermenuSpeed = new Setting<>("flowermenuSpeed", 0.25f);
	public static Setting<String> mapperToken = new Setting<>("mapperToken", "");
	public static Setting<Integer> bbDisplayState = new Setting<>("bbDisplayState", 0);
	public static Setting<Boolean> ttfHighlight = new Setting<>("ttfHighlight", true);
	public static Setting<Boolean> invShowLogin = new Setting<>("invShowLogin", true);
	public static Setting<Boolean> beltShowLogin = new Setting<>("beltShowLogin", true);
	public static Setting<Boolean> studyLock = new Setting<>("studyLock", false);
	public static Setting<Boolean> showmapgrid = new Setting<>("showmapgrid", false);
	public static Setting<Boolean> toggleDlight = new Setting<>("toggleDlight", false);
	public static Setting<Boolean> flavorObjsVisual = new Setting<>("flavorObjsVisual", false);
	public static Setting<Boolean> flavorObjsAudial = new Setting<>("flavorObjsAudial", false);
	public static Setting<Boolean> animalRadiuses = new Setting<>("animalRadiuses", false);
	public static Setting<Boolean> playerRadiuses = new Setting<>("playerRadiuses", false);
	public static Setting<Boolean> disableJorbfont = new Setting<>("disableJorbfont", false);
	public static Setting<Float> fontScale = new Setting<>("fontScale", 1.0f);
	public static Setting<Boolean> disableMultichatNotification = new Setting<>("disableMultichatNotification", false);
	public static Setting<Boolean> disableSessWnd = new Setting<>("disableSessWnd", false);
	public static Setting<Float> cupboardHeight = new Setting<>("cupboardHeight", 1.00f);


	public static Setting<Boolean> hideTrees = new Setting<>("hideTrees", true);
	public static Setting<Boolean> hideBushes = new Setting<>("hideBushes", true);
	public static Setting<Boolean> hideHouses = new Setting<>("hideHouses", false);
	public static Setting<Boolean> hideWalls = new Setting<>("hideWalls", true);
	public static Setting<Boolean> hideCrops = new Setting<>("hideCrops", true);
	public static Setting<Integer> hideRed = new Setting<>("hideRed", 65);
	public static Setting<Integer> hideGreen = new Setting<>("hideGreen", 180);
	public static Setting<Integer> hideBlue = new Setting<>("hideBlue", 255);
	public static Setting<Integer> hideAlpha = new Setting<>("hideAlpha", 200);
	public static Setting<Boolean> hideToggle = new Setting<>("hideToggle", false);

	public static Setting<Boolean> pathfinder = new Setting<>("pathfinder", false);

	public static Setting<ConcurrentHashMap<String, Boolean>> autodropItems = new Setting<>("autodropItems", new ConcurrentHashMap<>());
	public static Setting<ConcurrentHashMap<String, Boolean>> flowerOptOpens = new Setting<>("flowerOptOpens", new ConcurrentHashMap<>());
	public static Setting<ConcurrentHashMap<String, Boolean>> animalRads = new Setting<>("animalRads", new ConcurrentHashMap<>());
	public static Setting<ConcurrentHashMap<String, Float>> customVolumes = new Setting<>("customVolumes", new ConcurrentHashMap<>());
	public static Setting<ArrayList<TimerWnd.Timer>> timersSet = new Setting<>("timersSet", new ArrayList<>());

	public static Setting<Boolean> proximityPlayerAggro = new Setting<>("proximityPlayerAggro", false);
	public static Setting<Boolean> proximityKritterAggro = new Setting<>("proximityKritterAggro", true);

	public static Setting<Boolean> reverseBadCamX = new Setting<>("reverseBadCamX", false);
	public static Setting<Boolean> reverseBadCamY = new Setting<>("reverseBadCamY", false);

	public static Setting<Boolean> resinfo = new Setting<>("resinfo", false);

	public static Setting<ArrayList<String>> scriptsKeybinded = new Setting<>("scriptsKeybinded", new ArrayList<>());

	public static KeyBinding kb_growthStages = KeyBinding.get("kb_growthStages", KeyMatch.forcode(KeyEvent.VK_P, KeyMatch.C));
	public static KeyBinding kb_resinfo = KeyBinding.get("kb_resinfo", KeyMatch.forcode(KeyEvent.VK_I, KeyMatch.S));

	public static KeyBinding kb_bbtoggle = KeyBinding.get("kb_bbtoggle", KeyMatch.forcode(KeyEvent.VK_B, KeyMatch.S));
	public static KeyBinding kb_pathfinder = KeyBinding.get("kb_pathfinder", KeyMatch.forcode(KeyEvent.VK_S, KeyMatch.C));
	public static KeyBinding kb_hidetoggle = KeyBinding.get("kb_hidetoggle", KeyMatch.forcode(KeyEvent.VK_H, KeyMatch.C));
	public static KeyBinding kb_camswitch = KeyBinding.get("kb_camswitch", KeyMatch.forcode(KeyEvent.VK_C, KeyMatch.S));
	public static KeyBinding kb_dlighttoggle = KeyBinding.get("kb_dlighttoggle", KeyMatch.forcode(KeyEvent.VK_N, KeyMatch.C));
	public static KeyBinding kb_animalradius = KeyBinding.get("kb_animalradius", KeyMatch.forcode(KeyEvent.VK_D, KeyMatch.S));

	public static Setting<Integer> camCycle = new Setting<>("camCycle", 0);
	public static String[][] camCycles = new String[][]{{"cam", "bad"}, {"cam", "worse"}, {"cam", "follow"}, {"cam", "ortho"}, {"cam", "ortho", "-f"}};
}
