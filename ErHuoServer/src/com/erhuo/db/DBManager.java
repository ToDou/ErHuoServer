package com.erhuo.db;

import java.sql.Connection;
import java.sql.DriverManager;

import com.erhuo.tools.ServerConfig;

public class DBManager {

	private DBManager() {
	}

	private static DBManager db = new DBManager();

	public static DBManager getDBManager() {
		return db;

	}

	public Connection getConn() throws Exception {

		Class.forName(ServerConfig.getValue("database.driverClass"));
		return DriverManager.getConnection(ServerConfig
				.getValue("database.url"), ServerConfig
				.getValue("database.username"), ServerConfig
				.getValue("database.password"));

	}
	public static void main(String[] args) throws Exception {
		System.out.println(getDBManager().getConn());
	}
}
