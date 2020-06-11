package auctionCenter;

//singleton 
public class AuctionMarket {
	
	private static AuctionMarket market = null;
	
	// a representative for the market; handle all the requests
	public Auctioner auctioner;
	
	private int profits = 0;
	
	private AuctionMarket() {
		this.auctioner = new Auctioner();
	};
	
	public static AuctionMarket getInstance() {
		if(market == null) {
			market = new AuctionMarket();
		}
		return market;
	}
	
	// gain profits from transaction
	public void profit(int amount) {
		profits += amount;
	}
	
	public int getProfit() {
		return profits;
	}

}
