package pl.net.bluesoft.rnd.pt.ext.jbpm.utils;


/**
 * 
 * @author kkolodziej@bluesoft.net.pl
 *
 */
public enum NodeTypeEnum {
	
	XOR("decision"),TASK("task"),JAVA("java"),START("start"),END("end"),END_ERROR("end-error"),END_CANCEL("end-cancel"),ESB("esb"),SQL("sql"),HQL("hql"),SCRIPT("script"),JOIN("join"),CUSTOM("custom");
	
	private String name;
	
	private NodeTypeEnum(String name) {
		this.name = name;
	}
	
	public String getName(){
		
		return name;
	}

}

