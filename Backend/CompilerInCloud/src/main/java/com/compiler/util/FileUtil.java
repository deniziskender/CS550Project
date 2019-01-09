package com.compiler.util;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Writer;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;

public class FileUtil {
	public static final String SERVER_UPLOAD_LOCATION_FOLDER = "/opt/tomcat/webapps/CS550Files/";
	public static ResponseBuilder getOutput(Process proc, String outputFileName) throws IOException {
		ResponseBuilder response = null;

		// is any error occurred
		String errorLine = null;
		String outputError = null;
		BufferedReader error = new BufferedReader(new InputStreamReader(proc.getErrorStream()));
		while ((errorLine = error.readLine()) != null) {
			outputError += errorLine + "\n";
		}
		if (outputError != null) {
			response = Response.ok((Object) outputError);
			return response;
		}
		BufferedReader br = new BufferedReader(new InputStreamReader(proc.getInputStream()));
		// create file to output
		File file = new File(SERVER_UPLOAD_LOCATION_FOLDER + outputFileName);
		FileOutputStream is = new FileOutputStream(file);
		OutputStreamWriter osw = new OutputStreamWriter(is);
		Writer w = new BufferedWriter(osw);
		// go over each line
		String line = br.readLine();
		if (line != null) {
			w.write(line + "\n");
		}
		while (line != null) {
			line = br.readLine();
			if (line != null) {
				w.write(line + "\n");
			}
		}
		w.close();
		response = Response.ok((Object) file);
		response.header("Content-Disposition", "attachment; filename=\"" + outputFileName + "\"");
		return response;
	}
}
