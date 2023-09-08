package uk.gov.dwp.uc.pairtest;

import java.util.Map;

import thirdparty.paymentgateway.TicketPaymentService;
import thirdparty.seatbooking.SeatReservationService;
import uk.gov.dwp.uc.pairtest.domain.TicketTypeRequest;
import uk.gov.dwp.uc.pairtest.exception.InvalidPurchaseException;
import uk.gov.dwp.uc.pairtest.exception.PurchaseErrorCode;

/*
 * Implementation class for the TicketService interface responsible for managing
 * ticket purchases, payment processing, and seat reservations.
 * 
 * @author raghavendra.araveti
 */
public class TicketServiceImpl implements TicketService {

	private final TicketPaymentService paymentService;
	private final SeatReservationService reservationService;

	public TicketServiceImpl(TicketPaymentService paymentService, SeatReservationService reservationService) {
		this.paymentService = paymentService;
		this.reservationService = reservationService;
	}

	private static final int MAX_TICKETS_PER_PURCHASE = 20;
	private static final Map<TicketTypeRequest.Type, Integer> TICKET_PRICES = Map.of(TicketTypeRequest.Type.INFANT, 0,
			TicketTypeRequest.Type.CHILD, 10, TicketTypeRequest.Type.ADULT, 20);

	@Override
	public void purchaseTickets(Long accountId, TicketTypeRequest... ticketTypeRequests)
			throws InvalidPurchaseException {

		var hasAdultTicket = false;
		var hasChildOrInfantTicket = false;
		var totalPrice = 0;
		var numSeats = 0;
		
		// Validate accountId and ticketTypeRequests
		validatePurchaseRequest(accountId, ticketTypeRequests);

		// Calculate the total number of tickets requested, total price of the tickets and validate the purchase
		for (var ticket : ticketTypeRequests) {
			var numTickets = ticket.getNoOfTickets();
			var type = ticket.getTicketType();
			
			// Check if the number of tickets requested is non-negative and grater than zero
	        if (numTickets < 0) {
	            throw new InvalidPurchaseException(PurchaseErrorCode.INVALID_TICKET_QUANTITY, "Invalid ticket quantity: " + numTickets);
	        }

			// Check if the number of tickets exceeds the limit
			if (numSeats + numTickets > MAX_TICKETS_PER_PURCHASE) {
				throw new InvalidPurchaseException(PurchaseErrorCode.MAX_TICKETS_EXCEEDED,
						"Maximum " + MAX_TICKETS_PER_PURCHASE + " tickets can be purchased at a time");
			}

			// Calculate the total price based on ticket type
			totalPrice += numTickets * TICKET_PRICES.getOrDefault(type, 0);
			
			// Update the number of seats based on ticket type
			numSeats += switch (type) {
			case ADULT -> {
				hasAdultTicket = true;
				yield numTickets;
			}
			case CHILD, INFANT -> {
				hasChildOrInfantTicket = true;
				yield (type == TicketTypeRequest.Type.CHILD) ? numTickets : 0;
			}
			};
		}

		// Ensure that child or infant tickets can only be purchased with an adult ticket
		if (hasChildOrInfantTicket && !hasAdultTicket) {
			throw new InvalidPurchaseException(PurchaseErrorCode.MISSING_ADULT_TICKET, "Child or infant tickets cannot be purchased without an adult ticket");
		}

		// Make payment to the payment service
		paymentService.makePayment(accountId, totalPrice);
		
		// Reserve seats using the seat reservation service
		reservationService.reserveSeat(accountId, numSeats);
	}
	
	private void validatePurchaseRequest(Long accountId, TicketTypeRequest[] ticketTypeRequests)
            throws InvalidPurchaseException {
		
		// Validate the accountId
        if (accountId <= 0) {
            throw new InvalidPurchaseException(PurchaseErrorCode.INVALID_ACCOUNT_ID, "Invalid AccountId. An AccountId should be greater than zero");
        }

        // Validate the ticketTypeRequests
        if (ticketTypeRequests == null || ticketTypeRequests.length == 0) {
            throw new InvalidPurchaseException(PurchaseErrorCode.MISSING_TICKET_REQUEST, "At least one ticket type request is required");
        }
    }
}
