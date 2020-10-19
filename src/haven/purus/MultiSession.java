package haven.purus;

import haven.*;
import haven.Button;
import haven.render.Render;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.util.ArrayList;

public class MultiSession {
	public static final ArrayList<UI> sessions = new ArrayList<>();
	public static UI activeSession;

	public static KeyBinding kb_nextSession = KeyBinding.get("next_session", KeyMatch.forcode(KeyEvent.VK_PAGE_DOWN, 0));
	public static KeyBinding kb_prevSession = KeyBinding.get("previous_session", KeyMatch.forcode(KeyEvent.VK_PAGE_UP, 0));

	public static class MultiSessionWindow extends BetterWindow {

		boolean update = true;

		public MultiSessionWindow() {
			super(UI.scale(0, 0), "Sessions");
			this.visible = false;
		}

		public void update() {
			update = true;
		}

		@Override
		public void gtick(Render out) {
			if(update) {
				this.visible = true;
				for(Widget w : this.children(Button.class)) {
					w.destroy();
				}
				int ofsY = -UI.scale(5);
				synchronized(sessions) {
					for(UI session : sessions) {
						Button btn = add(new Button(UI.scale(200), (session.sess != null ? session.sess.username : "???")), 10, ofsY + UI.scale(5));
						btn.action = () -> {
							MultiSession.setActiveSession(session);
						};
						if(session == activeSession)
							btn.change(btn.text.text, Color.ORANGE);
						ofsY += btn.sz.y + UI.scale(5);
					}
					if(sessions.stream().noneMatch((ses) -> (ses.sess == null))) {
						Button btn = add(new Button(UI.scale(200), "New Session", () -> {
							MainFrame.mf.sessionCreate();
						}) {
							@Override
							public void click() {
								super.click();
								this.destroy();
							}
						}, 10, ofsY + UI.scale(5));
						ofsY += btn.sz.y + UI.scale(5);
					}
				}
				this.resize(UI.scale(220), ofsY + UI.scale(5));
				update = false;
			}
			super.gtick(out);
		}
	}

	public static void addSession(UI ui) {
		synchronized(sessions) {
			sessions.add(ui);
		}
		ui.audio.amb.volume = 0;
		ui.audio.pos.volume = 0;
		if(activeSession != null && activeSession.root != null && activeSession.root.multiSessionWindow != null)
			activeSession.root.multiSessionWindow.update();
	}
	public static void closeSession(UI ui) {
		synchronized(sessions) {
			if(ui == activeSession)
				nextSession(1);
			sessions.remove(ui);
		}
		synchronized(ui) {
			ui.destroy();
		}
		if(activeSession != null && activeSession.root != null && activeSession.root.multiSessionWindow != null)
			activeSession.root.multiSessionWindow.update();
	}

	public static void setActiveSession(UI ui) {
		if(ui.sess != null)
			MainFrame.mf.setTitle("Haven and Hearth \u2013 Purus Pasta 2 \u2013 " + ui.sess.username);
		else
			MainFrame.mf.setTitle("Haven and Hearth \u2013 Purus Pasta 2");
		if(activeSession != null) {
			activeSession.audio.amb.volume = 0;
			activeSession.audio.pos.volume = 0;
		}
		synchronized(sessions) {
			activeSession = ui;
		}
		ui.audio.amb.volume = Double.parseDouble(Utils.getpref("sfxvol-" + ui.audio.amb.name, "1.0"));
		ui.audio.pos.volume = Double.parseDouble(Utils.getpref("sfxvol-" + ui.audio.pos.name, "1.0"));
		if(activeSession.root != null && activeSession.root.multiSessionWindow != null)
			activeSession.root.multiSessionWindow.update();
	}

	public static void nextSession(int ofs) {
		synchronized(sessions) {
			if(activeSession == null) {
				if(!sessions.isEmpty())
					activeSession = sessions.get(sessions.size() - 1);
			} else {
				int currentIdx = sessions.indexOf(activeSession);
				currentIdx += ofs + sessions.size();
				currentIdx %= sessions.size();
				setActiveSession(sessions.get(currentIdx));
			}
		}
	}
}
