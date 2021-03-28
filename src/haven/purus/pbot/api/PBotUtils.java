package haven.purus.pbot.api;

import haven.*;
import haven.purus.pathfinder.Pathfinder;

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

	/**
	 * Left click to somewhere with pathfinder
	 * @param x X-Coordinate
	 * @param y Y-Coordinate
	 */
	public void pfLeftClick(double x, double y) {
		Pathfinder.run(new Coord2d(x, y),null, 1, 0, -1,"", pBotSession.gui);
	}

	/**
	 * Waits for the hourglass timer when crafting or drinking for example
	 * Also waits until the hourglass has been seen to change at least once
	 * If hourglass does not appear within timeout, returns false, else true
	 * @param timeout Timeout in milliseconds
	 */
	public boolean waitForHourglass(int timeout) {
		double prog = pBotSession.gui.prog;
		int retries = 0;
		while(prog == pBotSession.gui.prog) {
			if(retries > timeout/5)
				return false;
			retries++;
			prog = pBotSession.gui.prog;
			sleep(5);
		}
		while (pBotSession.gui.prog >= 0) {
			sleep(25);
		}
		return true;
	}

	/**
	 * Waits for the hourglass timer when crafting or drinking for example
	 * Also waits until the hourglass has been seen to change at least once
	 */
	public void waitForHourglass() {
		double prog = pBotSession.gui.prog;
		while (prog == pBotSession.gui.prog) {
			prog = pBotSession.gui.prog;
			sleep(5);
		}
		while (pBotSession.gui.prog >= 0) {
			sleep(20);
		}
	}

	/**
	 * Returns value of hourglass, -1 = no hourglass, else the value between 0.0 and 1.0
	 * @return value of hourglass
	 */
	public double getHourglass() {
		return pBotSession.gui.prog;
	}

	/**
	 * Returns the players inventory
	 * @return Inventory of the player
	 */
	public PBotInventory playerInventory() {
		return new PBotInventory(pBotSession.gui.maininv, pBotSession);
	}

	/**
	 * Drops an item from the hand and waits until it has been dropped
	 * @param mod 1 = shift, 2 = ctrl, 4 = alt
	 */
	public void dropItemFromHand(int mod) {
		pBotSession.gui.map.wdgmsg("drop", Coord.z, pBotSession.gui.map.player().rc.floor(OCache.posres), mod);
		while(getItemAtHand() != null)
			sleep(25);
	}

	/**
	 * Resource name of the tile in the given location
	 * @param x X-Coord of the location (rc coord)
	 * @param y Y-Coord of the location (rc coord)
	 * @return
	 */
	public String tileResnameAt(int x, int y) {
		while(true) {
			try {
				Coord loc = new Coord(x, y);
				int t = pBotSession.gui.ui.sess.glob.map.gettile(loc.div(MCache.tilesz2));
				Resource res = pBotSession.gui.ui.sess.glob.map.tilesetr(t);
				if(res != null)
					return res.name;
				else
					return null;
			} catch(Loading l) { }
		}
	}


}
