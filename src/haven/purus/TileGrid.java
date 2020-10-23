package haven.purus;

import static haven.MCache.tilesz;

import haven.*;
import haven.render.*;

import java.awt.event.KeyEvent;
import java.nio.ByteBuffer;
public class TileGrid implements Rendered, RenderTree.Node, TickList.Ticking, TickList.TickNode {

	public static KeyBinding kb_toggleTileGrid = KeyBinding.get("kb_toggleTileGrid", KeyMatch.forcode(KeyEvent.VK_G, 2));

	private final MCache mCache;
	static final VertexArray.Layout pfmt = new VertexArray.Layout(new VertexArray.Layout.Input(Homo3D.vertex, new VectorFormat(3, NumberFormat.FLOAT32), 0,  0, 16), new VertexArray.Layout.Input(VertexColor.color, new VectorFormat(4, NumberFormat.UNORM8),  0, 12, 16));
	private final Pipe.Op ptsz = new States.LineWidth(1.1);
	private Model model;
	private boolean update = true;
	public Coord ofs = Coord.z;

	public TileGrid(MCache mCache) {
			this.mCache = mCache;
	}

	public void upd(Coord ofs) {
		this.ofs = ofs;
		if(model == null) {
			VertexArray va = new VertexArray(pfmt, new VertexArray.Buffer(100 * 100 * 4 * pfmt.inputs[0].stride, DataBuffer.Usage.STATIC, this::initfill));
			model = new Model(Model.Mode.LINES, va, null);
		}
		update = true;
	}

	public void draw(Pipe st, Render g) {
		g.draw(st, model);
	}

	private FillBuffer initfill(VertexArray.Buffer dst, Environment env) {
		try {
			return (fill(dst, env));
		} catch(Loading l) {
			return (DataBuffer.Filler.zero().fill(dst, env));
		}
	}

	private FillBuffer fill(VertexArray.Buffer dst, Environment env) {
		FillBuffer ret = env.fillbuf(dst);
		ByteBuffer buf = ret.push();
		for(int i = 0; i < 100; i++) {
			for(int j = 0; j < 100; j++) {
				if(ofs == null)
					throw new Loading();
				buf.putFloat((i + ofs.x) * (float) tilesz.x).putFloat((-ofs.y - j) * (float) tilesz.y).putFloat(mCache.getz(Coord.z.sub(-ofs.x, -ofs.y).add(+i, +j)));
				buf.put((byte) 255).put((byte) 255).put((byte) 255).put((byte) 64);
				buf.putFloat((i + ofs.x) * (float) tilesz.x).putFloat((-ofs.y - j + 1) * (float) tilesz.y).putFloat(mCache.getz(Coord.z.sub(-ofs.x, -ofs.y).add(i, +j - 1)));
				buf.put((byte) 255).put((byte) 255).put((byte) 255).put((byte) 64);

				buf.putFloat((+j + ofs.x) * (float) tilesz.x).putFloat((-ofs.y - i) * (float) tilesz.y).putFloat(mCache.getz(Coord.z.sub(-ofs.x, -ofs.y).add(+j, +i)));
				buf.put((byte) 255).put((byte) 255).put((byte) 255).put((byte) 64);
				buf.putFloat((+j + ofs.x - 1) * (float) tilesz.x).putFloat((-ofs.y - i) * (float) tilesz.y).putFloat(mCache.getz(Coord.z.sub(-ofs.x, -ofs.y).add(+j - 1, +i)));
				buf.put((byte) 255).put((byte) 255).put((byte) 255).put((byte) 64);
			}
		}
		return (ret);
	}

	public void autogtick(Render g) {
		if(update) {
			try {
				g.update(model.va.bufs[0], this::fill);
				update = false;
			} catch(Loading l) {
			}
		}
	}

	public TickList.Ticking ticker() {
		return (this);
	}

	public void added(RenderTree.Slot slot) {
		slot.ostate(Pipe.Op.compose(Location.xlate(new Coord3f(0,0,0/*loftarhalp*/)), ptsz, new States.Depthtest(States.Depthtest.Test.TRUE), Rendered.last, VertexColor.instance));
	}
}
