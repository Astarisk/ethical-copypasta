package haven.purus;

import haven.*;

import static haven.Inventory.invsq;

public class HandsWdg extends Widget implements DTarget {

	private GItem[] slots = new GItem[2];
	private WItem[] wSlots = new WItem[2];

	public UI.Grab dm = null;
	public Coord doff;



	public HandsWdg() {
		super(UI.scale(invsq.sz().x * 2 + 1, invsq.sz().y));
	}

	@Override
	public boolean drop(Coord c, Coord ul) {
		Window eqw = gameui().equwnd;
		if(eqw != null) {
			Equipory eq = gameui().equwnd.getchild(Equipory.class);
			if(eq != null) {
				eq.wdgmsg("drop", c.x <= invsq.sz().x ? 6 : 7);
				return true;
			}
		}
		return false;
	}

	@Override
	public boolean iteminteract(Coord cc, Coord ul) {
		Window eqw = gameui().equwnd;
		if(eqw != null) {
			Equipory eq = gameui().equwnd.getchild(Equipory.class);
			if(eq != null && eq.slots[cc.x <= invsq.sz().x ? 6 : 7] != null) {
				eq.slots[cc.x <= invsq.sz().x ? 6 : 7].wdgmsg("itemact", 0);
				return true;
			}
		}
		return false;
	}

	@Override
	public void tick(double dt) {
		Window eqw = gameui().equwnd;
		if(eqw != null) {
			Equipory eq = gameui().equwnd.getchild(Equipory.class);
			if(eq != null)
				for(int i = 0; i < 2; i++) {
					if(eq.slots[6+i] != slots[i]) {
						slots[i] = eq.slots[6+i];
						if(wSlots[i] != null)
							wSlots[i].destroy();
						if(slots[i] == null)
							wSlots[i] = null;
						else
							wSlots[i] = add(new WItem(eq.slots[6+i]), UI.scale(i*(invsq.sz().x + 1), 0));
						raise();
					}
				}
		}
		super.tick(dt);
	}

	@Override
	public void draw(GOut g) {
		for(int i = 0; i < 2; i++)
			g.image(invsq, UI.scale((invsq.sz().x + 1) * i, 0));
		super.draw(g);
	}

	@Override
	protected void added() {
		raise();
		loadPosition();
		super.added();
	}

	public void savePosition() {
		Config.pref.putInt("handswdg_x", this.c.x);
		Config.pref.putInt("handswdg_y", this.c.y);
	}

	public void loadPosition() {
		this.c = new Coord(Config.pref.getInt("handswdg_x", this.c.x), Config.pref.getInt("handswdg_y", this.c.y));
		setInBounds();
	}

	public void setInBounds() {
		try {
			this.c = this.c.max(-this.sz.x/4*3, -this.sz.y/4*3).min(this.parent.sz.x - this.sz.x/4, this.parent.sz.y - this.sz.y/4);
			savePosition();
		} catch(NullPointerException e) {
			// Ignored
		}
	}

	public boolean mouseup(Coord c, int button) {
		if(dm != null) {
			dm.remove();
			dm = null;
		} else {
			super.mouseup(c, button);
		}
		return(true);
	}

	public boolean mousedown(Coord c, int button) {
		if(button == 1 && (c.x == invsq.sz().x || slots[c.x < invsq.sz().x ? 0 : 1] == null)) {
			doff = c;
			dm = ui.grabmouse(this);
			return true;
		}
		return super.mousedown(c, button);
	}

	@Override
	public void mousemove(Coord c) {
		if(dm != null) {
			this.c = this.c.add(c.add(doff.inv()));
			setInBounds();
		} else {
			super.mousemove(c);
		}
	}
}
