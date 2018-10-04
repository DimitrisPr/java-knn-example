class PointData {
	public double[] x;
	public double y;

	public PointData(double[] x, double y) {
		this.x = x.clone(); //Copy value don't reference!
		this.y = y;
	}
	
	public double dist(double[] x) {
		
		double distance = 0;
		for (int i = 0; i < x.length; ++i) {
			 distance += Math.sqrt( Math.abs(this.x[i]-x[i]) * Math.abs(this.x[i]-x[i]));
		}
		
		return distance;
	}
}

class Node {
	public double score;
	public PointData ptd;
	public Node next;

	public Node(double[] x, double y) {
		this.score = 0;
		this.ptd = new PointData(x, y); //Create a new PointData for each x
		this.next = null;
	}
}

class PointList {
	
	private Node head, tail;
	private int dimension=0, length;
	
    public PointList(int dim) {
    	this.head = null;
        this.tail = null;
    	this.dimension = dim;
    	this.length = 0;
    }

    public static PointList readData(String filename) {
        PointList list = null;
        java.io.BufferedReader br = null;
        try {
            br = new java.io.BufferedReader(new java.io.FileReader(filename));
            String line = br.readLine();
            String[] data = line.split(" ");
            int m = Integer.parseInt(data[0]);
            int n = Integer.parseInt(data[1]);
	    list = new PointList(n);

            double[] x = new double[n];
            while((line = br.readLine()) != null) {
                data = line.split(" ");
                for(int j = 0; j < n; j++) 
                    x[j] = Double.parseDouble(data[j]);
                list.append(x, Double.parseDouble(data[n]));
            }
        } catch (java.io.IOException e) {
            e.printStackTrace();
        } finally {
            try { if (br != null) br.close(); }
            catch (java.io.IOException ex) { ex.printStackTrace(); }
        }
	return(list);
    }

	public int getDim() {
		return dimension;
	}

	public int length() {
		return length;
	}

    public boolean append(double[] x, double y) {
    	
    	if (this.dimension == x.length) { 
	    	Node newNode = new Node(x,y);         
	    	
	        if( this.head == null && this.tail == null ) {     
				this.head = newNode;                
				this.tail = newNode;
	        }
	        else {
		        this.tail.next = newNode;             
		        this.tail = this.tail.next;      
	        }
	        
	        this.length++;
	        return true;
    	}
    	else {
    		return false;
    	}
    }

    public boolean append(PointList list) {
    	
    	if (this.dimension == list.dimension) { 
			
    		Node current = list.head;
    		int temp_length = list.length; 

    		for ( int i=0; i<temp_length; ++i ) { 
    			this.append(current.ptd.x.clone(),current.ptd.y);
    			current = current.next;
    		}

    		list.length = temp_length;
	    	
	    	return true;
    	}
    	else {
    		return false;
    		
    	}
    }
    
    public PointData rmFirst() {
    	if ( this.head != null ) {
	    	Node temp = this.head;
	    	this.head = this.head.next;
	    	length--;
	    	return temp.ptd;
    	}
    	else {
    		return null;
    	}
    }

	public void shuffle() {
		Node node = this.head;
		//Fisherβ€“Yates shuffle, Modern Edition
		for (int i = 0; i < this.length; ++i) {
			int j = (int) (Math.random() * (this.length - i + 1) );
			swapNodes(i, j, node);
			node = node.next;
		}
		
	}
	
	//Used in shuffle()
	private boolean swapNodes(int i, int j, Node node) {
		
		Node swap = head, tmp = new Node(new double[dimension], 0);
	
		if (i != j) { //If nodes are different, swap them
			
			//Find node at position j
			for (int l=0; l<j; ++l) {
				swap = swap.next;
			}
	
			// Swap the PointData of Node i & Node j
			tmp.ptd = node.ptd;
			node.ptd = swap.ptd;
			swap.ptd = tmp.ptd;
	
			return true;
		} 
		else { 
			//If Node i is the same as Node j
			return false;
		}
		
	}

    public PointData rmNearest(double[] x) {
    	
    	if ( this.head != null) {
    		Node prev = this.head, current = this.head, nearest = this.head;
    		double distance = current.ptd.dist(x);
	    	//Scan list to find & remove nearest
	    	while( current != null) {
	    		if ( current.ptd.dist(x) < distance ) {
	    			distance = current.ptd.dist(x);
	    			nearest = current;
	    			//Remove it from the list 
	    			prev.next = current.next;
	    		}
	    		else {
	    			prev = current;
	    		}
	    		
	    		current = current.next;
	    	}
	    	
	    	if ( nearest == this.head ) {
	    		//Remove head
	    		this.head = this.head.next;
	    	}
	    	return nearest.ptd;
    	}
    	else {
    		return null;
    	}
}

