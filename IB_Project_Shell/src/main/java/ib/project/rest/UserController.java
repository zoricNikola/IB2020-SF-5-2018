package ib.project.rest;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.security.KeyStore;
import java.security.cert.Certificate;
import java.security.cert.CertificateEncodingException;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import ib.project.certificate.CertificateGenerator;
import ib.project.certificate.CertificateReader;
import ib.project.dto.UserDTO;
import ib.project.keystore.KeyStoreReader;
import ib.project.model.User;
import ib.project.service.AuthorityServiceInterface;
import ib.project.service.UserServiceInterface;
import ib.project.util.Base64;
import ib.project.util.PasswordManager;

@RestController
@RequestMapping(value = "api/users")
public class UserController {
	
	@Autowired
	UserServiceInterface userService;
	
	@Autowired
	AuthorityServiceInterface authorityService;
	
	@PostMapping(consumes = "application/json")
	public ResponseEntity<UserDTO> saveUser(@RequestBody UserDTO userDTO) {
		
		if (userService.findByEmail(userDTO.getEmail()) != null)
			return new ResponseEntity<UserDTO>(HttpStatus.CONFLICT);
		
		User user = new User();
		user.setEmail(userDTO.getEmail());
		
		String password = userDTO.getPassword();
		byte[] salt = PasswordManager.generateSalt();
		user.setSalt(Base64.encodeToString(salt));
		
		byte[] hashedPassword = PasswordManager.hashPassword(password, salt);
		user.setPassword(Base64.encodeToString(hashedPassword));
		user.setCertificate("");
		user.setActive(false);
		user.setAuthority(authorityService.findByName("regular"));
		
		user = userService.save(user);
		
		CertificateGenerator.generateCertificate(user);
		
		user.setCertificate("./data/" + user.getId() + ".cer");
		user = userService.save(user);
		
		return new ResponseEntity<UserDTO>(new UserDTO(user), HttpStatus.CREATED);
	}
	
	@GetMapping(path = "/searchByEmail/{email}")
	public ResponseEntity<List<UserDTO>> searchUsersByEmail(@PathVariable("email") String email) {
		if (email == null)
			email = "";
		List<User> users = userService.searchByEmail(email);
		
		List<UserDTO> usersDTO = new ArrayList<UserDTO>();
		
		for (User user : users) {
			usersDTO.add(new UserDTO(user));
		}
		
		return new ResponseEntity<List<UserDTO>>(usersDTO, HttpStatus.OK);
	}
	
	@GetMapping(path = "/inactive")
	public ResponseEntity<List<UserDTO>> getInactiveUsers() {
		List<User> inactiveUsers = userService.findByActive(false);
		
		List<UserDTO> users = new ArrayList<UserDTO>();
		
		for (User user : inactiveUsers) {
			users.add(new UserDTO(user));
		}
		
		return new ResponseEntity<List<UserDTO>>(users, HttpStatus.OK);
		
	}
	
	@PutMapping(value = "/activate/{id}")
	public ResponseEntity<UserDTO> activateUser(@PathVariable("id") Long id) {
		User user = userService.findOne(id);
		if (user == null)
			return new ResponseEntity<UserDTO>(HttpStatus.BAD_REQUEST);
		
		user.setActive(true);
		
		user = userService.save(user);
		return new ResponseEntity<UserDTO>(new UserDTO(user), HttpStatus.OK);
		
	}
	
	@GetMapping(path = "/newCertificate/{id}")
	public ResponseEntity<Void> createNewCertificate(@PathVariable("id") Long id) {
		User user = userService.findOne(id);
		if (user == null)
			return new ResponseEntity<Void>(HttpStatus.BAD_REQUEST);
		
		CertificateGenerator.generateCertificate(user);
		
		return new ResponseEntity<Void>(HttpStatus.OK);
	}
	
	@GetMapping(path = "/downloadCertificate/{id}")
	public ResponseEntity<byte[]> downloadCertificate(@PathVariable("id") Long id) {

		Certificate certificate = CertificateReader.readBase64EncodedCertificate
				("./data/" + id + ".cer");
		
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		headers.add("filename", id + ".cer");

		byte[] bFile = new byte[0];
		try {
			bFile = certificate.getEncoded();
			return ResponseEntity.ok().headers(headers).body(bFile);
		} catch (CertificateEncodingException e) {
			e.printStackTrace();
		}

		return new ResponseEntity<byte[]>(HttpStatus.BAD_REQUEST);
	}
	
	@GetMapping(path = "/downloadKeyStore/{email}")
	public ResponseEntity<byte[]> downloadKeyStore(@PathVariable("email") String email) {
		User user = userService.findByEmail(email);
		if (user == null)
			return new ResponseEntity<byte[]>(HttpStatus.BAD_REQUEST);
			
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		headers.add("filename", user.getId() + ".jks");
		
		byte[] bFile = DemoController.readBytesFromFile("./data/" + user.getId() + ".jks");

		return ResponseEntity.ok().headers(headers).body(bFile);
	}

}
