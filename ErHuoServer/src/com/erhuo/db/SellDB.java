package com.erhuo.db;

import java.security.interfaces.RSAKey;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Vector;

public class SellDB {

	private Connection conn = null;

	public SellDB(Connection conn) {
		this.conn = conn;
	}

	// 添加物品信息
	public void add(Sell obj) throws Exception {
		PreparedStatement st = conn
				.prepareStatement("insert into sell(goodsname,goodsclass,goodsprice,goodsinfo,goodsphone,userid,usersellname,gkey,goodsphoto1,goodsphoto2,goodsphoto3) values(?,?,?,?,?,?,?,?,?,?,?)");

		st.setString(1, obj.getGoodsname());
		st.setString(2, obj.getGoodsclass());
		st.setString(3, obj.getGoodsprice());
		st.setString(4, obj.getGoodsinfo());
		st.setString(5, obj.getGoodsphone());
		st.setString(6, obj.getUserid());
		st.setString(7, obj.getUsersellname());
		st.setString(8, obj.getGkey());
		st.setString(9, obj.getGoodsphoto1());
		st.setString(10, obj.getGoodsphoto2());
		st.setString(11, obj.getGoodsphoto3());
		if (st.executeUpdate() <= 0) {
			throw new Exception();
		}
	}

	// 删除物品
	public void delete(int obj) throws Exception {
		PreparedStatement st = conn
				.prepareStatement("delete from sell where goodsid=?");
		st.setObject(1, obj);

		if (st.executeUpdate() <= 0) {
			throw new Exception();
		}
	}

	/*
	 * public Vector<Users> findKey(String key) throws Exception { return
	 * findColumnName("userid", key); }
	 */

	public static final String GOODSID = "goodsid";
	public static final String GOODSNAME = "goodsname";
	public static final String GOODSCLASS = "goodsclass";
	public static final String GOODSPRICE = "goodsprice";
	public static final String GOODSINFO = "goodsinfo";
	public static final String GOODSPHONE = "goodsphone";
	public static final String USERSELLNAME = "usersellname";
	public static final String GKEY = "gkey";
	public static final String GOODSPHOTO1 = "goodsphoto1";
	public static final String GOODSPHOTO2 = "goodsphoto2";
	public static final String GOODSPHOTO3 = "goodsphoto3";

	// 修改商品
	public void ChangeSell(String gname, Object value, int goodsid)
			throws Exception {
		PreparedStatement st = conn.prepareStatement("update sell set " + gname
				+ "=? where goodsid=?");
		st.setObject(1, value);
		st.setInt(2, goodsid);
		if (st.executeUpdate() <= 0) {
			throw new Exception();
		}
	}

	//
	public Vector<Sell> findgoods(String gname, Object value) throws Exception {
		PreparedStatement pst = conn
				.prepareStatement("SELECT * FROM sell WHERE " + gname + "=?");
		pst.setObject(1, value);
		ResultSet re = pst.executeQuery();
		Vector<Sell> list = new Vector<Sell>();
		while (re.next()) {
			Sell obj1 = new Sell();
			obj1.setGoodsid(re.getInt("goodsid"));
			obj1.setGoodsname(re.getString("goodsname"));
			obj1.setGoodsclass(re.getString("goodsclass"));
			obj1.setGoodsprice(re.getString("goodsprice"));
			obj1.setGoodsinfo(re.getString("goodsinfo"));
			obj1.setGoodsphone(re.getString("goodsphone"));
			obj1.setUserid(re.getString("userid"));
			obj1.setUsersellname(re.getString("usersellname"));
			obj1.setGkey(re.getString("gkey"));
			obj1.setGoodsphoto1(re.getString("goodsphoto1"));
			obj1.setGoodsphoto2(re.getString("goodsphoto2"));
			obj1.setGoodsphoto3(re.getString("goodsphoto3"));
			list.add(obj1);
		}
		return list;
	}

	public Vector<Sell> findAll() throws Exception {
		PreparedStatement pst = conn.prepareStatement("SELECT * FROM sell;");
		ResultSet re = pst.executeQuery();
		Vector<Sell> list = new Vector<Sell>();
		while (re.next()) {
			Sell obj1 = new Sell();
			obj1.setGoodsid(re.getInt("goodsid"));
			obj1.setGoodsname(re.getString("goodsname"));
			obj1.setGoodsclass(re.getString("goodsclass"));
			obj1.setGoodsprice(re.getString("goodsprice"));
			obj1.setGoodsinfo(re.getString("goodsinfo"));
			obj1.setGoodsphone(re.getString("goodsphone"));
			obj1.setUserid(re.getString("userid"));
			obj1.setUsersellname(re.getString("usersellname"));
			obj1.setGkey(re.getString("gkey"));
			obj1.setGoodsphoto1(re.getString("goodsphoto1"));
			obj1.setGoodsphoto2(re.getString("goodsphoto2"));
			obj1.setGoodsphoto3(re.getString("goodsphoto3"));
			list.add(obj1);
		}
		return list;
	}

	public Vector<Sell> findimgname() throws Exception {
		PreparedStatement pst = conn
				.prepareStatement("SELECT goodsphoto1 FROM sell");
		ResultSet re = pst.executeQuery();
		Vector<Sell> list = new Vector<Sell>();
		while (re.next()) {
			Sell obj = new Sell();
			obj.setGoodsphoto1(re.getString("goodsphoto1"));
			list.add(obj);
		}
		return list;
	}

	public boolean ishavegoods(String goodsname, String photo1)
			throws Exception {
		boolean b = false;
		PreparedStatement st = conn
				.prepareStatement("select goodsid from sell where goodsname=? and goodsphoto1=?");
		st.setObject(1, goodsname);
		st.setObject(2, photo1);
		ResultSet rsResultSet = st.executeQuery();
		while (rsResultSet.next()) {
			b = true;
		}
		return b;
	}
	
	public String goodsowner(int goodsid)
			throws Exception {
		String userString=null;
		PreparedStatement st = conn
				.prepareStatement("select userid from sell where goodsid=? ");
		st.setObject(1, goodsid);
		ResultSet rsResultSet = st.executeQuery();
		while (rsResultSet.next()) {
			userString = rsResultSet.getString("userid");
		}
		return userString;
	}

}