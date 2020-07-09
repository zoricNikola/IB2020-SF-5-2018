package ib.project.keystore;

import java.security.KeyStore;

import ib.project.model.User;

// KEY STORE PASSWORD FOR ALL: password

public class KeyStoreReader {
	
	private static char[] password = "password".toCharArray();
	
	public static void createKeyStore(User user) {
		KeyStore keyStore = null;
		
		try {
			keyStore = KeyStore.getInstance("JKS", "SUN");
			keyStore.load(null, password);
		} catch (Exception e) {
			e.printStackTrace();
			System.err.println("Exception creating new keyStore");
		}
		
	}
		
}
