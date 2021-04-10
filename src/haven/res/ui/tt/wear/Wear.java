/* Preprocessed source code */
package haven.res.ui.tt.wear;

import haven.*;
import java.awt.image.BufferedImage;

/* >tt: Wear */
public class Wear extends ItemInfo.Tip {
	public final int d, m;

	public Wear(Owner owner, int d, int m) {
		super(owner);
		this.d = d;
		this.m = m;
		if(owner instanceof GItem) {
			((GItem) owner).wear = ((double)d) /m;
		}
	}

	public static ItemInfo mkinfo(Owner owner, Object... args) {
		return(new Wear(owner, (Integer)args[1], (Integer)args[2]));
	}

	public static class Fac implements ItemInfo.InfoFactory {
		public Fac() {}
		public ItemInfo build(Owner owner, Object... args) {
			return mkinfo(owner, args);
		}
	}

	public BufferedImage tipimg() {
		return(Text.render(String.format("Wear: %,d/%,d", d, m)).img);
	}
}