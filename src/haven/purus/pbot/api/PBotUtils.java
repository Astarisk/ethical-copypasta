package haven.purus.pbot.api;

import haven.FlowerMenu;

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
	 * Waits until the flowermenu appears
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

}
