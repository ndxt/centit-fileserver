define(function(require) {
    var Page = require('core/page');
    var Config = require('config');
    //点击下载
    var clickDown = Page.extend(function() {
        this.submit = function() {
            var downUrl = Config.ContextPath+"fileserver/local/download/";
            var href;
            var field = this.parent.field;
            if(field.encrypt) {
                var desPwd = prompt("请输入des密码");
                href = downUrl + field.accessToken +"?password="+desPwd;
            }
            else{
                href = downUrl + field.accessToken;
            }
            window.open(href);
        };

        this.renderButton = function() {
            if(!this.parent.field){
                return false;
            }
            else {
                return true;
            }

        };
    });
    return clickDown;
});
