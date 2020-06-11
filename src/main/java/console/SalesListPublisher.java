package console;

import javax.jms.ConnectionFactory;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.camel.CamelContext;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.jms.JmsComponent;
import org.apache.camel.impl.DefaultCamelContext;

import auctionCenter.AuctionMarket;

public class SalesListPublisher {
	
	// publish the sales list with selected auction requests
	public static void publishSalesList(AuctionMarket market) throws Exception {
		
		 CamelContext context = new DefaultCamelContext();

	        // connect to ActiveMQ JMS broker listening on localhost on port 61616
	        ConnectionFactory connectionFactory = 
	        	new ActiveMQConnectionFactory("tcp://localhost:61616");
	        context.addComponent("jms",
	            JmsComponent.jmsComponentAutoAcknowledge(connectionFactory));
	        
	        // add our route to the CamelContext
	        context.addRoutes(new RouteBuilder() {
	            public void configure() {
	            	
	            	// notify all the subscribers
	            	for(int i = 0; i < market.auctioner.subscribers.size(); i++) {
	            		from("jms:topic:"  + Console.reboot_num + "Auction_Sys_AuctionRequests")	            			
	            		.to("jms:queue:"  + Console.reboot_num + "Auction_Sys_" + "Player" + market.auctioner.subscribers.get(i).getId());
	            	}
	            	
	            	// publish to sales list queue
	            	from("jms:topic:"  + Console.reboot_num + "Auction_Sys_AuctionRequests")	            			
	            	.convertBodyTo(String.class)
	            	.to("jms:queue:"  + Console.reboot_num + "Auction_Sys_SalesList");
	               
	                try {
	                	Thread.sleep(1000);
	                } catch (InterruptedException e) {
	                	e.printStackTrace();
	                }
	   
	            }
	        });
	        context.start();
	        Thread.sleep(10000);
	        context.stop();
	}

}
