package com.compiler.rest;

import java.io.File;

import javax.ws.rs.FormParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;

import org.apache.commons.io.FileUtils;
import org.hibernate.Session;

import com.compiler.constants.Info.ErrorMessage;
import com.compiler.serviceutil.TokenService;
import com.compiler.util.FileUtil;
import com.compiler.util.HibernateUtil;

@Path("/simulators")
public class SimulatorService {
	private static final String INPUT_FILE = "simulatortemp.asm";
	private static final String OUTPUT_FILE = "simulatorresult";

	@POST
	@Path("/getRealValueFromAssembly/")
	public Response getRealValueFromAssembly(@FormParam("assemblyCode") String assemblyCode, @FormParam("token") String token) {
		Session session = HibernateUtil.getSessionFactory().openSession();
		if (TokenService.isAccessTokenValid(session, token)) {
			ResponseBuilder response = null;
			try {
				File tempAsm = new File(FileUtil.SERVER_UPLOAD_LOCATION_FOLDER + INPUT_FILE);
				FileUtils.writeStringToFile(tempAsm, assemblyCode);
				Process proc = Runtime.getRuntime().exec(SCRIPT);
				proc.waitFor();
				response = FileUtil.getOutput(proc, OUTPUT_FILE);
			} catch (Exception ex) {
				response = Response.ok((Object) ErrorMessage.NOT_OK.getMessage());
			}
			return response.build();
		} else {
			return Response.ok((Object) ErrorMessage.ACCESS_TOKEN_EXPIRED.getMessage()).build();
		}
	}

	private static final String SCRIPT = FileUtil.SERVER_UPLOAD_LOCATION_FOLDER + "./assembler_iss_mem_init_gen.py "
			+ "-i " + FileUtil.SERVER_UPLOAD_LOCATION_FOLDER + INPUT_FILE + " -a 2 -s 0 ";
}