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
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.function.*;

import haven.purus.*;
import haven.purus.Config;
import haven.purus.alarms.AlarmManager;
import haven.purus.mapper.Mapper;
import haven.render.*;
import haven.resutil.WaterTile;

public class Gob implements RenderTree.Node, Sprite.Owner, Skeleton.ModOwner, Skeleton.HasPose {
    public Coord2d rc;
    public double a;
    public boolean virtual = false;
    int clprio = 0;
    public long id;
    public final Glob glob;
    Map<Class<? extends GAttrib>, GAttrib> attr = new ConcurrentHashMap<>();
    public final Collection<Overlay> ols = new LinkedBlockingDeque<>();
    public final Collection<RenderTree.Slot> slots = new ArrayList<>(1);
    private final Collection<SetupMod> setupmods = new ArrayList<>();
    private final Collection<ResAttr.Cell<?>> rdata = new LinkedList<ResAttr.Cell<?>>();
    private final Collection<ResAttr.Load> lrdata = new LinkedList<ResAttr.Load>();
    private static final MixColor dFrameDone =  new MixColor(255, 0, 0, 64);
	private static final MixColor dFrameEmpty =  new MixColor(0, 255, 0, 64);
	private static final MixColor ttEmpty =  new MixColor(255, 0, 0, 64);
	private static final MixColor ttDone =  new MixColor(0, 255, 0, 64);
	private static final HashSet<Long> alarmPlayed = new HashSet<Long>();

	public enum Knocked {
		UNKNOWN,
		TRUE,
		FALSE
	}
    public Knocked knocked = Knocked.UNKNOWN;

	public Type type;

    enum Type {
    	PLAYER,
		OTHER
	}

	private static final HashMap<String, Type> types = new HashMap<String, Type>(){{
		put("gfx/borka/body", Type.PLAYER);
    }};

    public static class Overlay implements RenderTree.Node {
	public final int id;
	public final Gob gob;
	public final Indir<Resource> res;
	public MessageBuf sdt;
	public Sprite spr;
	public boolean delign = false;
	private Collection<RenderTree.Slot> slots = null;
	private boolean added = false;


		public Overlay(Gob gob, int id, Indir<Resource> res, Message sdt) {
	    this.gob = gob;
	    this.id = id;
	    this.res = res;
	    this.sdt = new MessageBuf(sdt);
	    this.spr = null;
	}

	public Overlay(Gob gob, Sprite spr) {
	    this.gob = gob;
	    this.id = (int)(Math.random()*Integer.MAX_VALUE);
	    this.res = null;
	    this.sdt = null;
	    this.spr = spr;
	}

	public Overlay(Gob gob, Sprite spr, int id) {
		this.gob = gob;
		this.id = id;
		this.res = null;
		this.sdt = null;
		this.spr = spr;
	}

	private void init() {
	    if(spr == null) {
		spr = Sprite.create(gob, res.get(), sdt);
		if(added && (spr instanceof SetupMod))
		    gob.setupmods.add((SetupMod)spr);
	    }
	    if(slots == null)
		RUtils.multiadd(gob.slots, this);
	}

	private void add0() {
	    if(added)
		throw(new IllegalStateException());
	    if(spr instanceof SetupMod)
		gob.setupmods.add((SetupMod)spr);
	    added = true;
	}

	private void remove0() {
	    if(!added)
		throw(new IllegalStateException());
	    if(slots != null) {
		RUtils.multirem(new ArrayList<>(slots));
		slots = null;
	    }
	    if(spr instanceof SetupMod)
		gob.setupmods.remove(spr);
	    added = false;
	}

	public void remove() {
	    remove0();
	    gob.ols.remove(this);
		gob.updCustom();
	}

	public void added(RenderTree.Slot slot) {
	    slot.add(spr);
	    if(slots == null)
		slots = new ArrayList<>(1);
	    slots.add(slot);
	}

	public void removed(RenderTree.Slot slot) {
	    if(slots != null)
		slots.remove(slot);
	}
    }

    public static interface SetupMod {
	public default Pipe.Op gobstate() {return(null);}
	public default Pipe.Op placestate() {return(null);}
    }

