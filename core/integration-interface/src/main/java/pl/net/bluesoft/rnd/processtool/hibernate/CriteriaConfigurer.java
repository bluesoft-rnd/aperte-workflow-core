package pl.net.bluesoft.rnd.processtool.hibernate;

import org.hibernate.criterion.DetachedCriteria;

/**
 * @author: amichalak@bluesoft.net.pl
 */
public interface CriteriaConfigurer {
    void configure(DetachedCriteria criteria);
}
