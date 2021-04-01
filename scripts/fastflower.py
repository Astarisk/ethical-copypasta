# Quickly rightclicks all items with the same name and chooses the first menu option
# For example, blocks into sticks
import time
session = None
class Listener(object):
    def callback(self, itm):
        inv = itm.getInventory()
        if inv != None:
            items = inv.getInventoryItemsByNames(itm.getName())
            for item in items:
                item.activateItem()
                menu = session.PBotUtils().getFlowermenu(1000*15)
                if menu != None:
                    menu.choosePetal(0)
                else:
                    break
    class Java:
        implements = ["haven.purus.pbot.api.Callback"]

class Script:
    def run(self, sess):
        global session
        session = sess
        PBotUtils = sess.PBotUtils()
        PBotUtils.selectItem(Listener())
