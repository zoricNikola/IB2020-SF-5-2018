package ib.project.certificate;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.security.cert.Certificate;

import ib.project.util.Base64;

public class CertificateWriter {
	
	public static void saveCertificateBase64Encoded(Certificate certificate, String path) {
		
		try {
			BufferedWriter writer = new BufferedWriter(new FileWriter(path));
			
			writer.write("-----BEGIN CERTIFICATE-----");
			writer.newLine();
			writer.write(Base64.encodeToString(certificate.getEncoded()));
			writer.newLine();
			writer.write("-----END CERTIFICATE-----");
			writer.newLine();
			writer.close();
		} catch (Exception e) {
			e.printStackTrace();
			System.err.println("Exception saving certificate");
		}
		
	}

}
