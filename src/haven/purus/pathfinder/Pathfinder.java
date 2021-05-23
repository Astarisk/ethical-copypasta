package haven.purus.pathfinder;

import haven.*;
import haven.purus.BoundingBox;
import haven.purus.ClickPath;
import haven.purus.pbot.api.PBotUtils;
import haven.resutil.Ridges;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;
import java.util.stream.Collectors;

public class Pathfinder {

	private static final boolean PF_DEBUG = true;

	private static HashSet<String> inaccessibleTiles = new HashSet<String>() {{
		add("gfx/tiles/nil");
		add("gfx/tiles/deep");
		add("gfx/tiles/cave");
	}};

	private static HashSet<String> accessibleTilesBoating = new HashSet<String>() {{
		add("gfx/tiles/water");
		add("gfx/tiles/deep");
		add("gfx/tiles/odeep");
		add("gfx/tiles/odeeper");
	}};


		private static HashMap<String, Coord2d> doorOffsets = new HashMap<String, Coord2d>() {{
		put("gfx/terobjs/arch/windmill", new Coord2d(0, 27).add(0, MCache.tilesz.div(2.0).y));
		put("gfx/terobjs/arch/stonemansion", new Coord2d(49, 0).add(MCache.tilesz.div(2.0).x, 0));
		put("gfx/terobjs/arch/stonestead", new Coord2d(46, 0).add(MCache.tilesz.div(2.0).x, 0));
		put("gfx/terobjs/arch/logcabin", new Coord2d(22, 0).add(MCache.tilesz.div(2.0).x, 0));
		put("gfx/terobjs/arch/stonetower", new Coord2d(37, 0).add(MCache.tilesz.div(2.0).x, 0));
		put("gfx/terobjs/arch/greathall", new Coord2d(77, 0).add(MCache.tilesz.div(2.0).x, 0));
		put("gfx/terobjs/arch/timberhouse", new Coord2d(33, 0).add(MCache.tilesz.div(2.0).x, 0));
	}};

	private static void setGridTile(int x, int y, int markId, int[][] grid) {
		if(x > 0 && y > 0 && x < 1100 && y < 1100)
			grid[x][y] = markId;
	}

	// http://members.chello.at/~easyfilter/bresenham.html
	private static void drawCircle(int xm, int ym, int r, int markId, int[][] grid) {
		int x = -r, y = 0, err = 2-2*r;
		do {
			setGridTile(xm-x, ym+y, markId, grid);
			setGridTile(xm-y, ym-x, markId, grid);
			setGridTile(xm+x, ym-y, markId, grid);
			setGridTile(xm+y, ym+x, markId, grid);
			r = err;
			if(r <= y) err += ++y*2+1;
			if(r > x || err > y)
				err += ++x*2+1;
		} while(x < 0);
	}

	// http://members.chello.at/~easyfilter/bresenham.html
	private static void drawLine(int x0, int y0, int x1, int y1, double wd, int markId, int[][] grid) {
		int dx = Math.abs(x1-x0), sx = x0 < x1 ? 1 : -1;
		int dy = Math.abs(y1-y0), sy = y0 < y1 ? 1 : -1;
		int err = dx-dy, e2, x2, y2;
		double ed = dx+dy == 0 ? 1 : Math.sqrt((double) dx * dx + (double) dy * dy);

		for(wd = (wd+1)/2;;) {
			setGridTile(x0, y0, markId, grid);
			e2 = err; x2 = x0;
			if(2*e2 >= -dx) {
				for(e2 += dy, y2 = y0; e2 < ed*wd && (y1 != y2 || dx > dy); e2 += dx) {
					setGridTile(x0, y2 += sy, markId, grid);
				}
				if(x0 == x1)
					break;
				e2 = err; err -= dy; x0 += sx;
			}
			if(2*e2 <= dy) {
				for(e2 = dx-e2; e2 < ed*wd && (x1 != x2 || dx < dy); e2 += dy) {
					setGridTile(x2 += sx, y0, markId, grid);
				}
				if(y0 == y1)
					break;
				err += dx; y0 += sy;
			}
		}
	}

