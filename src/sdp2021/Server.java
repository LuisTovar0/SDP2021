package sdp2021;

import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;
import java.io.*;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static sdp2021.SDP.Code.ENTENDIDO;
import static sdp2021.SDP.Code.ERRO;
import static sdp2021.SDP.PWD;
import static sdp2021.SDP.path;

public class Server {
	private final SSLServerSocket serverSocket;

	// certificado
	private static final String TRUSTED_STORE = "server_J.jks";
	// porto em que o servidor recebe sockets
	public static final int port = 32500;
	// diretório-base para o servidor
	public static final String dir = path(false, "sdp2021", "resources", "serverStorage");
	// diretório onde estará o ficheiro de alojadores
	public static final String dirInfos = path(false, dir, "infos");
	public static final String FICHEIRO_ALOJADORES = "alojadores.txt";
	public static final File alojadoresFile = new File(dirInfos + FICHEIRO_ALOJADORES);

	// singleton
	private static volatile Server instance;

	public static Server getInstance() throws IOException {
		synchronized (Server.class) {
			if (instance == null)
				instance = new Server();
		}
		return instance;
	}

	private Server() throws IOException {
		// Trust these certificates provided by authorized clients
		System.setProperty("javax.net.ssl.trustStore", TRUSTED_STORE);
		System.setProperty("javax.net.ssl.trustStorePassword", PWD);

		// Use this certificate and private key as server certificate
		System.setProperty("javax.net.ssl.keyStore", TRUSTED_STORE);
		System.setProperty("javax.net.ssl.keyStorePassword", PWD);

		SSLServerSocketFactory sslssf = (SSLServerSocketFactory) SSLServerSocketFactory.getDefault();
		serverSocket = (SSLServerSocket) sslssf.createServerSocket(port);
		serverSocket.setNeedClientAuth(true);

		System.out.println("Server running on port " + port + "...");
		Path basePath = Paths.get(dir);
		Path infosPath = Paths.get(dirInfos);
//		System.out.println(basePath.toAbsolutePath()+", "+infosPath.toAbsolutePath());
		Files.createDirectories(basePath);
		Files.createDirectories(infosPath);
		System.out.println("File directory: " + dir);
	}

	public Thread start() {
		Thread serverThread = new Thread(() -> {
			while (!Thread.interrupted()) {
				try {
					Socket clientSock = serverSocket.accept();
					new Thread(new ReceiveSocketThread(clientSock)).start();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		});
		serverThread.start();
		return serverThread;
	}

	private static class ReceiveSocketThread implements Runnable, SDP {

		/**
		 * além de servir para escrever o ficheiro, é usada como flag.
		 * se for null, significa que não se está a receber um ficheiro.
		 * se estiver atribuída, segnifica que se está a receber um ficheiro.
		 */
		private FileOutputStream fileOutputStream = null;
		private File file = null;
		private FileOutputStream disponiveisOS = null;
		private Mensagem mensagem;

		private final Socket clientSocket;

		@Override
		public Socket getSocket() {
			return clientSocket;
		}

		public ReceiveSocketThread(Socket clientSock) throws IOException {
			clientSocket = clientSock;
		}

		@Override
		public void run() {
			try {
				boolean fim = false; // flag que sinaliza o fim da ligação
				do {
					mensagem = new Mensagem(this);

					if (mensagem.code == null) {
						System.out.println("bruh no code");
						enviaMensagem(ERRO);
						continue;
					}

					switch (mensagem.code) {
						case TESTE:
							enviaMensagem(ENTENDIDO);
							break;
						case FIM:
							if (fileOutputStream != null) {
								System.out.println("este cliente devia ter enviado ficheiro ou segmento!");
								enviaMensagem(ERRO);
								System.out.println(file.delete() ? "ficheiro apagado" : "não foi possível apagar o ficheiro previamente criado");
							}
							fim = true;
							break;
						case NOME_FICHEIRO:
							if (fileOutputStream != null) {
								System.out.println("não pode enviar novo ficheiro sem fechar o anterior");
								enviaMensagem(ERRO);
								fim = true;
							}
							if (abreFicheiro())
								enviaMensagem(ENTENDIDO);
							else {
								enviaMensagem(ERRO);
								fim = true;
							}
							break;
						case SEGMENTO:
							if (fileOutputStream == null) {
								System.out.println("o cliente não enviou nome do ficheiro!");
								enviaMensagem(ERRO);
								fim = true;
							} else {
								if (mensagem.nBytes != max_bytes) {
									System.out.println("o segmento devia ter " + max_bytes + " bytes mas tinha " + mensagem.nBytes);
									enviaMensagem(ERRO);
								}
								fileOutputStream.write(mensagem.dados);
								enviaMensagem(ENTENDIDO);
								System.out.println("segmento recebido");
							}
							break;
						case FIM_FICHEIRO:
							if (fileOutputStream == null) {
								System.out.println("o cliente não enviou nome do ficheiro!");
								enviaMensagem(ERRO);
								fim = true;
							} else {
								fileOutputStream.write(mensagem.dados);
								fileOutputStream.close();
								enviaMensagem(ENTENDIDO);
								fileOutputStream = null;
								System.out.println("ficheiro fechado");
							}
							break;
						case DISPONIVEL:
							adicionaDisponivel();
							enviaMensagem(ENTENDIDO);
							break;
						default:
							System.out.println("O servidor não recebe mensagens do tipo " + mensagem.code);
							enviaMensagem(ERRO);
							fim = true;
					}
				} while (!fim);
				clientSocket.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		private void adicionaDisponivel() throws IOException {
			if (disponiveisOS == null) {
				disponiveisOS = new FileOutputStream(alojadoresFile);
				System.out.println("ficheiro de alojadores disponíveis criado");
			}
			synchronized (alojadoresFile) {
				System.out.print("server got alojador: ");
				System.out.println(new String(mensagem.dados));
				disponiveisOS.write(mensagem.dados);
				disponiveisOS.write("\n".getBytes());
			}
			enviaMensagem(ENTENDIDO);
		}

		private boolean abreFicheiro() throws IOException {
			String fileName = new String(mensagem.dados);
			System.out.println("nome de ficheiro recebido: " + fileName);
			try {
				file = new File(dir + fileName);
				fileOutputStream = new FileOutputStream(file.getAbsolutePath());
				System.out.println("ficheiro " + file.getPath() + " aberto");
				enviaMensagem(ENTENDIDO);
				return true;
			} catch (FileNotFoundException e) {
				System.out.println("não foi possível abrir o ficheiro\n" + e.getMessage());
				return false;
			}
		}
	}
}
