package ib.project.certificate;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;

public class CertificateReader {
	
	public static Certificate readBase64EncodedCertificate(String path) {
		
		try {
			BufferedInputStream stream = new BufferedInputStream(new FileInputStream(path));
			CertificateFactory certFactory = CertificateFactory.getInstance("X.509");
			
			if (stream.available() > 0) {
				Certificate certificate = certFactory.generateCertificate(stream);
				return certificate;
			}
		} catch (Exception e) {
			e.printStackTrace();
			System.err.println("Exception reading certificate from file");
		}
		return null;
	}

}
