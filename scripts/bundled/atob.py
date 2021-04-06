class Script:
    def run(self, sess):
        PBotUtils = sess.PBotUtils()
        PBotGobAPI = sess.PBotGobAPI()
        gob = sess.PBotGobAPI().getClosestGobByResname(".*")
        if gob == None:
            return
        wndN = gob.windowNameForGob()
        print(gob.getResname())
        if wndN == None:
            return
        print(gob)
        gob.doClick(3,0)
        wnd = sess.PBotWindowAPI().waitForWindow(wndN, 1000*5)
        if wnd == None:
            return
        invs = wnd.getInventories()
        if len(invs) < 1:
            return
        for x in range(invs[0].freeSlotsInv()):
            invs[0].xferTo()
