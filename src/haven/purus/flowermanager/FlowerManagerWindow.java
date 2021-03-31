package haven.purus.flowermanager;

import haven.*;
import haven.purus.BetterWindow;
import haven.purus.Config;

import java.util.ArrayList;

public class FlowerManagerWindow extends BetterWindow {

	private PetalList vl;

	public FlowerManagerWindow() {
		super(UI.scale(300, 350), "Flowermenu manager");

		add(new Button(UI.scale(100), "Refresh"){
			@Override
			public boolean mousedown(Coord c, int button) {
				refresh();
				return super.mousedown(c, button);
			}
		}, UI.scale(150, 0));

		vl = new PetalList(825, 10);
		refresh();
		add(vl, UI.scale(25, 35));
	}

	public void refresh() {
		vl.clearItems();
		Config.flowerOptOpens.val.entrySet().stream().sorted((a, b) -> a.getKey().compareTo(b.getKey())).forEach(vi -> {vl.addItem(new PetalItem(vi.getKey(), vi.getValue()));});
	}


	@Override
	public void wdgmsg(Widget sender, String msg, Object... args) {
		if(sender == cbtn) {
			hide();
		} else {
			super.wdgmsg(sender, msg, args);
		}
	}

	public class PetalList extends Widget {

		ArrayList<PetalItem> items = new ArrayList<>();
		Scrollbar sb;
		int rowHeight = UI.scale(30);
		int rows, w;

		public PetalList(int w, int rows) {
			this.rows = rows;
			this.w = w;
			this.sz = new Coord(w, rowHeight * rows);
			sb = new Scrollbar(rowHeight * rows, 0, 100);
			add(sb, UI.scale(0, 0));
		}

		public PetalItem listitem(int i) {
			return items.get(i);
		}

		public void addItem(PetalItem item) {
			add(item);
			items.add(item);
		}

		public void clearItems() {
			this.children().forEach(w -> {if(w instanceof PetalItem)w.destroy();});
			items.clear();
		}

		public int listitems() {
			return items.size();
		}

		@Override
		public boolean mousewheel(Coord c, int amount) {
			sb.ch(amount);
			return true;
		}

		@Override
		public boolean mousedown(Coord c, int button) {
			int row = c.y / rowHeight + sb.val;
			if(row >= items.size())
				return super.mousedown(c, button);
			if(items.get(row).mousedown(c.sub(UI.scale(15), c.y / rowHeight * rowHeight), button))
				return true;
			return super.mousedown(c, button);
		}

		@Override
		public boolean mouseup(Coord c, int button) {
			int row = c.y / rowHeight + sb.val;
			if(row >= items.size())
				return super.mouseup(c, button);
			if(items.get(row).mouseup(c.sub(UI.scale(15), c.y / rowHeight * rowHeight), button))
				return true;
			return super.mouseup(c, button);
		}

		@Override
		public void draw(GOut g) {
			sb.max = items.size()-rows;
			for(int i=0; i<rows; i++) {
				if(i+sb.val >= items.size())
					break;
				GOut ig = g.reclip(new Coord(UI.scale(15), i*rowHeight), new Coord(w-UI.scale(15), rowHeight));
				items.get(i+sb.val).draw(ig);
			}
			super.draw(g);
		}

	}

	public static class PetalItem extends Widget {

		public String name;

		public PetalItem(String name, boolean value) {
			this.name = name;
			add(new CheckBox("Autopick " + name) {
				{a = value;}
				@Override
				public boolean mousedown(Coord c, int button) {
					Config.flowerOptOpens.val.put(name, !a);
					Config.flowerOptOpens.setVal(Config.flowerOptOpens.val);
					return super.mousedown(c, button);
				}
			}, UI.scale(0, 5));
		}

		@Override
		public void draw(GOut g) {
			super.draw(g);
		}

		@Override
		public void mousemove(Coord c) {
			if(c.x > 470)
				super.mousemove(c.sub(UI.scale(15), 0));
			else
				super.mousemove(c);
		}

		@Override
		public boolean mousedown(Coord c, int button) {
			if(super.mousedown(c, button))
				return true;
			return false;
		}
	}
}
