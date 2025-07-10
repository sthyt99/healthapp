package com.example.health_app.dto;

import java.util.Set;

/**
 * ユーザーDTOクラス
 * パスワードや内部IDをレスポンスから除く。
 * 表示項目をDTOに集中管理。
 */
public class UserDto {

	private Long id;
	private String username;
	private String email;
	private int age;
	private String gender;
	private Set<String> roles;

	public UserDto() {
	}

	public UserDto(Long id, String username, String email, int age, String gender, Set<String> roles) {
		this.id = id;
		this.username = username;
		this.email = email;
		this.age = age;
		this.gender = gender;
		this.roles = roles;
	}

	// Getter & Setter
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public int getAge() {
		return age;
	}

	public void setAge(int age) {
		this.age = age;
	}

	public String getGender() {
		return gender;
	}

	public void setGender(String gender) {
		this.gender = gender;
	}

	public Set<String> getRoles() {
		return roles;
	}

	public void setRoles(Set<String> roles) {
		this.roles = roles;
	}
}
