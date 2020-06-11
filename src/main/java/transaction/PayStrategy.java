package transaction;

import gameObject.*;

public interface PayStrategy {
	// pay buyer with amount bells
	boolean pay(Player buyer, int amount);
}
