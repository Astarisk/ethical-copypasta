package haven.purus;

import haven.*;
import haven.Label;

import java.awt.*;
import java.util.List;

public class Credentials {

	public static class CredentialsWidget extends Widget {
		private static Text.Foundry font = new Text.Foundry(Text.mono.deriveFont(Font.PLAIN, UI.scale(14))).aa(true);
		public CredentialsWidget() {
			super(UI.scale(200, 500));
			int yOfs = 0;
			for(String username : getUsernames()) {
				Label lbl = add(new Label(username, font){
					@Override
					public void mousemove(Coord c) {
						if(c.isect(Coord.z, sz)) {
							this.setcolor(Color.ORANGE);
						} else if(this.col == Color.orange) {
							this.setcolor(Color.white);
						}
						super.mousemove(c);
					}

					@Override
					public boolean mousedown(Coord c, int button) {
						this.parent.parent.wdgmsg("forget");
						this.parent.parent.wdgmsg("login", new AuthClient.NativeCred(username, getPassword(username)), true);
						return true;
					}
				}, UI.scale(5), yOfs);
				add(new Label("X", font) {
					@Override
					public void mousemove(Coord c) {
						if(c.isect(Coord.z, sz)) {
							this.setcolor(Color.RED);
						} else if(this.col == Color.RED) {
							this.setcolor(Color.WHITE);
						}
						super.mousemove(c);
					}

					@Override
					public boolean mousedown(Coord c, int button) {
						Credentials.removeCredentials(username);
						this.parent.parent.add(new CredentialsWidget());
						this.parent.destroy();
						return true;
					}
				}, sz.x - UI.scale(14), yOfs);
				yOfs += UI.scale(1) + lbl.sz.y;
			}
		}

		protected void drawbg(GOut g) {
			g.chcolor(0, 0, 0, 128);
			g.frect(Coord.z, sz);
			g.chcolor();
		}

		@Override
		public void draw(GOut g) {
			drawbg(g);
			super.draw(g);
		}
	}

	public static void saveCredentials(String username, String password) {
		haven.purus.database.Credentials.setCredential(username, password);
	}

	public static void removeCredentials(String username) {
		haven.purus.database.Credentials.removeCredential(username);
	}

	public static List<String> getUsernames() {
		return haven.purus.database.Credentials.getUsernames();
	}

	public static String getPassword(String username) {
		return haven.purus.database.Credentials.getPassword(username);
	}
}
