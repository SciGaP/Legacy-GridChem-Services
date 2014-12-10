package test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;

import org.apache.log4j.Logger;

public class CGITest {
	//public static Logger log = Logger.getLogger(CGITest.class.getName());
	
	public static void main(String [] args) {
		
		String path = "/gpfs2/scratch/users/ccguser/x_baya/x_baya_proj/test_job.ember.ncsa.illinois.edu.267993.120531/test_job.out";
		String host = "ember.ncsa.illinois.edu";
		
		String filePath = null;
		String saveToPath = "/tmp/GCFileTransferTest";
		URL cgiURL = null;
		String cgiLocation = "http://gridchem-mw.ncsa.illinois.edu:8080/get_remote_file.cgi";
		
		URLConnection connex;
		
		try {
			System.out.println("CGI location: " + cgiLocation);
			cgiURL = new URL(cgiLocation);

			System.out.println("Attemp to retrive " + path + " from host " + host);
			
			connex = cgiURL.openConnection();
			connex.setReadTimeout(420000);
			System.out.println("Connected to file transfer server at " + cgiURL.toString());
			
			connex.setDoOutput(true);
			
			PrintWriter outStream = new PrintWriter(connex.getOutputStream());
			System.out.println("Established output stream to job submission server");
			
			outStream.println("Username=" + URLEncoder.encode("x_baya","UTF-8"));
			outStream.println("AccessType=" + URLEncoder.encode("COMMUNITY","UTF-8"));
			outStream.println("host=" + URLEncoder.encode(host,"UTF-8"));
			outStream.println("filePath=" + URLEncoder.encode(path,"UTF-8"));
			outStream.println("saveToPath=" + URLEncoder.encode(saveToPath,"UTF-8"));
			outStream.flush();
			outStream.close();
			
			System.out.println("Remoted file fetched to " + saveToPath);
			
			BufferedReader in = new BufferedReader(new InputStreamReader(connex.getInputStream()));
			
			String aLine;
			while ((aLine = in.readLine()) != null) {
				if (aLine.contains("successfully")) {
					System.out.println("File retrieved successfully");
					break;
				}
			}
			
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
}
