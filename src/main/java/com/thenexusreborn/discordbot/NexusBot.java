package com.thenexusreborn.discordbot;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.MemberCachePolicy;
import net.dv8tion.jda.api.utils.cache.CacheFlag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class NexusBot {
    public static final Logger LOGGER = LoggerFactory.getLogger(NexusBot.class.getName());

    public static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    public static Yaml yaml;
    public static Map<String, Object> config;

    public static JDA jda;
    
    public static MCSocket socket;
    public static ScheduledFuture<?> scheduledFuture;
    
    public static Map<String, Pair<String, String>> linkChannels = new HashMap<>();

    public static void main(String[] args) throws Exception {
        Class.forName("com.mysql.cj.jdbc.Driver");

        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(4);
        
        while(true) {
            try {
                socket = new MCSocket();
                scheduledFuture = scheduler.scheduleAtFixedRate(socket.getSender(), 0L, 1, TimeUnit.SECONDS);
                scheduler.submit(socket.getListener());
                break;
            } catch (Exception e) {
                if (e.getMessage().contains("Connection refused")) {
                    continue;
                }
                LOGGER.error("Error while connecting that was not a connection refused error.");
                e.printStackTrace();
                return;
            }
        }

        File configFile = new File("config.yml");
        if (!configFile.exists()) {
            try (InputStream configStream = NexusBot.class.getClassLoader().getResourceAsStream("config.yml")) {
                Files.copy(configStream, configFile.toPath());
            }
        }

        yaml = new Yaml();

        InputStream inputStream = Files.newInputStream(configFile.toPath());
        config = yaml.load(inputStream);

        jda = JDABuilder.createDefault((String) config.get("token"))
                .enableIntents(GatewayIntent.GUILD_PRESENCES, GatewayIntent.GUILD_MEMBERS, GatewayIntent.MESSAGE_CONTENT, GatewayIntent.DIRECT_MESSAGES)
                .enableCache(CacheFlag.ACTIVITY, CacheFlag.MEMBER_OVERRIDES, CacheFlag.ROLE_TAGS, CacheFlag.VOICE_STATE)
                .addEventListeners(new DiscordListener())
                .setMemberCachePolicy(MemberCachePolicy.ALL)
                .build();
    }

    public static void cancelSocket() {
        LOGGER.info("Cancelling Socket");
        scheduledFuture.cancel(true);
    }
}