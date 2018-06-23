package XMLFileHandler;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.Node;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.XMLWriter;

/**
 * @author bchild
 *
 */
public class XPathUtils {
	//private static final Logger LOGGER = Logger.getLogger(XPathUtils.class.getName());

  private static final String SLASH = "/";
	private static final String R_BRACKET = "]";
	private static final String L_BRACKET = "[";
	
	/**
	 * Looks for the last '/' and returns the name of the last element
	 * @param xpath
	 * @return the child element name or null
	 */
	public static final String getChildElementName(String xpath) {
		if(StringUtils.isEmpty(xpath)) {
			return null;
		}
		String childName = xpath.substring(xpath.lastIndexOf(SLASH) + 1);
		return stripIndex(childName);
	}
	
	/**
	 * returns the xpath if traversing up the tree one node
	 * i.e. /root/suspension_rec returns /root
	 * @param xpath
	 * @return
	 */
	public static final String getParentXPath(String xpath) {
		if(StringUtils.isEmpty(xpath) || xpath.lastIndexOf(SLASH) <= 0) {
			return null;
		}
		return removeSpecialAttr(xpath.substring(0, xpath.lastIndexOf(SLASH)));  // rework
	}
	
	/**
	 * returns the index of the child element xpath
	 * i.e. /suspension_rec[3] returns 3.  /suspension_rec defaults to 1
	 * @param xpath
	 * @return 1, the index, or null if the provided xpath is empty
	 */
	public static Integer getChildElementIndex(String xpath) {
		if(StringUtils.isEmpty(xpath)) {
			return null;
		}
		
		if(xpath.endsWith(R_BRACKET)) {
			String value = xpath.substring(xpath.lastIndexOf(L_BRACKET) + 1, xpath.lastIndexOf(R_BRACKET));
			if(StringUtils.isNumeric(value)) {
				return Integer.valueOf(value);
			}
		} 
		return 1;
	}
	
	/**
	 * @param xpath
	 * @param childIndex
	 * @return
	 */
	public static String createPositionXpath(String xpath, Integer childIndex) {
		if(StringUtils.isEmpty(xpath)) {
			return null;
		}
		return stripIndex(xpath) + "[position()<" + childIndex + "]";
	}
	
	/**
	 * @param childName
	 * @return
	 */
	private static String stripIndex(String childName) {
		if(childName.endsWith(R_BRACKET)) {
			return childName.substring(0, childName.lastIndexOf(L_BRACKET));
		} else {
			return childName;
		}
	}
	
	public static String printDoc(Document document, String fileName) {
		String str = null;
		OutputFormat format = OutputFormat.createPrettyPrint();
		//format.setEncoding("ISO-8859-1");
		format.setEncoding("UTF-8");
		StringWriter writer = new StringWriter();
		XMLWriter xmlwriter = new XMLWriter(writer, format);
		try {
			xmlwriter.write( document );
			//LOGGER.debug(writer.getBuffer().toString());
			//System.out.println(writer.getBuffer().toString());
			 str = writer.getBuffer().toString();
			//System.out.println(str);
			if(fileName != null) {
				try {
					PrintWriter out = new PrintWriter(fileName);
					out.println(str);
					out.close();
				}
				catch(Exception e) {
					System.out.println(e.getMessage());
				}
			}
			
		} catch (IOException e) {
			//LOGGER.error(e.getMessage(), e);
			System.out.println(e.getMessage());
		}
		return str;
	}
	
	/**
	 * Recursive method to create an element and, if necessary, its parents and siblings
	 * @param document
	 * @param xpath to single element
	 * @param value if null an empty element will be created
	 * @return the created Node
	 */
	private static Node addElementToParent(Document document, String xpath, String value) {
	//	if(LOGGER.isDebugEnabled()) {
	//		LOGGER.debug("adding Element: " + xpath + " -> " + value);
	//	}
		
		String elementName = XPathUtils.getChildElementName(xpath);
		String parentXPath = XPathUtils.getParentXPath(xpath);
		Node parentNode = document.selectSingleNode(parentXPath);
		if(parentNode == null) {
			parentNode = addElementToParent(document, parentXPath, null);
			// Let's revisit sometime later. 
			//NameValuePair attr = getAttr(xpath);
			//if(attr !=null) {
			 //	((Element)parentNode).addAttribute(attr.getName(), attr.getValue());
			//}
			//get parent attribute and add here
			//System.out.println("Parent XPath -->" + xpath);
			//System.out.println("attr  Name--->" + attr.getName());
			//System.out.println("attr Value --->" + attr.getValue());
		}
		
		// create younger siblings if needed
		Integer childIndex = XPathUtils.getChildElementIndex(xpath);
		if(childIndex > 1) {
			List<?> nodelist = document.selectNodes(XPathUtils.createPositionXpath(xpath, childIndex));
			// how many to create = (index wanted - existing - 1 to account for the new element we will create)
			int nodesToCreate = childIndex - nodelist.size() - 1;
			for(int i = 0; i < nodesToCreate; i++) {
				((Element)parentNode).addElement(elementName);
				
			}
		}
		
		// create requested element
		Element created = ((Element)parentNode).addElement(elementName);
		if(null != value) {
			created.addText(value);
		}
		return created;
	}

	public static String createXML(ArrayList<NameValuePair> xpathList, String fileName) {
		String root= getRootElement(xpathList.get(0).getName());
		Document document = DocumentHelper.createDocument(DocumentHelper.createElement(root));

		for(NameValuePair path: xpathList) {
			//System.out.println(path.getName()+ "=" + path.getValue());
			addElementToParent(document,removeSpecialAttr(path.getName()), path.getValue());
		}
		
		return printDoc(document, fileName);
	}
	
	private static String removeSpecialAttr(String input) {
		String str = "";
		char ATTRSTART = '[';
		char ATTREND = ']';
		char ATTHERATE = '@';
		boolean skip = false;
		for(int i = 0 ;  i< input.length();++i ) {
			if(input.charAt(i) == ATTRSTART && input.charAt(i+1)==ATTHERATE) {
				skip = true;
			}
			if(skip == false) {
				str +=input.charAt(i);
			}
			if(input.charAt(i) == ATTREND) {
				skip = false;
			}
		}
		return str;
	}
	
	public static String getRootElement(String xpath) {
		String str = "";
		for(int i = 1; i < xpath.length(); ++i) { // Omitting first slash char
			if(xpath.charAt(i) == '/' || xpath.charAt(i) == '[') {
				break;
			}
			str += xpath.charAt(i);
		}
		return str;
	}
	private static NameValuePair getAttr(String xpath) {
		NameValuePair attr = null;
		String name = "";
		String value ="";
		boolean readName=false;
		boolean readValue=false;
		
		for(int i = 0; i <xpath.length(); ++i) {
			if(readName == true && xpath.charAt(i) != '=') {
				name += xpath.charAt(i);
			}
			if(readValue == true && xpath.charAt(i) != '\'') {
				value += xpath.charAt(i);
			}
			if(xpath.charAt(i) == '@') {
				readName = true;
			}
			if(xpath.charAt(i) == '=') {
				readName = false;
				readValue = true;
			}
		}
		if(name != null && value != null) {
			attr = new NameValuePair(name, value);
		}
		
		return attr;
	}
}