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

package com.dsh105.nexus.command.module.utility;

import com.dsh105.nexus.command.Command;
import com.dsh105.nexus.command.CommandModule;
import com.dsh105.nexus.command.CommandPerformEvent;
import com.dsh105.nexus.util.shorten.URLShortener;

import java.util.regex.Pattern;

@Command(command = "shorten",
        aliases = {"short"},
        needsChannel = false,
        help = "Shorten a URL via goo.gl",
        extendedHelp = {
                "{b}{p}{c} <url>{/b} - Shorten a URL via goo.gl."
        })
public class ShortenCommand extends CommandModule {

    private static Pattern GITHUB_URL_PATTERN = Pattern.compile("^(https?://)?(github)\\.(com)([/\\w \\.-]*)*/?$");
    private static Pattern URL_PATTERN = Pattern.compile("^(https?://)?([\\da-z\\.-]+)\\.([a-z\\.]{2,6})([/\\w \\.-]*)*/?$");

    @Override
    public boolean onCommand(CommandPerformEvent event) {
        if (event.getArgs().length != 1) {
            return false;
        }

        String longUrl = event.getArgs()[0];
        if (!URL_PATTERN.matcher(longUrl).matches()) {
            event.errorWithPing("Invalid URL entered");
            return true;
        }

        String shortUrl = URLShortener.shortenGit(longUrl);

        event.respondWithPing("Shortened to " + shortUrl);
        return true;
    }
}
