/*
 * This file is part of Nexus.
 *
 * Nexus is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Nexus is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Nexus.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.dsh105.nexus.command.module.bukkit;

import com.dsh105.nexus.command.Command;
import com.dsh105.nexus.command.CommandModule;
import com.dsh105.nexus.command.CommandPerformEvent;
import com.dsh105.nexus.exception.bukkit.BukkitUserException;
import com.dsh105.nexus.util.StringUtil;
import com.dsh105.nexus.util.shorten.URLShortener;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.ocpsoft.prettytime.PrettyTime;
import org.pircbotx.Colors;

import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.text.SimpleDateFormat;
import java.util.Date;

@Command(command = "bu",
        aliases = {"bukkituser", "buser"},
        needsChannel = false,
        help = "Bukkit user profile info",
        extendedHelp = {
                "{b}{p}{c} <user>{/b} - Shows a Bukkit user's profile information"
        })
public class BukkitUser extends CommandModule {

    @Override
    public boolean onCommand(CommandPerformEvent event) {
        String name;
        if (event.getArgs().length == 0) {
            name = event.getSender().getNick();
        } else {
            name = event.getArgs()[0];
        }
        int posts;
        int likes;
        long timestamp; //Example -> in ms
        try {
            URL statsURL;
            URLConnection con = new URL("http://forums.bukkit.org/members/?username=" + name).openConnection();
            con.connect();
            InputStream is = con.getInputStream();
            statsURL = con.getURL();
            is.close();
            String followerAmount;
            Document followers = Jsoup.connect(statsURL.toString()).get();
            Elements fol = followers.select("a.count");
            if (!fol.isEmpty()) {
                String folFrag = fol.last().toString();
                Document folz = Jsoup.parseBodyFragment(folFrag);
                followerAmount = folz.text();
            } else {
                followerAmount = "0";
            }
            Document doc = Jsoup.connect(statsURL + "mini-stats.xml").get();
            if (doc.location().equalsIgnoreCase("http://forums.bukkit.org/members/?username=" + name + "mini-stats.xml")) {
                event.errorWithPing("User not found :(");
            } else if (doc.text().equals("This member limits who may view their full profile.")) {
                event.errorWithPing("This user limits who may view their full profile");
            } else {
                String link = URLShortener.shorten(statsURL.toString());
                Element messages = doc.select("message_count").first();
                String messagesS = messages.text();
                posts = StringUtil.toInteger(messagesS);
                Element likecount = doc.select("like_count").first();
                String likesS = likecount.text();
                likes = StringUtil.toInteger(likesS);
                Element register = doc.select("register_date").first();
                String date = register.text();
                Element usern = doc.select("username").first();
                String user = usern.text();
                timestamp = StringUtil.toInteger(date);
                timestamp = timestamp * 1000;
                PrettyTime p = new PrettyTime();
                p.format(new Date(timestamp));
                Date d = new Date(timestamp);
                double likesr = likes;
                double postsr = posts;
                double ratio = likesr / postsr;
                double finalratio = (double) Math.round(ratio * 1000) / 1000;
                SimpleDateFormat ft = new SimpleDateFormat("yyyy-MM-dd");
                event.respond(Colors.OLIVE + Colors.BOLD + "Bukkit User: " + Colors.NORMAL + Colors.BOLD + user + Colors.NORMAL + " | " + link);
                event.respond("Messages: " + Colors.BOLD + posts + Colors.NORMAL + " | Likes: " + Colors.BOLD + likes + Colors.NORMAL + " | LtP: " + Colors.BOLD + finalratio + Colors.NORMAL + " | Followers: " + Colors.BOLD + followerAmount);
                event.respond("Registered: " + Colors.UNDERLINE + p.format(new Date(timestamp)) + " (" + ft.format(timestamp) + ")");
            }

        } catch (Exception e) {
            throw new BukkitUserException("An error occurred while trying to retrieve the information", e);
        }
        return true;
    }
}
