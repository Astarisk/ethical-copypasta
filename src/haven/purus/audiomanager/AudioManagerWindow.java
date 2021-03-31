package haven.purus.audiomanager;

import haven.*;
import haven.purus.BetterWindow;
import haven.purus.Config;

import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;

public class AudioManagerWindow extends BetterWindow {

	public static final ConcurrentHashMap<String, Long> recentClips = new ConcurrentHashMap<String, Long>();
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

		vl = new AudioManagerWindow.VolumeList(UI.scale(825), 10);
		refresh();
		add(vl, UI.scale(25, 35));
	}

	public void refresh() {
		vl.clearItems();
		Config.customVolumes.val.entrySet().stream()
				.sorted((b, a) -> {
			return Long.compare(recentClips.getOrDefault(a.getKey(), 0L), recentClips.getOrDefault(b.getKey(), 0L));
		})
				.forEach(vi -> {vl.addItem(new VolumeItem(vi.getKey(), recentClips.getOrDefault(vi.getKey(), 0L), vi.getValue()));});
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
			this.sz = new Coord(w, rowHeight * rows);
			sb = new Scrollbar(rowHeight * rows, 0, 100);
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
				GOut ig = g.reclip(new Coord(UI.scale(15), i*rowHeight), new Coord(w-UI.scale(15), rowHeight));
				items.get(i+sb.val).draw(ig);
			}
			super.draw(g);
		}

	}

	public static class VolumeItem extends Widget {

		public String resname;
		public long playedAt;
		private Label agoLbl = new Label((System.currentTimeMillis()-playedAt) + " seconds ago");

		public VolumeItem(String resname, long playedAt, float vol) {
			this.playedAt = playedAt;
			this.resname = resname;
			add(new Label(resname), UI.scale(0,0));
			add(agoLbl, UI.scale(235),0);
			add(new HSlider(UI.scale(200), 0, 100, (int)(vol*100)){
				@Override
				public void changed() {
					Config.customVolumes.val.put(resname, this.val/100.0f);
					Config.customVolumes.setVal(Config.customVolumes.val);
					super.changed();
				}
			}, UI.scale(470, 0));
			add(new Button(UI.scale(50), "Play") {
				@Override
				public boolean mousedown(Coord c, int button) {
					try {
						Audio.play(new Audio.VolAdjust(Audio.fromres(Resource.remote().loadwait(resname)), Config.customVolumes.val.get(resname)));

					}catch(Loading l){}
					return super.mousedown(c, button);
				}
			}, UI.scale(680, 0));
			updAgo();
		}

		public void updAgo() {
			if(playedAt == 0)
				this.agoLbl.settext("Not played in this session");
			else
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
