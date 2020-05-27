package app;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyStore;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Security;
import java.security.spec.InvalidKeySpecException;
import java.util.ArrayList;
import java.util.List;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;

import org.apache.xml.security.utils.JavaUtils;
import org.bouncycastle.jce.provider.BouncyCastleProvider;

import com.google.api.services.gmail.Gmail;
import com.google.api.services.gmail.model.Message;

import keystore.KeyStoreReader;
import model.mailclient.MailBody;
import support.MailHelper;
import support.MailReader;
import util.Base64;
import util.GzipUtil;

public class ReadMailClient extends MailClient {

	public static long PAGE_SIZE = 3;
	public static boolean ONLY_FIRST_PAGE = true;
	
//	private static final String KEY_FILE = "./data/session.key";
//	private static final String IV1_FILE = "./data/iv1.bin";
//	private static final String IV2_FILE = "./data/iv2.bin";
	
	public static void main(String[] args) throws IOException, InvalidKeyException, NoSuchAlgorithmException, InvalidKeySpecException, IllegalBlockSizeException, BadPaddingException, MessagingException, NoSuchPaddingException, InvalidAlgorithmParameterException, NoSuchProviderException {
        // Build a new authorized API client service.
        Gmail service = getGmailService();
        ArrayList<MimeMessage> mimeMessages = new ArrayList<MimeMessage>();
        
        String user = "me";
        String query = "is:unread label:INBOX";
        
        List<Message> messages = MailReader.listMessagesMatchingQuery(service, user, query, PAGE_SIZE, ONLY_FIRST_PAGE);
        for(int i=0; i<messages.size(); i++) {
        	Message fullM = MailReader.getMessage(service, user, messages.get(i).getId());
        	
        	MimeMessage mimeMessage;
			try {
				
				mimeMessage = MailReader.getMimeMessage(service, user, fullM.getId());
				
				System.out.println("\n Message number " + i);
				System.out.println("From: " + mimeMessage.getHeader("From", null));
				System.out.println("Subject: " + mimeMessage.getSubject());
				System.out.println("Body: " + MailHelper.getText(mimeMessage));
				System.out.println("\n");
				
				mimeMessages.add(mimeMessage);
	        
			} catch (MessagingException e) {
				e.printStackTrace();
			}	
        }
        
        System.out.println("Select a message to decrypt:");
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
	        
	    String answerStr = reader.readLine();
	    Integer answer = Integer.parseInt(answerStr);
	    
		MimeMessage chosenMessage = mimeMessages.get(answer);
	    
//      kreiranje mailBody-a
		
		String mailBodyCSV = MailHelper.getText(chosenMessage);
		MailBody mailBody = new MailBody(mailBodyCSV);
		byte[] cipherText = mailBody.getEncMessageBytes();
		byte[] cipherSecretKey = mailBody.getEncKeyBytes();
		IvParameterSpec ivParameterSpec1 = new IvParameterSpec(mailBody.getIV1Bytes());
		IvParameterSpec ivParameterSpec2 = new IvParameterSpec(mailBody.getIV2Bytes());
		
//		dobavljanje privatnog kljuca korisnika B
		KeyStore keyStore = KeyStoreReader.readKeyStore("./data/userb.jks", "userb".toCharArray());
		PrivateKey userbPrivateKey = KeyStoreReader.getPrivateKeyFromKeyStore(keyStore, "sima", "userb".toCharArray());
		
//		dekriptovanje tajnog kljuca
		Security.addProvider(new BouncyCastleProvider());
		Cipher rsaCipherDec = Cipher.getInstance("RSA/ECB/PKCS1Padding", "BC");
		rsaCipherDec.init(Cipher.DECRYPT_MODE, userbPrivateKey);
		byte[] secretKeyBytes = rsaCipherDec.doFinal(cipherSecretKey);
		SecretKey secretKey = new SecretKeySpec(secretKeyBytes, "AES");
		
		Cipher aesCipherDec = Cipher.getInstance("AES/CBC/PKCS5Padding");
//		SecretKey secretKey = new SecretKeySpec(JavaUtils.getBytesFromFile(KEY_FILE), "AES");
		
//		byte[] iv2 = JavaUtils.getBytesFromFile(IV2_FILE);
//		IvParameterSpec ivParameterSpec2 = new IvParameterSpec(iv2);
		//inicijalizacija za dekriptovanje
		aesCipherDec.init(Cipher.DECRYPT_MODE, secretKey, ivParameterSpec2);
		
		//dekompresovanje i dekriptovanje subject-a
		byte[] cipherSubject = Base64.decode(chosenMessage.getSubject());
		String compressedSubject = new String(aesCipherDec.doFinal(cipherSubject));
		String subjectText = GzipUtil.decompress(Base64.decode(compressedSubject));
		System.out.println("Subject text: " + subjectText);
		
//		byte[] iv1 = JavaUtils.getBytesFromFile(IV1_FILE);
//		IvParameterSpec ivParameterSpec1 = new IvParameterSpec(iv1);
		aesCipherDec.init(Cipher.DECRYPT_MODE, secretKey, ivParameterSpec1);
		
//		String str = MailHelper.getText(chosenMessage);
//		byte[] bodyEnc = Base64.decode(str);
		
		String compressedBody = new String(aesCipherDec.doFinal(cipherText));
		String bodyText = GzipUtil.decompress(Base64.decode(compressedBody));
		System.out.println("Body text: " + bodyText);
		
		

	}
}
