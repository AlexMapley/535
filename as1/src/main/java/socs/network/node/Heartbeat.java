package socs.network.node;

import socs.network.message.SOSPFPacket;

import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

public class Heartbeat extends TimerTask{
    private Router rtr;
    Heartbeat(Router r){

    }
    public void run() {
        for (int i = 0; i < 4; i++) {
            rtr.feedbacks[i] = false;
        }
        TimerTask timerTask = new Heartbeat(rtr);
        Timer timer = new Timer(true);
        // delay of 5 seconds for the router to finish connections
        timer.scheduleAtFixedRate(timerTask, 5 *1000, 30 * 1000);
        // 30 seconds


        System.out.println("Start");



            ObjectOutputStream oos = null;

            // gotta update lsa sequence number when sharing
            // LSA editedLsa = lsd._store.get(rd.simulatedIPAddress);
            // editedLsa.lsaSeqNumber += 1;
            // lsd.store(editedLsa);
            for (int i = 0; i < 4; i++) {
                if (rtr.ports[i] != null) {
                    try {
                        rtr.comSockets[i] = new Socket(rtr.ports[i].router2.processIPAddress, rtr.ports[i].router2.processPortNumber);
                        oos = new ObjectOutputStream(rtr.comSockets[i].getOutputStream());
                        SOSPFPacket outPacket = new SOSPFPacket();
                        outPacket.srcProcessIP = "127.0.0.1";
                        outPacket.srcProcessPort = rtr.rd.processPortNumber;
                        outPacket.dstProcessPort = rtr.ports[i].router2.processPortNumber;
                        outPacket.srcIP = rtr.rd.simulatedIPAddress;
                        outPacket.dstIP = rtr.ports[i].router2.processIPAddress;
                        outPacket.sospfType = 6; // We are sending the first handshake, ie. HELLO
                        outPacket.lsd = rtr.shareThreadLsd;
                        outPacket.printPacket("Outgoing");
                        oos.writeObject(outPacket);

                        // Close oos
                        oos.close();
                        rtr.comSockets[i].close();
                    }
                    catch (Exception e) {
                        System.out.println("Unable to write to socket");
                        System.out.println(e);
                    }
                }
            }
        try {

            Thread.sleep(20000);

        } catch (InterruptedException e) {
            e.printStackTrace();

        }

        for (int i = 0; i < 4; i++) {
            // this is for checking if feedback was returned

            if (rtr.ports[i] != null && rtr.feedbacks[i] == false) {
                rtr.processDisconnect(rtr.ports[i].router2.processPortNumber, true);
            }
        }
        timer.cancel();

        System.out.println("End");

    }




}
