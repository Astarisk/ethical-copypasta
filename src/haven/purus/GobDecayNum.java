package haven.purus;

import haven.*;
import haven.render.*;

import java.awt.*;

public class GobDecayNum extends GAttrib implements RenderTree.Node, PView.Render2D {
	private static final Text.Foundry font = new Text.Foundry(Text.mono.deriveFont(Font.BOLD, UI.scale(10))).aa(true);
	private static final Tex hlt0 = font.renderstroked("25%", new Color(255, 240, 220), Color.BLACK).tex();
	private static final Tex hlt1 = font.renderstroked("50%", new Color(255, 240, 220), Color.BLACK).tex();
	private static final Tex hlt2 = font.renderstroked("75%", new Color(255, 240, 220), Color.BLACK).tex();

	public int val;
	private Tex tex;

	public GobDecayNum(Gob g, int val) {
		super(g);
		update(val);
	}

	public void draw(GOut g, Pipe state) {
		if(tex != null) {
			Coord sc = Homo3D.obj2view(new Coord3f(0, 0, 6), state, Area.sized(g.sz())).round2();
			g.aimage(tex, sc, 0.5, 0.5);
		}
	}

	public void update(int val) {
		this.val = val;
		switch (val - 1) {
			case 0:
				tex = hlt0;
				break;
			case 1:
				tex = hlt1;
				break;
			case 2:
				tex = hlt2;
				break;
		}
	}
}
