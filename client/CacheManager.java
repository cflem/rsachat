import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigInteger;

public class CacheManager {
	
	public static boolean exists () {
		return (new File(".rsachat")).exists();
	}
	
	public static Cache getCache () throws IOException {
		if (!exists()) return null;
		FileInputStream fis = new FileInputStream(new File(".rsachat"));
		byte[] unlb = new byte[4];
		fis.read(unlb, 0, 4);
		int unl = toInt(unlb);
		byte[] unameb = new byte[unl];
		fis.read(unameb, 0, unl);
		byte[] modlb = new byte[4];
		fis.read(modlb, 0, 4);
		int modl = toInt(modlb);
		byte[] modb = new byte[modl];
		fis.read(modb, 0, modl);
		byte[] elb = new byte[4];
		fis.read(elb, 0, 4);
		int el = toInt(elb);
		byte[] eb = new byte[el];
		fis.read(eb, 0, el);
		byte[] dlb = new byte[4];
		fis.read(dlb, 0, 4);
		int dl = toInt(dlb);
		byte[] db = new byte[dl];
		fis.read(db, 0, dl);
		fis.close();
		return new Cache(new RSAPrivateKey(new BigInteger(modb), new BigInteger(eb), new BigInteger(db)), new String(unameb));
	}
	
	public static void setCache (Cache cache) throws IOException {
		byte[] unameb = cache.getUname().getBytes();
		RSAPrivateKey k = cache.getKey();
		byte[] modb = k.getMod().toByteArray();
		byte[] eb = k.getPublicExponent().toByteArray();
		byte[] db = k.getPrivateExponent().toByteArray();
		FileOutputStream fos = new FileOutputStream(new File(".rsachat"));
		fos.write(deInt(unameb.length));
		fos.write(unameb);
		fos.write(deInt(modb.length));
		fos.write(modb);
		fos.write(deInt(eb.length));
		fos.write(eb);
		fos.write(deInt(db.length));
		fos.write(db);
		fos.close();
	}
	
	private static byte[] deInt (int n) {
		byte[] ret = new byte[4];
		ret[0] = (byte) ((n>>24) & 0xff);
		ret[1] = (byte) ((n>>16) & 0xff);
		ret[2] = (byte) ((n>>8) & 0xff);
		ret[3] = (byte) (n & 0xff);
		return ret;
	}
	
	private static int toInt (byte[] buff) {
		return (buff[0]<<24) | (buff[1]<<16) | (buff[2]<<8) | buff[3];
	}

}
