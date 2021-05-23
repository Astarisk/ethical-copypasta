/*
 *  This file is part of the Haven & Hearth game client.
 *  Copyright (C) 2009 Fredrik Tolf <fredrik@dolda2000.com>, and
 *                     Björn Johannessen <johannessen.bjorn@gmail.com>
 *
 *  Redistribution and/or modification of this file is subject to the
 *  terms of the GNU Lesser General Public License, version 3, as
 *  published by the Free Software Foundation.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  Other parts of this source tree adhere to other copying
 *  rights. Please see the file `COPYING' in the root directory of the
 *  source tree for details.
 *
 *  A copy the GNU Lesser General Public License is distributed along
 *  with the source tree of which this file is a part in the file
 *  `doc/LPGL-3'. If it is missing for any reason, please see the Free
 *  Software Foundation's website at <http://www.fsf.org/>, or write
 *  to the Free Software Foundation, Inc., 59 Temple Place, Suite 330,
 *  Boston, MA 02111-1307 USA
 */

package haven;

import haven.purus.Credentials;
import haven.purus.MultiSession;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Scanner;
import java.util.concurrent.*;

public class LoginScreen extends Widget {
    public static final Text.Foundry
	textf = new Text.Foundry(Text.sans, 16).aa(true),
	textfs = new Text.Foundry(Text.sans, 14).aa(true);
    public static final Tex bg = Resource.loadtex("gfx/loginscr");
    public static final Position bgc = new Position(UI.scale(420, 300));
    private Login cur;
    private Text error, progress;
    private IButton btn;
    private Button optbtn;
    private OptWnd opts;
    private Button statusbtn;
    private Button pastadiscord, hnhdiscord;
    private ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();


    public LoginScreen() {
	super(bg.sz());
	setfocustab(true);
	add(new Img(bg), Coord.z);
	optbtn = adda(new Button(UI.scale(100), "Options"), pos("cbl").add(10, -10), 0, 1);
	optbtn.setgkey(GameUI.kb_opt);
	add(new Credentials.CredentialsWidget());
	statusbtn = adda(new Button(UI.scale(200), "Initializing..."), sz.x-UI.scale(210), UI.scale(120), 0, 1);
		executorService.scheduleWithFixedDelay(checkStatus,0, 5, TimeUnit.SECONDS);
	pastadiscord = adda(new Button(UI.scale(200), "Pasta Discord"), sz.x-UI.scale(210), UI.scale(40), 0, 1);
	hnhdiscord = adda(new Button(UI.scale(200), "HnH Public Discord"), sz.x-UI.scale(210), UI.scale(80), 0, 1);


	}

    private static abstract class Login extends Widget {
	Login(Coord sz) {super(sz);}

	abstract Object[] data();
	abstract boolean enter();
    }

    private class Pwbox extends Login {
	TextEntry user, pass;
	CheckBox savepass;

	private Pwbox(String username, boolean save) {
	    super(UI.scale(150, 150));
	    setfocustab(true);
	    Widget prev = add(new Label("User name", textf), 0, 0);
	    add(user = new TextEntry(UI.scale(150), username), prev.pos("bl").adds(0, 1));
	    prev = add(new Label("Password", textf), user.pos("bl").adds(0, 10));
	    add(pass = new TextEntry(UI.scale(150), ""), prev.pos("bl").adds(0, 1));
	    pass.pw = true;
	    add(savepass = new CheckBox("Remember me", true), pass.pos("bl").adds(0, 10));
	    savepass.a = save;
	    if(user.text.equals(""))
		setfocus(user);
	    else
		setfocus(pass);
	    savepass.settip("Saving your login does not save your password, but rather " +
			    "a randomly generated token that will be used to log in. " +
			    "You can manage your saved tokens in your Account Settings.",
			    true);
	    LoginScreen.this.adda(this, bgc.adds(0, 10), 0.5, 0.0);
	}

	public void wdgmsg(Widget sender, String name, Object... args) {
	}

	Object[] data() {
		Credentials.saveCredentials(user.text, pass.text);
	    return(new Object[] {new AuthClient.NativeCred(user.text, pass.text), savepass.a});
	}

	boolean enter() {
	    if(user.text.equals("")) {
		setfocus(user);
		return(false);
	    } else if(pass.text.equals("")) {
		setfocus(pass);
		return(false);
	    } else {
		return(true);
	    }
	}

	public boolean globtype(char k, KeyEvent ev) {
	    if((k == 'r') && ((ev.getModifiersEx() & (KeyEvent.META_DOWN_MASK | KeyEvent.ALT_DOWN_MASK)) != 0)) {
		savepass.set(!savepass.a);
		return(true);
	    }
	    return(false);
	}
    }

    private class Tokenbox extends Login {
	Button btn;
		
	private Tokenbox(String username) {
	    super(UI.scale(250, 100));
	    adda(new Label("Identity is saved for " + username, textfs), pos("cmid").y(0), 0.5, 0.0);
	    adda(btn = new Button(UI.scale(100), "Forget me"), pos("cmid"), 0.5, 0.5);
	    LoginScreen.this.adda(this, bgc.adds(0, 30), 0.5, 0.0);
	}
		
	Object[] data() {
	    return(new Object[0]);
	}
		
	boolean enter() {
	    return(true);
	}
		
	public void wdgmsg(Widget sender, String name, Object... args) {
	    if(sender == btn) {
		LoginScreen.this.wdgmsg("forget");
		return;
	    }
	    super.wdgmsg(sender, name, args);
	}
		
