package transaction;

import gameObject.*;

import javax.jms.ConnectionFactory;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.camel.CamelContext;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.component.jms.JmsComponent;
import org.apache.camel.impl.DefaultCamelContext;

import auctionCenter.*;
import console.Console;

public class Transaction {
	
	private Player seller;
	private Player buyer;
	private int amount;
	
	private PayStrategy payStrategy;
	
	public Transaction(Player seller, Player buyer, int amount) {
		this.seller = seller;
		this.buyer = buyer;
		this.amount = amount;
	}
	
	// initialize the transaction by checking the payment method
	public void init() {
		if(buyer.transacStyle == "bellOnly") {
			payStrategy = new BellOnlyStrategy();
		}else {
			payStrategy = new CombineStrategy();
		}
	}	
	
	// process the transaction through auction market
	public boolean handleTransaction(AuctionMarket market) {
		CamelContext context = new DefaultCamelContext();
		ConnectionFactory connectionFactory = new ActiveMQConnectionFactory("tcp://localhost:61616");
		context.addComponent("jms",JmsComponent.jmsComponentAutoAcknowledge(connectionFactory));
		
		boolean success = payStrategy.pay(buyer, amount);
		StringBuilder sb_seller = new StringBuilder();
		StringBuilder sb_buyer = new StringBuilder();
		if(success) {
			sb_seller.append("Transaction is successful.");
			sb_buyer.append("Transaction is successful.");
			
			// seller receive payment
			seller.recievePayment((int)(0.9*amount));
			// market takes profit
			market.profit((int)(0.1*amount));
			
			Item item = market.auctioner.itemsToSell.get(seller.getId());
			buyer.acquireItem(item);
			seller.removeItem(item);
			
			sb_seller.append(" You sold " + item.getName() + ".");
			sb_seller.append(" Your current balance is  " + seller.getBalance() + ".");
			sb_buyer.append(" You bought " + item.getName() + ".");
			sb_buyer.append(" Your current balance is  " + buyer.getBalance() + ".");
		}else {
			// handle transaction failure
			sb_seller.append("Transaction failed. Your item is returned.");
			sb_buyer.append("Transaction failed. You don't have enough bells/resources.");
		}
		ProducerTemplate template = context.createProducerTemplate();
    	template.sendBody("jms:queue:"  + Console.reboot_num + "Auction_Sys_Player" + seller.getId(), sb_seller.toString());
    	template.sendBody("jms:queue:"  + Console.reboot_num + "Auction_Sys_Player" + buyer.getId(), sb_buyer.toString());
    	
		return success;
	}
	
	

}
