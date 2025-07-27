package CentroDistribuicao;

import sdp2021.Client;
import sdp2021.Server;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.InetAddress;
import java.util.Collections;
import java.util.LinkedList;
import java.util.Scanner;

public class CentroDistribuicao {
	private final AlojadoresQueue alojadores = new AlojadoresQueue();
	private final Thread serverThread,
			analisaAlojadoresThread, analisaFicheirosThread;

	public static final String CENTRO_DISTR_CLI_NAME = "centro_distribuicao";

	public CentroDistribuicao() throws IOException {
		serverThread = Server.getInstance().start();
		analisaAlojadoresThread = analisarNovosAlojadores();
		analisaFicheirosThread = analisarFicheiros();
	}

	public void stop() {
		serverThread.interrupt();
		analisaAlojadoresThread.interrupt();
		analisaFicheirosThread.interrupt();
	}

	/**
	 * <p>verifica se há novos ficheiros para enviar, e envia-os</p>
	 */
	private Thread analisarFicheiros() {
		Thread thread = new Thread(() -> {
			System.out.println("analisando ficheiros recebidos de clientes emissores");
			File serverStorageDir = new File(Server.dir);
			while (!Thread.interrupted()) {
				String[] contents = serverStorageDir.list();
				try {
					Thread.sleep(300);
				} catch (InterruptedException e) {
					break;
				}
				if (contents != null) {
					LinkedList<String> c = new LinkedList<>();
					Collections.addAll(c, contents);
					c.remove("infos");
					c.remove("temp");
					if (c.size() > 0) {
						System.out.println("a enviar " + Server.dir + c.getFirst());
						sendFile(Server.dir + c.getFirst());
					}
				}
			}
		});
		thread.start();
		return thread;
	}

	/**
	 * realiza ciclo infinito sobre um ficheiro onde estão listados os alojadores
	 * e adiciona os novos à rotating queue
	 */
	private Thread analisarNovosAlojadores() {
		Thread thread = new Thread(() -> {
			System.out.println("analisando alojadores");
			Scanner fileScanner;
			try {
				//noinspection ResultOfMethodCallIgnored
				Server.alojadoresFile.createNewFile();
				fileScanner = new Scanner(Server.alojadoresFile);
			} catch (IOException e) {
				e.printStackTrace();
				return;
			}
			while (!Thread.interrupted()) {
				synchronized (Server.alojadoresFile) {
					if (fileScanner.hasNextLine()) {
						try {
							String line = fileScanner.nextLine();
							InetAddress ip = InetAddress.getByName(line);
							alojadores.add(ip);
						} catch (Exception ex) {
							ex.printStackTrace();
							return;
						}
					} else {
						try {
							fileScanner = new Scanner(Server.alojadoresFile);
						} catch (FileNotFoundException e) {
							e.printStackTrace();
							return;
						}
					}
				}
			}
		});
		thread.start();
		return thread;
	}

	private void sendFile(String fileName) {
		File file = new File(fileName);
		if (!file.exists()) {
			System.out.println("ficheiro não existe");
			return;
		}
		while (true) {
			InetAddress ipAlojador = alojadores.next();
			if (ipAlojador != null) {
				Client client;
				try {
					client = new Client(ipAlojador, Server.port, CENTRO_DISTR_CLI_NAME);
					client.sendFile(fileName, file.getName());
					client.fechaLigacao();
					System.out.println(file.delete()
							? "ficheiro enviado e apagado"
							: "ficheiro enviado mas não apagado");
					break;
				} catch (IOException ex) {
					alojadores.notAvailable(ipAlojador);
				}
			}
		}
	}

}
