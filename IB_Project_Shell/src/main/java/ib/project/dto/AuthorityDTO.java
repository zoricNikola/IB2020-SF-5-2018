package ib.project.dto;

import java.io.Serializable;

import ib.project.model.Authority;

public class AuthorityDTO implements Serializable {

	private static final long serialVersionUID = 5613950570487366371L;
	
	private Long id;
	
	private String name;
	
	public AuthorityDTO() {}

	public AuthorityDTO(Long id, String name) {
		super();
		this.id = id;
		this.name = name;
	}
	
	public AuthorityDTO(Authority authority) {
		this(authority.getId(), authority.getName());
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
	
	

}
