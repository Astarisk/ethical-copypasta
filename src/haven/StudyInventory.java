package haven;

import haven.purus.Config;
import haven.resutil.Curiosity;

import java.awt.*;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class StudyInventory extends Inventory {

    @RName("inv-study")
    public static class $_ implements Factory {
        public Widget create(UI ui, Object[] args) {
            return(new StudyInventory((Coord)args[0]));
        }
    }

    public class OldCurios {

        public HashMap<Coord, Pair<String, Tex>> oldCurios = new HashMap<Coord, Pair<String, Tex>>();

        public OldCurios() {
            load();
        }

        private void save() {
            Config.pref.put("oldcurios_" + gameui().chrid, asString());
        }

        private void load() {
            fromString(Config.pref.get("oldcurios_" + gameui().chrid, ""));
        }

        private String asString() {
            return oldCurios.entrySet().stream().map((entry) ->
                entry.getKey().x + " " + entry.getKey().y + " " + entry.getValue().a
            ).collect(Collectors.joining(";"));
        }

        private void fromString(String s) {
            oldCurios = new HashMap<>();
            if(s.isEmpty())
                return;
            String[] entries = s.split(";");
            for(String entry : entries) {
                String[] fields = entry.split(" ");
                setOld(new Coord(Integer.parseInt(fields[0]), Integer.parseInt(fields[1])), fields[2]);
            }
        }

        private void clearSlots(Coord c, Coord sz) {
            oldCurios.entrySet().removeIf((entry -> {
                for(int x=0; x<sz.x; x++) {
                    for(int y=0; y<sz.y; y++) {
                        if(c.add(x, y).isect(entry.getKey(),UI.unscale(entry.getValue().b.sz()).div(30)))
                            return true;
                    }
                }
                return false;
            }));
        }

        public void setNew(Coord c, Coord sz, String resname) {
            clearSlots(c, sz);
            oc.oldCurios.entrySet().removeIf((entry) -> entry.getValue().a.equals(resname));
            save();
        }

        public void setOld(Coord c, String resname) {
        	try {
        		Resource.Image img = Resource.remote().loadwait(resname).layer(Resource.imgc);
        		if(img == null)
        			return;
            	Tex tex = img.tex();
				clearSlots(c, UI.unscale(tex.sz()).div(30));
				oldCurios.put(c, new Pair<String, Tex>(resname, tex));
				save();
        	} catch(Loading l) {}
        }
    }

    private OldCurios oc;

    public StudyInventory(Coord sz) {
        super(sz);
    }

    @Override
    protected void added() {
        super.added();
        oc = new OldCurios();
    }

    @Override
    public void addchild(Widget child, Object... args) {
        super.addchild(child, args);
        if(child instanceof GItem) {
        	try {
				oc.setNew(((GItem) child).witem().invLoc(), ((GItem) child).size(), ((GItem) child).getres().name);
			} catch(Loading l) {}
        }
    }

    @Override
    public void cdestroy(Widget w) {
        if(w instanceof GItem) {
        	try {
				oc.setOld(((GItem) w).witem().invLoc(), ((GItem) w).getres().name);
				Curiosity ci = ItemInfo.find(Curiosity.class, ((GItem) w).info());
				if(ci != null && ((GItem) w).witem().itemmeter.get() > 0.99) {
					Resource.Tooltip tt = ((GItem) w).resource().layer(Resource.Tooltip.class);
					if(tt != null)
						gameui().syslog.append(tt.t + " LP: " + ci.exp, Color.LIGHT_GRAY);
				}
			} catch(Loading l) { }
		}
        super.cdestroy(w);
    }

    @Override
    public void draw(GOut g) {
        super.draw(g);
        for(Map.Entry<Coord, Pair<String, Tex>> e : oc.oldCurios.entrySet()) {
            g.chcolor(new Color(111, 111, 11));
            g.image(e.getValue().b, e.getKey().mul(sqsz).add(1,1));
        }
    }
}
