package console;

import auctionCenter.*;
import java.util.*;

import javax.jms.ConnectionFactory;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.camel.CamelContext;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.jms.JmsComponent;
import org.apache.camel.impl.DefaultCamelContext;

import gameObject.*;

public class Console {
	
	
	public static int reboot_num = 6;
	
	public static void main(String args[]) throws Exception{
		
		// take inputs(press any key + enter) from console to break the entire process into different stages 
		Scanner scan = new Scanner(System.in);
		
		// create the auction market singleton
		AuctionMarket market = AuctionMarket.getInstance();
		
		// store all the players in the auction system
		Hashtable<Integer, Player> allPlayers = new Hashtable<>();
		
		// initialize the auction system with players and an auction market
		initPlayers(allPlayers, market);
		
		// allow players who is interested to the auction to subscribe
		SalesListSubscriber.subscribeToMarket(allPlayers);		
		
		// allow processes to initialize the auction system
		Thread.sleep(10000);
		
		// breakpoint to check initialization and subscription
		System.out.println("breakpoint to check initialization and subscription in activeMQ: Press any key + enter to continue");
		scan.next();
		
		new Thread(){
			public void run(){
				try {
					// publish the sales list with selected auction requests
					SalesListPublisher.publishSalesList(market);
				} catch (Exception e) {					
					e.printStackTrace();
				}
			}
		}.start();
		
		Thread.sleep(1000);
		
		new Thread(){
			public void run(){
				try {
					// handle auction requests from .csv files
					AuctionRequest.requestAuctionStage(allPlayers);
				} catch (Exception e) {					
					e.printStackTrace();
				}
			}
		}.start();
		
		Thread.sleep(1000);
		
		// breakpoint to check the sales list
		System.out.println("breakpoint to check the sales list in activeMQ: Press any key + enter to continue");
		scan.next();		
		
		// handle watch requests from .csv files
		WatchRequestProcess.handleRequests(allPlayers, market);
		
		// breakpoint to check the watcher's registration
		System.out.println("breakpoint to check the watcher's registration in activeMQ: Press any key + enter to continue");
		scan.next();
		
		// handle bid offers from .csv files
		BiddingProcess.bidding(market);
		
		// breakpoint to check updates to the watchers and bidding 
		System.out.println("breakpoint to check updates to the watchers and bidding in activeMQ: Press any key + enter to continue");
		scan.next();
		
		// process all the bids and make the final call/find the bidder withs the highest offer
		BiddingProcess.finalCall(market);
		
		// breakpoint to check the final calls for the item
		System.out.println("breakpoint to check the final calls for the item in activeMQ: Press any key + enter to continue");
		scan.next();	
		
		// process the transactions between buyers and sellers
		TransactionStage.transact(allPlayers, market);
		
		scan.close();
		
		// check the market's profits
		System.out.println("The market's profit is " + market.getProfit() + ".");
			
	}
	
	// load initial players and their procession
	public static void initPlayers(Hashtable<Integer, Player> players, AuctionMarket market) throws Exception {
		
		 CamelContext context = new DefaultCamelContext();

	        // connect to ActiveMQ JMS broker listening on localhost on port 61616
	        ConnectionFactory connectionFactory = 
	        	new ActiveMQConnectionFactory("tcp://localhost:61616");
	        context.addComponent("jms",
	            JmsComponent.jmsComponentAutoAcknowledge(connectionFactory));
	        
	        // add our route to the CamelContext
	        context.addRoutes(new RouteBuilder() {
	            public void configure() {
	                from("file:data/players?noop=true")
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
	                    	String playerName = details[0].substring(1);
	                    	String transacStyle = details[4].substring(0,details[4].length() - 1);
	                    	Player tmpPlayer = new Player(playerid, playerName);
	                    	tmpPlayer.setTransactionStyle(transacStyle);
	                    	Item tmpItem = new Item(details[2], Integer.parseInt(details[3]));
	                    	// actions
	                    	tmpPlayer.registerWithAuctioner(market.auctioner);
	                    	tmpPlayer.acquireItem(tmpItem);
	                    	players.put(playerid, tmpPlayer);
	                    	
	                    }
	                })
	                .to("jms:queue:" + reboot_num + "Auction_Sys_Players");
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
