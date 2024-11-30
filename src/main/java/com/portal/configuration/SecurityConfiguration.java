package com.portal.configuration;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfiguration;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.portal.constant.SecurityConstant;
import com.portal.filter.JwtAccessDeniedHandler;
import com.portal.filter.JwtAuthenticationEntryPoint;
import com.portal.filter.JwtAuthorizationFilter;

@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class SecurityConfiguration {

	    private JwtAuthorizationFilter jwtAuthorizationFilter;
	    private JwtAccessDeniedHandler jwtAccessDeniedHandler;
	    private JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;
	    private UserDetailsService userDetailsService;
	    private BCryptPasswordEncoder bCryptPasswordEncoder;

	    @Autowired
	    public SecurityConfiguration(JwtAuthorizationFilter jwtAuthorizationFilter,
	                                 JwtAccessDeniedHandler jwtAccessDeniedHandler,
	                                 JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint,
	                                 @Qualifier("userDetailsService")UserDetailsService userDetailsService,
	                                 BCryptPasswordEncoder bCryptPasswordEncoder) {
	        this.jwtAuthorizationFilter = jwtAuthorizationFilter;
	        this.jwtAccessDeniedHandler = jwtAccessDeniedHandler;
	        this.jwtAuthenticationEntryPoint = jwtAuthenticationEntryPoint;
	        this.userDetailsService = userDetailsService;
	        this.bCryptPasswordEncoder = bCryptPasswordEncoder;
	    }

	    
	    @Bean
	    public AuthenticationManager authenticationManager(
	                                 AuthenticationConfiguration configuration) throws Exception {
	        return configuration.getAuthenticationManager();
	    }
	    
	    
	    
	    
	   // @Override
	    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
	        auth.userDetailsService(userDetailsService).passwordEncoder(bCryptPasswordEncoder);
	    }
	    
	    
	    
		    @Bean
		    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
		        return http.csrf().disable()
		               .authorizeHttpRequests()
		                .requestMatchers(SecurityConstant.PUBLIC_URLS).permitAll()
		                .and()
		                .authorizeHttpRequests().requestMatchers("/**")
		                .authenticated().and()
		                .sessionManagement()
		                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
		                .and()
		                .exceptionHandling().accessDeniedHandler(jwtAccessDeniedHandler)
		                .authenticationEntryPoint(jwtAuthenticationEntryPoint)
		                .and()
		                .addFilterBefore(jwtAuthorizationFilter, UsernamePasswordAuthenticationFilter.class)
		                .build();
		        
		        
		    }

	    /*@Override
	    protected void configure(HttpSecurity http) throws Exception {
	        http.csrf().disable().cors().and()
	                .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
	                .and().authorizeRequests().requestMatchers(SecurityConstant.PUBLIC_URLS).permitAll()
	                .anyRequest().authenticated()
	                .and()
	                .exceptionHandling().accessDeniedHandler(jwtAccessDeniedHandler)
	                .authenticationEntryPoint(jwtAuthenticationEntryPoint)
	                .and()
	                .addFilterBefore(jwtAuthorizationFilter, UsernamePasswordAuthenticationFilter.class);
	    }*/

	  

	    
	
	
}
