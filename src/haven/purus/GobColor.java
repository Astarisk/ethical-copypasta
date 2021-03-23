package haven.purus;

import haven.GAttrib;
import haven.Gob;
import haven.render.MixColor;
import haven.render.Pipe;

public class GobColor extends GAttrib implements Gob.SetupMod {
	private MixColor color;

	public GobColor(Gob g, MixColor color) {
		super(g);
		this.color = color;
	}

	public Pipe.Op placestate() {
		return color;
	}

}
