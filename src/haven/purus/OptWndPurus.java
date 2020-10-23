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

		private List<Entry> subentries = new ArrayList<>();

		public Entry(Widget w, String keywords) {
			this.keywords = keywords;
			this.w = w;
		}

		public void addSubentry(Entry e) {
			subentries.add(e);
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

		Entry root = new Entry(add(new Widget()), "*");
		List<Widget> filteredWidgets = new ArrayList<>();

		private Scrollbar sb;

		public EntryList(Coord sz) {
			super(sz);
			sb = add(new Scrollbar(sz.y, 0, 0), sz.x, 0);
			sb.resize(sz.y);
			sb.show();
			sb.c = new Coord(sz.x - sb.sz.x, 0);
		}

		public void search(String s) {
			filteredWidgets = root.match(s);
			this.sb.max = 0;
			for(Widget w : filteredWidgets) {
				this.sb.max += w.sz.y + UI.scale(15);
				if(w.parent == null) {
					add(w);
				}
			}
			for(Widget w : this.children(Widget.class)) {
				w.hide();
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
					w.c = new Coord(10, curY-sb.val);
					w.visible = true;
				} else {
					w.visible = false;
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

		searchField = add(new TextEntry(UI.scale(700), ""), UI.scale(50, 5));

		el = add(new EntryList(UI.scale(600, 750)), UI.scale(100, 25));

		Entry thingToggles = new Entry(new Label("Toggle things on login"), "Toggle on login");
		el.root.addSubentry(thingToggles);

		thingToggles.addSubentry(new Entry(new CheckBox("Toggle tracking on login"){
			{a = Config.toggleTracking.val;}
			@Override
			public boolean mousedown(Coord c, int button) {
				Config.toggleTracking.setVal(!this.a);
				return super.mousedown(c, button);
			}
		}, "Toggle tracking on login"));
		thingToggles.addSubentry(new Entry(new CheckBox("Toggle criminal acts on login"){
			{a = Config.toggleCriminalacts.val;}
			@Override
			public boolean mousedown(Coord c, int button) {
				Config.toggleCriminalacts.setVal(!this.a);
				return super.mousedown(c, button);
			}
		}, "Toggle criminal acts on login"));
		thingToggles.addSubentry(new Entry(new CheckBox("Toggle siege pointers on login"){
			{a = Config.toggleSiege.val;}
			@Override
			public boolean mousedown(Coord c, int button) {
				Config.toggleSiege.setVal(!this.a);
				return super.mousedown(c, button);
			}
		}, "Toggle siege pointers on login"));

		Entry uiSettings = new Entry(new Label("UI Settings"), "UI Settings");
		el.root.addSubentry(uiSettings);

		uiSettings.addSubentry(new Entry(new CheckBox("Use hardware cursor [Requires restart]"){
			{a = Config.hwcursor.val;}
			@Override
			public boolean mousedown(Coord c, int button) {
				Config.hwcursor.setVal(!this.a);
				return super.mousedown(c, button);
			}
		}, "Use hardware cursor [Requires restart]"));

		uiSettings.addSubentry(new Entry(new CheckBox("Show gob damage [Requires restart]"){
			{a = Config.showGobDecayNum.val;}
			@Override
			public boolean mousedown(Coord c, int button) {
				Config.showGobDecayNum.setVal(!this.a);
				return super.mousedown(c, button);
			}
		}, "Show gob damage [Requires restart]"));

		Entry cameraSettings = new Entry(new Label("Camera settings"), "Camera settings");
		el.root.addSubentry(cameraSettings);

		Entry camScrollLbl = new Entry(new Label("Camera scroll zoom sensitivity"), "Camera scroll zoom sensitivity");
		cameraSettings.addSubentry(camScrollLbl);
		camScrollLbl.addSubentry(new Entry(new HSlider(UI.scale(400), 1, 200, Math.round(10 * Config.cameraScrollSensitivity.val)) {
			@Override
			public void changed() {
				Config.cameraScrollSensitivity.setVal(this.val / 10.0f);
				super.changed();
			}
		}, ""));
		el.search("");

		Entry debugSettings = new Entry(new Label("Debug Settings"), "Debug Settings");
		el.root.addSubentry(debugSettings);

		debugSettings.addSubentry(new Entry(new CheckBox("Write resource source codes in debug directory"){
			{a = Config.debugRescode.val;}
			@Override
			public boolean mousedown(Coord c, int button) {
				Config.debugRescode.setVal(!this.a);
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
