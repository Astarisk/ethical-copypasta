class Script:
    def run(self, sess):
        PBotUtils = sess.PBotUtils()
        PBotGobAPI = sess.PBotGobAPI()

        closestHerb = PBotGobAPI.getClosestGobByResname("gfx/terobjs/herbs/.*|gfx/kritter/jellyfish/jellyfish")
        if closestHerb == None or closestHerb.dist(PBotGobAPI.getPlayer()) > 8*11:
            closestHerb = PBotGobAPI.getClosestGobByResname("gfx/kritter/.*")
        if closestHerb != None and closestHerb.dist(PBotGobAPI.getPlayer()) < 8*11:
            closestHerb.doClick(3, 0)
            menu = PBotUtils.getFlowermenu(5000)
            if menu != None:
                menu.choosePetal("Pick")
