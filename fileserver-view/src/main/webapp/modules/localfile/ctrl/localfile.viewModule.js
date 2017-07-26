define(function(require) {
    var Page = require('core/page');


    var viewModule = Page.extend(function() {
        this.listModule = "../../modules/localfile/images/listModule.jpg";
        this.ViewModule = "../../modules/localfile/images/viewModule.jpg"
        this.submit = function() {
            this.parent.isBlockDownBtn = false;
            if(this.parent.nowViewModule === this.parent.IMGVIEW){
                this.parent.nowViewModule = this.parent.LISTVIEW;
            }
            else if(this.parent.nowViewModule === this.parent.LISTVIEW){
                this.parent.nowViewModule = this.parent.IMGVIEW;
            }
            this.parent.reload();
        };

        this.renderButton = function(){
            if(this.parent.nowViewModule === this.parent.IMGVIEW) {
                $("#viewModule").css("background", "url(" + this.ViewModule + ")");
            }
            else if(this.parent.nowViewModule === this.parent.LISTVIEW) {
                $("#viewModule").css("background", "url(" + this.listModule + ")");
            }
            $("#viewModule").css("background-size","cover");
        }
    });
    return viewModule;
});