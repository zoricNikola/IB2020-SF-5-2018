package ib.project.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import ib.project.model.Authority;
import ib.project.repository.AuthorityRepository;

@Service
public class AuthorityService implements AuthorityServiceInterface {

	@Autowired
	AuthorityRepository authorityRepository;
	
	@Override
	public Authority findOne(Long authorityId) {
		return authorityRepository.getOne(authorityId);
	}

	@Override
	public Authority findByName(String name) {
		return authorityRepository.findByName(name);
	}

	@Override
	public Authority save(Authority authority) {
		return authorityRepository.save(authority);
	}

}
