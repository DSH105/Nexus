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
                "{b}{p}{c}{/b} <from> <to> <amount> - where 'from' and 'to' are valid currency codes.",
                "Data is provided with no guarantees whatsoever."})
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

        if (!StringUtil.isDouble(amountStr)) {
            event.respond("Amount is not a valid value.");
            return false;
        }

        double amount = Double.parseDouble(amountStr);

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
