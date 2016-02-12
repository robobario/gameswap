package org.gameswap.web.resource;

import org.glassfish.hk2.utilities.binding.AbstractBinder;

public class ContextInjectableProvider<T> extends AbstractBinder {
    private final Class<T> clazz;
    private final T instance;

    public ContextInjectableProvider(Class<T> clazz, T instance) {
        this.clazz = clazz;
        this.instance = instance;
    }

    @Override
    protected void configure() {
        bind(instance).to(clazz);
    }
}