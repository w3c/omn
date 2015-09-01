package info.openmultinet.ontology.exceptions;

/**
 * This class is for elements that are required in an RSpec by the XSD, but are
 * missing
 * 
 * @author robynloughnane
 *
 */
public class MissingRspecElementException extends Exception {

	private static final long serialVersionUID = 1L;

	public MissingRspecElementException() {
		super();
	}

	public MissingRspecElementException(final String missingElement) {
		super("The element " + missingElement
				+ " is required by the RSpec XML schema.");
	}

	public MissingRspecElementException(final Throwable cause) {
		super(cause);
	}

}
