package gameObject;

import java.util.*;

import javax.jms.ConnectionFactory;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.camel.CamelContext;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.component.jms.JmsComponent;
import org.apache.camel.impl.DefaultCamelContext;

import auctionCenter.Auctioner;
import console.Console;
import message.*;

public class Player implements Trader{
	
	private int id;
	private String name;
	private int bellBalance;
	// item list
	private List<Item> procession;
	private Auctioner auctioner;
	public String transacStyle;	
	
	public Player(int id, String name){
		this.id = id;
		this.name = name;
		this.bellBalance = 50000;
		this.procession = new ArrayList<Item>();
	}
	
	public void setTransactionStyle(String style) {
		this.transacStyle = style;
	}
	
	public void registerWithAuctioner(Auctioner auctioner) {
		this.auctioner = auctioner;
	}
	
	// add an item to the players' procession list
	public void acquireItem(Item item) {
		procession.add(item);
	}
	
	// remove an item from the players' procession list
	public void removeItem(Item item) {
		for(int i = 0; i < procession.size(); i++) {
			if(procession.get(i).getName().equals(item.getName())) {
				procession.remove(i);
				break;
			}
		}
	}
	
	// pay a certain amount of bells
	public boolean payWithBell(int price) {
		if(price > bellBalance) {
			return false;
		}else {
			bellBalance -= price;
			return true;
		}
	}
	
	// trade the procession/items into bells
	public int payWithItem() {
		int sum = 0;
		for(int i = 0; i < procession.size(); i++) {
			sum += procession.get(i).getValue();
		}
		procession = new ArrayList<Item>();
		return sum;
	}
	
	// receive bells 
	public void recievePayment(int amount) {
		bellBalance += amount;
	}
	
	// request to sell item; return true if request is successful
	@Override
	public boolean requestAuction(Item item) {
		String payload = "Request to sell " + item.getName() + ".";
		String requestId = id + " " + java.util.Calendar.getInstance().getTime();
		AuctionRequest request = new AuctionRequest(requestId, payload);
		
		CamelContext context = new DefaultCamelContext();
		ConnectionFactory connectionFactory = new ActiveMQConnectionFactory("tcp://localhost:61616");
		context.addComponent("jms",JmsComponent.jmsComponentAutoAcknowledge(connectionFactory));
		
		// send msg to MQ
		StringBuilder sb = new StringBuilder();
		sb.append(this.name + " " + this.id + " send out auction request as follows:");
		sb.append(request.toString());
		
		ProducerTemplate template = context.createProducerTemplate();
    	template.sendBody("jms:queue:"  + Console.reboot_num + "Auction_Sys_Player" + id, sb.toString());
    	
    	// send msg to console
		System.out.println("*****--------------------------------------------------");
		System.out.println(this.name + " " + this.id + " send out auction request as follows:");
		request.displayMessage();
		AuctionReply reply = auctioner.handleRequest(request, this, item);
		System.out.println("*****--------------------------------------------------");
		return receiveRequestResult(reply);
		
	}
	
	// receive auction request
	public boolean receiveRequestResult(AuctionReply reply) {
		
		CamelContext context = new DefaultCamelContext();
		ConnectionFactory connectionFactory = new ActiveMQConnectionFactory("tcp://localhost:61616");
		context.addComponent("jms",JmsComponent.jmsComponentAutoAcknowledge(connectionFactory));
		
		StringBuilder sb = new StringBuilder();
		sb.append(this.name + " " + this.id + " received request reply message as follows:\n");
		sb.append(reply.toString());
		
		// send msg to MQ
		ProducerTemplate template = context.createProducerTemplate();
    	template.sendBody("jms:queue:"  + Console.reboot_num + "Auction_Sys_Player" + id, sb.toString());
		
    	// send msg to console
		System.out.println("------------------------------------------------------");
		System.out.println(this.name + " " + this.id + " received request reply message as follows:");
		reply.displayMessage();
		System.out.println("------------------------------------------------------");
		return reply.isSuccess();
	}
	
	// subscribe to market via auctioner
	@Override
	public void subscribe() {
		auctioner.addSubscriber(this);
	}
	
	// unsubscribe 
	@Override
	public void unsubscribe() {
		auctioner.removeSubscriber(this);
	}
	
	// watch a seller's item
	@Override
	public void watch(int sellerId) {
		auctioner.addWatcher(this, sellerId);
	}
	
	// receive updates from item the player is watching
	public void updatesFromWatchList(int sellerId, Item item, int offer) {
		CamelContext context = new DefaultCamelContext();
		ConnectionFactory connectionFactory = new ActiveMQConnectionFactory("tcp://localhost:61616");
		context.addComponent("jms",JmsComponent.jmsComponentAutoAcknowledge(connectionFactory));
		
		StringBuilder sb = new StringBuilder();
		sb.append("News from watch item: " + item.getName());
		sb.append(" . Its new bid is " + offer + ".");
		
		ProducerTemplate template = context.createProducerTemplate();
    	template.sendBody("jms:queue:"  + Console.reboot_num + "Auction_Sys_Player" + id, sb.toString());
	}	
	
	public int getId() {
		return id;
	}
	
	public int getBalance() {
		return bellBalance;
	}	

}
