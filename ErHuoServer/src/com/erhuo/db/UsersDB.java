package com.erhuo.db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Vector;

public class UsersDB {
	private Connection conn = null;

	public UsersDB(Connection conn) {
		this.conn = conn;
	}

	public void add(Users obj) throws Exception {
		PreparedStatement st = conn
				.prepareStatement("insert into users(userid,userpassword,username,sex,address,grade) values(?,?,?,?,?,?)");
		
		st.setString(1, obj.getUserid());
		st.setString(2, obj.getPassword());
		st.setString(3, obj.getUserName());
		st.setString(4, obj.getSex());
		st.setString(5, obj.getAddress());
		st.setString(6, obj.getGrade());
		if (st.executeUpdate() <= 0) {
			throw new Exception();
		}
	}

	public void set(Users obj) throws Exception {
		PreparedStatement st = conn
				.prepareStatement("update users set userpassword,username,sex,address,grade where userid=?");
		st.setString(2, obj.getPassword());
		st.setString(3, obj.getUserName());
		st.setString(4, obj.getSex());
		st.setString(5, obj.getAddress());
		st.setString(6, obj.getGrade());
		if (st.executeUpdate() <= 0) {
			throw new Exception();
		}
	}

	public void delete(int obj) throws Exception {
		PreparedStatement st = conn
				.prepareStatement("delete from users where userid=?");
		st.setObject(1, obj);

		if (st.executeUpdate() <= 0) {
			throw new Exception();
		}
	}

	public Vector<Users> findKey(String key) throws Exception {
		return findColumnName("userid", key);
	}

	public static final String USERID = "userid";
	public static final String PASSWORD = "userpassword";
	public static final String USERNAME = "username";
	public static final String SEX = "sex";
	public static final String AGE = "age";
	//修改昵称
	public void setName(Users obj) throws Exception {
		PreparedStatement st = conn
				.prepareStatement("update users set username=? where userid=?");
		st.setString(1, obj.getUserName());
		st.setString(2, obj.getUserid());
		if (st.executeUpdate() <= 0) {
			throw new Exception();
		}
	}
	//修改密码
	public void setPassword(Users obj) throws Exception {
		PreparedStatement st = conn
				.prepareStatement("update users set userpassword=? where userid=?");
		st.setString(1, obj.getPassword());
		st.setString(2, obj.getUserid());
		if (st.executeUpdate() <= 0) {
			throw new Exception();
		}
	}
	//修改性别
	public void setSex(Users obj) throws Exception {
		PreparedStatement st = conn
				.prepareStatement("update users set sex=? where userid=?");
		st.setString(1, obj.getSex());
		st.setString(2, obj.getUserid());
		if (st.executeUpdate() <= 0) {
			throw new Exception();
		}
	}
	//修改地址 
	public void setAddress(Users obj) throws Exception {
		PreparedStatement st = conn
				.prepareStatement("update users set address=? where userid=?");
		st.setString(1, obj.getAddress());
		st.setString(2, obj.getUserid());
		if (st.executeUpdate() <= 0) {
			throw new Exception();
		}
	}
	//修改年级
		public void setGrade(Users obj) throws Exception {
			PreparedStatement st = conn
					.prepareStatement("update users set grade=? where userid=?");
			st.setString(1, obj.getGrade());
			st.setString(2, obj.getUserid());
			if (st.executeUpdate() <= 0) {
				throw new Exception();
			}
		}
	//
	public Vector<Users> finduser(String cname, Object value)
			throws Exception {
		PreparedStatement pst = conn
				.prepareStatement("SELECT * FROM users WHERE " + cname + "=?");
		pst.setObject(1, value);
		ResultSet re = pst.executeQuery();
		Vector<Users> list = new Vector<Users>();
		while (re.next()) {
			Users obj1 = new Users();
			obj1.setUserid(re.getString("userid"));
			obj1.setPassword(re.getString("userpassword"));
			obj1.setUserName(re.getString("username"));
			obj1.setSex(re.getString("sex"));
			obj1.setAddress(re.getString("address"));
			obj1.setGrade(re.getString("grade"));
			list.add(obj1);
		}
		return list;
	}

	public Vector<Users> findColumnName(String cname, Object value)
			throws Exception {
		PreparedStatement pst = conn
				.prepareStatement("SELECT * FROM users WHERE " + cname + "=?");
		pst.setObject(1, value);
		ResultSet re = pst.executeQuery();
		Vector<Users> list = new Vector<Users>();
		while (re.next()) {
			Users obj1 = new Users();
			obj1.setUserid(re.getString("userid"));
			obj1.setPassword(re.getString("userpassword"));
			obj1.setUserName(re.getString("username"));
			obj1.setSex(re.getString("sex"));
			obj1.setAddress(re.getString("address"));
			obj1.setGrade(re.getString("grade"));
			list.add(obj1);
		}
		return list;
	}

	public Vector<Users> findAll() throws Exception {
		PreparedStatement pst = conn.prepareStatement("SELECT * FROM users");
		ResultSet re = pst.executeQuery();
		Vector<Users> list = new Vector<Users>();
		while (re.next()) {
			Users obj1 = new Users();
			obj1.setUserid(re.getString("userid"));
			obj1.setPassword(re.getString("userpassword"));
			obj1.setUserName(re.getString("username"));
			obj1.setSex(re.getString("sex"));
			obj1.setAddress(re.getString("address"));
			obj1.setGrade(re.getString("grade"));
			
			list.add(obj1);
		}
		return list;
	}
	
	public boolean ishaveusers(String userid)
			throws Exception {
		boolean b = false;
		PreparedStatement st = conn
				.prepareStatement("select userid from users where userid=?");
		st.setObject(1, userid);
		ResultSet rsResultSet = st.executeQuery();
		while (rsResultSet.next()) {
			b = true;
		}
		return b;
	}


}