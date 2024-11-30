package com.portal.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.portal.domain.User;


@Repository
public interface UserRepository extends JpaRepository<User,Long>{

	User findUserByUsername(String username);

    User findUserByEmail(String email);
	
}
