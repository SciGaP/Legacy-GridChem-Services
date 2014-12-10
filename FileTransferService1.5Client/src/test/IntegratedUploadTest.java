package test;

import java.rmi.RemoteException;

import org.gridchem.service.socket.FileUploadThread;

public class IntegratedUploadTest {
	public static void main(String[] args) throws RemoteException,
			InterruptedException {

		FileUploadThread thread = new FileUploadThread("x_baya", "x_baya_proj", "uploadTest", "200M_file", "/Users/fanye/200M_file");
		
		thread.start();

		synchronized (thread) {
			thread.wait();
		}

		return;
	}
}
