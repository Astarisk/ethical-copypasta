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

import haven.purus.MultiSession;

import java.awt.event.KeyEvent;

public class LoginScreen extends Widget {
    Login cur;
    Text error;
    IButton btn;
    Button optbtn;
    OptWnd opts;
    static Text.Foundry textf, textfs;
    static Tex bg = Resource.loadtex("gfx/loginscr");
    Text progress = null;

    static {
	textf = new Text.Foundry(Text.sans, 16).aa(true);
	textfs = new Text.Foundry(Text.sans, 14).aa(true);
    }

    public LoginScreen() {
	super(bg.sz());
	setfocustab(true);
	add(new Img(bg), Coord.z);
	optbtn = adda(new Button(UI.scale(100), "Options"), UI.scale(10), sz.y - UI.scale(10), 0, 1);
    }

    private static abstract class Login extends Widget {
	abstract Object[] data();
	abstract boolean enter();
    }

    private class Pwbox extends Login {
	TextEntry user, pass;
	CheckBox savepass;

	private Pwbox(String username, boolean save) {
	    setfocustab(true);
	    resize(UI.scale(new Coord(150, 150)));
	    Composer composer = new Composer(this);
	    composer.add(new Label("User name", textf));
	    user = new TextEntry(UI.scale(150), username);
	    composer.add(user);
	    composer.add(UI.scale(5));
	    composer.add(new Label("Password", textf));
	    pass = new TextEntry(UI.scale(150), "");
	    composer.add(pass);
	    pass.pw = true;
	    savepass = new CheckBox("Remember me", true);
	    composer.add(UI.scale(5));
	    composer.add(savepass);
	    savepass.a = save;
	    if(user.text.equals(""))
		setfocus(user);
	    else
		setfocus(pass);
	    LoginScreen.this.add(this, LoginScreen.this.sz.mul(new Coord2d(345 / 800., 310 / 600.)).round());
	}

	public void wdgmsg(Widget sender, String name, Object... args) {
	}

	Object[] data() {
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
	Text label;
	Button btn;
		
	private Tokenbox(String username) {
	    label = textfs.render("Identity is saved for " + username, java.awt.Color.WHITE);
	    btn = new Button(UI.scale(100), "Forget me");
	    resize(UI.scale(new Coord(250, 100)));
	    add(btn, sz.div(2).sub(btn.sz.div(2)));
	    LoginScreen.this.add(this, LoginScreen.this.sz.mul(new Coord2d(295. / 800., 330. / 600.)).round());
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
		
	public void draw(GOut g) {
	    g.image(label.tex(), new Coord((sz.x / 2) - (label.sz().x / 2), 0));
	    super.draw(g);
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
		    protected void depress() {Audio.play(Button.lbtdown.stream());}
		    protected void unpress() {Audio.play(Button.lbtup.stream());}
		}, (int)Math.round(sz.x * 419. / 800.), (int)Math.round(sz.y * 510. / 600.), 0.5, 0.5);
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
	    g.image(error.tex(), UI.scale(new Coord(420, 450)).sub((error.sz().x / 2), 0));
	if(progress != null)
	    g.image(progress.tex(), UI.scale(new Coord(420, 350)).sub((progress.sz().x / 2), 0));
    }

    public boolean keydown(KeyEvent ev) {
	if(ev.getKeyChar() == 10) {
	    if((cur != null) && cur.enter())
		wdgmsg("login", cur.data());
	    return(true);
	}
	return(super.keydown(ev));
    }
}