    /* XXX: This whole thing didn't turn out quite as nice as I had
     * hoped, but hopefully it can at least serve as a source of
     * inspiration to redo attributes properly in the future. There
     * have already long been arguments for remaking GAttribs as
     * well. */
    public static class ResAttr {
	public boolean update(Message dat) {
	    return(false);
	}

	public void dispose() {
	}

	public static class Cell<T extends ResAttr> {
	    final Class<T> clsid;
	    Indir<Resource> resid = null;
	    MessageBuf odat;
	    public T attr = null;

	    public Cell(Class<T> clsid) {
		this.clsid = clsid;
	    }

	    public void set(ResAttr attr) {
		if(this.attr != null)
		    this.attr.dispose();
		this.attr = clsid.cast(attr);
	    }
	}

	private static class Load {
	    final Indir<Resource> resid;
	    final MessageBuf dat;

	    Load(Indir<Resource> resid, Message dat) {
		this.resid = resid;
		this.dat = new MessageBuf(dat);
	    }
	}

	@Resource.PublishedCode(name = "gattr", instancer = FactMaker.class)
	public static interface Factory {
	    public ResAttr mkattr(Gob gob, Message dat);
	}

	public static class FactMaker implements Resource.PublishedCode.Instancer {
	    public Factory make(Class<?> cl, Resource ires, Object... argv) {
		if(Factory.class.isAssignableFrom(cl))
		    return(Resource.PublishedCode.Instancer.stdmake(cl.asSubclass(Factory.class), ires, argv));
		if(ResAttr.class.isAssignableFrom(cl)) {
		    try {
			final java.lang.reflect.Constructor<? extends ResAttr> cons = cl.asSubclass(ResAttr.class).getConstructor(Gob.class, Message.class);
			return(new Factory() {
				public ResAttr mkattr(Gob gob, Message dat) {
				    return(Utils.construct(cons, gob, dat));
				}
			    });
		    } catch(NoSuchMethodException e) {
		    }
		}
		return(null);
	    }
	}
    }

    public Gob(Glob glob, Coord2d c, long id) {
	this.glob = glob;
	this.rc = c;
	this.id = id;
	if(id < 0)
	    virtual = true;
		updwait(this::updCustom, waiting -> {});
    }

    public Gob(Glob glob, Coord2d c) {
	this(glob, c, -1);
    }

    public void ctick(double dt) {
	for(GAttrib a : attr.values())
	    a.ctick(dt);
	loadrattr();
	for(Iterator<Overlay> i = ols.iterator(); i.hasNext();) {
	    Overlay ol = i.next();
	    if(ol.slots == null) {
		try {
		    ol.init();
		} catch(Loading e) {}
	    } else {
		boolean done = ol.spr.tick(dt);
		if((!ol.delign || (ol.spr instanceof Sprite.CDel)) && done) {
		    ol.remove0();
		    i.remove();
		}
	    }
	}
	updstate();
	if(virtual && ols.isEmpty() && (getattr(Drawable.class) == null))
	    glob.oc.remove(this);
	if(isPlayer() && this.glob.map.grids != null) {
		MCache.Grid g = this.glob.map.grids.get(rc.floor().div(11*100));
		if(g != null && glob.sess.ui.gui != null && glob.sess.ui.gui.charname != null) {
			Mapper.players.putIfAbsent(glob.sess.ui.gui.charname, new Pair<>(glob.sess.ui.gui.hatname, new Pair(rc.div(11).floor().mod(new Coord(100,100)), g.id)));
		}
	}
    }

    public void gtick(Render g) {
	Drawable d = getattr(Drawable.class);
	if(d != null)
	    d.gtick(g);
	for(Overlay ol : ols) {
	    if(ol.spr != null)
		ol.spr.gtick(g);
	}
    }

    public void addol(Overlay ol, boolean async) {
	if(!async)
	    ol.init();
	ol.add0();
	ols.add(ol);
	updCustom();
    }
    public void addol(Overlay ol) {
	addol(ol, true);
    }
    public void addol(Sprite ol) {
	addol(new Overlay(this, ol));
    }
    public void addol(Indir<Resource> res, Message sdt) {
	addol(new Overlay(this, -1, res, sdt));
    }

    public Overlay findol(int id) {
	for(Overlay ol : ols) {
	    if(ol.id == id)
		return(ol);
	}
	return(null);
    }

