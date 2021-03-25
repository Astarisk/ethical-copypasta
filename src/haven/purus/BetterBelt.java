package haven.purus;

import haven.Coord;

public class BetterBelt extends BetterWindow {
	public BetterBelt(Coord sz, String cap, boolean lg, Coord tlo, Coord rbo) {
		super(sz, cap, lg, tlo, rbo);
	}

	@Override
	protected void added() {
		gameui().betterBelt = this;
		super.added();
	}

	@Override
	public void destroy() {
		gameui().betterBelt = null;
		super.destroy();
	}
}
