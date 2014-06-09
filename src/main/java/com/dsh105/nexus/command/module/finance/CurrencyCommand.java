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

package com.dsh105.nexus.command.module.finance;

import com.dsh105.nexus.command.Command;
import com.dsh105.nexus.command.CommandModule;
import com.dsh105.nexus.command.CommandPerformEvent;
import com.dsh105.nexus.exception.currency.CurrencyException;
import com.dsh105.nexus.util.StringUtil;
import com.dsh105.nexus.util.currency.CurrencyConverter;
import com.dsh105.nexus.util.currency.RateExchangeLookup;

@Command(command = "currency",
        aliases = {"cur"},
        needsChannel = false,
        help = "Converts between two currencies.",
        extendedHelp = {
                "This command allows conversion between two currencies. A list of currency codes is available at http://www.xe.com/currency/",
                "{b}{p}{c} <from> <to> <amount>{/b} - where 'from' and 'to' are valid currency codes.",
                "Data is provided with no guarantees whatsoever."
        })
public class CurrencyCommand extends CommandModule {

    @Override
    public boolean onCommand(CommandPerformEvent event) {
        final String[] args = event.getArgs();
        if (args.length != 3) {
            return false;
        }

        String from = args[0];
        String to = args[1];
        String amountStr = args[2];

        double amount = StringUtil.toDouble(amountStr);

        try {
            CurrencyConverter converter = new CurrencyConverter(new RateExchangeLookup());
            double newAmount = CurrencyConverter.roundToPennies(converter.convertAmount(amount, from, to));
            event.respond(String.format("%.2f %s is %.2f %s to the nearest penny.", amount, from, newAmount, to));
        } catch (CurrencyException exception) {
            event.respond("Conversion failed. Check currency codes are correct and try again.");
        }
        return true;
    }
}
