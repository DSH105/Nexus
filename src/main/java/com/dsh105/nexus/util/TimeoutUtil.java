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

package com.dsh105.nexus.util;

import java.util.concurrent.TimeoutException;

public class TimeoutUtil {

    public static void timeout(Thread task, long timeout) throws TimeoutException {
        task.start();
        try {
            task.join(timeout);
        } catch (InterruptedException ignored) {

        }

        if (task.isAlive()) {
            task.interrupt();
            throw new TimeoutException();
        }
    }
}