    public PointList findKNearest(double[] x, int k) {
    	
    	PointList KNN = new PointList(dimension);
    	for ( int i=0; i<k; ++i ) {
    		PointData NNptd = rmNearest(x);
    		KNN.append(NNptd.x,NNptd.y);
    	}
    	
    	return KNN;
    }

    public double classify(double[] x) {
    	Node KNNcurrent = this.head;
    	PointList c = new PointList(dimension); //Category List
    	
    	while ( KNNcurrent != null ) { //Go through KNN list
    		KNNcurrent.score = KNNcurrent.ptd.dist(x); //Calculate each score
    		create_category_list_and_add_weights(c, KNNcurrent);
    		KNNcurrent = KNNcurrent.next;
    	}
    	
    	return the_category_with_the_smallest_weight(c);
    }
    
    private void create_category_list_and_add_weights(PointList c , Node KNNcurrent) { 
    	//Adds all KNN categories to a list and increases weights
    	 
    	Node node = new Node(KNNcurrent.ptd.x.clone(),KNNcurrent.ptd.y), current=c.head;
    	node.score = 1 / KNNcurrent.score;
    	boolean found = false;
    	
    	if( current == null ) {     //If it's the first node
			c.head = node;                //1st node is head & tail
			c.tail = node;
			c.length++;
        }
        else {
        	//Search for cat
        	while ( current != null && found == false ) {
        		if ( node.ptd.y == current.ptd.y ) {
        			found = true;
        			break;
        		}
        		current = current.next;
        	}
        	//If category exists just add weight
        	if ( found == true ) {
        		current.score += node.score;
        	}
        	else { //Add category
        		c.tail.next = node;             //add node to the back
    	        c.tail = c.tail.next;       //reset tail to last node
    	        c.length++;
        	}
        }
    }
    
    private double the_category_with_the_smallest_weight(PointList c) { 
    	//Finds and returns the category with the smallest weight
    	
    	Node current = c.head;
    	double smallest_weight = c.head.score, category = c.head.ptd.y;
    	
    	while ( current != null ) {
    		if ( current.score < smallest_weight ) {
    			smallest_weight = current.score;
    			category = current.ptd.y;
    		}
    		current = current.next;
    	}
    	
    	return category;
    }
    
    //DEBUG ONLY
    public void print_list() {
    	
    	Node node = this.head;
    	
		while( node != null) {
			
			for (int y = 0; y < dimension; ++y) {
				System.out.print("x[" + y + "] = " + node.ptd.x[y] + ", ");
			}
			System.out.print(node.ptd.x + "\n");
			
			node = node.next;
		}
    }
}

public class KNN {
    public static void main(String[] args) {
    	
    	String filename = "wine.dat";
    	while ( filename != null ) { //For each file do the process
    		System.out.print(
    				"\nFile: " + filename + 
    				"\n________________\n________________\n\n");
    		
	    	for (int k=1; k<=10; ++k) {
	    		
	    		PointList big = PointList.readData(filename), 
	        			small = new PointList(big.getDim());
	    		
	    		big.shuffle(); 
	    		
	    		/*** Create Big & Small List ***/
	    		// 	 By removing the 25% of node
	    		// 	 From big and putting them
	    		//   Into small
	    		
	    		int small_length = (int)Math.floor(big.length()*25/100)+1; //The length of the small list
	    		
	    		for (int i=0; i<small_length; ++i) { //Create small & ibg
	    			PointData ptd = big.rmFirst();
	    			small.append(ptd.x.clone(),ptd.y);
	    		}
	    		
	    		PointList temp_big = new PointList(big.getDim()); //Back-up big list for later use
	    		temp_big.append(big); //Back-up big list for later use
	    		
	    		/*** KNN Algorithm ***/
	    		//Initialize
	    		PointData ptd = small.rmFirst();
	    		int errors = 0;
	    		
	    		//Begin KNN
	    		while ( ptd != null ) { //For every element in the small list
	    			double initial_class = ptd.y; //The real class
	    			double calculated_class = big.findKNearest(ptd.x, k).classify(ptd.x); //The calculated class
	    			
	    			if ( initial_class != calculated_class ) 
	    			{
	    				errors++;
	    			}
	    			
	    			ptd = small.rmFirst(); //Prepare next element for classification 
	    			
	    			//Restore Big from its back-up since some of its values have been removed before
	    			big = new PointList(temp_big.getDim());
	    			big.append(temp_big);
	    		}

	    		double mean_accuracy =  ( (double)(small_length - errors)/ small_length) * 100;
	    				
	    		System.out.print("For k="+k+", main accuracy is " + Math.round(mean_accuracy) + "%\n---\n" );
	    		errors = 0; //Restore errors for next loop
	    	}
	    	
	    	if ( filename == "wine.dat") {
	    		filename = "housing.dat";
	    	}
	    	else {
	    		filename = null;
	    	}
    	}
    }
    
}

