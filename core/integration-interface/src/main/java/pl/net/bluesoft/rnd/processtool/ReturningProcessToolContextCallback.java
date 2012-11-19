package pl.net.bluesoft.rnd.processtool;

import javax.xml.soap.SOAPException;

public interface ReturningProcessToolContextCallback<T> {
    T processWithContext(ProcessToolContext ctx);
}

