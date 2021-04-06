package haven.purus;

import haven.*;

import java.util.ArrayList;
import java.util.Random;

import static haven.Inventory.invsq;

public class RecentCrafts extends Widget {

	private static final OwnerContext.ClassResolver<CraftSlot> ctx = new OwnerContext.ClassResolver<CraftSlot>()
			.add(Glob.class, slot -> slot.wdg().ui.sess.glob)
			.add(Session.class, slot -> slot.wdg().ui.sess);

	private ArrayList<CraftSlot> recent = new ArrayList<>();

	Coord doff;
	UI.Grab mg;
	boolean moved = false;
	boolean vertical = false;

	public class CraftSlot implements GSprite.Owner {
		MenuGrid.Pagina p;

		CraftSlot(MenuGrid.Pagina p) {
			this.p = p;
		}
		private GSprite spr = null;
		public GSprite spr() {
			GSprite ret = this.spr;
			if(ret == null)
				ret = this.spr = GSprite.create(this, p.res.get(), Message.nil);
			return(ret);
		}

		public Resource getres() {return(p.res.get());}
		public Random mkrandoom() {return(new Random(System.identityHashCode(this)));}
		public <T> T context(Class<T> cl) {return(ctx.context(cl, this));}
		public Widget wdg() {
			return RecentCrafts.this;
		}
	}

	public RecentCrafts() {
		super(new Coord((invsq.sz().x + UI.scale(2)) * 8, UI.scale(34)));
		if(haven.purus.Config.pref.getBoolean("recentcrafts_flip", false))
			flip();
	}

	private void flip() {
		this.vertical = !this.vertical;
		haven.purus.Config.pref.putBoolean("recentcrafts_flip", this.vertical);
		this.sz = this.sz.swap();
		setInBounds();
	}

	private Coord beltc(int i) {
		Coord c = new Coord((((invsq.sz().x + UI.scale(2)) * i)), 0);
		if(vertical)
			return c.swap();
		else
			return c;
	}

	private int beltslot(Coord c) {
		for(int i = 0; i < 8; i++) {
			if(c.isect(beltc(i), invsq.sz()))
				return i;
		}
		return(-1);
	}

	public void draw(GOut g) {
		for(int i = 0; i < 8; i++) {
			Coord c = beltc(i);
			g.image(invsq, beltc(i));
			try {
				if(i < recent.size() && recent.get(i) != null) {
					recent.get(i).spr().draw(g.reclip(c.add(UI.scale(1), UI.scale(1)), invsq.sz().sub(UI.scale(2), UI.scale(2))));
				}
			} catch(Loading e) {}
			g.chcolor();
		}
	}
	@Override
	protected void added() {
		loadPosition();
		setInBounds();
		super.added();
	}

	public void savePosition() {
		haven.purus.Config.pref.putInt("recentcrafts_x", this.c.x);
		haven.purus.Config.pref.putInt("recentcrafts_y", this.c.y);
	}

	public void loadPosition() {
		this.c = new Coord(haven.purus.Config.pref.getInt("recentcrafts_x", this.c.x), haven.purus.Config.pref.getInt("recentcrafts_y", this.c.y));
	}

	@Override
	public boolean mouseup(Coord c, int button) {
		if(mg != null)
			mg.remove();
		mg = null;
		return super.mouseup(c, button);
	}

	@Override
	public void mousemove(Coord c) {
		if(mg != null) {
			this.c = this.c.add(c.add(doff.inv()));
			setInBounds();
			moved = true;
		}
		super.mousemove(c);
	}

	public void push(MenuGrid.Pagina p) {
		recent.removeIf((el) -> el.p.equals(p));
		recent.add(0, new CraftSlot(p));
		if(recent.size() > 8)
			recent.remove(recent.size()-1);
	}

	public void setInBounds() {
		try {
			this.c = this.c.max(-this.sz.x/4*3, -this.sz.y/4*3).min(this.parent.sz.x - this.sz.x/4, this.parent.sz.y - this.sz.y/4);
			savePosition();
		} catch(NullPointerException e) {
			// Ignored
		}
	}
	public boolean mousedown(Coord c, int button) {
		if(button == 2) {
			flip();
			return true;
		}
		int slot = beltslot(c);
		if(slot != -1 && slot < recent.size()) {
			if(button == 1) {
				gameui().menu.wdgmsg("act", (Object[]) recent.get(slot).p.act().ad);
				if((recent.get(slot).p.act().ad[0].equals("craft")))
					gameui().makewnd.setLastAction(recent.get(slot).p);
			}
			return(true);
		} else {
			if(button == 1) {
				doff = c;
				mg = ui.grabmouse(this);
				moved = false;
				return true;
			}
		}
		return(false);
	}

}
