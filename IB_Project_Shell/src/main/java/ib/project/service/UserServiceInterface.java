package ib.project.service;

import java.util.List;

import ib.project.model.User;

public interface UserServiceInterface {

	User findOne(Long userId);
	
	User findByEmail(String email);
	
	List<User> findByActive(boolean active);
	
	User save(User user);
	
}
