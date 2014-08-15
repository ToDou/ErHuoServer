package com.erhuo.server;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Date;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Vector;

import com.erhuo.db.AskBuy;
import com.erhuo.db.AskBuyDB;
import com.erhuo.db.Collect;
import com.erhuo.db.CollectDB;
import com.erhuo.db.DBManager;
import com.erhuo.db.News;
import com.erhuo.db.NewsDB;
import com.erhuo.db.Pinglun;
import com.erhuo.db.PinglunDB;
import com.erhuo.db.Sell;
import com.erhuo.db.SellDB;
import com.erhuo.db.Users;
import com.erhuo.db.UsersDB;
import com.erhuo.tools.ServerConfig;

//登录服务器
public class LoginServer extends Thread {
	String goodsname = null;

	private Socket socket = null;

	public LoginServer(Socket socket) {
		this.socket = socket;
	}

	private static HashMap<String, ObjectOutputStream> UserPool = new HashMap();
	// private static Vector<OutputStream> pool=new Vector();

	// 子线程 服务客户端请求
	String userid = null;

	public void run() {
		try {

			// 登录

			while (true) {
				ObjectInputStream oin = new ObjectInputStream(
						socket.getInputStream());
				ObjectOutputStream oout = new ObjectOutputStream(
						socket.getOutputStream());
				MyMessage m = (MyMessage) oin.readObject();
				System.out.println("1");
				if (m.getType().equalsIgnoreCase(MyMessage.LOGIN)) {

					String userid = m.getValue().get("userid").toString();

					String password = m.getValue().get("password").toString();

					Connection conn = DBManager.getDBManager().getConn();
					UsersDB db = new UsersDB(conn);
					Vector<Users> v = db.findKey(userid);
					MyMessage m1 = new MyMessage();
					if (v.size() >= 1) {

						if (v.get(0).getPassword().equalsIgnoreCase(password)) {
							Hashtable<String, Object> table = new Hashtable<String, Object>();
							table.put("message", "ok");
							m1.setReturnValue(table);
							this.userid = userid;

							// 抢线
							ObjectOutputStream oout1 = UserPool.get(userid);
							if (oout1 != null) {
								try {
									oout1.close();
									UserPool.remove(userid);
								} catch (Exception ex) {
								}
							}
							UserPool.put(userid, oout);
						} else {

							Hashtable<String, Object> table = new Hashtable<String, Object>();
							table.put("message", "psswordError");
							m1.setReturnValue(table);
						}

					} else {
						Hashtable<String, Object> table = new Hashtable<String, Object>();
						table.put("message", "notUser");
						m1.setReturnValue(table);
					}
					oout.writeObject(m1);
					oout.flush();
					conn.close();

					if (!m1.getReturnValue().get("message").equals("ok")) {
						throw new Exception();
					}

				} else if (m.getType().equalsIgnoreCase(MyMessage.LOGOUT)) {
					UserPool.remove(userid);
					throw new Exception();
				} else if (m.getType().equalsIgnoreCase(MyMessage.REG)) {// 注册
					Connection conn = DBManager.getDBManager().getConn();
					MyMessage m1 = new MyMessage();
					try {

						conn.setAutoCommit(false);
						UsersDB db = new UsersDB(conn);
						String useridString = m.getValue().get("userid")
								.toString();
						boolean userb = false;
						userb = db.ishaveusers(useridString);
						if (userb) {
							Hashtable<String, Object> table = new Hashtable<String, Object>();
							table.put("message", "havereg");
							m1.setReturnValue(table);
						} else {
							Users u1 = new Users();
							u1.setUserid(m.getValue().get("userid").toString());
							u1.setPassword(m.getValue().get("userpassword")
									.toString());
							u1.setUserName(m.getValue().get("username")
									.toString());
							u1.setSex(m.getValue().get("sex").toString());
							u1.setAddress(m.getValue().get("address").toString());
							u1.setGrade(m.getValue().get("grade").toString());
							db.add(u1);
							conn.commit();

							Hashtable<String, Object> table = new Hashtable<String, Object>();
							table.put("message", "ok");
							table.put("userid", u1.getUserid() + "");
							m1.setReturnValue(table);
						}
					} catch (Exception e) {
						// TODO: handle exception
						conn.rollback();
						Hashtable<String, Object> table = new Hashtable<String, Object>();
						table.put("message", "error" + e.getMessage());
						m1.setReturnValue(table);
					} finally {
						conn.close();
					}
					oout.writeObject(m1);
					oout.flush();
					oout.close();
				} else if (m.getType().equalsIgnoreCase(MyMessage.USERINFO)) {// 个人信息同步
					Connection conn = DBManager.getDBManager().getConn();
					MyMessage m1 = new MyMessage();
					try {

						conn.setAutoCommit(false);
						UsersDB db = new UsersDB(conn);
						Users u1 = new Users();
						Vector<Users> v = db.findColumnName(db.USERID, m
								.getValue().get("userid").toString());
						u1 = v.get(0);
						conn.commit();

						Hashtable table = new Hashtable();
						table.put("message", "ok");
						table.put("username", u1.getUserName() + "");
						table.put("sex", u1.getSex() + "");
						table.put("address", u1.getAddress() + "");
						table.put("grade", u1.getGrade() + "");
						m1.setReturnValue(table);

					} catch (Exception e) { // TODO: handle exception
						conn.rollback();
						Hashtable table = new Hashtable();
						table.put("message", "error" + e.getMessage());
						m1.setReturnValue(table);
					} finally {
						conn.close();
					}
					oout.writeObject(m1);
					oout.close();
				} else if (m.getType().equalsIgnoreCase(MyMessage.CHANGENAME)) {// 修改昵称
					Connection conn = DBManager.getDBManager().getConn();
					MyMessage m1 = new MyMessage();
					try {

						conn.setAutoCommit(false);
						UsersDB db = new UsersDB(conn);
						Users u1 = new Users();
						u1.setUserid(m.getValue().get("userid").toString());
						u1.setUserName(m.getValue().get("username").toString());
						db.setName(u1);
						conn.commit();

						Hashtable table = new Hashtable();
						table.put("message", "ok");
						table.put("username", u1.getUserName() + "");
						m1.setReturnValue(table);

					} catch (Exception e) { // TODO: handle exception
						conn.rollback();
						Hashtable table = new Hashtable();
						table.put("message", "error" + e.getMessage());
						m1.setReturnValue(table);
					} finally {
						conn.close();
					}
					oout.writeObject(m1);
					oout.close();
				} else if (m.getType().equalsIgnoreCase(
						MyMessage.CHANGEPASSWORD)) {// 修改密码
					Connection conn = DBManager.getDBManager().getConn();
					MyMessage m1 = new MyMessage();
					try {

						conn.setAutoCommit(false);
						UsersDB db = new UsersDB(conn);
						Users u1 = new Users();
						u1.setUserid(m.getValue().get("userid").toString());
						String passwordString = m.getValue()
								.get("olduserpassword").toString();
						Vector<Users> v = db.findColumnName(db.USERID,
								u1.getUserid());
						u1 = v.get(0);
						if (!u1.getPassword().equalsIgnoreCase(passwordString)) {
							conn.commit();
							Hashtable table = new Hashtable();
							table.put("message", "passworderror");
							m1.setReturnValue(table);
						} else {
							u1.setPassword(m.getValue().get("userpassword")
									.toString());
							db.setPassword(u1);
							conn.commit();

							Hashtable table = new Hashtable();
							table.put("message", "ok");
							table.put("userpassword", u1.getPassword() + "");
							m1.setReturnValue(table);
						}
					} catch (Exception e) { // TODO: handle exception
						conn.rollback();
						Hashtable table = new Hashtable();
						table.put("message", "error");
						m1.setReturnValue(table);
					} finally {
						conn.close();
					}
					oout.writeObject(m1);
					oout.close();
				} else if (m.getType().equalsIgnoreCase(MyMessage.CHANGESEX)) {// 修改性别
					Connection conn = DBManager.getDBManager().getConn();
					MyMessage m1 = new MyMessage();
					try {

						conn.setAutoCommit(false);
						UsersDB db = new UsersDB(conn);
						Users u1 = new Users();
						u1.setUserid(m.getValue().get("userid").toString());
						u1.setSex(m.getValue().get("sex").toString());
						db.setSex(u1);
						conn.commit();

						Hashtable table = new Hashtable();
						table.put("message", "ok");
						table.put("sex", u1.getSex() + "");
						m1.setReturnValue(table);

					} catch (Exception e) { // TODO: handle exception
						conn.rollback();
						Hashtable table = new Hashtable();
						table.put("message", "error");
						m1.setReturnValue(table);
					} finally {
						conn.close();
					}
					oout.writeObject(m1);
					oout.close();
				} else if (m.getType().equalsIgnoreCase(MyMessage.CHANGEADDRESS)) {// 修改地址
					Connection conn = DBManager.getDBManager().getConn();
					MyMessage m1 = new MyMessage();
					try {

						conn.setAutoCommit(false);
						UsersDB db = new UsersDB(conn);
						Users u1 = new Users();
						u1.setUserid(m.getValue().get("userid").toString());
						u1.setAddress(m.getValue().get("address").toString());
						db.setAddress(u1);
						conn.commit();

						Hashtable table = new Hashtable();
						table.put("message", "ok");
						table.put("address", u1.getAddress() + "");
						m1.setReturnValue(table);

					} catch (Exception e) { // TODO: handle exception
						conn.rollback();
						Hashtable table = new Hashtable();
						table.put("message", "error");
						m1.setReturnValue(table);
					} finally {
						conn.close();
					}
					oout.writeObject(m1);
					oout.close();
				} 
				else if (m.getType().equalsIgnoreCase(MyMessage.CHANGEGRADE)) {// 修改年级
					Connection conn = DBManager.getDBManager().getConn();
					MyMessage m1 = new MyMessage();
					try {

						conn.setAutoCommit(false);
						UsersDB db = new UsersDB(conn);
						Users u1 = new Users();
						u1.setUserid(m.getValue().get("userid").toString());
						u1.setGrade(m.getValue().get("grade").toString());
						db.setGrade(u1);
						conn.commit();

						Hashtable table = new Hashtable();
						table.put("message", "ok");
						table.put("grade", u1.getGrade() + "");
						m1.setReturnValue(table);

					} catch (Exception e) { // TODO: handle exception
						conn.rollback();
						Hashtable table = new Hashtable();
						table.put("message", "error");
						m1.setReturnValue(table);
					} finally {
						conn.close();
					}
					oout.writeObject(m1);
					oout.close();
				}
				else if (m.getType().equalsIgnoreCase(MyMessage.ADDGOODS)) {// 添加商品
					Connection conn = DBManager.getDBManager().getConn();
					MyMessage m1 = new MyMessage();
					try {
						String useridString2 = null;
						SellDB db = new SellDB(conn);
						boolean b = db.ishavegoods(m.getValue()
								.get("goodsname").toString(),
								m.getValue().get("goodsphoto1").toString());
						String goodsclass = m.getValue().get("goodsclass")
								.toString();
						String goodsname = m.getValue().get("goodsname")
								.toString();
						String useridString = m.getValue().get("userid")
								.toString();
						if (b) {
							Hashtable table = new Hashtable();
							table.put("message", "您已经提交过该商品！");
							m1.setReturnValue(table);
						} else {
							Sell u1 = new Sell();
							u1.setGoodsclass(m.getValue().get("goodsclass")
									.toString());
							u1.setGoodsinfo(m.getValue().get("goodsinfo")
									.toString());
							u1.setGoodsname(m.getValue().get("goodsname")
									.toString());
							u1.setGoodsphone(m.getValue().get("goodsphone")
									.toString());
							u1.setGoodsprice(m.getValue().get("goodsprice")
									.toString());
							u1.setUserid(m.getValue().get("userid").toString());
							u1.setUsersellname(m.getValue().get("usersellname")
									.toString());
							u1.setGoodsphoto1(m.getValue().get("goodsphoto1")
									.toString());
							u1.setGoodsphoto2(m.getValue().get("goodsphoto2")
									.toString());
							u1.setGoodsphoto3(m.getValue().get("goodsphoto3")
									.toString());
							String key = new Date().getTime() + "R"
									+ (Math.random() * 1000) + "R"
									+ (Math.random() * 1000);

							u1.setGkey(key);
							db.add(u1);
							Vector<Sell> v = db.findgoods(db.GKEY, key);
							u1 = v.get(0);
							int goodsidString = u1.getGoodsid();

							PreparedStatement pst1 = conn
									.prepareStatement("select userid from askbuy where agoodsclass=? and agoodsname like '%"
											+ goodsname + "%'");
							pst1.setObject(1, goodsclass);
							ResultSet rs1 = pst1.executeQuery();
							boolean ishave = false;
							while (rs1.next()) {
								useridString2 = rs1.getString("userid");
								int goodsid = goodsidString;
								NewsDB db1 = new NewsDB(conn);
								if (!db1.ishavenews(useridString, goodsid)) {
									ishave = true;
									News u2 = new News();
									u2.setUserid(useridString2);
									u2.setGoodsid(goodsid);
									u2.setNews("亲！你求购的" + goodsname
											+ "已经有货了，赶紧点击查看吧！");
									db1.add(u2);
								}
							}
							Hashtable table = new Hashtable();
							table.put("message", "ok");
							table.put("goodsid", goodsidString);
							m1.setReturnValue(table);

							if (ishave) {
								MyMessage m2 = new MyMessage();
								Hashtable table1 = new Hashtable();
								table1.put("message", "ok");
								table1.put("name", goodsname);
								m2.setReturnValue(table1);
								new Action(m2, useridString2).start();
							}
						}
					} catch (Exception e) { // TODO: handle exception
						Hashtable table = new Hashtable();
						table.put("message", "error");
						m1.setReturnValue(table);
					} finally {
						conn.close();
					}
					oout.writeObject(m1);
					oout.close();
				} else if (m.getType().equalsIgnoreCase(MyMessage.ADDASKBUY)) {// 添加求购信息
					Connection conn = DBManager.getDBManager().getConn();
					MyMessage m1 = new MyMessage();
					try {
						AskBuyDB db = new AskBuyDB(conn);
						String userid = m.getValue().get("userid").toString();
						String agoodsclass = m.getValue().get("agoodsclass")
								.toString();
						String agoodsname = m.getValue().get("agoodsname")
								.toString();
						boolean b = db.ishaveaskbuy(
								m.getValue().get("agoodsname").toString(), m
										.getValue().get("askbuycontent")
										.toString(), userid);
						if (b) {
							Hashtable table = new Hashtable();
							table.put("message", "该商品您已经求购过了！");
							m1.setReturnValue(table);
						} else {
							AskBuy u1 = new AskBuy();
							u1.setUserid(m.getValue().get("userid").toString());
							u1.setAgoodsname(m.getValue().get("agoodsname")
									.toString());
							u1.setAskbuyphone(m.getValue().get("askbuyphone")
									.toString());
							u1.setAskbuycontent(m.getValue()
									.get("askbuycontent").toString());
							u1.setUserqiuname(m.getValue().get("userqiuname")
									.toString());
							u1.setAgoodsclass(m.getValue().get("agoodsclass")
									.toString());
							db.add(u1);

							PreparedStatement pst1 = conn
									.prepareStatement("select goodsid from sell where goodsclass=? and goodsname like '%"
											+ agoodsname + "%'");
							pst1.setObject(1, agoodsclass);
							ResultSet rs1 = pst1.executeQuery();
							boolean ishave = false;
							while (rs1.next()) {
								int goodsid = rs1.getInt("goodsid");
								NewsDB db1 = new NewsDB(conn);
								if (!db1.ishavenews(userid, goodsid)) {
									ishave = true;
									News u2 = new News();
									u2.setUserid(userid);
									u2.setGoodsid(goodsid);
									u2.setNews("亲！你求购的" + agoodsname
											+ "已经有货了，赶紧点击查看吧！");
									db1.add(u2);
								}
							}

							Hashtable table = new Hashtable();
							table.put("message", "ok");
							m1.setReturnValue(table);
							if (ishave) {
								MyMessage m2 = new MyMessage();
								Hashtable table1 = new Hashtable();
								table1.put("message", "ok");
								table1.put("name", agoodsname);
								m2.setReturnValue(table1);
								new Action(m2, userid).start();

							}
						}
					} catch (Exception e) { // TODO: handle exception
						Hashtable table = new Hashtable();
						table.put("message", "error" + e.getMessage());
						m1.setReturnValue(table);
					} finally {
						conn.close();
					}
					oout.writeObject(m1);
					oout.flush();
				} else if (m.getType().equalsIgnoreCase(MyMessage.DOWNQIUGOU)) {// 下载所有求购信息
					Connection conn = DBManager.getDBManager().getConn();
					MyMessage m1 = new MyMessage();
					try {

						ResultSet rs = conn.createStatement().executeQuery(
								"select top 10" + " askbuyid," + "agoodsname,"
										+ "agoodsclass," + "askbuycontent,"
										+ "askbuytime, " + "askbuyphone, "
										+ "userqiuname " + " from askbuy "
										+ " order by askbuyid desc");

						Vector<AskBuyS> ab = new Vector<AskBuyS>();

						while (rs.next()) {
							AskBuyS ask = new AskBuyS();
							ask.setAskbuyid(rs.getInt("askbuyid"));
							ask.setAgoodsname(rs.getString("agoodsname"));
							ask.setAgoodsclass(rs.getString("agoodsclass"));
							ask.setAskbuycontent(rs.getString("askbuycontent"));
							ask.setAskbuytime(rs.getString("askbuytime"));
							ask.setAskbuyphone(rs.getString("askbuyphone"));
							ask.setUserqiuname(rs.getString("userqiuname"));
							ab.add(ask);
						}
						Hashtable table = new Hashtable();
						table.put("message", "ok");
						table.put("askbuy", ab);
						m1.setReturnValue(table);
					} catch (Exception e) {
						// TODO: handle exception
						e.printStackTrace();
						Hashtable table = new Hashtable();
						table.put("message", "error");
						m1.setReturnValue(table);
					} finally {
						conn.close();
					}
					oout.writeObject(m1);
					oout.flush();
				} else if (m.getType()
						.equalsIgnoreCase(MyMessage.REFRESHASKBUY)) {// 刷新求购
					Connection conn = DBManager.getDBManager().getConn();
					MyMessage m1 = new MyMessage();
					int askbuyid = (Integer) m.getValue().get("askbuyid");
					try {

						ResultSet rs = conn.createStatement().executeQuery(
								"select" + " askbuyid," + "agoodsname,"
										+ "agoodsclass," + "askbuycontent,"
										+ "askbuytime, " + "askbuyphone, "
										+ "userqiuname " + " from askbuy "
										+ "where askbuyid>" + askbuyid
										+ " order by askbuyid desc");
						Vector<AskBuyS> ab = new Vector<AskBuyS>();

						while (rs.next()) {
							AskBuyS ask = new AskBuyS();
							ask.setAskbuyid(rs.getInt("askbuyid"));
							ask.setAgoodsname(rs.getString("agoodsname"));
							ask.setAgoodsclass(rs.getString("agoodsclass"));
							ask.setAskbuycontent(rs.getString("askbuycontent"));
							ask.setAskbuytime(rs.getString("askbuytime"));
							ask.setAskbuyphone(rs.getString("askbuyphone"));
							ask.setUserqiuname(rs.getString("userqiuname"));
							ab.add(ask);
						}
						Hashtable table = new Hashtable();
						table.put("message", "ok");
						table.put("askbuy", ab);
						System.out.println(ab.size() + "------------");
						m1.setReturnValue(table);
					} catch (Exception e) {
						// TODO: handle exception
						e.printStackTrace();
						Hashtable table = new Hashtable();
						table.put("message", "error");
						m1.setReturnValue(table);
					} finally {
						conn.close();
					}
					oout.writeObject(m1);
					oout.flush();
				} else if (m.getType().equalsIgnoreCase(MyMessage.MOREASKBUY)) {// 求购加载更多
					Connection conn = DBManager.getDBManager().getConn();
					MyMessage m1 = new MyMessage();
					int num = (Integer) m.getValue().get("num");
					int num2 = num + 10;
					try {

						ResultSet rs = conn
								.createStatement()
								.executeQuery(
										"select"
												+ " askbuyid,"
												+ "agoodsname,"
												+ "agoodsclass,"
												+ "askbuycontent,"
												+ "askbuytime, "
												+ "askbuyphone, "
												+ "userqiuname "
												+ "from (select *,row_number() over (order by askbuyid desc) as id from askbuy) a where id between "
												+ num + " and " + num2
												+ " order by id");

						Vector<AskBuyS> ab = new Vector<AskBuyS>();

						while (rs.next()) {
							AskBuyS ask = new AskBuyS();
							ask.setAskbuyid(rs.getInt("askbuyid"));
							ask.setAgoodsname(rs.getString("agoodsname"));
							ask.setAgoodsclass(rs.getString("agoodsclass"));
							ask.setAskbuycontent(rs.getString("askbuycontent"));
							ask.setAskbuytime(rs.getString("askbuytime"));
							ask.setAskbuyphone(rs.getString("askbuyphone"));
							ask.setUserqiuname(rs.getString("userqiuname"));
							ab.add(ask);
						}
						Hashtable table = new Hashtable();
						table.put("message", "ok");
						table.put("askbuy", ab);
						System.out.println(ab.size() + "------------");
						m1.setReturnValue(table);
					} catch (Exception e) {
						// TODO: handle exception
						e.printStackTrace();
						Hashtable table = new Hashtable();
						table.put("message", "error");
						m1.setReturnValue(table);
					} finally {
						conn.close();
					}
					oout.writeObject(m1);
					oout.flush();
				} else if (m.getType().equalsIgnoreCase(
						MyMessage.CHANGEASKFORINFO)) {// 修改求购下载信息
					Connection conn = DBManager.getDBManager().getConn();
					MyMessage m1 = new MyMessage();
					try {

						conn.setAutoCommit(false);
						AskBuyDB db = new AskBuyDB(conn);
						AskBuy u1 = new AskBuy();
						Vector<AskBuy> v = db.findgoods(db.ASKBUYID, m
								.getValue().get("askbuyid").toString());
						u1 = v.get(0);
						conn.commit();

						Hashtable table = new Hashtable();
						table.put("message", "ok");
						table.put("agoodsname", u1.getAgoodsname() + "");
						table.put("agoodsclass", u1.getAgoodsclass() + "");
						table.put("askbuycontent", u1.getAskbuycontent() + "");
						table.put("askbuyphone", u1.getAskbuyphone() + "");
						table.put("userqiuname", u1.getUserqiuname() + "");
						m1.setReturnValue(table);

					} catch (Exception e) { // TODO: handle exception
						conn.rollback();
						Hashtable table = new Hashtable();
						table.put("message", "error" + e.getMessage());
						m1.setReturnValue(table);
					} finally {
						conn.close();
					}
					oout.writeObject(m1);
					oout.close();
				} else if (m.getType().equalsIgnoreCase(MyMessage.CHANGEASKBUY)) {// 修改求购信息
					Connection conn = DBManager.getDBManager().getConn();
					MyMessage m1 = new MyMessage();
					try {

						conn.setAutoCommit(false);
						AskBuyDB db = new AskBuyDB(conn);
						db.ChangeAskBuy(m.getValue().get("type").toString(), m
								.getValue().get("value").toString(),
								(Integer) (m.getValue().get("askbuyid")));
						conn.commit();

						Hashtable table = new Hashtable();
						table.put("message", "ok");
						m1.setReturnValue(table);
					} catch (Exception e) { // TODO: handle exception
						conn.rollback();
						Hashtable table = new Hashtable();
						table.put("message", "error" + e.getMessage());
						m1.setReturnValue(table);
					} finally {
						conn.close();
					}
					oout.writeObject(m1);
					oout.close();
				} else if (m.getType().equalsIgnoreCase(
						MyMessage.USERQIUGOUINFO)) {// 下载用户的求购信息
					Connection conn = DBManager.getDBManager().getConn();
					MyMessage m1 = new MyMessage();
					try {
						String userid = m.getValue().get("userid").toString();
						PreparedStatement pst = conn
								.prepareStatement("select askbuyid,agoodsname,agoodsclass,askbuycontent,askbuytime,askbuyphone,userqiuname  from askbuy where userid=? order by askbuyid desc");
						pst.setObject(1, userid);
						ResultSet rs = pst.executeQuery();

						Vector<AskBuyS> ab = new Vector<AskBuyS>();

						while (rs.next()) {
							AskBuyS ask = new AskBuyS();
							ask.setAskbuyid(rs.getInt("askbuyid"));
							ask.setAgoodsname(rs.getString("agoodsname"));
							ask.setAgoodsclass(rs.getString("agoodsclass"));
							ask.setAskbuycontent(rs.getString("askbuycontent"));
							ask.setAskbuytime(rs.getString("askbuytime"));
							ask.setAskbuyphone(rs.getString("askbuyphone"));
							ask.setUserqiuname(rs.getString("userqiuname"));
							ab.add(ask);
						}
						Hashtable table = new Hashtable();
						table.put("message", "ok");
						table.put("askbuy", ab);
						System.out.println(ab.size() + "------------");
						m1.setReturnValue(table);
					} catch (Exception e) {
						// TODO: handle exception
						e.printStackTrace();
						Hashtable table = new Hashtable();
						table.put("message", "error");
						m1.setReturnValue(table);
					} finally {
						conn.close();
					}
					oout.writeObject(m1);
					oout.flush();
				} else if (m.getType().equalsIgnoreCase(MyMessage.QIUGOUDELETE)) {// 删除求购信息
					Connection conn = DBManager.getDBManager().getConn();
					MyMessage m1 = new MyMessage();
					try {
						conn.setAutoCommit(false);
						AskBuyDB db = new AskBuyDB(conn);
						int gid = (Integer) m.getValue().get("askbuyid");
						db.delete(gid);
						conn.commit();
						Hashtable table = new Hashtable();
						table.put("message", "ok");
						m1.setReturnValue(table);
					} catch (Exception e) { // TODO: handle exception
						conn.rollback();
						Hashtable table = new Hashtable();
						table.put("message", "error" + e.getMessage());
						m1.setReturnValue(table);
					} finally {
						conn.close();
					}
					oout.writeObject(m1);
					oout.close();
				} else if (m.getType().equalsIgnoreCase(MyMessage.MAINSHOW)) {// 主菜单显示
					Connection conn = DBManager.getDBManager().getConn();
					String useridString;
					MyMessage m1 = new MyMessage();
					try {

						ResultSet rs = conn.createStatement().executeQuery(
								"select top 16" + " goodsid,userid," + "goodsname,"
										+ "goodsprice," + "selltime, "
										+ "goodsphoto1 " + " from sell "
										+ " order by selltime desc");

						Vector<SellS> ab = new Vector<SellS>();

						while (rs.next()) {
							SellS ask = new SellS();
							ask.setGoodsid(rs.getInt("goodsid"));
							ask.setGoodsname(rs.getString("goodsname"));
							useridString=rs.getString("userid");
							ask.setGoodsprice(rs.getString("goodsprice"));
							ask.setSelltime(rs.getString("selltime"));
							ask.setGoodsphoto1(rs.getString("goodsphoto1"));
							PreparedStatement pst1 = conn
									.prepareStatement("select address,grade from users where userid=?");
							pst1.setObject(1, useridString);
							ResultSet rs1=pst1.executeQuery();
							while (rs1.next()) {
								ask.setGrade(rs1.getString("grade"));
								ask.setAddress(rs1.getString("address"));
							}
							
							ab.add(ask);
						}
						Hashtable table = new Hashtable();
						table.put("message", "ok");
						table.put("sell", ab);
						System.out.println(ab.size() + "------------");
						m1.setReturnValue(table);
					} catch (Exception e) {
						// TODO: handle exception
						e.printStackTrace();
						Hashtable table = new Hashtable();
						table.put("message", "error");
						m1.setReturnValue(table);
					} finally {
						conn.close();
					}
					oout.writeObject(m1);
					oout.flush();
				} else if (m.getType().equalsIgnoreCase(
						MyMessage.REFRESHMAINSHOW)) {// 刷新显示主页面
					Connection conn = DBManager.getDBManager().getConn();
					String useridString;
					MyMessage m1 = new MyMessage();
					int goodsid = (Integer) m.getValue().get("goodsid");
					try {

						ResultSet rs = conn
								.createStatement()
								.executeQuery(
										"select goodsid,userid,goodsname,goodsprice,selltime,goodsphoto1 from sell where goodsid>"
												+ goodsid
												+ " order by selltime desc");

						Vector<SellS> ab = new Vector<SellS>();

						while (rs.next()) {
							SellS ask = new SellS();
							ask.setGoodsid(rs.getInt("goodsid"));
							ask.setGoodsname(rs.getString("goodsname"));
							useridString=rs.getString("userid");
							ask.setGoodsprice(rs.getString("goodsprice"));
							ask.setSelltime(rs.getString("selltime"));
							ask.setGoodsphoto1(rs.getString("goodsphoto1"));
							PreparedStatement pst1 = conn
									.prepareStatement("select address,grade from users where userid=?");
							pst1.setObject(1, useridString);
							ResultSet rs1=pst1.executeQuery();
							while (rs1.next()) {
								ask.setGrade(rs1.getString("grade"));
								ask.setAddress(rs1.getString("address"));
							}
							
							ab.add(ask);
						}
						Hashtable table = new Hashtable();
						table.put("message", "ok");
						table.put("sell", ab);
						System.out.println(ab.size() + "------------");
						m1.setReturnValue(table);
					} catch (Exception e) {
						// TODO: handle exception
						e.printStackTrace();
						Hashtable table = new Hashtable();
						table.put("message", "error");
						m1.setReturnValue(table);
					} finally {
						conn.close();
					}
					oout.writeObject(m1);
					oout.flush();
				} else if (m.getType().equalsIgnoreCase(
						MyMessage.LoadMoreMAINSHOW)) {// 主菜单加载更多
					Connection conn = DBManager.getDBManager().getConn();
					String useridString;
					MyMessage m1 = new MyMessage();
					int num = (Integer) m.getValue().get("num");
					int num2 = num + 10;
					try {

						ResultSet rs = conn
								.createStatement()
								.executeQuery(
										"select "
												+ " goodsid,userid,"
												+ "goodsname,"
												+ "goodsprice,"
												+ "selltime, "
												+ "goodsphoto1 "
												+ "from (select *,row_number() over (order by selltime desc) as id from sell) a where id between "
												+ num + " and " + num2
												+ " order by id");

						Vector<SellS> ab = new Vector<SellS>();

						while (rs.next()) {
							
							SellS ask = new SellS();
							ask.setGoodsid(rs.getInt("goodsid"));
							ask.setGoodsname(rs.getString("goodsname"));
							useridString=rs.getString("userid");
							ask.setGoodsprice(rs.getString("goodsprice"));
							ask.setSelltime(rs.getString("selltime"));
							ask.setGoodsphoto1(rs.getString("goodsphoto1"));
							PreparedStatement pst1 = conn
									.prepareStatement("select address,grade from users where userid=?");
							pst1.setObject(1, useridString);
							ResultSet rs1=pst1.executeQuery();
							while (rs1.next()) {
								ask.setGrade(rs1.getString("grade"));
								ask.setAddress(rs1.getString("address"));
							}
							
							ab.add(ask);
						}
						Hashtable table = new Hashtable();
						table.put("message", "ok");
						table.put("sell", ab);
						System.out.println(ab.size() + "------------");
						m1.setReturnValue(table);
					} catch (Exception e) {
						// TODO: handle exception
						e.printStackTrace();
						Hashtable table = new Hashtable();
						table.put("message", "error");
						m1.setReturnValue(table);
					} finally {
						conn.close();
					}
					oout.writeObject(m1);
					oout.flush();
			} else if (m.getType().equalsIgnoreCase(MyMessage.REFRESHGOODS)) {// 主菜单刷新
					Connection conn = DBManager.getDBManager().getConn();
					String useridString;
					MyMessage m1 = new MyMessage();
					try {

						ResultSet rs = conn.createStatement().executeQuery(
								"select top 16" + " goodsid,userid," + "goodsname,"
										+ "goodsprice," + "selltime, "
										+ "goodsphoto1 " + " from sell "
										+ " order by selltime desc");

						Vector<SellS> ab = new Vector<SellS>();

						while (rs.next()) {
							SellS ask = new SellS();
							ask.setGoodsid(rs.getInt("goodsid"));
							ask.setGoodsname(rs.getString("goodsname"));
							useridString=rs.getString("userid");
							ask.setGoodsprice(rs.getString("goodsprice"));
							ask.setSelltime(rs.getString("selltime"));
							ask.setGoodsphoto1(rs.getString("goodsphoto1"));
							PreparedStatement pst1 = conn
									.prepareStatement("select address,grade from users where userid=?");
							pst1.setObject(1, useridString);
							ResultSet rs1=pst1.executeQuery();
							while (rs1.next()) {
								ask.setGrade(rs1.getString("grade"));
								ask.setAddress(rs1.getString("address"));
							}
							
							ab.add(ask);
						}
						Hashtable table = new Hashtable();
						table.put("message", "ok");
						table.put("sell", ab);
						System.out.println(ab.size() + "------------");
						m1.setReturnValue(table);
					} catch (Exception e) {
						// TODO: handle exception
						e.printStackTrace();
						Hashtable table = new Hashtable();
						table.put("message", "error");
						m1.setReturnValue(table);
					} finally {
						conn.close();
					}
					oout.writeObject(m1);
					oout.flush();
				} else if (m.getType()
						.equalsIgnoreCase(MyMessage.USERGOODSINFO)) {// 个人商品显示
					Connection conn = DBManager.getDBManager().getConn();
					MyMessage m1 = new MyMessage();
					try {
						
						String useridString;
						String userid = m.getValue().get("userid").toString();
						PreparedStatement pst = conn.prepareStatement("select"
								+ " goodsid," + "goodsname," + "goodsprice,"+"userid,"
								+ "selltime, " + "goodsphoto1 " + " from sell "
								+ "where userid=?" + " order by selltime desc");
						pst.setObject(1, userid);
						ResultSet rs = pst.executeQuery();

						Vector<SellS> ab = new Vector<SellS>();

						while (rs.next()) {
							SellS ask = new SellS();
							ask.setGoodsid(rs.getInt("goodsid"));
							ask.setGoodsname(rs.getString("goodsname"));
							useridString=rs.getString("userid");
							ask.setGoodsprice(rs.getString("goodsprice"));
							ask.setSelltime(rs.getString("selltime"));
							ask.setGoodsphoto1(rs.getString("goodsphoto1"));
							PreparedStatement pst1 = conn
									.prepareStatement("select address,grade from users where userid=?");
							pst1.setObject(1, useridString);
							ResultSet rs1=pst1.executeQuery();
							while (rs1.next()) {
								ask.setGrade(rs1.getString("grade"));
								ask.setAddress(rs1.getString("address"));
							}
							
							ab.add(ask);
						}
						Hashtable table = new Hashtable();
						table.put("message", "ok");
						table.put("sell", ab);
						System.out.println(ab.size() + "------------");
						m1.setReturnValue(table);
					} catch (Exception e) {
						// TODO: handle exception
						e.printStackTrace();
						Hashtable table = new Hashtable();
						table.put("message", "error");
						m1.setReturnValue(table);
					} finally {
						conn.close();
					}
					oout.writeObject(m1);
					oout.flush();
				} else if (m.getType().equalsIgnoreCase(
						MyMessage.USERGOODSDELETE)) {// 删除个人商品
					Connection conn = DBManager.getDBManager().getConn();
					MyMessage m1 = new MyMessage();
					try {
						conn.setAutoCommit(false);
						SellDB db = new SellDB(conn);
						int gid = (Integer) m.getValue().get("goodsid");
						String userid = m.getValue().get("userid").toString();
						db.delete(gid);
						CollectDB db2 = new CollectDB(conn);
						boolean b = db2.ishavegoods(gid);
						if (b) {
							db2.delete1(gid);
						}
						Hashtable table = new Hashtable();
						table.put("message", "ok");
						m1.setReturnValue(table);

						conn.commit();
					} catch (Exception e) { // TODO: handle exception
						conn.rollback();
						Hashtable table = new Hashtable();
						table.put("message", "error" + e.getMessage());
						m1.setReturnValue(table);
					} finally {
						conn.close();
					}
					oout.writeObject(m1);
					oout.close();
				} else if (m.getType().equalsIgnoreCase(
						MyMessage.CHANGEGOODSINFO)) {// 修改个人商品信息下载
					Connection conn = DBManager.getDBManager().getConn();
					MyMessage m1 = new MyMessage();
					try {

						conn.setAutoCommit(false);
						SellDB db = new SellDB(conn);
						Sell u1 = new Sell();
						Vector<Sell> v = db.findgoods(db.GOODSID, m.getValue()
								.get("goodsid").toString());
						u1 = v.get(0);
						conn.commit();

						Hashtable table = new Hashtable();
						table.put("message", "ok");
						table.put("goodsname", u1.getGoodsname() + "");
						table.put("goodsclass", u1.getGoodsclass() + "");
						table.put("goodsinfo", u1.getGoodsinfo() + "");
						table.put("goodsphone", u1.getGoodsphone() + "");
						table.put("usersellname", u1.getUsersellname() + "");
						table.put("goodsprice", u1.getGoodsprice() + "");
						table.put("goodsphoto1", u1.getGoodsphoto1() + "");
						table.put("goodsphoto2", u1.getGoodsphoto2() + "");
						table.put("goodsphoto3", u1.getGoodsphoto3() + "");
						m1.setReturnValue(table);

					} catch (Exception e) { // TODO: handle exception
						conn.rollback();
						Hashtable table = new Hashtable();
						table.put("message", "error" + e.getMessage());
						m1.setReturnValue(table);
					} finally {
						conn.close();
					}
					oout.writeObject(m1);
					oout.close();
				} else if (m.getType().equalsIgnoreCase(MyMessage.CHANGEGOODS)) {// 修改商品信息
					Connection conn = DBManager.getDBManager().getConn();
					MyMessage m1 = new MyMessage();
					try {

						conn.setAutoCommit(false);
						SellDB db = new SellDB(conn);
						db.ChangeSell(m.getValue().get("type").toString(), m
								.getValue().get("value").toString(),
								(Integer) (m.getValue().get("goodsid")));
						conn.commit();

						Hashtable table = new Hashtable();
						table.put("message", "ok");
						m1.setReturnValue(table);
					} catch (Exception e) { // TODO: handle exception
						conn.rollback();
						Hashtable table = new Hashtable();
						table.put("message", "error" + e.getMessage());
						m1.setReturnValue(table);
					} finally {
						conn.close();
					}
					oout.writeObject(m1);
					oout.close();
				} else if (m.getType()
						.equalsIgnoreCase(MyMessage.GOODSINFOSHOW)) {// 详细信息显示
					Connection conn = DBManager.getDBManager().getConn();
					MyMessage m1 = new MyMessage();
					try {
						int goodsid = (Integer) (m.getValue().get("goodsid"));
						PreparedStatement pst = conn
								.prepareStatement("select goodsname,userid,goodsprice,selltime,goodsinfo,goodsphoto2,goodsphoto3,usersellname,goodsphone,goodsphoto1  from sell where goodsid=?");
						
						pst.setObject(1, goodsid);
						String useridString;
						ResultSet rs = pst.executeQuery();
						Vector<SellS> ab = new Vector<SellS>();
						
						while (rs.next()) {
							SellS ask = new SellS();
							ask.setGoodsname(rs.getString("goodsname"));
							ask.setUserid(rs.getString("userid"));
							useridString=rs.getString("userid");
							ask.setGoodsprice(rs.getString("goodsprice"));
							ask.setSelltime(rs.getString("selltime"));
							ask.setGoodsphoto1(rs.getString("goodsphoto1"));
							ask.setGoodsinfo(rs.getString("goodsinfo"));
							ask.setGoodsphoto2(rs.getString("goodsphoto2"));
							ask.setUsersellname(rs.getString("usersellname"));
							ask.setGoodsphoto3(rs.getString("goodsphoto3"));
							ask.setGoodsphone(rs.getString("goodsphone"));
							PreparedStatement pst1 = conn
									.prepareStatement("select username,sex,address,grade from users where userid=?");
							pst1.setObject(1, useridString);
							ResultSet rs1=pst1.executeQuery();
							while (rs1.next()) {
								ask.setUsername(rs1.getString("username"));
								ask.setSex(rs1.getString("sex"));
								ask.setGrade(rs1.getString("grade"));
								ask.setAddress(rs1.getString("address"));
							}
							ab.add(ask);
						}
							Hashtable table = new Hashtable();
						table.put("message", "ok");
						table.put("sell", ab);
						m1.setReturnValue(table);
					} catch (Exception e) {
						// TODO: handle exception
						e.printStackTrace();
						Hashtable table = new Hashtable();
						table.put("message", "error");
						m1.setReturnValue(table);
					} finally {
						conn.close();
					}
					oout.writeObject(m1);
					oout.flush();
				} else if (m.getType().equalsIgnoreCase(MyMessage.SHOUCANG)) {// 收藏
					Connection conn = DBManager.getDBManager().getConn();
					MyMessage m1 = new MyMessage();
					
					String userid2;
					String username = null,sex = null,address = null,goodsname = null;
					try {
						Hashtable table1 = new Hashtable();
						String userid=m.getValue().get("userid").toString();
						int goodsid=(Integer)m.getValue().get("goodsid");
						CollectDB db = new CollectDB(conn);
						boolean b = db.findcollect(m.getValue().get("userid")
								.toString(),
								(Integer) (m.getValue().get("goodsid")));
						if (b) {
							Collect u1 = new Collect();
							u1.setUserid(m.getValue().get("userid").toString());
							u1.setGoodsid((Integer) (m.getValue()
									.get("goodsid")));
							

							SellDB selldb = new SellDB(conn);
							 userid2 = selldb.goodsowner((Integer) (m
									.getValue().get("goodsid")));
							if (userid2.equals(m.getValue().get("userid")
									.toString())) {
								Hashtable table = new Hashtable();
								
								table.put("message", "isown");
								m1.setReturnValue(table);
							} else {
								db.add(u1);
								Hashtable table = new Hashtable();
								table.put("message", "ok");
								table1.put("message", "ok");
								
								
								
								PreparedStatement pst1 = conn
										.prepareStatement("select username,sex,address from users where userid=?");
								pst1.setObject(1, userid);
								ResultSet rs1=pst1.executeQuery();
								while (rs1.next()) {
									table1.put("username", rs1.getString("username"));
									username=rs1.getString("username");
									
									sex=rs1.getString("sex");
									table1.put("address",rs1.getString("address"));
									address=rs1.getString("address");
								}
								
								PreparedStatement pst2 = conn
										.prepareStatement("select goodsname from sell where goodsid=?");
								pst2.setObject(1, goodsid);
								ResultSet rs2=pst2.executeQuery();
								while (rs2.next()) {
									table1.put("goodsname", rs2.getString("goodsname"));
									goodsname=rs2.getString("goodsname");
								}
								table1.put("type", "shoucang");
								String usersex="妹子";
								if (sex.equals("男")) {
									usersex="汉子";
									
								}
								table1.put("sex", usersex);
								NewsDB db1 = new NewsDB(conn);
								News u2 = new News();
								u2.setUserid(userid2);
								u2.setGoodsid(goodsid);
								u2.setNews("亲！住在"+address+"的"+usersex+username+"收藏了您的" +goodsname);
								db1.add(u2);
								System.out.println("=1");
								System.out.println(userid2);
								m1.setReturnValue(table);
								MyMessage m2 = new MyMessage();
								m2.setReturnValue(table1);
								System.out.println("=2");
								new Action(m2, userid2).start();
								System.out.println("=3");
							}
						} else {
							Hashtable table = new Hashtable();
							table.put("message", "该商品你已经收藏过了！");
							m1.setReturnValue(table);
						}
					} catch (Exception e) { // TODO: handle exception
						Hashtable table = new Hashtable();
						table.put("message", "error" + e.getMessage());
						m1.setReturnValue(table);
					} finally {
						conn.close();
					}
					oout.writeObject(m1);
					oout.close();
				} else if (m.getType().equalsIgnoreCase(MyMessage.SHOUCANGJ)) {// 收藏夹的显示
					Connection conn = DBManager.getDBManager().getConn();
					MyMessage m1 = new MyMessage();
					try {
						String useridString = m.getValue().get("userid")
								.toString();
						String userid1;
						PreparedStatement pst = conn
								.prepareStatement("select top 10 collecttime,goodsid from collect where userid=? order by collecttime desc");
						pst.setString(1, useridString);
						ResultSet rs = pst.executeQuery();
						Vector<CollectS> ab = new Vector<CollectS>();
						while (rs.next()) {
							CollectS ask = new CollectS();
							ask.setCollecttime(rs.getString("collecttime"));
							Integer goodsid = rs.getInt("goodsid");
							ask.setGoodsid(goodsid);
							PreparedStatement pst1 = conn
									.prepareStatement("select goodsphoto1,goodsprice,goodsname,userid from sell where goodsid=?");
							pst1.setObject(1, goodsid);
							ResultSet rs1 = pst1.executeQuery();
							while (rs1.next()) {
								ask.setGoodsname(rs1.getString("goodsname"));
								userid1=rs1.getString("userid");
								ask.setGoodsprice(rs1.getString("goodsprice"));
								ask.setGoodsphoto1(rs1.getString("goodsphoto1"));
								PreparedStatement pst2 = conn
										.prepareStatement("select address from users where userid=?");
								pst2.setObject(1, userid1);
								ResultSet rs2=pst2.executeQuery();
								while (rs2.next()) {
									ask.setAddress(rs2.getString("address"));
								}
								ab.add(ask);
							}
						}
						Hashtable table = new Hashtable();
						table.put("message", "ok");
						table.put("collect", ab);
						System.out.println(ab.size() + "------------");
						m1.setReturnValue(table);
					} catch (Exception e) {
						// TODO: handle exception
						e.printStackTrace();
						Hashtable table = new Hashtable();
						table.put("message", "error");
						m1.setReturnValue(table);
					} finally {
						conn.close();
					}
					oout.writeObject(m1);
					oout.flush();
				} else if (m.getType()
						.equalsIgnoreCase(MyMessage.DELETECOLLECT)) {// 删除收藏
					Connection conn = DBManager.getDBManager().getConn();
					MyMessage m1 = new MyMessage();
					System.out.println("23");
					try {
						conn.setAutoCommit(false);
						CollectDB db = new CollectDB(conn);
						int gid = (Integer) m.getValue().get("goodsid");
						String useridString = m.getValue().get("userid")
								.toString();
						System.out.println(gid + "");
						System.out.println(useridString);
						db.delete(gid, useridString);
						conn.commit();
						Hashtable table = new Hashtable();
						table.put("message", "ok");
						m1.setReturnValue(table);
					} catch (Exception e) { // TODO: handle exception
						conn.rollback();
						Hashtable table = new Hashtable();
						table.put("message", "error" + e.getMessage());
						m1.setReturnValue(table);
					} finally {
						conn.close();
					}
					oout.writeObject(m1);
					oout.close();
				} else if (m.getType().equalsIgnoreCase(MyMessage.PINGLUN)) {// 评论信息
					Connection conn = DBManager.getDBManager().getConn();
					int goodsid=(Integer)m.getValue().get("goodsid");
					String userid1=m.getValue().get("userid").toString();
					
					MyMessage m1 = new MyMessage();
					try {
						String userid2 = null,username = null,goodsname = null;
						PinglunDB db = new PinglunDB(conn);
						Pinglun u1 = new Pinglun();
						u1.setUserid(m.getValue().get("userid").toString());
						u1.setGoodsid((Integer) (m.getValue().get("goodsid")));
						u1.set_leavewordc(m.getValue().get("leavewordc")
								.toString());
						db.add(u1);
						Hashtable table = new Hashtable();
						Hashtable table1 = new Hashtable();
						table.put("message", "ok");
						table1.put("message", "ok");
						
						PreparedStatement pst1 = conn
								.prepareStatement("select goodsname,userid from sell where goodsid=?");
						pst1.setObject(1, goodsid);
						ResultSet rs1=pst1.executeQuery();
						while (rs1.next()) {
							table1.put("goodsname", rs1.getString("goodsname"));
							goodsname=rs1.getString("goodsname");
							userid2=rs1.getString("userid");
							System.out.println(userid2);
						}
						
						if (userid2!=userid1) {
							PreparedStatement pst2 = conn
									.prepareStatement("select username from users where userid=?");
							pst2.setObject(1, userid1);
							ResultSet rs2=pst2.executeQuery();
							while (rs2.next()) {
								table1.put("username", rs2.getString("username"));
								username= rs2.getString("username");
							}
							
							NewsDB db1 = new NewsDB(conn);
							News u2 = new News();
							u2.setUserid(userid2);
							u2.setGoodsid(goodsid);
							u2.setNews("亲！"+username+"在你的" +goodsname+"留了言赶紧查看吧！");
							db1.add(u2);
							
							table1.put("type", "liuyan");
							
							MyMessage m2 = new MyMessage();
							m2.setReturnValue(table1);
							new Action(m2, userid2).start();
						}
						m1.setReturnValue(table);

					} catch (Exception e) { // TODO: handle exception
						Hashtable table = new Hashtable();
						table.put("message", "error" + e.getMessage());
						m1.setReturnValue(table);
					} finally {
						conn.close();
					}
					oout.writeObject(m1);
					oout.close();
				} else if (m.getType().equalsIgnoreCase(MyMessage.PINGLUN_DOWN)) {// 更新评论信息
					Connection conn = DBManager.getDBManager().getConn();
					MyMessage m1 = new MyMessage();
					try {
						String goofsidString = m.getValue().get("goodsid")
								.toString();
						PreparedStatement pst = conn
								.prepareStatement("select leavewordtime,userid,leavewordc from leaveword where goodsid=? order by leavewordtime desc");
						pst.setString(1, goofsidString);
						ResultSet rs = pst.executeQuery();
						Vector<Pingluns> ab = new Vector<Pingluns>();
						while (rs.next()) {
							Pingluns ask = new Pingluns();
							ask.setLeavewordtime(rs.getString("leavewordtime"));
							String userid = rs.getString("userid");
							ask.setLeavewordc(rs.getString("leavewordc"));
							PreparedStatement pst1 = conn
									.prepareStatement("select username from users where userid=?");
							pst1.setObject(1, userid);
							ResultSet rs1 = pst1.executeQuery();
							while (rs1.next()) {
								ask.setUsername(rs1.getString("username"));

							}
							ab.add(ask);
						}
						Hashtable table = new Hashtable();
						table.put("message", "ok");
						//
						table.put("pinglun", ab);
						System.out.println(ab.size() + "------------");
						m1.setReturnValue(table);
					} catch (Exception e) {
						// TODO: handle exception
						e.printStackTrace();
						Hashtable table = new Hashtable();
						table.put("message", "error");
						m1.setReturnValue(table);
					} finally {
						conn.close();
					}
					oout.writeObject(m1);
					oout.flush();
				}
				else if (m.getType().equalsIgnoreCase(MyMessage.CATAGORYSEARCH)) {// 按类别搜索
					Connection conn = DBManager.getDBManager().getConn();
					MyMessage m1 = new MyMessage();
					String goodsname;
					try {
						String useridString;
						goodsname = m.getValue().get("goodsname").toString();
						String catagory = m.getValue().get("catagory")
								.toString();
						String type = m.getValue().get("type").toString();
						System.out.println(catagory);
						System.out.println(type);
						System.out.println(goodsname);
						PreparedStatement pst = conn
								.prepareStatement("select goodsid,userid,goodsname,goodsprice,selltime,goodsphoto1 from sell where goodsclass=? and goodsname like '%"
										+ goodsname
										+ "%' order by goodsprice "
										+ type + "");
						pst.setObject(1, catagory);
						ResultSet rs = pst.executeQuery();

						Vector<SellS> ab = new Vector<SellS>();

						while (rs.next()) {
							SellS ask = new SellS();
							ask.setGoodsid(rs.getInt("goodsid"));
							useridString=rs.getString("userid");
							ask.setGoodsname(rs.getString("goodsname"));
							ask.setGoodsprice(rs.getString("goodsprice"));
							ask.setSelltime(rs.getString("selltime"));
							ask.setGoodsphoto1(rs.getString("goodsphoto1"));
							
							PreparedStatement pst1 = conn
									.prepareStatement("select address,grade from users where userid=?");
							pst1.setObject(1, useridString);
							ResultSet rs1=pst1.executeQuery();
							while (rs1.next()) {
								ask.setGrade(rs1.getString("grade"));
								ask.setAddress(rs1.getString("address"));
							}
							
							ab.add(ask);
						}
						Hashtable table = new Hashtable();
						table.put("message", "ok");
						table.put("sell", ab);
						System.out.println(ab.size() + "------------");
						m1.setReturnValue(table);
					} catch (Exception e) {
						// TODO: handle exception
						e.printStackTrace();
						Hashtable table = new Hashtable();
						table.put("message", "error");
						m1.setReturnValue(table);
					} finally {
						conn.close();
					}
					oout.writeObject(m1);
					oout.flush();
				} else if (m.getType().equalsIgnoreCase(MyMessage.SEARCH)) {// 按内容搜索
					Connection conn = DBManager.getDBManager().getConn();
					MyMessage m1 = new MyMessage();
					try {
						String useridString;
						String goodsname = m.getValue().get("goodsname")
								.toString();
						String type = m.getValue().get("type").toString();
						System.out.println(type);
						System.out.println(goodsname);
						PreparedStatement pst = conn
								.prepareStatement("select goodsid,userid,goodsname,goodsprice,selltime,goodsphoto1 from sell  where goodsname like '%"
										+ goodsname
										+ "%' order by goodsprice "
										+ type + "");
						ResultSet rs = pst.executeQuery();
						Vector<SellS> ab = new Vector<SellS>();

						while (rs.next()) {
							SellS ask = new SellS();
							ask.setGoodsid(rs.getInt("goodsid"));
							useridString=rs.getString("userid");
							ask.setGoodsname(rs.getString("goodsname"));
							ask.setGoodsprice(rs.getString("goodsprice"));
							ask.setSelltime(rs.getString("selltime"));
							ask.setGoodsphoto1(rs.getString("goodsphoto1"));
							
							PreparedStatement pst1 = conn
									.prepareStatement("select address,grade from users where userid=?");
							pst1.setObject(1, useridString);
							ResultSet rs1=pst1.executeQuery();
							while (rs1.next()) {
								ask.setGrade(rs1.getString("grade"));
								ask.setAddress(rs1.getString("address"));
							}
							
							ab.add(ask);
						}
						Hashtable table = new Hashtable();
						table.put("message", "ok");
						table.put("sell", ab);
						System.out.println(ab.size() + "------------");
						m1.setReturnValue(table);
					} catch (Exception e) {
						// TODO: handle exception
						e.printStackTrace();
						Hashtable table = new Hashtable();
						table.put("message", "error");
						m1.setReturnValue(table);
					} finally {
						conn.close();
					}
					oout.writeObject(m1);
					oout.flush();
				} else if (m.getType().equalsIgnoreCase(
						MyMessage.MAIN_CATAGORYSEARCH)) {// 主页面类别选择显示
					Connection conn = DBManager.getDBManager().getConn();
					MyMessage m1 = new MyMessage();
					try {
						String catagory = m.getValue().get("catagory")
								.toString();
						String useridString;
						ResultSet rs = null;
						String[] str1 = { "文化/体育", "电子商品及其配件", "服饰", "虚拟商品",
								"房屋出租", "宠物", "车辆", "生活用品", "票务/优惠券/旅游",
								"免费赠送", "其它" };
						if (catagory.equalsIgnoreCase(str1[0])) {
							PreparedStatement pst = conn
									.prepareStatement("select goodsid,userid,goodsname,goodsprice,selltime,goodsphoto1 from sell where goodsclass in ('音乐/电影/书籍', '运动/户外活动', '文具办公', '艺术品/收藏品' ) order by goodsid desc ");

							rs = pst.executeQuery();
						} else if (catagory.equalsIgnoreCase(str1[1])) {
							PreparedStatement pst = conn
									.prepareStatement("select goodsid,userid,goodsname,goodsprice,selltime,goodsphoto1 from sell where goodsclass in ('电脑/配件','手机配件', '家用电器', '电子产品' ) order by goodsid desc ");

							rs = pst.executeQuery();
						} else if (catagory.equalsIgnoreCase(str1[2])) {
							PreparedStatement pst = conn
									.prepareStatement("select goodsid,userid,goodsname,goodsprice,selltime,goodsphoto1 from sell where goodsclass in ( '男装', '女装', '鞋包服饰' ) order by goodsid desc ");

							rs = pst.executeQuery();
						} else if (catagory.equalsIgnoreCase(str1[3])) {
							PreparedStatement pst = conn
									.prepareStatement("select goodsid,userid,goodsname,goodsprice,selltime,goodsphoto1 from sell where goodsclass in ('手机号', '点卡/账号', '游戏装备', 'QQ专区') order by goodsid desc ");

							rs = pst.executeQuery();
						} else if (catagory.equalsIgnoreCase(str1[4])) {
							PreparedStatement pst = conn
									.prepareStatement("select goodsid,userid,goodsname,goodsprice,selltime,goodsphoto1 from sell where goodsclass in ('房屋出租' ) order by goodsid desc ");

							rs = pst.executeQuery();
						} else if (catagory.equalsIgnoreCase(str1[5])) {
							PreparedStatement pst = conn
									.prepareStatement("select goodsid,userid,goodsname,goodsprice,selltime,goodsphoto1 from sell where goodsclass in ( '宠物交易', '宠物用品', '宠物领养与赠送') order by goodsid desc ");

							rs = pst.executeQuery();
						} else if (catagory.equalsIgnoreCase(str1[6])) {
							PreparedStatement pst = conn
									.prepareStatement("select goodsid,userid,goodsname,goodsprice,selltime,goodsphoto1 from sell where goodsclass in ('自行车', '电动车', '摩托车', '配件') order by goodsid desc ");

							rs = pst.executeQuery();
						} else if (catagory.equalsIgnoreCase(str1[7])) {
							PreparedStatement pst = conn
									.prepareStatement("select goodsid,userid,goodsname,goodsprice,selltime,goodsphoto1 from sell where goodsclass in ('家具', '日用品', '家居饰品', '装修建材' ) order by goodsid desc ");

							rs = pst.executeQuery();
						} else if (catagory.equalsIgnoreCase(str1[8])) {
							PreparedStatement pst = conn
									.prepareStatement("select goodsid,userid,goodsname,goodsprice,selltime,goodsphoto1 from sell where goodsclass in ( '折扣卡/优惠券', '购物卡', '车票/旅游' ) order by goodsid desc ");

							rs = pst.executeQuery();
						} else if (catagory.equalsIgnoreCase(str1[9])) {
							PreparedStatement pst = conn
									.prepareStatement("select goodsid,userid,goodsname,goodsprice,selltime,goodsphoto1 from sell where goodsclass in ('免费赠送') order by goodsid desc ");

							rs = pst.executeQuery();
						} else if (catagory.equalsIgnoreCase(str1[10])) {
							PreparedStatement pst = conn
									.prepareStatement("select goodsid,userid,goodsname,goodsprice,selltime,goodsphoto1 from sell where goodsclass in ('其他' ) order by goodsid desc ");

							rs = pst.executeQuery();
						}
						Vector<SellS> ab = new Vector<SellS>();

						while (rs.next()) {
							SellS ask = new SellS();
							ask.setGoodsid(rs.getInt("goodsid"));
							useridString=rs.getString("userid");
							ask.setGoodsname(rs.getString("goodsname"));
							ask.setGoodsprice(rs.getString("goodsprice"));
							ask.setSelltime(rs.getString("selltime"));
							ask.setGoodsphoto1(rs.getString("goodsphoto1"));
							
							PreparedStatement pst1 = conn
									.prepareStatement("select address,grade from users where userid=?");
							pst1.setObject(1, useridString);
							ResultSet rs1=pst1.executeQuery();
							while (rs1.next()) {
								ask.setGrade(rs1.getString("grade"));
								ask.setAddress(rs1.getString("address"));
							}
							
							ab.add(ask);
						}
						Hashtable table = new Hashtable();
						table.put("message", "ok");
						table.put("sell", ab);
						System.out.println(ab.size() + "------------");
						m1.setReturnValue(table);
					} catch (Exception e) {
						// TODO: handle exception
						e.printStackTrace();
						Hashtable table = new Hashtable();
						table.put("message", "error");
						m1.setReturnValue(table);
					} finally {
						conn.close();
					}
					oout.writeObject(m1);
					oout.flush();
				} else if (m.getType().equalsIgnoreCase(MyMessage.MAIN_CATAGORYPOPULARITY)) {// 主页面类别选择人气推荐显示
					Connection conn = DBManager.getDBManager().getConn();
					MyMessage m1 = new MyMessage();
					try {
						String useridString;
						ResultSet rs = null;
						PreparedStatement pst = conn
								.prepareStatement("select top 16 x.goodsid,userid,goodsname,goodsprice,selltime,goodsphoto1 from sell x order by (("
										+ "SELECT COUNT(*) as collectnum "
										+ "FROM sell,collect "
										+ "where sell.goodsid=collect.goodsid and sell.goodsid=x.goodsid)*0.7+( "
										+ "select COUNT(*) as leavenum "
										+ "FROM sell,leaveword "
										+ "where sell.goodsid=leaveword.goodsid and sell.goodsid=x.goodsid)*0.3) desc;");

						rs = pst.executeQuery();
						Vector<SellS> ab = new Vector<SellS>();

						while (rs.next()) {
							SellS ask = new SellS();
							ask.setGoodsid(rs.getInt("goodsid"));
							useridString=rs.getString("userid");
							ask.setGoodsname(rs.getString("goodsname"));
							ask.setGoodsprice(rs.getString("goodsprice"));
							ask.setSelltime(rs.getString("selltime"));
							ask.setGoodsphoto1(rs.getString("goodsphoto1"));
							
							PreparedStatement pst1 = conn
									.prepareStatement("select address,grade from users where userid=?");
							pst1.setObject(1, useridString);
							ResultSet rs1=pst1.executeQuery();
							while (rs1.next()) {
								ask.setGrade(rs1.getString("grade"));
								ask.setAddress(rs1.getString("address"));
							}
							
							ab.add(ask);
						}
						Hashtable table = new Hashtable();
						table.put("message", "ok");
						table.put("sell", ab);
						System.out.println(ab.size() + "------------");
						m1.setReturnValue(table);
					} catch (Exception e) {
						// TODO: handle exception
						e.printStackTrace();
						Hashtable table = new Hashtable();
						table.put("message", "error");
						m1.setReturnValue(table);
					} finally {
						conn.close();
					}
					oout.writeObject(m1);
					oout.flush();
				}else if (m.getType().equalsIgnoreCase(
						MyMessage.POPULARITYMORE)) {// 主菜单人气推荐加载更多
					Connection conn = DBManager.getDBManager().getConn();
					MyMessage m1 = new MyMessage();
					String useridString;
					int num = (Integer) m.getValue().get("num");
					int num2 = num + 10;
					try {

						ResultSet rs = conn
								.createStatement()
								.executeQuery(
										"select "
												+ " goodsid,userid,"
												+ "goodsname,"
												+ "goodsprice,"
												+ "selltime, "
												+ "goodsphoto1 "
												+ "from (select *,row_number() over (order by (("
										+ "SELECT COUNT(*) as collectnum "
										+ "FROM sell,collect "
										+ "where sell.goodsid=collect.goodsid and sell.goodsid=x.goodsid)*0.7+( "
										+ "select COUNT(*) as leavenum "
										+ "FROM sell,leaveword "
										+ "where sell.goodsid=leaveword.goodsid and sell.goodsid=x.goodsid)*0.3) desc) as id from sell x) a where id between "
												+ num + " and " + num2
												+ " order by id");

						Vector<SellS> ab = new Vector<SellS>();

						while (rs.next()) {
							SellS ask = new SellS();
							ask.setGoodsid(rs.getInt("goodsid"));
							useridString=rs.getString("userid");
							ask.setGoodsname(rs.getString("goodsname"));
							ask.setGoodsprice(rs.getString("goodsprice"));
							ask.setSelltime(rs.getString("selltime"));
							ask.setGoodsphoto1(rs.getString("goodsphoto1"));
							
							PreparedStatement pst1 = conn
									.prepareStatement("select address,grade from users where userid=?");
							pst1.setObject(1, useridString);
							ResultSet rs1=pst1.executeQuery();
							while (rs1.next()) {
								ask.setGrade(rs1.getString("grade"));
								ask.setAddress(rs1.getString("address"));
							}
							
							ab.add(ask);
						}
						Hashtable table = new Hashtable();
						table.put("message", "ok");
						table.put("sell", ab);
						System.out.println(ab.size() + "------------");
						m1.setReturnValue(table);
					} catch (Exception e) {
						// TODO: handle exception
						e.printStackTrace();
						Hashtable table = new Hashtable();
						table.put("message", "error");
						m1.setReturnValue(table);
					} finally {
						conn.close();
					}
					oout.writeObject(m1);
					oout.flush();
				}
				else if (m.getType().equalsIgnoreCase(
						MyMessage.MAIN_CATAGORYSEARCHREFRESH)) {// 主页面类别选择显示刷新
					Connection conn = DBManager.getDBManager().getConn();
					String useridString;
					MyMessage m1 = new MyMessage();
					try {
						String catagory = m.getValue().get("catagory")
								.toString();
						System.out.println("1" + catagory);
						int goodsid = (Integer) m.getValue().get("goodsid");
						ResultSet rs = null;
						String[] str1 = { "文化/体育", "电子商品及其配件", "服饰", "虚拟商品",
								"房屋出租", "宠物", "车辆", "生活用品", "票务/优惠券/旅游",
								"免费赠送", "其它" };
						if (catagory.equalsIgnoreCase(str1[0])) {
							PreparedStatement pst = conn
									.prepareStatement("select goodsid,userid,goodsname,goodsprice,selltime,goodsphoto1 from sell where goodsclass in ('音乐/电影/书籍', '运动/户外活动', '文具办公', '艺术品/收藏品' ) and goodsid>"
											+ goodsid
											+ " order by goodsid desc ");

							rs = pst.executeQuery();
						} else if (catagory.equalsIgnoreCase(str1[1])) {
							PreparedStatement pst = conn
									.prepareStatement("select goodsid,userid,goodsname,goodsprice,selltime,goodsphoto1 from sell where goodsclass in ('电脑/配件','手机配件', '家用电器', '电子产品' ) and goodsid>"
											+ goodsid
											+ " order by goodsid desc ");

							rs = pst.executeQuery();
						} else if (catagory.equalsIgnoreCase(str1[2])) {
							PreparedStatement pst = conn
									.prepareStatement("select goodsid,userid,goodsname,goodsprice,selltime,goodsphoto1 from sell where goodsclass in ('男装','女装','鞋包服饰') and goodsid>"
											+ goodsid
											+ " order by goodsid desc ");

							rs = pst.executeQuery();
						} else if (catagory.equalsIgnoreCase(str1[3])) {
							PreparedStatement pst = conn
									.prepareStatement("select goodsid,userid,goodsname,goodsprice,selltime,goodsphoto1 from sell where goodsclass in ('手机号', '点卡/账号', '游戏装备', 'QQ专区') and goodsid>"
											+ goodsid
											+ " order by goodsid desc ");

							rs = pst.executeQuery();
						} else if (catagory.equalsIgnoreCase(str1[4])) {
							PreparedStatement pst = conn
									.prepareStatement("select goodsid,userid,goodsname,goodsprice,selltime,goodsphoto1 from sell where goodsclass in ('房屋出租' ) and goodsid>"
											+ goodsid
											+ " order by goodsid desc ");

							rs = pst.executeQuery();
						} else if (catagory.equalsIgnoreCase(str1[5])) {
							PreparedStatement pst = conn
									.prepareStatement("select goodsid,userid,goodsname,goodsprice,selltime,goodsphoto1 from sell where goodsclass in ( '宠物交易', '宠物用品', '宠物领养与赠送') and goodsid>"
											+ goodsid
											+ " order by goodsid desc ");

							rs = pst.executeQuery();
						} else if (catagory.equalsIgnoreCase(str1[6])) {
							PreparedStatement pst = conn
									.prepareStatement("select goodsid,goodsname,goodsprice,selltime,goodsphoto1 from sell where goodsclass in ('自行车', '电动车', '摩托车', '配件') and goodsid>"
											+ goodsid
											+ " order by goodsid desc ");

							rs = pst.executeQuery();
						} else if (catagory.equalsIgnoreCase(str1[7])) {
							System.out.println(catagory + "");
							PreparedStatement pst = conn
									.prepareStatement("select goodsid,userid,goodsname,goodsprice,selltime,goodsphoto1 from sell where ('家具','日用品','家居饰品','装修建材') and goodsid>"
											+ goodsid
											+ " order by goodsid desc ");

							rs = pst.executeQuery();
						} else if (catagory.equalsIgnoreCase(str1[8])) {
							PreparedStatement pst = conn
									.prepareStatement("select goodsid,userid,goodsname,goodsprice,selltime,goodsphoto1 from sell where goodsclass in ( '折扣卡/优惠券', '购物卡', '车票/旅游' ) and goodsid>"
											+ goodsid
											+ " order by goodsid desc ");

							rs = pst.executeQuery();
						} else if (catagory.equalsIgnoreCase(str1[9])) {
							PreparedStatement pst = conn
									.prepareStatement("select goodsid,userid,goodsname,goodsprice,selltime,goodsphoto1 from sell where goodsclass in ('免费赠送') and goodsid>"
											+ goodsid
											+ " order by goodsid desc ");

							rs = pst.executeQuery();
						} else if (catagory.equalsIgnoreCase(str1[10])) {
							PreparedStatement pst = conn
									.prepareStatement("select goodsid,userid,goodsname,goodsprice,selltime,goodsphoto1 from sell where goodsclass in ('其他' ) and goodsid>"
											+ goodsid
											+ " order by goodsid desc ");

							rs = pst.executeQuery();
						}
						Vector<SellS> ab = new Vector<SellS>();

						while (rs.next()) {
							SellS ask = new SellS();
							ask.setGoodsid(rs.getInt("goodsid"));
							useridString=rs.getString("userid");
							ask.setGoodsname(rs.getString("goodsname"));
							ask.setGoodsprice(rs.getString("goodsprice"));
							ask.setSelltime(rs.getString("selltime"));
							ask.setGoodsphoto1(rs.getString("goodsphoto1"));
							
							PreparedStatement pst1 = conn
									.prepareStatement("select address,grade from users where userid=?");
							pst1.setObject(1, useridString);
							ResultSet rs1=pst1.executeQuery();
							while (rs1.next()) {
								ask.setGrade(rs1.getString("grade"));
								ask.setAddress(rs1.getString("address"));
							}
							
							ab.add(ask);
						}
						Hashtable table = new Hashtable();
						table.put("message", "ok");
						table.put("sell", ab);
						System.out.println(ab.size() + "------------");
						m1.setReturnValue(table);
					} catch (Exception e) {
						// TODO: handle exception
						e.printStackTrace();
						Hashtable table = new Hashtable();
						table.put("message", "error");
						m1.setReturnValue(table);
					} finally {
						conn.close();
					}
					oout.writeObject(m1);
					oout.flush();
				} else if (m.getType().equalsIgnoreCase(
						MyMessage.MOREMAIN_CATAGORYSEARCH)) {// 主页面类别选择显示加载更多
					Connection conn = DBManager.getDBManager().getConn();
					String useridString;
					MyMessage m1 = new MyMessage();
					try {
						String catagory = m.getValue().get("catagory")
								.toString();
						int num = (Integer) m.getValue().get("num");
						int num2 = num + 7;
						ResultSet rs = null;
						String[] str1 = { "文化/体育", "电子商品及其配件", "服饰", "虚拟商品",
								"房屋出租", "宠物", "车辆", "生活用品", "票务/优惠券/旅游",
								"免费赠送", "其它" };
						if (catagory.equalsIgnoreCase(str1[0])) {
							PreparedStatement pst = conn
									.prepareStatement("select "
											+ " goodsid,userid,"
											+ "goodsname,"
											+ "goodsprice,"
											+ "selltime, "
											+ "goodsphoto1 "
											+ "from (select *,row_number() over (order by goodsid desc) as id from sell where goodsclass in ('音乐/电影/书籍', '运动/户外活动', '文具办公', '艺术品/收藏品' )) a where id between "
											+ num + " and " + num2
											+ " order by id");

							rs = pst.executeQuery();
						} else if (catagory.equalsIgnoreCase(str1[1])) {
							PreparedStatement pst = conn
									.prepareStatement("select "
											+ " goodsid,userid,"
											+ "goodsname,"
											+ "goodsprice,"
											+ "selltime, "
											+ "goodsphoto1 "
											+ "from (select *,row_number() over (order by goodsid desc) as id from sell where goodsclass in ('电脑/配件','手机配件', '家用电器', '电子产品')) a where id between "
											+ num + " and " + num2
											+ " order by id");

							rs = pst.executeQuery();
						} else if (catagory.equalsIgnoreCase(str1[2])) {
							PreparedStatement pst = conn
									.prepareStatement("select "
											+ " goodsid,userid,"
											+ "goodsname,"
											+ "goodsprice,"
											+ "selltime, "
											+ "goodsphoto1 "
											+ "from (select *,row_number() over (order by goodsid desc) as id from sell where goodsclass in ( '男装', '女装', '鞋包服饰')) a where id between "
											+ num + " and " + num2
											+ " order by id");

							rs = pst.executeQuery();
						} else if (catagory.equalsIgnoreCase(str1[3])) {
							PreparedStatement pst = conn
									.prepareStatement("select "
											+ " goodsid,userid,"
											+ "goodsname,"
											+ "goodsprice,"
											+ "selltime, "
											+ "goodsphoto1 "
											+ "from (select *,row_number() over (order by goodsid desc) as id from sell where goodsclass in ( '手机号', '点卡/账号', '游戏装备', 'QQ专区')) a where id between "
											+ num + " and " + num2
											+ " order by id");

							rs = pst.executeQuery();
						} else if (catagory.equalsIgnoreCase(str1[4])) {
							PreparedStatement pst = conn
									.prepareStatement("select "
											+ " goodsid,userid,"
											+ "goodsname,"
											+ "goodsprice,"
											+ "selltime, "
											+ "goodsphoto1 "
											+ "from (select *,row_number() over (order by goodsid desc) as id from sell where goodsclass in ('房屋出租')) a where id between "
											+ num + " and " + num2
											+ " order by id");

							rs = pst.executeQuery();
						} else if (catagory.equalsIgnoreCase(str1[5])) {
							PreparedStatement pst = conn
									.prepareStatement("select "
											+ " goodsid,userid,"
											+ "goodsname,"
											+ "goodsprice,"
											+ "selltime, "
											+ "goodsphoto1 "
											+ "from (select *,row_number() over (order by goodsid desc) as id from sell where goodsclass in ( '宠物交易', '宠物用品', '宠物领养与赠送')) a where id between "
											+ num + " and " + num2
											+ " order by id");

							rs = pst.executeQuery();
						} else if (catagory.equalsIgnoreCase(str1[6])) {
							PreparedStatement pst = conn
									.prepareStatement("select "
											+ " goodsid,userid,"
											+ "goodsname,"
											+ "goodsprice,"
											+ "selltime, "
											+ "goodsphoto1 "
											+ "from (select *,row_number() over (order by goodsid desc) as id from sell where goodsclass in ( '自行车', '电动车', '摩托车', '配件')) a where id between "
											+ num + " and " + num2
											+ " order by id");

							rs = pst.executeQuery();
						} else if (catagory.equalsIgnoreCase(str1[7])) {
							PreparedStatement pst = conn
									.prepareStatement("select "
											+ " goodsid,userid,"
											+ "goodsname,"
											+ "goodsprice,"
											+ "selltime, "
											+ "goodsphoto1 "
											+ "from (select *,row_number() over (order by goodsid desc) as id from sell where goodsclass in ( '家具', '日用品', '家居饰品', '装修建材' )) a where id between "
											+ num + " and " + num2
											+ " order by id");
							rs = pst.executeQuery();
						} else if (catagory.equalsIgnoreCase(str1[8])) {
							PreparedStatement pst = conn
									.prepareStatement("select "
											+ " goodsid,userid,"
											+ "goodsname,"
											+ "goodsprice,"
											+ "selltime, "
											+ "goodsphoto1 "
											+ "from (select *,row_number() over (order by goodsid desc) as id from sell where goodsclass in ( '折扣卡/优惠券', '购物卡', '车票/旅游')) a where id between "
											+ num + " and " + num2
											+ " order by id");

							rs = pst.executeQuery();
						} else if (catagory.equalsIgnoreCase(str1[9])) {
							PreparedStatement pst = conn
									.prepareStatement("select "
											+ " goodsid,userid,"
											+ "goodsname,"
											+ "goodsprice,"
											+ "selltime, "
											+ "goodsphoto1 "
											+ "from (select *,row_number() over (order by goodsid desc) as id from sell where goodsclass in ( '免费赠送')) a where id between "
											+ num + " and " + num2
											+ " order by id");

							rs = pst.executeQuery();
						} else if (catagory.equalsIgnoreCase(str1[10])) {
							PreparedStatement pst = conn
									.prepareStatement("select "
											+ " goodsid,userid,"
											+ "goodsname,"
											+ "goodsprice,"
											+ "selltime, "
											+ "goodsphoto1 "
											+ "from (select *,row_number() over (order by goodsid desc) as id from sell where goodsclass in ('其他')) a where id between "
											+ num + " and " + num2
											+ " order by id");

							rs = pst.executeQuery();
						}
						Vector<SellS> ab = new Vector<SellS>();

						while (rs.next()) {
							SellS ask = new SellS();
							ask.setGoodsid(rs.getInt("goodsid"));
							useridString=rs.getString("userid");
							ask.setGoodsname(rs.getString("goodsname"));
							ask.setGoodsprice(rs.getString("goodsprice"));
							ask.setSelltime(rs.getString("selltime"));
							ask.setGoodsphoto1(rs.getString("goodsphoto1"));
							
							PreparedStatement pst1 = conn
									.prepareStatement("select address,grade from users where userid=?");
							pst1.setObject(1, useridString);
							ResultSet rs1=pst1.executeQuery();
							while (rs1.next()) {
								ask.setGrade(rs1.getString("grade"));
								ask.setAddress(rs1.getString("address"));
							}
							
							ab.add(ask);
						}
						Hashtable table = new Hashtable();
						table.put("message", "ok");
						table.put("sell", ab);
						System.out.println(ab.size() + "------------");
						m1.setReturnValue(table);
					} catch (Exception e) {
						// TODO: handle exception
						e.printStackTrace();
						Hashtable table = new Hashtable();
						table.put("message", "error");
						m1.setReturnValue(table);
					} finally {
						conn.close();
					}
					oout.writeObject(m1);
					oout.flush();
				} else if (m.getType().equalsIgnoreCase(
						MyMessage.LOADISHAVEQIUGOU)) {// 主页显示时看是否有信息
					Connection conn = DBManager.getDBManager().getConn();
					MyMessage m1 = new MyMessage();
					try {
						String userid = m.getValue().get("userid").toString();
						PreparedStatement pst = conn
								.prepareStatement("select agoodsname,agoodsclass from askbuy where userid=?");
						pst.setString(1, userid);
						boolean ishave = false;
						ResultSet rs = pst.executeQuery();
						while (rs.next()) {
							String agoodsname = rs.getString("agoodsname");
							String agoodsclass = rs.getString("agoodsclass");
							PreparedStatement pst1 = conn
									.prepareStatement("select goodsid from sell where agoodsclass=? and agoodsname like '%"
											+ agoodsname + "%'");
							pst1.setObject(1, agoodsclass);
							ResultSet rs1 = pst1.executeQuery();
							while (rs1.next()) {
								int goodsid = rs1.getInt("goodsid");
								NewsDB db = new NewsDB(conn);
								if (!db.ishavenews(userid, goodsid)) {
									ishave = true;
									conn.setAutoCommit(false);
									News u1 = new News();
									u1.setUserid(userid);
									u1.setGoodsid(goodsid);
									u1.setNews("亲！你求购" + agoodsname
											+ "的已经有货了，赶紧点击查看吧！");
									db.add(u1);
									conn.commit();
								}
							}
						}
						if (ishave) {
							Hashtable table = new Hashtable();
							table.put("message", "ok");
							m1.setReturnValue(table);
						}
					} catch (Exception e) {
						// TODO: handle exception
						conn.rollback();
						e.printStackTrace();
						Hashtable table = new Hashtable();
						table.put("message", "error");
						m1.setReturnValue(table);
					} finally {
						conn.close();
					}
					oout.writeObject(m1);
					oout.flush();
				} else if (m.getType().equalsIgnoreCase(MyMessage.DOWNNEWS)) {// 下载用户信息
					Connection conn = DBManager.getDBManager().getConn();
					MyMessage m1 = new MyMessage();
					try {
						String userid = m.getValue().get("userid").toString();
						PreparedStatement pst = conn
								.prepareStatement("select news,newstime,goodsid,state from news where userid=? order by newstime desc");
						pst.setObject(1, userid);
						ResultSet rs = pst.executeQuery();

						Vector<NewsS> ab = new Vector<NewsS>();

						while (rs.next()) {
							NewsS ask = new NewsS();
							ask.setGoodsid(rs.getInt("goodsid"));
							ask.setNews(rs.getString("news"));
							ask.setNewstime(rs.getString("newstime"));
							ask.setState(rs.getInt("state"));
							ab.add(ask);
						}
						Hashtable table = new Hashtable();
						table.put("message", "ok");
						table.put("news", ab);
						m1.setReturnValue(table);
					} catch (Exception e) {
						// TODO: handle exception
						e.printStackTrace();
						Hashtable table = new Hashtable();
						table.put("message", "error");
						m1.setReturnValue(table);
					} finally {
						conn.close();
					}
					oout.writeObject(m1);
					oout.flush();
				} else if (m.getType().equalsIgnoreCase(MyMessage.CHANGESTATE)) {// 信息阅读后修改状态
					Connection conn = DBManager.getDBManager().getConn();
					MyMessage m1 = new MyMessage();
					try {
						String userid = m.getValue().get("userid").toString();
						String goodsid = m.getValue().get("goodsid").toString();
						PreparedStatement pst = conn
								.prepareStatement("update news set state=? where userid=? and goodsid=?");
						pst.setObject(1, 1);
						pst.setObject(2, userid);
						pst.setObject(3, goodsid);
						if (pst.executeUpdate() <= 0) {
							throw new Exception();
						}
						Hashtable table = new Hashtable();
						table.put("message", "ok");
						m1.setReturnValue(table);
					} catch (Exception e) {
						// TODO: handle exception
						e.printStackTrace();
						Hashtable table = new Hashtable();
						table.put("message", "error");
						m1.setReturnValue(table);
					} finally {
						conn.close();
					}
					oout.writeObject(m1);
					oout.flush();
				} else if (m.getType().equalsIgnoreCase(MyMessage.STATENUM)) {// 刚登陆时的信息提示
					Connection conn = DBManager.getDBManager().getConn();
					MyMessage m1 = new MyMessage();
					try {
						String userid = m.getValue().get("userid").toString();

						PreparedStatement pst = conn
								.prepareStatement("select count(*) as num from news where userid=? and state=0");
						pst.setObject(1, userid);
						ResultSet rs = pst.executeQuery();
						int num = 0;
						if (rs.next()) {
							num = rs.getInt("num");
						}
						Hashtable table = new Hashtable();
						table.put("message", "ok");
						table.put("num", num);
						m1.setReturnValue(table);
					} catch (Exception e) {
						// TODO: handle exception
						e.printStackTrace();
						Hashtable table = new Hashtable();
						table.put("message", "error");
						m1.setReturnValue(table);
					} finally {
						conn.close();
					}
					oout.writeObject(m1);
					oout.flush();
				}
			}

		} catch (Exception e) {
			try {
				socket.close();
			} catch (Exception e1) {
			}
		}

	}

	private static ServerSocket server = null;
	private static boolean b = true;

	public static void openServer() throws Exception {
		try {
			server = new ServerSocket(Integer.parseInt(ServerConfig
					.getValue("server_port")));
			while (b) {
				new LoginServer(server.accept()).start();
			}

		} catch (Exception ex) {
			ex.printStackTrace();
			try {
				server.close();
			} catch (Exception e) {
			}
			throw ex;
		}
	}

	public static void closeServer() throws Exception {
		try {
			b = false;
			server.close();
		} catch (Exception ex) {
			throw ex;
		}
	}

}
