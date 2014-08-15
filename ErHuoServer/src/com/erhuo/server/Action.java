package com.erhuo.server;

import java.io.ObjectOutputStream;
import java.util.HashMap;
import java.util.Set;

public class Action extends Thread {
	MyMessage m = null;
	String userid=null;

	public Action(MyMessage m,String userid) {
		this.m = m;
		this.userid=userid;
	}
	public void run() {
		System.out.println(userid+"1");
		System.out.println("lalala1");
		HashMap<String, ObjectOutputStream> map = (HashMap<String, ObjectOutputStream>) ActionServer.UserPool
				.clone();
		System.out.println("lalala2");
		Set<String> keys = map.keySet();
		System.out.println("lalala3");
		for (String string : keys) {
			try {
				System.out.println("lalala4");
				System.out.println(string+"");
				if (string.equals(userid)) {
					System.out.println(userid+"2");
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
