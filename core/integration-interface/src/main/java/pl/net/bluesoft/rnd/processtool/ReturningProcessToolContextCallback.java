package pl.net.bluesoft.rnd.processtool;

public interface ReturningProcessToolContextCallback<T> {
    T processWithContext(ProcessToolContext ctx);
}

