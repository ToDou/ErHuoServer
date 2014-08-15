package com.erhuo.db;

public class Sysdiagrams{

private  String  name;
public void setName(String name){
	this.name=name;
}
public String getName(){
	return name;
}

private  int  principal_id;
public void setPrincipal_id(int principal_id){
	this.principal_id=principal_id;
}
public int getPrincipal_id(){
	return principal_id;
}

private  int  diagram_id;
public void setDiagram_id(int diagram_id){
	this.diagram_id=diagram_id;
}
public int getDiagram_id(){
	return diagram_id;
}

private  int  version;
public void setVersion(int version){
	this.version=version;
}
public int getVersion(){
	return version;
}

private  String  definition;
public void setDefinition(String definition){
	this.definition=definition;
}
public String getDefinition(){
	return definition;
}

}