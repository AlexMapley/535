package socs.network.node;

import java.io.*;
import java.util.Map;
import socs.network.message.LSA;
import socs.network.message.LinkDescription;


import java.util.HashMap;

public class LinkStateDatabase implements Serializable{

  //linkID => LSAInstance
  HashMap<String, LSA> _store = new HashMap<String, LSA>();

  private RouterDescription rd = null;
  public WeightedGraph wg;


  public LinkStateDatabase(RouterDescription routerDescription) {
    rd = routerDescription;
    LSA l = initLinkStateDatabase();
    _store.put(l.linkStateID, l);
    wg = new WeightedGraph();

  }

  public void store(LSA lsa) {
    _store.put(lsa.linkStateID, lsa);
  }
  /**
   * output the shortest path from this router to the destination with the given IP address
   */
  public void getShortestPath(String destinationIP) {

    int destination_node = indexFinder(destinationIP);
    // set this to vertex of rd.simulatedIPAddress later
    final int NO_PARENT = -1;
    short startVertex = (short)(indexFinder(rd.simulatedIPAddress));
    // number of vertices
    int num_V = wg.edges[0].length;
    // shortest_Ds will hold the shortest distance from src to i
    int[] shortest_Ds = new int[num_V];
    // added[i] will be true if vertex i is included/ in the shortest path tree
    // or if shortest distance from src to i is finalized
    boolean[] added = new boolean[num_V];
    // Initialize all distances as Integer.MAX_VALUE and added[] as false
    // v_index = vertice index
    for (int v_Index = 0; v_Index < num_V; v_Index++)
    {
      shortest_Ds[v_Index] = Integer.MAX_VALUE;
      added[v_Index] = false;
    }

    // Distance of source vertex from itself is always 0
    shortest_Ds[startVertex] = 0;
    // Path array to store shortest path tree
    // size is amount of vertices available
    int[] parents = new int[num_V];
    // The starting vertex does not have a parent
    parents[startVertex] = NO_PARENT;

    //Find shortest path for all vertices
    for (int i = 0; i < num_V ; i++){
      // Pick the min distance vertex from the set of vertices not yet processed
      // Nearest vertex is always equal to startNode in first iteration
      int nearestVertex = i;
      int shortestDistance = Integer.MAX_VALUE;
      //
      for (int vertexIndex = 0; vertexIndex < num_V ; vertexIndex++){
        // By checking whether we've added the vertice and
        // whether it's shorter than the shortest distance we have now
        // out of all the vertices so far
        if (!added[vertexIndex] && (shortest_Ds[vertexIndex] < shortestDistance) ){
          nearestVertex = vertexIndex;
          // sets the shortest distance to the distance between the vertice
          shortestDistance = shortest_Ds[vertexIndex];
        }
      }
      //

      // Marked the picked vertex as processed
      // put an if here for if nearestVertex != -1
      if(nearestVertex != -1) {
        added[nearestVertex] = true;
        // Update the distance value of the adjacent vertices of the picked vertex.
        for (int vertexIndex = 0; vertexIndex < num_V; vertexIndex++) {
          int edgeDistance = wg.edges[nearestVertex][vertexIndex];
          if (edgeDistance > 0 && ((shortestDistance + edgeDistance) < shortest_Ds[vertexIndex])) {
            parents[vertexIndex] = nearestVertex;
            shortest_Ds[vertexIndex] = shortestDistance + edgeDistance;
          }
        }
      }



    }
    // Print solution
    printSolution(startVertex, shortest_Ds, parents, destination_node);



    //return destinationIP;
  }

