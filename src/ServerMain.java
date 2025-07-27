import sdp2021.Server;

import java.io.IOException;

public class ServerMain {
	public static void main(String[] args) throws IOException, InterruptedException {
//		String folder = SDP.path(false,"sdp2021" , "resources" , "serverStorage");
		Thread server = Server.getInstance().start();
//		Thread.sleep(10 * 1000);
//		server.interrupt();
	}
}
