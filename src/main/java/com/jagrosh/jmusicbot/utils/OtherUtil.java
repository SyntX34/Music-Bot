/*
 * Copyright 2018 John Grosh (jagrosh).
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.jagrosh.jmusicbot.utils;

import com.jagrosh.jmusicbot.JMusicBot;
import com.jagrosh.jmusicbot.entities.Prompt;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.ApplicationInfo;
import net.dv8tion.jda.api.entities.User;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.*;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * @author John Grosh <john.a.grosh@gmail.com>
 */
public class OtherUtil {
    public final static String NEW_VERSION_AVAILABLE = "There is a new version of JMusicBot JP available!\n"
            + "Current version: %s\n"
            + "Latest version: %s\n\n"
            + " https://github.com/SyntX34/Music-Bot/releases/latest Download the latest version from here.";
    private final static String WINDOWS_INVALID_PATH = "c:\\windows\\system32\\";

    /**
    * Gets the path from a string
    * Also fixes Windows' tendency to try to start in system32
    * If a bot tries to access this path, it will start from the location of the jar file instead.
    *
    * @param path String path
    * @return the path object
    */
    public static Path getPath(String path) {
        Path result = Paths.get(path);
        // special logic to prevent trying to access system32
        if (result.toAbsolutePath().toString().toLowerCase().startsWith(WINDOWS_INVALID_PATH)) {
            try {
                result = Paths.get(new File(JMusicBot.class.getProtectionDomain().getCodeSource().getLocation().toURI()).getParentFile().getPath() + File.separator + path);
            } catch (URISyntaxException ex) {
                ex.printStackTrace();
            }
        }
        return result;
    }

    /**
    * Loads a resource from a jar as a string
    *
    * @param clazz Class base object
    * @param name Name of the resource
    * @return A string containing the content of the resource
    */
    public static String loadResource(Object clazz, String name) {
        try {
            return readString(clazz.getClass().getResourceAsStream(name));
        } catch (Exception ex) {
            return null;
        }
    }

    public static String readString(InputStream inputStream) throws IOException {
        ByteArrayOutputStream into = new ByteArrayOutputStream();
        byte[] buf = new byte[32768];
        for (int n; 0 < (n = inputStream.read(buf)); ) {
            into.write(buf, 0, n);
        }
        into.close();
        return into.toString(StandardCharsets.UTF_8);
    }

    /**
    * Loads image data from a URL
    *
    * @param url Image URL
    * @return URL inputstream
    */
    public static InputStream imageFromUrl(String url) {
        if (url == null)
            return null;
        try {
            URL u = new URL(url);
            URLConnection urlConnection = u.openConnection();
            urlConnection.setRequestProperty("user-agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/78.0.3904.87 Safari/537.36");
            return urlConnection.getInputStream();
        } catch (IOException | IllegalArgumentException ignore) {
        }
        return null;
    }

    /**
    * Parses the activity from a string
    *
    * @param game the game, including the action such as 'playing' or 'watching'
    * @return the parsed activity
    */
    public static Activity parseGame(String game) {
        if (game == null || game.trim().isEmpty() || game.trim().equalsIgnoreCase("default"))
            return null;
        String lower = game.toLowerCase();

        if (lower.startsWith("playing"))
            return Activity.playing(makeNonEmpty(game.substring(7).trim()));
        if (lower.startsWith("listening to"))
            return Activity.listening(makeNonEmpty(game.substring(12).trim()));
        if (lower.startsWith("listening"))
            return Activity.listening(makeNonEmpty(game.substring(9).trim()));
        if (lower.startsWith("watching"))
            return Activity.watching(makeNonEmpty(game.substring(8).trim()));
        if (lower.startsWith("streaming")) {
            String[] parts = game.substring(9).trim().split("\\s+", 2);
            if (parts.length == 2) {
                return Activity.streaming(makeNonEmpty(parts[1]), "https://twitch.tv/" + parts[0]);
            }
        }
        return Activity.customStatus(game);
    }

    public static String makeNonEmpty(String str) {
        return str == null || str.isEmpty() ? "\u200B" : str;
    }

    public static OnlineStatus parseStatus(String status) {
        if (status == null || status.trim().isEmpty())
            return OnlineStatus.ONLINE;
        OnlineStatus st = OnlineStatus.fromKey(status);
        return st == null ? OnlineStatus.ONLINE : st;
    }

    public static String checkVersion(Prompt prompt) {
        // Get current version number
        String version = getCurrentVersion();

        // Check for new version
        String latestVersion = getLatestVersion();

        if (latestVersion != null && !latestVersion.equals(version) && JMusicBot.CHECK_UPDATE) {
            prompt.alert(Prompt.Level.WARNING, "Version", String.format(NEW_VERSION_AVAILABLE, version, latestVersion));
        }

        // Return the current version
        return version;
    }

    public static String getCurrentVersion() {
        if (JMusicBot.class.getPackage() != null && JMusicBot.class.getPackage().getImplementationVersion() != null)
            return JMusicBot.class.getPackage().getImplementationVersion();
        else
            return "not clear";
    }

    public static String getLatestVersion() {
        try {
            Response response = new OkHttpClient.Builder().build()
                    .newCall(new Request.Builder().get().url("https://api.github.com/repos/Cosgy-Dev/MusicBot-JP-java/releases/latest").build())
                    .execute();
            ResponseBody body = response.body();
            if (body != null) {
                try (Reader reader = body.charStream()) {
                    JSONObject obj = new JSONObject(new JSONTokener(reader));
                    return obj.getString("tag_name");
                } finally {
                    response.close();
                }
            } else
                return null;
        } catch (IOException | JSONException | NullPointerException ex) {
            return null;
        }
    }

    public static String getUnsupportedBotReason(JDA jda)
    {
        if (jda.getSelfUser().getFlags().contains(User.UserFlag.VERIFIED_BOT))
            return "Using JMusicBot JP is not supported as it is a verified bot.\n" +
                    "Since music will be played on VC, there is a possibility that copyright issues may occur, so we ask for your understanding and cooperation.";

        ApplicationInfo info = jda.retrieveApplicationInfo().complete();
        if (info.isBotPublic())
            return "Using JMusicBot JP is not supported as it is a public bot.\n" +
                    "Please disable \"PUBLIC BOT\" in the Developer Portal.\n" +
                    "https://discord.com/developers/applications/" + info.getId() + "/bot\n" +
                    "Since music will be played on VC, there is a possibility that copyright issues may occur, so we ask for your understanding and cooperation.";

        return null;
    }
}