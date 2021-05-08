package haven.purus;

import haven.*;
import haven.Button;
import haven.Label;
import haven.Scrollbar;
import haven.purus.mapper.Mapper;
import haven.render.BaseColor;
import haven.render.Location;
import haven.render.Pipe;
import haven.render.States;

import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

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
		((Label)thingToggles.w).setcolor(Color.ORANGE);
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
		Label runlbl;
		String[] speed = {"Crawling", "Walking", "Running", "Sprinting"};
		Entry speedOnloginLbl = new Entry(runlbl = new Label("Set movement speed on login: " + speed[Config.speedOnLogin.val]), "Set movement speed on login");
		thingToggles.addSubentry(speedOnloginLbl);
		speedOnloginLbl.addSubentry(new Entry(new HSlider(UI.scale(150), 0, 3, Config.speedOnLogin.val) {
			@Override
			public void changed() {
				Config.speedOnLogin.setVal(this.val);
				runlbl.settext("Set movement speed on login: " + speed[this.val]);
				super.changed();
			}
		}, ""));

		Entry uiSettings = new Entry(new Label("UI Settings"), "UI Settings");
		((Label)uiSettings.w).setcolor(Color.ORANGE);
		el.root.addSubentry(uiSettings);

		uiSettings.addSubentry(new Entry(new CheckBox("Use hardware cursor [Requires restart]"){
			{a = Config.hwcursor.val;}
			@Override
			public boolean mousedown(Coord c, int button) {
				Config.hwcursor.setVal(!this.a);
				return super.mousedown(c, button);
			}
		}, "Use hardware cursor [Requires restart]"));


		uiSettings.addSubentry(new Entry(new CheckBox("Disable chat notification sound on party & village chat"){
			{a = Config.disableMultichatNotification.val;}
			@Override
			public boolean mousedown(Coord c, int button) {
				Config.disableMultichatNotification.setVal(!this.a);
				return super.mousedown(c, button);
			}
		}, "Disable chat notification sound on party & village chat"));

		uiSettings.addSubentry(new Entry(new CheckBox("Disable fraktur font (ugly) [Requires restart]"){
			{a = Config.disableJorbfont.val;}
			@Override
			public boolean mousedown(Coord c, int button) {
				Config.disableJorbfont.setVal(!this.a);
				return super.mousedown(c, button);
			}
		}, "Disable fraktur font [Requires restart]"));

		uiSettings.addSubentry(new Entry(new CheckBox("Disable session wnd"){
			{a = Config.disableSessWnd.val;}
			@Override
			public boolean mousedown(Coord c, int button) {
				Config.disableSessWnd.setVal(!this.a);
				MultiSession.activeSession.root.multiSessionWindow.update();
				return super.mousedown(c, button);
			}
		}, "Disable session wnd"));

		Label fontScaleLbl = new Label("Font size scale [Requires restart]" + Config.fontScale.val + "x [Requires restart]");
		Entry fontScaleLblEntry = new Entry(fontScaleLbl, "Font size scale [Requires restart] font scaling");
		uiSettings.addSubentry(fontScaleLblEntry);
		fontScaleLblEntry.addSubentry(new Entry(new HSlider(UI.scale(400), 0, 100, Math.round(100 * (Config.fontScale.val - 1.0f))) {
			@Override
			public void changed() {
				Config.fontScale.setVal(this.val / 100.0f + 1);
				fontScaleLbl.settext("Font size scale: " + Config.fontScale.val + "x [Requires restart]");
				super.changed();
			}
		}, ""));

		uiSettings.addSubentry(new Entry(new CheckBox("Show gob damage [Requires restart]"){
			{a = Config.showGobDecayNum.val;}
			@Override
			public boolean mousedown(Coord c, int button) {
				Config.showGobDecayNum.setVal(!this.a);
				return super.mousedown(c, button);
			}
		}, "Show gob damage [Requires restart]"));

		Label flwSpeed = new Label("Flowermenu speed: " + Config.flowermenuSpeed.val + "s");
		Entry flowerMenulbl = new Entry(flwSpeed, "Flower menu speed flowermenu");
		uiSettings.addSubentry(flowerMenulbl);
		flowerMenulbl.addSubentry(new Entry(new HSlider(UI.scale(400), 0, 100, Math.round(100 * Config.flowermenuSpeed.val)) {
			@Override
			public void changed() {
				Config.flowermenuSpeed.setVal(this.val / 100f);
				flwSpeed.settext("Flowermenu speed: " + Config.flowermenuSpeed.val + "s");
				super.changed();
			}
		}, ""));

		uiSettings.addSubentry(new Entry(new CheckBox("Show inventory on login") {
			{a = Config.invShowLogin.val;}
			@Override
			public boolean mousedown(Coord c, int button) {
				Config.invShowLogin.setVal(!this.a);
				return super.mousedown(c, button);
			}
		}, "Show inventory on login"));

		uiSettings.addSubentry(new Entry(new CheckBox("Show belt on login") {
			{a = Config.beltShowLogin.val;}
			@Override
			public boolean mousedown(Coord c, int button) {
				Config.beltShowLogin.setVal(!this.a);
				return super.mousedown(c, button);
			}
		}, "Show belt on login"));

		Entry cameraSettings = new Entry(new Label("Camera settings"), "Camera settings");
		((Label)cameraSettings.w).setcolor(Color.ORANGE);
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

		cameraSettings.addSubentry(new Entry(new CheckBox("Bad Cam: Reverse X Axis"){
			{a = Config.reverseBadCamX.val;}
			@Override
			public boolean mousedown(Coord c, int button) {
				Config.reverseBadCamX.setVal(!this.a);
				return super.mousedown(c, button);
			}
		}, "Bad Cam: Reverse X Axis"));

		cameraSettings.addSubentry(new Entry(new CheckBox("Bad Cam: Reverse Y Axis"){
			{a = Config.reverseBadCamY.val;}
			@Override
			public boolean mousedown(Coord c, int button) {
				Config.reverseBadCamY.setVal(!this.a);
				return super.mousedown(c, button);
			}
		}, "Bad Cam: Reverse Y Axis"));

		Entry displaySettings = new Entry(new Label("Display settings"), "Display settings");
		((Label)displaySettings.w).setcolor(Color.ORANGE);
		el.root.addSubentry(displaySettings);

		displaySettings.addSubentry(new Entry(new CheckBox("Hide visual flavor objects [Requires restart]"){
			{a = Config.flavorObjsVisual.val;}
			@Override
			public boolean mousedown(Coord c, int button) {
				Config.flavorObjsVisual.setVal(!this.a);
				return super.mousedown(c, button);
			}
		}, "Hide visual flavor objects [Requires restart]"));

		displaySettings.addSubentry(new Entry(new CheckBox("Hide audio flavor objects [Requires restart]"){
			{a = Config.flavorObjsAudial.val;}
			@Override
			public boolean mousedown(Coord c, int button) {
				Config.flavorObjsAudial.setVal(!this.a);
				return super.mousedown(c, button);
			}
		}, "Hide all flavor (including sound) objects [Requires restart]"));

		displaySettings.addSubentry(new Entry(new CheckBox("Highlight tanning tubs and drying frames"){
			{a = Config.ttfHighlight.val;}
			@Override
			public boolean mousedown(Coord c, int button) {
				Config.ttfHighlight.setVal(!this.a);
				gameui().map.refreshGobsAll();
				return super.mousedown(c, button);
			}
		}, "Highlight tanning tubs and drying frames"));


		displaySettings.addSubentry(new Entry(new CheckBox("Enable columns above players"){
			{a = Config.playerRadiuses.val;}
			@Override
			public boolean mousedown(Coord c, int button) {
				Config.playerRadiuses.setVal(!this.a);
				gameui().map.refreshGobsAll();
				return super.mousedown(c, button);
			}
		}, "\"Enable columns above players"));

		Label cpScaleLbl = new Label("Cupboard height: " + Config.cupboardHeight.val + "x");
		Entry cpMenuLbl = new Entry(cpScaleLbl, "Cupboard height");
		displaySettings.addSubentry(cpMenuLbl);
		cpMenuLbl.addSubentry(new Entry(new HSlider(UI.scale(400), 1, 20, Math.round(20 * Config.cupboardHeight.val)) {
			@Override
			public void changed() {
				Config.cupboardHeight.setVal(this.val / 20f);
				SkelSprite.cupboardSize = new Location(new Matrix4f(1, 0, 0, 0,
						0, 1, 0, 0,
						0, 0, Config.cupboardHeight.val, 0,
						0, 0, 0, 1));
				for(Gob gob : gameui().ui.sess.glob.oc) {
					try {
						Resource res = gob.getres();
						if(res != null && res.name.equals("gfx/terobjs/cupboard"))
							gob.setattr(gob.getattr(ResDrawable.class));
					} catch(Loading l) {}
				}
				cpScaleLbl.settext("Cupboard height: " + Config.cupboardHeight.val + "x");
				super.changed();
			}
		}, ""));

		Entry combatSettings = new Entry(new Label("Combat Settings"), "Combat Settings");
		((Label)combatSettings.w).setcolor(Color.ORANGE);
		el.root.addSubentry(combatSettings);

		combatSettings.addSubentry(new Entry(new CheckBox("Proximity aggro kritters when clicked"){
			{a = Config.proximityKritterAggro.val;}
			@Override
			public boolean mousedown(Coord c, int button) {
				Config.proximityKritterAggro.setVal(!this.a);
				return super.mousedown(c, button);
			}
		}, "Proximity aggro kritters when clicked"));

		combatSettings.addSubentry(new Entry(new CheckBox("Proximity aggro players when clicked"){
			{a = Config.proximityPlayerAggro.val;}
			@Override
			public boolean mousedown(Coord c, int button) {
				Config.proximityPlayerAggro.setVal(!this.a);
				return super.mousedown(c, button);
			}
		}, "Proximity aggro players when clicked"));

		Entry debugSettings = new Entry(new Label("Debug Settings"), "Debug Settings");
		((Label)debugSettings.w).setcolor(Color.ORANGE);
		el.root.addSubentry(debugSettings);

		debugSettings.addSubentry(new Entry(new CheckBox("Write resource source codes in debug directory"){
			{a = Config.debugRescode.val;}
			@Override
			public boolean mousedown(Coord c, int button) {
				Config.debugRescode.setVal(!this.a);
				return super.mousedown(c, button);
			}
		}, "Write resource source codes in debug directory"));


		debugSettings.addSubentry(new Entry(new CheckBox("Print wdgmsg to console"){
			{a = Config.debugWdgmsg.val;}
			@Override
			public boolean mousedown(Coord c, int button) {
				Config.debugWdgmsg.setVal(!this.a);
				return super.mousedown(c, button);
			}
		}, "Print wdgmsg to console"));

		Entry hideSettings = new Entry(new Label("Hide Settings"), "Hide Settings");
		((Label)hideSettings.w).setcolor(Color.ORANGE);
		el.root.addSubentry(hideSettings);

		el.root.addSubentry(new Entry(new Label("Toggle hide by ctrl + h (default keybinding)"), "Toggle hide by"));

		hideSettings.addSubentry(new Entry(new CheckBox("Hide trees"){
			{a = Config.hideTrees.val;}
			@Override
			public boolean mousedown(Coord c, int button) {
				Config.hideTrees.setVal(!this.a);
				gameui().map.removeCustomSprites(1340);
				gameui().map.refreshGobsAll();
				return super.mousedown(c, button);
			}
		}, "Hide trees"));

		hideSettings.addSubentry(new Entry(new CheckBox("Hide bushes"){
			{a = Config.hideBushes.val;}
			@Override
			public boolean mousedown(Coord c, int button) {
				Config.hideBushes.setVal(!this.a);
				gameui().map.removeCustomSprites(1340);
				gameui().map.refreshGobsAll();
				return super.mousedown(c, button);
			}
		}, "Hide bushes"));

		hideSettings.addSubentry(new Entry(new CheckBox("Hide crops"){
			{a = Config.hideCrops.val;}
			@Override
			public boolean mousedown(Coord c, int button) {
				Config.hideCrops.setVal(!this.a);
				gameui().map.removeCustomSprites(1340);
				gameui().map.refreshGobsAll();
				return super.mousedown(c, button);
			}
		}, "Hide crops"));

		hideSettings.addSubentry(new Entry(new CheckBox("Hide walls"){
			{a = Config.hideWalls.val;}
			@Override
			public boolean mousedown(Coord c, int button) {
				Config.hideWalls.setVal(!this.a);
				gameui().map.removeCustomSprites(1340);
				gameui().map.refreshGobsAll();
				return super.mousedown(c, button);
			}
		}, "Hide walls"));

		hideSettings.addSubentry(new Entry(new CheckBox("Hide houses"){
			{a = Config.hideHouses.val;}
			@Override
			public boolean mousedown(Coord c, int button) {
				Config.hideHouses.setVal(!this.a);
				gameui().map.removeCustomSprites(1340);
				gameui().map.refreshGobsAll();
				return super.mousedown(c, button);
			}
		}, "Hide houses"));


		Entry hideRedLbl = new Entry(new Label("Hide red"), "Hide red");
		hideSettings.addSubentry(hideRedLbl);
		hideRedLbl.addSubentry(new Entry(new HSlider(UI.scale(400), 0, 255, Config.hideRed.val) {
			@Override
			public void changed() {
				Config.hideRed.setVal(this.val);
				GobHideBox.HidePol.emat = Pipe.Op.compose(new BaseColor(new java.awt.Color(Config.hideRed.val, Config.hideGreen.val, Config.hideBlue.val, Config.hideAlpha.val)), new States.LineWidth(3));
				gameui().map.removeCustomSprites(1340);
				gameui().map.refreshGobsAll();
				super.changed();
			}
		}, ""));

		Entry hideGreenLbl = new Entry(new Label("Hide green"), "Hide green");
		hideSettings.addSubentry(hideGreenLbl);
		hideGreenLbl.addSubentry(new Entry(new HSlider(UI.scale(400), 0, 255, Config.hideGreen.val) {
			@Override
			public void changed() {
				Config.hideGreen.setVal(this.val);
				GobHideBox.HidePol.emat = Pipe.Op.compose(new BaseColor(new java.awt.Color(Config.hideRed.val, Config.hideGreen.val, Config.hideBlue.val, Config.hideAlpha.val)), new States.LineWidth(3));
				gameui().map.removeCustomSprites(1340);
				gameui().map.refreshGobsAll();
				super.changed();
			}
		}, ""));

		Entry hideBlueLbl = new Entry(new Label("Hide blue"), "Hide blue");
		hideSettings.addSubentry(hideBlueLbl);
		hideBlueLbl.addSubentry(new Entry(new HSlider(UI.scale(400), 0, 255, Config.hideBlue.val) {
			@Override
			public void changed() {
				Config.hideBlue.setVal(this.val);
				GobHideBox.HidePol.emat = Pipe.Op.compose(new BaseColor(new java.awt.Color(Config.hideRed.val, Config.hideGreen.val, Config.hideBlue.val, Config.hideAlpha.val)), new States.LineWidth(3));
				gameui().map.removeCustomSprites(1340);
				gameui().map.refreshGobsAll();
				super.changed();
			}
		}, ""));

		Entry hideAlphaLbl = new Entry(new Label("Hide alpha"), "Hide alpha");
		hideSettings.addSubentry(hideAlphaLbl);
		hideAlphaLbl.addSubentry(new Entry(new HSlider(UI.scale(400), 0, 255, Config.hideAlpha.val) {
			@Override
			public void changed() {
				Config.hideAlpha.setVal(this.val);
				GobHideBox.HidePol.emat = Pipe.Op.compose(new BaseColor(new java.awt.Color(Config.hideRed.val, Config.hideGreen.val, Config.hideBlue.val, Config.hideAlpha.val)), new States.LineWidth(3));
				gameui().map.removeCustomSprites(1340);
				gameui().map.refreshGobsAll();
				super.changed();
			}
		}, ""));

		Entry mapSettings = new Entry(new Label("Map Settings"), "Map Settings");
		((Label)mapSettings.w).setcolor(Color.ORANGE);

		el.root.addSubentry(mapSettings);

		Entry mapExplanation = new Entry(new RichTextBox(UI.scale(500, 50), "With the token you can save markers and access other map features. If you want to share your markers and tokens in the map with your friends, copy and save your friends token here.\nThe map can be accessed at https://hnhmap.vatsul.com/"), "map");
		mapSettings.addSubentry(mapExplanation);

		Entry mapTokenStatus = new Entry(new Label("Map Token:"), "map token");
		mapSettings.addSubentry(mapTokenStatus);

		Entry mapToken = new Entry(new TextEntry(UI.scale(500), Config.mapperToken.val), "map token");
		mapSettings.addSubentry(mapToken);

		Entry mapTokenBtn = new Entry(new Widget(UI.scale(600, 50)) {
			{
				add(new Button(UI.scale(100), "Save token") {
					@Override
					public boolean mousedown(Coord c, int button) {
						String newToken = ((TextEntry)mapToken.w).text;
						try {
							URL url = new URL(Mapper.apiURL + "/token/" + newToken + "/valid");
							Scanner scan = new Scanner(url.openStream());
							if(scan.hasNextLine() && scan.nextLine().equals("Valid")) {
								Config.mapperToken.setVal(newToken);
								((Label)mapTokenStatus.w).setcolor(Color.GREEN);
								((Label)mapTokenStatus.w).settext("Map token was successfully updated!");
							} else {
								((Label)mapTokenStatus.w).setcolor(Color.RED);
								((Label)mapTokenStatus.w).settext("Map token entered was invalid! Changes not saved.");
							}
						} catch(IOException e) {
							((Label)mapTokenStatus.w).setcolor(Color.RED);
							((Label)mapTokenStatus.w).settext("Error while connecting to server! Changes not saved.");
							e.printStackTrace();
						}
						return super.mousedown(c, button);
					}
					{visible = true;}

				}, UI.scale(0, 0));
				this.add(new Button(UI.scale(175), "Copy token to clipboard") {
					@Override
					public boolean mousedown(Coord c, int button) {
						StringSelection ss = new StringSelection(Config.mapperToken.val);
						Toolkit.getDefaultToolkit().getSystemClipboard().setContents(ss, ss);
						return super.mousedown(c, button);
					}
					{visible = true;}
				}, UI.scale(125, 0));
				add(new Button(UI.scale(200), "Paste token from clipboard") {
					@Override
					public boolean mousedown(Coord c, int button) {
						try {
							String newToken = Toolkit.getDefaultToolkit().getSystemClipboard().getContents(null).getTransferData(DataFlavor.stringFlavor).toString();
							if(newToken != null && newToken.length() < 128) {
									URL url = new URL("https://hnhmap.vatsul.com/api/token/" + newToken + "/valid");
									Scanner scan = new Scanner(url.openStream());
									if(scan.hasNextLine() && scan.nextLine().equals("Valid")) {
										Config.mapperToken.setVal(newToken);
										((Label) mapTokenStatus.w).setcolor(Color.GREEN);
										((Label) mapTokenStatus.w).settext("Map token was successfully updated!");
										((TextEntry)mapToken.w).settext(newToken);
									} else {
										((Label) mapTokenStatus.w).setcolor(Color.RED);
										((Label) mapTokenStatus.w).settext("Map token entered was invalid! Changes not saved.");
									}
							}
						} catch(IOException | UnsupportedFlavorException e) {
							((Label) mapTokenStatus.w).setcolor(Color.RED);
							((Label) mapTokenStatus.w).settext("Error while connecting to server! Changes not saved.");
							e.printStackTrace();
						}
						return super.mousedown(c, button);
					}
					{visible = true;}

				}, UI.scale(325, 0));

				add(new Button(UI.scale(60), "Open") {
					@Override
					public boolean mousedown(Coord c, int button) {
						try {
							Toolkit.getDefaultToolkit().beep();
							if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
								Desktop.getDesktop().browse(new URI("https://hnhmap.vatsul.com/map/" + Config.mapperToken.val));
							}
						} catch(URISyntaxException | IOException e) {
						}
						return super.mousedown(c, button);
					}
					{visible = true;}

				}, UI.scale(530, 0));
			}

			@Override
			public void draw(GOut g) {
				for(Widget wdg = child; wdg != null; wdg = next) {
					next = wdg.next;
					wdg.visible = true;
				}
				super.draw(g);
			}
		}, "maptoken save");
		mapSettings.addSubentry(mapTokenBtn);

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
