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
	public Response getAssemblyFromCV1(@FormParam("cCode") String cCode) {
		ResponseBuilder runCompiler = runCompiler(cCode, SCRIPT_V1);
		return runCompiler.build();
	}

	@POST
	@Path("/getAssemblyFromCV2/")
	public Response getAssemblyFromCV2(@FormParam("cCode") String cCode) {
		ResponseBuilder runCompiler = runCompiler(cCode, SCRIPT_V2);
		return runCompiler.build();
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