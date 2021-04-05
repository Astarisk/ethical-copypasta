package haven.purus;

import haven.*;
import haven.render.*;

import java.awt.*;
import java.util.HashMap;
import java.util.Objects;

public class GobText extends Sprite implements RenderTree.Node, PView.Render2D {
	private static final Text.Foundry font = new Text.Foundry(Text.mono.deriveFont(Font.BOLD, UI.scale(10))).aa(true);
	private static final HashMap<CachedTexKey, CachedTexVal> texts = new HashMap<>();

	private static class CachedTexKey {
		Color col;
		String text;
		CachedTexKey(String text, Color col) {
			this.col = col;
			this.text = text;
		}

		@Override
		public boolean equals(Object o) {
			if(this == o)
				return true;
			if(o == null || getClass() != o.getClass())
				return false;
			CachedTexKey that = (CachedTexKey) o;
			return Objects.equals(col, that.col) && Objects.equals(text, that.text);
		}

		@Override
		public int hashCode() {
			return Objects.hash(col, text);
		}
	}

	private static class CachedTexVal {
		Tex tex;
		int cnt = 1;
		CachedTexVal(Tex tex) {
			this.tex = tex;
		}
	}

	public final String text;
	private Tex tex;
	private int zOfs;
	private Color col;

	public GobText(Gob g, String text, Color col, int zOfs) {
		super(null, null);
		this.text = text;
		this.tex = font.renderstroked(text, col, Color.BLACK).tex();
		this.zOfs = zOfs;
		this.col = col;
		CachedTexVal ctv = texts.get(new CachedTexKey(text, col));
		if(ctv != null) {
			ctv.cnt++;
			this.tex = ctv.tex;
		} else {
			texts.put(new CachedTexKey(text, col), new CachedTexVal(this.tex));
		}
	}

	public void draw(GOut g, Pipe state) {
		Coord sc = Homo3D.obj2view(new Coord3f(0, 0, 6 + zOfs), state, Area.sized(g.sz())).round2();
		g.aimage(tex, sc, 0.5, 0.5);
	}

	@Override
	public void removed(RenderTree.Slot slot) {
		CachedTexVal ctv = texts.get(new CachedTexKey(text, col));
		if(ctv != null && --ctv.cnt == 0) {
			texts.remove(new CachedTexKey(text, col));
		}
	}
}
