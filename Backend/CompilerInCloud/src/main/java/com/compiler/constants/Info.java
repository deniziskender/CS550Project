package com.compiler.constants;

public class Info {

	public enum SuccessMessage {
		// @formatter:off
		LOGOUT("Succesfully logged out."),
		PROFILE_VALIDATED("Your profile succesfully validated."),
		PROFILE_SUSPENSION("Your profile succesfully suspended."),
		ALREADY_COMPILED_THIS_CODE("Already compiled this code, check your profile.");

		// @formatter:on

		public String message;

		SuccessMessage(String message) {
			this.message = message;
		}

		public String getMessage() {
			return message;
		}
	}

	public enum ErrorMessage {
		// @formatter:off
		USER_NAME_FORMAT_ERROR("User name is not valid."),
		USER_LOGIN_ERROR("Mail address or password is wrong."),
		E_MAIL_FORMAT_ERROR("E-mail is not valid."),
		ONLY_OZU_MAIL_IS_VALID("Only ozu.edu.tr and ozyegin.edu.tr mail addresses are allowed."),
		E_MAIL_EMPTY_ERROR("E-mail can not be empty."),
		PASSWORD_EMPTY_ERROR("Password can not be empty."),
		USER_NOT_FOUND("User not found."), 
		USERNAME_ALREADY_EXIST("Username already exists."), 
		MAIL_ALREADY_EXIST("Mail address already exists."),
		USER_SUSPENDED_ACCOUNT("User suspended the profile, not available to see."), 
		PHONE_NUMBER_VISIBILITY("Phone numbers can only be seen by admins."),

		REFRESH_TOKEN_IS_ALREADY_USED("Refresh token is already used."),
		ACCESS_TOKEN_EXPIRED("Your account is expired. Please sign in."),
		NOT_OK("Not Ok.");
		// @formatter:on

		public String message;

		ErrorMessage(String message) {
			this.message = message;
		}

		public String getMessage() {
			return message;
		}
	}
}
