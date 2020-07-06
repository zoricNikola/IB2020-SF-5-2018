package ib.project.dto;

import java.io.Serializable;

import ib.project.model.User;

public class UserDTO implements Serializable {

	private static final long serialVersionUID = 6168322922068752388L;

	private Long id;
	
	private String email;
	
	private String password;
	
	private String certificate;
	
	private boolean active;
	
	private AuthorityDTO authority;
	
	public UserDTO() {}

	public UserDTO(Long id, String email, String password, String certificate, boolean active, AuthorityDTO authority) {
		super();
		this.id = id;
		this.email = email;
		this.password = password;
		this.certificate = certificate;
		this.active = active;
		this.authority = authority;
	}
	
	public UserDTO(User user) {
		this(user.getId(), user.getEmail(), user.getPassword(), user.getCertificate(), user.isActive(), new AuthorityDTO(user.getAuthority()));
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getCertificate() {
		return certificate;
	}

	public void setCertificate(String certificate) {
		this.certificate = certificate;
	}

	public boolean isActive() {
		return active;
	}

	public void setActive(boolean active) {
		this.active = active;
	}

	public AuthorityDTO getAuthority() {
		return authority;
	}

	public void setAuthority(AuthorityDTO authority) {
		this.authority = authority;
	}
	
}
