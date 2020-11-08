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

import java.util.*;
import java.awt.Color;

public class Party {
    Map<Long, Member> memb = new TreeMap<Long, Member>();
    Member leader = null;
    public static final int PD_LIST = 0;
    public static final int PD_LEADER = 1;
    public static final int PD_MEMBER = 2;
    private Glob glob;
	
    public Party(Glob glob) {
	this.glob = glob;
    }
	
    public class Member {
	public final long gobid;
	private Coord2d c = null;
	private double ma = Math.random() * Math.PI * 2;
	private double oa = Double.NaN;
	public Color col = Color.BLACK;

	public Member(long gobid) {
	    this.gobid = gobid;
	}

	public Gob getgob() {
	    return(glob.oc.getgob(gobid));
	}
	
	public Coord2d getc() {
	    Gob gob;
	    try {
		if((gob = getgob()) != null) {
		    this.oa = gob.a;
		    return(new Coord2d(gob.getc()));
		}
	    } catch(Loading e) {}
	    this.oa = Double.NaN;
	    return(c);
	}

	public double geta() {
	    return(Double.isNaN(oa) ? ma : oa);
	}
    }
	
    public void msg(Message msg) {
	while(!msg.eom()) {
	    int type = msg.uint8();
	    if(type == PD_LIST) {
		ArrayList<Long> ids = new ArrayList<Long>();
		while(true) {
		    long id = msg.uint32();
		    if(id == 0xffffffffl)
			break;
		    ids.add(id);
		}
		Map<Long, Member> nmemb = new TreeMap<Long, Member>();
		for(long id : ids) {
		    Member m = memb.get(id);
		    if(m == null)
			m = new Member(id);
		    nmemb.put(id, m);
		}
		long lid = (leader == null)?-1:leader.gobid;
		memb = nmemb;
		leader = memb.get(lid);
	    } else if(type == PD_LEADER) {
		Member m = memb.get(msg.uint32());
		if(m != null)
		    leader = m;
	    } else if(type == PD_MEMBER) {
		Member m = memb.get(msg.uint32());
		Coord2d c = null;
		boolean vis = msg.uint8() == 1;
		if(vis)
		    c = msg.coord().mul(OCache.posres);
		Color col = msg.color();
		if(m != null) {
		    if((m.c != null) && (c != null))
			m.ma = m.c.angle(c);
		    m.c = c;
		    m.col = col;
		}
	    }
	}
    }
}
