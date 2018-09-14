setI18NText("");
define(function(require) {
    var $ = require('jquery');
	var Config = require('config');
    var Core = require('core/core');
	var Page = require('core/page');
    var uploadAdd =require('../ctrl/localfile.uploadAdd');
    var downloadFile =require('../ctrl/localfile.downloadFile');
    var returnFile = require('../ctrl/localfile.returnFile');
    var viewModule = require('../ctrl/localfile.viewModule');
    var watchDetail = require('../ctrl/localfile.watchDetail');

	// 角色信息列表
	return Page.extend(function() {
	    //大的表格
        var url= "service/local/userdir";
        //保存当前文件数据
        var fileData ;
        //文件路径
        var zipSrc = "../../modules/localfile/images/zip.jpg";
        var fileSrc = "../../modules/localfile/images/file.jpg";
        var lockSrc = "../../modules/localfile/images/lock.jpg";
        //是否在最顶层
        this.isInTop = true;
        //是否可以点亮下载图标
        // this.isBlockDownBtn = false;
        //保存当前所有路径数组
        this.pathArray = [
            {path:'top'}
        ];
        //当前路径
        this.showPath = "";
        //下载参数
        this.dlFile = {};
        this.userCode= GLOBAL_IDENTIFY.userCode,this.unitCode="";

        this.injecte([
            new uploadAdd('uploadAdd'),
            new downloadFile('downloadFile'),
            new returnFile('returnFile'),
            new viewModule('viewModule'),
            new watchDetail('watchDetail')
        ]);
        //topUrl
        var topUrl = 'service/local/catalog';
        //模式选择
        this.IMGVIEW = 1;
        this.LISTVIEW = 2;
        //保存renderRow最初始方法
        this.saveRenderRowFun;
        //当前模式
        this.nowViewModule = this.LISTVIEW;
        //保存当前文件
        this.field = null;

        this.load = function (panel) {
            this.table  = panel.find('#fileManagerSystem');
            this.table.cdatagrid({
                // 必须要加此项!!
                controller: this,
                onBeforeSelect: function (index, field) {
                    this.onSelectRow(index,field);
                    if(field.fileType === "f") {
                        this.field = field;
                    }
                    else{
                        this.field = null;
                    }
                }.bind(this),
                onDblClickRow: function (index, field) {
                    this.onDblClickRow(index,field);
                }.bind(this)
            });

            this.saveRenderRowFun =  this.table.cdatagrid('options').view.renderRow;


            //更新数据
            this.reload();

            //绑定事件
            this.bindEvent();
        };



        //重新加载列表
        this.reload = function() {

            //模式选择
            this.switchView();
            //是否在最顶层
            if(this.isInTop) {
                //请求文件夹数量并造虚假数据
                Core.ajax(topUrl)
                    .then(function (data) {
                        var simData = [];
                        simData[0] = {'fileName':'个人文件','value':""};
                        for(var i = 0 ; i <data.length;i++){
                            simData[i+1] = {'fileName':""};
                            simData[i+1].fileName = data[i].unitName;
                            simData[i+1].value = data[i].unitCode;
                        }
                        this.table.datagrid('loadData', simData);
                        fileData = simData;
                    }.bind(this));
            }
            //请求文件夹下的文件列表
            else{
                Core.ajax(url + this.showPath).then(function (data) {
                    this.table.datagrid('loadData', data);
                    fileData = data;
                }.bind(this));
            }
            //更新路径
            this.renderPath(this.pathArray);
        };


        //更新路径
        this.renderPath = function(arr){
            var pathContainer = $('#showUrlPath');
            var aElem;
            pathContainer.empty();
            for(var i = 0 ; i <arr.length ; i ++){
                aElem = document.createElement('a');
                aElem.innerHTML = (i==arr.length-1)?arr[i].path:arr[i].path+'/';
                aElem.setAttribute('value',i+1);
                pathContainer.append(aElem);
            }
        };

        //绑定事件函数
        this.bindEvent = function(){
            //清除所有事件
            $('body').unbind();
            //绑定路径
            $('#showUrlPath').on('click','a',function(e){
                e.preventDefault();
                var index = e.target.getAttribute('value');
                this.pathArray.splice(index);
                if(index == 1){
                    this.isInTop = true;
                    this.showPath = "";
                }
                else{
                    if(this.pathArray.length>2) {
                        this.showPath = "/";
                    }
                    else {
                        this.showPath = "";
                    }
                    for(var i = 2 ; i < this.pathArray.length;i++){
                        var last = ((i==this.pathArray.length-1)?"": '/');
                        this.showPath += ''+this.pathArray[i].path + last;
                    }
                    this.isInTop = false;
                }
                this.isBlockDownBtn = false;
                this.renderButtonEvent();
                this.reload();
            }.bind(this));
            //绑定图标时的点击事件
            $('body').on('mousedown','div.clickRow',function(e){
                e.preventDefault();
                //将target定位给父元素div
                var target = (e.target.nodeName!=='DIV')?e.target.parentNode: e.target;
                var index = target.getAttribute('index');
                $('.imgRowSelected').removeClass('imgRowSelected');
                target.className += " imgRowSelected";
                this.onSelectRow(index,fileData[index]);
                this.field = fileData[index];
                this.renderButtonEvent();
            }.bind(this));

            $('body').on('dblclick','div.clickRow',function(e){
                e.preventDefault();
                //定位给父元素
                var target = (e.target.nodeName != "DIV")?e.target.parentNode:e.target;
                var index = target.getAttribute('index');
                this.onDblClickRow(index,fileData[index]);
            }.bind(this));
        };


        //切换显示模式
        this.switchView = function(){
            this.field = null;
            if(this.nowViewModule === this.IMGVIEW){
                this.table.cdatagrid('getPanel').find('.datagrid-header').hide();
                this.table.cdatagrid('options').view.renderRow = function(target, fields, frozen, rowIndex, rowData) {
                    //如果为空对象直接返回
                    if(JSON.stringify(rowData)=="{}" ){
                        return;
                    }
                    var path;
                    var encrypt ="";
                    var verision = "";
                    var versionNum;
                    if(rowData.fileType === "f"){
                        path = zipSrc;
                        versionNum = (rowData.versions>9)?"9+":rowData.versions;
                        verision = '<i style="position:absolute;top:5%;left:85%;font-size:14px;font-weight:bold;">'+versionNum+'</i>';
                    }
                    else{
                        path = fileSrc;
                    }
                    if(rowData.encrypt){
                        encrypt = '<img style="position:absolute;top:23%;left:85%;width:15px;height:15px;" src = '+lockSrc+'>';
                    }
                    return '<div  index = '+rowIndex+' class="clickRow" >'+
                        '<img   src ='+path+' style="width:70px;height:70px;margin-left:10px;">'+
                        verision+
                        encrypt+
                        '<p style="text-align: center">'+
                        rowData.fileName+
                        '</p>'+
                        '</div>';
                };
            }
            else if(this.nowViewModule === this.LISTVIEW){
                this.table.cdatagrid('getPanel').find('.datagrid-header').show();
                this.table.cdatagrid('options').view.renderRow = this.saveRenderRowFun;
            }
        };

        //选中
        this.onSelectRow = function (index, field) {
            if(!field.fileType){
                this.isBlockDownBtn = false;
            }
            else if(field.fileType === 'f'){
                this.isBlockDownBtn = true;
            }
            else{
                this.isBlockDownBtn = false;
            }
            this.dlFile.fileName = field.fileName;
            this.dlFile.path = this.showPath;
            this.dlFile.path = this.dlFile.path[0] === "/" ? this.dlFile.path.substr(1) : this.dlFile.path;
        }.bind(this);

        //双击事件
        this.onDblClickRow = function(index,field){
            if (this.isInTop) {
                this.showPath = "";
                if (field.value) {
                    url = "service/local/unitdir/" + field.value + this.showPath;
                    this.userCode = "";
                    this.unitCode = field.value;
                }
                else {
                    url = "service/local/userdir";
                    this.userCode = GLOBAL_IDENTIFY.userCode;
                    this.unitCode = "";
                }
                this.isInTop = false;
                this.pathArray.push({path: field.fileName});
                this.reload();
            }
            else {
                if (field.fileType === "d") {
                    this.showPath = this.showPath + '/' + field.fileName;
                    this.pathArray.push({path: field.fileName});
                    this.reload();
                }
                else{
                    this.panel.find("#downloadFile").click();
                }
            }
        }.bind(this);

        //eaysui原生渲染按钮函数
        this.renderButtonEvent = function() {
            var panel =  this.table.datagrid('getPanel');
            var rows =  this.table.datagrid('getSelections');
            panel.find('.datagrid-toolbar a').each(function() {
                var btn = $(this), trigger = btn.data('trigger'), renderButton = btn.data('controller').renderButton;
                var result;
                btn.linkbutton('enable');

                // 什么也没有选择
                if (trigger == 'none') {
                    renderButton && (result = renderButton(btn));
                }
                // 选择了一条记录
                else if (rows.length == 1 && (trigger == 'single' || trigger == 'multiple')) {
                    renderButton && (result = renderButton(btn, rows[0]));
                }
                // 选择了多条记录
                else if (rows.length > 1 && trigger == 'multiple') {
                    renderButton && (result = renderButton(btn, rows));
                }
                else {
                    btn.linkbutton('disable');
                }

                if (result === true) {
                    btn.linkbutton('enable');
                }
                else if (result === false) {
                    btn.linkbutton('disable');
                }
            });
        };
    });
});
