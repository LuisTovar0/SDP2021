package CentroDistribuicao;

import java.net.InetAddress;
import java.util.LinkedList;
import java.util.Queue;

class AlojadoresQueue {
	private final Queue<AlojadorDTO> queue = new LinkedList<>();

	public boolean contains(InetAddress elem) {
		synchronized (queue) {
			for (AlojadorDTO alojador : queue)
				if (alojador.ip.equals(elem))
					return true;
			return false;
		}
	}

	public int size() {
		synchronized (queue) {
			return queue.size();
		}
	}

	public void add(InetAddress ip) {
		if (!contains(ip))
			synchronized (queue) {
				queue.add(new AlojadorDTO(ip));
				System.out.println("alojador em " + ip.getHostAddress() + " adicionado");
			}
	}

	public InetAddress next() {
		synchronized (queue) {
			if (queue.size() > 0) {
				AlojadorDTO element;
				do {
					element = queue.remove();
					queue.add(element);
				} while (!element.disponivel);
				System.out.println(element);
				return element.ip;
			} else return null;
		}
	}

	public void notAvailable(InetAddress ipAlojador) {
		synchronized (queue) {
			for (AlojadorDTO alojador : queue)
				if (alojador.ip.equals(ipAlojador)) {
					alojador.disponivel = false;
					return;
				}
		}
	}

	private class AlojadorDTO {
		private final InetAddress ip;
		private boolean disponivel = true;

		public AlojadorDTO(InetAddress ip) {
			this.ip = ip;
		}

		public String toString() {
			return "Alojador em " + ip.getHostAddress() + " - " + (disponivel ? "disponivel" : "indisponível");
		}
	}
}

