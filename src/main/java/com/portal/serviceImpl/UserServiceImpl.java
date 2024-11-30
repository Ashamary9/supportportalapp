package com.portal.serviceImpl;

import java.io.IOException;
import java.nio.file.CopyOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Date;
import java.util.List;

import javax.mail.MessagingException;

import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.support.BeanDefinitionDsl.Role;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import com.portal.constant.FileConstant;
import com.portal.constant.UserImplConstant;
import com.portal.domain.User;
import com.portal.domain.UserPrincipal;
import com.portal.enumeration.Roles;
import com.portal.exception.EmailExistException;
import com.portal.exception.EmailNotFoundException;
import com.portal.exception.UserNotFoundException;
import com.portal.exception.UsernameExistException;
import com.portal.repository.UserRepository;
import com.portal.service.UserService;

import jakarta.transaction.Transactional;




@Service
@Transactional
@Qualifier("userDetailsService")
public class UserServiceImpl implements UserService,UserDetailsService{

	
	
	    private static final String EMPTY = null;
		private static final CopyOption REPLACE_EXISTING = null;
		private Logger LOGGER = LoggerFactory.getLogger(getClass());
	    private UserRepository userRepository;
	    private BCryptPasswordEncoder passwordEncoder;
	    
	    @Autowired
	    private LoginAttemptServiceImpl loginAttemptService;
	    
	    @Autowired
	    private EmailService emailService;
		private String newLastName;

	    @Autowired
	    public UserServiceImpl(UserRepository userRepository, BCryptPasswordEncoder passwordEncoder) {
	        this.userRepository = userRepository;
	        this.passwordEncoder = passwordEncoder;
	    }
	    

