package test;

import java.rmi.RemoteException;

import org.apache.axis2.AxisFault;
import org.gridchem.service.filetransfer.FileTransferServiceStub;
import org.gridchem.service.filetransfer.FileTransferServiceStub.RequestDownloadFile;
import org.gridchem.service.filetransfer.FileTransferServiceStub.RequestDownloadFileResponse;

public class MainTest {
	
	public static void main(String [] args) throws RemoteException {
		FileTransferServiceStub stub = new FileTransferServiceStub("http://ccg-mw2.ncsa.uiuc.edu:8080/axis2/services/FileTransferService");
		
		RequestDownloadFile requestDownload = new RequestDownloadFile();
		
		requestDownload.setUserName("x_baya");
		requestDownload.setAccessType("COMMUNITY");
		requestDownload.setHost("ember.ncsa.illinois.edu");
		requestDownload
				.setPath("/scratch/users/ccguser/x_baya/x_baya_proj/test_job.ember.ncsa.illinois.edu.267993.120531/test_job.out");
		
		RequestDownloadFileResponse reponse = stub.requestDownloadFile(requestDownload);
		
		System.out.println(reponse.get_return());
	}
}
