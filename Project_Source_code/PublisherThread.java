import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Timer;
import java.util.TimerTask;

public class PublisherThread implements Runnable{
	int i,j;
	Socket publisher_socket;
	String  request,request2;
	String msg,Id;
	ItemMap it;
	float result;
	float price [];
	float[] profit;
	String symbols_profit [];
	String symbols_price[];
	String qry [];
	boolean loop=true,bid=true,timeout=false,login=false;
	long time=Server.bid_time;
	ArrayList<String> symbollist;
	
	
	
	
	
	
	
	
	public PublisherThread(Socket client,ItemMap it) {
		this.it=it;
		this.publisher_socket=client;
		

	}



	@Override
	public void run() {
		// TODO Auto-generated method stub
	

        try {
        	
        	PrintWriter out = new  PrintWriter(publisher_socket.getOutputStream(),true);
            BufferedReader in =new BufferedReader(new InputStreamReader(publisher_socket.getInputStream()));
           
            
        	
        	
        	
            

        	
        	while(loop) {

        		if(login==false) {
        		out.println("please enter your id to register ?");
                Id=in.readLine();
                if(Id.equalsIgnoreCase("off"))
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
        		
      
if(bid) {
            msg=in.readLine();
        	qry=msg.split("\\s+");
    
        if(!msg.equalsIgnoreCase("-1")) {
        	synchronized(it) {
        	result=it.profit_update(qry[0], Integer.parseInt(qry[1]),Float.parseFloat(qry[2]));
        	out.println(result);
        	if(result==-1.0)
        		continue;
        	else
        		System.out.println("[prft] "+qry[0]+" "+qry[2]);
        	it.notifyAll();
        	}
        }
        
        else if( msg.equalsIgnoreCase("-1")) 
			 {
				bid=false;
					request=in.readLine();
				
						if(!request.equals("-1")) 
					{
							symbols_profit =request.split("\\s+");
							profit = new float[(symbols_profit.length -1)];
							j=1;
							System.out.println(request);
							for( i=0;i<(symbols_profit.length)-1;i++) 
							{
								profit[i]=it.get(symbols_profit[j]).getprofit();
								//System.out.println(symbols_profit[j]+profit[i]); debug line for checking user entered symbols
								j++;
							
							}
							Server.checkarray(profit, out);
					}
				
					request2=in.readLine();
					
						if(!request2.equals("-1"))
						{
							bid=false;
							symbols_price =request2.split("\\s+");
							price = new float[(symbols_price.length -1)];
							j=1;
							System.out.println(request2);
							for( i=0;i<(symbols_price.length)-1;i++) 
							{
								price[i]=it.getprice(symbols_price[j]);
								//System.out.println(symbols_price[j]+price[i]); debug line for checking user entered symbols
								j++;
							
							}
							Server.checkarray(price, out);
						}	
						
						
					}     
}	
else if(!bid ) {
	
	//out.println("in waiting....");
Timer timer=new Timer();
TimerTask checktime=new TimerTask() {
public void run() {
	if(System.currentTimeMillis()>=time) {
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

if(!request.equals("-1"))
{
j=1; 
for(i=0;i<(symbols_profit.length)-1;i++) 
{
if(profit[i]<it.get(symbols_profit[j]).getprofit())
{
profit[i]=it.get(symbols_profit[j]).getprofit();
out.println(symbols_profit[j]+" "+"Profit"+" "+profit[i]);
}
j++;	
}
}
if(!request2.equals("-1")) {
j=1; 
for(i=0;i<(symbols_price.length)-1;i++) 
{
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
// TODO Auto-generated catch block
e.printStackTrace();
}


timer.cancel();	
}

}		

        	
        
        	}
        		
      
        	 out.println(-2.0);
        }
        
	    catch (IOException e) {
        		System.out.println("IO expception detected....");
        }
        
        finally {
        	try {
				publisher_socket.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
        }
        System.out.println("loop exited");
        
	}
}



	



