/* Preprocessed source code */
package haven.purus;

import haven.*;
import haven.render.*;
import java.nio.*;

/* >spr: BPRad */
public class GobBoundingBox extends Sprite implements RenderTree.Node {
	static final Pipe.Op emat = Pipe.Op.compose(new BaseColor(new java.awt.Color(255, 255, 255)), new States.LineWidth(3));
	final Model emod;
	final Gob gob;
	private BoundingBox bb;
	static final Pipe.Op op2 = Pipe.Op.compose(new States.Depthtest(States.Depthtest.Test.TRUE), Rendered.last, emat);

	static final VertexArray.Layout pfmt = new VertexArray.Layout(new VertexArray.Layout.Input(Homo3D.vertex, new VectorFormat(3, NumberFormat.FLOAT32), 0,  0, 12));

	public GobBoundingBox(Gob gob, BoundingBox bb) {
		super(null, null);
		this.gob = gob;
		this.bb = bb;

		VertexArray va = new VertexArray(pfmt, new VertexArray.Buffer(bb.vertices * 2 * pfmt.inputs[0].stride, DataBuffer.Usage.STATIC, this::fill));

		this.emod = new Model(Model.Mode.LINES, va, null);
	}

	private FillBuffer fill(VertexArray.Buffer dst, Environment env) {
		FillBuffer ret = env.fillbuf(dst);
		ByteBuffer buf = ret.push();
		for(BoundingBox.Polygon pol : this.bb.polygons) {
			Coord2d previous = pol.vertices.get(pol.vertices.size()-1);
			for(Coord2d vertex : pol.vertices) {
				buf.putFloat((float) previous.x).putFloat((float) (pol.neg ? -previous.y : previous.y)).putFloat(1.0f);
				buf.putFloat((float) vertex.x).putFloat((float) (pol.neg ? -vertex.y : vertex.y)).putFloat(1.0f);
				previous = vertex;
			}
		}
		return (ret);
	}

	public void added(RenderTree.Slot slot) {
		if(Config.bbDisplayState.val == 1) {
			slot.ostate(emat);
		} else if(Config.bbDisplayState.val == 2) {
			slot.ostate(op2);
		}
		slot.add(emod);
	}
}
