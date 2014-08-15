package com.erhuo.db;

import java.security.interfaces.RSAKey;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Vector;

public class PinglunDB {

	private Connection conn = null;

	public PinglunDB(Connection conn) {
		this.conn = conn;
	}

	// 对货物进行评论
	public void add(Pinglun obj) throws Exception {
		PreparedStatement st = conn
				.prepareStatement("insert into leaveword(goodsid,userid,leavewordc) values(?,?,?)");
		st.setInt(1, obj.getGoodsid());
		st.setString(2, obj.getUserid());
		st.setString(3, obj.get_leavewordc());
		if (st.executeUpdate() <= 0) {
			throw new Exception();
		}
	}
}