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

import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;

public class TextEntry extends SIWidget {
    public static final Color defcol = new Color(255, 205, 109), dirtycol = new Color(255, 232, 209);
    public static final Text.Foundry fnd = new Text.Foundry(Text.serif, 12).aa(true);
    public static final BufferedImage lcap = Resource.loadsimg("gfx/hud/text/l");
    public static final BufferedImage rcap = Resource.loadsimg("gfx/hud/text/r");
    public static final BufferedImage mext = Resource.loadimg("gfx/hud/text/m");
    public static final Tex caret = Resource.loadtex("gfx/hud/text/caret");
    public static final int toffx = lcap.getWidth();
    public static final Coord coff = UI.scale(new Coord(-3, 0));
    public static final int wmarg = lcap.getWidth() + rcap.getWidth() + UI.scale(1);
    public boolean dshow = false;
    public LineEdit buf;
    public int sx;
    public boolean pw = false;
    public String text;
    private boolean dirty = false;
    private double focusstart;
    private Text.Line tcache = null;

    @RName("text")
    public static class $_ implements Factory {
	public Widget create(UI ui, Object[] args) {
	    if(args[0] instanceof Coord)
		return(new TextEntry(UI.scale((Coord)args[0]), (String)args[1]));
	    else
		return(new TextEntry(UI.scale((Integer)args[0]), (String)args[1]));
	}
    }

    public void settext(String text) {
	buf.setline(text);
	redraw();
    }

    public void rsettext(String text) {
	buf = new LineEdit(this.text = text) {
		protected void done(String line) {
		    activate(line);
		}
		
		protected void changed() {
		    redraw();
		    TextEntry.this.text = line;
		    TextEntry.this.changed();
		}
	    };
	redraw();
    }

    public void commit() {
	dirty = false;
	redraw();
    }

    public void uimsg(String name, Object... args) {
	if(name == "settext") {
	    settext((String)args[0]);
	} else if(name == "get") {
	    wdgmsg("text", buf.line);
	} else if(name == "pw") {
	    pw = ((Integer)args[0]) != 0;
	} else if(name == "dshow") {
	    dshow = ((Integer)args[0]) != 0;
	} else if(name == "cmt") {
	    commit();
	} else {
	    super.uimsg(name, args);
	}
    }

    protected String dtext() {
	if(pw) {
	    String ret = "";
	    for(int i = 0; i < buf.line.length(); i++)
		ret += "\u2022";
	    return(ret);
	} else {
	    return(buf.line);
	}
    }

    public void draw(BufferedImage img) {
	Graphics g = img.getGraphics();
	String dtext = dtext();
	tcache = fnd.render(dtext, (dshow && dirty)?dirtycol:defcol);
	g.drawImage(mext, 0, 0, sz.x, sz.y, null);

	g.drawImage(tcache.img, toffx - sx, (sz.y - tcache.img.getHeight()) / 2, null);

	g.drawImage(lcap, 0, 0, null);
	g.drawImage(rcap, sz.x - rcap.getWidth(), 0, null);

	g.dispose();
    }

    public void draw(GOut g) {
	super.draw(g);
	if(hasfocus) {
	    int cx = tcache.advance(buf.point);
	    int lx = cx - sx + 1;
	    if(cx < sx) {sx = cx; redraw();}
	    if(cx > sx + (sz.x - wmarg)) {sx = cx - (sz.x - wmarg); redraw();}
	    if(((Utils.rtime() - focusstart) % 1.0) < 0.5)
		g.image(caret, coff.add(toffx + lx, (sz.y - tcache.img.getHeight()) / 2));
	}
    }

    public TextEntry(int w, String deftext) {
	super(new Coord(w, UI.scale(mext.getHeight())));
	rsettext(deftext);
	setcanfocus(true);
    }

    @Deprecated
    public TextEntry(Coord sz, String deftext) {
	this(sz.x, deftext);
    }

    protected void changed() {
	dirty = true;
    }

    public void activate(String text) {
	if(canactivate)
	    wdgmsg("activate", text);
    }

    public boolean gkeytype(KeyEvent ev) {
	activate(buf.line);
	return(true);
    }

    public boolean keydown(KeyEvent e) {
	return(buf.key(e));
    }

    public boolean mousedown(Coord c, int button) {
	parent.setfocus(this);
	if(tcache != null) {
	    buf.point = tcache.charat(c.x + sx);
	}
	return(true);
    }

    public void gotfocus() {
	focusstart = Utils.rtime();
    }

    public void resize(int w) {
	resize(w, sz.y);
    }
}
