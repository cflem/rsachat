public class Cache {
	private RSAPrivateKey key;
	private String uname;
	
	public Cache (RSAPrivateKey key, String uname) {
		this.key = key;
		this.uname = uname;
	}
	
	public RSAPrivateKey getKey () { return key; }
	public String getUname () { return uname; }

}
