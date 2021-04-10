from typing import List, Tuple

class PBotGob(object):
    def __init__(self, gob):
        self._gob = gob

    ## Click the gob
    # @param btn 1 = left, 2 = middle, 3 = right
    # @param mod Key modifier mask 1 = shift 2 = ctrl 4 = alt
    # @param meshid can be a door, roasting spit etc.
    # @param olid gob overlay to click, for example roasting spit
    def do_click(self, btn: int = 1, mod: int = 0, meshid: int = -1, olid: int = 0):
        self._gob.doClick(btn, mod, meshid, olid)

    ## Click the gob with pathfinder
    # @param btn 1 = left, 2 = middle, 3 = right
    # @param mod Key modifier mask 1 = shift 2 = ctrl 4 = alt
    # @param meshid can be a door, roasting spit etc.
    # @param olid gob overlay to click, for example roasting spit
    def pf_click(self, btn: int = 1, mod: int = 0, meshid: int = -1, olid: int = 0):
        self._gob.pfClick(btn, mod, meshid, olid)

    ## Euclidean distance between this and target gob
    # @param gob target
    # @return the distance
    def dist(self, gob: "PBotGob") -> float:
        return float(self._gob.dist(gob._gob))

    ## Returns rc-coords of the gob
    # @return x and y coords
    def get_coords(self) -> Tuple[float, float]:
        c = self._gob.getCoords()
        return float(c.x), float(c.y),

    ## Get gob id of the gob
    # @return id
    def get_id(self) -> int:
        return int(self._gob.getId())

    ## Check if stockpile is full
    # @return true if stockpile is full else false
    def stockpile_is_full(self) -> bool:
        return self._gob.stockpileIsFull()

    ## Itemact with gob, to fill trough with item in hand for example
    # @param mod 1 = shift, 2 = ctrl, 4 = alt, combine bits for multiple
    def item_click(self, mod: int):
        self._gob.itemClick(mod)

    ## Add cool hovering text above the gob
    # @param text text to add
    # @param height height that the text hovers at
    # @param r red color 0-255
    # @param g green color 0-255
    # @param b blue color 0-255
    # @return id of text used to remove it
    def add_gob_text(self, text: str, height: int = 20, r: int = 255, g: int = 255, b: int = 255) -> int:
        return self._gob.addGobText(text, r, g, b, 255, height)

    ## Remove the added hovering text from gob that was added with add_gob_text
    # @param id id of the gobtext
    def remove_gob_text(self, id: int):
        self._gob.removeGobText(id)

    ## Returns the name of the gobs resource file or null if not found
    # @return gob resname
    def get_resname(self) -> str:
        return self._gob.getResname()

    ## Get name of window for gob from gobWindowMap
    # @return Window name
    def window_name_for_gob(self) -> str:
        return self._gob.windowNameForGob()

    ## Set gob as colored, replaces previous if color already set
    # @param r Red between 0-255
    # @param g Green between 0-255
    # @param b Blue between 0-255
    # @param a Alpha between 0-255
    def set_marked(self, r: int = 255, g: int = 0, b: int = 0, a: int = 255):
        self._gob.setMarked(r, g, b, a);

    ## Remove color marking
    def set_unmarked(self):
        self._gob.setUnmarked()

    ## Check if the object is moving
    # @return retuns true if gob is moving, false otherwise
    def is_moving(self) -> bool:
        return self._gob.isMoving()

    ## Get speed of this gob if it is moving
    # @return speed of gob
    def get_speed(self) -> float:
        return self._gob.getSpeed()

    ## Returns resnames of poses of this gob if it has any poses
    # @return resnames of poses
    def get_poses(self) -> List[str]:
        return list(self._gob.getPoses())

    ## Get overlays of the gob, get meshid with get_overlay_id
    # @return resnames of poses
    def get_overlay_names(self) -> List[str]:
        return list(self._gob.getOverlayNames())

    ## Return meshid of the overlay with the given resname
    # @param overlay_name exact match
    # @return meshid of the overlay or -1 if not found
    def get_overlay_id(self, overlay_name: str) -> int:
        return self._gob.getOverlayId(overlay_name)

    ## Sdt may tell information about things such as tanning tub state, crop stage etc.
    # @return sdt of the gob, -1 if not found
    def get_sdt(self) -> int:
        return self._gob.getSdt()

    ## Check if the gob is KO/dead
    # @return true if the animal is knocked out, false if not
    def is_knocked(self) -> bool:
        return self._gob.isKnocked()
