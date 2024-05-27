package com.thenexusreborn.discordbot;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.function.Consumer;

public class MCSocket implements Runnable {
    private final Socket socket;
    
    private PrintWriter out;
    private BufferedReader in;
    
    private LinkedList<String> messages = new LinkedList<>();
    
    private Map<String, Consumer<String>> listeners = new HashMap<>();
    
    private SocketListener listener;
    private SocketSender sender;

    public MCSocket() throws IOException {
        this.socket = new Socket("127.0.0.1", 8044);
        this.out = new PrintWriter(socket.getOutputStream(), true);
        this.in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        
        this.listener = new SocketListener();
        this.sender = new SocketSender();
    }
    
    public void sendMessage(String message, String listenCheck, Consumer<String> consumer) {
        this.messages.add(message);
        listeners.put(listenCheck, consumer);
    }

    public SocketSender getSender() {
        return sender;
    }

    public SocketListener getListener() {
        return listener;
    }

    @Override
    public void run() {
       
    }
    
    public class SocketListener implements Runnable {
        public void run() {
            try {
                String inputLine;
                while ((inputLine = in.readLine()) != null) {
                    if (inputLine.equals("exit")) {
                        break;
                    } else if (inputLine.startsWith("linksuccess")) {
                        String[] inputSplit = inputLine.split(" ");
                        Pair<String, String> guildChannelPair = NexusBot.linkChannels.get(inputSplit[1]);
                        Guild guild = NexusBot.jda.getGuildById(guildChannelPair.key());
                        TextChannel channel = guild.getTextChannelById(guildChannelPair.value());
                        Member member = guild.getMemberById(inputSplit[1]);
                        channel.sendMessage("<@" + member.getId() + "> you have successfully linked your Minecraft Account **" + inputSplit[2] + "**").queue();
                    }

                    for (Map.Entry<String, Consumer<String>> entry : new HashSet<>(listeners.entrySet())) {
                        System.out.println(entry.getKey());
                        if (inputLine.startsWith(entry.getKey())) {
                            entry.getValue().accept(inputLine);
                            listeners.remove(entry.getKey());
                            break;
                        }
                    }
                }
            } catch (IOException e) {
                NexusBot.cancelSocket();
                out.close();
                try {
                    in.close();
                    socket.close();
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
                e.printStackTrace();
            }
        }
    }

    public class SocketSender implements Runnable {
        public void run() {
            if (messages.isEmpty()) {
                out.println("heartbeat");
                out.flush();
                return;
            }
            
            out.println(messages.poll());
            out.flush();
        }
    }
}
