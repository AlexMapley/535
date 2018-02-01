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

  protected LinkStateDatabase lsd;

  RouterDescription rd = new RouterDescription();

  //assuming that all routers are with 4 ports
  Link[] ports = new Link[4];

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
                  ServerSocket routerSocket = new ServerSocket(rd.processPortNumber);
                  ObjectInputStream inFromRouters = new ObjectInputStream(routerSocket.getInputStream());
                  ObjectOutputStream outToRouters = new ObjectOutputStream(routerSocket.getOutputStream());
                  SOSPFPacket inPacket = new SOSPFPacket();

                  while (true) {

                      // Read and process incoming packets
                      inPacket = (SOSPFPacket)inFromRouters.readObject();
                      boolean seenRouter = false;
                      for (int i = 0; i < 4; i++) {
                        if (ports[i].router2.simulatedIPAddress == inPacket.srcIP) {
                          seenRouter = true;
                        }
                      }

                      // If new router, we need to attach it
                      if (seenRouter == false) {
                        processAttach(
                        inPacket.srcProcessIP, inPacket.srcProcessPort,
                        inPacket.srcIP, 1
                        );
                      }

                      // Get index of router in ports[]
                      int routerIndex = 0;
                      for (int i = 0; i < 4; i++) {
                        if ports[i].router2.simulatedIPAddress == inPacket.srcIP) {
                          routerIndex = i;
                        }
                      }

                      // Incoming 0 (Hello) packet
                      if (inPacket.sospfType == 0) {
                        ports[routerIndex].router2.status = RouterStatus.INIT;

                        // Need to send response
                        SOSPFPacket outPacket = new SOSPFPacket();
                        outPacket.srcProcessIP = "127.0.0.1";
                        outPacket.srcProcessPort = rd.processPortNumber;
                        outPacket.srcIP = rd.simulatedIPAddress;
                        outPacket.dstIP = ports[routerIndex].router2.processIPAddress;
                        outPacket.sospfType = 1; // We are sending the second handshake, ie. 1 (hey)
                        outToRouters.writeObject(outPacket);
                      }

                      // Incoming 1 (Hey) packet
                      if (inPacket.sospfType == 1) {
                        ports[routerIndex].router2.status = RouterStatus.TWO_WAY;

                        // Need to send response
                        SOSPFPacket outPacket = new SOSPFPacket();
                        outPacket.srcProcessIP = "127.0.0.1";
                        outPacket.srcProcessPort = rd.processPortNumber;
                        outPacket.srcIP = rd.simulatedIPAddress;
                        outPacket.dstIP = ports[routerIndex].router2.processIPAddress;
                        outPacket.sospfType = 1; // We are sending the second handshake, ie. 1 (hey)
                        outToRouters.writeObject(outPacket);
                      }

                  }
              } catch (IOException e) {
                  System.err.println("Accept failed.");
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
    int openIndex = 0;
    for (int i = 0; i < 4; i ++) {
      if (ports[i] == null) {
        openIndex = i;
        break;
      }
    }
    System.out.println("Open Link at ports[" + openIndex + "]");

    // Add Link to ports[]
    Link newLink = new Link(rd, otherRouter);
    ports[openIndex] = newLink;
  }

  private void flushLinks() {
    ports = new Link[4];
  }


  /**
   * broadcast Hello to neighbors
   */
  private void processStart() {
    System.out.println("Starting Router...");
    System.out.println("Process IP: " + rd.processIPAddress);
    System.out.println("Simulated IP: " + rd.simulatedIPAddress);
    System.out.println("Open port: " + rd.processPortNumber);

    // Attempt to contact other routers
    Runnable routerPinger = new Runnable() {
          @Override
          public void run() {
            Socket helloSocket;
            ObjectOutputStream outToRouters;

            for (int i = 0; i < 4; i++) {
              if (ports[i] != null) {
                try {
                  ports[i].router2.status = RouterStatus.INIT;
                  helloSocket = new Socket(ports[i].router2.processIPAddress, ports[i].router2.processPortNumber);
                  outToRouters = new ObjectOutputStream(helloSocket.getOutputStream());
                  SOSPFPacket helloPacket = new SOSPFPacket();
                  helloPacket.srcProcessIP = "127.0.0.1";
                  helloPacket.srcProcessPort = rd.processPortNumber;
                  helloPacket.srcIP = rd.simulatedIPAddress;
                  helloPacket.dstIP = ports[i].router2.processIPAddress;
                  helloPacket.sospfType = 0; // We are sending the first handshake, ie. HELLO
                  outToRouters.writeObject(helloPacket);
                }
                catch (IOException io) {
                    System.out.println("Unable to delcare socket");
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
    System.out.println("WE BE SEEIN NEIGHBORS UP IN HERE CHA FEEL");

  }

  /**
   * disconnect with all neighbors and quit the program
   */
  private void processQuit() {

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
          //output neighbors
          flushLinks();
        }else if (command.equals("quit")) {
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
