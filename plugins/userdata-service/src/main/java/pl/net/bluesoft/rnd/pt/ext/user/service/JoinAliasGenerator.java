package pl.net.bluesoft.rnd.pt.ext.user.service;

/**
 * @author: amichalak@bluesoft.net.pl
 */
public class JoinAliasGenerator {
    private int index = 0;
    private String base;

    public JoinAliasGenerator(String base) {
        this.base = base;
    }

    public String next() {
        return base + ++index;
    }

    public String last() {
        return base + index;
    }
}
