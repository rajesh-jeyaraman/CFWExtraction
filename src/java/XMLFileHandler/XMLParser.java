package XMLFileHandler;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;

public class XMLParser extends DefaultHandler {
	public static ArrayList<NameValuePair> xpathList= null;  // Reset this variable before you start parsing. (TODO: Better way)
	//public String str = null;
    private String xPath = "";  // Temporarily removed '/'. To decide whether to start with double "//" for root tag
    private XMLReader xmlReader;
    private XMLParser parent;
    private StringBuilder characters = new StringBuilder();
    private Map<String, Integer> elementNameCount = new HashMap<String, Integer>();
    
    public XMLParser(XMLReader xmlReader) {
        this.xmlReader = xmlReader;
        this.xpathList = xpathList;
    }

    private XMLParser(String xPath, XMLReader xmlReader, XMLParser parent) {
        this(xmlReader);
        this.xPath = xPath;
        this.parent = parent;
    }

    @Override
    public void startElement(String uri, String localName, String qName, Attributes atts) throws SAXException {
        Integer count = elementNameCount.get(qName);
        if(null == count) {
            count = 1;
        } else {
            count++;
        }
        elementNameCount.put(qName, count);
        String childXPath = xPath + "/" + qName + "[" + count + "]";

        int attsLength = atts.getLength();
        for(int x=0; x<attsLength; x++) {
           // System.out.println(childXPath + "[@" + atts.getQName(x) + "='" + atts.getValue(x) + ']');
           //str =childXPath + "[@" + atts.getQName(x) + "='" + atts.getValue(x) + ']';
        	childXPath += "[@" + atts.getQName(x) + "='" + atts.getValue(x) + "']";
            
        }

        XMLParser child = new XMLParser(childXPath, xmlReader, this);
        xmlReader.setContentHandler(child);
    }

    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException {
        String value = characters.toString().trim();
        
        if(value.length() > 0) {
            //System.out.println(xPath + "='" + characters.toString() + "'");
            //str+=xPath + "='" + characters.toString() + "'\n";
        	xpathList.add(new NameValuePair(xPath,characters.toString()));
        }
        xmlReader.setContentHandler(parent);
    }

    @Override
    public void characters(char[] ch, int start, int length) throws SAXException {
    	characters.append(ch, start, length);
    }

}