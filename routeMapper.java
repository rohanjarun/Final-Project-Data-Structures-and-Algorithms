import java.util.*;

import java.io.*;

import java.lang.*;

public class RouteMapper{

	private Graph routeMap;

	private Map<String, String[]> attractions = new HashMap<String, String[]>();

	public RouteMapper(File attractions, File roads, int size){

		routeMap = new Graph(size);

		initGraph(roads);

		initGraphEdges(roads);

		initAttractions(attractions);
	}

	private void initGraph(File roads){ // adds all the cities + states to nodes in graph

		Set<String[]> cityTracker = new HashSet<String[]>();

		BufferedReader csvReader;

		try{
			csvReader = new BufferedReader(new FileReader(roads));
			String row;

			try{
				while((row = csvReader.readLine()) != null){
					String [] data = row.split(",");
					String [] cityState1 = data[0].split(" ");
					String [] cityState2 = data[1].split(" ");

					if(cityState1.length > 2){
						for(int i = 1; i < cityState1.length - 1; i ++){
							cityState1[0] = cityState1[0] + " " + cityState1[i];
						}
						cityState1[1] = cityState1[cityState1.length-1];
					}
					if(cityState2.length > 2){
						for(int i = 1; i < cityState2.length - 1; i ++){
							cityState2[0] = cityState2[0] + " " + cityState2[i];
						}

						cityState2[1] = cityState2[cityState2.length-1];
					}



					if(!cityTracker.contains(cityState1)){
						this.routeMap.addNode(cityState1[0], cityState1[1]);
						cityTracker.add(cityState1);
					}
					if(!cityTracker.contains(cityState2)){
						this.routeMap.addNode(cityState2[0], cityState2[1]);
						cityTracker.add(cityState2);
					}
				}
			}catch(IOException c){
				System.out.println(c);
			}
		}catch(FileNotFoundException e){
			System.out.println(e);
		}
	}

	private void initGraphEdges(File roads){ // makes the edge weights for all nodes

		BufferedReader csvReader;

		try{
			csvReader = new BufferedReader(new FileReader(roads));
			String row;

			try{
				while((row = csvReader.readLine()) != null){
					String [] data = row.split(",");
					String [] cityState1 = data[0].split(" ");
					String [] cityState2 = data[1].split(" ");

					if(cityState1.length > 2){
						for(int i = 1; i < cityState1.length - 1; i ++){
							cityState1[0] = cityState1[0] + " " + cityState1[i];
						}
						cityState1[1] = cityState1[cityState1.length-1];
					}
					if(cityState2.length > 2){
						for(int i = 1; i < cityState2.length - 1; i ++){
							cityState2[0] = cityState2[0] + " " + cityState2[i];
						}
						cityState2[1] = cityState2[cityState2.length-1];
					}

					this.routeMap.addEdge(cityState1[0], cityState1[1], cityState2[0], cityState2[1], Integer.parseInt(data[2]), Integer.parseInt(data[3]));
				}
			}catch(IOException c){
				System.out.println(c);
			}
		}catch(FileNotFoundException e){
			System.out.println(e);
		}
		
	}

	private void initAttractions(File attractions){ // maps attractions
		BufferedReader csvReader;
		try{
			csvReader = new BufferedReader(new FileReader(attractions));
			String row; 
			try{
				while((row = csvReader.readLine()) != null){
					String [] data = row.split(",");
					String [] cityState = data[1].split(" ");

					if(cityState.length > 2){
						for(int i = 1; i < cityState.length - 1; i ++){
							cityState[0] = cityState[0] + " " + cityState[i];
						}
						cityState[1] = cityState[cityState.length-1];
					}

					this.attractions.put(data[0], cityState);
				}
			}catch(IOException c){
				System.out.println(c);
			}
		}catch(FileNotFoundException e){
			System.out.println(e);
		}
	}

	public List<String> route(String startingCity, String startingState, String endingCity, String endingState, List<String> attractions){ //constructs a hashmap of all locations needed to be visited and passes
		List<String> route;																												// it on to shortestPath()
		
		HashMap<Integer, String> h = new HashMap<Integer, String>();
		int startingIndex = routeMap.getIndex(startingCity + " " + startingState);

		for(int i = 0; i < attractions.size(); i ++){
			String [] location = this.attractions.get(attractions.get(i));
			String locationString = location[0] + " " + location[1];
			int index = routeMap.getIndex(locationString);
			h.put(index, locationString);
		}
		int endingIndex = routeMap.getIndex(endingCity + " " + endingState);
		h.put(endingIndex, endingCity + " " + endingState);
		
		route = shortestPath(startingIndex, h); 
		return route;

	}

