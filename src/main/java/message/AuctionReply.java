package message;


public class AuctionReply extends Message{
	
	private boolean success;
	
	public AuctionReply(String id, boolean success, String payload) {
		this.type = "reply for auction";
		this.id = id;
		this.success = success;
		this.payload = payload;
	}
	
	public boolean isSuccess() {
		return success;
	}
}
