package socs.network.node;
import java.io.*;
public class RouterDescription implements Serializable{
  //used to socket communication
  String processIPAddress;
  short processPortNumber;
  //used to identify the router in the simulated network space
  String simulatedIPAddress;
  //status of the router
  RouterStatus status;
}
