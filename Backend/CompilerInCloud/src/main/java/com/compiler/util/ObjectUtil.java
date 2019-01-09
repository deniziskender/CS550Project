package com.compiler.util;

import com.compiler.constants.Constants;
import com.compiler.model.AccessToken;
import com.compiler.model.ActivationToken;
import com.compiler.model.RefreshToken;
import com.compiler.model.User;
import com.compiler.model.UserEdition;
import com.compiler.model.UserSuspension;;

public class ObjectUtil {
	public static User createUser(String name, String surname, String mail, String password) {
		User user = new User();
		user.setName(name);
		user.setSurname(surname);
		user.setMail(mail);
		user.setPassword(password);
		user.setIsValidated(Constants.ZERO);
		user.setCreateDate(DateUtil.getCurrentDate());
		return user;
	}

	public static AccessToken createAccessToken(String text, int userId) {
		AccessToken accessToken = new AccessToken();
		accessToken.setText(text);
		accessToken.setUserId(userId);
		accessToken.setExpirationDate(DateUtil.getNextMonth());
		accessToken.setCreateDate(DateUtil.getCurrentDate());
		return accessToken;
	}

	public static RefreshToken createRefreshToken(String text, int userId) {
		RefreshToken refreshToken = new RefreshToken();
		refreshToken.setText(text);
		refreshToken.setUserId(userId);
		refreshToken.setIsUsed(Constants.ZERO);
		refreshToken.setCreateDate(DateUtil.getCurrentDate());
		return refreshToken;
	}

	public static ActivationToken createActivationToken(String text, int userId) {
		ActivationToken refreshToken = new ActivationToken();
		refreshToken.setText(text);
		refreshToken.setUserId(userId);
		refreshToken.setCreateDate(DateUtil.getCurrentDate());
		return refreshToken;
	}

	public static UserSuspension createUserSuspension(int userId) {
		UserSuspension userSuspension = new UserSuspension();
		userSuspension.setUserId(userId);
		userSuspension.setSuspendDate(DateUtil.getCurrentDate());
		return userSuspension;
	}
	
	public static UserEdition createUserEdition(int userId) {
		UserEdition userEdition = new UserEdition();
		userEdition.setUserId(userId);
		userEdition.setEditDate(DateUtil.getCurrentDate());
		return userEdition;
	}
}