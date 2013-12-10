package org.aperteworkflow.webapi.context.impl;

import pl.net.bluesoft.rnd.processtool.web.domain.IProcessToolRequestContext;

import javax.servlet.http.HttpServletRequest;

/**
 * @author: mpawlak@bluesoft.net.pl
 */
public interface IWebProcessToolContextFactory
{
    IProcessToolRequestContext create(final HttpServletRequest request);
}
