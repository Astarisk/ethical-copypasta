package haven.purus.pbot.api;

import haven.*;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.rmi.Remote;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;

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

	public void closeSession() {
		this.gui.ui.sess.close();
	}

	// Create new session log in char etc. null if not successful
	public PBotSession newSession(String username, String password, String charname) throws InterruptedException, ExecutionException {
		FutureTask task = new FutureTask (() -> {
			AuthClient.Credentials creds = new AuthClient.NativeCred(username, password);
			byte[] cookie;
			String acctname;
			try(AuthClient auth = new AuthClient((Config.authserv == null) ? Config.defserv : Config.authserv, Config.authport)) {
				try {
					acctname = creds.tryauth(auth);
				} catch(AuthClient.Credentials.AuthException e) {
					System.out.println("Error while authenticating1 " + e.getMessage());
					return null;
				}
				cookie = auth.getcookie();
			} catch(UnknownHostException e) {
				System.out.println("Error while authenticating" + "Could not locate server");
				return null;
			} catch(IOException e) {
				System.out.println("Error while authenticating2 " + e.getMessage());
				return null;
			}
			Session sess = new Session(new InetSocketAddress(Config.defserv, Config.mainport), acctname, cookie);
			try {
				Thread.sleep(100);
			} catch(InterruptedException e) {
				e.printStackTrace();
			}
			while(true) {
				if(sess.state == "") {
					break;
				} else if(sess.connfailed != 0) {
					System.out.println("Connection failed " + sess.connerror);
					return null;
				}
				synchronized(sess) {
					try {
						sess.wait();
					} catch(InterruptedException e) {
						e.printStackTrace();
					}
				}
			}
			RemoteUI rui = new RemoteUI(sess);
			sess.charAutoSel = charname;
			MainFrame.mf.sessionCreate(rui);
			int retries = 0;
			while(retries < 10 * 30) {
				System.out.println(sess.ui);
				if(sess.ui != null)
					System.out.println(sess.ui.gui);
				if(sess.ui != null && sess.ui.gui != null) {
					//Thread.sleep(1000);
					return new PBotSession(sess.ui.gui);
				}
				try {
					Thread.sleep(100);
				} catch(InterruptedException e) {
					e.printStackTrace();
				}
				retries++;
			}
			return null;
		});
		new HackThread(task, "Session thread").start();
		return (PBotSession) task.get();
	}


}
