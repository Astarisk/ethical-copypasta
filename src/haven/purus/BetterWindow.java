package haven.purus;

import haven.Coord;
import haven.Window;

public class BetterWindow extends Window {
	public BetterWindow(Coord sz, String cap, boolean lg, Coord tlo, Coord rbo) {
		super(sz, cap, lg, tlo, rbo);
		setInBounds();
	}

	public BetterWindow(Coord sz, String cap) {
		super(sz, cap);
		setInBounds();
	}

	public BetterWindow(Coord sz, String cap, boolean lg) {
		super(sz, cap, lg);
		setInBounds();
	}

	@Override
	protected void added() {
		loadPosition();
		super.added();
	}

	public void savePosition() {
		Config.pref.putInt("window#" + this.cap.text + "x", this.c.x);
		Config.pref.putInt("window#" + this.cap.text + "y", this.c.y);
	}

	public void loadPosition() {
		this.c = new Coord(Config.pref.getInt("window#" + this.cap.text + "x", this.c.x), Config.pref.getInt("window#" + this.cap.text + "y", this.c.y));
	}

	public void setInBounds() {
		try {
			this.c = this.c.max(-this.sz.x/4*3, -this.sz.y/4*3).min(this.parent.sz.x - this.sz.x/4, this.parent.sz.y - this.sz.y/4);
			savePosition();
		} catch(NullPointerException e) {
			// Ignored
		}
	}

	@Override
	public boolean show(boolean show) {
		setInBounds();
		return super.show(show);
	}

	@Override
	public void show() {
		setInBounds();
		super.show();
	}

	@Override
	public void mousemove(Coord c) {
		if(dm != null) {
			this.c = this.c.add(c.add(doff.inv()));
			setInBounds();
		} else {
			super.mousemove(c);
		}
	}
}
