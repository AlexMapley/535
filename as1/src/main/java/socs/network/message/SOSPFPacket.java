package socs.network.message;

import java.io.*;
import java.util.Vector;
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
  //0 - startup handhshake part 1
  //1 - startup handhshake part 2
  //2 - startup handhshake part 3
  //3 LinkState Update
  public String routerID;

  //used by HELLO message to identify the sender of the message
  //e.g. when router A sends HELLO to its neighbor, it has to fill this field with its own
  //simulated IP address
  public String neighborID; //neighbor's simulated IP address

  //used by LSAUPDATE
  public Vector<LSA> lsaArray = null;

  public void printPacket(String flag) {
    System.out.println("\n______________________________");
    System.out.println(flag + " Packet Description:");
    System.out.println("Header: " + sospfType);
    System.out.println("Source port: " + srcProcessPort);
    System.out.println("Source ip: " + srcIP);
    System.out.println("Destination port: " + dstProcessPort);
    System.out.println("Destination ip: " + dstIP);
    if(lsaArray != null){
      System.out.println("LSA Array: " + lsaArray);
    }
    System.out.println("______________________________\n");
  }

}
