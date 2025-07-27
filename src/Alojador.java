import sdp2021.*;

import java.io.IOException;
import java.util.Scanner;

public class Alojador {
	public static final String ALOJADOR_CLI_NAME = "alojador";

	public static void main(String[] args) throws IOException {
//		System.out.println(InetAddress.getLocalHost().getHostAddress().trim());
		System.out.print("insira o IP do Centro de distribuição: ");
		Scanner scanner = new Scanner(System.in);
		String ipCD = scanner.next();
		System.out.print("insira o IP com que registar o alojador: ");
		String ipAl = scanner.next();


		Client alojadorClient = new Client(ipCD, Server.port, ALOJADOR_CLI_NAME);
		// enviar disponibilidade ao Centro de distribuição
		byte[] IP = ipAl.trim().getBytes();
		alojadorClient.enviaMensagem(SDP.Code.DISPONIVEL, IP);
		Mensagem mensagem = new Mensagem(alojadorClient);
		if (mensagem.code != SDP.Code.ENTENDIDO) {
			alojadorClient.fechaLigacao();
			throw new IOException("não foi possível informar disponibilidade ao centro de distribuição");
		}
		alojadorClient.fechaLigacao();
		System.out.println("disponibilidade de alojador foi informada ao centro de distribuição");

		// iniciar receção de ficheiros
		Server.getInstance().start();
	}
}
