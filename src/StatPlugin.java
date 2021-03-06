package net.dugged.cutelessmod;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class StatPlugin {
	private final ExecutorService service = Executors.newCachedThreadPool();
	public long lastTick = 0;
	private DataOutputStream streamOut;
	private Socket socket;
	private int amount;
	private boolean fullStatistics;

	public void connect() throws IOException {
		socket = new Socket("127.0.0.1", 8192);
		streamOut = new DataOutputStream(socket.getOutputStream());
		amount = 0;
		fullStatistics = false;
	}

	public void disconnect() throws IOException {
		if (socket != null) {
			socket.close();
			streamOut.close();
			socket = null;
			streamOut = null;
		}
	}

	public boolean isConnected() {
		return socket != null && socket.isConnected() && !socket.isClosed();
	}

	public void sendStatIncrease(int count, boolean syncStats) {
		amount = count;
		fullStatistics = syncStats;
		service.submit(new StatPluginThread());
	}

	class StatPluginThread implements Runnable {
		public void run() {
			try {
				if (fullStatistics) {
					streamOut.writeUTF("sync stat: " + amount);
				} else {
					streamOut.writeUTF("increase stat: " + amount);
				}
				streamOut.flush();
			} catch (IOException e) {
				CutelessMod.LOGGER.info("An error occured while sending to the OBS-Plugin!");
			}
		}
	}
}
