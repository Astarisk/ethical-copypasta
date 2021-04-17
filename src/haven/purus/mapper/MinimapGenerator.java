package haven.purus.mapper;

import haven.*;
import haven.resutil.Ridges;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * @author APXEOLOG (Artyom Melnikov), at 28.01.2019
 */
public class MinimapGenerator {

	private static String tileres(int t, MCache map) {
		Resource res = map.tilesetr(t);
		return (map.tilesetr(t) == null ? null : res.name);
	}

	public static class Tileinfo {

		int[][] tiles = new int[MCache.cmaps.x][MCache.cmaps.y];
		List<Coord> ridges = new ArrayList<>();
		HashMap<Integer, String> tileset = new HashMap<>();

		public Tileinfo(MCache map, MCache.Grid grid) {
			for(int y=0; y<MCache.cmaps.y; y++) {
				for(int x=0; x<MCache.cmaps.x; x++){
					tiles[x][y] = grid.tiles[y*MCache.cmaps.x + x];
				}
			}
			Coord c = new Coord();
			for (c.y = 0; c.y < MCache.cmaps.y; c.y++) {
				for (c.x = 0; c.x < MCache.cmaps.x; c.x++) {
					int t = grid.gettile(c);
					if(!tileset.containsKey(t))
						tileset.put(t, tileres(t, map));
				}
			}
			for (c.y = 1; c.y < MCache.cmaps.y - 1; c.y++) {
				for (c.x = 1; c.x < MCache.cmaps.x - 1; c.x++) {
					int t = grid.gettile(c);
					Tiler tl = map.tiler(t);
					if ((tl instanceof Ridges.RidgeTile && Ridges.brokenp(map, c, grid)))
						ridges.add(new Coord(c));
				}
			}
		}
	}

	public static Tileinfo getTileinfo(MCache map, MCache.Grid grid) {
		return new Tileinfo(map, grid);
	}
}
