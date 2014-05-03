/**
 * Create a couple of threads to simulate players in a CTF game. 
 * @author Andrew Hood <andrewhood125@gmail.com>
 * 
 * Copyright (c) 2014 Andrew Hood. All rights reserved.
 */

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Scanner;

public class CTFDebug
{ 
    
    public static void main(String[] args)
    {
        if(args.length != 2)
        {
            System.err.println("Usage: java CTFServer [host] [portnumber]");
            System.exit(1);
        }
        String host = args[0];
        
        int portNumber = 4444;
        try
        {
            portNumber = Integer.parseInt(args[1]);
        } catch(NumberFormatException ex) {
            System.err.println(ex.getMessage());
            System.exit(2);
        }
       
        Player andrewhood125 = new Player(host, portNumber, true, "andrewhood125", "10:40:f3:97:28:9e", "35.12109549,-89.93835153");
        Thread andrewhood125Thread = new Thread(andrewhood125);
        andrewhood125Thread.start();
        
        Player specOps = new Player(host, portNumber, false, "Spec-Ops-81", "01:23:45:67:89:ab", "35.12113808,-89.93849768");
        Thread specOpsThread = new Thread(specOps);
        specOpsThread.start();
        
        Player player2 = new Player(host, portNumber, false, "Spec-Ops-83", "01:23:45:67:89:ab", "35.121143808,-89.93849768");
        Thread player2Thread = new Thread(player2);
        player2Thread.start();
        
        Player player3 = new Player(host, portNumber, false, "dorito", "01:23:45:67:89:ff", "35.121143808,-89.93889768");
        Thread player3Thread = new Thread(player3);
        player3Thread.start();
        
        Player player4 = new Player(host, portNumber, false, "Spec-Ops-43", "01:23:55:67:89:ab", "35.121543808,-89.93849768");
        Thread player4Thread = new Thread(player4);
        player4Thread.start();
        
        Player player5 = new Player(host, portNumber, false, "dorito-and-dip", "01:23:45:67:e9:ff", "35.121143808,-89.93679768");
        Thread player5Thread = new Thread(player5);
        player5Thread.start();
    }
}
