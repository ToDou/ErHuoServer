package com.erhuo.server;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Date;

import com.erhuo.tools.ServerConfig;

public class FileServer extends Thread {
	private Socket socket = null;

	public FileServer(Socket socket) {
		this.socket = socket;
	}

	public void run() {

		try {
			InputStream in = socket.getInputStream();
			OutputStream out = socket.getOutputStream();
			File outputFile = null;
			byte[] b = new byte[1000];
			in.read(b);
			// download,[image,amr],xixihaha.jpg
			// upload,[image,amr],50
			String cmd = new String(b).trim();
			if (cmd.startsWith("download")) {
				String[] ss = cmd.split(",");
				String filename = ss[2];
				if (ss[1].equalsIgnoreCase("image")) {
					outputFile = new File(ServerConfig
							.getValue("output_image_path"), filename);

				} 
				FileInputStream fin = new FileInputStream(outputFile);
				out.write((outputFile.length() + "").getBytes());
				out.flush();
				in.read(b);
				int len = 0;
				byte[] b2 = new byte[1024];
				while ((len = fin.read(b2)) != -1) {
					out.write(b2, 0, len);
					out.flush();
				}
				 
				fin.close();
			} else if (cmd.startsWith("upload")) {
				// upload,[image,amr],50
				String[] ss = cmd.split(",");
				long length = Long.parseLong(ss[2]);
				String filename = "";
				if (ss[1].equalsIgnoreCase("image")) {

					filename = new Date().getTime() + "R"
							+ (int) (Math.random() * 1000) + "R"
							+ (int) (Math.random() * 1000) + ".jpg";

					outputFile = new File(ServerConfig
							.getValue("output_image_path"), filename);

				}
				out.write(("ok," + filename).getBytes());
				out.flush();
				FileOutputStream outs = new FileOutputStream(outputFile);
				int len = 0;
				long size = 0;
				byte[] b2 = new byte[1024];
				while ((len = in.read(b2)) != -1) {
					if (size >= length) {
						break;
					}
					outs.write(b2, 0, len);
					size += len;
				}
				outs.flush();
				outs.close();

			}

		} catch (Exception e) {
			e.printStackTrace();

		} finally {

			try {
				socket.close();
			} catch (IOException e) {

			}
		}

	}

	static ServerSocket server = null;
	static boolean b = true;

	public static void openServer() throws Exception {
		try {
			server = new ServerSocket(Integer.parseInt(ServerConfig
					.getValue("file_server_port")));
			while (b) {
				new FileServer(server.accept()).start();
			}
		} catch (Exception ex) {
			ex.printStackTrace();
			try {
				server.close();
			} catch (Exception e) {
			}
			throw ex;
		}
	}

	public static void closeServer() throws Exception {
		try {
			b = false;
			server.close();
		} catch (Exception ex) {
			throw ex;
		}
	}

}
