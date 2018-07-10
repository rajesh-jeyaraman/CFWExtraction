package XMLFileHandler;

import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.TreeSet;

import org.apache.wink.json4j.JSONArray;
import org.apache.wink.json4j.JSONException;
import org.apache.wink.json4j.JSONObject;

public class ConfigFileParserJson {

	private String fileName = null;
	JSONArray entityList = null; 
	ArrayList<NameValuePair> xpaths = null;
	
	ConfigFileParserJson(String fileName){
		this.fileName = fileName;
	}
	
	private void parseFile() throws Exception {
		
		FileInputStream fs = new FileInputStream(fileName);
		JSONObject obj = new JSONObject(fs);
		entityList = new JSONArray(obj.getJSONArray("result"));
		
		xpaths = new ArrayList<NameValuePair>();
		
		for(Object entity: entityList) {
			JSONObject e = (JSONObject) entity;
			//System.out.println(e.getString("path"));
			xpaths.add(getProperties(e));
		}
		
		fs.close();
	}
	
	public ArrayList<NameValuePair> getXpathList() throws Exception {
		
		if(entityList == null) {
			parseFile();
		}
		
		return xpaths;
	}
	
	public JSONArray getObjectList() throws Exception {
		
		if(entityList == null) {
			parseFile();
		}
		
		return entityList;
	}
	public NameValuePair getProperties(JSONObject e) throws Exception {
		NameValuePair n = new NameValuePair(e.getString("frompath"), "");
		
		n.setTopath(e.getString("topath"));
		n.setDatatype(e.getString("datatype"));
		n.setFieldName(e.getString("name"));
		n.setSearch(e.getString("search"));
		n.setResult(e.getString("result"));
		n.setId(e.getString("id"));
		return n;
	}
	
	public String getDataType(NameValuePair element) {
		String str = DATATYPE.STRING;
		
		for(NameValuePair p: xpaths) {
			String path1 = XPathUtils.removeAttr(element.getName());
			String path2 = XPathUtils.removeAttr(p.getName());
			if(path1.equalsIgnoreCase(path2)) {
				str = p.getDatatype();
				System.out.println(str);
			}
			//System.out.println(p.getName());
		}
		
		return str;
	}
	
}
