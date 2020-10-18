package haven.purus;

import haven.*;
import haven.Label;
import haven.Scrollbar;

import java.util.ArrayList;
import java.util.List;

public class OptWndPurus extends BetterWindow {

	private class Entry {

		String keywords;
		Widget w;

		List<Entry> subentries = new ArrayList<>();

		public Entry(Widget w, String keywords) {
			this.keywords = keywords;
			this.w = w;
		}

		List<Widget> match(String keyword) {
			ArrayList<Widget> l = new ArrayList<>();
			if(this.keywords.toLowerCase().contains(keyword.toLowerCase())) {
				l.add(this.w);
				for(Entry e:subentries)
					l.addAll(e.match(""));
			} else {
				for(Entry e:subentries)
					l.addAll(e.match(keyword));
			}
			return l;
		}

	}

	private class EntryList extends Widget {

		Entry root = new Entry(new Widget(), "*");
		List<Widget> filteredWidgets = new ArrayList<>();

		private Scrollbar sb;

		public EntryList(Coord sz) {
			super(sz);
			sb = add(new Scrollbar(sz.y, 0, 00), sz.x, 0);
			sb.resize(sz.y);
			sb.show();
			sb.c = new Coord(sz.x - sb.sz.x, 0);
		}

		public void search(String s) {
			filteredWidgets = root.match(s);
			this.sb.max = 0;
			for(Widget w : filteredWidgets) {
				this.sb.max += w.sz.y + UI.scale(15);
			}
			this.sb.max -= this.sz.y;
			if(this.sb.max < 0)
				this.sb.hide();
			else
				this.sb.show();
			this.sb.val = 0;
		}

		public void draw(GOut g) {
			int curY = 0;
			for(Widget w : filteredWidgets) {
				if(curY-sb.val > sz.y)
					break;
				if(curY + w.sz.y > sb.val) {
					Coord cc = xlate(new Coord(10, curY-sb.val), true);
					GOut g2;
					g2 = g.reclip(cc, w.sz);
					w.draw(g2);
				}
				curY += w.sz.y+UI.scale(15);
			}
			super.draw(g);
		}

		@Override
		public boolean mousedown(Coord c, int button) {
			int curY = 0;
			for(Widget w : filteredWidgets) {
				if(curY-sb.val > sz.y)
					break;
				if(curY + w.sz.y > sb.val) {
					Coord cc = xlate(new Coord(10, curY-sb.val), true);
					if(c.isect(cc, w.sz)) {
						if(w.mousedown(c.add(cc.inv()), button)) {
							return (true);
						}
					}
				}
				curY += w.sz.y+UI.scale(15);
			}
			return super.mousedown(c, button);
		}

		@Override
		public boolean mousewheel(Coord c, int amount) {
			sb.ch(amount*UI.scale(50));
			return true;
		}
	}

	private TextEntry searchField;
	private EntryList el;
	private String currentSearchword = "";

	public OptWndPurus() {
		super(UI.scale(800, 800), "Pasta Options");

		searchField = add(new TextEntry(700, ""), UI.scale(50, 5));

		el = add(new EntryList(UI.scale(600, 750)), UI.scale(100, 25));

		Entry thingToggles = new Entry(new Label("Toggle things on login"), "Toggle on login");
		el.root.subentries.add(thingToggles);

		thingToggles.subentries.add(new Entry(new CheckBox("Toggle tracking on login"){
			{a = Config.toggleTracking;}
			@Override
			public boolean mousedown(Coord c, int button) {
				Config.toggleTracking = !this.a;
				Config.pref.putBoolean("toggleTracking", !this.a);
				return super.mousedown(c, button);
			}
		}, "Toggle tracking on login"));
		thingToggles.subentries.add(new Entry(new CheckBox("Toggle criminal acts on login"){
			{a = Config.toggleCriminalacts;}
			@Override
			public boolean mousedown(Coord c, int button) {
				Config.toggleCriminalacts = !this.a;
				Config.pref.putBoolean("toggleCriminalacts", !this.a);
				return super.mousedown(c, button);
			}
		}, "Toggle criminal acts on login"));
		thingToggles.subentries.add(new Entry(new CheckBox("Toggle siege pointers on login"){
			{a = Config.toggleSiege;}
			@Override
			public boolean mousedown(Coord c, int button) {
				Config.toggleSiege = !this.a;
				Config.pref.putBoolean("toggleSiege", !this.a);
				return super.mousedown(c, button);
			}
		}, "Toggle siege pointers on login"));

		Entry uiSettings = new Entry(new Label("UI Settings"), "UI Settings");
		el.root.subentries.add(uiSettings);

		uiSettings.subentries.add(new Entry(new CheckBox("Use hardware cursor [Requires restart]"){
			{a = Config.hwcursor;}
			@Override
			public boolean mousedown(Coord c, int button) {
				Config.hwcursor = !this.a;
				Config.pref.putBoolean("hwcursor", !this.a);
				return super.mousedown(c, button);
			}
		}, "Use hardware cursor [Requires restart]"));

		el.search("");

		Entry debugSettings = new Entry(new Label("Debug Settings"), "Debug Settings");
		el.root.subentries.add(debugSettings);

		debugSettings.subentries.add(new Entry(new CheckBox("Write resource source codes in debug directory"){
			{a = Config.debugRescode;}
			@Override
			public boolean mousedown(Coord c, int button) {
				Config.debugRescode = !this.a;
				Config.pref.putBoolean("debugRescode", !this.a);
				return super.mousedown(c, button);
			}
		}, "Write resource source codes in debug directory"));

		el.search("");
	}

	@Override
	public void draw(GOut g) {
		if(!currentSearchword.equals(searchField.text)) {
			currentSearchword = searchField.text;
			el.search(currentSearchword);
		}
		super.draw(g);
	}

	@Override
	public void wdgmsg(Widget sender, String msg, Object... args) {
		if(sender == this && msg.equals("close")) {
			hide();
		} else {
			super.wdgmsg(sender, msg, args);
		}
	}

}
