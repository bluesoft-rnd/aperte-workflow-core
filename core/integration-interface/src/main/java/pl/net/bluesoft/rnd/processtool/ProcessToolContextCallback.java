package pl.net.bluesoft.rnd.processtool;

public abstract class ProcessToolContextCallback implements ReturningProcessToolContextCallback<Void> {
    public abstract void withContext(ProcessToolContext ctx);

    @Override
    public Void processWithContext(ProcessToolContext ctx) {
        withContext(ctx);
        return null;
}
}

