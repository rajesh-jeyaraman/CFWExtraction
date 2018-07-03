package XMLFileHandler;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;

import org.apache.wink.json4j.JSONArray;
import org.apache.wink.json4j.JSONObject;

public class XmlFileExtratorConfig {

	private String fileName = null;
	JSONArray entityList = null; 
	String configFolderPath = null;
	String dataFolderPath = null;
	String outputFolderPath = null;
	String filePathSeperator = null;
	String responseFolderPath = null;
	String sipOutputFolderPath = null;
	String holding = null;
	String appName = null;
	String producer = null;
	String sipentity = null;
	String schema = null;
	String processTriggerFileName = null;
	String datafileNameIndicator = null;

	XmlFileExtratorConfig(String fileName){
		this.fileName = fileName;
		
	}
	
	public boolean parseFile() throws Exception {
		
		File f = new File(fileName);
		if(f.exists() == false) {
			System.out.println("Unable to read base config json file" + fileName);
			return false;
		}
		
		FileInputStream fs = new FileInputStream(fileName);
		JSONObject obj = new JSONObject(fs);
		entityList = new JSONArray(obj.getJSONArray("config"));
				
		for(Object entity: entityList) {
			JSONObject e = (JSONObject) entity;
			configFolderPath = e.getString("configFolderPath");
			dataFolderPath = e.getString("dataFolderPath");
			outputFolderPath = e.getString("outputFolderPath");
			filePathSeperator = e.getString("filePathSeperator");
			responseFolderPath = e.getString("responseFolderPath");
			sipOutputFolderPath = e.getString("sipOutputFolderPath");
			processTriggerFileName = e.getString("processTriggerFileName");
			datafileNameIndicator = e.getString("datafileNameIndicator");
			holding = e.getString("holding");
			appName = e.getString("appName");
			producer = e.getString("producer");
			sipentity = e.getString("entity");
			schema = e.getString("schema");
			
			if(outputFolderPath == null) {
				outputFolderPath = dataFolderPath + "output" + File.separator;
			}
			
			if(processTriggerFileName == null) {
				processTriggerFileName = "Notification.xml";
			}
			
			createOutDir(outputFolderPath);
			createDirIfNotExists(sipOutputFolderPath);
			createDirIfNotExists(responseFolderPath);
		}
		
		fs.close();
		return validateInput();
	}
	private void createOutDir(String dir) {
		File od = new File(dir);
		
		if(!od.exists()) {
			if(od.mkdir()) {
				System.out.println(dir + " created");
			}
		}
		else {
			//Workaround in case outputFolderPath contains any folders or files already -- In windows observed folder and files not getting deleted after sip creation. 
			ArrayList<String> dirList = new ArrayList<String>();
			
			File outputFolder = new File(dir);
			File[] files = outputFolder.listFiles();
			for(File file: files) {
				if(file.isDirectory() == true) {
					dirList.add(dir + file.getName() + filePathSeperator);
				}
			}
			if(dirList.size() >0) {
				outputFolderPath = dir + "_" + String.valueOf(dirList.size()) + File.separator;
				File nod = new File(outputFolderPath);
				if(!nod.exists()) {
					if(nod.mkdir()) {
						System.out.println(outputFolderPath + " created");
					}
				}
			}
		}
		
	}
	private void createDirIfNotExists(String dir) throws Exception{
		File od = new File(dir);
		
		if(!od.exists()) {
			if(od.mkdir()) {
				System.out.println(dir + " created");
			}
		}	
	}
	
	private boolean validateInput() {
		if(configFolderPath != null) {
			File f = new File(configFolderPath);
			if(f.exists() == false) {
				System.out.println("Config Folder Path does not exists:" + configFolderPath);
				return false;
			}
		}
		if(dataFolderPath != null) {
			File f = new File(dataFolderPath);
			if(f.exists() == false) {
				System.out.println("Config Folder Path does not exists:" + dataFolderPath);
				return false;
			}
		}
		return true;
	}
		
	public String getSchema() {
		return schema;
	}

	public void setSchema(String schema) {
		this.schema = schema;
	}

	public ArrayList<String> getDataDirectoryList(){
		ArrayList<String> dataDirectories = new ArrayList<String>();
		
		// Goto Data folder and check for success notification. 
		File dataFolder = new File(dataFolderPath);
		File[] files = dataFolder.listFiles();
		for(File file: files) {
			if(file.isDirectory() == true) {
				dataDirectories.add(dataFolderPath + file.getName() + filePathSeperator);
			}
		}
		return dataDirectories;
	}
	
