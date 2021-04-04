import itertools

class Script:
    def getWInv(self, wnd):
        print(wnd)
        if wnd != None:
            inv = wnd.getInventories()
            if len(inv) > 0:
                return inv[0]
        return None

    def run(self, sess):
        PBotUtils = sess.PBotUtils()
        PBotGobAPI = sess.PBotGobAPI()
        invs = [PBotUtils.playerInventory(), self.getWInv(sess.PBotWindowAPI().getWindow("Belt"))]
        for inv in filter(None, invs):
            for itm in filter(lambda x: x != None and str(x.getContentsName()).endswith("of Water"), itertools.chain(inv.getInventoryItemsByResnames(".*"), sess.PBotCharacterAPI().getEquipment())):
                itm.activateItem();
                menu = PBotUtils.getFlowermenu(5000)
                if menu != None:
                    menu.choosePetal("Drink")
                    return
