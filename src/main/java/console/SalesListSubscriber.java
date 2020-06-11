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

import gameObject.Player;

public class SalesListSubscriber {
	
		// allow players who is interested to the auction to subscribe
		public static void subscribeToMarket(Hashtable<Integer, Player> players) throws Exception {
			
			 CamelContext context = new DefaultCamelContext();

		        // connect to ActiveMQ JMS broker listening on localhost on port 61616
		        ConnectionFactory connectionFactory = 
		        	new ActiveMQConnectionFactory("tcp://localhost:61616");
		        context.addComponent("jms",
		            JmsComponent.jmsComponentAutoAcknowledge(connectionFactory));
		        
		        // add our route to the CamelContext
		        context.addRoutes(new RouteBuilder() {
		            public void configure() {
		                from("file:data/subscribers?noop=true")
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
		                    	System.out.println(res[0]);
		                    	String[] details = res[0].split(" ");
		                    	int playerid = Integer.parseInt(details[1].substring(0,details[1].length()-1));
		                    	Player player = players.get(playerid);	                    	
		                    	// actions
		                    	player.subscribe();                 	
		                    }
		                })
		                .to("jms:queue:"  + Console.reboot_num + "Auction_Sys_RegisterSubscription");
		                //.to("jms:queue:dump");
		                try {
		                	Thread.sleep(1000);
		                } catch (InterruptedException e) {
		                	e.printStackTrace();
		                }
		   
		            }
		        });
		        context.start();
		        Thread.sleep(5000);
		        context.stop();
		}

}
