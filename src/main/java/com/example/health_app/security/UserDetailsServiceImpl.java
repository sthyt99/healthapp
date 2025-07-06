package com.example.health_app.security;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;

import com.example.health_app.constant.AppMessage;
import com.example.health_app.entity.User;
import com.example.health_app.repository.UserRepository;

import jakarta.persistence.EntityNotFoundException;

/**
 * DBからユーザーを検索するサービスクラス
 */
@Service
public class UserDetailsServiceImpl implements UserDetailsService {

	private final UserRepository userRepository;

	public UserDetailsServiceImpl(UserRepository userRepository) {
		this.userRepository = userRepository;
	}

	@Override
	public UserDetails loadUserByUsername(String username) {

		User user = userRepository.findByUsername(username)
				.orElseThrow(() -> new EntityNotFoundException(AppMessage.USER_NOT_FOUND));

		return new CustomUserDetails(user);
	}
}
