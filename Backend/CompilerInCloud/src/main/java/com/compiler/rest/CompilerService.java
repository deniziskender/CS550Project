package com.compiler.rest;

import java.io.File;

import javax.ws.rs.FormParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;

import org.apache.commons.io.FileUtils;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;

import com.compiler.constants.GetQueries;
import com.compiler.constants.Info.ErrorMessage;
import com.compiler.constants.Info.SuccessMessage;
import com.compiler.model.Compilation;
import com.compiler.serviceutil.TokenService;
import com.compiler.util.EncrypterUtil;
import com.compiler.util.FileUtil;
import com.compiler.util.HibernateUtil;
import com.compiler.util.ObjectUtil;

@Path("/compilers")
public class CompilerService {
	private static final String INPUT_FILE = "compilertest.c";

	private static final String SCRIPT_V1 = FileUtil.SERVER_UPLOAD_LOCATION_FOLDER + "./vscompilerV1 "
			+ FileUtil.SERVER_UPLOAD_LOCATION_FOLDER + INPUT_FILE + " -- ";
	private static final String SCRIPT_V2 = FileUtil.SERVER_UPLOAD_LOCATION_FOLDER + "./vscompilerV2 "
			+ FileUtil.SERVER_UPLOAD_LOCATION_FOLDER + INPUT_FILE + " -- ";

	@POST
	@Path("/getAssemblyFromCV1/")
	public Response getAssemblyFromCV1(@FormParam("cCode") String cCode, @FormParam("token") String token) {
		Session session = null;
		ResponseBuilder response = null;
		try {
			session = HibernateUtil.getSessionFactory().openSession();
			if (TokenService.isAccessTokenValid(session, token)) {
				response = runCompiler(session, token, cCode, SCRIPT_V1);
			} else {
				response = Response.ok((Object) ErrorMessage.ACCESS_TOKEN_EXPIRED.getMessage());
			}
		} catch (HibernateException e) {
			System.err.println(e.getMessage());
		} finally {
			// first closes the session and then returns the value
			if (session != null && session.isOpen()) {
				session.close();
			}
		}
		return response.build();
	}

	@POST
	@Path("/getAssemblyFromCV2/")
	public Response getAssemblyFromCV2(@FormParam("cCode") String cCode, @FormParam("token") String token) {
		Session session = null;
		ResponseBuilder response = null;
		try {
			session = HibernateUtil.getSessionFactory().openSession();
			if (TokenService.isAccessTokenValid(session, token)) {
				response = runCompiler(session, token, cCode, SCRIPT_V2);
			} else {
				response = Response.ok((Object) ErrorMessage.ACCESS_TOKEN_EXPIRED.getMessage());
			}
		} catch (HibernateException e) {
			System.err.println(e.getMessage());
		} finally {
			// first closes the session and then returns the value
			if (session != null && session.isOpen()) {
				session.close();
			}
		}
		return response.build();
	}

	private ResponseBuilder runCompiler(Session session, String token, String cCode, String command) {
		// encrypt the c code
		String encryptedCCode = EncrypterUtil.encrypt(cCode);
		// get compilation from the db
		Query query = session.createQuery(GetQueries.getCompilationByKey);
		query.setParameter("encryptedCCode", encryptedCCode);
		Compilation compilation = (Compilation) query.uniqueResult();
		// already compiled so do not compile get the compilation
		ResponseBuilder response = null;
		if (compilation != null) {
			// File testAsm = new File(compilation.getFileName());
			// response = Response.ok((Object) testAsm);
			response = Response.ok((Object) SuccessMessage.ALREADY_COMPILED_THIS_CODE.getMessage());
		}
		// not compiled until now so compile
		else {
			try {
				File testAsm = new File(FileUtil.SERVER_UPLOAD_LOCATION_FOLDER + INPUT_FILE);
				FileUtils.writeStringToFile(testAsm, cCode);
				Process proc = Runtime.getRuntime().exec(command);
				proc.waitFor();
				// get last file name
				query = session.createQuery(GetQueries.getMaxFileIdFromCompilations);
				Integer lastInsertedFileId = (Integer) query.uniqueResult();
				if (lastInsertedFileId == null) {
					lastInsertedFileId = 1;
				}
				String fileName = ((lastInsertedFileId + 1) + ".asm");
				response = FileUtil.getOutput(proc, fileName);

				Integer userId = TokenService.getUserIdByAccessToken(session, token);

				HibernateUtil.insertCompilation(session,
						ObjectUtil.createCompilation(encryptedCCode, fileName, userId));
				
			} catch (Exception e) {
				System.err.println(e.getMessage());
				response = Response.ok((Object) ErrorMessage.NOT_OK.getMessage());
			}
		}
		return response;
	}
}