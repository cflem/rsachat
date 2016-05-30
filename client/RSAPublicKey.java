import java.math.BigInteger;

public class RSAPublicKey {
	private BigInteger modulus;
	private BigInteger publicExponent;
	private int keysize;
	
	public RSAPublicKey (BigInteger modulus, BigInteger publicExponent) {
		this.modulus = modulus;
		this.publicExponent = publicExponent;
		this.keysize = (int)Math.pow(2, Math.ceil(Math.log(modulus.bitLength())/Math.log(2)));
	}
	
	public int getSize () {
		return keysize;
	}
	
	public BigInteger getMod () {
		return modulus;
	}
	
	public BigInteger getPE () {
		return publicExponent;
	}
	
	public static RSAPublicKey derive (RSAPrivateKey k) {
		return new RSAPublicKey(k.getMod(), k.getPublicExponent());
	}

}
