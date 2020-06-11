package notificationCenter;

import message.*;


public class NotificationCenter {
	
	// generate request-success auction reply
	public AuctionReply generateSellRequestSuccessMsg(String id) {
		String payload = "Your auction sell request has been successfully processed.";
		AuctionReply reply = new AuctionReply(id, true, payload);
		return reply;
	}
	
	// generate request-fail auction reply
	public AuctionReply generateSellRequestFailMsg(String id) {
		String payload = "Your auction sell request has been denied as the first 5 slots have been taken.";
		AuctionReply reply = new AuctionReply(id, false, payload);
		return reply;
	}

}
