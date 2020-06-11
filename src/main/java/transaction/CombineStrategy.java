package transaction;

import gameObject.Player;

public class CombineStrategy implements PayStrategy{

	@Override
	public boolean pay(Player buyer, int amount) {
		// TODO Auto-generated method stub
		int itemValue = buyer.payWithItem();
		if(itemValue > amount) {
			buyer.recievePayment(itemValue - amount);
			return true;
		}else {
			return buyer.payWithBell(amount - itemValue);
		}
	}
	
}
