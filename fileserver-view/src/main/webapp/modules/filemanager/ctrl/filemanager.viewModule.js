define(function(require) {
    var Page = require('core/page');


    var manangerViewModule = Page.extend(function() {
        this.listModule = "../../modules/localfile/images/listModule.jpg";
        this.ViewModule = "../../modules/localfile/images/viewModule.jpg"
        this.submit = function() {
            if(this.parent.nowViewModule === this.parent.IMGVIEW){
                this.parent.nowViewModule = this.parent.LISTVIEW;

            }
            else if(this.parent.nowViewModule === this.parent.LISTVIEW){
                this.parent.nowViewModule = this.parent.IMGVIEW;
            }
            this.parent.reload();
        };
        //
        this.renderButton = function(){

            if(this.parent.nowViewModule === this.parent.IMGVIEW) {
                $("#manangerViewModule").css("background", "url(" + this.ViewModule + ")");
            }
            else if(this.parent.nowViewModule === this.parent.LISTVIEW) {
                $("#manangerViewModule").css("background", "url(" + this.listModule + ")");
            }
            $("#manangerViewModule").css("background-size","cover");
        }
    });
    return manangerViewModule;
});