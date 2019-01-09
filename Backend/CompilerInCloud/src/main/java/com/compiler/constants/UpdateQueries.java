package com.compiler.constants;

public class UpdateQueries {
	public static final String editRefreshToken = "UPDATE RefreshToken SET isUsed = :isUsed " + "WHERE text  = :text";

	public static final String makeAccessTokenInvalid = "UPDATE AccessToken SET isInvalid = :isInvalid "
			+ "WHERE text  = :text";

	public static final String makeAllAccessTokensInvalid = "UPDATE AccessToken SET isInvalid = :isInvalid "
			+ "WHERE userId  = :userId";

	public static final String editUser = "UPDATE User SET name =  :name , surname =  :surname , mail =  :mail ,"
			+ "password =  :password , bio =  :bio , phoneNumber =  :phoneNumber , instagramLink =  :instagramLink , "
			+ "twitterLink =  :twitterLink , linkedinLink =  :linkedinLink WHERE id  = :id";

	public static final String suspendUser = "UPDATE User SET isSuspended =  :isSuspended WHERE id  = :id";

	public static final String activateUser = "UPDATE User SET isActivated =  :isActivated WHERE id  = :id";
}
