/**
 * Communicate with the server and send GPS coords. 
 * @author Andrew Hood <andrewhood125@gmail.com>
 * 
 * Copyright (c) 2014 Andrew Hood. All rights reserved.
 */
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.BufferedReader;
import java.io.IOException;

public class Reader implements Runnable
{
    private BufferedReader in;
    private Player player;
    
    Reader(BufferedReader in, Player player)
    {
        this.in = in;
        this.player = player;
    }
    
    public void read()
    {
        try
        {
            System.out.println("[" + player.username +"][in]: " + in.readLine());
        } catch  (IOException e) {
            System.err.println("No I/O");
            System.exit(1);
        }
    }
    
    public void read(JsonObject jo)
    {
        System.out.println("[" + player.username +"][in]: " + jo.toString());
    }
    
    public void run()
    {
        while(true)
        {
            JsonObject jo = readLine();
            JsonElement action = jo.get("ACTION");
            if(jo.has("SUCCESS"))
            {
                read(jo);
            } else {
                switch(action.getAsString())
                {
                    case "STATE": player.lobbyState = jo.get("STATE").getAsInt();
                                  read(jo);
                                  break;
                    case "LOBBIES": readLobbyID(jo); read(jo); break;
                    case "JOINED": read(jo); break;
                    case "KILL": readKill(jo);break;
                    case "SPAWN": readSpawn(jo);break;
                    case "START": read(jo);break;
                    case "LOBBY": readLobby(jo);break;
                    case "FLAG": readFlag(jo);break;
                    case "BASE": readBase(jo);break;
                    case "CAPTURE": readCapture(jo);break;
                    case "DROP": readDrop(jo);break;
                    default: read(jo);
                }
            }
            
        }
    }
    
    public JsonObject readLine()
    {
        try 
        {
            JsonParser jp = new JsonParser();
            JsonElement je = jp.parse(in.readLine());
            JsonObject jo = je.getAsJsonObject();
            return jo;
        } catch(IOException ex) {
            System.err.println(ex.getMessage());
        }
        return null;
    }
    
    public void readCapture(JsonObject jo)
    {
        read(jo);
        if(jo.get("PLAYER").getAsString().equals(player.username))
        {
            player.isHoldingFlag = true;
        }
    }
    
    public void readDrop(JsonObject jo)
    {
        read(jo);
        player.getFlags();
        if(jo.get("PLAYER").getAsString().equals(player.username))
        {
            player.isHoldingFlag = false;
        }
    }
    
    public void readKill(JsonObject jo)
    {
        read(jo);
        if(jo.get("PLAYER").getAsString().equals(player.username))
        {
            player.alive = false;
        }
    }
    
    public void readSpawn(JsonObject jo)
    {
        read(jo);
        if(jo.get("PLAYER").getAsString().equals(player.username))
        {
            player.alive = true;
        }
    }
    
    public void readFlag(JsonObject jo)
    {
        read(jo);
        JsonArray flags = jo.get("FLAGS").getAsJsonArray();
        for(int i = 0; i < flags.size(); i++)
        {
            JsonObject flag = flags.get(i).getAsJsonObject();
            if(flag.get("TEAM").getAsInt() != player.team)
            {
                String[] latLong = flag.get("LOCATION").getAsString().split(",");
                player.flagLat = Double.parseDouble(latLong[0].trim());
                player.flagLong = Double.parseDouble(latLong[1].trim());
            }
        }
    }
    
    public void readBase(JsonObject jo)
    {
        read(jo);
        JsonArray bases = jo.get("BASES").getAsJsonArray();
        for(int i = 0; i < bases.size(); i++)
        {
            JsonObject base = bases.get(i).getAsJsonObject();
            if(base.get("TEAM").getAsInt() == player.team)
            {
                String[] latLong = base.get("LOCATION").getAsString().split(",");
                player.baseLat = Double.parseDouble(latLong[0].trim());
                player.baseLong = Double.parseDouble(latLong[1].trim());
            }
        }
    }
    
    public void readLobby(JsonObject jo)
    {
        read(jo);
        if(jo.has("NORTH"))
        {
            player.north = jo.get("NORTH").getAsDouble();
            player.south = jo.get("SOUTH").getAsDouble();
            player.east = jo.get("EAST").getAsDouble();
            player.west = jo.get("WEST").getAsDouble();
            player.accuracy = jo.get("ACCURACY").getAsDouble();
            JsonArray players = jo.get("PLAYERS").getAsJsonArray();
            for(int i = 0; i < players.size(); i++)
            {
                JsonObject player_jo = players.get(i).getAsJsonObject();
                if(player_jo.get("USERNAME").getAsString().equals(player.username))
                {
                    player.team = player_jo.get("TEAM").getAsInt();
                }
            }
        }
    }
    
    public void readLobbyID(JsonObject temp)
    {
        
        JsonArray lobbies = temp.get("LOBBIES").getAsJsonArray();
        JsonElement firstLobby = lobbies.get(lobbies.size()-1);
        JsonElement lobbyElement = firstLobby.getAsJsonObject().get("LOBBY");
        player.lobbyID = lobbyElement.getAsString();
    }
}