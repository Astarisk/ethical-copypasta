#Color gobs in the selected area
#used to demonstrate PBot API callbacks
import time
import random
session = None
class Listener(object):
    def callback(self, area):
        for gob in session.PBotGobAPI().gobsInArea(area.getA(), area.getB()):
            gob.setMarked(*[random.randint(0, 255) for x in range(4)])

    class Java:
        implements = ["haven.purus.pbot.api.Callback"]

class Script:
    def run(self, sess):
        global session
        session = sess
        PBotGobAPI = sess.PBotGobAPI()
        sess.PBotUtils().selectArea(Listener())
