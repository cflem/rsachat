import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.Arrays;

public class ChatClient {
	private UserInterface ui;
	private String server;
	private int port;
	private Socket sockfd;
	private byte[] authkey;
	
	public ChatClient (UserInterface ui, String server, int port) {
		this.ui = ui;
		this.server = server;
		this.port = port;
		this.sockfd = null;
	}
	
	public void login (String username, RSAPrivateKey userkey) throws IOException {
		try {
			sockfd = new Socket(server, port);
			BufferedInputStream bis = new BufferedInputStream(sockfd.getInputStream());
			BufferedOutputStream bos = new BufferedOutputStream(sockfd.getOutputStream());
			byte[] buffer = new byte[256];
			bis.read(buffer);
			if (!new String(buffer).trim().equalsIgnoreCase("GIVE ME A COOKIE")) throw new IOException("Server isn't responding correctly.");
			bos.write(("EXISTS "+username).getBytes());
			bos.flush();
			Arrays.fill(buffer, (byte)0);
			bis.read(buffer);
			if (new String(buffer).trim().equalsIgnoreCase("GET OUT")) return;
			// buffer is now full of the authentication key, but encrypted
			authkey = rsaunpad(padNum(RSABase.decryptBlock(buffer, userkey), (userkey.getSize()/8)-1));
			bos.write(authkey);
			bos.flush();
			Arrays.fill(buffer, (byte)0);
			bis.read(buffer);
			if (!new String(buffer).trim().equalsIgnoreCase("OK")) return;
			baseProtocol();
		} catch (IOException e) {
			return;
		} finally {
			ui.disconnect();
			sockfd.close();
		}
	}
	
	public void signup (String username, RSAPrivateKey userkey) throws IOException { // the key is assumed to be 2048-bit as of now
		try {
			sockfd = new Socket(server, port);
			BufferedInputStream bis = new BufferedInputStream(sockfd.getInputStream());
			BufferedOutputStream bos = new BufferedOutputStream(sockfd.getOutputStream());
			byte[] buffer = new byte[256];
			bis.read(buffer);
			if (!new String(buffer).trim().equalsIgnoreCase("GIVE ME A COOKIE")) throw new IOException("Server isn't responding correctly.");
			bos.write(("CREATE "+username).getBytes());
			bos.flush();
			Arrays.fill(buffer, (byte)0);
			bis.read(buffer);
			if (!new String(buffer).trim().equalsIgnoreCase("SEND")) return;
			byte[] mod = padNum(userkey.getMod().toByteArray(), userkey.getSize()/8);
			bos.write(mod);
			bos.flush();
			Arrays.fill(buffer, (byte)0);
			bis.read(buffer);
			if (!new String(buffer).trim().equalsIgnoreCase("GOT")) return;
			byte[] pE = userkey.getPublicExponent().toByteArray();
			pE = padNum(padNum(pE, pE.length), 16);
			bos.write(pE);
			bos.flush();
			Arrays.fill(buffer, (byte)0);
			bis.read(buffer);
			authkey = rsaunpad(padNum(RSABase.decryptBlock(buffer, userkey), (userkey.getSize()/8)-1));
			bos.write(authkey);
			bos.flush();
			Arrays.fill(buffer, (byte)0);
			bis.read(buffer);
			if (!new String(buffer).trim().equalsIgnoreCase("OK")) return;
			baseProtocol();
		} catch (Exception e) {
			e.printStackTrace();
			return;
		} finally {
			ui.disconnect();
			sockfd.close();
		}
	}
	
	public void sendMessage (String to, String message) throws IOException {
		sendMessage(to.getBytes(), message.getBytes());
	}
	
	public void sendMessage (byte[] to, byte[] message) throws IOException {
		try {
			int tolen = to.length, mlen = message.length;
			if (tolen+mlen+8 > 256) return; // the server isn't waiting for anything bigger than 256
			byte[] buffer = new byte[tolen+mlen+8];
			stampInt(buffer, tolen);
			for (int i = 0; i < tolen; i++) {
				buffer[i+4] = to[i];
			}
			stampInt(buffer, tolen+4, mlen);
			for (int i = 0; i < mlen; i++) {
				buffer[tolen+8+i] = message[i];
			}
			
			encrypt(buffer, authkey);
			BufferedOutputStream bos = new BufferedOutputStream(sockfd.getOutputStream());
			bos.write(buffer);
			bos.flush();
		} catch (IOException e) {
			ui.disconnect();
			sockfd.close();
		}
	}
	
	private void baseProtocol () throws IOException {
		BufferedInputStream bis = new BufferedInputStream(sockfd.getInputStream());
		BufferedOutputStream bos = new BufferedOutputStream(sockfd.getOutputStream());
		bos.write(("READY").getBytes());
		bos.flush();
		ui.readyUp();
		
		byte[] buffer = new byte [512];
		int len;
		while ((len = bis.read(buffer)) >= 0) { // 4 bytes for sender length, 4 bytes for message length
			if (len < 8) continue;
			decrypt(buffer, authkey);
			int senderlen = toInt(buffer); // ntohl isn't a thing
			if (senderlen+9 > 512) throw new IOException("Sender length too long: "+senderlen);
			String sender = new String(Arrays.copyOfRange(buffer, 4, senderlen+4));
			int msglen = toInt(buffer, 4+senderlen);
			if (senderlen+msglen+9 > 512) throw new IOException("Message too long: "+msglen);
			String msg = new String(Arrays.copyOfRange(buffer, 8+senderlen, 8+senderlen+msglen));
			ui.displayMessage(sender, msg);
		}
		return;
	}
	
	private void encrypt (byte[] data, byte[] key) { // this is a play on the vigenere
		if (key.length < 1) System.out.println("No key found.");
		for (int i = 0; i < data.length; i++) {
			data[i] ^= key[i%key.length];
		}
	}
	
	private void decrypt (byte[] data, byte[] key) {
		encrypt(data, key);
	}
		
	private byte[] padNum (byte[] raw, int len) {
		int diff = len-raw.length;
		if (diff < 0) return Arrays.copyOfRange(raw, -diff, raw.length);
		byte[] padded = new byte[len];
		for (int i = diff; i < len; i++) {
			padded[i] = raw[i-diff];
		}
		return padded;
	}
	
	private void stampInt (byte[] buff, int n) {
		stampInt(buff, 0, n);
	}
	
	private void stampInt (byte[] buff, int offset, int n) {
		buff[offset] = (byte) ((n>>24)&0xff);
		buff[offset+1] = (byte) ((n>>16)&0xff);
		buff[offset+2] = (byte) ((n>>8)&0xff);
		buff[offset+3] = (byte) (n&0xff);
	}
	
	private int toInt (byte[] raw) {
		return toInt(raw, 0);
	}
	
	private int toInt (byte[] raw, int start) {
		int[] uchars = new int[4];
		for (int i = 0; i < 4; i++) {
			uchars[i] = (raw[start+i] < 0) ? raw[start+i]+256 : raw[start+i];
		}
		return (uchars[0] << 24) | (uchars[1] << 16) | (uchars[2] << 8) | uchars[3];
	}
		
	private byte[] rsaunpad (byte[] padded) {
		int len = toInt(padded); // ntohl isn't a thing
		if (len > padded.length-1 || len < 0) return padded; // padding is broken
		return Arrays.copyOfRange(padded, 4, len+4);
	}

}
