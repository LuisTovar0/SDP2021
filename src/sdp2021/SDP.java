package sdp2021;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public interface SDP {
	int version = 0;
	int max_bytes = 255;
	String fileSep = System.getProperty("file.separator");
	String PWD = "estudasses";

	Socket getSocket();

	default void enviaMensagem(Code code) throws IOException {
		DataOutputStream dos = new DataOutputStream(getSocket().getOutputStream());
		dos.writeByte(version); // versão
		dos.writeByte(code.codigo); // código dado
		dos.writeByte(0); // n_bytes = 0
	}

	default void enviaMensagem(Code code, byte[] dados) throws IOException {
		DataOutputStream dos = new DataOutputStream(getSocket().getOutputStream());
		dos.writeByte(version); // versão
		dos.writeByte(code.codigo); // código dado
		int n_bytes = dados.length;
		dos.writeByte(n_bytes);
		dos.write(dados);
	}

	static String path(boolean isFile, String... args) {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < args.length; i++) {
			sb.append(args[i]);
			// se for um ficheiro, não adiciona / depois do último
			if (i != args.length - 1 || !isFile)
				sb.append(fileSep);
		}
		return sb.toString();
	}

	enum Code {
		TESTE(0),
		FIM(1),
		ENTENDIDO(2), // informa o cliente do sucesso de uma operação
		FIM_FICHEIRO(3), // fim do ficheiro
		NOME_FICHEIRO(4), // tem de ser enviado antes do conteúdo
		ERRO(5), // informa o cliente de uma falha
		// informa o centro de distribuição da disponibilidade de um alojador
		DISPONIVEL(6),
		SEGMENTO(255); // segmento do ficheiro

		public int codigo;

		Code(int codigo) {
			this.codigo = codigo;
		}

		public static Code valueOf(int c) {
			for (Code code : Code.values())
				if (c == code.codigo)
					return code;
			return null;
		}
	}
}
