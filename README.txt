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


