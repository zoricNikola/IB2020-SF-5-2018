package app;


import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Security;
import java.security.cert.Certificate;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.mail.internet.MimeMessage;

import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.apache.log4j.varia.NullAppender;
import org.apache.xml.security.utils.JavaUtils;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import com.google.api.services.gmail.Gmail;

import keystore.KeyStoreReader;
import model.mailclient.MailBody;
import util.Base64;
import util.GzipUtil;
import util.IVHelper;
import util.SignatureManager;
import support.MailHelper;
import support.MailWritter;

public class WriteMailClient extends MailClient {

//	private static final String KEY_FILE = "./data/session.key";
//	private static final String IV1_FILE = "./data/iv1.bin";
//	private static final String IV2_FILE = "./data/iv2.bin";
	private static char[] password = "password".toCharArray();
	
	public static void main(String[] args) {
		
        try {
        	BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        	System.out.println("Insert your email:");
            String sender = reader.readLine();
            
            org.apache.log4j.BasicConfigurator.configure(new NullAppender());
            
            String url = "http://localhost:8443/api/users/getKeyStorePath/" + sender;
            DefaultHttpClient httpClient = new DefaultHttpClient();
            HttpGet request = new HttpGet(url);
            request.addHeader("accept", "application/json");
            
            HttpResponse response = httpClient.execute(request);
            if (response.getStatusLine().getStatusCode() != 200)
            	throw new RuntimeException("Error getting certificate path from server");
            
            HttpEntity entity = response.getEntity();
            String keyStorePath = EntityUtils.toString(entity);
        	
        	Gmail service = getGmailService();
            
        	System.out.println("Insert a reciever:");
            String reciever = reader.readLine();
        	
            System.out.println("Insert a subject:");
            String subject = reader.readLine();
            
            
            System.out.println("Insert body:");
            String body = reader.readLine();
            
            
            //Compression
            String compressedSubject = Base64.encodeToString(GzipUtil.compress(subject));
            String compressedBody = Base64.encodeToString(GzipUtil.compress(body));
            
            //Key generation
            KeyGenerator keyGen = KeyGenerator.getInstance("DESede"); 
			SecretKey secretKey = keyGen.generateKey();
			Cipher desCipherEnc = Cipher.getInstance("DESede/CBC/PKCS5Padding");
			
			//inicijalizacija za sifrovanje 
			IvParameterSpec ivParameterSpec1 = IVHelper.createIV();
			desCipherEnc.init(Cipher.ENCRYPT_MODE, secretKey, ivParameterSpec1);
			
			KeyStore keyStore = KeyStoreReader.readKeyStore(keyStorePath, password);
			PrivateKey privateKey = KeyStoreReader.getPrivateKeyFromKeyStore(keyStore, "certificate", password);
			
			
			//sifrovanje
			byte[] ciphertext = desCipherEnc.doFinal(compressedBody.getBytes());
//			String ciphertextStr = Base64.encodeToString(ciphertext);
			System.out.println("Kriptovan tekst: " + Base64.encodeToString(ciphertext));
			
			//potpisivanje
			byte[] signature = SignatureManager.sign(ciphertext, privateKey);
			
			
			//inicijalizacija za sifrovanje 
			IvParameterSpec ivParameterSpec2 = IVHelper.createIV();
			desCipherEnc.init(Cipher.ENCRYPT_MODE, secretKey, ivParameterSpec2);
			
			byte[] ciphersubject = desCipherEnc.doFinal(compressedSubject.getBytes());
			String ciphersubjectStr = Base64.encodeToString(ciphersubject);
			System.out.println("Kriptovan subject: " + ciphersubjectStr);
			
			//preuzimanje javnog kljuca iz sertifikata korisnika B
			String recieverCertificateAlias = reciever.split("@")[0];
			PublicKey userbPublicKey = keyStore.getCertificate(recieverCertificateAlias).getPublicKey();
			
			//kriptovanje tajnog kljuca
			Security.addProvider(new BouncyCastleProvider());
			Cipher rsaCipherEnc = Cipher.getInstance("RSA/ECB/PKCS1Padding", "BC");
			rsaCipherEnc.init(Cipher.ENCRYPT_MODE, userbPublicKey);
			byte[] cipherSecretKey = rsaCipherEnc.doFinal(secretKey.getEncoded());
//			String secretKeyEncString = Base64.encodeToString(secretKeyEnc);
			
			//kreiranje mail body-a
			MailBody mailBody = new MailBody(ciphertext, ivParameterSpec1.getIV(), ivParameterSpec2.getIV(), cipherSecretKey, signature);
			
			//snimaju se bajtovi kljuca i IV.
//			JavaUtils.writeBytesToFilename(KEY_FILE, secretKey.getEncoded());
//			JavaUtils.writeBytesToFilename(IV1_FILE, ivParameterSpec1.getIV());
//			JavaUtils.writeBytesToFilename(IV2_FILE, ivParameterSpec2.getIV());
			
        	MimeMessage mimeMessage = MailHelper.createMimeMessage(reciever, ciphersubjectStr, mailBody.toCSV());
        	MailWritter.sendMessage(service, "me", mimeMessage);
        	
        }catch (Exception e) {
        	e.printStackTrace();
		}
	}
}
