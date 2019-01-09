package com.compiler.rest;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.Scanner;

import javax.ws.rs.FormParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;

import org.apache.commons.io.FileUtils;

import com.compiler.constants.Info.ErrorMessage;
import com.compiler.util.FileUtil;

@Path("/preprocessors")
public class PreprocessorService {
	private static final String INPUT_FILE = "preprocessortest.c";
	private static final String OUTPUT_FILE = "system.xml";

	@POST
	@Path("/getXmlFromC/")
	public Response getAssemblyFromC(@FormParam("cCode") String cCode) {
		ResponseBuilder response = null;
		try {
			File testCFile = new File(FileUtil.SERVER_UPLOAD_LOCATION_FOLDER + INPUT_FILE);
			FileUtils.writeStringToFile(testCFile, cCode);
			Scanner scanner = new Scanner(testCFile);
			File output = new File(FileUtil.SERVER_UPLOAD_LOCATION_FOLDER + OUTPUT_FILE);
			FileOutputStream is = new FileOutputStream(output);
			OutputStreamWriter osw = new OutputStreamWriter(is);
			Writer w = new BufferedWriter(osw);
			w.write(XML_VERSION_TAG + NEW_LINE);
			w.write(SYSTEM_START_TAG + NEW_LINE);
			int counter = 0;
			while (scanner.hasNext()) {
				String nextLine = scanner.nextLine();
				String[] strs = nextLine.split("\\s+");
				if (nextLine.startsWith(DEFINE_PREFIX)) {
					String value = strs[strs.length - 1];
					counter++;
					if (counter == 1) {
						w.write(TAB + PERIPHERAL_START_TAG + ONE_SPACE + NAME + value + ONE_SPACE);
					} else if (counter == 2) {
						w.write(TYPE + value + END_TAG + NEW_LINE);
					} else if (counter == 3) {
						w.write(FOUR_SPACE + MEM_ADDR_LOW_START_TAG + value + MEM_ADDR_LOW_END_TAG + NEW_LINE);
					} else if (counter == 4) {
						w.write(FOUR_SPACE + MEM_START_HIGH_START_TAG + value + MEM_START_HIGH_END_TAG + NEW_LINE);
						w.write(TAB + PERIPHERAL_END_TAG + NEW_LINE);
					}
					if (counter == PERIPHERAL_PROPERTY_LENGTH) {
						counter = 0;
					}
				}
			}
			w.write(SYSTEM_END_TAG);
			w.close();
			scanner.close();
			response = Response.ok((Object) output);
			response.header("Content-Disposition", "attachment; filename=\"" + OUTPUT_FILE + "\"");
		} catch (Exception ex) {
			response = Response.ok((Object) ErrorMessage.NOT_OK.getMessage());
		}
		return response.build();
	}

	private static final String ONE_SPACE = " ";
	private static final String TAB = "   ";
	private static final String FOUR_SPACE = "    ";
	private static final String NEW_LINE = "\n";
	private static final String END_TAG = ">";

	private static final int PERIPHERAL_PROPERTY_LENGTH = 4;
	private static final String DEFINE_PREFIX = "#define";
	private static final String XML_VERSION_TAG = "<?xml version=\"1.0\"?>";
	private static final String SYSTEM_START_TAG = "<system>";
	private static final String SYSTEM_END_TAG = "</system>";
	private static final String NAME = "name=";
	private static final String TYPE = "type=";
	private static final String PERIPHERAL_START_TAG = "<peripheral";
	private static final String PERIPHERAL_END_TAG = "</peripheral>";
	private static final String MEM_ADDR_LOW_START_TAG = "<mem_addr_low>";
	private static final String MEM_ADDR_LOW_END_TAG = "</mem_addr_low>";
	private static final String MEM_START_HIGH_START_TAG = "<mem_addr_high>";
	private static final String MEM_START_HIGH_END_TAG = "</mem_addr_high>";
}