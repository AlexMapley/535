package socs.network.node;


import java.io.*;
import java.util.*;

class Edge {

  public String[] nodes = new String[2];
  public Integer weight;

  Edge(String v1, String v2, int weight) {
        this.nodes[0] = v1;
        this.nodes[1] = v2;
        this.weight = weight;
    }
}

public class WeightedGraph {

    private ArrayList<Edge> edges = new ArrayList<Edge>();
    private ArrayList<String> nodes = new ArrayList<String>();
    private int nb_nodes = 0;
    private String source = "";
    private String destination = "";

    WeightedGraph() {
      // We dont need a complicated constructor,
      // We just make an Empty Graph
    }

    public void addEdge(Edge e) throws RuntimeException{
        /*Ensures that it is a new edge if both nodes already in the graph*/
        String n1 = e.nodes[0];
        String n2 = e.nodes[1];
        if (this.nodes.indexOf(n1) >= 0 && this.nodes.indexOf(n2) >= 0) {
            for (int z = 0; z < this.edges.size(); z++) {
                String[] n = this.edges.get(z).nodes;
                if (n1.equals(n[0]) && n2.equals(n[1])) {
                    throw new RuntimeException("The edge (" + n1 + ", " + n2 + ") already exists");
                }
            }
        }

        /*Update nb_nodes if necessary*/
        if (this.nodes.indexOf(n1) == -1){
            this.nodes.add(n1);
            this.nb_nodes += 1;
        }
        if (this.nodes.indexOf(n2) == -1){
            this.nodes.add(n2);
            this.nb_nodes += 1;
        }

        this.edges.add(e);
    }

    public Edge getEdge(String node1, String node2){
    	for(Edge e:edges){
    		if(e.nodes[0]==node1 && e.nodes[1]==node2){
    			return e;
    		}
    	}
    	return null;
    }

    public String getSource(){
    	return this.source;
    }

    public String getDestination(){
    	return this.destination;
    }

    public void setEdge(String node1, String node2, int weight){
    	for(Edge e:edges){
    		if(e.nodes[0]==node1 && e.nodes[1]==node2){
    			e.weight=weight;
    		}
    	}
    }

    public ArrayList<Edge> getEdges(){
        return this.edges;
    }

    public ArrayList<String> getNodes() {
      return this.nodes;
    }

    public int getNbNodes(){
        return this.nb_nodes;
    }

    public String toString() {
    	String out = this.source + " " + this.destination + "\n";
        out += "Router in the system: " + Integer.toString(this.nb_nodes);
        for (int i = 0; i < this.edges.size(); i++){
            Edge e = edges.get(i);
            out += "\n" + e.nodes[0] + " " + e.nodes[1] + " " + e.weight;
        }
        return out;
    }

    public ArrayList<Edge> listOfEdgesSorted()  {
        ArrayList<Edge> edges = new ArrayList<Edge>(this.edges);
        Collections.sort(edges,
            new Comparator<Edge>() {
            public int compare(Edge  e1, Edge  e2)
            {
                return  e1.nodes[0].compareTo(e2.nodes[0]);
            }
        });

        return edges;
    }

    public void djikstras(String source, String destination) {

      int index;
      ArrayList<String> dNodes = getNodes();
      ArrayList<Integer> distances = new ArrayList<Integer>();
      ArrayList<Edge> optimalPath = new ArrayList<Edge>();
      // ArrayList<String> unsettled = new ArrayList<String>();
      // ArrayList<String> settled = new ArrayList<String>();

      // Populate dummy graph with edges
      WeightedGraph graphClone = new WeightedGraph();
      for (Edge e : listOfEdgesSorted()) {
        if (e.weight != 0)
          graphClone.addEdge(e);
      }

      // Set node distance all to -1
      for (String node : dNodes) {
        index = dNodes.indexOf(node);
        distances.add(index,100000000);
      }

      // Set distance to original vertex as 0
      index = dNodes.indexOf(source);
      distances.set(index,0);



      Set<String> settledNodes = new HashSet();
      Set<String> unsettledNodes = new HashSet();
      unsettledNodes.add(source);



      // Is there an immediate path?
      for (Edge e : listOfEdgesSorted()) {
        if (e.nodes[0].equals(source) && e.nodes[1].equals(destination)) {
          optimalPath.add(e);
        }
      }

      // Output graph and optimal path
      System.out.println(this.toString());
      System.out.println("\nOptimal Path:");
      for (Edge e : optimalPath) {
        System.out.println(e.nodes[0] + " -> " + e.nodes[1] + " (" + e.weight + ")");
      }
    }

}
