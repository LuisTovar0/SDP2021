import sdp2021.Client;
import sdp2021.SDP;
import sdp2021.Server;

import java.io.IOException;
import java.util.Scanner;

public class ClienteEmissor {
	public static final String EMISSOR_CLI_NAME = "cliente_emissor";

	public static void main(String[] args) throws IOException {
		Scanner scanner = new Scanner(System.in);
		System.out.print("Server's IP address: ");
		String IPADD = scanner.next();

		Client client = new Client(IPADD, Server.port, EMISSOR_CLI_NAME);
		String clientFiles = SDP.path(false, "sdp2021", "resources", "clientFiles");
//		Thread.sleep(5 * 1000);
		System.out.println("enviar cabeçalho");
		client.sendFile(SDP.path(true, clientFiles, "cabeçalho.txt"), "Os_Lusíadas_Cabeçalho.txt");
//		Thread.sleep(5 * 1000);
		System.out.println("enviar canto primeiro");
		client.sendFile(SDP.path(true, clientFiles, "canto1.txt"), "Os_Lusíadas_Canto_Primeiro.txt");
//		Thread.sleep(5 * 1000);
		System.out.println("enviar canto segundo");
		client.sendFile(SDP.path(true, clientFiles, "canto2.txt"), "Os_Lusíadas_Canto_Segundo.txt");
		client.fechaLigacao();
	}
}
