package com.compiler.rest;

import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.FormParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.loader.plan.build.spi.QuerySpaceTreePrinter;

import com.compiler.constants.Constants;
import com.compiler.constants.GetQueries;
import com.compiler.constants.Info.ErrorMessage;
import com.compiler.constants.Info.SuccessMessage;
import com.compiler.constants.UpdateQueries;
import com.compiler.dto.UndetailedUserDTO;
import com.compiler.dto.UserDTO;
import com.compiler.dto.UserTokenDTO;
import com.compiler.model.AccessToken;
import com.compiler.model.Compilation;
import com.compiler.model.RefreshToken;
import com.compiler.model.User;
import com.compiler.serviceutil.TokenService;
import com.compiler.util.EncrypterUtil;
import com.compiler.util.FileUtil;
import com.compiler.util.HibernateUtil;
import com.compiler.util.Mapper;
import com.compiler.util.ObjectUtil;
import com.compiler.util.ParameterValidationUtil;
import com.compiler.util.RandomTokenGen;
import com.google.gson.Gson;

@Path("/users")
public class UserService {
	@POST
	@Path("/getNewTokens/")
	public String getNewTokens(@FormParam("refreshToken") String refreshToken) {
		Session session = null;
		try {
			session = HibernateUtil.getSessionFactory().openSession();
			if (TokenService.isRefreshTokenValid(session, refreshToken)) {
				Query query = session.createQuery(GetQueries.getRefreshTokenByText);
				query.setParameter("text", refreshToken);
				RefreshToken oldRefreshToken = (RefreshToken) query.uniqueResult();

				query = session.createQuery(UpdateQueries.editRefreshToken);
				query.setParameter("isUsed", 1);
				query.setParameter("text", refreshToken);
				query.executeUpdate();

				RandomTokenGen randomGen = new RandomTokenGen();
				String newAccessToken = randomGen.nextString();
				HibernateUtil.insertAccessToken(session,
						ObjectUtil.createAccessToken(newAccessToken, oldRefreshToken.getUserId()));

				String newRefreshToken = randomGen.nextString();
				HibernateUtil.insertRefreshToken(session,
						ObjectUtil.createRefreshToken(newRefreshToken, oldRefreshToken.getUserId()));

				UserTokenDTO tokenDto = new UserTokenDTO();
				tokenDto.setAccessToken(newAccessToken);
				tokenDto.setRefreshToken(newRefreshToken);
				return new Gson().toJson(tokenDto);
			} else {
				return new Gson().toJson(ErrorMessage.REFRESH_TOKEN_IS_ALREADY_USED.getMessage());
			}
		} catch (HibernateException e) {
			System.err.println(e.getMessage());
		} finally {
			if (session != null && session.isOpen()) {
				session.close();
			}
		}
		return new Gson().toJson(ErrorMessage.NOT_OK.getMessage());
	}

	// returns all profile information
	@POST
	@Path("/getUserById/")
	public String getUserById(@FormParam("requestedUserId") int requestedUserId, @FormParam("token") String token) {
		Session session = null;
		try {
			session = HibernateUtil.getSessionFactory().openSession();
			if (TokenService.isAccessTokenValid(session, token)) {
				// get user
				Query query = session.createQuery(GetQueries.getUserById);
				query.setParameter("id", requestedUserId);
				User user = (User) query.uniqueResult();
				if (user != null) {
					if (user.getIsSuspended() == Constants.ONE) {
						return new Gson().toJson(ErrorMessage.USER_SUSPENDED_ACCOUNT.getMessage());
					} else {
						UserDTO userDTO = Mapper.mapFromUserToUserDTO(user);
						query = session.createQuery(GetQueries.getCompilationByUserId);
						userDTO.setCompilations(getCompilations(session, user.getId(), query));
						return new Gson().toJson(userDTO);
					}
				} else {
					return new Gson().toJson(ErrorMessage.USER_NOT_FOUND.getMessage());
				}
			} else {
				return new Gson().toJson(ErrorMessage.ACCESS_TOKEN_EXPIRED.getMessage());
			}
		} catch (HibernateException e) {
			System.err.println(e.getMessage());
		} finally {
			if (session != null && session.isOpen()) {
				session.close();
			}
		}
		return new Gson().toJson(ErrorMessage.NOT_OK);
	}

