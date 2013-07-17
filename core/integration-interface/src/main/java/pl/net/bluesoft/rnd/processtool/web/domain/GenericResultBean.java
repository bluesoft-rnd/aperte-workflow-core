package pl.net.bluesoft.rnd.processtool.web.domain;

/**
 * @author: "mpawlak@bluesoft.net.pl"
 */
public class GenericResultBean extends AbstractResultBean
{
    private Object data;

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }
}
