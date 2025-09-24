package com.example.health_app.dto;

import java.util.Set;

import com.example.health_app.entity.Role;

public class AuthResponseDto {

	private String token;
	private String username;
	private Set<Role> roles;
	
	public AuthResponseDto(String token, String username, Set<Role> roles) {
		this.token = token;
		this.username = username;
		this.roles = roles;
	}

	public String getToken() {
		return token;
	}

	public void setToken(String token) {
		this.token = token;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public Set<Role> getRoles() {
		return roles;
	}

	public void setRoles(Set<Role> roles) {
		this.roles = roles;
	}
}
