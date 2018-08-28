package XMLFileHandler;

public class XmlMain {

	private static XmlFileExtratorConfig config = null;

	public static void main(String[] args) {
		try {
			Processor process = new Processor();
			// Initial Validation
			if(args.length == 2){
				config = new XmlFileExtratorConfig(args[0]);
				if(config.setConfigFileName(args[0]) == false) {
					System.out.println("XMLFileExtractorConfig.json file is missing");
					return;
				}

				if(config.setJobId(args[1]) == false) {
					System.out.println("Job Id is missing");
					return;
				}
				//Read the configuration setting for this process
				config.setConfig(config);
				if(config.getConfig().parseFile() == false){
					return;
				}

				//Create a xml file to perform xpath search during data extraction.
				String xml = process.getJsonConfigAsXml(config); //getParserConfigAsXml();
				//				System.out.println(xml);
				process.initDom(xml);
				//				Looks like enough to look for only one directory
				process.start();
			}
			else{
				System.out.println("XMLFileExtractorConfig.json file or JobID is missing");
			}

			/*	
			ArrayList<String> dirList = process.getConfig().getDataDirectoryList();
			dirList.add(process.getConfig().getDataFolderPath());		// Data can be either in main data folder or first level sub-folders. 
			for(String dir: dirList) {
				process.getConfig().setDataFolderPath(dir);
				process.start();
			}
			 */
		}
		catch(Exception e)
		{
			System.out.println(e.getMessage());
		}
		return;
	}
}
