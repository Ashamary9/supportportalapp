package com.portal.controller;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

import javax.mail.MessagingException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.portal.constant.FileConstant;
import com.portal.constant.SecurityConstant;
import com.portal.domain.HttpResponse;
import com.portal.domain.User;
import com.portal.domain.UserPrincipal;
import com.portal.exception.EmailExistException;
import com.portal.exception.EmailNotFoundException;
import com.portal.exception.ExceptionHandling;
import com.portal.exception.UserNotFoundException;
import com.portal.exception.UsernameExistException;
import com.portal.service.UserService;
import com.portal.utility.JwtTokenProvider;



@RestController
@RequestMapping(path = { "/", "/user"})
public class UserController extends ExceptionHandling{

	
	    private AuthenticationManager authenticationManager;
	    private UserService userService;
	    private JwtTokenProvider jwtTokenProvider;

	    @Autowired
	    public UserController(AuthenticationManager authenticationManager, UserService userService, JwtTokenProvider jwtTokenProvider) {
	        this.authenticationManager = authenticationManager;
	        this.userService = userService;
	        this.jwtTokenProvider = jwtTokenProvider;
	    }

	    @PostMapping("/login")
	    public ResponseEntity<User> login(@RequestBody User user) {
	        authenticate(user.getUsername(), user.getPassword());
	        User loginUser = userService.findUserByUsername(user.getUsername());
	        UserPrincipal userPrincipal = new UserPrincipal(loginUser);
	        HttpHeaders jwtHeader = getJwtHeader(userPrincipal);
	        return new ResponseEntity<>(loginUser, jwtHeader, HttpStatus.OK);
	    }

	    @PostMapping("/register")
	    public ResponseEntity<User> register(@RequestBody User user) throws UserNotFoundException, UsernameExistException, EmailExistException, MessagingException {
	        User newUser = userService.register(user.getFirstName(), user.getLastName(), user.getUsername(), user.getEmail());
	        return new ResponseEntity<>(newUser, HttpStatus.OK);
	    }
	    
	    
	    @PostMapping("/add")
	    public ResponseEntity<User>addUser(@RequestParam("firstName")String firstName,
	    		@RequestParam("lastName")String lastName,
	    		@RequestParam("userName")String username,
	    		@RequestParam("email")String email,
	    		@RequestParam("role")String role,
	    		@RequestParam("isActive")String isActive,
	    		@RequestParam("isNotLocked")String isNotLocked,
	    		@RequestParam(value="profileImage",required=false)MultipartFile profileImage) throws UserNotFoundException, UsernameExistException, EmailExistException, IOException{
	    	
	    	User newUser=userService.addUser(firstName, lastName, username, email,
	    			role, Boolean.parseBoolean(isNotLocked),Boolean.parseBoolean(isActive), profileImage);
	    	
	    	return new ResponseEntity<> (newUser, HttpStatus.OK);
	    	
	    }
	    
	    
	    @PostMapping("/update")
	    public ResponseEntity<User>updateUser(@RequestParam("currentUsername")String currentUsername,
	    		@RequestParam("firstName")String firstName,
	    		@RequestParam("lastName")String lastName,
	    		@RequestParam("userName")String username,
	    		@RequestParam("email")String email,
	    		@RequestParam("role")String role,
	    		@RequestParam("isActive")String isActive,
	    		@RequestParam("isNotLocked")String isNotLocked,
	    		@RequestParam(value="profileImage",required=false)MultipartFile profileImage) throws UserNotFoundException, UsernameExistException, EmailExistException, IOException{
	    	
	    	User updateUser=userService.updateUser(currentUsername, firstName, lastName, username, email, role, Boolean.parseBoolean(isNotLocked),Boolean.parseBoolean(isActive), profileImage);
	    	
	    	return new ResponseEntity<> (updateUser, HttpStatus.OK);
	    	
	    }
	    
	    
	    @GetMapping("/find/{username}")
	    public ResponseEntity<User>getUser(@PathVariable("username")String username){
	    	
	    	User user=userService.findUserByUsername(username);
	    	return new ResponseEntity<>(user,HttpStatus.OK);
	    	
	    	
	    }
	    
	    @GetMapping("/list")
	    public ResponseEntity<List<User>>getAllUsers(){
	    	
	    	List<User> users=userService.getUsers();
	    	return new ResponseEntity<>(users,HttpStatus.OK);
	    	
	    	
	    }
	    
	    @GetMapping("/resetPassword/{email}")
	    public ResponseEntity<HttpResponse> resetPassword(@PathVariable("email")String email) throws EmailNotFoundException, MessagingException{
	    	
	    	userService.resetPassword(email);
	    	return response(HttpStatus.OK, "An email with new password sent to:"+email);
	    	
	    	
	    }
	    
	    @DeleteMapping("/delete/{id}")
	    @PreAuthorize("hasAnyAuthority('user:delete')")
	    public ResponseEntity<HttpResponse>deleteUser(@PathVariable("id")long id){
	    
	    	userService.deleteUser(id);
	    	return response(HttpStatus.NO_CONTENT, "User deleted successfully");
	    	
	    }
	    
	    @PostMapping("/updateProfileImage")
	    public ResponseEntity<User>updateProfileImage(@RequestParam("username")String username,
	    @RequestParam(value="profileImage")MultipartFile profileImage) throws UserNotFoundException, UsernameExistException, EmailExistException, IOException{
	    	
	    	User user=userService.updateProfileImage(username, profileImage);
	    	
	    	return new ResponseEntity<> (user, HttpStatus.OK);
	    	
	    }
	    
	    
	    @GetMapping(path="/imgage/{username}/{fileName}",produces=MediaType.IMAGE_JPEG_VALUE)
	    public byte[] getProfileImage(@PathVariable("username")String username,@PathVariable("fileName") String fileName) throws IOException {
	    	
	    	return Files.readAllBytes(Paths.get(FileConstant.USER_FOLDER+username+FileConstant.FORWARD_SLASH+fileName)); //"user.home+"supportportal/user/rick/rick.jpg"
	    
	    }
	    
	    @GetMapping(path="/imgage/{profile}/{username}",produces=MediaType.IMAGE_JPEG_VALUE)
	    public byte[] getTempProfileImage(@PathVariable("username")String username) throws IOException {
	    	
	    	URL url=new URL(FileConstant.TEMP_PROFILE_IMAGE_BASE_URL+username);
	    	ByteArrayOutputStream byteArrayOutputStream=new ByteArrayOutputStream();
	    	
	    	try(InputStream inputStream=url.openStream()){
	    		int byteRead;
	    		byte[]chunk=new byte[1024];
	    		while((byteRead=inputStream.read(chunk))>0) {
	    			byteArrayOutputStream.write(chunk, 0, byteRead);//0-1024 bytes
	    			
	    		}
	    	}
	    			return byteArrayOutputStream.toByteArray();
	    
	    }
	    
	    

	    private ResponseEntity<HttpResponse> response(HttpStatus httpStatus, String message) {
			
	    	
	    	return new ResponseEntity<>(new HttpResponse(httpStatus.value(),httpStatus,httpStatus.getReasonPhrase().toUpperCase(),
	    			message.toUpperCase()),httpStatus);
		}

		private HttpHeaders getJwtHeader(UserPrincipal user) {
	        HttpHeaders headers = new HttpHeaders();
	        headers.add(SecurityConstant.JWT_TOKEN_HEADER, jwtTokenProvider.generateJwtToken(user));
	        return headers;
	    }

	    private void authenticate(String username, String password) {
	        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(username, password));
	    }
	
	
	
}
