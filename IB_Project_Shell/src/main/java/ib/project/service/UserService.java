package ib.project.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import ib.project.model.User;
import ib.project.repository.UserRepository;

@Service
public class UserService implements UserServiceInterface {

	@Autowired
	UserRepository userRepository;
	
	@Override
	public User findOne(Long userId) {
		return userRepository.getOne(userId);
	}

	@Override
	public User findByEmail(String email) {
		return userRepository.findByEmail(email);
	}

	@Override
	public User save(User user) {
		return userRepository.save(user);
	}

	@Override
	public List<User> findByActive(boolean active) {
		return userRepository.findByActive(active);
	}

}
