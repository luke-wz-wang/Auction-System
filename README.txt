
## General Topic

  Animal Crossing: New Horizons(ACNH) has thousands of items in the game and a limited amount of storage for each player. Players may feel forced to sell their items to Nook shop with a low pre-defined price when the available storage is running low. With an auction system in the game, players can place their items for sale, and others are allowed to bid. Apart from gaining more profits, players can acquire items they want or hard to acquire.


## EIP Patterns & Design Patterns

EIP Patterns:

1. Publish-Subscribe Channel: players can subscribe to the auction market and receive information of items for sale each week

2. Point-to-Point: each item will either sell to another player successfully or returned to the owner if no bid is placed

3. Request-Reply: sellers need to place request to sell their items, and only the first 5 sellers can sell their items with success reply confirmation

4. Event-Driver Consumer: the buyer will be called for transaction if they win the bid, and they are obligated to buy the item (may add a pre-paid mechanism)

5. Aggregator: the auction market will gather the sell requests on Monday, store them, and announce the final sell list on Tuesday

6. Competing Consumers: multiple players compete for the item on the market and potentially can receive it, and the market determines who will actually receive the item.

-----------------------------------------------------------------------------------------

Design Patterns:

1. Singleton: the auction market will be a singleton; players cannot sell the same item on multiple auction markets;

2. Mediator: all the players including sellers and buyers, the items as well as the transaction/delivery will rely on the market mediator;

3. Observer: players watch a certain item on the market, and will get notified if new bid is placed on the item;

4. Strategy: the currency in ACNH is called bells. The transactions can be made using bells as well as other items that sells for an equal amount of bells with a pre-defined price. The buyer will only receive bells, but the buyer can choose to pay with bells or items or even resources like golden nugget.


## Program Usage

To run the test, go to Console.java in package console and run the main method. To simplify the testing of the system, all the input are generated and stored as .csv files in data folder. 

To reboot the auction system (clear queues on activeMQ), simply change the reboot_num to a new number in Console.java.

-----------------------------------------------------------------------------------------

There are several breakpoints in order to break the entire auction process into several parts where the details can be observed in ActiveMQ; when reaching a breakpoint(asking for any input + enter to continue), you can check the new messages in the related queues described as follows.


Here are the breakpoints:

1) breakpoint to check initialization and subscription

2) breakpoint to check the sales list

3) breakpoint to check the watcher's registration

4) breakpoint to check updates to the watchers and bidding 

5) breakpoint to check the final calls for the item

6) check final output of the market's profits in console output

-----------------------------------------------------------------------------------------

The generated messages/output are sent to activeMQ:

Each player has its own queue, where all the auction request/reply, subscription, watch feed, as well as transaction information that are related to the player are stored; 

Topic: Auction_Sys_AuctionRequest: temporary stores the auction requests, and the market will accept top 5 auction requests;

Auction_Sys_Sales_List stores messages of the final sales on the market; 

Auction_Sys_RegisterSubscription lists players who subscribe to the market

Topic: Auction_Sys_AuctionRequest;

Auction_Sys_Watchers lists players who watch a single item on the market;

Auction_Sys_Bidding lists all the bid in the bidding process;

Auction_Sys_Final_Final_Call lists the final offer between seller and buyer;

Auction_Sys_Transaction lists the transaction status between seller and buyer.


