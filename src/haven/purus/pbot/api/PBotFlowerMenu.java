package haven.purus.pbot.api;

import haven.FlowerMenu;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class PBotFlowerMenu {

	private FlowerMenu menu;

	PBotFlowerMenu(FlowerMenu menu) {
		this.menu = menu;
	}

	/**
	 * Close this flowermenu
	 */
	public void closeMenu() {
		this.menu.choose(null);
	}

	/**
	 * Choose option
	 * @param name Exact name of the option
	 */
	public void choosePetal(String name) {
		for(FlowerMenu.Petal p : this.menu.opts) {
			if(p.name.equals(name)) {
				menu.choose(p);
				menu.destroy();
				return;
			}
		}
	}

	/**
	 * @return Names of the petal options
	 */
	public List<String> getPetalNames() {
		return Arrays.stream(this.menu.opts)
				.map(opt -> opt.name)
				.collect(Collectors.toList());
	}
}
