package com.dsh105.nexus.command.module.general;

import com.dsh105.nexus.command.Command;
import com.dsh105.nexus.command.CommandModule;
import com.dsh105.nexus.command.CommandPerformEvent;
import org.pircbotx.Colors;

@Command(command = "temp", needsChannel = false, help = "Temperature converter",
        extendedHelp = {"{b}{p}{c}{/b} <temp>[F/C] - Converts the entered temperature to either celsius or fahrenheit"})
public class TempCommand extends CommandModule {
    public static double cTof(double c) {
        c = c * 9;
        c = c / 5;
        c = c + 32;
        return c;
    }

    public static double fToC(double c) {
        c = c - 32;
        c = c * 5;
        c = c / 9;
        return c;
    }

    @Override
    public boolean onCommand(CommandPerformEvent event) {
        if (event.getArgs().length < 1) {
            return false;
        } else if (event.getArgs()[0].toLowerCase().endsWith("c")) {
            String[] args = event.getArgs()[0].split("c");
            double temp = Double.valueOf(args[0]);
            double finalTemp = cTof(temp);
            String colour;
            if (temp < 20) {
                colour = Colors.BLUE;
            } else if (temp >= 20 && temp < 40) {
                colour = Colors.OLIVE;
            } else {
                colour = Colors.RED;
            }
            event.respondWithPing(colour + Colors.BOLD + args[0] + "°C" + Colors.NORMAL + " to fahrenheit: " + Colors.BOLD + colour + finalTemp + "°");
            return true;
        } else if (event.getArgs()[0].toLowerCase().endsWith("f")) {
            String[] args = event.getArgs()[0].split("f");
            double temp = Double.valueOf(args[0]);
            double finalTemp = fToC(temp);
            String colour;
            if (finalTemp < 20) {
                colour = Colors.BLUE;
            } else if (finalTemp >= 20 && finalTemp < 40) {
                colour = Colors.OLIVE;
            } else {
                colour = Colors.RED;
            }
            event.respondWithPing(colour + Colors.BOLD + args[0] + "°F" + Colors.NORMAL + " to celsius: " + Colors.BOLD + colour + finalTemp + "°");
        } else {
            double finalTempC = fToC(Double.valueOf(event.getArgs()[0]));
            double finalTempF = cTof(Double.valueOf(event.getArgs()[0]));
            double argsToDouble = Double.valueOf(event.getArgs()[0]);
            String colour;
            if (finalTempC < 20) {
                colour = Colors.BLUE;
            } else if (finalTempC >= 20 && finalTempC < 40) {
                colour = Colors.OLIVE;
            } else {
                colour = Colors.RED;
            }
            String colour2;
            if (argsToDouble < 20) {
                colour2 = Colors.BLUE;
            } else if (argsToDouble >= 20 && argsToDouble < 40) {
                colour2 = Colors.OLIVE;
            } else {
                colour2 = Colors.RED;
            }
            event.respondWithPing(colour + Colors.BOLD + event.getArgs()[0] + "°F" + Colors.NORMAL + " to celsius: " + Colors.BOLD + colour + finalTempC + "°");
            event.respondWithPing(colour2 + Colors.BOLD + event.getArgs()[0] + "°C" + Colors.NORMAL + " to fahrenheit: " + Colors.BOLD + colour2 + finalTempF + "°");
            return true;
        }
        return true;
    }
}
