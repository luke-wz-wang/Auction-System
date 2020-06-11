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

public class WatchRequestProcess {

	// handle auction requests from .csv files
	public static void handleRequests(Hashtable<Integer, Player> players, AuctionMarket market) throws Exception {
		
		 CamelContext context = new DefaultCamelContext();

	        // connect to ActiveMQ JMS broker listening on localhost on port 61616
	        ConnectionFactory connectionFactory = 
	        	new ActiveMQConnectionFactory("tcp://localhost:61616");
	        context.addComponent("jms",
	            JmsComponent.jmsComponentAutoAcknowledge(connectionFactory));
	        
	        // add our route to the CamelContext
	        context.addRoutes(new RouteBuilder() {
	            public void configure() {
	                from("file:data/watch?noop=true")
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
	                    	
	                    	int watcherId = Integer.parseInt(details[0].substring(1));
	                    	int sellerId = Integer.parseInt(details[1]);
	                    	String itemName = details[3];	     
	                    	
	                    	// actions
	                    	Player watcher = players.get(watcherId);
	                    	watcher.watch(sellerId);	                    		                    	           
	                    	
	                    	StringBuilder sb = new StringBuilder();
	                    	sb.append("Player" + watcherId + " is watching Player" + sellerId + "'s item: ");
	                    	sb.append(itemName + ".");
	                    	
	                    	ProducerTemplate template = context.createProducerTemplate();
	                    	template.sendBody("jms:queue:"  + Console.reboot_num + "Auction_Sys_Player" + watcherId, sb.toString());
	                    }
	                })
	                .to("jms:queue:"  + Console.reboot_num + "Auction_Sys_Watchers");
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
