from py4j.java_gateway import JavaGateway, CallbackServerParameters, GatewayParameters
import importlib
import threading

class PBotRunner(object):

    def start(scriptName, pBotSession):
        importlib.invalidate_caches()
        script = importlib.import_module(scriptName)
        importlib.reload(script)
        threading.Thread(target=script.Script.run, args=[pBotSession]).start()
    class Java:
        implements = ["haven.purus.pbot.Py4j.PBotScriptLoader"]
gateway = JavaGateway(callback_server_parameters=CallbackServerParameters(),
                      python_server_entry_point=PBotRunner,
                      gateway_parameters=GatewayParameters(auto_field=True))
print("0")
