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

package com.dsh105.nexus;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.logging.Level;
import java.util.logging.LogRecord;

public class ExceptionHandler implements Thread.UncaughtExceptionHandler {

    @Override
    public void uncaughtException(Thread thread, Throwable throwable) {
        Nexus.LOGGER.severe("Oh no! Something unexpected happened!");
        throwable.printStackTrace();
        Nexus.getInstance().channelLogHandler.publish(new LogRecord(Level.SEVERE, getStacktrace(throwable)));
    }

    private String getStacktrace(Throwable throwable) {
        final StringWriter result = new StringWriter();
        final PrintWriter writer = new PrintWriter(result);
        throwable.printStackTrace(writer);
        return result.toString();
    }
}