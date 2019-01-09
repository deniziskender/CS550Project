package com.compiler.dao;

import org.apache.log4j.Logger;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.cfg.beanvalidation.GroupsPerOperation.Operation;

public class BaseDAO {

	final static Logger logger = Logger.getLogger(BaseDAO.class);

	public void execute(Session session, Object object, Operation operationType) {
		Transaction tx = null;
		try {
			tx = session.beginTransaction();

			if (Operation.INSERT.equals(operationType)) {
				insert(session, object);
			} else if (Operation.UPDATE.equals(operationType)) {
				update(session, object);
			} else if (Operation.DELETE.equals(operationType)) {
				delete(session, object);
			}

			tx.commit();
		} catch (Exception e) {
			if (tx != null) {
				tx.rollback();
			}
			logger.error(String.format("Execute hata: %s", e.getMessage()));
		}
	}

	private void insert(Session session, Object object) {
		session.save(object);
	}

	// diger taraftan update edilen object gelecek.
	private void update(Session session, Object object) {
		session.update(object);
	}

	private void delete(Session session, Object object) {
		session.delete(object);
	}
}