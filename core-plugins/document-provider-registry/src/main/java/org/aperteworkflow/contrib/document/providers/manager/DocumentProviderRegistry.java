package org.aperteworkflow.contrib.document.providers.manager;

import pl.net.bluesoft.rnd.util.func.Func;

import java.util.HashMap;
import java.util.Map;

/**
 * @author tlipski@bluesoft.net.pl
 */
public class DocumentProviderRegistry {
    
    private final Map<String,Func<DocumentProvider>> providerMap = new HashMap<String,Func<DocumentProvider>>();

    /**
     * Register new provider returning function. This function should return new class instance, because
     * this instance will be configured using DocumentProvider.configure.
     *
     * @param name
     * @param provider
     */
    public synchronized void registerProvider(String name, Func<DocumentProvider> provider) {
        providerMap.put(name, provider);
    }

    public DocumentProvider getProvider(String name, Map<String,String> configuration) {
        Func<DocumentProvider> providerFunc = providerMap.get(name);
        if (providerFunc == null) return null;
        DocumentProvider provider = providerFunc.invoke();
        provider.configure(configuration);
        return provider;

    }

    public void unregisterProvider(String name) {
        providerMap.remove(name);
    }
}
