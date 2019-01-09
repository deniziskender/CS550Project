package com.compiler.dto;

import java.io.Serializable;

public class UndetailedUserDTO implements Serializable {
	private static final long serialVersionUID = 1L;
	private int userId;
	private String name;
	private String surname;
	private String mail;
	
	public int getUserId() {
		return userId;
	}

	public void setUserId(int userId) {
		this.userId = userId;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getSurname() {
		return surname;
	}

	public void setSurname(String surname) {
		this.surname = surname;
	}

	public String getMail() {
		return mail;
	}

	public void setMail(String mail) {
		this.mail = mail;
	}
}
