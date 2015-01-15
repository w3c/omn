package info.openmultinet.ontology.translators.geni.exceptions;

public class InvalidModelException extends Exception {

	private static final long serialVersionUID = 1L;

	public InvalidModelException() {
		super();
	}

	public InvalidModelException(String message, Throwable cause,
			boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

	public InvalidModelException(String message, Throwable cause) {
		super(message, cause);
	}

	public InvalidModelException(String message) {
		super(message);
	}

	public InvalidModelException(Throwable cause) {
		super(cause);
	}
	
}
