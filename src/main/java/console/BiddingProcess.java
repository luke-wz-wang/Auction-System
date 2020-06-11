package console;



import javax.jms.ConnectionFactory;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.camel.CamelContext;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.jms.JmsComponent;
import org.apache.camel.impl.DefaultCamelContext;

import auctionCenter.*;
import gameObject.*;
import java.util.*;
import javafx.util.Pair;

public class BiddingProcess {
	
	// handle bid offers from .csv files
	public static void bidding(AuctionMarket market) throws Exception {
		
		 CamelContext context = new DefaultCamelContext();

	        // connect to ActiveMQ JMS broker listening on localhost on port 61616
	        ConnectionFactory connectionFactory = 
	        	new ActiveMQConnectionFactory("tcp://localhost:61616");
	        context.addComponent("jms",
	            JmsComponent.jmsComponentAutoAcknowledge(connectionFactory));
	        
	        // add our route to the CamelContext
	        context.addRoutes(new RouteBuilder() {
	            public void configure() {
	                from("file:data/bid?noop=true")
	                .choice()
	                .when(header("CamelFileName")
	                		.endsWith(".csv"))
	                .unmarshal()
	                .csv()
	                .split(body())
	                .convertBodyTo(String.class)
	                .process(new Processor() {                    
	                    public void process(Exchange exchange) throws Exception {
	                    	String[] res = exchange.getIn().getBody(String.class).split("\t");
	                    	String[] details = res[0].split(" ");
	                    	
	                    	int bidderId = Integer.parseInt(details[0].substring(1));
	                    	int sellerId = Integer.parseInt(details[1]);
	                    	int offer = Integer.parseInt(details[2]);
	                    	
	                    	String itemName = details[3];
	                    	int itemPrice = Integer.parseInt(details[4].substring(0, details[2].length()-1));
	                    	
	                    	// actions
	                    	Pair<Integer, Integer> newBidder = new Pair<>(bidderId, offer);
	                    	for (Map.Entry mapElement : market.auctioner.salesList.entrySet()) {
	                    		Player bidder = (Player)mapElement.getKey();
	                    		// check bidder bids on an existed item
	                    		if(bidder.getId() == sellerId){
	                    			Item item = new Item(itemName, itemPrice);
	                    			market.auctioner.itemsToSell.put(sellerId, item);
	                    			
	                    			// update the current offers if the bidder makes a higher offer
	                    			if(market.auctioner.offers.containsKey(sellerId)) {
	                    				Pair<Integer, Integer> curBidder = market.auctioner.offers.get(sellerId);
	                    				if(curBidder.getValue() < offer) {
	                    					market.auctioner.offers.put(sellerId, newBidder);
	                    					// notify watchers of this item with the new bid
	                    					List<Player> watcherList = market.auctioner.watchers.get(sellerId);
	                    					for(int i = 0; i < watcherList.size(); i++) {
	                    						Player watcher = watcherList.get(i);
	                    						watcher.updatesFromWatchList(sellerId, item, offer);
	                    					}
	                    					
	                    				}
	                    			}else {
	                    				market.auctioner.offers.put(sellerId, newBidder);
	                    				// notify watchers of this item with the new bid
                    					List<Player> watcherList = market.auctioner.watchers.get(sellerId);
                    					for(int i = 0; i < watcherList.size(); i++) {
                    						Player watcher = watcherList.get(i);
                    						watcher.updatesFromWatchList(sellerId, item, offer);
                    					}
	                    			}
	                    			break;
	                    		}
	                    		
	                    	}
	                    }
	                })
	                .to("jms:queue:" +  + Console.reboot_num + "Auction_Sys_Bidding");
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
	
	// process all the bids and make the final call/find the bidder withs the highest offer
	public static void finalCall(AuctionMarket market) throws Exception {
		
		 CamelContext context = new DefaultCamelContext();

	        // connect to ActiveMQ JMS broker listening on localhost on port 61616
	        ConnectionFactory connectionFactory = 
	        	new ActiveMQConnectionFactory("tcp://localhost:61616");
	        context.addComponent("jms",
	            JmsComponent.jmsComponentAutoAcknowledge(connectionFactory));
	        
	        ProducerTemplate template = context.createProducerTemplate();
	        
	        // sent the final call messages to the MQ
	        for (Map.Entry mapElement : market.auctioner.offers.entrySet()) {	        	
	        	StringBuilder sb = new StringBuilder();
            	sb.append("SellerID: " + (int)mapElement.getKey() + " ");
            	Pair<Integer, Integer> winner = (Pair<Integer, Integer>)mapElement.getValue();
            	sb.append("BuyerId: " + winner.getKey() + " ");
            	sb.append("Offer: " + winner.getValue());          
            	
        		template.sendBody("jms:queue:"  + Console.reboot_num + "Auction_Sys_Final_Call", sb.toString());
        		
     
        	}
	       
	}

}
