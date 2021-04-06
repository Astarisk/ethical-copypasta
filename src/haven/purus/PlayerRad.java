/* Preprocessed source code */
package haven.purus;

import haven.*;
import haven.render.*;
import haven.render.Model.Indices;

import java.awt.*;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

/* >spr: BPRad */
public class PlayerRad extends Sprite {
	final Pipe.Op smat;
	final VertexBuf.VertexData posa;
	final VertexBuf vbuf;
	final Model smod;

	public PlayerRad(Owner owner, Resource res, Color col) {
		super(owner, res);
		smat = new BaseColor(col);
		float r = 1.0f;
		int n = Math.max(24, (int)(2 * Math.PI * r / 11.0));
		FloatBuffer posb = Utils.wfbuf(n * 3 * 2);
		FloatBuffer nrmb = Utils.wfbuf(n * 3 * 2);
		for(int i = 0; i < n; i++) {
			float s = (float)Math.sin(2 * Math.PI * i / n);
			float c = (float)Math.cos(2 * Math.PI * i / n);
			posb.put(     i  * 3 + 0, c * r).put(     i  * 3 + 1, s * r).put(     i  * 3 + 2,  12);
			posb.put((n + i) * 3 + 0, c * r).put((n + i) * 3 + 1, s * r).put((n + i) * 3 + 2, 24);
			nrmb.put(     i  * 3 + 0, c).put(     i  * 3 + 1, s).put(     i  * 3 + 2, 0);
			nrmb.put((n + i) * 3 + 0, c).put((n + i) * 3 + 1, s).put((n + i) * 3 + 2, 0);
		}
		VertexBuf.VertexData posa = new VertexBuf.VertexData(posb);
		VertexBuf.NormalData nrma = new VertexBuf.NormalData(nrmb);
		VertexBuf vbuf = new VertexBuf(posa, nrma);
		this.smod = new Model(Model.Mode.TRIANGLES, vbuf.data(), new Indices(n * 6, NumberFormat.UINT16, DataBuffer.Usage.STATIC, this::sidx));
		this.posa = posa;
		this.vbuf = vbuf;
	}

	private FillBuffer sidx(Indices dst, Environment env) {
		FillBuffer ret = env.fillbuf(dst);
		ShortBuffer buf = ret.push().asShortBuffer();
		for(int i = 0, n = dst.n / 6; i < n; i++) {
			int b = i * 6;
			buf.put(b + 0, (short)i).put(b + 1, (short)(i + n)).put(b + 2, (short)((i + 1) % n));
			buf.put(b + 3, (short)(i + n)).put(b + 4, (short)(((i + 1) % n) + n)).put(b + 5, (short)((i + 1) % n));
		}
		return(ret);
	}

	public void added(RenderTree.Slot slot) {
		slot.ostate(Pipe.Op.compose(Rendered.postpfx,
				new States.Facecull(States.Facecull.Mode.NONE),
				Location.goback("gobx")));
		slot.add(smod, smat);
	}
}