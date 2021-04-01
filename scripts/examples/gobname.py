#Add text to selected gob
#used to demonstrate PBot API callbacks
import time
class GobListener(object):
    # This will be called with a new python thread
    # other callbacks use the same form
    # they may return different objects, object is mentioned in callback register function
    def callback(self, gob):
        id = gob.addGobText("Selected", 255, 255, 0, 128, 12)
        time.sleep(5)
        gob.removeGobText(id)

    class Java:
        implements = ["haven.purus.pbot.api.Callback"]

class Script:
    def run(self, sess):
        PBotGobAPI = sess.PBotGobAPI()
        PBotGobAPI.selectGob(GobListener())
