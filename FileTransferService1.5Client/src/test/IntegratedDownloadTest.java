package test;

import java.rmi.RemoteException;

import org.gridchem.service.socket.FileDownloadThread;

public class IntegratedDownloadTest {
	public static void main(String[] args) throws RemoteException,
			InterruptedException {

		FileDownloadThread thread = new FileDownloadThread(
				"spamidig",
				"COMMUNITY",
				"ember.ncsa.illinois.edu",
				"/scratch/users/ccguser/spamidig/spamidig_proj/ddscat_test.ember.ncsa.illinois.edu.272520.120709/200M_file",
				"/Users/fanye/200M_file");
		thread.start();

		synchronized (thread) {
			thread.wait();
		}

		return;
	}
}
