package pl.net.bluesoft.rnd.pt.dict.global;

import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;


public class FelixServiceBridge {


    public static <T> T getServiceByReference(Class<T> serviceClass, BundleContext context) {
        ServiceReference ref = context.getServiceReference(serviceClass.getName());
        return ref != null ? (T) context.getService(ref) : null;
    }

}
