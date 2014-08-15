package com.erhuo.db;

public class Pinglun {

	private String userid;
	private int goodsid;
	private String leavewordtime;
	private String  leavewordc;
	public void set_leavewordc(String leavewordc){
		this.leavewordc=leavewordc;
	}
	public String get_leavewordc(){
		return leavewordc;
	}
	public String getUserid() {
		return userid;
	}
	public void setUserid(String userid) {
		this.userid = userid;
	}
	public int getGoodsid() {
		return goodsid;
	}
	public void setGoodsid(int goodsid) {
		this.goodsid = goodsid;
	}
	public String get_leavewordtime() {
		return leavewordtime;
	}
	public void set_leavewordtime(String leavewordtime) {
		this.leavewordtime = leavewordtime;
	}

}