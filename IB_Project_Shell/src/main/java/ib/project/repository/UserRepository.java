package ib.project.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import ib.project.model.User;

public interface UserRepository extends JpaRepository<User, Long> {
	
	User findByEmail(String email);
	
	List<User> findByActive(boolean active);
	
	List<User> findByEmailIgnoreCaseContaining(String email);

}
