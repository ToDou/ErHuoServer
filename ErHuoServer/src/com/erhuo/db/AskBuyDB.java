package com.erhuo.db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Vector;

public class AskBuyDB {
	
	private Connection conn = null;

	public AskBuyDB(Connection conn) {
		this.conn = conn;
	}
	//添加物品信息
	public void add(AskBuy obj) throws Exception {
		PreparedStatement st = conn
				.prepareStatement("insert into askbuy(userid,agoodsname,agoodsclass,askbuycontent,askbuyphone,userqiuname) values(?,?,?,?,?,?)");

		st.setString(1, obj.getUserid());
		st.setString(2, obj.getAgoodsname());
		st.setString(3, obj.getAgoodsclass());
		st.setString(4, obj.getAskbuycontent());
		st.setString(5, obj.getAskbuyphone());
		st.setString(6, obj.getUserqiuname());
		if (st.executeUpdate() <= 0) {
			throw new Exception();
		}
	}
	//删除物品
	public void delete(int obj) throws Exception {
		PreparedStatement st = conn
				.prepareStatement("delete from askbuy where askbuyid=?");
		st.setObject(1, obj);

		if (st.executeUpdate() <= 0) {
			throw new Exception();
		}
	}

	/*public Vector<Users> findKey(String key) throws Exception {
		return findColumnName("userid", key);
	}*/
	
	public static final String USERID = "userid";
	public static final String AGOODSNAME = "agoodsname";
	public static final String AGOODSCLASS = "agoodsclass";
	public static final String ASKBUYCONTENT = "askbuycontent";
	public static final String ASKBUYTIME = "askbuytime";
	public static final String ASKBUYPHONE = "askbuyphone";
	public static final String ASKBUYID = "askbuyid";
	public static final String USERQIUNAME = "userqiuname";
	
		public void ChangeAskBuy(String gname, Object value,int askbuyid) throws Exception{
			PreparedStatement st = conn
					.prepareStatement("update askbuy set "+gname+"=? where askbuyid=?");
			st.setObject(1, value);
			st.setInt(2, askbuyid);
			if (st.executeUpdate() <= 0) {
				throw new Exception();
			}
		}
	
	//
	public Vector<AskBuy> findgoods(String gname, Object value)
			throws Exception {
		PreparedStatement pst = conn
				.prepareStatement("SELECT * FROM askbuy WHERE " + gname + "=?");
		pst.setObject(1, value);
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
	
	public boolean ishaveaskbuy(String agoodsname, String askbuycontent,String userid)
			throws Exception {
		boolean b = false;
		PreparedStatement st = conn
				.prepareStatement("select askbuyid from askbuy where agoodsname=? and askbuycontent=? and userid=?");
		st.setObject(1, agoodsname);
		st.setObject(2, askbuycontent);
		st.setObject(3, userid);
		ResultSet rsResultSet = st.executeQuery();
		while (rsResultSet.next()) {
			b = true;
		}
		return b;
	}

}