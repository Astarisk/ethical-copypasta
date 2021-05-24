package haven.res.ui.tt.craftprep;
import java.awt.Color;
import haven.GItem;
import haven.ItemInfo;

public class CraftPrep extends ItemInfo implements GItem.ColorInfo
{
	public static final Color mycol;
	public static final Color notmycol;
	public final boolean mine;

	public CraftPrep(final ItemInfo.Owner owner, final boolean mine) {
		super(owner);
		this.mine = mine;
	}

	public static class Fac implements ItemInfo.InfoFactory {
		public Fac() {}
		public ItemInfo build(Owner owner, Object... args) {
			return mkinfo(owner, args);
		}
	}

	public static ItemInfo mkinfo(final ItemInfo.Owner owner, final Object... array) {
		boolean b = true;
		if (array.length > 1) {
			b = ((int)array[1] != 0);
		}
		return new CraftPrep(owner, b);
	}

	public Color olcol() {
		return this.mine ? CraftPrep.mycol : CraftPrep.notmycol;
	}

	static {
		mycol = new Color(0, 255, 0, 64);
		notmycol = new Color(255, 128, 0, 64);
	}
}