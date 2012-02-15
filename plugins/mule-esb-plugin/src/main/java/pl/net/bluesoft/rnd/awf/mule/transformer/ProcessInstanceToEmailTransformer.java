package pl.net.bluesoft.rnd.awf.mule.transformer;

import org.mule.RequestContext;
import org.mule.api.MuleMessage;
import org.mule.api.transformer.TransformerException;
import org.mule.transformer.AbstractMessageTransformer;
import org.mule.transformer.AbstractTransformer;
import org.mule.transport.email.MailProperties;
import pl.net.bluesoft.rnd.processtool.model.ProcessInstance;

public class ProcessInstanceToEmailTransformer extends AbstractMessageTransformer {

//    @Override
//    protected Object doTransform(Object src, String encoding) throws TransformerException {
//       ProcessInstance processInstance = (ProcessInstance) src;
//        String email = processInstance.getSimpleAttributeValue("email");
//
//        String body =  processInstance.getSimpleAttributeValue("msg");
//
//        RequestContext.getEventContext().getMessage().setProperty(
//                MailProperties.TO_ADDRESSES_PROPERTY, email);
//        return body;
//    }

    @Override
    public Object transformMessage(MuleMessage message, String outputEncoding) throws TransformerException {
        ProcessInstance processInstance = ((ProcessInstance) message.getPayload());
        String email = processInstance.getSimpleAttributeValue("email");

        String body =  processInstance.getSimpleAttributeValue("msg");
        System.out.println("dupa");
        message.setProperty(MailProperties.TO_ADDRESSES_PROPERTY, email);
        return body;

    }

}
