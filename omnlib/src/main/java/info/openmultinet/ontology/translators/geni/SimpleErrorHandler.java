package info.openmultinet.ontology.translators.geni;

import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

public class SimpleErrorHandler implements ErrorHandler {
	
	boolean valid;
	
	public SimpleErrorHandler() {
	    this.valid = true;
	}
	
	public void warning(SAXParseException e) throws SAXException {
		valid = false;
		System.out.println(e.getMessage());
	}

	public void error(SAXParseException e) throws SAXException {
		valid = false;
		System.out.println(e.getMessage());
	}

	public void fatalError(SAXParseException e) throws SAXException {
		valid = false;
		System.out.println(e.getMessage());
	}

	public boolean getValid() {
		return valid;
	}
}