package com.compiler.util;

import com.compiler.dto.UndetailedUserDTO;
import com.compiler.dto.UserDTO;
import com.compiler.model.User;

public class Mapper {
	public static UndetailedUserDTO mapFromUserToUndetailedUserDTO(User model) {
		UndetailedUserDTO dto = new UndetailedUserDTO();
		dto.setUserId(model.getId());
		dto.setName(model.getName());
		dto.setSurname(model.getSurname());
		dto.setMail(model.getMail());
		return dto;
	}

	public static UserDTO mapFromUserToUserDTO(User model) {
		UserDTO dto = new UserDTO();
		dto.setUserId(model.getId());
		dto.setName(model.getName());
		dto.setSurname(model.getSurname());
		dto.setPhoneNumber(model.getPhoneNumber());
		dto.setBio(model.getBio());
		dto.setMail(model.getMail());
		dto.setTwitterLink(model.getTwitterLink());
		dto.setInstagramLink(model.getInstagramLink());
		dto.setLinkedinLink(model.getLinkedinLink());
		return dto;
	}
}