package haven.purus.audiomanager;

import haven.*;
import haven.purus.BetterWindow;
import haven.purus.Config;

import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;

public class AudioManagerWindow extends BetterWindow {

	public static final ConcurrentHashMap<String, VolumeItem> recentClips = new ConcurrentHashMap<String, VolumeItem>();
	private AudioManagerWindow.VolumeList vl;

	public AudioManagerWindow() {
		super(UI.scale(825, 400), "Volume manager");

		add(new Button(UI.scale(100), "Refresh"){
			@Override
			public boolean mousedown(Coord c, int button) {
				refresh();
				return super.mousedown(c, button);
			}
		}, UI.scale(700, 0));

		add(new Label("Audio resname"), UI.scale(40, 15));
		add(new Label("Last played"), UI.scale(280, 15));
		add(new Label("Volume"), UI.scale(520, 15));

		vl = new AudioManagerWindow.VolumeList(825, 10);
		refresh();
		add(vl, UI.scale(25, 35));
	}

	public void refresh() {
		vl.clearItems();
		synchronized(recentClips) {
			recentClips.values().stream().sorted((b, a) -> Long.compare(a.playedAt, b.playedAt)).forEach(vi -> {vl.addItem(vi); vi.updAgo();});
		}
	}


	@Override
	public void wdgmsg(Widget sender, String msg, Object... args) {
		if(sender == cbtn) {
			hide();
		} else {
			super.wdgmsg(sender, msg, args);
		}
	}

	public class VolumeList extends Widget {

		ArrayList<AudioManagerWindow.VolumeItem> items = new ArrayList<>();
		Scrollbar sb;
		int rowHeight = UI.scale(30);
		int rows, w;

		public VolumeList(int w, int rows) {
			this.rows = rows;
			this.w = w;
			this.sz = UI.scale(w, rowHeight * rows);
			sb = new Scrollbar(rowHeight *rows, 0, 100);
			add(sb, UI.scale(0, 0));
		}

		public AudioManagerWindow.VolumeItem listitem(int i) {
			return items.get(i);
		}

		public void addItem(AudioManagerWindow.VolumeItem item) {
			add(item);
			items.add(item);
		}

		public void clearItems() {
			this.children().forEach(w -> {if(w instanceof VolumeItem)w.destroy();});
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
				GOut ig = g.reclip(UI.scale(UI.scale(15), i*rowHeight), UI.scale(w-UI.scale(15), rowHeight));
				items.get(i+sb.val).draw(ig);
			}
			super.draw(g);
		}

	}

	public static class VolumeItem extends Widget {

		public String resname;
		public long playedAt;
		private Label agoLbl = new Label((System.currentTimeMillis()-playedAt) + " seconds ago");

		public VolumeItem(String resname, long playedAt) {
			this.playedAt = playedAt;
			this.resname = resname;
			add(new Label(resname), UI.scale(0,0));
			add(agoLbl, UI.scale(UI.scale(235),0));
			add(new HSlider(UI.scale(200), 0, 100, (int)(new Config.Setting<Float>("volume_" + resname, 1.0f).val*100)){
				@Override
				public void changed() {
					new Config.Setting<Float>("volume_" + resname, 1.0f).setVal(this.val/100.0f);
					super.changed();
				}
			}, UI.scale(470, 0));
			add(new Button(50, "Play") {
				@Override
				public boolean mousedown(Coord c, int button) {
					try {
						Audio.play(new Audio.VolAdjust(Audio.fromres(Resource.remote().loadwait(resname)), new Config.Setting<Float>("volume_", 1.0f).val));
					}catch(Loading l){}
					return super.mousedown(c, button);
				}
			}, UI.scale(680, 0));
			updAgo();
		}

		public void updAgo() {
			this.agoLbl.settext((System.currentTimeMillis() - playedAt)/1000 + " seconds ago");
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
