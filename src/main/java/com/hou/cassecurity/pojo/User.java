package com.hou.cassecurity.pojo;

import java.util.List;

public class User {
	
	private int id;
	
	private String username;
	
	private String pwd;
	
	private int	avalibale;
	
	private String note;
	
	private List<Role> roles;

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getPwd() {
		return pwd;
	}

	public void setPwd(String pwd) {
		this.pwd = pwd;
	}

	public int getAvalibale() {
		return avalibale;
	}

	public void setAvalibale(int avalibale) {
		this.avalibale = avalibale;
	}

	public String getNote() {
		return note;
	}

	public void setNote(String note) {
		this.note = note;
	}

	public List<Role> getRoles() {
		return roles;
	}

	public void setRoles(List<Role> roles) {
		this.roles = roles;
	}
	
	

}