    public void dispose() {
	for(GAttrib a : attr.values())
	    a.dispose();
	for(ResAttr.Cell rd : rdata) {
	    if(rd.attr != null)
		rd.attr.dispose();
	}
    }

    public void move(Coord2d c, double a) {
	Moving m = getattr(Moving.class);
	if(m != null)
	    m.move(c);
	this.rc = c;
	double olda = this.a;
	this.a = a;
		if(isPlayer() && this.a != olda &&  glob.sess.ui != null && glob.sess.ui.gui != null && glob.sess.ui.gui.map != null && glob.sess.ui.gui.map.cp != null) {
				if(Math.abs(a - (glob.sess.ui.gui.map.cp.rte[0].angle(this.rc) + Math.PI)) > Math.PI/180) {
					glob.sess.ui.gui.map.wrongdir = true;
				} else {
					glob.sess.ui.gui.map.wrongdir = false;
				}
		}
		if(isPlayer() && glob.map != null) {
			Coord toc = this.rc.floor(MCache.tilesz).div(MCache.cmaps);
			for(int i=-1; i<=1; i++)
				for(int j=-1; j<=1; j++)
					try {glob.map.getgrid(toc.add(i,j));} catch(MCache.LoadingMap l) {}
		}
    }

    public boolean isPlayer() {
    	return (glob.sess.ui != null && glob.sess.ui.gui != null && this.id == glob.sess.ui.gui.plid);
	}

    public Coord3f getc() {
	Moving m = getattr(Moving.class);
	Coord3f ret = (m != null)?m.getc():getrc();
	DrawOffset df = getattr(DrawOffset.class);
	if(df != null)
	    ret = ret.add(df.off);
		if(knocked != Knocked.UNKNOWN)
			try {
				if(glob.map.tiler(glob.map.gettile(new Coord2d(ret).floor(MCache.tilesz))) instanceof WaterTile) {
					ret.z += 5;
				}
			} catch(Loading l) {}

		return(ret);
    }

    public Coord3f getrc() {
	return(glob.map.getzp(rc));
    }

    protected Pipe.Op getmapstate(Coord3f pc) {
	Tiler tile = glob.map.tiler(glob.map.gettile(new Coord2d(pc).floor(MCache.tilesz)));
	return(tile.drawstate(glob, pc));
    }

    private Class<? extends GAttrib> attrclass(Class<? extends GAttrib> cl) {
	while(true) {
	    Class<?> p = cl.getSuperclass();
	    if(p == GAttrib.class)
		return(cl);
	    cl = p.asSubclass(GAttrib.class);
	}
    }

    public <C extends GAttrib> C getattr(Class<C> c) {
	GAttrib attr = this.attr.get(attrclass(c));
	if(!c.isInstance(attr))
	    return(null);
	return(c.cast(attr));
    }

    private void setattr(Class<? extends GAttrib> ac, GAttrib a) {
	GAttrib prev = attr.remove(ac);
	if(prev != null) {
		if((prev instanceof RenderTree.Node) && (prev.slots != null))
		RUtils.multirem(new ArrayList<>(prev.slots));
	    if(prev instanceof SetupMod)
		setupmods.remove(prev);
	}
	if(a != null) {
	    if(a instanceof RenderTree.Node && !a.hide) {
			try {
		    RUtils.multiadd(this.slots, (RenderTree.Node)a);
		} catch(Loading l) {
		    if(prev instanceof RenderTree.Node && !prev.hide) {
			RUtils.multiadd(this.slots, (RenderTree.Node)prev);
			attr.put(ac, prev);
		    }
		    if(prev instanceof SetupMod)
			setupmods.add((SetupMod)prev);
		    throw(l);
		}
	    }
	    if(a instanceof SetupMod)
		setupmods.add((SetupMod)a);
	    attr.put(ac, a);
	}
	if(prev != null)
	    prev.dispose();
	if((ac == Drawable.class && a != prev) || ac == KinInfo.class)
		updCustom();
    }

    public void setattr(GAttrib a) {
	setattr(attrclass(a.getClass()), a);
    }

    public void delattr(Class<? extends GAttrib> c) {
	setattr(attrclass(c), null);
    }

