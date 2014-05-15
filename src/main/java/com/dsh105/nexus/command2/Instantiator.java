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

        for(int i = 0; i < args.length; i++) {
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
