package socs.network.node;


import java.io.*;

public class WeightedGraph{
    // 6 routers
    // -------------------------------might need to be fixed--------------------------------
    // value is the weight
    public short[][] edges;

    // Store the simulatedIP for each vertex(router)
    public String[] myID;

    // find some way to connect this with LSD
    public WeightedGraph() {
        myID = new String[6];
        edges  = new short[6][6];
    }
}