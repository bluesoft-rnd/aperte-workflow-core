// servlet address
var servletAddress = "/aperteworkflow/osgiex/queues";
// interval in miliseconds
var interval = 30000;
var auto_refresh;

function setCurrentUser(user) {
    createCookie("apw_refresher_settings", user, 360);
    return 1;
}

function setRefreshInterval(inter) {
    interval = inter;

    if (auto_refresh) clearInterval(auto_refresh);

    var func = refresh;
    auto_refresh = setInterval(func, interval);

    return 1;
}

function clearRefreshCurrentUser() {
    createCookie("apw_refresher_settings", null, 360);
}

function refresh() {
    var currentUser = readCookie("apw_refresher_settings");
    if (!currentUser || currentUser == "null" || currentUser == "") {
        return 1;
    }

    var address;
    if (window.location.origin) {
        address = window.location.origin + servletAddress;
    } else if (window.location.host) {
        address = window.location.protocol + "//" + window.location.host + servletAddress + "?t=" + (Math.random() * 99999);
    }

    $.ajaxSetup({
        cache: false
    });

    $.ajax({
        type: "GET",
        url: address,
        data: {
            userLogin: currentUser
        },
        success: function (data) {
            $.each(data, function (user, mainUserQueues) {
                var totalTasks = 0;
                var totalQueues = 0;

                $.each(mainUserQueues, function (key, value) {
                    if (key.indexOf("substituted-" != -1)) {
                        if (key.indexOf("user-task-name") != -1) {
                            totalTasks = totalTasks + value;
                        }
                        if (key.indexOf("user-queue-name") != -1) {
                            totalQueues = totalQueues + value;
                        }
                    }

                    var newQueueSize = value;
                    var obj = $("div." + key + " span").last();

                    if (!obj || obj.length == 0) {
                        return true;
                    }
                    var innerText = obj.html();

                    var index = innerText.indexOf("(");

                    if (!index || index <= 0) {
                        return true;
                    }
                    var caption = innerText.substring(0, index);
                    obj.html(caption + "(" + newQueueSize + ")");

                    var divObj = $("div." + key).last();
                    if (newQueueSize > 0) {
                        divObj.removeClass("v-disabled");
                    } else {
                        divObj.addClass("v-disabled");
                    }
                });

                var nodeClass = "substituted-" + user + "-user-root-node";
                var obj = $("div.v-tree-node-caption-" + nodeClass + " span").last();

                if (!obj || obj.length == 0) {
                    return true;
                }
                var innerText = obj.html();

                var index = innerText.indexOf("(");

                if (!index || index <= 0) {
                    return true;
                }
                var caption = innerText.substring(0, index);
                obj.html(caption + "(" + totalTasks + "," + totalQueues + ")");

            });
        }
    });

}

$(window).bind('unload', function () {
    clearRefreshCurrentUser();
});

$(document).ready(function () {
    var func = refresh;
    auto_refresh = setInterval(func, interval);
});