package socs.network.node;

import socs.network.message.LSA;
import java.io.*;
import java.util.*;




public class WeightedGraph implements Serializable {

    // identification for getting indices of edges
    public String[] nodeId = new String[6];
    public short[][] edges = new short[6][6];

    public WeightedGraph(){
        nodeId[0] = "192.168.1.1";
        nodeId[1] = "192.168.1.100";
        nodeId[2] = "192.168.2.1";
        nodeId[3] = "192.168.3.1";
        nodeId[4] = "192.168.4.1";
        nodeId[5] = "192.168.5.1";
    }

    public String[] nodeGetter(){
        return nodeId;
    }

}
