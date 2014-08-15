package com.erhuo.db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Vector;

public class NewsDB {
	
	private Connection conn = null;

	public NewsDB(Connection conn) {
		this.conn = conn;
	}
	//添加信息
	public void add(News obj) throws Exception {
		PreparedStatement st = conn
				.prepareStatement("insert into news(userid,news,goodsid) values(?,?,?)");

		st.setString(1, obj.getUserid());
		st.setString(2, obj.getNews());
		st.setInt(3, obj.getGoodsid());
		if (st.executeUpdate() <= 0) {
			throw new Exception();
		}
	}
	//删除信息
	public void delete(int obj,String userid) throws Exception {
		PreparedStatement st = conn
				.prepareStatement("delete from news where userid=? and goodsid=?");
		st.setObject(1, userid);
		st.setObject(2, obj);
		if (st.executeUpdate() <= 0) {
			throw new Exception();
		}
	}

	/*public Vector<Users> findKey(String key) throws Exception {
		return findColumnName("userid", key);
	}*/
	
	public static final String STATE = "state";
	
		public void ChangeNews(String gname, Object value,int userid) throws Exception{
			PreparedStatement st = conn
					.prepareStatement("update news set "+gname+"=? where userid=?");
			st.setObject(1, value);
			st.setInt(2, userid);
			if (st.executeUpdate() <= 0) {
				throw new Exception();
			}
		}
	
	//
	public Vector<News> findgoods(String gname, Object value)
			throws Exception {
		PreparedStatement pst = conn
				.prepareStatement("SELECT * FROM news WHERE " + gname + "=?");
		pst.setObject(1, value);
		ResultSet re = pst.executeQuery();
		Vector<News> list = new Vector<News>();
		while (re.next()) {
			News obj1 = new News();
			obj1.setUserid(re.getString("userid"));
			obj1.setNews(re.getString("news"));
			obj1.setNewstime(re.getString("newstime"));
			obj1.setGoodsid(re.getInt("goodsid"));
			obj1.setState(re.getInt("state"));
			list.add(obj1);
		}
		return list;
	}
	
	public Vector<AskBuy> findAll() throws Exception {
		PreparedStatement pst = conn.prepareStatement("SELECT * FROM askbuy");
		ResultSet re = pst.executeQuery();
		Vector<AskBuy> list = new Vector<AskBuy>();
		while (re.next()) {
			AskBuy obj1 = new AskBuy();
			obj1.setUserid(re.getString("userid"));
			obj1.setAgoodsname(re.getString("agoodsname"));
			obj1.setAgoodsclass(re.getString("agoodsclass"));
			obj1.setAskbuycontent(re.getString("askbuycontent"));
			obj1.setAskbuytime(re.getString("askbuytime"));
			obj1.setAskbuyphone(re.getString("askbuyphone"));
			obj1.setAskbuyid(re.getInt("askbuyid"));
			obj1.setUserqiuname(re.getString("userqiuname"));
			list.add(obj1);
		}
		return list;
	}
	
	public boolean ishavenews(String userid, int goodsid)
			throws Exception {
		boolean b = false;
		PreparedStatement st = conn
				.prepareStatement("select userid from news where userid=? and goodsid=?");
		st.setObject(1, userid);
		st.setObject(2, goodsid);
		ResultSet rsResultSet = st.executeQuery();
		while (rsResultSet.next()) {
			b = true;
		}
		return b;
	}

}