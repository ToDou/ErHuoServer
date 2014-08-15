package com.erhuo.db;

import java.security.interfaces.RSAKey;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Vector;

public class CollectDB {

	private Connection conn = null;

	public CollectDB(Connection conn) {
		this.conn = conn;
	}

	// 添加物品信息
	public void add(Collect obj) throws Exception {
		PreparedStatement st = conn
				.prepareStatement("insert into collect(userid,goodsid) values(?,?)");
		st.setString(1, obj.getUserid());
		st.setInt(2, obj.getGoodsid());
		if (st.executeUpdate() <= 0) {
			throw new Exception();
		}
	}

	// 删除物品
	public void delete(int obj,String userid) throws Exception {
		PreparedStatement st = conn
				.prepareStatement("delete from collect where goodsid=? and userid=?");
		st.setObject(1, obj);
		st.setObject(2, userid);
		if (st.executeUpdate() <= 0) {
			throw new Exception();
		}
	}
	
	public void delete1(int obj) throws Exception {
		PreparedStatement st = conn
				.prepareStatement("delete from collect where goodsid=?");
		st.setObject(1, obj);
		if (st.executeUpdate() <= 0) {
			throw new Exception();
		}
	}
	
	public boolean findcollect(String userid,int goodsid) throws Exception {
		boolean b=true;
		PreparedStatement st = conn
				.prepareStatement("select * from collect where userid=? and goodsid=?");
		st.setObject(1, userid);
		st.setObject(2, goodsid);
		ResultSet re = st.executeQuery();
		while (re.next()) {
			b=false;
		}
		return b;
		}
	
	public boolean ishavegoods(int goodsid)
			throws Exception {
		boolean b = false;
		PreparedStatement st = conn
				.prepareStatement("select goodsid from collect where goodsid=?");
		st.setObject(1, goodsid);
		ResultSet rsResultSet = st.executeQuery();
		while (rsResultSet.next()) {
			b = true;
		}
		return b;
	}
}