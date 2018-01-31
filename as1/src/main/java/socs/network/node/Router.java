package socs.network.node;

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

                  while (true) {
                      Socket listenerSocket = routerSocket.accept();
                      //ObjectOutputStream objectout = new ObjectOutputStream(listenerSocket.getOutputStream());
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

    Socket startingSocket;
    PrintWriter startingStream;

    // Attempt to contact other router
    for (int i = 0; i < 4; i++) {
        try {
          startingSocket = new Socket(ports[i].router2.processIPAddress, ports[i].router2.processPortNumber);
          startingStream = new PrintWriter(startingSocket.getOutputStream(), true);
        }
        catch (IOException io) {
            System.out.println("Unable to delcare socket");
        }
        catch (NullPointerException e) {
        }
    }

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
