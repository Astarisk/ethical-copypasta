package haven.purus.pbot.api;

import haven.Coord;
import haven.Inventory;
import haven.UI;
import haven.WItem;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class PBotInventory {

	private final Inventory inventory;
	private final PBotSession pBotSession;

	PBotInventory(Inventory inventory, PBotSession pBotSession) {
		this.inventory = inventory;
		this.pBotSession = pBotSession;
	}

	/**
	 * Return all items that the inventory contains
	 * @return List of items in the inventory
	 */
	public List<PBotItem> getInventoryContents() {
		return inventory.children(WItem.class).stream()
				.map(witem -> new PBotItem(witem.item, pBotSession))
				.collect(Collectors.toList());
	}

	/**
	 * Returns a list of items with specific regex pattern from the inventory
	 * @param pattern Regex pattern matching item names
	 * @return List of items with name matching given pattern
	 */
	public List<PBotItem> getInventoryItemsByNames(String pattern) {
		Pattern pat = Pattern.compile(pattern);
		return inventory.children(WItem.class).stream()
				.map(witem -> new PBotItem(witem.item, pBotSession))
				.filter(item -> {
					String name = item.getName();
					return (name != null && pat.matcher(name).matches());
				})
				.collect(Collectors.toList());
	}

	/**
	 * @param pattern Regex pattern matching item resnames
	 * @return List of items with resname matching pattern
	 */
	public List<PBotItem> getInventoryItemsByResnames(String pattern) {
		List<PBotItem> items = new ArrayList<>();
		Pattern pat = Pattern.compile(pattern);
		return inventory.children(WItem.class).stream()
				.map(witem -> new PBotItem(witem.item, pBotSession))
				.filter(item -> {
					String resname = item.getResname();
					return (resname != null && pat.matcher(resname).matches());
				})
				.collect(Collectors.toList());
	}

	/**
	 * Finds an item with certain location from the inventory
	 * @param x x-coordinate of the item location in inventory
	 * @param y y-coordinate of the item location in inventory
	 * @return Null if not found
	 */
	public PBotItem getItemFromInventoryAtLocation(int x, int y) {
		for(WItem witem : inventory.children(WItem.class)) {
			if(witem.c.div(UI.scale(33)).x == x && witem.c.div(UI.scale(33)).y == y) {
				return new PBotItem(witem.item, pBotSession);
			}
		}
		return null;
	}

	/**
	 * Drop item from the hand to given slot in inventory
	 * @param x x coordinate in inventory to drop the item into
	 * @param y y coordinate in inventory to drop the item into
	 */
	public void dropItemToInventory(int x, int y) {
		inventory.wdgmsg("drop", new Coord(x, y));
	}

	/**
	 * Amount of free slots in the inventory
	 * @return Amount of free inventory slots
	 */
	public int freeSlotsInv() {
		return inventory.isz.x * inventory.isz.y -  inventory.children(WItem.class)
				.stream().map(witem -> witem.sz.div(UI.scale(33)))
				.mapToInt((c) -> c.x * c.y)
				.sum();
	}

	/**
	 * Transfer 1 item by scrolling
	 */
	public void xferTo() {
		pBotSession.PBotUtils().playerInventory().inventory.wdgmsg("invxf", inventory.wdgid(), 1);
	}

	/**
	 * Size of the inventory
	 * @return Size
	 */
	public Coord size() {
		return this.inventory.isz;
	}
}
