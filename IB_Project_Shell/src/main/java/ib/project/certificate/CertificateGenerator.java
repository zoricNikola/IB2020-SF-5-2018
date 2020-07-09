package ib.project.certificate;

import java.math.BigInteger;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.Security;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.asn1.x500.X500NameBuilder;
import org.bouncycastle.asn1.x500.style.BCStyle;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.cert.X509v3CertificateBuilder;
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
import org.bouncycastle.cert.jcajce.JcaX509v3CertificateBuilder;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.operator.ContentSigner;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;

import ib.project.keystore.KeyStoreReader;
import ib.project.keystore.KeyStoreWriter;
import ib.project.model.IssuerData;
import ib.project.model.SubjectData;
import ib.project.model.User;

public class CertificateGenerator {
	
	public static char[] password = "password".toCharArray();
	public static String rootCAPath = "./data/root.jks";
	public static String rootCAAlias = "Root CA Certificate";
	public static String certificateAlias = "certificate";
	
	static {
		Security.addProvider(new BouncyCastleProvider());
	}
	
	public static KeyPair generateKeyPair() {
		
		try {
			KeyPairGenerator keyGenerator = KeyPairGenerator.getInstance("RSA");
			keyGenerator.initialize(1024);
			KeyPair pair = keyGenerator.generateKeyPair();
			return pair;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public static void generateCertificate(User user) {
		
		KeyPair keyPair = generateKeyPair();
		X500Name x500NameSubject = generateX500Name(user.getEmail().split("@")[0], "RS", user.getEmail(), user.getId().toString());
		
		SimpleDateFormat iso8601Formatter = new SimpleDateFormat("yyyy-MM-dd");
		Date startDate = new Date();
		Date endDate = new Date();
		try {
			startDate = iso8601Formatter.parse("2020-07-10");
			endDate = iso8601Formatter.parse("2021-07-10");
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		String serialNumber = user.getId().toString();
		
		SubjectData subjectData = new SubjectData(keyPair.getPublic(), x500NameSubject, serialNumber, startDate, endDate);
		
		// getting issuer info
		KeyStore issuerKeyStore = KeyStoreReader.readKeyStore(rootCAPath, password);
		Certificate issuerCertificate = KeyStoreReader.getCertificateFromKeyStore(issuerKeyStore, rootCAAlias);
		PrivateKey issuerPrivateKey = KeyStoreReader.getPrivateKeyFromKeyStore(issuerKeyStore, rootCAAlias, password);
		IssuerData issuerData = KeyStoreReader.getIssuerFromCertificate(issuerCertificate, issuerPrivateKey);
		
		// creating signed certificate
		X509Certificate certificate = generateCertificate(issuerData, subjectData);
		String certPath = "./data/" + user.getId() + ".cer";
		CertificateWriter.saveCertificateBase64Encoded(certificate, certPath);
		System.out.println("Certificate saved as .cer file");
		
		KeyStore keyStore = KeyStoreReader.createKeyStore();
		KeyStoreWriter.addToKeyStore(keyStore, certificateAlias, keyPair.getPrivate(), password, certificate);
		String keyStorePath = "./data/" + user.getId() + ".jks";
		KeyStoreWriter.saveKeyStore(keyStore, keyStorePath, password);
		System.out.println("KeyStore saved as .jks file");
	}
	
	public static X500Name generateX500Name(String commonName, String country, String email, String uID) {
		X500NameBuilder builder = new X500NameBuilder(BCStyle.INSTANCE);
		
		builder.addRDN(BCStyle.CN, commonName);
		builder.addRDN(BCStyle.C, country);
		builder.addRDN(BCStyle.E, email);
		builder.addRDN(BCStyle.UID, uID);
		
		return builder.build();
	}
	
	public static X509Certificate generateCertificate(IssuerData issuerData, SubjectData subjectData) {
		try {
			JcaContentSignerBuilder signerBuilder = new JcaContentSignerBuilder("SHA256WithRSAEncryption");
			signerBuilder.setProvider("BC");
			ContentSigner contentSigner = signerBuilder.build(issuerData.getPrivateKey());
			
			X509v3CertificateBuilder builder = new JcaX509v3CertificateBuilder(
					issuerData.getX500name(), new BigInteger(subjectData.getSerialNumber()), 
					subjectData.getStartDate(), subjectData.getEndDate(),
					subjectData.getX500name(), subjectData.getPublicKey());
			
			X509CertificateHolder holder = builder.build(contentSigner);
			
			JcaX509CertificateConverter converter = new JcaX509CertificateConverter();
			converter = converter.setProvider("BC");
			
			return converter.getCertificate(holder);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return null;
	}
	
	public static void generateMyRootCA() {
		//Generating certificate
		SimpleDateFormat iso8601Formatter = new SimpleDateFormat("yyyy-MM-dd");
		Date startDate = new Date();
		Date endDate = new Date();
		try {
			startDate = iso8601Formatter.parse("2020-07-10");
			endDate = iso8601Formatter.parse("2021-07-10");
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		X500NameBuilder nameBuilder = new X500NameBuilder(BCStyle.INSTANCE);
		nameBuilder.addRDN(BCStyle.CN, "Nikola Zoric");
		nameBuilder.addRDN(BCStyle.SURNAME, "Zoric");
		nameBuilder.addRDN(BCStyle.GIVENNAME, "Nikola");
		nameBuilder.addRDN(BCStyle.O, "CSS");
		nameBuilder.addRDN(BCStyle.OU, "NS");
		nameBuilder.addRDN(BCStyle.C, "RS");
		nameBuilder.addRDN(BCStyle.E, "nikola.se.zoric@gmail.com");
		nameBuilder.addRDN(BCStyle.UID, "1");
		
		String serialNumber = "1";
		
		X500Name x500Name = nameBuilder.build();
		
		KeyPair keyPair = generateKeyPair();
		
		IssuerData issuerData = new IssuerData(keyPair.getPrivate(), x500Name);
		
		SubjectData subjectData = new SubjectData(keyPair.getPublic(), x500Name, serialNumber, startDate, endDate);
		
		X509Certificate certificate = generateCertificate(issuerData, subjectData);
		System.out.println("Root CA created");
		//Generating KeyStore
		KeyStore keyStore = null;
		
		try {
			keyStore = KeyStore.getInstance("JKS");
			keyStore.load(null, "password".toCharArray());
			
			KeyStoreWriter.addToKeyStore(keyStore, rootCAAlias, keyPair.getPrivate(), password, certificate);
			System.out.println("Root CA added to KeyStore");
			KeyStoreWriter.saveKeyStore(keyStore, rootCAPath, password);
			System.out.println("Root CA KeyStore saved");
		} catch (Exception e) {
			e.printStackTrace();
			System.err.println("Exception creating new keyStore");
			return;
		}
		
	}

}
