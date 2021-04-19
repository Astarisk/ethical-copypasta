package haven.purus;

import haven.*;
import haven.Button;
import haven.Config;
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
		public void wdgmsg(Widget sender, String msg, Object... args) {
			if(sender == cbtn) {
				reqdestroy();
				return;
			}
			super.wdgmsg(sender, msg, args);
		}

		@Override
		public void gtick(Render out) {
			if(update) {
				if(haven.purus.Config.disableSessWnd.val) {
					hide();
					super.gtick(out);
					return;
				}
				show();
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
		ui.audio.amb.setVolumeNoSave(0);
		ui.audio.pos.setVolumeNoSave(0);
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
			if(ui.sess != null)
				ui.sess.close();
			else
				ui.destroy();
		}
		if(activeSession != null && activeSession.root != null && activeSession.root.multiSessionWindow != null)
			activeSession.root.multiSessionWindow.update();
	}

	public static void setActiveSession(UI ui) {
		if(ui.sess != null)
			MainFrame.mf.setTitle("Haven and Hearth \u2013 Purus Pasta 2 " + Config.version + " \u2013 " + ui.sess.username);
		else
			MainFrame.mf.setTitle("Haven and Hearth \u2013 Purus Pasta 2 "+ Config.version);
		if(activeSession != null) {
			activeSession.audio.amb.setVolumeNoSave(0);
			activeSession.audio.pos.setVolumeNoSave(0);
		}
		synchronized(sessions) {
			activeSession = ui;
		}
		activeSession.audio.amb.setVolumeNoSave(Double.parseDouble(Utils.getpref("sfxvol-" + ui.audio.amb.name, "1.0")));
		activeSession.audio.pos.setVolumeNoSave(Double.parseDouble(Utils.getpref("sfxvol-" + ui.audio.pos.name, "1.0")));
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
