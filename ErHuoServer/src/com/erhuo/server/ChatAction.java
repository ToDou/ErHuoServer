package com.erhuo.server;

import java.io.ObjectOutputStream;
import java.util.HashMap;
import java.util.Set;

public class ChatAction extends Thread {
	MyMessage m = null;
	String userid=null;

	public ChatAction(MyMessage m,String userid) {
		this.m = m;
		this.userid=userid;
	}
	public void run() {
		System.out.println(userid+"0");
		System.out.println("lalala10");
		HashMap<String, ObjectOutputStream> map = (HashMap<String, ObjectOutputStream>) ChatActionServer.UserPool
				.clone();
		System.out.println("lalala20");
		Set<String> keys = map.keySet();
		System.out.println("lalala30");
		for (String string : keys) {
			try {
				System.out.println("lalala40");
				System.out.println(string+"Œ“");
				if (string.equals(userid)) {
					System.out.println(userid+"0");
					ObjectOutputStream out = map.get(string);
					synchronized (out) {
						out.writeObject(m);
						out.flush();
					}
				}
			} catch (Exception e) {
			}
		}
	}

}
