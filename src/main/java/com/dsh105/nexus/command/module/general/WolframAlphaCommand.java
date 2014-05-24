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

package com.dsh105.nexus.command.module.general;

import com.dsh105.nexus.Nexus;
import com.dsh105.nexus.command.Command;
import com.dsh105.nexus.command.CommandModule;
import com.dsh105.nexus.command.CommandPerformEvent;
import com.dsh105.nexus.exception.general.WolframAlphaQueryException;
import com.dsh105.nexus.util.StringUtil;
import com.dsh105.nexus.util.shorten.URLShortener;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.pircbotx.Colors;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

@Command(command = "wa",
        aliases = {"wolfram", "wolframalpha"},
        needsChannel = false,
        help = "Query WolframAlpha.",
        extendedHelp = {"{b}{p}{c}{/b} <query> - Request information from WolframAlpha."})
public class WolframAlphaCommand extends CommandModule {

    /*
     * WolframAlpha Docs - http://products.wolframalpha.com/api/documentation.html
     */

    public static final String API_URL = "http://api.wolframalpha.com/v2/query?input=%s&appid=%s";
    public static final String QUERY_URL = "http://www.wolframalpha.com/input/?i=%s";

    @Override
    public boolean onCommand(CommandPerformEvent event) {
        if (event.getArgs().length == 0) {
            return false;
        }

        String apiKey = Nexus.getInstance().getConfig().getWolframAlphaKey();
        if (apiKey.isEmpty()) {
            event.errorWithPing("Failed to query WolframAlpha - API key has not been configured.");
            Nexus.LOGGER.warning("User attempted to access WolframAlpha - API key is invalid!");
            return true;
        }

        String input = StringUtil.combineSplit(0, event.getArgs(), " ");

        StringBuilder answer = new StringBuilder();
        try {
            String apiUrl = String.format(API_URL, input, apiKey);
            Nexus.LOGGER.info("Requesting WolframAlpha interpretation at " + apiUrl);

            SAXReader reader = new SAXReader();
            Document document = reader.read(Unirest.get(apiUrl).asBinary().getBody());

            Element root = document.getRootElement();

            if (Boolean.valueOf(root.attribute("success").getValue())) {
                for (Iterator pods = root.elementIterator("pod"); pods.hasNext(); ) {
                    Element pod = (Element) pods.next();
                    String primary = pod.attributeValue("primary");
                    if (primary != null && Boolean.valueOf(primary)) {
                        for (Iterator subpods = pod.elementIterator("subpod"); subpods.hasNext(); ) {
                            Element subpod = (Element) subpods.next();
                            String result = subpod.element("plaintext").getText();
                            if (result != null && !result.isEmpty()) {
                                answer.append(result.replaceAll("\\n", " - ").replaceAll("\\s+", " "));
                            }
                        }
                    }
                }
                if (answer.length() > 0) {
                    event.respondWithPing(answer + " (" + URLShortener.shorten(String.format(QUERY_URL, input)) + ")");
                    return true;
                }
            }

            List<String> tips = new ArrayList<>();
            for (Iterator tipElements = root.element("tips").elementIterator("tip"); tipElements.hasNext(); ) {
                if (tips.size() > 3) {
                    break;
                }
                Element tip = (Element) tipElements.next();
                String result = tip.attributeValue("text");
                if (result != null && !result.isEmpty()) {
                    tips.add(result.replaceAll("\\s+", " "));
                }
            }
            event.errorWithPing("WolframAlpha could not interpret that!");
            if (!tips.isEmpty()) {
                event.errorWithPing(Colors.BOLD + "Tips" + Colors.BOLD + ": " + StringUtil.combineSplit(0, tips.toArray(new String[tips.size()]), "; "));
            }
            return true;
        } catch (UnirestException e) {
            throw new WolframAlphaQueryException("Failed to execute WolframAlpha query: " + input, e);
        } catch (DocumentException e) {
            throw new WolframAlphaQueryException("Failed to execute WolframAlpha query: " + input, e);
        }
    }
}