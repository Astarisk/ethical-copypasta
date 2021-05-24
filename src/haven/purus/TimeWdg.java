package haven.purus;

import haven.*;

public class TimeWdg extends Widget {
	private static final long secinday = 60 * 60 * 24;
	private static final long dewyladysmantletimemin = 4 * 60 * 60 + 45 * 60;
	private static final long dewyladysmantletimemax = 7 * 60 * 60 + 15 * 60;
	private static int seasons[] = {10, 35, 10, 5};
	public String servertime;
	public Tex servertimetex;
	private Glob glob;

	public TimeWdg(Glob glob) {
		super(UI.scale(360, 40));
		this.glob = glob;
	}

	public void servertimecalc() {
		if(glob.ast == null)
			return;

		long secs = (long)glob.globtime();
		long day = secs / secinday;
		long secintoday = secs % secinday;
		long hours = secintoday / 3600;
		long mins = (secintoday % 3600) / 60;
		double nextseason = (1 - glob.ast.sp) * seasons[glob.ast.is];

		String fmt;
		switch (glob.ast.is) {
			case 0:
				fmt = nextseason == 1 ? "Day %d, %02d:%02d. Spring (%.2f RL day left)." : "Day %d, %02d:%02d. Spring (%.2f RL days left).";
				break;
			case 1:
				fmt = nextseason == 1 ? "Day %d, %02d:%02d. Summer (%.2f RL day left)." : "Day %d, %02d:%02d. Summer (%.2f RL days left).";
				break;
			case 2:
				fmt = nextseason == 1 ? "Day %d, %02d:%02d. Autumn (%.2f RL day left)." : "Day %d, %02d:%02d. Autumn (%.2f RL days left).";
				break;
			case 3:
				fmt = nextseason == 1 ? "Day %d, %02d:%02d. Winter (%.2f RL day left)." : "Day %d, %02d:%02d. Winter (%.2f RL days left).";
				break;
			default:
				fmt = "Unknown Season";
		}

		servertime = String.format(fmt, day, hours, mins, nextseason);

		if (secintoday >= dewyladysmantletimemin && secintoday <= dewyladysmantletimemax)
			servertime += " (Dewy Lady's Mantle)";

		servertimetex = Text.render(servertime).tex();
	}

	@Override
	public void draw(GOut g) {
		Tex time = servertimetex;
		if(time != null) {
			g.image(time, new Coord(UI.scale(360 / 2) - time.sz().x / 2, 0));
		}
	}
}