    private Class<? extends ResAttr> rattrclass(Class<? extends ResAttr> cl) {
	while(true) {
	    Class<?> p = cl.getSuperclass();
	    if(p == ResAttr.class)
		return(cl);
	    cl = p.asSubclass(ResAttr.class);
	}
    }

    @SuppressWarnings("unchecked")
    public <T extends ResAttr> ResAttr.Cell<T> getrattr(Class<T> c) {
	for(ResAttr.Cell<?> rd : rdata) {
	    if(rd.clsid == c)
		return((ResAttr.Cell<T>)rd);
	}
	ResAttr.Cell<T> rd = new ResAttr.Cell<T>(c);
	rdata.add(rd);
	return(rd);
    }

    public static <T extends ResAttr> ResAttr.Cell<T> getrattr(Object obj, Class<T> c) {
	if(!(obj instanceof Gob))
	    return(new ResAttr.Cell<T>(c));
	return(((Gob)obj).getrattr(c));
    }

    private void loadrattr() {
	boolean upd = false;
	for(Iterator<ResAttr.Load> i = lrdata.iterator(); i.hasNext();) {
	    ResAttr.Load rd = i.next();
	    ResAttr attr;
	    try {
		attr = rd.resid.get().getcode(ResAttr.Factory.class, true).mkattr(this, rd.dat.clone());
	    } catch(Loading l) {
		continue;
	    }
	    ResAttr.Cell<?> rc = getrattr(rattrclass(attr.getClass()));
	    if(rc.resid == null)
		rc.resid = rd.resid;
	    else if(rc.resid != rd.resid)
		throw(new RuntimeException("Conflicting resattr resource IDs on " + rc.clsid + ": " + rc.resid + " -> " + rd.resid));
	    rc.odat = rd.dat;
	    rc.set(attr);
	    i.remove();
	    upd = true;
	}
    }

    public void setrattr(Indir<Resource> resid, Message dat) {
	for(Iterator<ResAttr.Cell<?>> i = rdata.iterator(); i.hasNext();) {
	    ResAttr.Cell<?> rd = i.next();
	    if(rd.resid == resid) {
		if(dat.equals(rd.odat))
		    return;
		if((rd.attr != null) && rd.attr.update(dat))
		    return;
		break;
	    }
	}
	for(Iterator<ResAttr.Load> i = lrdata.iterator(); i.hasNext();) {
	    ResAttr.Load rd = i.next();
	    if(rd.resid == resid) {
		i.remove();
		break;
	    }
	}
	lrdata.add(new ResAttr.Load(resid, dat));
	loadrattr();
    }

    public void delrattr(Indir<Resource> resid) {
	for(Iterator<ResAttr.Cell<?>> i = rdata.iterator(); i.hasNext();) {
	    ResAttr.Cell<?> rd = i.next();
	    if(rd.resid == resid) {
		i.remove();
		rd.attr.dispose();
		break;
	    }
	}
	for(Iterator<ResAttr.Load> i = lrdata.iterator(); i.hasNext();) {
	    ResAttr.Load rd = i.next();
	    if(rd.resid == resid) {
		i.remove();
		break;
	    }
	}
    }

    public void draw(GOut g) {}

    public Type determineType(String name) {
    	return types.get(name);
	}

    public static class GobClick extends Clickable {
	public final Gob gob;

	public GobClick(Gob gob) {
	    this.gob = gob;
	}

	public Object[] clickargs(ClickData cd) {
	    Object[] ret = {0, (int)gob.id, gob.rc.floor(OCache.posres), 0, -1};
	    for(Object node : cd.array()) {
		if(node instanceof Gob.Overlay) {
		    ret[0] = 1;
		    ret[3] = ((Gob.Overlay)node).id;
		}
		if(node instanceof FastMesh.ResourceMesh)
		    ret[4] = ((FastMesh.ResourceMesh)node).id;
	    }
	    return(ret);
	}

	public String toString() {
	    return(String.format("#<gob-click %d %s>", gob.id, gob.getres()));
	}
    }

    private class GobState implements Pipe.Op {
	final Pipe.Op mods;

