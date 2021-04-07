from typing import List

class PBotFlowerMenu(object):
    def __init__(self, menu):
        self.__menu = menu

    ## Close this flowermenu
    def close(self):
        self.__menu.closeMenu()

    ## Choose option
    # @param name Exact name of the option
    def choose_petal(self, name: str):
        self.__menu.choosePetal(name)

    ## Choose option by number
    # @param num option number 0-indexed
    def choose_petal_num(self, num: int):
        self.__menu.choosePetal(num)

    ## Get options of this flowermenu
    def get_petal_names(self) -> List[str]:
        return list(self.__menu.getPetalNames())