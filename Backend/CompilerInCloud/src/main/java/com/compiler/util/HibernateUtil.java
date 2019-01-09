package com.compiler.util;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.hibernate.cfg.beanvalidation.GroupsPerOperation.Operation;

import com.compiler.dao.BaseDAO;
import com.compiler.model.AccessToken;
import com.compiler.model.ActivationToken;
import com.compiler.model.Compilation;
import com.compiler.model.RefreshToken;
import com.compiler.model.User;
import com.compiler.model.UserEdition;
import com.compiler.model.UserSuspension;

public class HibernateUtil {
	private static final SessionFactory sessionFactory = buildSessionFactory();
	private static BaseDAO baseDAO = new BaseDAO();

	private static SessionFactory buildSessionFactory() {
		try {
			// Create the SessionFactory from hibernate.cfg.xml
			return new Configuration().configure().buildSessionFactory();
		} catch (Throwable ex) {
			// Make sure you log the exception, as it might be swallowed
			System.err.println("Initial SessionFactory creation failed." + ex);
			throw new ExceptionInInitializerError(ex);
		}
	}

	public static SessionFactory getSessionFactory() {
		return sessionFactory;
	}

	public static void shutdown() {
		// Close caches and connection pools
		getSessionFactory().close();
	}

	public static void insertUser(Session session, User user) {
		baseDAO.execute(session, user, Operation.INSERT);
	}

	public static void insertAccessToken(Session session, AccessToken model) {
		baseDAO.execute(session, model, Operation.INSERT);
	}

	public static void insertRefreshToken(Session session, RefreshToken model) {
		baseDAO.execute(session, model, Operation.INSERT);
	}

	public static void insertActivationToken(Session session, ActivationToken model) {
		baseDAO.execute(session, model, Operation.INSERT);
	}
	
	public static void insertUserSuspension(Session session, UserSuspension model) {
		baseDAO.execute(session, model, Operation.INSERT);
	}
	
	public static void insertUserEdition(Session session, UserEdition model) {
		baseDAO.execute(session, model, Operation.INSERT);
	}	

	public static void insertCompilation(Session session, Compilation model) {
		baseDAO.execute(session, model, Operation.INSERT);
	}

}
