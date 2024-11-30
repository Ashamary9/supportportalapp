package com.portal.listener;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.security.authentication.event.AuthenticationSuccessEvent;
import org.springframework.stereotype.Component;

import com.portal.domain.User;
import com.portal.domain.UserPrincipal;
import com.portal.serviceImpl.LoginAttemptServiceImpl;

@Component
public class AuthenticationSuccessListener {

	

	@Autowired
	private LoginAttemptServiceImpl loginAttemptService;
	
@EventListener	
public void onAuthenticationSuccess(AuthenticationSuccessEvent event) {
		
		Object principal=event.getAuthentication().getPrincipal();
		if(principal instanceof UserPrincipal) {
			UserPrincipal user=(UserPrincipal)event.getAuthentication().getPrincipal();
			loginAttemptService.evictUserFromLoginAttemptCache(user.getUsername());
		}
		
	}
	
}
