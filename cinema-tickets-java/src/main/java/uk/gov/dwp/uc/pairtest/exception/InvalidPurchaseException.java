package uk.gov.dwp.uc.pairtest.exception;

/**
 * Custom exception class representing errors related to invalid ticket
 * purchases in the ticket booking system.
 * 
 * 
 * @author raghavendra.araveti
 */
public class InvalidPurchaseException extends RuntimeException {

	private static final long serialVersionUID = 1L;
	private final PurchaseErrorCode errorCode;

	public InvalidPurchaseException(String message) {
		super(message);
		this.errorCode = null;
	}

	public InvalidPurchaseException(String message, Throwable cause) {
		super(message, cause);
		this.errorCode = null;
	}

	public InvalidPurchaseException(PurchaseErrorCode errorCode, String message) {
		super(message);
		this.errorCode = errorCode;
	}

	public PurchaseErrorCode getErrorCode() {
		return errorCode;
	}
}
