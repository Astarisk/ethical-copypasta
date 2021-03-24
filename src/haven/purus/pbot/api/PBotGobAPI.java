package haven.purus.pbot.api;

import haven.Gob;
import haven.Loading;
import haven.Resource;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class PBotGobAPI {
	PBotSession pBotSession;

	PBotGobAPI(PBotSession pBotSession) {
		this.pBotSession = pBotSession;
	}

	/**
	 * @param resname regex pattern
	 * @return List of PBotGobs with resname matching the given regex pattern
	 */
	public List<PBotGob> getGobsByResname(String resname) {
		List<PBotGob> gobs = new ArrayList<>();
		Pattern pat = Pattern.compile(resname);
		synchronized(pBotSession.gui.ui.sess.glob.oc) {
			for(Gob gob : pBotSession.gui.ui.sess.glob.oc) {
				try {
					Resource res = gob.getres();
					if(res != null && pat.matcher(res.name).matches())
						gobs.add(new PBotGob(gob, pBotSession));
				} catch(Loading l){}
			}
		}
		return gobs;
	}

	public PBotGob getPlayer() {
		return new PBotGob(pBotSession.gui.map.player(), pBotSession);
	}

	/**
	 * @param resname regex pattern
	 * @return Closest PBotGob from player with resname matching the given regex pattern
	 */
	public PBotGob getClosestGobByResname(String resname) {
		double bestDistance = Double.MAX_VALUE;
		PBotGob bestGob = null;
		Pattern pat = Pattern.compile(resname);
		synchronized(pBotSession.gui.ui.sess.glob.oc) {
			for(Gob gob : pBotSession.gui.ui.sess.glob.oc) {
				try {
					Resource res = gob.getres();
					PBotGob pgob = new PBotGob(gob, pBotSession);
					if(res != null && pat.matcher(res.name).matches() && pgob.dist(getPlayer()) < bestDistance) {
						bestDistance = pgob.dist(getPlayer());
						bestGob = pgob;
					}
				} catch(Loading l){}
			}
		}
		return bestGob;
	}
}
