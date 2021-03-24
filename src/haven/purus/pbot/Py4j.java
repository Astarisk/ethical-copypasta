package haven.purus.pbot;
import haven.purus.pbot.api.PBotSession;
import py4j.GatewayServer;

public class Py4j {

	public static GatewayServer server;

	public interface PBotScriptLoader {
		public void start(String scriptPath, PBotSession pBotSession);
	}

	public static void start() {
		try {
			ProcessBuilder pb = new ProcessBuilder("python", "scripts/loader.py");
			pb.redirectError(ProcessBuilder.Redirect.INHERIT);
			pb.inheritIO();
			Process p = pb.start();
			Runtime.getRuntime().addShutdownHook(new Thread(p::destroyForcibly));
		} catch(Exception e) {
			e.printStackTrace();
		}
		new Thread(() -> {
			server = new GatewayServer();
			server.start();
		}, "PBot Runner").start();
	}
}
