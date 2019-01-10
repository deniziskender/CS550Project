package com.compiler.dto;

import java.io.Serializable;
import java.util.ArrayList;

public class UserDTO implements Serializable {
	private static final long serialVersionUID = 1L;
	private int userId;
	private String name;
	private String surname;
	private String mail;
	private String bio;
	private String phoneNumber;
	private ArrayList<String> compilations;

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

	public String getBio() {
		return bio;
	}

	public void setBio(String bio) {
		this.bio = bio;
	}

	public String getPhoneNumber() {
		return phoneNumber;
	}

	public void setPhoneNumber(String phoneNumber) {
		this.phoneNumber = phoneNumber;
	}

	public ArrayList<String> getCompilations() {
		return compilations;
	}

	public void setCompilations(ArrayList<String> compilations) {
		this.compilations = compilations;
	}
}
