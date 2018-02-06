package socs.network.node;
import java.net.Socket;

public class Link {

  RouterDescription router1;
  RouterDescription router2;


  public Link(RouterDescription r1, RouterDescription r2) {
    router1 = r1;
    router2 = r2;
  }
}
