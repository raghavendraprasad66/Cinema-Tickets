/**
 * 
 */
package uk.gov.dwp.uc.pairtest;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.Mockito;

import thirdparty.paymentgateway.TicketPaymentService;
import thirdparty.seatbooking.SeatReservationService;
import uk.gov.dwp.uc.pairtest.domain.TicketTypeRequest;
import uk.gov.dwp.uc.pairtest.exception.InvalidPurchaseException;
import uk.gov.dwp.uc.pairtest.exception.PurchaseErrorCode;

/**
 * This class contains unit tests for the `TicketServiceImpl` class. It uses
 * Mockito to create mock objects for the payment and seat reservation services.
 * These tests cover various scenarios related to ticket purchases and error
 * handling.
 * 
 * Test cases include checking for null or empty ticket type requests, exceeding
 * the maximum ticket limit, attempting to purchase child or infant tickets
 * without an adult ticket, valid ticket purchases, and validation of account
 * IDs.
 * 
 * Each test method verifies the expected behavior of the `purchaseTickets`
 * method in `TicketServiceImpl`, including the expected error messages and
 * error codes when invalid purchases are made.
 * 
 * 
 * @author raghavendra.araveti
 *
 */
public class TicketServiceImplTest {

	private TicketPaymentService mockPaymentService;
	private SeatReservationService mockReservationService;
	private TicketServiceImpl ticketService;

	@Rule
	public ExpectedException thrown = ExpectedException.none();

	@Before
	public void setUp() {
		mockPaymentService = mock(TicketPaymentService.class);
		mockReservationService = mock(SeatReservationService.class);
		ticketService = new TicketServiceImpl(mockPaymentService, mockReservationService);
	}

	@Test
	public void testPurchaseTicketsWithNullTicketTypeRequests() throws InvalidPurchaseException {
		thrown.expect(InvalidPurchaseException.class);
		thrown.expectMessage("At least one ticket type request is required");
		TicketTypeRequest[] ticketTypeRequests = null;

		try {
			ticketService.purchaseTickets(1L, ticketTypeRequests);
		} catch (InvalidPurchaseException e) {
			// Verify that no interactions with paymentService and reservationService
			// occurred
			verify(mockPaymentService, Mockito.never()).makePayment(Mockito.anyLong(), Mockito.anyInt());
			verify(mockReservationService, Mockito.never()).reserveSeat(Mockito.anyLong(), Mockito.anyInt());

			// Check the error code
			assertEquals(PurchaseErrorCode.MISSING_TICKET_REQUEST, e.getErrorCode());

			throw e; // Re-throw the exception
		}

	}

	@Test
	public void testPurchaseTicketsWithEmptyTicketTypeRequests() throws InvalidPurchaseException {
		thrown.expect(InvalidPurchaseException.class);
		thrown.expectMessage("At least one ticket type request is required");
		TicketTypeRequest[] ticketTypeRequests = new TicketTypeRequest[] {};

		try {
			ticketService.purchaseTickets(1L, ticketTypeRequests);
		} catch (InvalidPurchaseException e) {
			// Verify that no interactions with paymentService and reservationService
			// occurred
			verify(mockPaymentService, Mockito.never()).makePayment(Mockito.anyLong(), Mockito.anyInt());
			verify(mockReservationService, Mockito.never()).reserveSeat(Mockito.anyLong(), Mockito.anyInt());

			// Check the error code
			assertEquals(PurchaseErrorCode.MISSING_TICKET_REQUEST, e.getErrorCode());

			throw e; // Re-throw the exception
		}
	}

