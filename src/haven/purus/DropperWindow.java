package haven.purus;

import haven.*;

import java.util.ArrayList;

public class DropperWindow extends BetterWindow {

	private DropList vl;

	public DropperWindow() {
		super(UI.scale(300, 350), "Item autodrop manager");

		add(new Button(UI.scale(100), "Add item") {
			@Override
			public boolean mousedown(Coord c, int button) {
				gameui().msg("Click item on any inventory to add!");
				gameui().autodropItmCb = true;
				return super.mousedown(c, button);
			}
		}, UI.scale(25, 0));

		add(new Button(UI.scale(100), "Refresh") {
			@Override
			public boolean mousedown(Coord c, int button) {
				refresh();
				return super.mousedown(c, button);
			}
		}, UI.scale(150, 0));

		vl = new DropList(825, 10);
		refresh();
		add(vl, UI.scale(25, 35));
	}

	public void refresh() {
		vl.clearItems();
		Config.autodropItems.val.entrySet().stream().sorted((a, b) -> a.getKey().compareTo(b.getKey())).forEach(vi -> {vl.addItem(new AutodropItem(vi.getKey(), vi.getValue()));});
	}

	@Override
	public void wdgmsg(Widget sender, String msg, Object... args) {
		if(sender == cbtn) {
			hide();
		} else {
			super.wdgmsg(sender, msg, args);
		}
	}

	public class DropList extends Widget {

		ArrayList<AutodropItem> items = new ArrayList<>();
		Scrollbar sb;
		int rowHeight = UI.scale(30);
		int rows, w;

		public DropList(int w, int rows) {
			this.rows = rows;
			this.w = w;
			this.sz = new Coord(w, rowHeight * rows);
			sb = new Scrollbar(rowHeight * rows, 0, 100);
			add(sb, UI.scale(0, 0));
		}

		public AutodropItem listitem(int i) {
			return items.get(i);
		}

		public void addItem(AutodropItem item) {
			add(item);
			items.add(item);
		}

		public void clearItems() {
			this.children().forEach(w -> {if(w instanceof AutodropItem)w.destroy();});
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

	public class AutodropItem extends Widget {

		public String name;

		public AutodropItem(String name, boolean value) {
			this.name = name;
			add(new CheckBox(name) {
				{a = value;}
				@Override
				public boolean mousedown(Coord c, int button) {
					Config.autodropItems.val.put(name, !a);
					Config.autodropItems.setVal(Config.autodropItems.val);
					return super.mousedown(c, button);
				}
			}, UI.scale(0, 5));

			add(new Button(UI.scale(60), "Delete") {
				@Override
				public boolean mousedown(Coord c, int button) {
					Config.autodropItems.val.remove(name);
					Config.autodropItems.setVal(Config.autodropItems.val);
					refresh();
					return super.mousedown(c, button);
				}
			}, UI.scale(200, 5));
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
