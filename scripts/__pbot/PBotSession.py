from __future__ import annotations

from .PBotCharacterAPI import PBotCharacterAPI
from .PBotGobAPI import PBotGobAPI
from .PBotUtils import PBotUtils
from .PBotWindowAPI import PBotWindowAPI


class PBotSession(object):
    def __init__(self, session):
        self.PBotUtils: PBotUtils = PBotUtils(session)
        self.PBotGobAPI: PBotGobAPI = PBotGobAPI(session)
        self.PBotWindowAPI: PBotWindowAPI = PBotWindowAPI(session)
        self.PBotCharacterAPI: PBotCharacterAPI = PBotCharacterAPI(session)
