package org.gridchem.service.exceptions;

/**
 * This exception is used to mark business rule violations.
 *
 * @author Christian Bauer <christian@hibernate.org>
 */
@SuppressWarnings("serial")
public class BusinessException
	extends RuntimeException {

	public BusinessException() {}

	public BusinessException(String message) {
		super(message);
	}

	public BusinessException(String message, Throwable cause) {
		super(message, cause);
	}

	public BusinessException(Throwable cause) {
		super(cause);
	}
}
