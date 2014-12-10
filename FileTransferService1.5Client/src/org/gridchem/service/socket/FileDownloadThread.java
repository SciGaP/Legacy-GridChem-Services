package org.gridchem.service.socket;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.rmi.RemoteException;

import org.apache.axis2.AxisFault;
import org.gridchem.service.filetransfer.FileTransferServiceStub;
import org.gridchem.service.filetransfer.FileTransferServiceStub.RequestDownloadFile;
import org.gridchem.service.filetransfer.FileTransferServiceStub.RequestDownloadFileResponse;

import com.asprise.util.ui.progress.ProgressDialog;

public class FileDownloadThread extends Thread{
	private String userName;
	private String accessType;
	private String hostName;
	private String path;
	private String localPath;

	private Socket socket = null;
	private int port = 8821;

	public static int bufferSize = 2048;

	public FileDownloadThread() {
		;
	}
	
	public FileDownloadThread(int bufferSize) {
		FileDownloadThread.bufferSize = bufferSize;
	}

	public FileDownloadThread(String userName, String accessType,
			String hostName, String path, String localPath) {
		this.userName = userName;
		this.accessType = accessType;
		this.hostName = hostName;
		this.path = path;
		this.localPath = localPath;
	}

	public String read(BufferedInputStream bis) {
		byte[] contents = new byte[bufferSize];

		int bytesRead = 0;
		String string = null;
		try {
			bytesRead = bis.read(contents);
			string = new String(contents, 0, bytesRead);
		} catch (IOException e) {
			e.printStackTrace();
		}

		return string;
	}

	public void run() {
		synchronized (this) {
			try {
				FileTransferServiceStub stub = new FileTransferServiceStub(
						"http://gridchem-mw.ncsa.illinois.edu:8080/axis2/services/FileTransferService");
				RequestDownloadFile requestDownload = new RequestDownloadFile();
				requestDownload.setUserName(this.userName);
				requestDownload.setAccessType(this.accessType);
				requestDownload.setHost(this.hostName);
				requestDownload.setPath(this.path);

				ProgressDialog progressDialog = new ProgressDialog(null,
						"Download Progress");
				progressDialog.millisToPopup = 0;
				progressDialog.millisToDecideToPopup = 50;
				progressDialog.displayTimeLeft = false;
				progressDialog.showAnimation = true;
				progressDialog.beginTask("Preparing file", 100, true);

				Thread.sleep(500);
				progressDialog.worked(0);

				RequestDownloadFileResponse response = stub
						.requestDownloadFile(requestDownload);

				Thread.sleep(500);
				String filePath = new String(response.get_return());
				System.out.println(filePath);

				String filePathList[] = filePath.split("/");
				String fileName = filePathList[filePathList.length - 1];

				socket = new Socket("gridchem-mw.ncsa.illinois.edu", port);

				BufferedInputStream bufferedSocketInputStream = new BufferedInputStream(
						socket.getInputStream(), bufferSize);

				BufferedOutputStream bufferedSocketOutputStream = new BufferedOutputStream(
						socket.getOutputStream(), bufferSize);

				bufferedSocketOutputStream.write("correct token".getBytes());
				bufferedSocketOutputStream.flush();

				String msg = null;
				msg = read(bufferedSocketInputStream);
				System.out.println(msg);
				if (!msg.equals("authenticated")) {
					return;
				}
				System.out.println("Successfully authenticated");

				bufferedSocketOutputStream.write("download".getBytes());
				bufferedSocketOutputStream.flush();

				msg = read(bufferedSocketInputStream);
				System.out.println(msg);

				bufferedSocketOutputStream.write(new String(filePath)
						.getBytes());
				bufferedSocketOutputStream.flush();

				msg = read(bufferedSocketInputStream);
				System.out.println(msg);

				long size = Long.parseLong(msg);
				System.out.println("File size is: " + size);

				msg = read(bufferedSocketInputStream);
				System.out.println(msg);

				if (!msg.equals("Start sending data stream")) {
					return;
				}

				//String savePath = "/Users/fanye/" + fileName;
				String savePath = localPath;
				
				File saveFile = new File(savePath);
				if (!saveFile.getParentFile().exists()) {
		            saveFile.getParentFile().mkdirs();
		        }

				progressDialog.setTaskName("Fetching " + fileName);

				long onePercentChunk = size / bufferSize / 50;
				// progressDialog.beginTask("Fetching " + fileName,
				// (int) size / 2048, true);

				byte[] buf = new byte[bufferSize];

				BufferedOutputStream bufferedFileOutputStream = new BufferedOutputStream(
						new FileOutputStream(savePath), bufferSize);

				// System.out.println("File length: " + len + "\n");
				System.out.println("Receiving file: " + fileName + "\n");

				long startTime = System.currentTimeMillis();

				long flag = 0;
				while (true) {
					int read = 0;
					if (bufferedSocketInputStream != null) {
						read = bufferedSocketInputStream.read(buf);
					}

					if (read == -1) {
						break;
					}

					bufferedFileOutputStream.write(buf, 0, read);
					bufferedFileOutputStream.flush();

					flag++;
					if (flag == onePercentChunk) {
						progressDialog.worked(2);
						flag = 0;
					}
				}

				long endTime = System.currentTimeMillis();

				System.out.println("File saved at " + savePath + "\n");
				System.out
						.println("Total elapsed milliseconds in downloading is :"
								+ (endTime - startTime));

				bufferedFileOutputStream.close();
				bufferedSocketInputStream.close();
				bufferedSocketOutputStream.close();

				progressDialog.finished();
				/*
				 * while ((msg = br.readLine()) != null)
				 * System.out.println(msg);
				 */
			} catch (AxisFault e) {
				e.printStackTrace();
			} catch (RemoteException e) {
				e.printStackTrace();
			} catch (InterruptedException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			} finally {
				System.out.println("Closing socket");
				try {
					if (socket != null)
						socket.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
				notify();
			}
		}
	}
}
