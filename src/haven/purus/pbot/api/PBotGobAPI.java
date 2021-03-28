package haven.purus.pbot.api;

import haven.*;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class PBotGobAPI {
	PBotSession pBotSession;

	PBotGobAPI(PBotSession pBotSession) {
		this.pBotSession = pBotSession;
	}

	/**
	 * Get gobs matching the specific regex pattern
	 * @param resname regex pattern
	 * @return List of PBotGobs with resname matching the given regex pattern
	 */
	public List<PBotGob> getGobsByResname(String resname) {
		List<PBotGob> gobs = new ArrayList<>();
		Pattern pat = Pattern.compile(resname);
		synchronized(pBotSession.gui.ui.sess.glob.oc) {
			for(Gob gob : pBotSession.gui.ui.sess.glob.oc) {
				Resource res;
				while(true) {
					try {
						res = gob.getres();
						break;
					} catch(Loading l){PBotUtils.sleep(10);}
				}
					if(res != null && pat.matcher(res.name).matches())
						gobs.add(new PBotGob(gob, pBotSession));
			}
		}
		return gobs;
	}

	/**
	 * Get the gob player if it exists
	 * @return Player gob or null if not found
	 */
	public PBotGob getPlayer() {
		Gob gob = pBotSession.gui.map.player();
		if(gob == null)
			return null;
		else
			return new PBotGob(gob, pBotSession);
	}

	/**
	 * Get closest gob matching the specific regex pattern
	 * @param resname regex pattern
	 * @return Closest PBotGob from player with resname matching the given regex pattern
	 */
	public PBotGob getClosestGobByResname(String resname) {
		double bestDistance = Double.MAX_VALUE;
		PBotGob bestGob = null;
		Pattern pat = Pattern.compile(resname);
		synchronized(pBotSession.gui.ui.sess.glob.oc) {
			for(Gob gob : pBotSession.gui.ui.sess.glob.oc) {
				Resource res;
				while(true) {
					try {
						res = gob.getres();
						break;
					} catch(Loading l){PBotUtils.sleep(10);}
				}
				PBotGob pgob = new PBotGob(gob, pBotSession);
					if(res != null && pat.matcher(res.name).matches() && pgob.dist(getPlayer()) < bestDistance) {
						bestDistance = pgob.dist(getPlayer());
						bestGob = pgob;
					}
			}
		}
		return bestGob;
	}

	/**
	 * Returns gob with exactly the given coords or null if not found
	 * @param x x-coordinate of the gob
	 * @param y y-coordinate of the gob
	 * @return Gob with coordinates or null
	 */
	public PBotGob getGobWithCoords(double x, double y) {
		synchronized (pBotSession.gui.ui.sess.glob.oc) {
			for (Gob gob : pBotSession.gui.ui.sess.glob.oc) {
				if(Math.abs(gob.rc.x - x) < 0.001 && Math.abs(gob.rc.y - y) < 0.001)
					return new PBotGob(gob, pBotSession);
			}
		}
		return null;
	}

	/**
	 * Find object by ID
	 * @param id ID of the object to look for
	 * @return Object, or null if not found
	 */
	public PBotGob findGobById(long id) {
		Gob gob = pBotSession.gui.ui.sess.glob.oc.getgob(id);
		if(gob == null)
			return null;
		else
			return new PBotGob(pBotSession.gui.ui.sess.glob.oc.getgob(id), pBotSession);
	}

	/**
	 * Used to register gob callbacks see examples/gobname.py for usage
	 * @param cb callback
	 */
	public void selectGob(GobCallback cb) {
		pBotSession.PBotUtils().sysMsg("To select gob, alt + left click");
		synchronized(pBotSession.gui.map.gobCbQueue) {
			pBotSession.gui.map.gobCbQueue.add(new Pair<>(cb, pBotSession));
		}
	}

}
