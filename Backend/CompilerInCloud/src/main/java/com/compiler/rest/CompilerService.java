package com.compiler.rest;

import java.io.File;

import javax.ws.rs.FormParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;

import org.apache.commons.io.FileUtils;
import org.hibernate.HibernateException;
import org.hibernate.Session;

import com.compiler.constants.Info.ErrorMessage;
import com.compiler.serviceutil.TokenService;
import com.compiler.util.FileUtil;
import com.compiler.util.HibernateUtil;

@Path("/compilers")
public class CompilerService {
	private static final String INPUT_FILE = "compilertest.c";
	private static final String OUTPUT_FILE = "compilertemp.asm";

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
				response = runCompiler(cCode, SCRIPT_V1);
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
				response = runCompiler(cCode, SCRIPT_V2);
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

	private ResponseBuilder runCompiler(String cCode, String command) {
		ResponseBuilder response = null;
		try {
			File testAsm = new File(FileUtil.SERVER_UPLOAD_LOCATION_FOLDER + INPUT_FILE);
			FileUtils.writeStringToFile(testAsm, cCode);
			Process proc = Runtime.getRuntime().exec(command);
			proc.waitFor();
			response = FileUtil.getOutput(proc, OUTPUT_FILE);
		} catch (Exception ex) {
			response = Response.ok((Object) ErrorMessage.NOT_OK.getMessage());
		}
		return response;
	}
}