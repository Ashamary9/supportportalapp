package com.portal.service;

import java.io.IOException;
import java.util.List;

import javax.mail.MessagingException;

import org.springframework.web.multipart.MultipartFile;

import com.portal.domain.User;
import com.portal.exception.EmailExistException;
import com.portal.exception.EmailNotFoundException;
import com.portal.exception.UserNotFoundException;
import com.portal.exception.UsernameExistException;

public interface UserService {

	
    User register(String firstName, String lastName, String username, String email) throws UserNotFoundException, UsernameExistException, EmailExistException,MessagingException;

    List<User> getUsers();

    User findUserByUsername(String username);

    User findUserByEmail(String email);
    
    User addUser(String firstName,String lastName,String username,String email,String role,boolean isNotLocked,boolean isActive,MultipartFile profileImage) throws UserNotFoundException, UsernameExistException, EmailExistException, IOException;

    User updateUser(String currentUsername,String newFirstName,String newLastName,String newUsername,String newEmail,String role,boolean isNotLocked,boolean isActive,MultipartFile profileImage) throws UserNotFoundException, UsernameExistException, EmailExistException, IOException;

    void deleteUser(long id);
    
    void resetPassword(String email) throws EmailNotFoundException, MessagingException;
    
    User updateProfileImage(String username,MultipartFile profileImage) throws UserNotFoundException, UsernameExistException, EmailExistException, IOException;
    
}
