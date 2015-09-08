package info.openmultinet.ontology.exceptions;

/**
 * This class is for elements that are required in an RSpec by the XSD, but are
 * missing
 * 
 * @author robynloughnane
 *
 */
public class DeprecatedRspecVersionException extends Exception {

	private static final long serialVersionUID = 1L;

	public DeprecatedRspecVersionException() {
		super();
	}

	public DeprecatedRspecVersionException(final String version) {
		super("RSpec version " + version
				+ " is deprecated and not supported by the translator.");
	}

	public DeprecatedRspecVersionException(final Throwable cause) {
		super(cause);
	}

}
