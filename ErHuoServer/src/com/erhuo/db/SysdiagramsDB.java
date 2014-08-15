package com.erhuo.db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Vector;

public class SysdiagramsDB {
private Connection conn = null;

public SysdiagramsDB(Connection conn) {
this.conn = conn;
}

 public void add(Sysdiagrams obj) throws Exception{
	 PreparedStatement st = conn.prepareStatement("insert into sysdiagrams(name,principal_id,diagram_id,version,definition) values(?,?,?,?,?)");
	st.setString(1, obj.getName());
	st.setInt(2, obj.getPrincipal_id());
	st.setInt(3, obj.getDiagram_id());
	st.setInt(4, obj.getVersion());
	st.setString(5, obj.getDefinition());
		 if(st.executeUpdate()<=0){
			    throw new Exception();
		   }
	 }
public void set(Sysdiagrams obj) throws Exception{
		PreparedStatement st = conn.prepareStatement("update sysdiagrams set  principal_id =?,diagram_id =?,version =?,definition =? where name=?");
st.setInt(1, obj.getPrincipal_id());
st.setInt(2, obj.getDiagram_id());
st.setInt(3, obj.getVersion());
st.setString(4, obj.getDefinition());
st.setString(5, obj.getName());
		if(st.executeUpdate()<=0){
			   throw new Exception();
		}
	}
public void delete(String obj) throws Exception{ 
		PreparedStatement st = conn.prepareStatement("delete from sysdiagrams where name=?");
		st.setObject(1,obj);

		if(st.executeUpdate()<=0){
			   throw new Exception();
	}
}
public Vector<Sysdiagrams> findKey(String key) throws Exception {
	return findColumnName("name",key);
}
public static final String PRINCIPAL_ID = "principal_id";
public static final String DIAGRAM_ID = "diagram_id";
public static final String VERSION = "version";
public static final String DEFINITION = "definition";
public Vector<Sysdiagrams> findColumnName(String cname, Object value)
throws Exception {
	PreparedStatement pst = conn
			.prepareStatement("SELECT * FROM sysdiagrams WHERE " + cname
+ "=?");
	pst.setObject(1, value);
	ResultSet re = pst.executeQuery();
	Vector<Sysdiagrams> list = new Vector<Sysdiagrams>();
	while (re.next()) {
		Sysdiagrams obj1 = new Sysdiagrams();
		obj1.setName(re.getString("name"));
		obj1.setPrincipal_id(re.getInt("principal_id"));
		obj1.setDiagram_id(re.getInt("diagram_id"));
		obj1.setVersion(re.getInt("version"));
		obj1.setDefinition(re.getString("definition"));
		list.add(obj1);
}
	return list;
}
public Vector<Sysdiagrams> findAll()throws Exception {
	PreparedStatement pst = conn.prepareStatement("SELECT * FROM sysdiagrams");
	ResultSet re = pst.executeQuery();
	Vector<Sysdiagrams> list = new Vector<Sysdiagrams>();
	while (re.next()) {
		Sysdiagrams obj1 = new Sysdiagrams();
		obj1.setName(re.getString("name"));
		obj1.setPrincipal_id(re.getInt("principal_id"));
		obj1.setDiagram_id(re.getInt("diagram_id"));
		obj1.setVersion(re.getInt("version"));
		obj1.setDefinition(re.getString("definition"));
		list.add(obj1);
}
	return list;
}


}