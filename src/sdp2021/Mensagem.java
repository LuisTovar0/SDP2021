package sdp2021;


import java.io.DataInputStream;
import java.io.IOException;

public class Mensagem {
	public final int version;
	public final SDP.Code code;
	public final int nBytes;
	public final byte[] dados;

	/**
	 * <p>Lê os campos e os dados do socket.</p>
	 * <p>Estes ficam armazenados na instância criada.</p>
	 *
	 * @param sdp o socket de onde se vai ler os dados
	 * @throws IOException na situação de não ser possível ler do socket
	 */
	public Mensagem(SDP sdp) throws IOException {
		DataInputStream dis = new DataInputStream(sdp.getSocket().getInputStream());
		byte bVersion = dis.readByte();
		byte bCode = dis.readByte();
		byte bNBytes = dis.readByte();

		this.version = bVersion < 0 ? bVersion + 256 : bVersion;
		int iCode = bCode < 0 ? bCode + 256 : bCode;
		this.code = SDP.Code.valueOf(iCode);
		this.nBytes = bNBytes < 0 ? bNBytes + 256 : bNBytes;
		this.dados = new byte[nBytes];
		for (int i = 0; i < nBytes; i++)
			dados[i] = dis.readByte();

		System.out.println("versão " + version + "; código " + code + "; " + nBytes + " bytes");
	}
}
