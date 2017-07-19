define(function(require) {
    var Page = require('core/page');

    // 发布版本
    var returnFile = Page.extend(function() {
        this.submit = function() {
            if((this.parent.showPath === "")&&(!this.parent.isInTop) ){
                this.parent.isInTop = true;
            }
            else if((this.parent.showPath === "")&&(this.parent.isInTop)){
                alert("已经在最顶层");
                return;
            }
            this.parent.showPath = this.parent.showPath.substr(0,this.parent.showPath.lastIndexOf('/'));
            this.parent.pathArray.pop();
            this.parent.isBlockDownBtn = false;
            this.parent.renderButtonEvent();
            this.parent.reload();
        };
    });
    return returnFile;
});