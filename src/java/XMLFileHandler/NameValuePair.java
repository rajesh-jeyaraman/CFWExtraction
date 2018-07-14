package XMLFileHandler;

public class NameValuePair {
	private String name;
	private String value;
	private String frompath;
	private String topath;
	private String datatype;
	private String fieldName;
	private String search;
	private String result;
	private String id;
	private String minOccur;
	private String maxOccur;
	
	NameValuePair(String name, String value){
		this.name = name;
		this.value = value;
		this.frompath = name; // Same as name if no value mentioned.
		this.topath = name;   // Same as name if no value mentioned.
		this.datatype = "string";
		this.fieldName = "";
		this.search = "true";
		this.result = "true";
		this.id = "";
		this.minOccur = "0";
		this.maxOccur = "";
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getValue() {
		return value;
	}
	public void setValue(String value) {
		this.value = value;
	}

	public String getFrompath() {
		return frompath;
	}
	public void setFrompath(String frompath) {
		this.frompath = frompath;
	}
	public String getTopath() {
		return topath;
	}
	public void setTopath(String topath) {
		this.topath = topath;
	}
	public String getDatatype() {
		return datatype;
	}
	public void setDatatype(String datatype) {
		this.datatype = datatype;
	}
	public String getFieldName() {
		return fieldName;
	}
	public void setFieldName(String fieldName) {
		this.fieldName = fieldName;
	}
	public String getSearch() {
		return search;
	}
	public void setSearch(String search) {
		this.search = search;
	}
	public String getResult() {
		return result;
	}
	public void setResult(String result) {
		this.result = result;
	}
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getMinOccur() {
		return minOccur;
	}
	public void setMinOccur(String minOccur) {
		this.minOccur = minOccur;
	}
	public String getMaxOccur() {
		return maxOccur;
	}
	public void setMaxOccur(String maxOccur) {
		this.maxOccur = maxOccur;
	}
	

	
}
