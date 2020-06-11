package console;

import java.util.Hashtable;

import javax.jms.ConnectionFactory;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.camel.CamelContext;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.jms.JmsComponent;
import org.apache.camel.impl.DefaultCamelContext;

import gameObject.Item;
import gameObject.Player;

public class AuctionRequest {
	
		// handle auction requests from .csv files
		public static void requestAuctionStage(Hashtable<Integer, Player> players) throws Exception {
			
			 CamelContext context = new DefaultCamelContext();

		        // connect to ActiveMQ JMS broker listening on localhost on port 61616
		        ConnectionFactory connectionFactory = 
		        	new ActiveMQConnectionFactory("tcp://localhost:61616");
		        context.addComponent("jms",
		            JmsComponent.jmsComponentAutoAcknowledge(connectionFactory));
		        
		        // add our route to the CamelContext
		        context.addRoutes(new RouteBuilder() {
		            public void configure() {
		            	            	
		                from("file:data/auctionRequest?noop=true")
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
		                    	int playerid = Integer.parseInt(details[1]);
		                    	Player player = players.get(playerid);
		                    	Item tmpItem = new Item(details[2], Integer.parseInt(details[3].substring(0,details[3].length() - 1)));
		                    	// actions
		                    	StringBuilder sb = new StringBuilder();
		                    	sb.append(res[0] + " ");
		                    	sb.append(player.requestAuction(tmpItem));
		                    	exchange.getIn().setBody(sb.toString());	
		                    }
		                })
		                .choice()
		                	.when(body().contains("true"))
		                .to("jms:topic:" + Console.reboot_num + "Auction_Sys_AuctionRequests");
		                //.to("jms:queue:dump");
		                try {
		                	Thread.sleep(1000);
		                } catch (InterruptedException e) {
		                	e.printStackTrace();
		                }
		   
		            }

		        });
		        context.start();
		        Thread.sleep(2000);
		        context.stop();
		}	

}