	// login
	@POST
	@Path("/getUserByMailAndPassword/")
	public String getUserByMailAndPassword(@FormParam("mail") String mail, @FormParam("password") String password) {
		if (StringUtils.isBlank(mail)) {
			return new Gson().toJson(ErrorMessage.E_MAIL_EMPTY_ERROR.getMessage());
		} else if (StringUtils.isBlank(password)) {
			return new Gson().toJson(ErrorMessage.PASSWORD_EMPTY_ERROR.getMessage());
		}
		Session session = null;
		try {
			session = HibernateUtil.getSessionFactory().openSession();
			String encryptedPassword = EncrypterUtil.encrypt(password);
			Query query = session.createQuery(GetQueries.getUserByMailAndPassword);
			query.setParameter("mail", mail);
			query.setParameter("password", encryptedPassword);
			User user = (User) query.uniqueResult();
			if (user != null) {
				UndetailedUserDTO userDTO = Mapper.mapFromUserToUndetailedUserDTO(user);
				// if suspended, remove suspension
				query = session.createQuery(UpdateQueries.suspendUser);
				query.setParameter("id", user.getId());
				query.setParameter("isSuspended", 0);
				int result = query.executeUpdate();
				if (result == Constants.ZERO) {
					return new Gson().toJson(ErrorMessage.NOT_OK.getMessage());
				} else {
					// make other access token invalid
					query = session.createQuery(UpdateQueries.makeAllAccessTokensInvalid);
					query.setParameter("userId", userDTO.getUserId());
					query.setParameter("isInvalid", Constants.ONE);
					query.executeUpdate();
					// create the new ones
					RandomTokenGen randomGen = new RandomTokenGen();
					String accessToken = randomGen.nextString();
					HibernateUtil.insertAccessToken(session,
							ObjectUtil.createAccessToken(accessToken, userDTO.getUserId()));

					String refreshToken = randomGen.nextString();
					HibernateUtil.insertRefreshToken(session,
							ObjectUtil.createRefreshToken(refreshToken, userDTO.getUserId()));

					UserTokenDTO tokenDto = new UserTokenDTO();
					tokenDto.setAccessToken(accessToken);
					tokenDto.setRefreshToken(refreshToken);
					return new Gson().toJson(tokenDto);
				}
			} else {
				return new Gson().toJson(ErrorMessage.USER_LOGIN_ERROR.getMessage());
			}
		} catch (HibernateException e) {
			System.err.println(e.getMessage());
		} finally {
			if (session != null && session.isOpen()) {
				session.close();
			}
		}
		return new Gson().toJson(ErrorMessage.NOT_OK.getMessage());
	}

	@POST
	@Path("/logout/")
	public String logout(@FormParam("token") String token) {
		Session session = null;
		try {
			session = HibernateUtil.getSessionFactory().openSession();
			if (TokenService.isAccessTokenValid(session, token)) {
				Query query = session.createQuery(UpdateQueries.makeAccessTokenInvalid);
				query.setParameter("text", token);
				query.setParameter("isInvalid", Constants.ONE);
				int result = query.executeUpdate();
				if (result == Constants.ZERO) {
					return new Gson().toJson(ErrorMessage.NOT_OK.getMessage());
				} else {
					return new Gson().toJson(SuccessMessage.LOGOUT.getMessage());
				}
			} else {
				return new Gson().toJson(ErrorMessage.ACCESS_TOKEN_EXPIRED.getMessage());
			}
		} catch (HibernateException e) {
			System.err.println(e.getMessage());
		} finally {
			if (session != null && session.isOpen()) {
				session.close();
			}
		}
		return new Gson().toJson(ErrorMessage.NOT_OK);
	}

