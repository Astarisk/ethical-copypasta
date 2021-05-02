package haven.purus.timer;

import haven.*;
import haven.purus.BetterWindow;
import haven.purus.Config;
import haven.purus.MultiSession;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class TimerWnd extends BetterWindow {

	private TimerList vl;

	private static final Resource timerNotify = Resource.local().loadwait("sfx/alarms/timer");

	private static final SimpleDateFormat dfmt = new SimpleDateFormat("dd.MM HH:mm");


	public static ScheduledExecutorService timerNotifiers = Executors.newSingleThreadScheduledExecutor();

	public static void timerCalc(Glob glob) {
		long now = (long)(glob.globtime() / 3.29);
		timerNotifiers.shutdownNow();
		timerNotifiers = Executors.newSingleThreadScheduledExecutor();
		for(Timer t : Config.timersSet.val) {
			if(t.startedAt != null) {
				t.readyAt = System.currentTimeMillis()/1000 + (long)((t.startedAt/3.29 + t.duration()) - now);
				if(t.readyAt <= 0) {
					Audio.play(timerNotify);
					MultiSession.activeSession.root.adda(new TimerNotifier(t), MultiSession.activeSession.root.sz.div(2), 0.5, 0.5);
					t.startedAt = null;
					Config.timersSet.setVal(Config.timersSet.val);
					changed++;
				} else {
					timerNotifiers.schedule(() -> {
						if(t.startedAt == null)
							return;
						Audio.play(timerNotify);
						t.startedAt = null;
						Config.timersSet.setVal(Config.timersSet.val);
						changed++;
						MultiSession.activeSession.root.adda(new TimerNotifier(t), MultiSession.activeSession.root.sz.div(2), 0.5, 0.5);
					}, t.readyAt - System.currentTimeMillis() / 1000, TimeUnit.SECONDS);
				}
			}
		}
	}

	public static class TimerNotifier extends Window {
		public TimerNotifier(Timer t) {
			super(UI.scale(200, 60), "Timer completed!");
			adda(new Label("Timer " + t.name + " has finished."), UI.scale(100, 10), 0.5, 0.5);
			adda(new Button(UI.scale(60), "Ok"), UI.scale(100, 40), 0.5, 0.5)
			.action(this::reqdestroy);
		}
	}

	private static long changed = 1;
	private long changedLast = 0;

	public TimerWnd() {
		super(UI.scale(500, 300), "Timers");

		add(new Button(UI.scale(100), "Add timer"), UI.scale(5, 0))
		.action(() -> {
			Timer t = new Timer("timer", 0, null);
			vl.addItem(t);
			Config.timersSet.val.add(t);
			Config.timersSet.setVal(Config.timersSet.val);
		});

		add(new Label("Hours"), UI.scale(345, 15));
		add(new Label("Minutes"), UI.scale(345 + 50, 15));
		add(new Label("Seconds"), UI.scale(345 + 100, 15));

		vl = new TimerList(UI.scale(500 - 15), 8);
		add(vl, UI.scale(15, 35));
	}

	public void refresh() {
		vl.clearItems();
		Config.timersSet.val.forEach(t -> vl.addItem(t));
	}


	@Override
	public void wdgmsg(Widget sender, String msg, Object... args) {
		if(sender == cbtn) {
			hide();
		} else {
			super.wdgmsg(sender, msg, args);
		}
	}

	public static class Timer implements Serializable {

		public String name;
		public long seconds;
		public long minutes;
		public long hours;
		public long readyAt = 0;
		public Long startedAt;

		public Timer(String name, long duration, Long startedAt) {
			this.name = name;
			this.seconds = duration % 60;
			this.minutes = (duration % 3600) / 60;
			this.hours = duration / 3600;
			this.startedAt = startedAt;
		}

		public long duration() {
			return seconds + minutes * 60 + hours * 3600;
		}

		public long remaining() {
			return readyAt - (System.currentTimeMillis() / 1000);
		}
	}

	@Override
	public void tick(double dt) {
		if(changedLast != changed) {
			changedLast = changed;
			refresh();
		}
		super.tick(dt);
	}

	public class TimerList extends Widget {

		ArrayList<TimerItem> items = new ArrayList<>();
		Scrollbar sb;
		int rowHeight = UI.scale(30);
		int rows, w;
		ArrayList<TimerItem> visibleItems = new ArrayList<>();

		public TimerList(int w, int rows) {
			super(UI.scale(w, 30 * rows));
			this.rows = rows;
			this.w = w;
			sb = new Scrollbar(rowHeight * rows, 0, 100) {
				@Override
				public void changed() {
					updShow();
					super.changed();
				}
			};
			add(sb, UI.scale(0, 0));
		}

		public void updShow() {
			for(TimerItem ti : visibleItems) {
				ti.remove();
			}
			visibleItems.clear();
			sb.max = items.size()-rows;
			for(int i=0; i<rows; i++) {
				if(i+sb.val >= items.size())
					break;
				visibleItems.add(items.get(i+sb.val));
				add(items.get(i+sb.val), new Coord(UI.scale(15), i*rowHeight));
				items.get(i+sb.val).show();
			}
		}

		public TimerItem listitem(int i) {
			return items.get(i);
		}

		public void addItem(Timer t) {
			items.add(new TimerItem(t, this.w));
			updShow();
		}

		public void clearItems() {
			this.children().forEach(w -> {if(w instanceof TimerItem)w.destroy();});
			visibleItems.clear();
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

	}
	public class TimerItem extends Widget {

		private Timer timer;
		private Label remaining;

		public TimerItem(Timer timer, int w) {
			super(UI.scale(w, 30));
			this.timer = timer;
			int x = 0;
			add(new TextEntry(UI.scale(200), timer.name) {
				@Override
				protected void changed() {
					timer.name = this.text;
					Config.timersSet.setVal(Config.timersSet.val);
					super.changed();
				}
			}, UI.scale(x, 5));
			x += 200 + 5;
			if(timer.startedAt == null) {
				add(new Button(UI.scale(50), "Start"), UI.scale(x, 5))
						.action(() -> {
							timer.startedAt = (long) gameui().ui.sess.glob.globtime();
							Config.timersSet.setVal(Config.timersSet.val);
							changed++;
							timerCalc(gameui().ui.sess.glob);
						});
			} else {
				add(new Button(UI.scale(50), "Stop"), UI.scale(x, 5))
						.action(() -> {
							timer.startedAt = null;
							Config.timersSet.setVal(Config.timersSet.val);
							changed++;
							timerCalc(gameui().ui.sess.glob);
						});
			}
			x +=  50 + 5;
			add(new Button(UI.scale(50), "Delete") {
				@Override
				public void click() {
					timer.startedAt = null;
					Config.timersSet.val.remove(timer);
					Config.timersSet.setVal(Config.timersSet.val);
					changed++;
					super.click();
				}
			}, UI.scale(x, 5));
			x +=  50 + 5;
			if(timer.startedAt != null) {
				remaining = add(new Label(String.format("%02d : %02d : %02d", timer.remaining() / 3600, timer.remaining() % 3600 / 60, timer.remaining() % 60)), UI.scale(x, 5));
			} else {
				add(new TextEntry(UI.scale(45), Long.toString(timer.hours)) {
					@Override
					protected void changed() {
						try {
							timer.hours = Long.parseLong(this.text);
							Config.timersSet.setVal(Config.timersSet.val);
							changed++;
						} catch(NumberFormatException e) {
						}
						super.changed();
					}
				}, UI.scale(x, 5));
				x += 50;
				add(new TextEntry(UI.scale(45), Long.toString(timer.minutes)) {
					@Override
					protected void changed() {
						try {
							timer.minutes = Long.parseLong(this.text);
							Config.timersSet.setVal(Config.timersSet.val);
							changed++;
						} catch(NumberFormatException e) {
						}
						super.changed();
					}
				}, UI.scale(x, 5));
				x += 50;
				add(new TextEntry(UI.scale(45), Long.toString(timer.seconds)) {
					@Override
					protected void changed() {
						try {
							timer.seconds = Long.parseLong(this.text);
							Config.timersSet.setVal(Config.timersSet.val);
							changed++;
						} catch(NumberFormatException e) {
						}
						super.changed();
					}
				}, UI.scale(x, 5));
			}
		}

		@Override
		public void draw(GOut g) {
			if(timer.startedAt != null) {
				remaining.settext(String.format("%02d : %02d : %02d", timer.remaining() / 3600, timer.remaining() % 3600 / 60, timer.remaining() % 60) + "  " + dfmt.format(new Date(System.currentTimeMillis() + timer.remaining() * 1000)));
			}
			super.draw(g);
		}
	}
}
