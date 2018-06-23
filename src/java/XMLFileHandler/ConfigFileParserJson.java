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
	ArrayList<String> xpaths = null;
	
	ConfigFileParserJson(String fileName){
		this.fileName = fileName;
	}
	
	private void parseFile() throws Exception {
		
		FileInputStream fs = new FileInputStream(fileName);
		JSONObject obj = new JSONObject(fs);
		entityList = new JSONArray(obj.getJSONArray("result"));
		
		xpaths = new ArrayList<String>();
		
		for(Object entity: entityList) {
			JSONObject e = (JSONObject) entity;
			//System.out.println(e.getString("path"));
			xpaths.add(e.getString("path"));
			
		}
		
		fs.close();
	}
	
	public ArrayList<String> getXpathList() throws Exception {
		
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
	
}
