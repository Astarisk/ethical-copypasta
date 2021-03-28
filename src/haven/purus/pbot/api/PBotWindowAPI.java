package haven.purus.pbot.api;

import haven.Window;

import java.util.List;

public class PBotWindowAPI {

	private PBotSession pBotSession;

	PBotWindowAPI(PBotSession pBotSession) {
		this.pBotSession = pBotSession;
	}

	/**
	 * Wait for a window with a specific name to appear
	 * @param name Name of the window
	 * @param timeout Timeout in milliseconds to wait for the window to appear
	 * @return Returns the window or null if not found before timeout expires
	 */
	public PBotWindow waitForWindow(String name, long timeout) {
		int retries = 0;
		while(retries * 25L < timeout) {
			PBotWindow window = getWindow(name);
			if(window != null)
				return window;
			retries++;
			PBotUtils.sleep(25);
		}
		return null;
	}

	/**
	 * Wait for a window with a specific name to appear
	 * @param name Name of the window
	 * @return Returns the window
	 */
	public Window waitForWindow(String name) {
		while(true) {
			PBotWindow window = getWindow(name);
			PBotUtils.sleep(25);
		}
	}

	/**
	 * Wait for a window with a specific name to disappear
	 * @param timeout in milliseconds
	 * @param name Name of the window
	 * @return false if the window did not close before the timeout (it may or may not still close in the future)
	 */
	public boolean waitForWindowClose(String name, long timeout) {
		int retries = 0;
		while(getWindow(name) != null) {
			if(retries * 25L >= timeout) {
				return false;
			}
			retries++;
			PBotUtils.sleep(50);
		}
		return true;
	}

	/**
	 * Wait for a window with a specific name to disappear
	 * @param name Name of the window
	 */
	public void waitForWindowClose(String name) {
		while(getWindow(name) != null) {
			PBotUtils.sleep(50);
		}
	}

	/**
	 * Get a window with name
	 * @param name Name of the window
	 * @return The window or null if not found
	 */
	public PBotWindow getWindow(String name) {
		for(Window w : pBotSession.gui.children(Window.class)) {
			if(w.cap.text.equals(name))
				return new PBotWindow(w, pBotSession);
		}
		return null;
	}

}
