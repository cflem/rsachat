import java.io.IOException;

public abstract class UserInterface implements Runnable {
	private Thread runner;
	protected ChatClient cli;

	public void createUser () throws IOException {
		final RSAPrivateKey usrkey = RSAPrivateKey.keygen(2048);
		final String uname = unamePrompt();
		CacheManager.setCache(new Cache(usrkey, uname));
		runner = new Thread (new Runnable() {
			public void run () {
				try {
					cli.signup(uname, usrkey);
				} catch (IOException e) {}
			}
		});
		runner.start();
	}
	
	public void loadCacheUser () throws IOException {
		final Cache me = CacheManager.getCache();
		runner = new Thread (new Runnable() {
			public void run () {
				try {
					cli.login(me.getUname(), me.getKey());
				} catch (IOException e) {}
			}
		});
		runner.start();
	}

	public void startRunner (String server, int port) {
		try {
			cli = new ChatClient(this, server, port);
			if (CacheManager.exists()) loadCacheUser();
			else createUser();
		} catch (Exception e) {}

	}

	public abstract String unamePrompt ();
	public abstract void displayMessage (String sender, String message);
	public abstract void disconnect ();
	public abstract void sendMessage (String to, String message);
	public abstract void readyUp ();

}