	private GobState() {
	    if(setupmods.isEmpty()) {
		this.mods = null;
	    } else {
		Pipe.Op[] mods = new Pipe.Op[setupmods.size()];
		int n = 0;
		for(SetupMod mod : setupmods) {
		    if((mods[n] = mod.gobstate()) != null)
			n++;
		}
		this.mods = (n > 0) ? Pipe.Op.compose(mods) : null;
	    }
	}

	public void apply(Pipe buf) {
	    if(!virtual)
		buf.prep(new GobClick(Gob.this));
	    buf.prep(new TickList.Monitor(Gob.this));
	    if(mods != null)
		buf.prep(mods);
	}

	public boolean equals(GobState that) {
	    return(Utils.eq(this.mods, that.mods));
	}
	public boolean equals(Object o) {
	    return((o instanceof GobState) && equals((GobState)o));
	}
    }
    private GobState curstate = null;
    private GobState curstate() {
	if(curstate == null)
	    curstate = new GobState();
	return(curstate);
    }

    private void updstate() {
	GobState nst;
	try {
	    nst = new GobState();
	} catch(Loading l) {
	    return;
	}
	if(!Utils.eq(nst, curstate)) {
	    for(RenderTree.Slot slot : slots)
		slot.ostate(nst);
	    this.curstate = nst;
	}
    }

