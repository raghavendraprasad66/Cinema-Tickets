/**
 * 
 */
package uk.gov.dwp.uc.pairtest.exception;

/**
 * An enumeration representing error codes related to ticket purchases in the
 * ticket booking system.
 * 
 * This enum defines various error codes that can occur during ticket purchases
 * and serves as a standardized way to identify and handle different types of
 * errors.
 * 
 * @author raghavendra.araveti
 *
 */
public enum PurchaseErrorCode {

	INVALID_ACCOUNT_ID, MISSING_TICKET_REQUEST, INVALID_TICKET_QUANTITY, MAX_TICKETS_EXCEEDED, MISSING_ADULT_TICKET
}
