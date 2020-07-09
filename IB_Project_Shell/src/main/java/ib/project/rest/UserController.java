package ib.project.rest;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
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

}
