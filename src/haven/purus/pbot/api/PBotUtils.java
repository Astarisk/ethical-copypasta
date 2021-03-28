package haven.purus.pbot.api;

import haven.*;

public class PBotUtils {
	private PBotSession pBotSession;

	PBotUtils(PBotSession pBotSession) {
		this.pBotSession = pBotSession;
	}

	/**
	 * Send message to be displayed in the client
	 * @param msg Message content
	 */
	public void sysMsg(String msg) {
		pBotSession.gui.msg(msg);
	}

	/**
	 * Waits until the flowermenu appears with timeout
	 * @param timeout Timeout to wait before returning null if menu is not opened
	 * @return Flowermenu that was opened
	 */
	public PBotFlowerMenu getFlowermenu(long timeout) {
		long start = System.currentTimeMillis();
		try {
			while(System.currentTimeMillis() - start < timeout) {
				FlowerMenu menu = pBotSession.gui.ui.root.getchild(FlowerMenu.class);
				if(menu != null)
					return new PBotFlowerMenu(menu);
				Thread.sleep(30);
			}
		} catch(InterruptedException ie) {}
		return null;
	}

	/**
	 * Waits until the flowermenu appears
	 * @return Flowermenu that was opened
	 */
	public PBotFlowerMenu getFlowermenu() {
		return getFlowermenu(Long.MAX_VALUE);
	}

	/**
	 * Itemact with item in hand, for example, to make a stockpile
	 */
	public void makePile() {
		pBotSession.gui.map.wdgmsg("itemact", Coord.z, Coord.z, 0);
	}

	/**
	 * Use to place something, for example, a stockpile
	 * @param x x place stockpile to
	 * @param y y place stockpile to
	 */
	public void placeThing(double x, double y) {
		pBotSession.gui.map.wdgmsg("place", new Coord2d(x, y).floor(OCache.posres), 0, 1, 0);
	}

	/**
	 * Use to cancel stockpile placing for example
	 */
	public void cancelPlace() {
		pBotSession.gui.map.wdgmsg("place", pBotSession.gui.map.player().rc.floor(OCache.posres), 0, 3, 0);
	}

	/**
	 * Wait for the given time
	 * @param time time in milliseconds
	 */
	public static void sleep(int time) {
		try {
			Thread.sleep(time);
		} catch(InterruptedException e) {
			e.printStackTrace();
		}
	}


	/**
	 * Returns the item currently in the hand
	 * @return Item at hand
	 */
	public PBotItem getItemAtHand() {
		if(pBotSession.gui.vhand == null)
			return null;
		else
			return new PBotItem(pBotSession.gui.vhand.item, pBotSession);
	}


}
