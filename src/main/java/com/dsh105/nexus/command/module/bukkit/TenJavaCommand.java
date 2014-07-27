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
import com.dsh105.nexus.exception.bukkit.TenJavaDataLookupException;
import com.dsh105.nexus.util.JsonUtil;
import com.dsh105.nexus.util.StringUtil;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.Unirest;
import org.ocpsoft.prettytime.PrettyTime;
import org.pircbotx.Colors;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

@Command(command = "tenjava",
        aliases = {"tj"},
        needsChannel = false,
        help = "See the total prize pool, and the top donors",
        extendedHelp = {
                "{b}{p}{c}{/b} - View tenjava donated points",
                "{b}{p}{c} top{/b} - View the top donors to tenjava.",
                "{b}{p}{c} top <number>{/b} - View the top donors to tenjava.",
                "{b}{p}{c} judge{/b} - View judging stats.",
                "{b}{p}{c} judge <judge_name>{/b} - View stats for a certain judge."
        })
public class TenJavaCommand extends CommandModule {

    public static final String TEN_JAVA_URL = "https://tenjava.com/api/points";
    public static final String TEN_JAVA_TEAM_STATS_URL = "https://tenjava.com/api/team/stats";
    public static final int TOP_LIMIT = 10;
    private final DecimalFormat df = new DecimalFormat("0.00");

    @Override
    public boolean onCommand(CommandPerformEvent event) {
        if (event.getArgs().length == 0) {
            try {
                HttpResponse<JsonNode> jsonResponse = Unirest.get(TEN_JAVA_URL)
                        .header("accept", "application/json")
                        .asJson();
                int points = jsonResponse.getBody().getObject().getInt("points");
                long time = jsonResponse.getBody().getObject().getLong("last_update");
                PrettyTime pt = new PrettyTime();
                time = time * 1000;

                event.respondWithPing("Current points donated: " + Colors.BOLD + points + " ($" + df.format(points * 0.05) + " USD)" + Colors.NORMAL + ". Last updated: " + Colors.BOLD + pt.format(new Date(time)) + Colors.BOLD + " http://tenjava.com/points");
            } catch (Exception e) {
                throw new TenJavaDataLookupException("An error occurred in the lookup process", e);
            }
        }

        if (event.getArgs().length == 1 || event.getArgs().length == 2) {
            if (event.getArgs()[0].equalsIgnoreCase("top")) {

                int limit = TOP_LIMIT;

                if (event.getArgs().length == 2) {
                    limit = StringUtil.toInteger(event.getArgs()[1]);
                }

                try {
                    TenJavaDonor[] donors = JsonUtil.read(Unirest.get(TEN_JAVA_URL).header("accept", "application/json"), "better_top", TenJavaDonor[].class);
                    StringBuilder builder = new StringBuilder();
                    for (int i = 0; i < limit; i++) {
                        TenJavaDonor donor = donors[i];
                        builder.append(Colors.BOLD + StringUtil.removePing(donor.getUsername()) + Colors.BOLD + " (" + donor.getAmount() + ")");
                        if (i != limit) {
                            builder.append(", ");
                        }
                    }

                    event.respondWithPing("Top " + limit + " donors:" + Colors.NORMAL + " " + builder.toString());
                    return true;
                } catch (Exception e) {
                    throw new TenJavaDataLookupException("An error occurred in the lookup process", e);
                }
            } else if (event.getArgs()[0].equalsIgnoreCase("judge")) {
                TenJavaJudge[] judges = JsonUtil.read(Unirest.get(TEN_JAVA_TEAM_STATS_URL).header("accept", "application/json"), "judges", TenJavaJudge[].class);
                String[] judgeNames = new String[judges.length];
                for (int i = 0; i < judgeNames.length; i++) {
                    judgeNames[i] = judges[i].getGithubUserName();
                }
                List<String> judgeNamesList = Arrays.asList(judgeNames);
                if (event.getArgs().length == 2) {
                    if (event.getArgs()[1].equalsIgnoreCase("list")) {
                        event.respondWithPing("TenJava judges: " + StringUtil.buildSentenceList(judgeNames));
                        return true;
                    }

                    String judgeName = event.getArgs()[1];
                    if (judgeNamesList.contains(judgeName)) {
                        TenJavaJudge judge = judges[judgeNamesList.indexOf(judgeName)];
                        event.respondWithPing("Judging stats ({0}):" + judge.getGithubUserName());
                        event.respondWithPing("Items: {0}/{1} ({2} remaining) - {3}", judge.getAssignedItems() + "", judge.getCompletedItems() + "", judge.getRemainingItems() + "", judge.getPercentComplete() + "%");
                    } else {
                        event.errorWithPing("Judge not found: {0}. Use {1} for a list of judges", judgeName, event.getCommandPrefix() + event.getCommand() + " judge list");
                    }
                    return true;
                } else {
                    int total = 0;
                    int completed = 0;
                    for (TenJavaJudge judge : judges) {
                        total += judge.getAssignedItems();
                        completed += judge.getCompletedItems();
                    }
                    int percentCompleted = (total / completed) * 100;
                    event.respondWithPing("TenJava Judging stats: {0}/{1} items completed ({2})", completed + "", total + "", percentCompleted + "%");
                    return true;
                }
            }
        }

        return true;
    }

    public class TenJavaDonor {
        private int amount;
        private String username;

        public int getAmount() {
            return amount;
        }

        public String getUsername() {
            return username;
        }
    }

    public class TenJavaJudge {
        @SerializedName("github_username")
        private String githubUserName;
        @SerializedName("assignedItems")
        private int assignedItems;
        @SerializedName("completed_items")
        private int completedItems;
        @SerializedName("remaining_items")
        private int remainingItems;
        @SerializedName("percentage_complete")
        private int percentComplete;

        public String getGithubUserName() {
            return githubUserName;
        }

        public int getAssignedItems() {
            return assignedItems;
        }

        public int getCompletedItems() {
            return completedItems;
        }

        public int getRemainingItems() {
            return remainingItems;
        }

        public int getPercentComplete() {
            return percentComplete;
        }
    }
}

