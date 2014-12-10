package org.gridchem.service.filetransfer;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;

import org.apache.log4j.Logger;

public class FileTransferService {
	public static Logger log = Logger.getLogger(FileTransferService.class
			.getName());

	public FileTransferService() {

	}

	public String requestDownloadFile(String userName, String accessType,
			String host, String path) {
		log.debug("User " + userName + " with " + accessType + "access");

		String saveToPath = "/tmp/" + path.substring(path.indexOf(userName));

		File saveToFile = new File(saveToPath);
		new File(saveToFile.getParent()).mkdirs();

		if (saveToFile.exists()) {
			saveToFile.delete();
			log.info(saveToPath + "exists and is deleted");
		}

		URL cgiURL = null;
		String cgiLocation = "http://gridchem-mw.ncsa.illinois.edu:8080/get_remote_file.cgi";

		URLConnection connex;

		try {
			log.info("CGI location: " + cgiLocation);
			cgiURL = new URL(cgiLocation);

			log.info("Attemp to retrieve " + path + " from host " + host);

			connex = cgiURL.openConnection();
			log.info("Connected to file transfer server at "
					+ cgiURL.toString());

			connex.setDoOutput(true);

			PrintWriter outStream = new PrintWriter(connex.getOutputStream());
			log.info("Established output stream to job submission server");

			outStream.println("Username="
					+ URLEncoder.encode(userName, "UTF-8"));
			outStream.println("AccessType="
					+ URLEncoder.encode(accessType, "UTF-8"));
			outStream.println("host=" + URLEncoder.encode(host, "UTF-8"));
			outStream.println("filePath=" + URLEncoder.encode(path, "UTF-8"));
			outStream.println("saveToPath="
					+ URLEncoder.encode(saveToPath, "UTF-8"));
			outStream.flush();
			outStream.close();

			log.info("Remoted file fetched to " + saveToPath);

			BufferedReader in = new BufferedReader(new InputStreamReader(
					connex.getInputStream()));

			String line;
			while ((line = in.readLine()) != null) {
				if (line.contains("successfully")) {
					log.info("File retrieved successfully");
					break;
				}
			}

		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		return saveToPath;
	}

	public String requestUploadFile(String userName, String experimentName,
			String jobName, String fileName) {

		String uploadPath = "/tmp/" + userName + File.separator
				+ experimentName + File.separator + jobName + File.separator
				+ fileName;

		return uploadPath;
	}
}