    public void updCustom() {
    	if(updateseq == 0)
    		return;
    	new Runnable() {
			@Override
			public void run() {
				boolean hide = false;
				try {
					Resource res = Gob.this.getres();
					if(res != null) {
						if(!alarmPlayed.contains(id)) {
							if(AlarmManager.play(res.name, Gob.this))
								alarmPlayed.add(id);
							if(type == Gob.Type.PLAYER && id != glob.sess.ui.gui.plid) {
								KinInfo kin = getattr(KinInfo.class);
								if(kin == null) {
									Audio.play(Resource.local().loadwait("sfx/alarms/whitePlayer"));
									alarmPlayed.add(id);
								} else if(kin.group == 2) {
									alarmPlayed.add(id);
									Audio.play(Resource.local().loadwait("sfx/alarms/redPlayer"));
								}
							}
						}
						if(res.name.startsWith("gfx/kritter")) {
							Overlay ol = findol(1341);
							if(!Config.animalRads.val.containsKey(res.name)) {
								Config.animalRads.val.put(res.name, true);
								Config.animalRads.setVal(Config.animalRads.val);
							}
							if(Config.animalRadiuses.val && Config.animalRads.val.getOrDefault(res.name, true) && knocked != Knocked.TRUE) {
								if(ol == null) {
									addol(new Overlay(Gob.this, new AnimalRad(Gob.this, null, 5 * MCache.tilesz2.y), 1341));
								}
							} else if(ol != null) {
								ol.remove();
							}
						} else if(res.name.startsWith("gfx/borka/body")) {
							Overlay ol = findol(1342);
							if(Config.playerRadiuses.val && !isPlayer()) {
								if(ol == null) {
									KinInfo kin = getattr(KinInfo.class);
									if(kin == null)
										addol(new Overlay(Gob.this, new PlayerRad(Gob.this, null, Color.WHITE), 1342));
									else
										addol(new Overlay(Gob.this, new PlayerRad(Gob.this, null, BuddyWnd.gc[kin.group]), 1342));
									updstate();
								}
							} else if(ol != null) {
								ol.remove();
							}
						}
						String resname = res.name;
						if(Config.bbDisplayState.val > 0) {
							BoundingBox bb = BoundingBox.getBoundingBox(Gob.this);
							Overlay ol = findol(1339);
							if(ol != null && bb == null) {
								ol.remove();
							} else if(ol == null && bb != null) {
								addol(new Overlay(Gob.this, new GobBoundingBox(Gob.this, bb), 1339));
							}
						}
						Drawable d = getattr(Drawable.class);
						if(Config.hideToggle.val && d != null) {
							if(Config.hideTrees.val && res.name.startsWith("gfx/terobjs/trees") && !res.name.endsWith("log") && !res.name.endsWith("oldtrunk")) {
								hide = true;
							} else if(Config.hideHouses.val && (res.name.endsWith("/stonemansion") || res.name.endsWith("/logcabin") || res.name.endsWith("/greathall") || res.name.endsWith("/stonestead") || res.name.endsWith("/timberhouse") || res.name.endsWith("stonetower") || res.name.endsWith("windmill"))) {
								hide = true;
							} else if(Config.hideWalls.val && res.name.startsWith("gfx/terobjs/arch/pali") && !res.name.equals("gfx/terobjs/arch/palisadegate") && !res.name.equals("gfx/terobjs/arch/palisadebiggate") || res.name.startsWith("gfx/terobjs/arch/brick") && !res.name.equals("gfx/terobjs/arch/brickwallgate") && !res.name.equals("gfx/terobjs/arch/brickwallbiggate") || res.name.startsWith("gfx/terobjs/arch/pole") && !res.name.equals("gfx/terobjs/arch/polegate") && !res.name.equals("gfx/terobjs/arch/polebiggate")) {
								hide = true;
							} else if(Config.hideBushes.val && res.name.startsWith("gfx/terobjs/bushes")) {
								hide = true;
							} else if(Config.hideCrops.val && res.name.startsWith("gfx/terobjs/plants") && !res.name.endsWith("trellis")) {
								hide = true;
							}
						}
						if(d != null && d.hide != hide) {
							d.hide = hide;
							glob.loader.defer(() -> {
								synchronized(Gob.this) {
									setattr(d);
								}
							}, null);
							Overlay ol = findol(1340);
							if(!hide && ol != null) {
								ol.remove();
							} else if(hide && ol == null) {
								BoundingBox bb = BoundingBox.getBoundingBox(Gob.this);
								addol(new Overlay(Gob.this, new GobHideBox(bb), 1340));
							}
						}
						ResDrawable rd = getattr(ResDrawable.class);
						if(Config.ttfHighlight.val) {
							if(res.name.equals("gfx/terobjs/dframe")) {
								boolean done = true;
								boolean empty = true;
								for(Overlay ol : ols) {
									if(ol.res != null) {
										Resource olres = ol.res.get();
										if(olres.name.endsWith("-blood") || olres.name.endsWith("-windweed") || olres.name.endsWith("-fishraw")) {
											done = false;
										}
										if(olres.name.startsWith("gfx/terobjs/dframe-")) {
											empty = false;
										}
									}
								}
								synchronized(this) {
									if(empty)
										glob.loader.defer(() -> {
											synchronized(Gob.this) {
												setattr(new GobColor(Gob.this, dFrameEmpty));
											}
										}, null);
									else if(done)
										glob.loader.defer(() -> {
											synchronized(Gob.this) {
												setattr(new GobColor(Gob.this, dFrameDone));
											}
										}, null);
									else
										glob.loader.defer(() -> {
											synchronized(Gob.this) {
												delattr(GobColor.class);
											}
										}, null);
								}
							} else if(res.name.equals("gfx/terobjs/ttub")) {
								if(rd != null) {
									int r = ((ResDrawable) rd).sdt.peekrbuf(0);
									if((r & (0x8)) == 0x8) {
										glob.loader.defer(() -> {
											synchronized(Gob.this) {
												setattr(new GobColor(Gob.this, ttDone));
											}
										}, null);
									} else if((r & (0x4)) == 0 || r == 5) {
										glob.loader.defer(() -> {
											synchronized(Gob.this) {
												setattr(new GobColor(Gob.this, ttEmpty));
											}
										}, null);
									} else {
										glob.loader.defer(() -> {
											synchronized(Gob.this) {
												delattr(GobColor.class);
											}
										}, null);
									}
								}
							}
						} else {
							delattr(GobColor.class);
						}

						if(Config.growthStages.val) {
							if(resname.startsWith("gfx/terobjs/bushes") || (resname.startsWith("gfx/terobjs/trees") && !resname.endsWith("log") && !resname.endsWith("oldtrunk"))) {
								if(rd != null) {
									int fscale = rd.sdt.peekrbuf(1);
									if(fscale != -1) {
										Gob.Overlay ovl = findol(1337);
										synchronized(this) {
											if(ovl == null) {
												addol(new Gob.Overlay(Gob.this, new GobText(Gob.this, fscale + "%", Color.WHITE, 5), 1337));
											} else if(!((GobText) ovl.spr).text.equals(fscale + "%")) {
												ovl.remove();
												addol(new Gob.Overlay(Gob.this, new GobText(Gob.this, fscale + "%", Color.WHITE, 5), 1337));
											}
										}
									}
								}
							} else if(resname.startsWith("gfx/terobjs/plants") && !resname.endsWith("trellis") && rd != null) {
								int stage = rd.sdt.peekrbuf(0);
								if(cropstgmaxval == 0) {
									for(FastMesh.MeshRes layer : res.layers(FastMesh.MeshRes.class)) {
										int stg = layer.id / 10;
										if(stg > cropstgmaxval)
											cropstgmaxval = stg;
									}
								}
								Overlay ol = findol(1338);
								String text;
								Color col;
								if(resname.endsWith("/fallowplant")) {
									col = Color.GRAY;
									text = "-1";
								} else if(stage == cropstgmaxval) {
									col = Color.GREEN;
									text = stage + "/" + cropstgmaxval;
								} else if(stage == 0) {
									col = Color.RED;
									text = stage + "/" + cropstgmaxval;
								} else {
									col = Color.YELLOW;
									text = stage + "/" + cropstgmaxval;
								}
								synchronized(this) {
									if(ol == null) {
										addol(new Gob.Overlay(Gob.this, new GobText(Gob.this, text, col, -4), 1338));
									} else if(!((GobText) ol.spr).text.equals(text)) {
										ol.remove();
										addol(new Gob.Overlay(Gob.this, new GobText(Gob.this, text, col, -4), 1338));
									}
								}
							}
						}
					}
				} catch(Loading l) {
					l.waitfor(this, waiting -> {});
				}
			}
		}.run();
	}

