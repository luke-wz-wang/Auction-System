package message;


public class AuctionRequest extends Message {
	
	public AuctionRequest(String id, String payload) {
		this.type = "request for auction";
		this.id = id;
		this.payload = payload;
	}
	
	
	
	
}
