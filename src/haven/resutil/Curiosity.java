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

package haven.resutil;

import haven.*;
import java.awt.Graphics;
import java.awt.image.BufferedImage;

public class Curiosity extends ItemInfo.Tip {
    public final int exp, mw, enc, time;

    public Curiosity(Owner owner, int exp, int mw, int enc, int time) {
	super(owner);
	this.exp = exp;
	this.mw = mw;
	this.enc = enc;
	this.time = time;
	if(owner instanceof GItem)
		((GItem) owner).studytime = time / Glob.SERVER_TIME_RATIO;
    }

    static String[] units = {"s", "m", "h", "d"};
    static int[] div = {60, 60, 24};
    static String timefmt(int time) {
	int[] vals = new int[units.length];
	vals[0] = (int)(time / Glob.SERVER_TIME_RATIO); // Use real time
	for(int i = 0; i < div.length; i++) {
	    vals[i + 1] = vals[i] / div[i];
	    vals[i] = vals[i] % div[i];
	}
	StringBuilder buf = new StringBuilder();
	for(int i = units.length - 1; i >= 0; i--) {
	    if(vals[i] > 0) {
		if(buf.length() > 0)
		    buf.append(' ');
		buf.append(vals[i]);
		buf.append(units[i]);
	    }
	}
	return(buf.toString());
    }

    public BufferedImage tipimg() {
	StringBuilder buf = new StringBuilder();
	if(exp > 0)
	    buf.append(String.format("Learning points: $col[192,192,255]{%s}\n", Utils.thformat(exp)));
	if(time > 0)
	    buf.append(String.format("Study time: $col[192,255,192]{%s}" + " (LP/hour: $col[192,192,255]{%d})\n", timefmt(time), (int)(exp / (time / Glob.SERVER_TIME_RATIO / 60 / 60.0))));
	if(mw > 0)
	    buf.append(String.format("Mental weight: $col[255,192,255]{%d}\n", mw));
	if(enc > 0)
	    buf.append(String.format("Experience cost: $col[255,255,192]{%d}\n", enc));
	if(enc > 0)
			buf.append(String.format("LP/EXP: $col[192,192,255]{%s}\n", Utils.thformat(exp/enc)));
		if(time > 0)
			buf.append(String.format("LP/Weight: $col[192,192,255]{%s}\n", Utils.thformat((Math.round(exp/mw)))));
		if(mw > 0 && time > 0)
			buf.append(String.format("LP/Hour/Weight: $col[192,192,255]{%s}\n", Utils.thformat((Math.round(exp/(time / Glob.SERVER_TIME_RATIO / 60 / 60)/mw)))));
		if(owner instanceof GItem) {
			Coord isz = ((GItem)owner).size();
			buf.append(String.format("LP/Hour/Size: $col[192,192,255]{%s}", Utils.thformat(Math.round(exp/(time / Glob.SERVER_TIME_RATIO / 60 / 60))/(isz.x*isz.y))));
		}

		return(RichText.render(buf.toString(), 0).img);
    }
}