    public void added(RenderTree.Slot slot) {
    	synchronized(this) {
			slot.ostate(curstate());
			for(Overlay ol : ols) {
				if(ol.slots != null)
					slot.add(ol);
			}
			for(GAttrib a : attr.values()) {
				if(a instanceof RenderTree.Node && !a.hide)
					slot.add((RenderTree.Node) a);
			}
			slots.add(slot);
		}
    }

    public void removed(RenderTree.Slot slot) {
	slots.remove(slot);
    }

    private Waitable.Queue updwait = null;
    private int updateseq = 0;
	int cropstgmaxval = 0;
	void updated() {
	synchronized(this) {
		updateseq++;
	    if(updwait != null)
		updwait.wnotify();
		}
    }

    public void updwait(Runnable callback, Consumer<Waitable.Waiting> reg) {
	/* Caller should probably synchronize on this already for a
	 * call like this to even be meaningful, but just in case. */
	synchronized(this) {
	    if(updwait == null)
		updwait = new Waitable.Queue();
	    reg.accept(updwait.add(callback));
	}
    }

    public static class DataLoading extends Loading {
	public final transient Gob gob;
	public final int updseq;

	/* It would be assumed that the caller has synchronized on gob
	 * while creating this exception. */
	public DataLoading(Gob gob, String message) {
	    super(message);
	    this.gob = gob;
	    this.updseq = gob.updateseq;
	}

	public void waitfor(Runnable callback, Consumer<Waitable.Waiting> reg) {
	    synchronized(gob) {
		if(gob.updateseq != this.updseq) {
		    reg.accept(Waitable.Waiting.dummy);
		    callback.run();
		} else {
		    gob.updwait(callback, reg);
		}
	    }
	}
    }

    public Random mkrandoom() {
	return(Utils.mkrandoom(id));
    }

    public Resource getres() {
	Drawable d = getattr(Drawable.class);
	if(d != null)
	    return(d.getres());
	return(null);
    }

    public Skeleton.Pose getpose() {
	Drawable d = getattr(Drawable.class);
	if(d != null)
	    return(d.getpose());
	return(null);
    }

    private static final ClassResolver<Gob> ctxr = new ClassResolver<Gob>()
	.add(Glob.class, g -> g.glob)
	.add(Session.class, g -> g.glob.sess);
    public <T> T context(Class<T> cl) {return(ctxr.context(cl, this));}

    @Deprecated
    public Glob glob() {return(context(Glob.class));}

    /* Because generic functions are too nice a thing for Java. */
    public double getv() {
	Moving m = getattr(Moving.class);
	if(m == null)
	    return(0);
	return(m.getv());
    }