	@POST
	@Produces({ MediaType.APPLICATION_JSON })
	@Path("/editProfile/")
	public String editProfile(@FormParam("name") String name, @FormParam("surname") String surname,
			@FormParam("mail") String mail, @FormParam("password") String password, @FormParam("bio") String bio,
			@FormParam("phoneNumber") String phoneNumber, @FormParam("instagramLink") String instagramLink,
			@FormParam("twitterLink") String twitterLink, @FormParam("linkedinLink") String linkedinLink,
			@FormParam("token") String token) {
		if (!ParameterValidationUtil.isMailAddressFormatted(mail)) {
			return new Gson().toJson(ErrorMessage.E_MAIL_FORMAT_ERROR.getMessage());
		} else if (StringUtils.isBlank(password)) {
			return new Gson().toJson(ErrorMessage.PASSWORD_EMPTY_ERROR.getMessage());
		} else if (!StringUtils.endsWith(mail, Constants.AT_OZU_DOT_EDU_DOT_TR)
				&& !StringUtils.endsWith(mail, Constants.AT_OZYEGIN_DOT_EDU_DOT_TR)) {
			return new Gson().toJson(ErrorMessage.ONLY_OZU_MAIL_IS_VALID.getMessage());
		}
		Session session = null;
		try {
			session = HibernateUtil.getSessionFactory().openSession();
			if (TokenService.isAccessTokenValid(session, token)) {
				session = HibernateUtil.getSessionFactory().openSession();
				String encryptedPassword = EncrypterUtil.encrypt(password);
				Query query = session.createQuery(UpdateQueries.editUser);
				query.setParameter("name", name);
				query.setParameter("surname", surname);
				query.setParameter("mail", mail);
				query.setParameter("password", encryptedPassword);
				query.setParameter("bio", bio);
				query.setParameter("phoneNumber", phoneNumber);
				query.setParameter("instagramLink", instagramLink);
				query.setParameter("twitterLink", twitterLink);
				query.setParameter("linkedinLink", linkedinLink);

				Integer userId = TokenService.getUserIdByAccessToken(session, token);
				query.setParameter("id", userId);
				int result = query.executeUpdate();
				// nothing is updated
				if (result == 0) {
					new Gson().toJson(ErrorMessage.NOT_OK.getMessage());
				} else {
					HibernateUtil.insertUserEdition(session, ObjectUtil.createUserEdition(userId));
					// get user details to return
					query = session.createQuery(GetQueries.getUserById);
					query.setParameter("id", userId);
					User user = (User) query.uniqueResult();
					UserDTO userDTO = Mapper.mapFromUserToUserDTO(user);
					return new Gson().toJson(userDTO);
				}
			} else {
				return new Gson().toJson(ErrorMessage.ACCESS_TOKEN_EXPIRED.getMessage());
			}

		} catch (HibernateException e) {
			System.err.println(e.getMessage());
		} finally {
			// first closes the session and then returns the value
			if (session != null && session.isOpen()) {
				session.close();
			}
		}
		return new Gson().toJson(ErrorMessage.NOT_OK.getMessage());
	}

	// sign up
	@POST
	@Produces({ MediaType.APPLICATION_JSON })
	@Path("/insertUser/")
	public String insertUser(@FormParam("name") String name, @FormParam("surname") String surname,
			@FormParam("mail") String mail, @FormParam("password") String password) {
		if (!ParameterValidationUtil.isMailAddressFormatted(mail)) {
			return new Gson().toJson(ErrorMessage.E_MAIL_FORMAT_ERROR.getMessage());
		} else if (StringUtils.isBlank(password)) {
			return new Gson().toJson(ErrorMessage.PASSWORD_EMPTY_ERROR.getMessage());
		} else if (!StringUtils.endsWith(mail, Constants.AT_OZU_DOT_EDU_DOT_TR)
				&& !StringUtils.endsWith(mail, Constants.AT_OZYEGIN_DOT_EDU_DOT_TR)) {
			return new Gson().toJson(ErrorMessage.ONLY_OZU_MAIL_IS_VALID.getMessage());
		}
		Session session = null;
		try {
			session = HibernateUtil.getSessionFactory().openSession();
			Query query = session.createQuery(GetQueries.getUserByMail);
			query.setParameter("mail", mail);
			List<User> userList = (ArrayList<User>) query.list();
			if (!CollectionUtils.isEmpty(userList)) {
				return new Gson().toJson(ErrorMessage.MAIL_ALREADY_EXIST.getMessage());
			}
			String encryptedPassword = EncrypterUtil.encrypt(password);
			HibernateUtil.insertUser(session, ObjectUtil.createUser(name, surname, mail, encryptedPassword));

			query = session.createQuery(GetQueries.getUserByMail);
			query.setParameter("mail", mail);
			User insertedUser = (User) query.uniqueResult();

			if (insertedUser != null) {
				RandomTokenGen randomGen = new RandomTokenGen();
				String accessToken = randomGen.nextString();
				HibernateUtil.insertAccessToken(session,
						ObjectUtil.createAccessToken(accessToken, insertedUser.getId()));

				String refreshToken = randomGen.nextString();
				HibernateUtil.insertRefreshToken(session,
						ObjectUtil.createRefreshToken(refreshToken, insertedUser.getId()));

				RandomTokenGen activationGen = new RandomTokenGen(Constants.EIGHT);
				String activationToken = activationGen.nextString();
				HibernateUtil.insertActivationToken(session,
						ObjectUtil.createActivationToken(activationToken, insertedUser.getId()));

				UserTokenDTO tokenDto = new UserTokenDTO();
				tokenDto.setAccessToken(accessToken);
				tokenDto.setRefreshToken(refreshToken);
				return new Gson().toJson(tokenDto);
			} else {
				return new Gson().toJson(ErrorMessage.NOT_OK.getMessage());
			}
		} catch (HibernateException e) {
			System.err.println(e.getMessage());
		} finally {
			// first closes the session and then returns the value
			if (session != null && session.isOpen()) {
				session.close();
			}
		}
		return new Gson().toJson(ErrorMessage.NOT_OK.getMessage());
	}

