package haven.res.ui.tt.alch.elixir;/* Preprocessed source code */
import haven.*;
import java.util.*;
import java.awt.image.BufferedImage;

/* >tt: Elixir */
public class Elixir extends ItemInfo.Tip {
    public final int time;
    public final List<ItemInfo> effs;

    public Elixir(Owner owner, int time, List<ItemInfo> effs) {
	super(owner);
	this.time = time;
	this.effs = effs;
    }

	public static class Fac implements ItemInfo.InfoFactory {
		public Fac() {}
		public ItemInfo build(Owner owner, Object... args) {
			return mkinfo(owner, args);
		}
	}

    public static ItemInfo mkinfo(Owner owner, Object... args) {
	int time = (Integer)args[1];
	List<ItemInfo> effs = new ArrayList<>();
	for(Object raw : (Object[])args[2])
	    effs.add(ItemInfo.buildinfo(owner, new Object[] {(Object[])raw}).get(0));
	return(new Elixir(owner, time, effs));
    }

    static String[] units = {"s", "m", "h", "d"};
    static int[] div = {60, 60, 24};
    static String timefmt(int time) {
	int[] vals = new int[units.length];
	vals[0] = time;
	for(int i = 0; i < div.length; i++) {
	    vals[i + 1] = vals[i] / div[i];
	    vals[i] = vals[i] % div[i];
	}
	StringBuilder buf = new StringBuilder();
	for(int i = units.length - 1; i >= 0; i--) {
	    if(vals[i] > 0) {
		if(buf.length() > 0)
		    buf.append(' ');
		buf.append(vals[i]);
		buf.append(units[i]);
	    }
	}
	return(buf.toString());
    }

    private static final Text head = Text.render("Effects:");
    private static final Text none = RichText.render("$i{None}", -1);
    public void layout(Layout l) {
	l.cmp.add(head.img, new Coord(0, l.cmp.sz.y));
	if(effs.isEmpty()) {
	    l.cmp.add(none.img, new Coord(10, l.cmp.sz.y));
	} else {
	    for(ItemInfo eff : effs)
		l.cmp.add(ItemInfo.longtip(Collections.singletonList(eff)), new Coord(10, l.cmp.sz.y));
	}
	l.cmp.add(Text.render("Duration: " + timefmt(time)).img, new Coord(10, l.cmp.sz.y));
    }
}
