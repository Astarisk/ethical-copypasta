package haven.purus.mapper;

import haven.*;
import haven.Label;
import haven.purus.Config;
import org.json.JSONArray;
import org.json.JSONObject;

import javax.imageio.ImageIO;
import javax.net.ssl.HttpsURLConnection;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Scanner;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class Mapper {
	public static String apiURL = "https://hnhmap.vatsul.com/api" + "/";

	private static ScheduledExecutorService executor;

	public static ConcurrentHashMap<String, Pair<String, Pair<Coord, Long>>> players = new ConcurrentHashMap<String, Pair<String, Pair<Coord, Long>>>();

	private static Runnable locUpd =  new Runnable() {
		@Override
		public void run() {
			try {

				JSONArray arr = new JSONArray();

				players.forEach((name, pair) -> {
					if(name != null && pair.b != null && pair.b.a != null && pair.b.b != null) {
						JSONObject obj = new JSONObject();
						obj.put("hatres", pair.a);
						obj.put("ofsX", pair.b.a.x);
						obj.put("ofsY", pair.b.a.y);
						obj.put("gridId", pair.b.b);
						obj.put("charname", name);
						arr.put(obj);
					}
				});
				players.clear();

				if(arr.length() == 0) {
					return;
				}
				HttpsURLConnection conn = (HttpsURLConnection) new URL(new URL(apiURL),  "charloc/" + Config.mapperToken.val).openConnection();
				conn.setRequestProperty("User-Agent", "H&H Client/" + haven.Config.confid);
				conn.setRequestMethod("POST");
				conn.setRequestProperty("Content-Type", "application/json;charset=UTF-8");
				conn.setDoOutput(true);
				DataOutputStream dos = new DataOutputStream(conn.getOutputStream());
				dos.write(arr.toString().getBytes(StandardCharsets.UTF_8));
				dos.close();
				conn.getResponseCode();
			} catch(IOException e) {
				//e.printStackTrace();
			}
		}
	};

	static {
		executor = Executors.newScheduledThreadPool(1);
		executor.scheduleWithFixedDelay(locUpd, 5, 15, TimeUnit.SECONDS);
		executor.execute(() -> {
			boolean regen = false;
			if(Config.mapperToken.val.length() == 0)
				regen = true;
			else
				try {
					URL url = new URL(Mapper.apiURL + "/token/" + Config.mapperToken.val + "/valid");
					Scanner scan = new Scanner(url.openStream());
					if(scan.hasNextLine() && scan.nextLine().equals("Valid")) {
					} else {
						regen = true;
					}
				} catch(IOException e) {
					e.printStackTrace();
				}

			if(regen) {
				try {
					HttpsURLConnection conn = (HttpsURLConnection) new URL(apiURL + "token/generate").openConnection();
					conn.setRequestProperty("User-Agent", "H&H Client/" + haven.Config.confid);
					String token;
					try(BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()))) {
						token = reader.readLine();
					} finally {
						conn.disconnect();
					}
					if(conn.getResponseCode() == 200)
						Config.mapperToken.setVal(token);
				} catch(IOException e) {
					e.printStackTrace();
				}
			}
		});
	}

	public static void receiveGrid(MCache map, MCache.Grid grid, MCache.Grid top, MCache.Grid right, MCache.Grid down, MCache.Grid left) {
		sendGridData(grid, top, right, down, left);

		MinimapGenerator.Tileinfo tileinfo = null;
		while(tileinfo == null) {
			try {
				tileinfo = MinimapGenerator.getTileinfo(map,grid);
			} catch(Loading l) {
				try {
					l.waitfor();
				} catch(InterruptedException e) {
				}
			}
		}
		sendMaptile2(grid.id, tileinfo);
	}

	private static void sendMaptile2(long gridId, MinimapGenerator.Tileinfo tileinfo) {
		Runnable run = new Runnable() {
			@Override
			public void run() {
				try {
					HttpsURLConnection conn = (HttpsURLConnection) new URL(new URL(apiURL), Config.mapperToken.val + "/maptile2").openConnection();
					conn.setRequestMethod("POST");
					conn.setRequestProperty("Content-Type", "application/json;charset=UTF-8");
					conn.setRequestProperty("User-Agent", "H&H Client/" + haven.Config.confid);
					conn.setDoOutput(true);

					JSONObject obj = new JSONObject();

					obj.putOnce("gridId", gridId);
					obj.putOnce("tiles", tileinfo.tiles);
					obj.putOnce("tileset", tileinfo.tileset.entrySet().stream().collect(JSONObject::new,
							(o, entry) -> {
								o.put(entry.getKey().toString(), entry.getValue());
							}, JSONObject::similar /* not used in non-parallel streams, hopefully */));

					obj.putOnce("ridges", tileinfo.ridges.stream().collect(JSONArray::new,
							(arr, c) -> {
								JSONObject cobj = new JSONObject();
								cobj.putOnce("x", c.x);
								cobj.putOnce("y", c.y);
								arr.put(cobj);
							}, JSONArray::putAll));

					DataOutputStream dos = new DataOutputStream(conn.getOutputStream());
					dos.write(obj.toString().getBytes(StandardCharsets.UTF_8));
					dos.close();
					conn.getResponseCode();
				} catch(IOException e) {
					//e.printStackTrace();
				}
			}
		};
		executor.execute(run);
	}

	public static void sendMarkerData(long gridId, int ofsX, int ofsY, String resname, String name) {
		Runnable run = new Runnable() {
			@Override
			public void run() {
				try {
					JSONArray arr = new JSONArray();
					JSONObject obj = new JSONObject();
					obj.put("gridId", gridId);
					obj.put("ofsX", ofsX);
					obj.put("ofsY", ofsY);

					obj.put("res", resname);
					obj.put("name", name);
					arr.put(obj);


					HttpsURLConnection conn = (HttpsURLConnection) new URL(new URL(apiURL), "waypoints/" + Config.mapperToken.val + "/client/markers").openConnection();
					conn.setRequestMethod("POST");
					conn.setRequestProperty("Content-Type", "application/json;charset=UTF-8");
					conn.setRequestProperty("User-Agent", "H&H Client/" + haven.Config.confid);
					conn.setDoOutput(true);
					DataOutputStream dos = new DataOutputStream(conn.getOutputStream());
					dos.write(arr.toString().getBytes(StandardCharsets.UTF_8));
					dos.close();
					conn.getResponseCode();
				} catch(IOException e) {
					//e.printStackTrace();
				}
			}
		};
		executor.schedule(run, 0, TimeUnit.SECONDS);
	}

	public static void sendMarkerData(MapFile mapFile) {
		Runnable run = new Runnable() {
			@Override
			public void run() {
				try {
					JSONArray arr = new JSONArray();
					if (mapFile.lock.readLock().tryLock()) {
						mapFile.markers.stream().forEach((marker) -> {
							if(marker instanceof MapFile.SMarker) {
								Coord markerOfs = new Coord(marker.tc.x, marker.tc.y);
								Coord offsetTiles = markerOfs.mod(new Coord(100, 100));
								MapFile.Segment seg = mapFile.segments.get(marker.seg);
								JSONObject obj = new JSONObject();
								obj.put("gridId", seg.map.get(markerOfs.div(100)));
								obj.put("ofsX", offsetTiles.x);
								obj.put("ofsY", offsetTiles.y);

								obj.put("res", ((MapFile.SMarker) marker).res.name);
								obj.put("name", marker.nm);
								arr.put(obj);
							}
						});
						mapFile.lock.readLock().unlock();
					} else {
						System.out.println("Sending markers failed!");
						return;
					}


					HttpsURLConnection conn = (HttpsURLConnection) new URL(new URL(apiURL), "waypoints/" + Config.mapperToken.val + "/client/markers").openConnection();
					conn.setRequestMethod("POST");
					conn.setRequestProperty("Content-Type", "application/json;charset=UTF-8");
					conn.setRequestProperty("User-Agent", "H&H Client/" + haven.Config.confid);
					conn.setDoOutput(true);
					DataOutputStream dos = new DataOutputStream(conn.getOutputStream());
					dos.write(arr.toString().getBytes(StandardCharsets.UTF_8));
					dos.close();
					conn.getResponseCode();
				} catch(IOException e) {
					//e.printStackTrace();
				}
			}
		};
		executor.schedule(run, 5, TimeUnit.SECONDS);
	}

	private static void sendGridData(MCache.Grid... grids) {
		Runnable run = new Runnable() {
			@Override
			public void run() {
				if(grids.length <= 1)
					return;
				try {
					HttpsURLConnection conn = (HttpsURLConnection) new URL(new URL(apiURL), Config.mapperToken.val + "/grid").openConnection();
					conn.setRequestMethod("POST");
					conn.setRequestProperty("Content-Type", "application/json;charset=UTF-8");
					conn.setRequestProperty("User-Agent", "H&H Client/" + haven.Config.confid);
					conn.setDoOutput(true);

					JSONArray ar = new JSONArray();

					for(MCache.Grid g : grids) {
						if(g == null)
							continue;
						JSONObject obj = new JSONObject();
						obj.append("id", g.id);
						obj.append("x", g.gc.x);
						obj.append("y", g.gc.y);
						ar.put(obj);
					}
					DataOutputStream dos = new DataOutputStream(conn.getOutputStream());
					dos.write(ar.toString().getBytes(StandardCharsets.UTF_8));
					dos.close();
					conn.getResponseCode();
				} catch(IOException e) {
					//e.printStackTrace();
				}
			}
		};
		executor.execute(run);
	}

	private static void sendMaptile(long gridId, BufferedImage maptile) {
		Runnable run = new Runnable() {
			@Override
			public void run() {
				try {
					HttpsURLConnection conn = (HttpsURLConnection) new URL(new URL(apiURL), Config.mapperToken.val + "/maptile").openConnection();
					conn.setRequestMethod("POST");
					conn.setRequestProperty("Content-Type", "application/json;charset=UTF-8");
					conn.setRequestProperty("User-Agent", "H&H Client/" + haven.Config.confid);
					conn.setDoOutput(true);

					ByteArrayOutputStream os = new ByteArrayOutputStream();
					ImageIO.write(maptile, "png", os);
					os.flush();

					JSONObject obj = new JSONObject();
					obj.append("gridId", gridId);
					obj.append("maptile", new String(Base64.getEncoder().encode(os.toByteArray()), StandardCharsets.UTF_8));

					DataOutputStream dos = new DataOutputStream(conn.getOutputStream());
					dos.write(obj.toString().getBytes(StandardCharsets.UTF_8));
					dos.close();
					conn.getResponseCode();
				} catch(IOException e) {
					//e.printStackTrace();
				}
			}
		};
		executor.execute(run);
	}
}
