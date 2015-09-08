package info.openmultinet.ontology.exceptions;

public class InvalidModelException extends Exception {

	private static final long serialVersionUID = 1L;

	public InvalidModelException() {
		super();
	}

	public InvalidModelException(final String message, final Throwable cause,
			final boolean enableSuppression, final boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

	public InvalidModelException(final String message, final Throwable cause) {
		super(message, cause);
	}

	public InvalidModelException(final String message) {
		super(message);
	}

	public InvalidModelException(final Throwable cause) {
		super(cause);
	}

}