	public boolean globtype(char k, KeyEvent ev) {
	    if((k == 'f') && ((ev.getModifiersEx() & (KeyEvent.META_DOWN_MASK | KeyEvent.ALT_DOWN_MASK)) != 0)) {
		LoginScreen.this.wdgmsg("forget");
		return(true);
	    }
	    return(false);
	}
    }

    private void mklogin() {
	synchronized(ui) {
	    adda(btn = new IButton("gfx/hud/buttons/login", "u", "d", "o") {
		    protected void depress() {ui.sfx(Button.clbtdown.stream());}
		    protected void unpress() {ui.sfx(Button.clbtup.stream());}
		}, bgc.adds(0, 210), 0.5, 0.5);
	    progress(null);
	}
    }

    private void error(String error) {
	synchronized(ui) {
	    if(this.error != null)
		this.error = null;
	    if(error != null)
		this.error = textf.render(error, java.awt.Color.RED);
	}
    }

    private void progress(String p) {
	synchronized(ui) {
	    if(progress != null)
		progress = null;
	    if(p != null)
		progress = textf.render(p, java.awt.Color.WHITE);
	}
    }

	@Override
	public void destroy() {
		executorService.shutdown();
		super.destroy();
	}

    private void clear() {
	if(cur != null) {
	    ui.destroy(cur);
	    cur = null;
	    ui.destroy(btn);
	    btn = null;
	}
	progress(null);
    }

    public void wdgmsg(Widget sender, String msg, Object... args) {
	if(sender == btn) {
	    if(cur.enter())
		super.wdgmsg("login", cur.data());
	    return;
	} else if(sender == optbtn) {
	    if(opts == null) {
		opts = adda(new OptWnd(false) {
			public void hide() {
			    /* XXX */
			    reqdestroy();
			}
		    }, sz.div(2), 0.5, 0.5);
	    } else {
		opts.reqdestroy();
		opts = null;
	    }
	    return;
	} else if(sender == opts) {
	    opts.reqdestroy();
	    opts = null;
	} else if(sender == statusbtn) {
		Desktop desktop = Desktop.isDesktopSupported() ? Desktop.getDesktop() : null;
		if(desktop != null && desktop.isSupported(Desktop.Action.BROWSE)) {
			try {
				desktop.browse(new URI("https://www.havenandhearth.com/portal/"));
			} catch (IOException | URISyntaxException e) {
				e.printStackTrace();
			}
		}
	} else if(sender == pastadiscord) {
		Desktop desktop = Desktop.isDesktopSupported() ? Desktop.getDesktop() : null;
		if(desktop != null && desktop.isSupported(Desktop.Action.BROWSE)) {
			try {
				desktop.browse(new URI("https://discord.gg/S8ZPxJmHXS"));
			} catch (IOException | URISyntaxException e) {
				e.printStackTrace();
			}
		}
	} else if(sender == hnhdiscord) {
		Desktop desktop = Desktop.isDesktopSupported() ? Desktop.getDesktop() : null;
		if(desktop != null && desktop.isSupported(Desktop.Action.BROWSE)) {
			try {
				desktop.browse(new URI("https://discord.gg/ctCypnN"));
			} catch (IOException | URISyntaxException e) {
				e.printStackTrace();
			}
		}
	}
	super.wdgmsg(sender, msg, args);
    }

    public void cdestroy(Widget ch) {
	if(ch == opts) {
	    opts = null;
	}
    }

    public void uimsg(String msg, Object... args) {
	synchronized(ui) {
	    if(msg == "passwd") {
		clear();
		cur = new Pwbox((String)args[0], (Boolean)args[1]);
		mklogin();
	    } else if(msg == "token") {
		clear();
		cur = new Tokenbox((String)args[0]);
		mklogin();
	    } else if(msg == "error") {
		error((String)args[0]);
	    } else if(msg == "prg") {
		error(null);
		clear();
		progress((String)args[0]);
	    }
	}
    }

    public void presize() {
	c = parent.sz.div(2).sub(sz.div(2));
    }

    protected void added() {
	presize();
	parent.setfocus(this);
		ui.root.multiSessionWindow = ui.root.add(new MultiSession.MultiSessionWindow());
    }

    public void draw(GOut g) {
	super.draw(g);
	if(error != null)
	    g.aimage(error.tex(), bgc.adds(0, 150), 0.5, 0.0);
	if(progress != null)
	    g.aimage(progress.tex(), bgc.adds(0, 50), 0.5, 0.0);
    }

    public boolean keydown(KeyEvent ev) {
	if(key_act.match(ev)) {
	    if((cur != null) && cur.enter())
		wdgmsg("login", cur.data());
	    return(true);
	}
	return(super.keydown(ev));
    }

    Runnable checkStatus = new Runnable() {
		@Override
		public void run() {
			try {
				URL url = new URL("http://www.havenandhearth.com/portal/index/status");
				Scanner scan = new Scanner(url.openStream());
				while(scan.hasNextLine()) {
					String line = scan.nextLine();
					if(line.contains("h2")) {
						String status = line.substring(line.indexOf("<h2>")+4, line.indexOf("</h2>"));
						if(status.equals("The server is up"))
							statusbtn.change(status, Color.GREEN);
						else
							statusbtn.change(status, Color.RED);
					}
				}
				Thread.sleep(5000);
			} catch(IOException e) {
				e.printStackTrace();
			} catch(InterruptedException e) {
				// Ignore
			}
		}
	};

}
