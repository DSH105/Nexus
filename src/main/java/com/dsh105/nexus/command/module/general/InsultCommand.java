package com.dsh105.nexus.command.module.general;

import com.dsh105.nexus.command.Command;
import com.dsh105.nexus.command.CommandModule;
import com.dsh105.nexus.command.CommandPerformEvent;
import com.dsh105.nexus.exception.general.InsultLookupException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

@Command(command = "insult", aliases = {}, needsChannel = false, help = "Insult generator",
        extendedHelp = {"{b}{p}{c}{/b} - Prints an insult"})

public class InsultCommand extends CommandModule {
    public static final String INSULT_URL = "http://www.insultgenerator.org/";

    @Override
    public boolean onCommand(CommandPerformEvent event) {
        try {
            Document doc = Jsoup.connect(INSULT_URL).get();
            Element insult = doc.select("td").first();
            String finalInsult = insult.text();
            if (event.getArgs().length < 1) {
                event.respond(finalInsult);
            }else {
                event.respond(event.getArgs()[0] + ": " + finalInsult);
            }
        } catch (Exception e) {
            throw new InsultLookupException("An error occurred while trying to retrieve the information", e);
        }
        return true;
    }
}
