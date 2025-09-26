package com.example.health_app.controller;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.Collections;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.example.health_app.entity.User;
import com.example.health_app.security.CustomUserDetails;
import com.example.health_app.security.JwtUtil;
import com.example.health_app.service.UserService;

import jakarta.persistence.EntityNotFoundException;

@SpringBootTest
@AutoConfigureMockMvc
class UserControllerTest {

	@Autowired
	MockMvc mockMvc;

	@MockitoBean
	UserService userService;

	@MockitoBean
	JwtUtil jwtUtil;

	@MockitoBean
	com.example.health_app.security.UserDetailsServiceImpl userDetailsService;

	// ---- 全テスト共通のデフォルト挙動 ----
	@org.junit.jupiter.api.BeforeEach
	void muteJwt() {
		given(jwtUtil.validateToken(anyString())).willReturn(false);
	}

	// ------- helpers -------
	private static User userEntity(long id, String username, String email) {
		User u = new User();
		u.setId(id);
		u.setUsername(username);
		u.setEmail(email);
		u.setRoles(Collections.emptySet());
		return u;
	}

	// ------- tests -------

	@Test
	void registerUser_returns200_andDto() throws Exception {
		var saved = userEntity(1L, "taro", "taro@example.com");
		given(userService.registerUser(any(User.class))).willReturn(saved);

		mockMvc.perform(post("/api/users/register")
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
						    {"username":"taro","email":"taro@example.com","password":"Passw0rd!"}
						"""))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.id").value(1))
				.andExpect(jsonPath("$.username").value("taro"))
				.andExpect(jsonPath("$.email").value("taro@example.com"));
	}

	@Test
	@WithMockUser(roles = "ADMIN")
	void getUserByUsername_admin_success() throws Exception {
		given(userService.findByUsernameOrThrow("hanako"))
				.willReturn(userEntity(2L, "hanako", "hanako@example.com"));

		mockMvc.perform(get("/api/users/{username}", "hanako"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.id").value(2))
				.andExpect(jsonPath("$.username").value("hanako"))
				.andExpect(jsonPath("$.email").value("hanako@example.com"));
	}

	@Test
	@WithMockUser(roles = "ADMIN")
	void getUserByUsername_admin_notFound() throws Exception {
		given(userService.findByUsernameOrThrow("missing"))
				.willThrow(new EntityNotFoundException());

		mockMvc.perform(get("/api/users/{username}", "missing"))
				.andExpect(status().isNotFound())
				.andExpect(content().string(com.example.health_app.constant.AppMessage.USER_NOT_FOUND));
	}

	@Test
	void getMyInfo_success_whenAuthenticated() throws Exception {
		var me = userEntity(10L, "me", "me@example.com");
		var cud = new CustomUserDetails(me);

		given(userService.findByUsernameOrThrow("me")).willReturn(me);

		mockMvc.perform(get("/api/users/me").with(user(cud)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.id").value(10))
				.andExpect(jsonPath("$.username").value("me"))
				.andExpect(jsonPath("$.email").value("me@example.com"));
	}

	@Test
	void getMyInfo_unauthorized_whenPrincipalNull() throws Exception {
		mockMvc.perform(get("/api/users/me"))
				.andExpect(status().isUnauthorized());
	}

	@Test
	void changePasswordBySelf_success() throws Exception {
		var me = userEntity(10L, "me", "me@example.com");
		var cud = new CustomUserDetails(me);

		given(userService.findByUsernameOrThrow("me")).willReturn(me);
		doNothing().when(userService).changePassword(10L, "N3wStrongPass!");

		mockMvc.perform(put("/api/users/password")
				.with(user(cud))
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
						    {"newPassword":"N3wStrongPass!"}
						"""))
				.andExpect(status().isOk())
				.andExpect(content().string(com.example.health_app.constant.AppMessage.USER_CHANGE_PASSWORD));

		verify(userService).changePassword(10L, "N3wStrongPass!");
	}

	@Test
	void changePasswordBySelf_tooShort_returnsBadRequest() throws Exception {
		var me = userEntity(10L, "me", "me@example.com");
		var cud = new CustomUserDetails(me);

		mockMvc.perform(put("/api/users/password")
				.with(user(cud))
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
						    {"newPassword":"123"}
						"""))
				.andExpect(status().isBadRequest());
	}

	@Test
	@WithMockUser(roles = "ADMIN")
	void changePasswordByAdmin_success() throws Exception {
		doNothing().when(userService).changePassword(99L, "AdminSetPass!");

		mockMvc.perform(put("/api/users/{id}/password", 99L)
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
						    {"newPassword":"AdminSetPass!"}
						"""))
				.andExpect(status().isOk())
				.andExpect(content().string(com.example.health_app.constant.AppMessage.USER_CHANGE_PASSWORD));

		verify(userService).changePassword(99L, "AdminSetPass!");
	}
}
