package ib.project.keystore;

import java.io.FileOutputStream;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.cert.Certificate;

public class KeyStoreWriter {
	
	public static void addToKeyStore(KeyStore keyStore, String alias, PrivateKey privateKey, char[] password, Certificate certificate) {
		try {
			keyStore.setKeyEntry(alias, privateKey, password, new Certificate[] {certificate});
		} catch (Exception e) {
			e.printStackTrace();
			System.err.println("Error adding certificate to KeyStore");
		}
	}
	
	public static void saveKeyStore(KeyStore keyStore, String filePath, char[] password) {
		try {
			keyStore.store(new FileOutputStream(filePath), password);
		} catch (Exception e) {
			e.printStackTrace();
			System.err.println("Exception saving KeyStore");
		}
	}

}