	// http://members.chello.at/~easyfilter/bresenham.html
	private static boolean lineIntersects(int x0, int y0, int x1, int y1, int[][] grid) {
		int dx = Math.abs(x1-x0), sx = x0 < x1 ? 1 : -1;
		int dy = -Math.abs(y1-y0), sy= y0<y1 ? 1 : -1;
		int err = dx+dy, e2;
		while(true) {
			if(grid[x0][y0] == 1)
				return true;
			if(x0==x1 && y0==y1)
				return false;
			e2 = 2*err;
			if(e2 >= dy) {
				err += dy;
				x0 += sx;
			}
			if(e2 <= dx) {
				err += dx;
				y0 += sy;
			}
		}
	}

	private static boolean lineIntersects(Coord c1, Coord c2, int[][] grid) {
		return lineIntersects(c1.x, c1.y, c2.x, c2.y, grid);
	}

	private static void markPolygon(BoundingBox.Polygon pol, int markId, int wd, int cr, int[][] grid) {
		Coord prev = pol.vertices.get(pol.vertices.size()-1).round().add(50*11, 50*11);
		for(Coord2d vert: pol.vertices) {
			Coord c  = vert.round().add(50*11, 50*11);
			Coord2d onc = new Coord2d(c.sub(prev)).rotate(Math.PI/2);
			onc = onc.div(onc.dist(Coord2d.z));
			for(int i=1; i<=wd; i++) {
				Coord2d nc = onc.mul(i);
				drawLine(new Coord2d(prev).add(nc).round().x, new Coord2d(prev).add(nc).round().y, new Coord2d(c).add(nc).round().x, new Coord2d(c).add(nc).round().y, 1.0, markId, grid);

				nc = nc.mul(-1);
				drawLine(new Coord2d(prev).add(nc).round().x, new Coord2d(prev).add(nc).round().y, new Coord2d(c).add(nc).round().x, new Coord2d(c).add(nc).round().y, 1.0, markId, grid);
			}

			drawLine(prev.x, prev.y, c.x, c.y, 1.0, markId, grid);
			prev = c;
		}
		for(Coord2d vert: pol.vertices) {
			Coord c  = vert.round().add(50*11, 50*11);
			for(int i=1; i<=cr; i++)
				drawCircle(c.x, c.y, i, markId, grid);
		}
	}

	private static void markPolygon2(BoundingBox.Polygon pol, int[][] grid) {
		Coord prev = pol.vertices.get(pol.vertices.size()-1).round().add(50*11, 50*11);
		for(Coord2d vert: pol.vertices) {
			Coord c  = vert.round().add(50*11, 50*11);
			drawCircle(c.x, c.y, 2, 0, grid);
			drawCircle(c.x, c.y, 1, 0, grid);
		}
		for(Coord2d vert: pol.vertices) {
			Coord c  = vert.round().add(50*11, 50*11);
			drawLine(prev.x, prev.y, c.x, c.y, 1.0, -1, grid);
			prev = c;
		}
		for(Coord2d vert: pol.vertices) {
			Coord c  = vert.round().add(50*11, 50*11);
			drawCircle(c.x, c.y, 1, -1, grid);
		}
	}

	private static void markTileInaccessible(Coord c, int[][] grid) {
		Coord2d rc = c.mul(MCache.tilesz);
		ArrayList<Coord2d> plist = new ArrayList<>();
		plist.add(rc);
		plist.add(rc.add(MCache.tilesz.x, 0));
		plist.add(rc.add(MCache.tilesz.x, MCache.tilesz.y));
		plist.add(rc.add(0, MCache.tilesz.y));
		markPolygon(new BoundingBox.Polygon(plist), 1, 0, 0, grid);
	}

