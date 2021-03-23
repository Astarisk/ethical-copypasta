package haven.purus;

import haven.*;

import java.util.ArrayList;

public class BoundingBox {

	public final ArrayList<Polygon> polygons;
	public int vertices = 0;

	private static Polygon acbcPol(Coord ac, Coord bc) {
		ArrayList<Coord2d> vertices = new ArrayList<>();
		vertices.add(new Coord2d(ac.x, ac.y));
		vertices.add(new Coord2d(bc.x, ac.y));
		vertices.add(new Coord2d(bc.x, bc.y));
		vertices.add(new Coord2d(ac.x, bc.y));
		return new Polygon(vertices);
	}

	public BoundingBox(ArrayList<Polygon> polygons) {
		this.polygons = polygons;
		for(Polygon pol : polygons) {
			vertices += pol.vertices.size();
		}
	}

	public static class Polygon {
		public final ArrayList<Coord2d> vertices;
		public Polygon(ArrayList<Coord2d> vertices) {
			this.vertices = vertices;
		}
	}

	public static BoundingBox getBoundingBox(Gob gob) {
		Resource res = gob.getres();
		if(res == null)
			return null;
		Resource.Obst obst = res.layer(Resource.Obst.class);
		Resource.Neg neg = res.layer(Resource.Neg.class);
		if (neg == null) {
			for (RenderLink.Res link : res.layers(RenderLink.Res.class)) {
				if (link.l instanceof  RenderLink.MeshMat && ((RenderLink.MeshMat)link.l).mesh != null) {
					obst = ((RenderLink.MeshMat)link.l).mesh.get().layer(Resource.Obst.class);
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
				return null;
		} else if (res.name.endsWith("/pow")) {
			GAttrib rd = gob.getattr(ResDrawable.class);
			if (rd == null)     // shouldn't happen
				return null;
			int state = ((ResDrawable) rd).sdt.peekrbuf(0);
			if (state == 17 || state == 33) // hf
				return null;
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
			return new BoundingBox(polygons);
		} else if(neg != null) {
			ArrayList<Polygon> polygons = new ArrayList<>();
			if(res.name.endsWith("/hwall"))
				polygons.add(acbcPol(new Coord(-1, 0), new Coord(0, 11)));
			else
				polygons.add(acbcPol(neg.ac, neg.bc));
			return new BoundingBox(polygons);
		} else {
			return null;
		}
	}
}
