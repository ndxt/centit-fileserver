setI18NText("");
define(function(require) {
    var Page = require('core/page');
    var Core = require("core/core");
    var Dialog = require('centit/centit.dialog');
    // 角色信息列表
    return Page.extend(function() {

        var vm;
        var watchDetail = "service/files/";
        var downLisrUrl = 'service/access/list/';
        this.load = function (panel) {
            vm = this;
            vm.tableInfo = panel.find('#file_detail');
            vm.tableDown = panel.find("#down_detail");
            vm.tableDown.cdatagrid({
                // 必须要加此项!!
                controller: this,
            });
            if(this.parent.field.versions>1){
                Dialog.destroy(panel);
                this.parent.panel.find("#downloadFile").click();
                return;
            }
            Core.ajax(watchDetail+this.parent.field.accessToken).then(function(data){
                var loadData = [
                    { name : '文件名', value : data['fileName'] },
                    { name : '创建时间', value : data['createTime'] },
                    { name : '下载次数', value : data['downloadTimes'] },
                    { name : '加密类型', value : data['encryptType'] },
                    { name : '文件ID', value : data['fileId'] },
                    { name : 'MD5', value : data['fileMd5'] },
                    { name : '文件大小', value : data['fileSize'] },
                    { name : '文件状态', value : data['fileState'] },
                    { name : '文件类型', value : data['fileType'] },
                    { name : '文件机构', value : data['fileUnit'] },
                    { name : '索引状态', value : data['indexState'] },
                    { name : '系统ID', value : data['optId'] },
                    { name : '操作类别', value : data['osId'] },
                    // { name : '存储路径', value : data['fileStorePath'] },
                    // { name : '系统方法', value : data['optMethod'] },
                    // { name : '系统标签', value : data['optTag'] }
                ];
                vm.tableInfo.propertygrid('loadData', loadData);
            });
            Core.ajax(downLisrUrl+this.parent.field.accessToken).then(function(data){
                vm.tableDown.datagrid('loadData', data);
            });
        };

        //渲染按钮
        this.renderButton = function() {
            if(!this.parent.field){
                return false;
            }
            else {
                return true;
            }

        };
    });
});