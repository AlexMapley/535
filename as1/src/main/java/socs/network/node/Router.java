package socs.network.node;

import socs.network.message.*;

import socs.network.util.Configuration;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.concurrent.*;
import java.util.*;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

public class Router {

  RouterDescription rd = new RouterDescription();

  // Assuming that all routers are with 4 ports
  Link[] ports = new Link[4];
  Socket[] clientSockets = new Socket[4];
  ObjectOutputStream[] clientStreams = new ObjectOutputStream[4];


  // Init Link State Database
  LinkStateDatabase lsd = new LinkStateDatabase(rd);


  public Router(Configuration config) {
    rd.simulatedIPAddress = config.getString("socs.network.router.ip");
    rd.processPortNumber = config.getShort("socs.network.router.port");
    rd.processIPAddress = "127.0.0.1";
    lsd = new LinkStateDatabase(rd);

    // Start listener thread
    Runnable listener = new Runnable() {
          @Override
          public void run() {

              try {


                  ServerSocket serverSocket = new ServerSocket(rd.processPortNumber);
                  SOSPFPacket inPacket = new SOSPFPacket();
                  Socket inSocket = serverSocket.accept();
                  ObjectInputStream ois = new ObjectInputStream(inSocket.getInputStream());

                  while (true) {

                      // Wait for a packet to come in
                      System.out.print("Waiting for another packet...\n>>");
                      inPacket = null;

                      // Read and process incoming packets
                      // This is still foobar, shouldnt need while loop read
                      while (inPacket == null) {
                        try {
                          inPacket = (SOSPFPacket) ois.readObject();
                        }
                        catch (ClassNotFoundException e) {
                          System.err.println(e);
                        }
                        catch (IOException e) {
                        }
                      }

                      System.out.println("packet received");

                      boolean seenRouter = false;
                      for (int i = 0; i < 4; i++) {
                        if (ports[i] != null && ports[i].router2.simulatedIPAddress == inPacket.srcIP) {
                          seenRouter = true;
                        }
                      }
                      inPacket.printPacket("Incoming");

                      // If this is a new router, we need to attach it
                      if (seenRouter == false) {
                        processAttach(
                        inPacket.srcProcessIP, inPacket.srcProcessPort,
                        inPacket.srcIP, (short) 1
                        );
                      }

                      // Get index of router in ports[]
                      int routerIndex = -1;
                      for (int i = 0; i < 4; i++) {
                        if (ports[i] != null && ports[i].router2.simulatedIPAddress.equals(inPacket.srcIP)){
                          routerIndex = i;
                        }
                      }

                      // Incoming 0 packet -> Outgoing 1 packet
                      if (inPacket.sospfType == 0) {
                        ports[routerIndex].router2.status = RouterStatus.INIT;
                        try {
                          // Need to send response
                          SOSPFPacket outPacket = new SOSPFPacket();
                          outPacket.srcProcessIP = inPacket.srcProcessIP; // localhost
                          outPacket.srcProcessPort = rd.processPortNumber;
                          outPacket.dstProcessPort = inPacket.srcProcessPort;
                          outPacket.srcIP = rd.simulatedIPAddress;
                          outPacket.dstIP = ports[routerIndex].router2.processIPAddress;
                          outPacket.sospfType = 1; // We are sending the second handshake, ie. 1
                          outPacket.printPacket("Outgoing");
                          clientStreams[routerIndex].writeObject(outPacket);
                        }
                        catch (Exception e) {
                          System.out.println(e);
                        }
                      }

                      // Incoming 1 packet -> Outgoing 2 packet
                      if (inPacket.sospfType == 1) {
                        ports[routerIndex].router2.status = RouterStatus.TWO_WAY;
                        try {
                          // Need to send response
                          SOSPFPacket outPacket = new SOSPFPacket();
                          outPacket.srcProcessIP = inPacket.srcProcessIP; // localhost
                          outPacket.srcProcessPort = rd.processPortNumber;
                          outPacket.dstProcessPort = inPacket.srcProcessPort;
                          outPacket.srcIP = rd.simulatedIPAddress;
                          outPacket.dstIP = ports[routerIndex].router2.processIPAddress;
                          outPacket.sospfType = 2; // We are sending the third handshake, ie. 2
                          outPacket.printPacket("Outgoing");
                          clientStreams[routerIndex].writeObject(outPacket);
                        }
                        catch (Exception e) {
                          System.out.println(e);
                        }
                      }

                      // Incoming 2 packet
                      if (inPacket.sospfType == 2) {
                        ports[routerIndex].router2.status = RouterStatus.TWO_WAY;
                      }

                      // Flush output Stream
                      clientStreams[routerIndex].flush();
                  }
              } catch (IOException e) {
                  System.err.println(e);
              }

          }
      };
    new Thread(listener).start();
  }

  /**
   * output the shortest path to the given destination ip
   * <p/>
   * format: source ip address  -> ip address -> ... -> destination ip
   *
   * @param destinationIP the ip adderss of the destination simulated router
   */
  private void processDetect(String destinationIP) {

  }

  /**
   * disconnect with the router identified by the given destination ip address
   * Notice: this command should trigger the synchronization of database
   *
   * @param portNumber the port number which the link attaches at
   */
  private void processDisconnect(short portNumber) {

  }

