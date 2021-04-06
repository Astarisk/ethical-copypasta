package haven.purus.pbot.api;

import haven.*;

import java.util.ArrayList;
import java.util.Comparator;
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
				if(gob.isPlayer())
					continue;
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
	public void selectGob(Callback cb) {
		pBotSession.PBotUtils().sysMsg("To select gob, alt + left click");
		synchronized(pBotSession.gui.map.gobCbQueue) {
			pBotSession.gui.map.gobCbQueue.add(new Pair<>(cb, pBotSession));
		}
	}

	// Sorts coordinate array to efficient zig-zag-like sequence for farming etc.
	private static class CoordSort implements Comparator<PBotGob> {
		public int compare(PBotGob a, PBotGob b) {
			if (a.getRcCoords().floor().x == b.getRcCoords().floor().x) {
				if (a.getRcCoords().floor().x % 2 == 0)
					return Integer.compare(b.getRcCoords().floor().y, a.getRcCoords().floor().y);
				else
					return Integer.compare(a.getRcCoords().floor().y, b.getRcCoords().floor().y);
			} else
				return Integer.compare(a.getRcCoords().floor().x, b.getRcCoords().floor().x);
		}
	}


	/**
	 * Returns a list of gobs in the rectangle between A and B points
	 * @param a A-point of the rectangle
	 * @param b B-point of the rectangle
	 * @return List of gobs in the area, sorted to zig-zag pattern
	 */
	public ArrayList<PBotGob> gobsInArea(Coord a, Coord b) {
		// Initializes list of crops to harvest between the selected coordinates
		ArrayList<PBotGob> gobs = new ArrayList<PBotGob>();
		double bigX = Math.max(a.x, b.x);
		double smallX = Math.min(a.x, b.x);
		double bigY = Math.max(a.y, b.y);
		double smallY = Math.min(a.y, b.y);
		synchronized(pBotSession.gui.ui.sess.glob.oc) {
			for(Gob gob : pBotSession.gui.ui.sess.glob.oc) {
				if(gob.rc.x <= bigX && gob.rc.x >= smallX && gob.getres() != null && gob.rc.y <= bigY
						&& gob.rc.y >= smallY) {
					gobs.add(new PBotGob(gob, pBotSession));
				}
			}
		}
		gobs.sort(new CoordSort());
		return gobs;
	}


}