	private List<String> shortestPath(int start, HashMap<Integer, String> attractions){ // calls dijkstras on locations in hashmap 
			List<String> route = new ArrayList<String>(); // get an array "parents" that can be used to trace back the path taken on shortest path to next destination
														
		
// makes a new queue of all those trace backs 
		int [] parents = new int[routeMap.size];

		Queue<String> s = new LinkedList<String>();
		int startingIndex = start;


		while(!attractions.isEmpty()){
			Pair p = dijkstras(startingIndex, parents, attractions);
			int endingIndex = p.getIndex();
			parents = p.getParents();
			walkParents(s, endingIndex, parents);
		}

		while(!s.isEmpty()){
			route.add(s.remove());
		}

		return route;
	}

	private void walkParents(Queue<String> s, int end, int [] parents){ // recursively walk an array of parents + them to queue
		if(end == -1)
			return;

		walkParents(s, parents[end], parents);
		s.add(this.routeMap.vertices[end].get());

	}

	private int leastCostUnknownVertex(int [] dist, boolean [] visited){ // find the least cost unknown vertex using dijkstras
		int min = Integer.MAX_VALUE, min_index = -1; 
  
        for (int v = 0; v < dist.length; v++) 
            if (visited[v] == false && dist[v] <= min) { 
                min = dist[v]; 
                min_index = v; 
            } 
        return min_index;
	}
// dijkstras algorithm 
	private Pair dijkstras(int source, int [] parents, HashMap<Integer, String> attractions){ 

		int [][] adjacencyMatrix = this.routeMap.adjacencyMatrix;
		int size = this.routeMap.size();

		int [] sDists = new int[size];
		boolean [] visited = new boolean[size];

		for(int i = 0; i < size; i ++){
			sDists[i] = Integer.MAX_VALUE;
			visited[i] = false;
		}

		sDists[source] = 0;
		parents = new int[size];
		parents[source] = -1;
		int l = -1;
		
		for(int i = 0; i < size; i++){
			l = leastCostUnknownVertex(sDists, visited);
			if(l == -1)
				break;
			int sDist = sDists[l];
			visited[l] = true;
			for(int v = 0; v < size; v++){
				int weight = adjacencyMatrix[l][v];

				if(weight > 0 && ((sDist + weight) < sDists[v])){
					parents[v] = l;
					sDists[v] = sDist + weight;
				}
			}
			if(attractions.containsKey(l)){
				System.out.println(attractions.get(l));
				attractions.remove(l);
				break;
			}
		}

		return new Pair(l, parents);
	}

	
//formatting check
	public void printRoute(List<String> route){ 
		System.out.println("Route:");
		System.out.println(route.get(0));
		for(int i = 1; i < route.size(); i ++){
			System.out.println("=>\n" + route.get(i));
		}
	}


	public static void main(String [] args){ //reads in the two different files
		File attractions = new File("attractions.csv");
		File roads = new File("roads.csv");
		int size = 0;
		Set<String[]> cityTracker = new HashSet<String[]>();
		BufferedReader csvReader;
		try{
			csvReader = new BufferedReader(new FileReader(roads));
			String row;
			try{
				while((row = csvReader.readLine()) != null){
					String [] data = row.split(",");
					String [] cityState1 = data[0].split(" ");
					String [] cityState2 = data[1].split(" ");
					if(!cityTracker.contains(cityState1)){
						cityTracker.add(cityState1);
						size++;
					}
					if(!cityTracker.contains(cityState2)){
						cityTracker.add(cityState2);
						size++;
					}
				}
			}catch(IOException c){
				System.out.println(c);
			}
		}catch(FileNotFoundException e){
			System.out.println(e);
		}
		RouteMapper r = new RouteMapper(attractions, roads, size);

		
		List<String> a = new ArrayList<String>();
		a.add("Alcatraz");
		a.add("Disney World");
		List<String> s = r.route("San Francisco", "CA", "Orlando", "FL", a);
		r.printRoute(s);
//the list of inputs can be altered here
	}
}