	public static boolean isMyBoat(Gob gob, Gob player) {
		while(true) {
			try {
				Resource res = gob.getres();
				if(res == null)
					return false;
				if(gob.getres().name.endsWith("e/rowboat") || gob.getres().name.endsWith("e/dugout")) {
					Coord3f gobLoc = gob.getc();
					Coord3f plLoc = player.getc(); // Z levels are same if lifted
					if(gobLoc.z != plLoc.z && new Coord2d(gobLoc).dist(new Coord2d(plLoc)) < 0.1) {
						return true;
					}
				}
				return false;
			} catch(Loading l) {
				Thread.onSpinWait();
			}
		}
	}

	public static void run(Coord2d target, Gob destGob, int button, int mod, int meshid, String action, GameUI gui) {
		run(target, destGob, button, mod, -1, meshid, action, gui);
	}

	ExecutorService ex = Executors.newSingleThreadScheduledExecutor();

	public static void run(Coord2d target, Gob destGob, int button, int mod, int overlay, int meshid, String action, GameUI gui) {
		if(gui.pathfinder != null)
			gui.pathfinder.stop();
		gui.map.pf_route_found = new FutureTask<Boolean>(() -> {
			BufferedImage bi;
			Graphics g;
			if(PF_DEBUG) {
				bi = new BufferedImage(100*11, 100*11, BufferedImage.TYPE_INT_RGB);
				g = bi.getGraphics();
			}
			long start = System.currentTimeMillis();
			ArrayList<BoundingBox.Polygon> bboxes = new ArrayList<>();
			Gob player = gui.map.player();
			if(player == null)
				return false;
			Coord2d origin = player.rc.floor().div(100).mul(new Coord2d(100, 100)).add(45, 45).floor(MCache.tilesz).mul(MCache.tilesz);
			int[][] grid = new int[100*11][100*11];
			Coord tgt;
			boolean doorOffset = false;
			if(destGob != null) {
				Resource res;
				while(true) {
					try {
						res = destGob.getres();
						break;
					} catch(Loading l) { }
				}
				if(button == 3 && res != null && doorOffsets.containsKey(res.name)) {
					tgt = destGob.rc.add(doorOffsets.get(res.name).rotate(destGob.a)).sub(origin).round().add(50*11, 50*11);
					doorOffset = true;
				} else {
					tgt = destGob.rc.sub(origin).round().add(50*11, 50*11);
				}
			} else {
				tgt = target.sub(origin).round().add(50*11, 50*11);
			}
			boolean boating = false;
			ArrayList<Gob> gobs = new ArrayList<>();
			synchronized(gui.ui.sess.glob.oc) {
				for(Gob gob : gui.ui.sess.glob.oc) {
					gobs.add(gob);
				}
			}
			forgob:
			for(Gob gob : gobs) {
				BoundingBox bb = BoundingBox.getBoundingBox(gob);
				if(bb == null || !bb.blocks)
					continue;
				if(isMyBoat(gob, player)) {
					Coord c = origin.div(MCache.tilesz).round();
					int t = gui.ui.sess.glob.map.gettile(c);
					if(accessibleTilesBoating.contains(gui.ui.sess.glob.map.tilesetr(t).name))
						boating = true;
					continue;
				}
				int retries = 0;
				while(retries++ < 10) {
					try {
						if(gob == player || (gob == destGob && !doorOffset) || (!gob.getres().name.equals("gfx/borka/body") && gob.getc().mul(1, 1, 0).dist(player.getc().mul(1, 1, 0)) < 1)) {
							continue forgob;
						}
						break;
					} catch(Loading l) {
						l.waitfor();
					}
				}
				for(BoundingBox.Polygon pol : bb.polygons) {
					bboxes.add(new BoundingBox.Polygon(pol.vertices.stream()
							.map((v) -> v.rotate(gob.a).add(gob.rc).sub(origin))
							.collect(Collectors.toCollection(ArrayList::new))));
				}
			}

			// Player probably just wants out of the boat
			if(boating && button == 1 && (mod & UI.MOD_CTRL) != 0) {
				gui.map.wdgmsg("click", Coord.z, target.floor(OCache.posres), button, mod);
				return true;
			}

			for(int i=-45; i<=45; i++) {
				for(int j=-45; j<=45; j++) {
					Coord c = origin.div(MCache.tilesz).round().add(i, j);
					while(true) {
						try {
							int t = gui.ui.sess.glob.map.gettile(c);
							Tiler tl = gui.ui.sess.glob.map.tiler(t);
							if(tl instanceof Ridges.RidgeTile) {
								if(Ridges.brokenp(gui.ui.sess.glob.map, c)) {
									markTileInaccessible(new Coord(i, j), grid);
								}
							}
							Resource res = gui.ui.sess.glob.map.tilesetr(t);
							if(boating) {
								if(res != null && !accessibleTilesBoating.contains(res.name)) {
									markTileInaccessible(new Coord(i, j), grid);
								}
							} else {
								if(res != null && (inaccessibleTiles.contains(res.name) || res.name.startsWith("gfx/tiles/rocks/"))) {
									markTileInaccessible(new Coord(i, j), grid);
								}
							}
						} catch(Loading l){
							PBotUtils.sleep(20);
							continue;
						}
						break;
					}

				}
			}

			for(BoundingBox.Polygon pol : bboxes) {
				markPolygon(pol, 1, 3, 3, grid);
			}
			grid[player.rc.sub(origin).round().add(50*11, 50*11).x][player.rc.sub(origin).round().add(50*11, 50*11).y] = 0;
			for(int i=1; i<=1; i++)
				drawCircle(player.rc.sub(origin).round().add(50*11, 50*11).x, player.rc.sub(origin).round().add(50*11, 50*11).y, i, 0, grid);
			if(destGob != null && !doorOffset) {
				BoundingBox bb = BoundingBox.getBoundingBox(destGob);
				if(bb != null) {
					for(BoundingBox.Polygon pol : bb.polygons) {
						markPolygon2(new BoundingBox.Polygon(pol.vertices.stream()
								.map((v) -> v.rotate(destGob.a).add(destGob.rc).sub(origin))
								.collect(Collectors.toCollection(ArrayList::new))), grid);
					}
				}
			}
			for(int i=-40*11; i<=40*11; i++) {
				grid[50*11-i][50*11-40*11] = 1;
				grid[50*11-40*11][50*11-i] = 1;
				grid[50*11+i][50*11+40*11] = 1;
				grid[50*11+40*11][50*11+i] = 1;
			}
			Coord[][] src = new Coord[100*11][100*11];
			double[][] cost = new double[100*11][100*11];
			for(int i=0; i<100*11; i++) {
				for(int j=0; j<100*11; j++)
					cost[i][j] = Double.MAX_VALUE;
			}
			PriorityQueue<Pair<Pair<Double, Double>, Coord>> q = new PriorityQueue<>((a, b) -> {
				return Double.compare(a.a.a,b.a.a);
			});
			if(PF_DEBUG) {
				for(int i = 0; i < grid.length; i++) {
					for(int j = 0; j < grid[0].length; j++) {
						if(grid[i][j] == 1) {
							bi.setRGB(i, j, Color.white.getRGB());
						} else if(grid[i][j] == -1) {
							bi.setRGB(i, j, Color.GREEN.getRGB());
						}
					}
				}
			}
			q.add(new Pair(new Pair(0d, 0d), player.rc.sub(origin).add(50*11, 50*11).round()));
			ArrayList<Coord> rte = new ArrayList<>();
			while(!q.isEmpty()) {
				double cst = q.peek().a.b;
				Coord c = q.poll().b;
				if(c.equals(tgt) || grid[c.x][c.y] == -1) {
					// backtrack
					do {
						while(rte.size() >= 2 && !lineIntersects(rte.get(rte.size()-2), c, grid)) {
							rte.remove(rte.size()-1);
						}
						rte.add(c);
						c = src[c.x][c.y];
					} while(c != null);
					if(PF_DEBUG) {
						System.out.println("RTE FOUND");
						System.out.println("length: " + rte.size());
					}
					q.clear();
					break;
				}
				if(grid[c.x][c.y] > 0) // visited or blocked
					continue;
				if(PF_DEBUG)
					bi.setRGB(c.x, c.y, Color.cyan.getRGB());
				grid[c.x][c.y] = 2; // visited
				for(int i=-1; i<=1; i++) {
					for(int j=-1; j<=1; j++) {
						double ncst = cst + (double)Math.sqrt(i*i+j*j);
						if(grid[c.x+i][c.y+j] <= 0 && ncst < cost[c.x+i][c.y+j]) {
							src[c.x+i][c.y+j] = c;
							cost[c.x+i][c.y+j] = ncst;
							q.add(new Pair(new Pair(ncst + (double)c.add(i,j).dist(tgt), ncst), c.add(i, j)));
						}
					}
				}
			}
			if(PF_DEBUG) {
				g.setColor(Color.ORANGE);
				for(int i = 1; i < rte.size(); i++) {
					g.drawLine(rte.get(i).x, rte.get(i).y, rte.get(i - 1).x, rte.get(i - 1).y);
				}
				try {
					ImageIO.write(bi, "png", new File("debug/pathfinder.png"));
				} catch(IOException e) {
					e.printStackTrace();
				}
				System.out.println("Finding route took: " + (System.currentTimeMillis()-start) + " ms");
			}
			if(rte.size() == 0) {
				return false;
			}
			Collections.reverse(rte);
			for(int i=1; i<rte.size() && gui.map.player() != null; i++) {
				if(destGob != null && (i == rte.size()-1) && !doorOffset) {
					if(action.length() > 0) {
						gui.wdgmsg("act", action);
					}
					if(destGob.rc != null) {
						gui.map.wdgmsg("click", gui.map.sz.div(2), destGob.rc.floor(OCache.posres), button, mod, (overlay == -1) ? 0 : 1, (int) destGob.id, destGob.rc.floor(OCache.posres), overlay, meshid);
						gui.map.cp = new ClickPath(player, new Coord2d[]{destGob.rc}, gui.ui.sess.glob.map);
					}
					if(action.length() > 0) {
						gui.map.wdgmsg("click", Coord.z, Coord.z, 3, 0);
					}
					break;
				}
				Coord2d[] croute = new Coord2d[rte.size()-i];
				for(int j=i; j<rte.size(); j++) {
					croute[j-i] = origin.add(new Coord2d(rte.get(j).sub(50*11, 50*11)));
				}
				gui.map.cp = new ClickPath(player, croute, gui.ui.sess.glob.map);
				Coord2d clickTgt = origin.add(new Coord2d(rte.get(i).sub(50*11, 50*11)));
				if(i != rte.size()-1 && clickTgt.dist(origin.add(new Coord2d(rte.get(rte.size()-1)).sub(50*11, 50*11))) <= 2.0)
					continue;
				if(i == rte.size()-1 && !doorOffset)
					gui.map.wdgmsg("click", Coord.z, clickTgt.floor(OCache.posres), button, mod);
				else
					gui.map.wdgmsg("click", Coord.z, clickTgt.floor(OCache.posres), 1, 0);
				while(gui.map.player() != null && ((player.getv() != 0 || player.rc.dist(clickTgt) > 0.5))) {
					PBotUtils.sleep(20);
					if(destGob != null && player.getv() == 0 && player.rc.dist(origin.add(new Coord2d(rte.get(rte.size()-1).sub(50*11, 50*11)))) < 5.5)
						break;
				}
			}
			if(doorOffset)
				gui.map.wdgmsg("click", Coord.z, destGob.rc.floor(OCache.posres), button, mod, 0, (int) destGob.id, destGob.rc.floor(OCache.posres), 0, meshid);
			return true;
		});
		gui.pathfinder = new Thread(gui.map.pf_route_found, "PF-thread");
		gui.pathfinder.setUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
			@Override
			public void uncaughtException(Thread t, Throwable e) {
				System.out.println(t + " " + e);
			}
		});
		gui.pathfinder.start();
	}
}
