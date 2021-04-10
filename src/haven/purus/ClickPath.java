package haven.purus;

import haven.*;
import haven.render.*;

import java.awt.*;
import java.nio.*;

public class ClickPath implements RenderTree.Node, TickList.Ticking, TickList.TickNode {
	static final Pipe.Op emat = Pipe.Op.compose(new BaseColor(Color.orange), new States.LineWidth(2));
	static final Pipe.Op op1 = Pipe.Op.compose(new States.Depthtest(States.Depthtest.Test.TRUE), Rendered.last, emat);
	static final VertexArray.Layout pfmt = new VertexArray.Layout(new VertexArray.Layout.Input(Homo3D.vertex, new VectorFormat(3, NumberFormat.FLOAT32), 0,  0, 12));
	Model emod;
	Gob gob;
	public Coord2d[] rte;
	MCache mCache;

	public ClickPath(Gob gob, Coord2d[] rte, MCache mCache) {
		this.gob = gob;
		this.rte = rte;
		this.mCache = mCache;
		VertexArray va = new VertexArray(pfmt, new VertexArray.Buffer(rte.length * 2 * pfmt.inputs[0].stride, DataBuffer.Usage.STATIC, this::fill));

		this.emod = new Model(Model.Mode.LINES, va, null);
	}

	private FillBuffer fill(VertexArray.Buffer dst, Environment env) {
		FillBuffer ret = env.fillbuf(dst);
		ByteBuffer buf = ret.push();
		try {
			Coord2d prev = Coord2d.z;
			if(gob != null)
				prev = new Coord2d(gob.getc());
			for(int i=0; i<rte.length; i++) {
				buf.putFloat((float) prev.x).putFloat((float) -prev.y).putFloat((float) mCache.getfz(prev.floor(MCache.tilesz)));
				buf.putFloat((float) rte[i].x).putFloat((float) -rte[i].y).putFloat((float) mCache.getfz(rte[i].floor(MCache.tilesz)));
				prev = rte[i];
			}
		} catch(Loading l) {}
		return (ret);
	}

	public void autogtick(Render g) {
		try {
			g.update(this.emod.va.bufs[0], this::fill);
		} catch(Loading l) {}
	}

	public void added(RenderTree.Slot slot) {
		slot.ostate(op1);
		slot.add(emod);
	}

	@Override
	public TickList.Ticking ticker() {
		return this;
	}
}
