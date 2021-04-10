package haven.purus.pbot.api;

import haven.*;
import haven.res.ui.tt.q.qbuff.QBuff;

public class PBotItem {

	private PBotSession pBotSession;
	private GItem item;

	public PBotItem(GItem item, PBotSession pBotSession) {
		this.item = item;
		this.pBotSession = pBotSession;
	}

	public boolean equals(PBotItem itm) {
		return (item.wdgid() == itm.item.wdgid());
	}

	/**
	 * Returns name of the item content
	 * @return Name of the item content or null if not found
	 */
	public String getContentsName() {
		for(ItemInfo info : item.info()) {
			if(info instanceof ItemInfo.Contents) {
				for(ItemInfo info2: ((ItemInfo.Contents)info).sub) {
					if(info2 instanceof ItemInfo.Name) {
						return ((ItemInfo.Name) info2).str.text;
					}
				}
			}
		}
		return null;
	}

	/**
	 * Returns quality of the item content
	 * @return Quality of the item content or -1 if not found
	 */
	public double getContentsQuality() {
		for(ItemInfo info : item.info()) {
			if(info instanceof ItemInfo.Contents) {
				for(ItemInfo info2: ((ItemInfo.Contents)info).sub) {
					if(info2 instanceof QBuff) {
						return ((QBuff) info2).q;
					}
				}
			}
		}
		return -1;
	}

	/**
	 * Take the item to hand
	 * @param wait Wait for item to appear on hand
	 */
	public void takeItem(boolean wait) {
		item.wdgmsg("take", Coord.z);
		if(wait) {
			while(pBotSession.PBotUtils().getItemAtHand() == null) {
				PBotUtils.sleep(25);
			}
		}
	}

	/**
	 * Get the inventory that this item is located in
	 * @return Inventory or null if not found
	 */
	public PBotInventory getInventory() {
		if(item.witem().parent instanceof Inventory)
			return new PBotInventory((Inventory)item.witem().parent, pBotSession);
		else
			return null;
	}

	/**
	 * Transfer an item to the active inventory, does not wait for item to transfer
	 */
	public void transferItem() {
		item.wdgmsg("transfer", Coord.z);
	}

	/**
	 * Right clicks the item in the inventory
	 */
	public void activateItem() {
		item.wdgmsg("iact", Coord.z, 3);
	}

	/**
	 * Itemact
	 * @param mod modifier for example 1 = shift etc.
	 */
	public void itemact(int mod) {
		item.wdgmsg("itemact",  mod);
	}

	/**
	 * Get an amount of something such as seeds in a stack
	 * @return Amount of something in the item
	 */
	public int getAmount() {
		int ret = -1;
		synchronized(item.ui) {
			for(ItemInfo o : item.info()) {
				if(o instanceof GItem.Amount)
					ret = ((GItem.Amount) o).itemnum();
			}
		}
		return ret;
	}

	/**
	 * Get location of the item in inventory, ie. 5,4
	 * @return Coord-object, access x and y for coords
	 */
	public Coord getInvLoc() {
		return item.witem().c.div(33);
	}

	/**
	 * Returns name of the item if it exists
	 * @return Name of item or null
	 */
	public String getName() {
		synchronized(item.ui) {
			while(true) {
				try {
					for(Object o : item.info().toArray()) {
						if(o instanceof ItemInfo.Name)
							return ((ItemInfo.Name) o).str.text;
					}
					break;
				} catch(Loading l) { }
				PBotUtils.sleep(20);
			}
		}
		return null;
	}

	/**
	 * Returns resname of the item
	 * @return Resname of the item or null if not found
	 */
	public String getResname() {
		while(true) {
			try {
				Resource res = item.getres();
				if(res == null)
					return null;
				else
					return res.name;
			} catch(Loading l) {
			}
		}
	}

	/**
	 * Returns quality of the item, or -1 if quality was not found
	 * @return Quality of the item
	 */
	public double getQuality() {
		return item.witem().quality();
	}

	/**
	 * Returns quality of the item, wait until the quality is calculated for the item
	 * @return Quality of the item
	 */
	public double getQuality2() {
		while(getQuality() == -1)
			PBotUtils.sleep(5);
		return getQuality() ;
	}

	/*
	 * Checks if player can drink from the item
	 * Only checks water and tea
	 * @return True if player can drink, else false

	public boolean canDrinkFrom() {
		synchronized(gitem.ui) {
			Pattern liquidPattern = Pattern.compile(String.format("[0-9.]+ l of (%s)",
					String.join("|", new String[]{"Water", "Piping Hot Tea", "Tea"}), Pattern.CASE_INSENSITIVE));
			ItemInfo.Contents contents = getContents();
			if(contents != null && contents.sub != null) {
				for(ItemInfo info : contents.sub) {
					if(info instanceof ItemInfo.Name) {
						ItemInfo.Name name = (ItemInfo.Name) info;
						if(name.str != null && liquidPattern.matcher(name.str.text).matches())
							return true;
					}
				}
			}
		}
		return false;
	}*/

	/**
	 * Drops the item from the inventory to ground, does not wait for it to drop
	 */
	public void dropItemFromInventory() {
		item.wdgmsg("drop", Coord.z);
	}

	/**
	 * Get size of this item
	 * @return Size of item access x, y
	 */
	public Coord getSize() {
		return item.size();
	}

}
