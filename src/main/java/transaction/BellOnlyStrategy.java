package transaction;

import gameObject.Player;

public class BellOnlyStrategy implements PayStrategy {

	@Override
	public boolean pay(Player buyer, int amount) {
		// TODO Auto-generated method stub
		return buyer.payWithBell(amount);
	}
}
