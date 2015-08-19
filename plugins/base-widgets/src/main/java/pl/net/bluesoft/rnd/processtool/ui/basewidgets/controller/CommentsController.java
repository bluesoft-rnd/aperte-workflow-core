package pl.net.bluesoft.rnd.processtool.ui.basewidgets.controller;

import org.aperteworkflow.admin.controller.CommentTime;
import pl.net.bluesoft.rnd.processtool.web.controller.ControllerMethod;
import pl.net.bluesoft.rnd.processtool.web.controller.IOsgiWebController;
import pl.net.bluesoft.rnd.processtool.web.controller.OsgiController;
import pl.net.bluesoft.rnd.processtool.web.controller.OsgiWebRequest;
import pl.net.bluesoft.rnd.processtool.web.domain.GenericResultBean;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.TimeZone;

/**
 * Created by Marcin Król on 2014-06-30.
 */
@OsgiController(name = "commentsController")
public class CommentsController implements IOsgiWebController {

    @ControllerMethod(action = "getCommentTime")
    public GenericResultBean getCommentTime(final OsgiWebRequest invocation) {

        GenericResultBean resultBean = new GenericResultBean();

        CommentTime commentTime = new CommentTime();
        Calendar calendar = GregorianCalendar.getInstance(TimeZone.getTimeZone("Europe/Warsaw"));
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd-MM-yy HH:mm");
        simpleDateFormat.setTimeZone(TimeZone.getTimeZone("Europe/Warsaw"));

        commentTime.setTime(calendar.getTime());
        commentTime.setFormattedTime(simpleDateFormat.format(calendar.getTime()));

        resultBean.setData(commentTime);
        return resultBean;
    }
}
