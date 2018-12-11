package com.hou.cassecurity.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class UserController {
	
	@RequestMapping("/login")
	@PreAuthorize("hasRole('USER')")
	public String login() {
		
		return "login";
	}
	
	@PreAuthorize("hasRole('USER')")
	@RequestMapping("/logout")
	public String logout() {
		
		return "logout";
	}
	
	@RequestMapping("/index")
	public String index() {
		
		return "index";
	}
	
	@PreAuthorize("hasRole('ADMIN')")
	@RequestMapping("/admin")
	public String welcom() {
		
		return "admin";
	}


}
