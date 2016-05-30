import java.io.IOException;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.Scanner;

public class RSABase {
	
	public static void main (String[] args) throws IOException {
		RSAPrivateKey k = RSAPrivateKey.keygen(2048);
		Scanner s = new Scanner(System.in);
		System.out.println("Print message (One Line):");
		byte[] pln = s.nextLine().getBytes();
		s.close();
		byte[] cool = RSABase.encrypt(pln, RSAPublicKey.derive(k));
		byte[] uncool = RSABase.decrypt(cool, k);
		System.out.println(new String(uncool));
	}
	
	public static byte[] pad (byte[] plaintext, int keysize) {
		byte[] padded = new byte[(8*plaintext.length+(keysize-((8*plaintext.length)%(keysize+1))))/8];
		for (int i = 0; i < plaintext.length; i++) padded[i] = plaintext[i];
		return padded;
	}
	
	public static byte[] encrypt (byte[] plaintext, RSAPublicKey pk) {
		plaintext = pad(plaintext, pk.getSize());
		int blocks = (plaintext.length*8)/pk.getSize(), blocksize = pk.getSize()/8;
		byte[] ciphertext = new byte[plaintext.length];
		for (int i = 0; i < blocks; i++) {
			byte[] blocktext = Arrays.copyOfRange(plaintext, blocksize*i, blocksize*(i+1));
			byte[] blockcipher = encryptBlock(blocktext, pk);
			for (int j = 0; j < blocksize; j++) {
				ciphertext[blocksize*i+j] = blockcipher[j];
			}
		}
		return ciphertext;
	}

	public static byte[] decrypt (byte[] ciphertext, RSAPrivateKey priv) {
		int blocks = (ciphertext.length*8)/priv.getSize(), blocksize = priv.getSize()/8;
		byte[] plaintext = new byte[ciphertext.length];
		for (int i = 0; i < blocks; i++) {
			byte[] blockcipher = Arrays.copyOfRange(ciphertext, blocksize*i, blocksize*(i+1));
			byte[] blocktext = decryptBlock(blockcipher, priv);
			for (int j = 0; j < blocksize; j++) {
				plaintext[blocksize*i+j] = blocktext[j];
			}
		}
		return plaintext;		
	}
	
	public static byte[] encryptBlock (byte[] plaintext, RSAPublicKey pk) {
		BigInteger msg = new BigInteger(plaintext);
		return msg.modPow(pk.getPE(), pk.getMod()).toByteArray();
	}
	
	public static byte[] decryptBlock (byte[] ciphertext, RSAPrivateKey priv) {
		BigInteger cipher = new BigInteger(ciphertext);
		byte[] plain = cipher.modPow(priv.getPrivateExponent(), priv.getMod()).toByteArray();
		return plain;
	}
	
}