package haven.purus.pbot.api;

import haven.Coord;
import haven.Coord2d;
import haven.Gob;
import haven.OCache;

public class PBotGob {

	private Gob gob;
	private PBotSession pBotSession;

	PBotGob(Gob gob, PBotSession pBotSession) {
		this.pBotSession = pBotSession;
		this.gob = gob;
	}

	/**
	 * Click the gob
	 * @param btn 1 = left, 2 = middle, 3 = right
	 * @param mod Key modifier mask 1 = shift 2 = ctrl 4 = alt
	 */
	public void doClick(int btn, int mod) {
		pBotSession.gui.map.wdgmsg("click", Coord.z, gob.rc.floor(OCache.posres), btn, mod, 0, (int)gob.id, gob.rc.floor(OCache.posres), 0, -1);
	}

	/**
	 * @param gob target
	 * @return Euclidean distance between this and target gob
	 */
	public double dist(PBotGob gob) {
		return gob.getCoords().dist(this.gob.rc);
	}

	/**
	 * Returns rc-coords of the gob
	 * @return Coord object with x and y attributes
	 */
	public Coord2d getCoords() {
		return this.gob.rc;
	}
}
