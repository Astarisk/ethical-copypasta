package haven.res.ui.tt.alch.heal;/* Preprocessed source code */
import haven.*;
import java.awt.image.BufferedImage;

/* >tt: HealWound */
public class HealWound extends ItemInfo.Tip {
    public final Indir<Resource> res;
    public final int a;

    public HealWound(Owner owner, Indir<Resource> res, int a) {
	super(owner);
	this.res = res;
	this.a = a;
    }

	public static class Fac implements ItemInfo.InfoFactory {
		public Fac() {}
		public ItemInfo build(Owner owner, Raw raw, Object... args) {
			return mkinfo(owner, raw, args);
		}
	}

    public static ItemInfo mkinfo(Owner owner, Raw raw, Object... args) {
	Indir<Resource> res = owner.context(Resource.Resolver.class).getres((Integer)args[1]);
	int a = ((Number)args[2]).intValue();
	return(new HealWound(owner, res, a));
    }

    public BufferedImage tipimg() {
	BufferedImage t1 = Text.render(String.format("Heal %d points of ", this.a)).img;
	BufferedImage t2 = Text.render(res.get().layer(Resource.tooltip).t).img;
	int h = t1.getHeight();
	BufferedImage icon = PUtils.convolvedown(res.get().layer(Resource.imgc).img, new Coord(h, h), CharWnd.iconfilter);
	return(catimgsh(0, t1, icon, t2));
    }
}
