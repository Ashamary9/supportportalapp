package com.portal.serviceImpl;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import org.springframework.stereotype.Service;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

@Service
public class LoginAttemptServiceImpl {

	
	private static final int MAXMUM_NUMBER_OF_ATTEMPT=5;
	private static final int ATTEMPT_INCREMENT=1;
	
	
	private LoadingCache<String,Integer>loginAttemptCache;


	public LoginAttemptServiceImpl() {
		super();
		loginAttemptCache=CacheBuilder.newBuilder().expireAfterAccess(15,TimeUnit.MINUTES)
				.maximumSize(100).build(new CacheLoader<String,Integer>(){
					public Integer load(String key) {
						return 0;
					}
				});
	
	}
	
	
	public void evictUserFromLoginAttemptCache(String username) {
		loginAttemptCache.invalidate(username);
	}
	
	
	public void addUserToLoginAttemptCache(String username) {
		int attempts=0;
		try {
			attempts= ATTEMPT_INCREMENT + loginAttemptCache.get(username);
			loginAttemptCache.put(username, attempts);
		}catch(ExecutionException e) {
			e.printStackTrace();
		}
		
	}
	
	
	public boolean hasExeededAttempts(String username)  {
		try {
			return loginAttemptCache.get(username)>= MAXMUM_NUMBER_OF_ATTEMPT;
		} catch (ExecutionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return false;
	}
	

}
