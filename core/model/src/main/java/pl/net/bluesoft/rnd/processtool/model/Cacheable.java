package pl.net.bluesoft.rnd.processtool.model;

public interface Cacheable<K, V> {
    K getKey();
    V getValue();
}
