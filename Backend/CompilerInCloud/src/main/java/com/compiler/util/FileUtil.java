package com.compiler.util;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.attribute.PosixFilePermission;
import java.util.HashSet;
import java.util.Set;

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

	private static int chmod(String filename, int mode) {
	    try {
	        Class<?> fspClass = Class.forName("java.util.prefs.FileSystemPreferences");
	        Method chmodMethod = fspClass.getDeclaredMethod("chmod", String.class, Integer.TYPE);
	        chmodMethod.setAccessible(true);
	        return (Integer)chmodMethod.invoke(null, filename, mode);
	    } catch (Throwable ex) {
	        return -1;
	    }
	}
	
	public static void setPermission(File file) throws IOException{
	    Set<PosixFilePermission> perms = new HashSet<PosixFilePermission>();
	    perms.add(PosixFilePermission.OWNER_READ);
	    perms.add(PosixFilePermission.OWNER_WRITE);
	    perms.add(PosixFilePermission.OWNER_EXECUTE);

	    perms.add(PosixFilePermission.OTHERS_READ);
	    perms.add(PosixFilePermission.OTHERS_WRITE);
	    perms.add(PosixFilePermission.OTHERS_EXECUTE);

	    perms.add(PosixFilePermission.GROUP_READ);
	    perms.add(PosixFilePermission.GROUP_WRITE);
	    perms.add(PosixFilePermission.GROUP_EXECUTE);

	    Files.setPosixFilePermissions(file.toPath(), perms);
	}
}