	@Test
	public void testPurchaseTicketsWithMoreThanMaxTicketsPerPurchase() throws InvalidPurchaseException {

		thrown.expect(InvalidPurchaseException.class);
		thrown.expectMessage("Maximum 20 tickets can be purchased at a time");

		TicketTypeRequest adultTicketType = new TicketTypeRequest(TicketTypeRequest.Type.ADULT, 20);
		TicketTypeRequest childTicketType = new TicketTypeRequest(TicketTypeRequest.Type.CHILD, 1);
		TicketTypeRequest infantTicketType = new TicketTypeRequest(TicketTypeRequest.Type.INFANT, 1);
		TicketTypeRequest[] ticketTypeRequests = { adultTicketType, childTicketType, infantTicketType };

		try {
			ticketService.purchaseTickets(1L, ticketTypeRequests);
		} catch (InvalidPurchaseException e) {
			// Verify that no interactions with paymentService and reservationService
			// occurred
			verify(mockPaymentService, Mockito.never()).makePayment(Mockito.anyLong(), Mockito.anyInt());
			verify(mockReservationService, Mockito.never()).reserveSeat(Mockito.anyLong(), Mockito.anyInt());

			// Check the error code
			assertEquals(PurchaseErrorCode.MAX_TICKETS_EXCEEDED, e.getErrorCode());

			throw e; // Re-throw the exception
		}
	}

	@Test
	public void testPurchaseTicketsWithChildAndInfantTicketWithoutAdultTicket() throws InvalidPurchaseException {

		thrown.expect(InvalidPurchaseException.class);
		thrown.expectMessage("Child or infant tickets cannot be purchased without an adult ticket");

		TicketTypeRequest childTicketType = new TicketTypeRequest(TicketTypeRequest.Type.CHILD, 1);
		TicketTypeRequest infantTicketType = new TicketTypeRequest(TicketTypeRequest.Type.INFANT, 1);
		TicketTypeRequest[] ticketTypeRequests = { childTicketType, infantTicketType };

		try {
			ticketService.purchaseTickets(1L, ticketTypeRequests);
		} catch (InvalidPurchaseException e) {
			// Verify that no interactions with paymentService and reservationService
			// occurred
			verify(mockPaymentService, Mockito.never()).makePayment(Mockito.anyLong(), Mockito.anyInt());
			verify(mockReservationService, Mockito.never()).reserveSeat(Mockito.anyLong(), Mockito.anyInt());

			// Check the error code
			assertEquals(PurchaseErrorCode.MISSING_ADULT_TICKET, e.getErrorCode());

			throw e; // Re-throw the exception
		}
	}

	@Test
	public void testPurchaseTicketsWithChildTicketWithoutAdultTicket() throws InvalidPurchaseException {

		thrown.expect(InvalidPurchaseException.class);
		thrown.expectMessage("Child or infant tickets cannot be purchased without an adult ticket");

		TicketTypeRequest childTicketType = new TicketTypeRequest(TicketTypeRequest.Type.CHILD, 1);
		TicketTypeRequest[] ticketTypeRequests = { childTicketType };

		try {
			ticketService.purchaseTickets(1L, ticketTypeRequests);
		} catch (InvalidPurchaseException e) {
			// Verify that no interactions with paymentService and reservationService
			// occurred
			verify(mockPaymentService, Mockito.never()).makePayment(Mockito.anyLong(), Mockito.anyInt());
			verify(mockReservationService, Mockito.never()).reserveSeat(Mockito.anyLong(), Mockito.anyInt());

			// Check the error code
			assertEquals(PurchaseErrorCode.MISSING_ADULT_TICKET, e.getErrorCode());

			throw e; // Re-throw the exception
		}
	}

