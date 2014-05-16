package com.dsh105.nexus.command.module.github;

import com.dsh105.nexus.Nexus;
import com.dsh105.nexus.command.Command;
import com.dsh105.nexus.command.CommandModule;
import com.dsh105.nexus.command.CommandPerformEvent;
import com.dsh105.nexus.exception.general.GenericUrlConnectionException;
import com.dsh105.nexus.hook.github.GitHubStatus;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import org.pircbotx.Colors;

import java.util.ArrayList;
import java.util.List;

@Command(command = "status",
        aliases = {"githubstatus", "gstatus", "ghstatus"},
        needsChannel = false,
        help = "View the Status of GitHub's services.",
        extendedHelp = "{b}{p}{c}{/b} - View the Status of GitHub's services.")
public class GitHubStatusCommand extends CommandModule {

    private List<GitHubStatus> statusMessages = new ArrayList<>();

    private static String URL = "https://status.github.com/api/messages.json";
    private static String MESSAGE = "GitHub Status: %s" + Colors.NORMAL + Colors.BOLD + " (%s)";

    @Override
    public boolean onCommand(CommandPerformEvent event) {
        try {
            load();
        } catch (UnirestException e) {
            throw new GenericUrlConnectionException("Error occurred while reading from " + URL, e);
        }

        GitHubStatus status = statusMessages.get(0);
        event.respond(String.format(MESSAGE, getFormatting(status.getStatus()), status.getBody()));

        return true;
    }

    private void load() throws UnirestException {
        statusMessages.clear();

        for (GitHubStatus statusMessage : Nexus.JSON.read(Unirest.get(URL), GitHubStatus[].class)) {
            statusMessages.add(statusMessage);
        }
    }

    private String getFormatting(String status) {
        if (status.equalsIgnoreCase("good")) {
            return Colors.GREEN + Colors.BOLD + "All systems operational!";
        } else if (status.equalsIgnoreCase("minor")) {
            return Colors.OLIVE + Colors.BOLD + "Minor system outage";
        } else if (status.equalsIgnoreCase("major")) {
            return Colors.RED + Colors.BOLD + "Major system outage";
        } else {
            return status;
        }
    }
}
