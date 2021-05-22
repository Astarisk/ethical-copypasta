package haven.purus;

import haven.*;

import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;

public class BoundingBox {

	public final ArrayList<Polygon> polygons;
	public int vertices = 0;
	public boolean blocks = true;

	public static Polygon acbcPol(Coord ac, Coord bc) {
		ArrayList<Coord2d> vertices = new ArrayList<>();
		vertices.add(new Coord2d(ac.x, ac.y));
		vertices.add(new Coord2d(bc.x, ac.y));
		vertices.add(new Coord2d(bc.x, bc.y));
		vertices.add(new Coord2d(ac.x, bc.y));
		Polygon pol = new Polygon(vertices);
		pol.neg = true;
		return pol;
	}

	public BoundingBox(ArrayList<Polygon> polygons, boolean blocks) {
		this.polygons = polygons;
		for(Polygon pol : polygons) {
			vertices += pol.vertices.size();
		}
		this.blocks = blocks;
	}

	public static class Polygon {
		public final ArrayList<Coord2d> vertices;
		public boolean neg;
		public Polygon(ArrayList<Coord2d> vertices) {
			this.vertices = vertices;
		}
	}

	public static BoundingBox getBoundingBox(Gob gob) {
		boolean blocks = true;
		Resource res;
		while(true) {
			try {
				res = gob.getres();
				break;
			} catch(Loading l) {
			}
		}
		if(res == null)
			return null;
		Resource.Obst obst = res.layer(Resource.Obst.class);
		Resource.Neg neg = res.layer(Resource.Neg.class);
		if (neg == null) {
			for (RenderLink.Res link : res.layers(RenderLink.Res.class)) {
				if (link.l instanceof  RenderLink.MeshMat && ((RenderLink.MeshMat)link.l).mesh != null) {
					neg = ((RenderLink.MeshMat)link.l).mesh.get().layer(Resource.Neg.class);
					break;
				}
			}
		}
		if (obst == null) {
			for (RenderLink.Res link : res.layers(RenderLink.Res.class)) {
				if (link.l instanceof  RenderLink.MeshMat && ((RenderLink.MeshMat)link.l).mesh != null) {
					obst = ((RenderLink.MeshMat)link.l).mesh.get().layer(Resource.Obst.class);
					break;
				}
			}
		}
		// dual state gobs
		if (res.name.endsWith("gate") && res.name.startsWith("gfx/terobjs/arch")) {
			GAttrib rd = gob.getattr(ResDrawable.class);
			if (rd == null)     // shouldn't happen
				return null;
			int state = ((ResDrawable) rd).sdt.peekrbuf(0);
			if (state == 1)     // open gate
				blocks = false;
		} else if (res.name.endsWith("/pow")) {
			GAttrib rd = gob.getattr(ResDrawable.class);
			if (rd == null)     // shouldn't happen
				return null;
			int state = ((ResDrawable) rd).sdt.peekrbuf(0);
			if (state == 17 || state == 33) // hf
				blocks = false;
		} else if(res.name.equals("gfx/terobjs/arch/cellardoor")) {
			blocks = false;
		}

		if(obst != null) {
			ArrayList<Polygon> polygons = new ArrayList<>();
			for(ArrayList<Coord2d> lst : obst.vertices) {
				ArrayList<Coord2d> vertices = new ArrayList<>();
				for(Coord2d c : lst) {
					vertices.add(c);
				}
				polygons.add(new Polygon(vertices));
			}
			return new BoundingBox(polygons, blocks);
		} else if(neg != null) {
			ArrayList<Polygon> polygons = new ArrayList<>();
			polygons.add(acbcPol(neg.ac, neg.bc));
			return new BoundingBox(polygons, blocks);
		} else {
			if(res.name.equals("gfx/kritter/sheep/lamb")) {
				return new BoundingBox(new ArrayList<Polygon>(){{add(acbcPol(new Coord(-4,-2), new Coord(5,2)));}}, true);
			}
			return null;
		}
	}
}