	@Test
	public void testPurchaseTicketsWithInfantTicketWithoutAdultTicket() throws InvalidPurchaseException {

		thrown.expect(InvalidPurchaseException.class);
		thrown.expectMessage("Child or infant tickets cannot be purchased without an adult ticket");

		TicketTypeRequest infantTicketType = new TicketTypeRequest(TicketTypeRequest.Type.INFANT, 1);
		TicketTypeRequest[] ticketTypeRequests = { infantTicketType };

		try {
			ticketService.purchaseTickets(1L, ticketTypeRequests);
		} catch (InvalidPurchaseException e) {
			// Verify that no interactions with paymentService and reservationService
			// occurred
			verify(mockPaymentService, Mockito.never()).makePayment(Mockito.anyLong(), Mockito.anyInt());
			verify(mockReservationService, Mockito.never()).reserveSeat(Mockito.anyLong(), Mockito.anyInt());

			// Check the error code
			assertEquals(PurchaseErrorCode.MISSING_ADULT_TICKET, e.getErrorCode());

			throw e; // Re-throw the exception
		}
	}

	@Test
	public void testPurchaseTicketsWithValidAdultAndChildTicketTypeRequests() throws InvalidPurchaseException {
		TicketTypeRequest adultTicketType = new TicketTypeRequest(TicketTypeRequest.Type.ADULT, 2);
		TicketTypeRequest childTicketType = new TicketTypeRequest(TicketTypeRequest.Type.CHILD, 1);
		TicketTypeRequest[] ticketTypeRequests = { adultTicketType, childTicketType };

		ticketService.purchaseTickets(123L, ticketTypeRequests);

		verify(mockPaymentService).makePayment(123L, 50);
		verify(mockReservationService).reserveSeat(123L, 3);
	}

	@Test
	public void testPurchaseTicketsWithValidAdultAndInfantTicketTypeRequests() throws InvalidPurchaseException {
		TicketTypeRequest adultTicketType = new TicketTypeRequest(TicketTypeRequest.Type.ADULT, 2);
		TicketTypeRequest infantTicketType = new TicketTypeRequest(TicketTypeRequest.Type.INFANT, 1);
		TicketTypeRequest[] ticketTypeRequests = { adultTicketType, infantTicketType };

		ticketService.purchaseTickets(123L, ticketTypeRequests);

		verify(mockPaymentService).makePayment(123L, 40);
		verify(mockReservationService).reserveSeat(123L, 2);
	}

	@Test
	public void testPurchaseTicketsWithValidAdultAndChildAndInfantTicketTypeRequests() throws InvalidPurchaseException {
		TicketTypeRequest adultTicketType = new TicketTypeRequest(TicketTypeRequest.Type.ADULT, 2);
		TicketTypeRequest childTicketType = new TicketTypeRequest(TicketTypeRequest.Type.CHILD, 1);
		TicketTypeRequest infantTicketType = new TicketTypeRequest(TicketTypeRequest.Type.INFANT, 1);
		TicketTypeRequest[] ticketTypeRequests = { adultTicketType, childTicketType, infantTicketType };

		ticketService.purchaseTickets(123L, ticketTypeRequests);

		verify(mockPaymentService).makePayment(123L, 50);
		verify(mockReservationService).reserveSeat(123L, 3);
	}

	@Test
	public void testPurchaseTicketsWithInvalidAccountId() throws InvalidPurchaseException {

		thrown.expect(InvalidPurchaseException.class);
		thrown.expectMessage("Invalid AccountId. An AccountId should be greater than zero");

		TicketTypeRequest adultTicketType = new TicketTypeRequest(TicketTypeRequest.Type.ADULT, 2);
		TicketTypeRequest[] ticketTypeRequests = { adultTicketType };

		try {
			ticketService.purchaseTickets(0L, ticketTypeRequests);
		} catch (InvalidPurchaseException e) {
			// Verify that no interactions with paymentService and reservationService
			// occurred
			verify(mockPaymentService, Mockito.never()).makePayment(Mockito.anyLong(), Mockito.anyInt());
			verify(mockReservationService, Mockito.never()).reserveSeat(Mockito.anyLong(), Mockito.anyInt());

			// Check the error code
			assertEquals(PurchaseErrorCode.INVALID_ACCOUNT_ID, e.getErrorCode());

			throw e; // Re-throw the exception
		}

	}

}
