import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeoutException;

public class ClientThread implements Runnable{
	
	int i,j;
	Socket Client_socket;
	String  request,request2;
	String msg,Id;
	ItemMap it;
	float reply_message;
	float price [];
	float[] profit;
	String symbols_profit [];
	String symbols_price[];
	String qry [];
	boolean loop,bid,timeout,login;
	long time;
	ArrayList<String> symbollist;
	
	
	
	
	
	
	
	
	
	public ClientThread(Socket client,ItemMap it) {
		this.it=it;
		this.Client_socket=client;
		symbollist=new ArrayList<String>();
		time=Server.bid_time;
		loop=true;
		bid=true;
		timeout=false;
		login=false;
	}



	@Override
	public void run() {
		// TODO Auto-generated method stub
	
	
        try {
        	
        	PrintWriter out = new  PrintWriter(Client_socket.getOutputStream(),true);
            BufferedReader in =new BufferedReader(new InputStreamReader(Client_socket.getInputStream()));
            

        	
        	
        		while(loop) 
        	{
        	
        			if(login==false) {
        				try {
                		out.println("enter your id to register ?");
                        Id=in.readLine();
                        if(Id.equalsIgnoreCase("off") ||System.currentTimeMillis()>=time )
                        	break;
                    synchronized(Server.mapId) {
                        if(!Server.mapId.containsKey(Integer.parseInt(Id))) {
                        	Server.mapId.put(Integer.parseInt(Id),symbollist);
                        	
                        	login=true;
                        	out.println(0);
                        }
                        else
                        {
                        	out.println(-1.0);
                        	continue;
                        }
                    }
        				}
        				catch(Exception e) {
        					out.println(-1.0);//invalid entry of login id 
        					continue;
        				}
              	}
                		
        			
        			
        	Timer sheduler =new Timer();
        	TimerTask task=new TimerTask() {
        		public void run() 
        		{
        			if(msg==null)
        				timeout=true;
        		}
        	};
        	
        	
        			
        		if(bid )
        			{	
        				msg=null;
        	        	timeout=false;
        	        	sheduler.schedule(task, 10000);
        				msg=in.readLine();
        				sheduler.cancel();
        				
        				if(timeout==true) 
        				{
        					out.println(-2.0);
        					continue;	
        				}
        				
        				// here we are entering to the subscription section to get notification on the bids 
        				 	if( msg.equalsIgnoreCase("-1")) 
        					 {
        						bid=false;
        							request=in.readLine();
        						
        								if(!request.equals("-1")) //profit section
        							{
        									symbols_profit =request.split("\\s+");
        									profit = new float[(symbols_profit.length -1)];
    										j=1;
    										System.out.println(request);
    										for( i=0;i<(symbols_profit.length)-1;i++) 
    										{
    											profit[i]=it.get(symbols_profit[j]).getprofit();
    											//System.out.println(symbols_profit[j]+profit[i]);
    											j++;
    										
    										}
    										Server.checkarray(profit, out);
        							}
        						
        							request2=in.readLine();
        							
        								if(!request2.equals("-1"))//price section
        								{
        									symbols_price =request2.split("\\s+");
        									price = new float[(symbols_price.length -1)];
        									j=1;
        									System.out.println(request2);
        									for( i=0;i<(symbols_price.length)-1;i++) 
        									{
        										price[i]=it.getprice(symbols_price[j]);
        										//System.out.println(symbols_price[j]+price[i]);
        										j++;
        									}
        									Server.checkarray(price, out);
        								}	
        								
        								
        								}
    										
        								
        							     
        				//bidding section----> here we are performing the task to update or check current price
        				
                		else if(!msg.equalsIgnoreCase("-1")) 
 				 		{
                		  
                			if(time>System.currentTimeMillis()) // under the bidding time 
                			{
        					synchronized(it) 
        						{			
        				 			qry=msg.split("\\s+");	
        							if(qry.length==2) {
        								reply_message=it.make_bid(qry[0], Float.parseFloat(qry[1]),Integer.parseInt(Id));
        								symbollist.add(qry[1]);
        								Server.mapId.replace(Integer.parseInt(Id),symbollist);//user id and the bid symbol will be placed as a record for a particular user 
        								out.println(reply_message);							//reply with updated price or -1 depending on the query 
        								if(reply_message!=-1.0)
        									System.out.println("[price] "+qry[0]+" "+reply_message);
        							}
        							else if(qry.length==1) {
        								reply_message=it.getprice(qry[0]);//reply with the current price of the symbol 
        								out.println(reply_message);
        							}
        							
        							else {
        								reply_message= (float) -1.0;  //reply for invalid bid/symbol					
        								out.println(reply_message);
        							}
        								if(reply_message==-1.0)
        								continue;
        							
        							
        								time=it.get(qry[0]).gettime();
        							if(System.currentTimeMillis()>(time-60000) && System.currentTimeMillis()<time) //checks time and updates time for the last 60 second bids 
        							{
        								System.out.println("time extended by 60 seconds due to the customer entry at last 60seconds ");// this function can be extended to any time limit
        								time=time+60000;
        								it.get(qry[0]).settime(time);
        							}
        							
        							it.notifyAll();							
        				 		}
                			}
                			
                			
                				else				// if timeup the server should disconnect the client to discard bidding 
                					loop=false; // this variable will break the loop and disconnect the client s
 				 		}
        	
        		
        			}
        		
        		//after subscription completed we are checking for a update here 
        	else if(!bid ) {
        		
        		//	out.println("in waiting....");
        		Timer timer=new Timer();
        	  TimerTask checktime=new TimerTask() {
        		public void run() {
        			if(System.currentTimeMillis()>=time) {// this allows to check for the time while waiting in the notification section 
        				synchronized(it) {
        				it.notifyAll();
        				}
        				
        				}
        				
        		}
        	};
        	timer.scheduleAtFixedRate(checktime, 0,1000);     
        	
        	synchronized(it) {
        	try {
			it.wait();

        	if(!request.equals("-1")) // checks for the profit changes 
        	{
        	j=1; 
        	for(i=0;i<(symbols_profit.length)-1;i++) //this loop checks for the profit update by comparing old profit  
        	{
        		if(it.containsKey(symbols_profit[j])==false)
        			continue;
        		
        	if(profit[i]<it.get(symbols_profit[j]).getprofit())
        		{
        		profit[i]=it.get(symbols_profit[j]).getprofit();
        		out.println(symbols_profit[j]+" "+"Profit"+" "+profit[i]);
        		}
        		j++;	
        		}
        	}
        	
        	if(!request2.equals("-1")) {//checks for the price changes 
        	j=1; 
        	for(i=0;i<(symbols_price.length)-1;i++) //this loop checks for the price update by comparing old price 
        	{
        		if(it.containsKey(symbols_price[j])==false)
        			continue;
        		
        	if(price[i]<it.get(symbols_price[j]).get_price())
        		{
        		price[i]=it.get(symbols_price[j]).get_price();
        		out.println(symbols_price[j]+" "+"Price"+" "+price[i]);
        		}
        		j++;	
        		}
        	}
        	
        	if(time<=System.currentTimeMillis())
        		loop=false;
				}
        	
        	catch (InterruptedException e)
        	{
			
				e.printStackTrace();
			}
        	
        	
            	
        	timer.cancel();	
        	}
        	
         }		
        		out.flush();
       }
          
        
        out.println(-2.0);// timeout 
        }
        
	
        
     
	
        catch (IOException e) {
        		System.out.println("IO expception Found......");
        }
        
        finally {
        	try {        		 
				Client_socket.close();
				System.out.println("client disconnected .............");
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} 
        }
        
       
        
	}


	
	
	

}



	



