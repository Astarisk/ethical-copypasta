class Script:
    def getBeltInv(self, sess):
        belt = sess.PBotWindowAPI().getWindow("Belt")
        if belt != None:
            inv = belt.getInventories()
            if len(inv) > 0:
                return inv[0]
        return None

    def run(self, sess):
        PBotUtils = sess.PBotUtils()
        PBotGobAPI = sess.PBotGobAPI()
        PBotWindowAPI = sess.PBotWindowAPI()
        invs = [PBotUtils.playerInventory(), self.getBeltInv(sess)]
        for inv in filter(None, invs):
            for itm in filter(lambda x: str(x.getContentsName()).endswith("of Water"), inv.getInventoryItemsByResnames(".*")):
                itm.activateItem();
                menu = PBotUtils.getFlowermenu(5000)
                if menu != None:
                    menu.choosePetal("Drink")
                    return
