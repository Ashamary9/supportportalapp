package com.portal.filter;

import java.io.IOException;
import java.io.OutputStream;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.Http403ForbiddenEntryPoint;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.portal.constant.SecurityConstant;
import com.portal.domain.HttpResponse;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class JwtAuthenticationEntryPoint extends Http403ForbiddenEntryPoint {

	    @Override
	     public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException exception) throws IOException {
	        HttpResponse httpResponse = new HttpResponse(HttpStatus.FORBIDDEN.value(), HttpStatus.FORBIDDEN, HttpStatus.FORBIDDEN.getReasonPhrase().toUpperCase(), SecurityConstant.FORBIDDEN_MESSAGE);
	        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
	        response.setStatus(HttpStatus.FORBIDDEN.value());
	        OutputStream outputStream = response.getOutputStream();
	        ObjectMapper mapper = new ObjectMapper();
	        mapper.writeValue(outputStream, httpResponse);
	        outputStream.flush();
	    }
	
	
	
}
