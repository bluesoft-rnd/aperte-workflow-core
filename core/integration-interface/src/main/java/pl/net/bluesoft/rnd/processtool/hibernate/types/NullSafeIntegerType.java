package pl.net.bluesoft.rnd.processtool.hibernate.types;

import org.hibernate.engine.SessionImplementor;
import org.hibernate.type.IntegerType;

/**
 * @author: amichalak@bluesoft.net.pl
 */
public class NullSafeIntegerType extends IntegerType {
    @Override
    public Integer next(Integer current, SessionImplementor session) {
        return current != null ? super.next(current, session) : Integer.valueOf(1);
    }
}
