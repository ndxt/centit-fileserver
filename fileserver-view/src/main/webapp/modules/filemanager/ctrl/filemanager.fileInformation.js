setI18NText("");
define(function(require) {
    var Page = require('core/page');
    var Core = require("core/core");
    // 角色信息列表
    return Page.extend(function() {
        //文件列表
        var fileList = 'service/access/list/';
        var vm;
        this.load = function (panel,data) {
            vm = this;
            vm.tableInfo = panel.find('#file_info');
            vm.tableDown = panel.find("#down_info");
            vm.tableDown.cdatagrid({
                // 必须要加此项!!
                controller: this
            });
            var loadData = [
                { name : '文件名', value : data['FILE_NAME'] },
                { name : '创建时间', value : data['CREATE_TIME'] },
                { name : '下载次数', value : data['DOWNLOAD_TIMES'] },
                { name : '加密类型', value : data['ENCRYPT_TYPE'] },
                { name : '文件ID', value : data['FILE_ID'] },
                { name : 'MD5', value : data['FILE_MD5'] },
                { name : '文件大小', value : data['FILE_SIZE'] },
                { name : '文件状态', value : data['FILE_STATE'] },
                { name : '文件类型', value : data['FILE_TYPE'] },
                { name : '文件机构', value : data['FILE_UNIT'] },
                { name : '索引状态', value : data['INDEX_STATE'] },
                { name : '系统ID', value : data['OPT_ID'] },
                { name : '操作类别', value : data['OS_ID'] }
                // {name : '存储路径', value : data['FILE_STORE_PATH'] },
                // { name : '系统方法', value : data['OPT_TAG'] },
                // { name : '系统标签', value : data['OS_ID'] }
            ];
            vm.tableInfo.propertygrid('loadData', loadData);
            Core.ajax(fileList+data['FILE_ID']).then(function(data){
                vm.tableDown.datagrid('loadData', data);
            });
        };
    });
});