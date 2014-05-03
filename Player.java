/**
 * Communicate with the server and send GPS coords. 
 * @author Andrew Hood <andrewhood125@gmail.com>
 * 
 * Copyright (c) 2014 Andrew Hood. All rights reserved.
 */
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Random;

public class Player implements Runnable
{
    public static final double STEP = 0.00001;
    private Gson gson;
    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;
    public boolean isLobbyLeader, isHoldingFlag, alive;
    private Reader reader;
    private Thread readerThread;
    public String username, btMAC, location;
    public int lobbyState;
    public String lobbyID;
    public int team;
    public double flagLat, flagLong;
    public double baseLat, baseLong;
    public double north, south, east, west, latitude, longitude, accuracy;
    
    Player(String host, int port, boolean isLobbyLeader, String username, String btMAC, String location)
    {
        this.isLobbyLeader = isLobbyLeader;
        this.username = username;
        this.btMAC = btMAC;
        this.location = location;
        String[] latLong = location.split(",");
        latitude = Double.parseDouble(latLong[0].trim());
        longitude = Double.parseDouble(latLong[1].trim());
        
        try
        {    
            socket = new Socket(host, port);
            out = new PrintWriter(socket.getOutputStream(),true);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            reader = new Reader(in, this);
            readerThread = new Thread(reader);
            readerThread.start();
        } catch (UnknownHostException e) {
            System.out.println("Unknown host: " + host);
            System.exit(1);
        } catch  (IOException e) {
            System.out.println("No I/O");
            System.exit(1);
        }
        gson = new Gson();
    }
    
    public void getBounds()
    {
        JsonObject temp = new JsonObject();
        temp.addProperty("ACTION","LOBBY");
        this.send(temp);
    }
    
    public void getFlags()
    {
        JsonObject temp = new JsonObject();
        temp.addProperty("ACTION","FLAG");
        this.send(temp);
    }
    
    public void getBases()
    {
        JsonObject temp = new JsonObject();
        temp.addProperty("ACTION","BASE");
        this.send(temp);
    }
    
    public void send(String message)
    {
        System.out.println("[" + username +"][out]: " + message);
        out.println(message);
    }
    
    public void send(JsonObject message)
    {
        System.out.println("[" + username +"][out]: " + message.toString());
        out.println(message);
    }
    
    public void roam()
    {
        
        double movement = STEP*2;
        boolean westbound = false;
        while(true)
        {
            while(longitude-accuracy > west)
            {
                while(latitude+accuracy < north)
                {
                    latitude += movement;
                    updateLocation(100);
                }
                longitude -= movement;
                while(latitude-accuracy > south)
                {
                    latitude -= movement;
                    updateLocation(100);
                }
            }
            
            while(longitude+accuracy < east)
            {
                while(latitude+accuracy < north)
                {
                    latitude += movement;
                    updateLocation(100);
                }
                longitude += movement;
                while(latitude-accuracy > south)
                {
                    latitude -= movement;
                    updateLocation(100);
                }
            }
        }
    }
    
    public void confused()
    {
        while(true)
        {
            // Randomly pick a direction
            double direction = Math.random()*360;
            if(direction < 90)
            {
                latitude += STEP;
                longitude += STEP;
                updateLocation();
            } else if(direction < 180) {
                latitude -= STEP;
                longitude += STEP;
                updateLocation();
            } else if(direction < 270) {
                latitude += STEP;
                longitude -= STEP;
                updateLocation();
            } else if(direction < 180) {
                latitude -= STEP;
                longitude -= STEP;
                updateLocation();
            }
        }
    }
    
    public void starPlayer()
    {
        while(true)
        {
            while(isHoldingFlag || !alive)
            {
                if(baseLat < latitude)
                {
                    latitude -= STEP;
                }
                if(baseLat > latitude)
                {
                    latitude += STEP;
                }
                if(baseLong < longitude)
                {
                    longitude -= STEP;
                } 
                if(baseLong > longitude)
                {
                    longitude += STEP;
                }
                updateLocation();
            }
            
            while(!isHoldingFlag && alive)
            {
                if(flagLat < latitude)
                {
                    latitude -= STEP;
                }
                if(flagLat > latitude)
                {
                    latitude += STEP;
                }
                if(flagLong < longitude)
                {
                    longitude -= STEP;
                } 
                if(flagLong > longitude)
                {
                    longitude += STEP;
                }
                updateLocation();
            }
        }
    }
    
    public void run()
    {
        send("{\"ACTION\":\"HELLO\",\"USERNAME\":\"" + username + "\",\"BLUETOOTH\":\"" + btMAC + "\"}");
        
        if(isLobbyLeader)
        {
            send("{\"ACTION\":\"CREATE\",\"LOCATION\":\"" + location + "\",\"SIZE\":\"0.0007\", \"ACCURACY\":0.00026}");
           
            try
            {
                Thread.sleep(5000);
            } catch(InterruptedException ex) {
                System.err.println(ex.getMessage());
            }
            send("{\"ACTION\":\"START\"}");
          
            
        } else {
            try
            {
                Thread.sleep(1000);
            } catch(InterruptedException ex) {
                System.err.println(ex.getMessage());
            }
            JsonObject temp = new JsonObject();
            temp.addProperty("ACTION","LOBBY");
            this.send(temp);
            try
            {
                Thread.sleep(1000);
            } catch(InterruptedException ex) {
                System.err.println(ex.getMessage());
            }
            send("{\"ACTION\":\"JOIN\", \"ID\":\"" + lobbyID + "\",\"LOCATION\":\"" + location + "\"}");
        }
        
        try
        {
            Thread.sleep(5000);
        } catch(InterruptedException ex) {
            System.err.println(ex.getMessage());
        }
        
        getBounds();
        getFlags();
        getBases();
        
        Random rand = new Random();
        switch(rand.nextInt(10))
        {
            
            case 0: 
            case 1: 
            case 2:
            case 3:
            case 4:
            case 5: confused(); break;
            case 6:
            case 7:
            case 8:
            case 9: starPlayer(); break;
            case 10: roam(); break;
            default: confused(); 
        }
        
        // Wait for any last incoming messages to be read.
        try
        {
            Thread.sleep(10000);
        } catch(InterruptedException ex) {
            System.err.println(ex.getMessage());
        }
    }
    
    public void updateLocation()
    {
        updateLocation(1000);
    }
    
    public void updateLocation(int sleepTime)
    {
        try
        {
            Thread.sleep(sleepTime);
        } catch(InterruptedException ex) {
            System.err.println(ex.getMessage());
        }
            
        JsonObject temp = new JsonObject();
        temp.addProperty("ACTION", "GPS");
        temp.addProperty("LOCATION", latitude + "," + longitude);
        send(temp);
    }
    
    
}