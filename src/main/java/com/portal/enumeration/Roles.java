package com.portal.enumeration;

import com.portal.constant.Authority;

public enum Roles {

	
	    ROLE_USER(Authority.USER_AUTHORITIES),
	    ROLE_HR(Authority.HR_AUTHORITIES),
	    ROLE_MANAGER(Authority.MANAGER_AUTHORITIES),
	    ROLE_ADMIN(Authority.ADMIN_AUTHORITIES),
	    ROLE_SUPER_ADMIN(Authority.SUPER_ADMIN_AUTHORITIES);

	    private String[] authorities;

	    Roles(String... authorities) {
	        this.authorities = authorities;
	    }

	    public String[] getAuthorities() {
	        return authorities;
	    }
	
	
}
