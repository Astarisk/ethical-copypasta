package haven.purus.pbot.api;

import haven.GameUI;

public class PBotSession {

	protected GameUI gui;
	private PBotGobAPI pBotGobAPI;
	private PBotUtils pBotUtils;

	public PBotSession(GameUI gui) {
		this.gui = gui;
		this.pBotUtils = new PBotUtils(this);
		this.pBotGobAPI = new PBotGobAPI(this);
	}

	public PBotGobAPI PBotGobAPI() {
		return this.pBotGobAPI;
	}

	public PBotUtils PBotUtils() {
		return this.pBotUtils;
	}

}
