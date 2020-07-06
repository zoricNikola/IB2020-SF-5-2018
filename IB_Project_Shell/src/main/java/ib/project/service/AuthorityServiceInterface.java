package ib.project.service;

import ib.project.model.Authority;

public interface AuthorityServiceInterface {
	
	Authority findOne(Long authorityId);
	
	Authority findByName(String name);
	
	Authority save(Authority authority);

}
