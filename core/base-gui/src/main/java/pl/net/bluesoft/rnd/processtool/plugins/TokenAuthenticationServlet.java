package pl.net.bluesoft.rnd.processtool.plugins;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import pl.net.bluesoft.rnd.processtool.ProcessToolContext;
import pl.net.bluesoft.rnd.processtool.ProcessToolContextCallback;
import pl.net.bluesoft.rnd.processtool.bpm.ProcessToolBpmConstants;
import pl.net.bluesoft.rnd.processtool.bpm.ProcessToolBpmConstants.TextModes;
import pl.net.bluesoft.rnd.processtool.di.ObjectFactory;
import pl.net.bluesoft.rnd.processtool.model.token.AccessToken;
import pl.net.bluesoft.rnd.processtool.plugins.util.HtmlTextDecorator;
import pl.net.bluesoft.rnd.processtool.plugins.util.IWriterTextDecorator;
import pl.net.bluesoft.rnd.processtool.plugins.util.PlainTextDecorator;
import pl.net.bluesoft.rnd.processtool.token.ITokenService;
import pl.net.bluesoft.rnd.processtool.token.TokenWrapper;
import pl.net.bluesoft.rnd.util.i18n.I18NSource;
import pl.net.bluesoft.rnd.util.i18n.I18NSourceFactory;

/**
 * Servlet which provides logic for authentication with one-use token. 
 * If action is not performed due to exception, token is not being used. 
 * 
 * @author mpawlak@bluesoft.net.pl
 *
 */
public abstract class TokenAuthenticationServlet extends HttpServlet 
{

	private static Logger				logger			= Logger.getLogger(TokenAuthenticationServlet.class.getName());
	
	/** Token ID paramater name */
	private static final String			TOKEN_PARAMETER = "tokenId";
	
	/** Request attribute. Print writer */
	protected static final String 		PRINT_WRITER = "printWriter";
	
	/** Request attribute. Text decorator */
	protected static final String 		TEXT_DECORATOR = "textDecorator";
	
	/** Request attribute. i18N source */
	protected static final String 		I18NSOURCE = "i18NSource";
	
	/** Request attribute. Process context */
	protected static final String 		PPROCESSTOOL_CONTEXT = "processToolContext";
	
	
	
	/** After authentication with current token this method is called. There is available context for
	 * given user
	 *  
	 * @param context
	 * @param task bpm task
	 * @param user authenticated user
	 */
	protected abstract void processRequest(HttpServletRequest req, ProcessToolContext ctx, TokenWrapper tokenWrapper) throws IllegalStateException;

	@Override
	protected void doGet(final HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException 
	{
		ProcessToolRegistry reg = (ProcessToolRegistry) getServletContext().getAttribute(ProcessToolRegistry.class.getName());
		
		/* Get the mime type. If no is provided, use plan text mode */
		String textModeParameter = req.getParameter(ProcessToolBpmConstants.TEXT_MODE);
		TextModes mode = TextModes.getTextModeType(textModeParameter);
		
		final PrintWriter out = resp.getWriter();
		final I18NSource i18NSource = I18NSourceFactory.createI18NSource(req.getLocale());
		
		/* Read text decoration mode */
		if(mode == null)
			mode = TextModes.PLAIN;
		
		/* Create text decorator based on text mode */
		final IWriterTextDecorator textDecorator = getTextDecorator(mode, i18NSource);
		
		resp.setContentType(mode.getMode());
		
		/* Add attributes to request */
		req.setAttribute(PRINT_WRITER, out);
		req.setAttribute(I18NSOURCE, i18NSource);
		req.setAttribute(ProcessToolBpmConstants.TEXT_MODE, mode);
		req.setAttribute(TEXT_DECORATOR, textDecorator);
		
		/* Check if there is token id parameter in request */
		final String tokenId = req.getParameter(TOKEN_PARAMETER);
		if(tokenId == null)
		{
			textDecorator.addText(i18NSource.getMessage("token.servlet.notokenspecified"));
			
			/* Write to user output page */
			out.write(textDecorator.getOutput());
			return;
		}
		
		reg.withProcessToolContext(new ProcessToolContextCallback() {
			
			@Override
			public void withContext(ProcessToolContext ctx) 
			{
				req.setAttribute(PPROCESSTOOL_CONTEXT, ctx);
				
				ITokenService accessTokenFacade = ObjectFactory.create(ITokenService.class);
				AccessToken accessToken = accessTokenFacade.getTokenByTokenId(tokenId);
				
				if(accessToken == null)
				{
					textDecorator.addText(i18NSource.getMessage("token.servlet.notokenfound", "",tokenId));
					
					/* Write to user output page */
					out.write(textDecorator.getOutput());
					return;
				}
				
				
				TokenWrapper tokenWrapper = accessTokenFacade.wrapAccessToken(accessToken);

				processRequest(req, ctx, tokenWrapper);
				
				req.removeAttribute(PPROCESSTOOL_CONTEXT);
				
			}
		});
		

		out.close();
		
		req.removeAttribute(PRINT_WRITER);
		req.removeAttribute(I18NSOURCE);
		req.removeAttribute(TEXT_DECORATOR);
	}
	@Override
	public void init() throws ServletException {
		super.init();
		logger.info(this.getClass().getSimpleName() + " INITIALIZED: " + getServletContext().getContextPath());
	}

	@Override
	public void destroy() {
		super.destroy();
		logger.info(this.getClass().getSimpleName() + " DESTROYED");
	}
	
	private IWriterTextDecorator getTextDecorator(TextModes mode, I18NSource i18NSource)
	{
		if(mode == null)
			return new PlainTextDecorator();
		else if(mode.equals(TextModes.HTML))
			return new HtmlTextDecorator(i18NSource);
		else
			return new PlainTextDecorator();
	}
	
}
