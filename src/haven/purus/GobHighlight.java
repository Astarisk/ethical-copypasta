package haven.purus;

import haven.GAttrib;
import haven.Gob;
import haven.render.*;

public class GobHighlight extends GAttrib implements Gob.SetupMod {
	private static final MixColor[] colors = new MixColor[64];
	private final long startTime;
	private boolean over = false;

	static {
		for(int i=0; i<32; i++) {
			colors[i] = new MixColor(70, 0, 100, i*8);
		}
		for(int i=32; i<64; i++) {
			colors[i] = colors[63-i];
		}
	}

	public GobHighlight(Gob g) {
		super(g);
		this.startTime = System.currentTimeMillis();
	}

	public Pipe.Op gobstate() {
		if(over)
			return null;
		long diff = System.currentTimeMillis()-startTime;
		if(diff > 5000) {
			over = true;
			return null;
		}
		int cnt = (int)Math.floor((diff%1000)/15.625);
		return colors[cnt];
	}
}
