package com.compiler.constants;

public class GetQueries {
	// user info
	public static final String getUserByLikeNameOrSurname = "from User WHERE name LIKE :nameOrSurname or surname LIKE :nameOrSurname ";
	public static final String getUserById = "from User WHERE id = :id";
	public static final String getUserByLastCreateDate = "from User order by createDate desc";
	public static final String getUserByMailAndPassword = "from User WHERE mail = :mail and password = :password";
	public static final String getUserByMail = "from User WHERE mail = :mail";
	public static final String getAccessTokenByText = "from AccessToken WHERE text = :text";
	public static final String getRefreshTokenByText = "from RefreshToken WHERE text = :text";
	public static final String getActivationTokenByText = "from ActivationToken WHERE text = :text";

	// compilations
	public static final String getCompilationByKey = "from Compilation WHERE encryptedCCode = :encryptedCCode";
	public static final String getCompilationByUserId = "from Compilation WHERE userId = :userId";
	public static final String getCompilationByUserIdAscending = "from Compilation WHERE userId = :userId order by createDate asc";
	public static final String getCompilationByUserIdDescending = "from Compilation WHERE userId = :userId order by createDate desc";
	public static final String getCompilationByKeyAndUserId = "from Compilation WHERE encryptedCCode = :encryptedCCode"
			+ " and userId = :userId";
	public static final String getMaxFileIdFromCompilations = "select max(id) from Compilation";

}
