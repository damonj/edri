package com.asiainfo.aiedri.rest;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Cookie;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.NewCookie;
import javax.ws.rs.core.Response;
import java.util.Iterator;
import java.util.Map;

public class TupleClient {
	//private static String serverUri = "http://10.1.249.198:27788/NetXpert/v1/tuple";  
	private static String serverUri = "http://localhost:8080/NetXpert/v1/tuple";  
	public static void main(String[] args) {
//		addRules();
//		deleteRules();
		//auth();
		whiteList();
		//allRules();
	}
    public static void auth(){
    	System.setProperty("javax.net.ssl.trustStore", "C:/Program Files (x86)/Java/jre7/lib/security/cacerts");
        System.setProperty("javax.net.ssl.trustStorePassword", "changeit");
    	Client client = ClientBuilder.newClient();  
        WebTarget target = client.target("https://10.1.249.198:27788/NetXpert/v1/authentication"); 
        String someJsonString=createUserJsonStr();;
        Entity<String> someEntity = Entity.entity(someJsonString, MediaType.APPLICATION_JSON);
        Response response = target.request().buildPost(someEntity).invoke();  
        String obj = response.readEntity(String.class); 
        Map<String, NewCookie> cookies=response.getCookies();
        if(cookies.size()>0){
       		Iterator<String> iterator = cookies.keySet().iterator();  
               while(iterator.hasNext()){  
                   String headName = iterator.next();  
                   System.out.println(headName + ":" + cookies.get(headName) + "[\\r\\n]");  
               } 
       	}
        System.out.println(obj);
        response.close(); 
    }

	public static void allRules(){
		Client client = ClientBuilder.newClient();
		WebTarget target = client.target(serverUri + "/all");
		Entity<String> someEntity = Entity.entity("", MediaType.APPLICATION_JSON);
		Cookie cook=new NewCookie("userName", "ainx","/","1",1,"",3600,null,false,false);
		Response response = target.request().cookie(cook).buildPost(someEntity).invoke();
		String obj = response.readEntity(String.class);
		System.out.println(obj);
		response.close();
	}

	public static void whiteList(){
		Client client = ClientBuilder.newClient();
		WebTarget target = client.target(serverUri + "/whitelist");
		Entity<String> someEntity = Entity.entity("", MediaType.APPLICATION_JSON);
		Cookie cook=new NewCookie("userName", "ainx","/","1",1,"",3600,null,false,false);
		Response response = target.request().cookie(cook).buildPost(someEntity).invoke();
		String obj = response.readEntity(String.class);
		System.out.println(obj);
		response.close();
	}
    
	public static void addRules(){
    	Client client = ClientBuilder.newClient();  
        WebTarget target = client.target(serverUri + "/add.json"); 
        String someJsonString=createJsonStr();;
        Entity<String> someEntity = Entity.entity(someJsonString, MediaType.APPLICATION_JSON);
        Cookie cook=new NewCookie("userName-liulx", "sfwew123456","/","1",1,"",3600,null,false,false);
        Response response = target.request().cookie(cook).buildPost(someEntity).invoke();  
        String obj = response.readEntity(String.class); 
        System.out.println(obj);
        response.close(); 
    }
    public static void deleteRules(){
    	Client client = ClientBuilder.newClient();  
        WebTarget target = client.target(serverUri + "/delete.json"); 
        String someJsonString=createDeleteJsonStr();;
        Entity<String> someEntity = Entity.entity(someJsonString, MediaType.APPLICATION_JSON);
        Response response = target.request().cookie("userName-liulx", "123456667").buildPost(someEntity).invoke();  
        String obj = response.readEntity(String.class); 
        System.out.println(obj);
        response.close(); 
    }
    
    private static String createDeleteJsonStr() { 
    	JSONObject rule1 = new JSONObject(); 
    	rule1.put("rule_id", "111111");
    	JSONArray array=new JSONArray();
    	array.add(rule1);
    	JSONObject obj=new JSONObject();
    	obj.put("request", array);
    	String str=obj.toString();
    	//System.out.println(str);
    	return str;
	}
	public static String createJsonStr(){
    	JSONObject node1 = new JSONObject(); 
    	JSONObject node2 = new JSONObject(); 
    	node1.put("sip", "1.2.3.4");
    	node1.put("dip", "123.25.32.4");
    	node2.put("sip", "1.2.3.4");
    	node2.put("dip", "123.25.32.4");
    	node2.put("sport", 35678);
    	JSONObject rule1 = new JSONObject(); 
    	JSONObject rule2 = new JSONObject(); 
    	rule1.put("valid_after", "1480043027");
    	rule1.put("valid_before", "1579843027");
    	rule1.put("valid_scope", "1");
    	rule1.put("rule_id", "111111");
    	rule1.put("rule_type", "sip+dip");
    	rule1.put("rule", node1);
    	rule1.put("uplink", 1);
    	rule1.put("downlink", 1);
    	rule2.put("valid_after", "1480043027");
    	rule2.put("valid_before", "1579843027");
    	rule2.put("valid_scope", "1");
    	rule2.put("rule_id", "111112");
    	rule2.put("rule_type", "sip+dip+sport");
    	rule2.put("rule", node2);
    	rule2.put("uplink", 1);
    	rule2.put("downlink", 1);
    	JSONArray array=new JSONArray();
    	array.add(rule1);
    	array.add(rule2);
    	JSONObject obj=new JSONObject();
    	obj.put("request", array);
    	String str=obj.toString();
    	//System.out.println(str);
    	return str;
    }
	
	private static String createUserJsonStr() {
		JSONObject obj=new JSONObject();
		obj.put("username", "liulixia");
		obj.put("password", "20060711");
		return obj.toString();
	}
}
