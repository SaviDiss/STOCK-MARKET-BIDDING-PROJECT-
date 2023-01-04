import java.io.IOException;

public class Main {

	

	
    public static void main(String [] args) throws IOException {


    	
       
       
          
        if(args.length!=0) { 
        	int temp= Integer.parseInt(args[0]);
        	Server.bid_time+=(temp*10000);
        }
        
        else
        	Server.bid_time+=60000;
        float limit =(((Server.bid_time-System.currentTimeMillis())/(float)60000));
        System.out.println("Sytem started with the time limit "+Math.round(limit)+" minutes");
        
        

        ItemMap item_map = new ItemMap();   // Datastructure
    
        CSVReader csvreader = new CSVReader("stocks.csv", item_map);    // CSVreader
        csvreader.read();   //item_map gets populated with data
        

        
        Server server = new Server(item_map);   	// Server
        server.start();       // Server starts running here.
        	
      
        
        

    }
}
