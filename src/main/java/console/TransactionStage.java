package console;

import java.util.Hashtable;
import java.util.Map;

import javax.jms.ConnectionFactory;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.camel.CamelContext;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.jms.JmsComponent;
import org.apache.camel.impl.DefaultCamelContext;

import auctionCenter.AuctionMarket;
import gameObject.Item;
import gameObject.Player;
import javafx.util.Pair;
import transaction.Transaction;

public class TransactionStage {
	
	// process the transactions between buyers and sellers
	public static void transact(Hashtable<Integer, Player> players, AuctionMarket market) throws Exception {
		
		 CamelContext context = new DefaultCamelContext();

	        // connect to ActiveMQ JMS broker listening on localhost on port 61616
	        ConnectionFactory connectionFactory = 
	        	new ActiveMQConnectionFactory("tcp://localhost:61616");
	        context.addComponent("jms",
	            JmsComponent.jmsComponentAutoAcknowledge(connectionFactory));
	        
	        // add our route to the CamelContext
	        context.addRoutes(new RouteBuilder() {
	            public void configure() {
	                from("jms:queue:"  + Console.reboot_num + "Auction_Sys_Final_Call")
	                .convertBodyTo(String.class)
	                .process(new Processor() {                    
	                    public void process(Exchange exchange) throws Exception {
	                    	String[] res = exchange.getIn().getBody(String.class).split("\t");
	                    	String[] details = res[0].split(" ");
	                    	
	                    	int sellerId = Integer.parseInt(details[1]);
	                    	int buyerId = Integer.parseInt(details[3]);
	                    	int offer = Integer.parseInt(details[5]);
	                    	
	                    	// actions
	                    	Player seller = players.get(sellerId);
	                    	Player buyer = players.get(buyerId);
	                    	
	                    	Transaction transaction = new Transaction(seller, buyer, offer);
	                    	transaction.init();
	                    	boolean success = transaction.handleTransaction(market);
	                    	                	
	                    	StringBuilder sb = new StringBuilder();
	                    	if(success) {
		                    	sb.append("Transaction between seller Player" + sellerId + " and buyer Player" + buyerId + " is successful.");

	                    	}else {
		                    	sb.append("Transaction between seller Player" + sellerId + " and buyer Player" + buyerId + " failed.");
	                    	}	                    	
	                    	exchange.getIn().setBody(sb.toString());
	                    	System.out.println(sb.toString());
	                    }
	                })
	                .to("jms:queue:"  + Console.reboot_num + "Auction_Sys_Transaction");
	                //.to("jms:queue:dump");
	                try {
	                	Thread.sleep(1000);
	                } catch (InterruptedException e) {
	                	e.printStackTrace();
	                }
	   
	            }
	        });
	        context.start();
	        Thread.sleep(3000);
	        context.stop();
	}

}