  /**
   * attach the link to the remote router, which is identified by the given simulated ip;
   * to establish the connection via socket, you need to indentify the process IP and process Port;
   * additionally, weight is the cost to transmitting data through the link
   * <p/>
   * NOTE: this command should not trigger link database synchronization
   */
  private void processAttach(String processIP, short processPort, String simulatedIP, short weight) {

    // Create other router description
    RouterDescription otherRouter = new RouterDescription();
    otherRouter.processIPAddress = "127.0.0.1";
    otherRouter.simulatedIPAddress = simulatedIP;
    otherRouter.processPortNumber = processPort;
    otherRouter.status = RouterStatus.INIT;

    // Finds open link in our ports array
    int openIndex = -1;
    boolean hasDescription = false;
    for (int i = 0; i < 4; i ++) {
      if (ports[i] == null) {
        openIndex = i;
        break;
      }
      else if ((ports[i].router2.simulatedIPAddress).equals(simulatedIP)) {
        hasDescription = true;
      }
    }


    if (hasDescription == false && openIndex != -1) {
      // Add Link to ports[]
      System.out.println("Establishing new link at ports[" + openIndex + "]");
      Link newLink = new Link(rd, otherRouter);
      ports[openIndex] = newLink;

      try {
        clientSockets[openIndex] = new Socket(otherRouter.processIPAddress, otherRouter.processPortNumber);
        clientStreams[openIndex] = new ObjectOutputStream(clientSockets[openIndex].getOutputStream());
      }
      catch (Exception e) {
        System.err.println("Trouble creating socket in process function");
        System.err.println(e);
      }
    }
  }

  private void flushLinks() {
    ports = new Link[4];
  }

  private void cmi() {
    System.out.println(">>");
  }

  private void lsa() {
    System.out.println(lsd.toString());
  }


  /**
   * broadcast Tcp handhshake to neighbors
   */
  private void processStart() {



    // Attempt to contact other routers
    Runnable routerPinger = new Runnable() {
          @Override
          public void run() {
            for (int i = 0; i < 4; i++) {
              if (ports[i] != null) {
                try {

                  // Contact Router
                  ports[i].router2.status = RouterStatus.INIT;
                  SOSPFPacket outPacket = new SOSPFPacket();
                  outPacket.srcProcessIP = "127.0.0.1";
                  outPacket.srcProcessPort = rd.processPortNumber;
                  outPacket.dstProcessPort = ports[i].router2.processPortNumber;
                  outPacket.srcIP = rd.simulatedIPAddress;
                  outPacket.dstIP = ports[i].router2.processIPAddress;
                  outPacket.sospfType = 0; // We are sending the first handshake, ie. HELLO

                  System.out.println("nullP 1");

                  clientStreams[i].writeObject(outPacket);
                  clientStreams[i].flush();
                  outPacket.printPacket("Outgoing");

                  System.out.println("nullP 2");

                  // Update Link State Database
                  LSA lsa = new LSA();
                  lsa.linkStateID = rd.simulatedIPAddress;
                  lsa.lsaSeqNumber = Integer.MIN_VALUE;
                  LinkDescription ld = new LinkDescription();
                  ld.linkID = ports[i].router2.simulatedIPAddress;
                  ld.portNum = -1;
                  ld.tosMetrics = 0;
                  lsa.links.add(ld);
                  lsd.store(lsa);

                  System.out.println("nullP 3?");



                }
                catch (Exception e) {
                    System.out.println("Unable to write to socket");
                    System.out.println(e);
                }
              }
            }
        }
      };
    new Thread(routerPinger).start();
  }

  /**
   * attach the link to the remote router, which is identified by the given simulated ip;
   * to establish the connection via socket, you need to indentify the process IP and process Port;
   * additionally, weight is the cost to transmitting data through the link
   * <p/>
   * This command does trigger the link database synchronization
   */
  private void processConnect(String processIP, short processPort,
                              String simulatedIP, short weight) {

  }

  /**
   * output the neighbors of the routers
   */
  private void processNeighbors() {
      for (int i = 0; i < 4; i++) {
        if(ports[i] != null) {
          System.out.println("Neighbor " + i + " | IP: " + ports[i].router2.simulatedIPAddress + " | " + ports[i].router2.status);
        }
      }
  }

  /**
   * disconnect with all neighbors and quit the program
   */
  private void processQuit() {

  }

  private void clear() {
    for(int i = 0; i < 75; i++) {
      System.out.println();
    }
  }

  public void terminal() {
    try {
      InputStreamReader isReader = new InputStreamReader(System.in);
      BufferedReader br = new BufferedReader(isReader);
      System.out.print(">> ");
      String command = br.readLine();
      while (true) {
        if (command.startsWith("detect ")) {
          String[] cmdLine = command.split(" ");
          processDetect(cmdLine[1]);
        } else if (command.startsWith("disconnect ")) {
          String[] cmdLine = command.split(" ");
          processDisconnect(Short.parseShort(cmdLine[1]));
        } else if (command.startsWith("quit")) {
          processQuit();
        } else if (command.startsWith("attach ")) {
          String[] cmdLine = command.split(" ");
          processAttach(cmdLine[1], Short.parseShort(cmdLine[2]),
                  cmdLine[3], Short.parseShort(cmdLine[4]));
        } else if (command.equals("start")) {
          processStart();
        } else if (command.equals("connect ")) {
          String[] cmdLine = command.split(" ");
          processConnect(cmdLine[1], Short.parseShort(cmdLine[2]),
                  cmdLine[3], Short.parseShort(cmdLine[4]));
        } else if (command.equals("neighbors")) {
          //output neighbors
          processNeighbors();
        } else if (command.equals("flushLinks")) {
          flushLinks();
        } else if (command.equals("clear")) {
          clear();
        }else if (command.equals("lsa")) {
          lsa();
        } else if (command.equals("quit")) {
          System.out.println("Quitting...");
          break;
        }
        else {
          //invalid command
          System.out.println("Incorrect Command");
        }
        System.out.print(">> ");
        command = br.readLine();
      }
      isReader.close();
      br.close();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

}
