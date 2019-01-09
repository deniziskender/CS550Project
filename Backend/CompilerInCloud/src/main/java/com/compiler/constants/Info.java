package com.compiler.constants;

public class Info {

	public enum SuccessMessage {
		// @formatter:off
		USER_CREATION("User successfully created.");

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