    public class Placed implements RenderTree.Node, TickList.Ticking, TickList.TickNode {
	private final Collection<RenderTree.Slot> slots = new ArrayList<>(1);
	private Placement cur;

	private Placed() {}

	private class Placement implements Pipe.Op {
	    final Pipe.Op flw, tilestate, mods;
	    final Coord3f oc, rc;
	    final double a;

	    Placement() {
		try {
		    Following flw = Gob.this.getattr(Following.class);
		    Pipe.Op flwxf = (flw == null) ? null : flw.xf();
		    Pipe.Op tilestate = null;
		    if(flwxf == null) {
			Coord3f oc = Gob.this.getc();
			Coord3f rc = new Coord3f(oc);
			rc.y = -rc.y;
				this.flw = null;
			this.oc = oc;
			this.rc = rc;
				this.a = Gob.this.a;
			tilestate = Gob.this.getmapstate(oc);
		    } else {
			this.flw = flwxf;
			this.oc = this.rc = null;
			this.a = Double.NaN;
		    }
		    this.tilestate = tilestate;
		    if(setupmods.isEmpty()) {
			this.mods = null;
		    } else {
			Pipe.Op[] mods = new Pipe.Op[setupmods.size()];
			int n = 0;
			for(SetupMod mod : setupmods) {
			    if((mods[n] = mod.placestate()) != null)
				n++;
			}
			this.mods = (n > 0) ? Pipe.Op.compose(mods) : null;
		    }
		} catch(Loading bl) {
		    throw(new Loading(bl) {
			    public String getMessage() {return(bl.getMessage());}

			    public void waitfor(Runnable callback, Consumer<Waitable.Waiting> reg) {
				Waitable.or(callback, reg, bl, Gob.this::updwait);
			    }
			});
		}
		}

	    public boolean equals(Placement that) {
		if(this.flw != null) {
		    if(!Utils.eq(this.flw, that.flw))
			return(false);
		} else {
		    if(!(Utils.eq(this.oc, that.oc) && (this.a == that.a)))
			return(false);
		}
		if(!Utils.eq(this.tilestate, that.tilestate))
		    return(false);
		if(!Utils.eq(this.mods, that.mods))
		    return(false);
		return(true);
	    }

	    public boolean equals(Object o) {
		return((o instanceof Placement) && equals((Placement)o));
	    }

	    Pipe.Op gndst = null;
	    public void apply(Pipe buf) {
		if(this.flw != null) {
		    this.flw.apply(buf);
		} else {
		    if(gndst == null)
			gndst = Pipe.Op.compose(new Location(Transform.makexlate(new Matrix4f(), this.rc), "gobx"),
						new Location(Transform.makerot(new Matrix4f(), Coord3f.zu, (float)-this.a), "gob"));
		    gndst.apply(buf);
		}
		if(tilestate != null)
		    tilestate.apply(buf);
		if(mods != null)
		    mods.apply(buf);
	    }
	}

	public Pipe.Op placement() {
	    return(new Placement());
	}

	public void autotick(double dt) {
	    synchronized(Gob.this) {
		Placement np;
		try {
		    np = new Placement();
		} catch(Loading l) {
		    return;
		}
		if(!Utils.eq(this.cur, np))
		    update(np);
		}
	}

	private void update(Placement np) {
	    for(RenderTree.Slot slot : slots)
		slot.ostate(np);
	    this.cur = np;
	}

	public void added(RenderTree.Slot slot) {
	    slot.ostate(curplace());
	    slot.add(Gob.this);
	    slots.add(slot);
	}

	public void removed(RenderTree.Slot slot) {
	    slots.remove(slot);
	}

	public Pipe.Op curplace() {
	    if(cur == null)
		cur = new Placement();
	    return(cur);
	}

	public Coord3f getc() {
	    return((this.cur != null) ? this.cur.oc : null);
	}

	public TickList.Ticking ticker() {return(this);}
    }
    public final Placed placed = new Placed();

	public LinMove getLinMove() {
		LinMove lm = getattr(LinMove.class);
		if (lm != null)
			return lm;

		Following follow = getattr(Following.class);
		if (follow != null)
			return follow.tgt().getattr(LinMove.class);

		return null;
	}

}
