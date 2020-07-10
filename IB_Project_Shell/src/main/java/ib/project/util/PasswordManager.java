package ib.project.util;

import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.util.Arrays;

import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

public class PasswordManager {
	
	public static byte[] generateSalt() {
		try {
			SecureRandom random = SecureRandom.getInstance("SHA1PRNG");
			byte[] salt = new byte[8];
			random.nextBytes(salt);
			return salt;
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public static byte[] decodeString(String data) {
		byte[] decoded = Base64.decode(data);
		return decoded;
	}
	
	public static byte[] hashPassword(String password, byte[] salt) {
		String algorithm = "PBKDF2WithHmacSHA1";
		int keyLength = 160;
		int iterations = 5000;
		KeySpec spec = new PBEKeySpec(password.toCharArray(), salt, iterations, keyLength);
		try {
			SecretKeyFactory skf = SecretKeyFactory.getInstance(algorithm);
			SecretKey sk = skf.generateSecret(spec);
			return sk.getEncoded();
		} catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public static boolean authenticate(String password, byte[] realPassword, byte[] salt) {
		byte[] hashedPassword = hashPassword(password, salt);
		return Arrays.equals(realPassword, hashedPassword);
	}

}
