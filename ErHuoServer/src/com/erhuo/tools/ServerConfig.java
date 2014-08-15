package com.erhuo.tools;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;

//属性文件操作类
public class ServerConfig {

	private static Properties p = new Properties();
	static {
		try {
			p.load(new FileInputStream("./server.ini"));
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.out.println("服务器加载配置文件出错！");
		}
	}

	public static String getValue(String key) {
		return p.getProperty(key);
	}
}
