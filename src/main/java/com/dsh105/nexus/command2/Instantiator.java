/**
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

package com.dsh105.nexus.command2;

import com.dsh105.nexus.Nexus;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

public class Instantiator {

    private Object[] argInstances;
    private Class[] argClasses;

    public Instantiator(Object... args) {
        this.argInstances = args;
        this.argClasses = new Class[args.length];

        for (int i = 0; i < args.length; i++) {
            argClasses[i] = argInstances[i].getClass();
        }
    }

    public Object instantiate(Class<?> clazz) {
        try {
            Constructor constructor = clazz.getConstructor(this.argClasses);
            constructor.setAccessible(true);
            return constructor.newInstance(this.argInstances);
        } catch (InvocationTargetException e) {
            Nexus.LOGGER.warning("Failed to instantiate commands class: " + clazz.getName());
            e.printStackTrace();
            return null;
        } catch (NoSuchMethodException e) {
            Nexus.LOGGER.warning("Failed to instantiate commands class: " + clazz.getName());
            e.printStackTrace();
            return null;
        } catch (InstantiationException e) {
            Nexus.LOGGER.warning("Failed to instantiate commands class: " + clazz.getName());
            e.printStackTrace();
            return null;
        } catch (IllegalAccessException e) {
            Nexus.LOGGER.warning("Failed to instantiate commands class: " + clazz.getName());
            e.printStackTrace();
            return null;
        }
    }
}
