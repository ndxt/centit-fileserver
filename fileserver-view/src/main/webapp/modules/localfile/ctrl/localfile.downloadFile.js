setI18NText("");
define(function(require) {
    var Core = require('core/core');
    var Page = require('core/page');
    var Dialog = require('centit/centit.dialog');
    var Config  =   require('config');
    var clickDown = require('../ctrl/localfile.clickDown');
    var watchDetail = require('../ctrl/localfile.watchDetail');
    // 角色信息列表
    return Page.extend(function() {
        //下载地址
        var downUrl =  Config.ContextPath+"service/local/download/";
        //请求文件信息地址
        var fileUrl =   'service/local/';
        //保存下载accessToken
        this.field = null;
        this.injecte([
            new clickDown('clickDown'),
            new watchDetail('watchDetail')
        ]);


        this.renderButton = function() {
            if(!this.parent.isBlockDownBtn){
                return false;
            }
            else {
                return true;
            }
        };
        this.load = function (panel) {
            var vm = this,
                table = vm.table = panel.find('#fileDown');
            vm.showPath = "";
            this.field = null;
            table.cdatagrid({
                // 必须要加此项!!
                controller: this,
                onDblClickRow:function(index,field){
                    var href;
                    if(field.encrypt) {
                        var desPwd = prompt("请输入des密码");
                        href = downUrl + field.accessToken +"?password="+desPwd;
                    }
                    else{
                        href = downUrl + field.accessToken;
                    }
                    window.open(href);
                },
                onBeforeSelect : function(index,field){
                    this.field = field;
                }.bind(this)
            });

            vm.reload();
        };

        this.reload = function() {
            var vm = this;
            var path = this.parent.dlFile.path;
            var fileName = this.parent.dlFile.fileName;
            var url;
            if(path){
                path = path +'/';
            }
            if(this.parent.unitCode===""){
                url = "userfile/";
            }
            else{
                url = "unitfile/"+this.parent.unitCode+"/";
            }
            Core.ajax(fileUrl +url+path + fileName)
                .then(function (data) {
                    if(data.length==1){
                        var href;
                        if(data[0].encrypt) {
                            var desPwd = prompt("请输入des密码");
                            href = downUrl + data[0].accessToken +"?password="+desPwd;
                        }
                        else{
                            href = downUrl + data[0].accessToken;
                        }
                        window.open(href);
                        Dialog.destroy(vm.panel);
                    }
                    else {
                        vm.table.datagrid('loadData', data);
                    }
                }.bind(this));

        };

    });
});