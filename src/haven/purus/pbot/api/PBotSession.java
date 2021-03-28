package haven.purus.pbot.api;

import haven.GameUI;

public class PBotSession {

	protected GameUI gui;
	private PBotGobAPI pBotGobAPI;
	private PBotUtils pBotUtils;
	private PBotCharacterAPI pBotCharacterAPI;
	private PBotWindowAPI pBotWindowAPI;

	public PBotSession(GameUI gui) {
		this.gui = gui;
		this.pBotUtils = new PBotUtils(this);
		this.pBotGobAPI = new PBotGobAPI(this);
		this.pBotCharacterAPI = new PBotCharacterAPI(this);
		this.pBotWindowAPI = new PBotWindowAPI(this);
	}

	public PBotGobAPI PBotGobAPI() {
		return this.pBotGobAPI;
	}

	public PBotUtils PBotUtils() {
		return this.pBotUtils;
	}

	public PBotCharacterAPI PBotCharacterAPI() {
		return this.pBotCharacterAPI;
	}

	public PBotWindowAPI PBotWindowAPI() {
		return this.pBotWindowAPI;
	}


}
