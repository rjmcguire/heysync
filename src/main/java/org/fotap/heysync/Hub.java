package org.fotap.heysync;

import org.jetlang.core.DisposingExecutor;

/**
 * @author <a href="mailto:peter.royal@pobox.com">peter royal</a>
 */
public class Hub {
    private final Dispatchers dispatchers = new Dispatchers();

    public <T> T dispatcherFor(Class<T> type) {
        if (isAsynchronable(type)) {
            return dispatchers.dispatcherFor(type);
        }

        throw new IllegalArgumentException(
            String.format("Cannot create a dispatcher for %s. " +
                          "It must be an interface that is marked with the %s annotation",
                          type.getName(),
                          Asynchronous.class.getName()));
    }

    public <T> void addReceiver(T receiver, DisposingExecutor executor) {
        if (!processInterfaces(receiver.getClass(), receiver, executor)) {
            throw new IllegalArgumentException(String.format("%s does not implement any %s interfaces",
                                                             receiver.getClass().getName(),
                                                             Asynchronous.class.getName()));
        }
    }

    private <T> boolean processInterfaces(Class<?> type, T receiver, DisposingExecutor executor) {
        boolean any = false;
        for (Class<?> clazz : type.getInterfaces()) {
            if (isAsynchronable(clazz)) {
                dispatchers.add(clazz, receiver, executor);
                any = true;
            }

            if (processInterfaces(clazz, receiver, executor)) {
                any = true;
            }
        }

        return any;
    }

    private static boolean isAsynchronable(Class<?> type) {
        return type.isInterface() && null != type.getAnnotation(Asynchronous.class);
    }
}
