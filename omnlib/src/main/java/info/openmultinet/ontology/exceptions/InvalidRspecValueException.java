package info.openmultinet.ontology.exceptions;

/**
 * This class is for attribute or element values that have the wrong type, e.g.
 * string instead of integer
 * 
 * @author robynloughnane
 *
 */
public class InvalidRspecValueException extends Exception {

	private static final long serialVersionUID = 1L;

	public InvalidRspecValueException() {
		super();
	}

	public InvalidRspecValueException(final String attributeOrElementName) {
		super("The value of " + attributeOrElementName + " is not valid.");
	}

	public InvalidRspecValueException(final Throwable cause) {
		super(cause);
	}

}
