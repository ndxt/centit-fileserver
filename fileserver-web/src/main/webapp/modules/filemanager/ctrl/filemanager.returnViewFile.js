define(function(require) {
    var Page = require('core/page');

    // 发布版本
    var returnViewFile = Page.extend(function() {
        this.submit = function() {
            if(this.parent.STATE === this.parent.TOPSTATE){
                alert("已经在最顶层");
                return;
            }
            else if(this.parent.STATE === this.parent.CLASSSTATE){
                this.parent.STATE = this.parent.TOPSTATE;
            }
            else if(this.parent.STATE === this.parent.OWNERSTATE){
                this.parent.STATE = this.parent.CLASSSTATE;
            }
            else if(this.parent.STATE === this.parent.FILESTATE){
                this.parent.STATE = this.parent.OWNERSTATE;
            }
            this.parent.reload();
        };
    });
    return returnViewFile;
});