	    @Override
	    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
	        User user = userRepository.findUserByUsername(username);
	        if (user == null) {
	            LOGGER.error(UserImplConstant.NO_USER_FOUND_BY_USERNAME + username);
	            throw new UsernameNotFoundException(UserImplConstant.NO_USER_FOUND_BY_USERNAME + username);
	        } else {
	        	
	        	validateLoginAttempt(user);
	        	
	            user.setLastLoginDateDisplay(user.getLastLoginDate());
	            user.setLastLoginDate(new Date());
	            userRepository.save(user);
	            UserPrincipal userPrincipal = new UserPrincipal(user);
	            LOGGER.info(UserImplConstant.FOUND_USER_BY_USERNAME + username);
	            return userPrincipal;
	        }
	    }

	    private void validateLoginAttempt(User user) {

			if(user.isNotLocked()) {
				if(loginAttemptService.hasExeededAttempts(user.getUsername())) {
					user.setNotLocked(false);
				}
				else {
					user.setNotLocked(true);
				}
			}else {
				loginAttemptService.evictUserFromLoginAttemptCache(user.getUsername());
			}
		}

	    
		@Override
	    public User register(String firstName, String lastName, String username, String email) throws UserNotFoundException, UsernameExistException, EmailExistException, MessagingException {
	        validateNewUsernameAndEmail(StringUtils.EMPTY, username, email);
	        User user = new User();
	        user.setUserId(generateUserId());
	        String password = generatePassword();
	        String encodedPassword = encodePassword(password);
	        user.setFirstName(firstName);
	        user.setLastName(lastName);
	        user.setUsername(username);
	        user.setEmail(email);
	        user.setJoinDate(new Date());
	        user.setPassword(encodedPassword);
	        user.setActive(true);
	        user.setNotLocked(true);
	        user.setRole(Roles.ROLE_USER.name());
	        user.setAuthorities(Roles.ROLE_USER.getAuthorities());
	        user.setProfileImageUrl(getTemporaryProfileImageUrl(username));
	        userRepository.save(user);
	        LOGGER.info("New user password: " + password);
	        emailService.sendNewPasswordEmail(firstName, password, email);
	        return user;
	    }
		
		
		
		@Override
		public User addUser(String firstName, String lastName, String username, String email, String role,
				boolean isNotLocked, boolean isActive, MultipartFile profileImage) throws UserNotFoundException, UsernameExistException, EmailExistException, IOException {
			validateNewUsernameAndEmail(StringUtils.EMPTY,username,email);
			 User user = new User();
			 String password=generatePassword();
		        user.setUserId(generateUserId());
		        user.setFirstName(firstName);
		        user.setLastName(lastName);
		        user.setUsername(username);
		        user.setEmail(email);
		        user.setPassword(encodePassword(password));
		        user.setJoinDate(new Date());
		        user.setActive(isActive);
		        user.setNotLocked(isNotLocked);
		        user.setRole(getRoleEnumName(role).name());
		        user.setAuthorities(getRoleEnumName(role).getAuthorities());
		        user.setProfileImageUrl(getTemporaryProfileImageUrl(username));
		        userRepository.save(user);
		        saveProfileImage(user,profileImage);
		         
			return user;
		}

		
		private void saveProfileImage(User user, MultipartFile profileImage) throws IOException {
     
			if(profileImage!=null) {
				Path userFolder=Paths.get(FileConstant.USER_FOLDER + user.getUsername()).toAbsolutePath().normalize();
				if(!Files.exists(userFolder)) {
					Files.createDirectories(userFolder);
					LOGGER.info(FileConstant.DIRECTORY_CREATED+userFolder);
					
				}
				Files.deleteIfExists(Paths.get(userFolder+user.getUsername(),FileConstant.DOT+FileConstant.JPG_EXTENTION));
				Files.copy(profileImage.getInputStream(),userFolder.resolve(user.getUsername()+FileConstant.DOT+FileConstant.JPG_EXTENTION),REPLACE_EXISTING);
				user.setProfileImageUrl(setProfileImageUrl(user.getUsername()));
				userRepository.save(user);
				LOGGER.info(FileConstant.FILE_SAVED_IN_FILE_SYSTEM+profileImage.getOriginalFilename());
				
			}
			
		}
		
		

		private String setProfileImageUrl(String username) {

	        return ServletUriComponentsBuilder.fromCurrentContextPath().path(FileConstant.USER_IMAGE_PATH+ username +FileConstant.FORWARD_SLASH+ username +FileConstant.DOT+FileConstant.JPG_EXTENTION).toUriString();
	        
	     }
		
		

		private Roles getRoleEnumName(String role) {

			return Roles.valueOf(role.toUpperCase());
		}
		
		

		@Override
		public User updateUser(String currentUsername, String newFirstName, String newLastName, String newUsername,
				String newEmail, String role, boolean isNotLocked, boolean isActive, MultipartFile profileImage) throws UserNotFoundException, UsernameExistException, EmailExistException, IOException {

		User currentUser=validateNewUsernameAndEmail(currentUsername,newUsername,newEmail);

			 User user = new User();
		        currentUser.setFirstName(newFirstName);
		        currentUser.setLastName(newLastName);
		        currentUser.setUsername(currentUsername);
		        currentUser.setEmail(newEmail);
		        currentUser.setActive(isActive);
		        currentUser.setNotLocked(isNotLocked);
		        currentUser.setRole(getRoleEnumName(role).name());
		        user.setAuthorities(getRoleEnumName(role).getAuthorities());

		        userRepository.save(currentUser);
		        saveProfileImage(currentUser,profileImage);
		         
			return currentUser;
			
			
		}
		

		@Override
		public void deleteUser(long id) {

			 userRepository.deleteById(id);;
		}
		

		@Override
		public void resetPassword(String email) throws EmailNotFoundException, MessagingException {

			User user=userRepository.findUserByEmail(email);
			if(user==null) {
				throw new EmailNotFoundException(UserImplConstant.NO_USER_FOUND_BY_EMAIL+email); 
			}
			String password=generatePassword();
	        user.setPassword(encodePassword(password));
	        userRepository.save(user);
	        emailService.sendNewPasswordEmail(user.getFirstName(), password, user.getEmail());
	       
		}

		@Override
		public User updateProfileImage(String username, MultipartFile profileImage) throws UserNotFoundException, UsernameExistException, EmailExistException, IOException {
			
			User user=validateNewUsernameAndEmail(username,null,null);
			saveProfileImage(user,profileImage);
			
			
			return user;
		}

	    @Override
	    public List<User> getUsers() {
	        return userRepository.findAll();
	    }

	    @Override
	    public User findUserByUsername(String username) {
	        return userRepository.findUserByUsername(username);
	    }

	    @Override
	    public User findUserByEmail(String email) {
	        return userRepository.findUserByEmail(email);
	    }

	    private String getTemporaryProfileImageUrl(String  username) {
	        return ServletUriComponentsBuilder.fromCurrentContextPath().path(FileConstant.DEFAULT_USER_IMAGE_PATH + username).toUriString();
	    }

	    private String encodePassword(String password) {
	        return passwordEncoder.encode(password);
	    }

	    private String generatePassword() {
	        return RandomStringUtils.randomAlphanumeric(10);
	    }

	    private String generateUserId() {
	        return RandomStringUtils.randomNumeric(10);
	    }

	    private User validateNewUsernameAndEmail(String currentUsername, String newUsername, String newEmail) throws UserNotFoundException, UsernameExistException, EmailExistException {
	        User userByNewUsername = findUserByUsername(newUsername);
	        User userByNewEmail = findUserByEmail(newEmail);
	        if(StringUtils.isNotBlank(currentUsername)) {
	            User currentUser = findUserByUsername(currentUsername);
	            if(currentUser == null) {
	                throw new UserNotFoundException(UserImplConstant.NO_USER_FOUND_BY_USERNAME + currentUsername);
	            }
	            if(userByNewUsername != null && !currentUser.getId().equals(userByNewUsername.getId())) {
	                throw new UsernameExistException(UserImplConstant.USERNAME_ALREADY_EXISTS);
	            }
	            if(userByNewEmail != null && !currentUser.getId().equals(userByNewEmail.getId())) {
	                throw new EmailExistException(UserImplConstant.EMAIL_ALREADY_EXISTS);
	            }
	            return currentUser;
	        } else {
	            if(userByNewUsername != null) {
	                throw new UsernameExistException(UserImplConstant.USERNAME_ALREADY_EXISTS);
	            }
	            if(userByNewEmail != null) {
	                throw new EmailExistException(UserImplConstant.EMAIL_ALREADY_EXISTS);
	            }
	            return null;
	        }
	    }

		
		

	
	
}
