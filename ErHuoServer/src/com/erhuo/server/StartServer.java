package com.erhuo.server;

public class StartServer {

	public static void main(String[] args) {

		new Thread(){
			public void run() {
				try {
					LoginServer.openServer();
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			};
			
		}.start();
		new Thread(){
			public void run() {
				try {
					ActionServer.openServer();
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
			};
			
		}.start();
		new Thread(){
			public void run() {
				
				try {
					FileServer.openServer();
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			};
			
		}.start();
		
	}

}