  // A utility function to print the constructed distances array and shortest paths
  private void printSolution(int startVertex, int[] distances, int[] parents, int destination) {
    int nVertices = distances.length;
    System.out.print("Path :");

    for (int vertexIndex = 0; vertexIndex < nVertices; vertexIndex++) {
      // the second part of this if statement just glosses over the fact that dist is max value
      // might need to double check
      if (vertexIndex != startVertex && distances[vertexIndex]!=Integer.MAX_VALUE && (vertexIndex == destination)){
        System.out.print("\n Router" + (startVertex + 1) + " -> ");
        System.out.print("Router" +(vertexIndex + 1) + " \t\t ");
        //System.out.print(distances[vertexIndex] + "\t\t");
        printPath(vertexIndex, parents);
        break;
      }
      else if((vertexIndex != startVertex) && (distances[vertexIndex]==Integer.MAX_VALUE)){
        System.out.print("\n Router" + (startVertex + 1) + " -> ");
        System.out.print("Router" +(vertexIndex + 1) + " \t\t ");
        System.out.print( "Routers are not connected");
        break;
      }
      else if(destination == -1){
        System.out.print( " \n Invalid destination");
        break;
      }
    }
    System.out.print("\n");
  }

  // Function to print shortest path from source to currentVertex using parents array
  private void printPath(int currentVertex, int[] parents)
  {
    // Base case : Source node has
    // been processed
    if (currentVertex == -1) {
      return;
    }
    printPath(parents[currentVertex], parents);
    // in the case of orginating node
    if(parents[currentVertex] == -1){
        System.out.print(wg.nodeId[currentVertex] + "  ");
    }
    else{
        System.out.print(" ( " +wg.edges[parents[currentVertex]][currentVertex]+ ") " +" --> " +wg.nodeId[currentVertex]);
    }
    //System.out.print(currentVertex + "  ");
    //System.out.print(parents[currentVertex] + "  ");
    //System.out.print(wg.nodeId[currentVertex] + " ( " + ") " +" --> ");
    //System.out.print(currentVertex + " ( " + ") " +" --> ");
  }

  //initialize the linkstate database by adding an entry about the router itself
  private LSA initLinkStateDatabase() {
    LSA lsa = new LSA();
    lsa.linkStateID = rd.simulatedIPAddress;
    lsa.lsaSeqNumber = 0;
    LinkDescription ld = new LinkDescription();
    ld.linkID = rd.simulatedIPAddress;
    ld.portNum = -1;
    ld.tosMetrics = 0;
    lsa.links.add(ld);
    return lsa;
  }


  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("\n\n_______________  Links  ________________\n");
    for (LSA lsa: _store.values()) {
      sb.append("=======================\n").append("LSA: ").append(lsa.linkStateID).append("(" + lsa.lsaSeqNumber + ")").append(":\n").append("______________\nIP,Port,Weight\n");
      for (LinkDescription ld : lsa.links) {
        sb.append(ld.linkID).append(",").append(ld.portNum).append(",").
                append(ld.tosMetrics).append("\n");
      }
      //sb.append("\n");
    }
    return sb.toString();
  }

  public LSA updateLSA(LSA oldLsa ,LSA newLsa){
    // checks which LSA has the most up to date sequence number
    int oldSeq = oldLsa.lsaSeqNumber;
    int newSeq = newLsa.lsaSeqNumber;
    if(oldSeq < newSeq) {
      return newLsa;
    }
    else{
      return oldLsa;
    }
  }

  public short indexFinder(String linkID){
    String[] nodes = wg.nodeGetter() ;
    for(short i = 0; i < 6; i++){
      //if(linkID.equals())
      if(linkID.equals(nodes[i])){
        return i;
      }
    }
    return -1;
  }

  public void updateGraph(){
    for(Map.Entry<String, LSA> entry : _store.entrySet()){
      short x = indexFinder(entry.getValue().linkStateID);
      for(LinkDescription ld : entry.getValue().links){

        String router1 = ld.linkID;
        int router2 = ld.portNum;

        // first_index
        //short x = indexFinder(router1);
        short y;

        if(router2 == -1){
          // link to self
          y = x;
        }
        else{
          y = (short)(router2 - 10000);
        }
        //System.out.println("x index: " + x + "      y index: " + y );
        wg.edges[x][y] = (short) ld.tosMetrics;
      }
    }
//     for(int one = 0; one < 6; one++){
//      for(int two = 0; two < 6; two++){
//        System.out.print(wg.edges[one][two] + "\t");
//      }
//      System.out.print("\n");
//     }
  }


}
