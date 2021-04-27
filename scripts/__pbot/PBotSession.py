from __future__ import annotations
from typing import Optional

from .PBotCharacterAPI import PBotCharacterAPI
from .PBotGobAPI import PBotGobAPI
from .PBotUtils import PBotUtils
from .PBotWindowAPI import PBotWindowAPI


class PBotSession(object):
    def __init__(self, session):
        self.session = session
        self.PBotUtils: PBotUtils = PBotUtils(session)
        self.PBotGobAPI: PBotGobAPI = PBotGobAPI(session)
        self.PBotWindowAPI: PBotWindowAPI = PBotWindowAPI(session)
        self.PBotCharacterAPI: PBotCharacterAPI = PBotCharacterAPI(session)

    ## Create new session return None if unsuccessful
    def new_session(self, username: str, password: str, charname: str) -> Optional[PBotSession]:
        sess = self.session.newSession(username, password, charname)
        if sess is None:
            return None
        else:
            return PBotSession(sess)
