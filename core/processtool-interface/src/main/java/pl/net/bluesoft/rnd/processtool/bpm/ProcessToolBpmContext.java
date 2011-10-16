package pl.net.bluesoft.rnd.processtool.bpm;

/**
 * @author tlipski@bluesoft.net.pl
 */
public class ProcessToolBpmContext {

	private ProcessToolSessionFactory sessionFactory;

	public void setSessionFactory(ProcessToolSessionFactory sessionFactory) {
		this.sessionFactory = sessionFactory;
	}

	public ProcessToolSessionFactory getSessionFactory() {
		return sessionFactory;
	}

}
