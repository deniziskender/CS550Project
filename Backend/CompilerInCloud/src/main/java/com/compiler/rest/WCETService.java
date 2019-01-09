package com.compiler.rest;

import java.io.File;

import javax.ws.rs.FormParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;

import org.apache.commons.io.FileUtils;

import com.compiler.constants.Info.ErrorMessage;
import com.compiler.util.FileUtil;

@Path("/wcet")
public class WCETService {
	private static final String INPUT_FILE = "wcettest.c";
	private static final String OUTPUT_FILE = "wcetresult";

	private static final String SCRIPT_V1 = FileUtil.SERVER_UPLOAD_LOCATION_FOLDER + "./vswcet "
			+ FileUtil.SERVER_UPLOAD_LOCATION_FOLDER + INPUT_FILE + " -- ";

	@POST
	@Path("/getWCETAnalysis/")
	public Response getAssemblyFromCV1(@FormParam("cCode") String cCode) {
		ResponseBuilder response = null;
		try {
			File testAsm = new File(FileUtil.SERVER_UPLOAD_LOCATION_FOLDER + INPUT_FILE);
			FileUtils.writeStringToFile(testAsm, cCode);
			Process proc = Runtime.getRuntime().exec(SCRIPT_V1);
			proc.waitFor();
			response = FileUtil.getOutput(proc, OUTPUT_FILE);
		} catch (Exception ex) {
			response = Response.ok((Object) ErrorMessage.NOT_OK.getMessage());
		}
		return response.build();
	}
}