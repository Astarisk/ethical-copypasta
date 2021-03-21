package haven.res.ui.tt.q.quality;

/* Preprocessed source code */
/* $use: ui/tt/q/qbuff */
import haven.*;
import java.awt.image.BufferedImage;
import haven.MenuGrid.Pagina;
import haven.purus.Config;

/* >tt: Quality */
public class ShowQuality extends MenuGrid.PagButton {
	public ShowQuality(Pagina pag) {
		super(pag);
	}

	public static class Fac implements Factory {
		public MenuGrid.PagButton make(Pagina pag) {
			return(new ShowQuality(pag));
		}
	}

	public BufferedImage img() {return(res.layer(Resource.imgc, 1).scaled());}

	public void use() {
		Config.displayQuality.setVal(!Config.displayQuality.val);
	}
}