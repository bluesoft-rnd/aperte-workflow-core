var PASSWORD_MIN_MEDIUM_LENGTH = ${config.password.weak.chars};
var PASSWORD_MIN_MEDIUM_DIGITS = ${config.password.weak.min.digits};
var PASSWORD_MIN_MEDIUM_LOWERCASES = ${config.password.weak.min.lowercases};
var PASSWORD_MIN_MEDIUM_CAPITALLETTERS = ${config.password.weak.min.capitalletters};
var PASSWORD_MIN_MEDIUM_SPECIALS = ${config.password.weak.min.specials};

var PASSWORD_MIN_STRONG_LENGTH = ${config.password.strong.chars};
var PASSWORD_MIN_STRONG_DIGITS = ${config.password.strong.min.digits};
var PASSWORD_MIN_STRONG_LOWERCASES = ${config.password.strong.min.lowercases};
var PASSWORD_MIN_STRONG_CAPITALLETTERS = ${config.password.strong.min.capitalletters};
var PASSWORD_MIN_STRONG_SPECIALS = ${config.password.strong.min.specials};

var USER_LOGGED_COOKIE = "cookie_user_id";
var USER_LOGGED_EMAIL_COOKIE = "user_email";



function isLoggedIn() 
{
    var loggedIn = jQuery.cookie(USER_LOGGED_COOKIE);
    var logged = (loggedIn != null && loggedIn != "-1" && loggedIn != "");
    return logged;
}
