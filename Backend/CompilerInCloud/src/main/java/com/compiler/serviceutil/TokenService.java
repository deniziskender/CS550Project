package com.compiler.serviceutil;

import java.util.Calendar;

import org.hibernate.Query;
import org.hibernate.Session;

import com.compiler.constants.Constants;
import com.compiler.constants.GetQueries;
import com.compiler.model.AccessToken;
import com.compiler.model.RefreshToken;

public class TokenService {

	public static boolean isAccessTokenValid(Session session, String text) {
		Query query = session.createQuery(GetQueries.getAccessTokenByText);
		query.setParameter("text", text);
		AccessToken accessToken = (AccessToken) query.uniqueResult();

		Calendar today = Calendar.getInstance();

		Calendar nextMonth = Calendar.getInstance();
		nextMonth.add(Calendar.MONTH, 1);

		return accessToken != null && nextMonth.getTime().after(accessToken.getExpirationDate())
				&& accessToken.getExpirationDate().after(today.getTime())
				&& accessToken.getIsInvalid() == Constants.ZERO;
	}

	public static boolean isRefreshTokenValid(Session session, String text) {
		Query query = session.createQuery(GetQueries.getRefreshTokenByText);
		query.setParameter("text", text);
		RefreshToken refreshToken = (RefreshToken) query.uniqueResult();
		return refreshToken.getIsUsed() == Constants.ZERO;
	}

	public static Integer getUserIdByAccessToken(Session session, String text) {
		Query query = session.createQuery(GetQueries.getAccessTokenByText);
		query.setParameter("text", text);
		AccessToken accessToken = (AccessToken) query.uniqueResult();
		return accessToken.getUserId();
	}
}
