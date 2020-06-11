package auctionCenter;

import gameObject.*;
import javafx.util.Pair;

import java.util.*;
import message.*;
import notificationCenter.*;

public class Auctioner {
	
	public List<Player> subscribers;
	
	public HashMap<Player, List<Item>> salesList;
	
	private List<Player> sellers;
	
	private NotificationCenter nfCenter;

	// key represents sellerId, value represents the current bidder with the highest offer
	public Hashtable<Integer, Pair<Integer, Integer>> offers;
	
	// key represents sellerId, value represents a list of watchers
	public Hashtable<Integer, List<Player>> watchers;
	
	// key represents sellerId, value represents the item to be sold
	public Hashtable<Integer, Item> itemsToSell;
	
	public Auctioner(){
		subscribers = new ArrayList<>();
		salesList = new HashMap<>();
		sellers = new ArrayList<>();
		nfCenter = new NotificationCenter();
		offers = new Hashtable<Integer, Pair<Integer, Integer>>();
		itemsToSell = new Hashtable<Integer, Item>();
		watchers = new Hashtable<Integer, List<Player>>();
	}
	
	// add a player to subscriber list
	public void addSubscriber(Player player) {
		if(!subscribers.contains(player)) {
			subscribers.add(player);
		}
	}
	
	// remove a player from subscriber list
	public void removeSubscriber(Player player) {
		// check if player has subscribed before
		if(subscribers.contains(player)) {
			subscribers.remove(player);
		}
	}
	
	// add a player to the seller list
	public boolean addSeller(Player player) {
		// limit seller amount to #
		if(sellers.size() < 5) {
			sellers.add(player);
			return true;
		}
		return false;
	}
	
	// add a watcher to the watcher list
	public void addWatcher(Player watcher, int sellerId) {
		if(watchers.containsKey(sellerId)) {
			List<Player> list = watchers.get(sellerId);
			list.add(watcher);
			watchers.put(sellerId, list);
		}else {
			List<Player> list = new ArrayList<Player>();
			list.add(watcher);
			watchers.put(sellerId, list);
		}
	}
	
	// put the item and seller to the sales list
	public void putOnSalesList(Player player, Item item) {
		if(salesList.containsKey(player)) {
			List<Item> itemList = salesList.get(player);
			itemList.add(item);
			salesList.put(player, itemList);
		}else {
			List<Item> itemList = new ArrayList<Item>();
			itemList.add(item);
			salesList.put(player, itemList);
		}
		
	}
	
	// handle the auction request message from a potential seller
	public AuctionReply handleRequest(Message msg,Player player, Item item) {
		boolean success = addSeller(player);
		AuctionReply reply;
		if(success) {
			putOnSalesList(player, item);
			reply = nfCenter.generateSellRequestSuccessMsg(msg.getId());
		}else {
			reply = nfCenter.generateSellRequestFailMsg(msg.getId());
		}
		return reply;
		
	}
		
}
