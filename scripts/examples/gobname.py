import time
class GobListener(object):
    def selected(self, gob):
        id = gob.addGobText("Selected", 255, 255, 0, 128, 12)
        time.sleep(5)
        gob.removeGobText(id)

    class Java:
        implements = ["haven.purus.pbot.api.GobCallback"]

class Script:
    def run(sess):
        PBotGobAPI = sess.PBotGobAPI()
        PBotGobAPI.selectGob(GobListener())
