import java.math.BigInteger;
import java.util.Random;

public class RSAPrivateKey {
	private BigInteger modulus, e, d;
	private int keysize;
	
	public RSAPrivateKey (BigInteger modulus, BigInteger e, BigInteger d) {
		this.modulus = modulus;
		this.e = e;
		this.d = d;
		this.keysize = (int)Math.pow(2, Math.ceil(Math.log(modulus.bitLength())/Math.log(2)));
	}
	
	public int getSize () {
		return keysize;
	}
	
	public BigInteger getMod () {
		return modulus;
	}
	
	public BigInteger getPublicExponent () {
		return e;
	}
	
	public BigInteger getPrivateExponent () {
		return d;
	}
	
	public static RSAPrivateKey keygen (int size) {
		int psize = (size/2)+((int)(Math.random()*5+2));
		int qsize = size-psize;
		Random rand = new Random();
		BigInteger p = BigInteger.probablePrime(psize, rand);
		BigInteger q = BigInteger.probablePrime(qsize, rand);
		BigInteger modulus = p.multiply(q);
		BigInteger e = BigInteger.valueOf(65537L); // this is the usual, doesn't really matter
		BigInteger toitient = p.subtract(BigInteger.ONE).multiply(q.subtract(BigInteger.ONE));
		BigInteger d = e.modInverse(toitient);
		return new RSAPrivateKey(modulus, e, d);
	}
	
	public String toString () {
		return "Modulus: 0x"+modulus.toString(16)+"\nPublic Exponent: 0x"+e.toString(16)+"\nPrivate Exponent: 0x"+d.toString(16);
	}

}
