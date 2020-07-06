package ib.project.rest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import ib.project.dto.UserDTO;
import ib.project.model.User;
import ib.project.service.AuthorityServiceInterface;
import ib.project.service.UserServiceInterface;

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
		user.setPassword(userDTO.getPassword());
		// Implement creating certificate / jks
		user.setCertificate("myCertificate");
		user.setActive(false);
		user.setAuthority(authorityService.findByName("regular"));
		
		user = userService.save(user);
		
		return new ResponseEntity<UserDTO>(new UserDTO(user), HttpStatus.CREATED);
	}

}
