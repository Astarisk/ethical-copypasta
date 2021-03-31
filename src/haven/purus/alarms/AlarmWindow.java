package haven.purus.alarms;

import haven.*;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

public class AlarmWindow extends Window {

	private AlarmList al;

	public AlarmWindow() {
		super(UI.scale(825, 400), "Alarms");

		add(new Label("Gob resname"), UI.scale(40, 15));
		add(new Label("Alarm filename"), UI.scale(280, 15));
		add(new Label("Volume"), UI.scale(520, 15));

		al = new AlarmList(825, 10);
		for(AlarmItem ai : AlarmManager.getAlarmItems()) {
			al.addItem(ai);
		}
		add(al, UI.scale(25, 35));

		add(new Label("Add your own alarm files as .wav format to alarms folder"), UI.scale(100, 394));

		TextEntry addGobResname = new TextEntry(UI.scale(225), "");
		add(addGobResname, UI.scale(40,360));
		TextEntry addAlarmFilename = new TextEntry(UI.scale(225), "");
		add(addAlarmFilename, UI.scale(275,360));
		HSlider addVolume = new HSlider(UI.scale(100),0, 100,50);
		add(addVolume, UI.scale(510, 360));

		add(new Button(UI.scale(50), "Add") {
			@Override
			public void click() {
				al.addItem(new AlarmItem(addGobResname.text, addAlarmFilename.text, addVolume.val, false));
				addGobResname.settext("");
				addAlarmFilename.settext("");
			}
		}, UI.scale(620, 360));

		add(new Button(UI.scale(62), "Save") {
			@Override
			public void click() {
				AlarmManager.load(al);
				AlarmManager.save();
				ui.gui.msg("Alarm settings saved!");
			}
		}, UI.scale(680, 345));

		add(new Button(UI.scale(62), "Defaults") {
			@Override
			public void click() {
				AlarmManager.defaultSettings();
				while(!al.items.isEmpty())
					al.deleteItem(al.items.get(0));
				for(AlarmItem ai : AlarmManager.getAlarmItems()) {
					al.addItem(ai);
				}
				ui.gui.msg("Default settings restored!");
			}
		}, UI.scale(680, 380));
	}

	@Override
	public void wdgmsg(Widget sender, String msg, Object... args) {
		if(sender == cbtn) {
			hide();
		} else {
			super.wdgmsg(sender, msg, args);
		}
	}

	public class AlarmList extends Widget {

		ArrayList<AlarmItem> items = new ArrayList<>();
		Scrollbar sb;
		int rowHeight = UI.scale(30);
		int rows, w;

		public AlarmList(int w, int rows) {
			this.rows = rows;
			this.w = w;
			this.sz = UI.scale(w, rowHeight * rows);
			sb = new Scrollbar(rowHeight * rows, 0, 100);
			add(sb, UI.scale(0, 0));
		}

		public AlarmItem listitem(int i) {
			return items.get(i);
		}

		public void addItem(AlarmItem item) {
			add(item);
			items.add(item);
		}

		public void deleteItem(AlarmItem item) {
			item.destroy();
			items.remove(item);
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
				GOut ig = g.reclip(new Coord(UI.scale(15), i*rowHeight), UI.scale(w-UI.scale(15), rowHeight));
				items.get(i+sb.val).draw(ig);
			}
			super.draw(g);
		}

		@Override
		public void wdgmsg(Widget sender, String msg, Object... args) {
			if(msg.equals("delete") && sender instanceof AlarmItem) {
				deleteItem((AlarmItem) sender);
			} else {
				super.wdgmsg(sender, msg, args);
			}
		}

	}

	public static class AlarmItem extends Widget {

		private TextEntry gobResname, alarmFilename;
		private HSlider volume;
		private CheckBox knocked;

		public AlarmItem(String gobResname, String alarmFilename, int volume, boolean knocked) {
			this.gobResname = new TextEntry(UI.scale(225), gobResname);
			add(this.gobResname, UI.scale(0,0));
			this.alarmFilename = new TextEntry(UI.scale(225), alarmFilename);
			add(this.alarmFilename, UI.scale(235),0);
			this.volume = new HSlider(UI.scale(100), 0, 100, volume);
			add(this.volume, UI.scale(470, 0));
			add(new Button(UI.scale(50), "Play") {
				@Override
				public boolean mousedown(Coord c, int button) {
					if(button != 1)
						return true;
					File file = new File("alarms/" + getAlarmFilename());
					if(!file.exists() || file.isDirectory()) {
						ui.gui.msg("Error while playing an alarm, file " + file.getAbsolutePath() + " does not exist!");
						return super.mousedown(c, button);
					}
					try {
						AudioInputStream in = AudioSystem.getAudioInputStream(file);
						AudioFormat tgtFormat = new AudioFormat(AudioFormat.Encoding.PCM_SIGNED, 44100, 16, 2,4, 44100, false);
						AudioInputStream pcmStream = AudioSystem.getAudioInputStream(tgtFormat, in);
						Audio.CS klippi = new Audio.PCMClip(pcmStream, 2);
						((Audio.Mixer)Audio.player.stream).add(new Audio.VolAdjust(klippi, getVolume()/50.0));
					} catch(UnsupportedAudioFileException e) {
						e.printStackTrace();
					} catch(IOException e) {
						e.printStackTrace();
					}
					return super.mousedown(c, button);

				}
			}, UI.scale(580, 0));
			add(new Button(UI.scale(50), "Delete") {
				@Override
				public boolean mousedown(Coord c, int button) {
					if(button != 1)
						return super.mousedown(c, button);
					wdgmsg(this.parent, "delete");
					return super.mousedown(c, button);
				}
			}, UI.scale(640, 0));
			add(this.knocked = new CheckBox("Ignore KO") {
				{a = knocked;}
				@Override
				public boolean mousedown(Coord c, int button) {
					//Config.toggleTracking.setVal(!this.a);
					return super.mousedown(c, button);
				}
			}, UI.scale(700, 5));
		}

		public int getVolume() {
			return volume.val;
		}

		public boolean getKnocked() {
			return this.knocked.a;
		}

		public String getGobResname() {
			return gobResname.text;
		}

		public String getAlarmFilename() {
			return alarmFilename.text;
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