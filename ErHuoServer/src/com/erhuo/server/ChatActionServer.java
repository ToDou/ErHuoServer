package com.erhuo.server;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.Connection;
import java.sql.ResultSet;
import java.util.Date;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Vector;

import com.erhuo.tools.ServerConfig;

//µÇÂ¼·þÎñÆ÷
public class ChatActionServer extends Thread {
	private Socket socket = null;

	public ChatActionServer(Socket socket) {
		this.socket = socket;
	}

	public static HashMap<String, ObjectOutputStream> UserPool = new HashMap();

	public void run() {
		try {
			ObjectInputStream oin = new ObjectInputStream(socket
					.getInputStream());
			ObjectOutputStream oout = new ObjectOutputStream(socket
					.getOutputStream());
			System.out.println("LBLBLB");
			while (true) {
				MyMessage m = (MyMessage) oin.readObject();
				System.out.println("AAA"+m.getValue().get("userid").toString());
				UserPool.put(m.getValue().get("userid").toString(), oout);
			}

		} catch (Exception e) {
			try {
				socket.close();
			} catch (Exception e1) {
			}
		}

	}

	private static ServerSocket server = null;
	private static boolean b = true;

	public static void openServer() throws Exception {
		try {
			server = new ServerSocket(Integer.parseInt(ServerConfig
					.getValue("chat_server_port")));
			while (b) {
				new ChatActionServer(server.accept()).start();
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
