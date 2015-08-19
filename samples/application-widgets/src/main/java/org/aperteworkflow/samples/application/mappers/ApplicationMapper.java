package org.aperteworkflow.samples.application.mappers;

import org.aperteworkflow.files.model.FilesRepositoryItem;
import pl.net.bluesoft.casemanagement.model.*;
import pl.net.bluesoft.rnd.processtool.model.IAttributesConsumer;
import pl.net.bluesoft.rnd.processtool.model.IAttributesProvider;
import pl.net.bluesoft.rnd.processtool.model.ProcessInstance;
import pl.net.bluesoft.rnd.processtool.plugins.IMapper;
import pl.net.bluesoft.rnd.processtool.plugins.Mapper;
import pl.net.bluesoft.rnd.processtool.plugins.MapperContext;

/**
 * Created by Dominik Dębowczyk on 2015-08-06.
 */
@Mapper(forProviderClass = ProcessInstance.class, forDefinitionNames = {})
public class ApplicationMapper implements IMapper {
    @Override
    public void map(IAttributesConsumer consumer, IAttributesProvider provider, MapperContext context) {
        consumer.setSimpleAttribute("name", provider.getSimpleAttributeValue("name"));
        consumer.setSimpleAttribute("surname", provider.getSimpleAttributeValue("surname"));
        consumer.setSimpleAttribute("description", provider.getSimpleAttributeValue("description"));
    }

}
