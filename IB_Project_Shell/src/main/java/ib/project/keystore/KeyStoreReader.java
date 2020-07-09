package ib.project.keystore;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;

import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.cert.jcajce.JcaX509CertificateHolder;

import ib.project.model.IssuerData;
import ib.project.model.User;

// KEY STORE PASSWORD FOR ALL: password

public class KeyStoreReader {
	
	private static char[] password = "password".toCharArray();
	
	public static KeyStore readKeyStore(String filePath, char[] password) {
		KeyStore keyStore = null;
		try {
			keyStore = KeyStore.getInstance("JKS");
			BufferedInputStream stream = new BufferedInputStream(new FileInputStream(filePath));
			keyStore.load(stream, password);
		} catch (Exception e) {
			e.printStackTrace();
			System.err.println("Exception reading KeyStore from file");
		}
		return keyStore;
	}
	
	public static Certificate getCertificateFromKeyStore(KeyStore keyStore, String alias) {
		Certificate certificate = null;
		try {
			certificate = keyStore.getCertificate(alias);
		} catch (Exception e) {
			e.printStackTrace();
			System.err.println("Exception reading certificate from KeyStore");
		}
		
		if (certificate == null)
			System.err.println("Certificate for wanted alias not found in KeyStore");
		
		return certificate;
	}
	
	public static PrivateKey getPrivateKeyFromKeyStore(KeyStore keyStore, String alias, char[] password) {
		PrivateKey privateKey = null;
		try {
			privateKey = (PrivateKey) keyStore.getKey(alias, password);
		} catch (Exception e) {
			e.printStackTrace();
			System.err.println("Exception reading private key from KeyStore");
		}
		
		if (privateKey == null)
			System.err.println("Private key for wanted alias not found in the KeyStore");
		
		return privateKey;
	}
	
	public static IssuerData getIssuerFromCertificate(Certificate certificate, PrivateKey privateKey) {
		try {
			X509Certificate x509Certificate = (X509Certificate) certificate;
			JcaX509CertificateHolder holder = new JcaX509CertificateHolder(x509Certificate);
			X500Name issuerName = holder.getIssuer();
			return new IssuerData(privateKey, issuerName);
		} catch (Exception e) {
			e.printStackTrace();
			System.err.println("Exception getting issuer from certificate");
		}
		
		return null;
	}
	
	public static KeyStore createKeyStore() {
		KeyStore keyStore = null;
		
		try {
			keyStore = KeyStore.getInstance("JKS");
			keyStore.load(null, password);
		} catch (Exception e) {
			e.printStackTrace();
			System.err.println("Exception creating new keyStore");
		}
		
		return keyStore;
	}
		
}
