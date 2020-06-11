package gameObject;

public interface Trader {
	
	// request to sell item; return true if request is successful
	boolean requestAuction(Item item);
	
	// subscribe to market via auctioner
	void subscribe();
	
	// unsubscribe 
	void unsubscribe();
	
	// watch a seller's item
	void watch(int sellerId);
	
}