	@POST
	@Path("/getProfile/")
	public String getProfile(@FormParam("token") String token) {
		Session session = null;
		try {
			session = HibernateUtil.getSessionFactory().openSession();
			if (TokenService.isAccessTokenValid(session, token)) {
				// get user
				Query query = session.createQuery(GetQueries.getAccessTokenByText);
				query.setParameter("text", token);
				AccessToken accessToken = (AccessToken) query.uniqueResult();
				// get profile
				query = session.createQuery(GetQueries.getUserById);
				query.setParameter("id", accessToken.getUserId());
				User user = (User) query.uniqueResult();
				if (user != null) {
					UserDTO userDTO = Mapper.mapFromUserToUserDTO(user);
					query = session.createQuery(GetQueries.getCompilationByUserId);
					userDTO.setCompilations(getCompilations(session, user.getId(), query));
					return new Gson().toJson(userDTO);
				}
			} else {
				return new Gson().toJson(ErrorMessage.ACCESS_TOKEN_EXPIRED.getMessage());
			}
		} catch (HibernateException e) {
			System.err.println(e.getMessage());
		} finally {
			if (session != null && session.isOpen()) {
				session.close();
			}
		}
		return new Gson().toJson(ErrorMessage.NOT_OK);
	}

	@POST
	@Path("/getCompilationsAscending/")
	public String getCompilationsAscending(@FormParam("token") String token) {
		Session session = null;
		try {
			session = HibernateUtil.getSessionFactory().openSession();
			if (TokenService.isAccessTokenValid(session, token)) {
				Query query = session.createQuery(GetQueries.getAccessTokenByText);
				query.setParameter("text", token);
				AccessToken accessToken = (AccessToken) query.uniqueResult();
				query = session.createQuery(GetQueries.getCompilationByUserIdAscending);
				return new Gson().toJson(getCompilations(session, accessToken.getUserId(), query));
			}
		} catch (HibernateException e) {
			System.err.println(e.getMessage());
		} finally {
			if (session != null && session.isOpen()) {
				session.close();
			}
		}
		return new Gson().toJson(ErrorMessage.NOT_OK);
	}

	@POST
	@Path("/getCompilationsDescending/")
	public String getCompilationsDescending(@FormParam("token") String token) {
		Session session = null;
		try {
			session = HibernateUtil.getSessionFactory().openSession();
			if (TokenService.isAccessTokenValid(session, token)) {
				Query query = session.createQuery(GetQueries.getAccessTokenByText);
				query.setParameter("text", token);
				AccessToken accessToken = (AccessToken) query.uniqueResult();
				query = session.createQuery(GetQueries.getCompilationByUserIdDescending);
				return new Gson().toJson(getCompilations(session, accessToken.getUserId(), query));
			}
		} catch (HibernateException e) {
			System.err.println(e.getMessage());
		} finally {
			if (session != null && session.isOpen()) {
				session.close();
			}
		}
		return new Gson().toJson(ErrorMessage.NOT_OK);
	}

	public static ArrayList<Compilation> getCompilations(Session session, int userId, Query query) {
		query.setParameter("userId", userId);
		ArrayList<Compilation> compilations = (ArrayList<Compilation>) query.list();
		if (CollectionUtils.isNotEmpty(compilations)) {
			for (Compilation compilation : compilations) {
				compilation.setFileName(FileUtil.SERVER_UPLOAD_LOCATION_FOLDER + compilation.getFileName());
			}
		}
		return compilations;
	}

	@POST
	@Path("/suspendAccount/")
	public String suspendAccount(@FormParam("token") String token) {
		Session session = null;
		try {
			session = HibernateUtil.getSessionFactory().openSession();
			if (TokenService.isAccessTokenValid(session, token)) {
				// get user
				Query query = session.createQuery(GetQueries.getAccessTokenByText);
				query.setParameter("text", token);
				AccessToken accessToken = (AccessToken) query.uniqueResult();
				if (accessToken != null) {
					query = session.createQuery(UpdateQueries.suspendUser);
					query.setParameter("id", accessToken.getUserId());
					query.setParameter("isSuspended", 1);
					int result = query.executeUpdate();
					if (result == Constants.ZERO) {
						return new Gson().toJson(ErrorMessage.NOT_OK.getMessage());
					} else {
						HibernateUtil.insertUserSuspension(session,
								(ObjectUtil.createUserSuspension(accessToken.getUserId())));
						return new Gson().toJson(SuccessMessage.PROFILE_SUSPENSION.getMessage());
					}
				}
			} else {
				return new Gson().toJson(ErrorMessage.ACCESS_TOKEN_EXPIRED.getMessage());
			}
		} catch (HibernateException e) {
			System.err.println(e.getMessage());
		} finally {
			if (session != null && session.isOpen()) {
				session.close();
			}
		}
		return new Gson().toJson(ErrorMessage.NOT_OK);
	}
}