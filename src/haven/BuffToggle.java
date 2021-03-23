package haven;


public class BuffToggle extends Buff {
	public String name;
	private Resource res;

	public BuffToggle(String name, Resource res) {
		super(null);
		this.name = name;
		this.res = res;
	}

	@Override
	public void draw(GOut g) {
		g.image(frame, Coord.z);
		try {
			Tex img = res.layer(Resource.imgc).tex();
			g.image(img, imgoff);
		} catch (Loading e) {
		}
	}

	private Text shorttip;

	@Override
	public Object tooltip(Coord c, Widget prev) {
		try {
			if (shorttip == null)
				shorttip = Text.render(res.layer(Resource.tooltip).t);
			return (shorttip.tex());
		} catch (Loading e) {
			return ("...");
		}
	}

	@Override
	public boolean mousedown(Coord c, int btn) {
		return true;
	}
}