	public ArrayList<String> getFileList(String directory){
		ArrayList<String> fileList = new ArrayList<String>();
		
		File dataFolder = new File(directory);
		File[] files = dataFolder.listFiles();
		for(File file: files) {
			if(file.isFile() == true) {
				fileList.add(directory + file.getName());						
			}
		}
		return fileList;
	}
	
	public ArrayList<String> getFileNameList(String directory){
		ArrayList<String> fileList = new ArrayList<String>();
		
		File dataFolder = new File(directory);
		File[] files = dataFolder.listFiles();
		for(File file: files) {
			if(file.isFile() == true) {
				fileList.add( file.getName());
			}
		}
		return fileList;
	}
	
	public ArrayList<String> getConfigFileList(){
		ArrayList<String> fileList = new ArrayList<String>();
		
		File dataFolder = new File(configFolderPath);
		File[] files = dataFolder.listFiles();
		for(File file: files) {
			if(file.isFile() == true) {
				if(file.getName().toLowerCase().contains(".xml") ||
					file.getName().toLowerCase().contains(".json")
					) {
					fileList.add(configFolderPath + file.getName());
				}
				
			}
		}
		return fileList;
	}
	
	public ArrayList<String> getDataFileList(){
		ArrayList<String> fileList = new ArrayList<String>();
		
		File dataFolder = new File(dataFolderPath);
		File[] files = dataFolder.listFiles();
		for(File file: files) {
			if(file.isFile() == true) {
				if(file.getName().toLowerCase().contains(".xml") == true){
					fileList.add(dataFolderPath + file.getName());
				}
				
			}
		}
		return fileList;
	}
	
	public boolean isDataFile(String fileName) {
		boolean status = false;
		
		if(datafileNameIndicator == null) {
			status = true; // Since no identifier is configured, consider all the files are potential data file for extraction. 
		}
		if(fileName.toLowerCase().contains(datafileNameIndicator)) {  //TODO: Support for multiple indicator type. For now only one string is configured. _case.xml
			status = true;
		}
		return status;		
	}
	
	public String getSipOutputFolderPath() {
		return sipOutputFolderPath;
	}

	public void setSipOutputFolderPath(String sipOutputFolderPath) {
		this.sipOutputFolderPath = sipOutputFolderPath;
	}

	public String getResponseFolderPath() {
		return responseFolderPath;
	}

	public void setResponseFolderPath(String responseFolderPath) {
		this.responseFolderPath = responseFolderPath;
	}
	
	public String getFilePathSeperator() {
		return filePathSeperator;
	}

	public void setFilePathSeperator(String filePathSeperator) {
		this.filePathSeperator = filePathSeperator;
	}

	public String getConfigFolderPath() {
		return configFolderPath;
	}

	public void setConfigFolderPath(String configFolderPath) {
		this.configFolderPath = configFolderPath;
	}

	public String getDataFolderPath() {
		return dataFolderPath;
	}

	public void setDataFolderPath(String dataFolderPath) {
		this.dataFolderPath = dataFolderPath;
	}

	public String getOutputFolderPath() {
		return outputFolderPath;
	}

	public void setOutputFolderPath(String outputFolderPath) {
		this.outputFolderPath = outputFolderPath;
	}
	
	public String getProcessTriggerFileName() {
		return processTriggerFileName;
	}

	public void setProcessTriggerFileName(String processTriggerFileName) {
		this.processTriggerFileName = processTriggerFileName;
	}

	public String getDatafileNameIndicator() {
		return datafileNameIndicator;
	}

	public void setDatafileNameIndicator(String datafileNameIndicator) {
		this.datafileNameIndicator = datafileNameIndicator;
	}

	public String getSipentity() {
		return sipentity;
	}

	public void setSipentity(String sipentity) {
		this.sipentity = sipentity;
	}

	public String getHolding() {
		return holding;
	}

	public void setHolding(String holding) {
		this.holding = holding;
	}

	public String getAppName() {
		return appName;
	}

	public void setAppName(String appName) {
		this.appName = appName;
	}

	public String getProducer() {
		return producer;
	}

	public void setProducer(String producer) {
		this.producer = producer;
	}


}
