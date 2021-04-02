/* Preprocessed source code */
package haven.purus;

import haven.*;
import haven.render.*;
import java.nio.*;
import java.util.ArrayList;
import java.util.Arrays;

/* >spr: BPRad */
public class GobHideBox extends Sprite implements RenderTree.Node {
	private static BoundingBox defaultBb = new BoundingBox(new ArrayList<>(Arrays.asList(BoundingBox.acbcPol(new Coord(5, 5), new Coord(-5, -5)))), false);

	public static class HidePol extends Sprite implements RenderTree.Node {
		public static Pipe.Op emat = Pipe.Op.compose(new BaseColor(new java.awt.Color(Config.hideRed.val, Config.hideGreen.val, Config.hideBlue.val, Config.hideAlpha.val)));
		final Model emod;
		private BoundingBox.Polygon pol;

		static final VertexArray.Layout pfmt = new VertexArray.Layout(new VertexArray.Layout.Input(Homo3D.vertex, new VectorFormat(3, NumberFormat.FLOAT32), 0,  0, 12));

		public HidePol(BoundingBox.Polygon pol) {
			super(null, null);
			this.pol = pol;

			VertexArray va = new VertexArray(pfmt, new VertexArray.Buffer((pol.vertices.size()) * pfmt.inputs[0].stride, DataBuffer.Usage.STATIC, this::fill));

			this.emod = new Model(Model.Mode.TRIANGLE_FAN, va, null);
		}

		private FillBuffer fill(VertexArray.Buffer dst, Environment env) {
			FillBuffer ret = env.fillbuf(dst);
			ByteBuffer buf = ret.push();
			if(pol.neg) {
				for(int i=pol.vertices.size()-1; i>=0; i--) {
					buf.putFloat((float) pol.vertices.get(i).x).putFloat((float) -pol.vertices.get(i).y).putFloat(1.0f);
				}
			} else {
				for(int i=0; i<pol.vertices.size(); i++) {
					buf.putFloat((float) pol.vertices.get(i).x).putFloat((float) pol.vertices.get(i).y).putFloat(1.0f);
				}
			}
			return (ret);
		}

		public void added(RenderTree.Slot slot) {
			slot.ostate(Pipe.Op.compose(emat));
			slot.add(emod);
		}
	}
	private BoundingBox bb;

	public GobHideBox(BoundingBox bb) {
		super(null, null);
		if(bb == null)
			this.bb = defaultBb;
		else
			this.bb = bb;
	}

	public void added(RenderTree.Slot slot) {
		for(BoundingBox.Polygon pol : bb.polygons) {
			new HidePol(pol).added(slot);
		}
	}
}
