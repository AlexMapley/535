package socs.network.message;

import java.io.*;
import java.util.Enumeration;
import java.util.Vector;
import socs.network.node.LinkStateDatabase;
import java.util.concurrent.atomic.AtomicInteger;

public class SOSPFPacket implements Serializable {

  //for inter-process communication
  public String srcProcessIP;
  public short srcProcessPort;
  public short dstProcessPort;

  //simulated IP address
  public String srcIP;
  public String dstIP;

  //common header
  public short sospfType;
  //0 - startup handshake part 1
  //1 - startup handshake part 2
  //2 - startup handshake part 3
  //3 LinkState Update
  public String routerID;

  //used by HELLO message to identify the sender of the message
  //e.g. when router A sends HELLO to its neighbor, it has to fill this field with its own
  //simulated IP address
  public String neighborID; //neighbor's simulated IP address
  public LinkStateDatabase lsd;
  public int weight;

    //used by LSAUPDATE
    // why are we using vector
  public Vector<LSA> lsaArray = null;
  //
  // gonna try sending the entire link state db




  public void printPacket(String flag) {
      System.out.println("\n______________________________");
      System.out.println(flag + " Packet Description:");
      System.out.println("Header: " + sospfType);
      System.out.println("Source port: " + srcProcessPort);
      System.out.println("Source ip: " + srcIP);
      System.out.println("Destination port: " + dstProcessPort);
      System.out.println("Destination ip: " + dstIP);
      // if(lsd != null){
      //     System.out.println("Link State DB : "+ "\n" + lsd.toString());
      // }
      // if(lsaArray != null){
      //     Enumeration en = lsaArray.elements();
      //     System.out.println("lsaArray : "+ "\n");
      //     while(en.hasMoreElements()){
      //         System.out.println(en.nextElement() + "\n");
      //     }
      //
      //
      // }

      System.out.println("______________________________\n");
  }

}
