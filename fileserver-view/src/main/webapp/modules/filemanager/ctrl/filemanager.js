setI18NText("");
define(function(require) {
    var $ = require('jquery');
    // var Config = require('config');
    var Core = require('core/core');
    var Page = require('core/page');
    var returnViewFile = require('../ctrl/filemanager.returnViewFile');
    var fileInformation = require('../ctrl/filemanager.fileInformation');
    var manangerViewModule = require('../ctrl/filemanager.viewModule');
    var Dialog = require('centit/centit.dialog');

    // 角色信息列表
    return Page.extend(function() {
        //层级状态
        this.TOPSTATE;
        this.CLASSSTATE;
        this.OWNERSTATE;
        this.FILESTATE;
        this.STATE;

        this.osId;
        this.optId;
        this.owner;
        this.filePathImage = "../../modules/filemanager/images/file.jpg";
        this.fileImage = "../../modules/filemanager/images/zip.jpg";
        //缓存数据
        this.BufferArray = [];
        var fileIn;
        this.injecte([
            new returnViewFile('returnViewFile'),
            new manangerViewModule('manangerViewModule'),
            fileIn =  new fileInformation('fileInformation')
        ]);
        //打开文件详细信息地址
        var openFileDetail = 'modules/filemanager/filemanager.fileInformation.html';

        //各层url
        var topUrl = 'fileserver/files/oss';
        var classUrl = 'fileserver/files/optids/';
        var ownerUrl = 'fileserver/files/owner/';
        var fileUrl = 'fileserver/files/files/';
        this.load = function (panel) {
            this.TOPSTATE = 1;
            this.CLASSSTATE = 2;
            this.OWNERSTATE = 3;
            this.FILESTATE = 4;
            this.STATE = this.TOPSTATE;
            var vm = this;
            //当前显示模式
            this.IMGVIEW = 1;
            this.LISTVIEW = 2;

            this.saveRenderRowFun;
            //当前模式
            this.nowViewModule = this.LISTVIEW;
            this.table  = panel.find('#fileSystemTable');
            this.table.cdatagrid({
                // 必须要加此项!!
                controller: this,
                onDblClickRow: function (index,field) {
                    this.onDblClickRow(index,this.BufferArray[index]);
                }.bind(this)
            });


            //保存原生显示方法
            this.saveRenderRowFun =  this.table.cdatagrid('options').view.renderRow;

            //更新数据
            vm.reload();
            //绑定事件
            this.bindEvent();
        }.bind(this);

        //重新加载列表
        this.reload = function() {
            this.switchView();
            var url;
            var simData = [];
            if(this.STATE === this.TOPSTATE){
                url = topUrl;
            }
            else if(this.STATE === this.CLASSSTATE){
                url = classUrl+this.osId;
            }
            else if(this.STATE === this.OWNERSTATE){
                url = ownerUrl+this.osId+'/'+this.optId;
            }
            else if(this.STATE === this.FILESTATE){
                url = fileUrl+this.osId+'/'+this.optId+'/'+this.owner;
            }


            Core.ajax(url)
                .then(function (data) {
                    data= (data===undefined)?[]:data;
                    this.BufferArray = data;
                    if(this.nowViewModule === this.LISTVIEW) {
                        simData = [];
                        //模拟数据
                        this.simData(data,simData);
                    }
                    else if(this.nowViewModule === this.IMGVIEW){
                        simData = data;
                    }
                    //保存一份数据
                    this.table.datagrid('loadData',simData);
                }.bind(this));
        };

       //绑定事件
        this.bindEvent = function(){
            //清楚所有事件
            $('body').unbind();
            //点击文件夹
            $('body').on('dblclick','div.viewClickRow',function(e){
                e.preventDefault();
                //定位给父元素
                var target = (e.target.nodeName !== "DIV")?e.target.parentNode:e.target;
                var index = target.getAttribute('index');
                if(this.STATE === this.FILESTATE) {
                    Dialog.open({
                        href: openFileDetail,
                        height:500,
                        width:500,
                        title:this.BufferArray[index].FILE_NAME,
                        buttons:"false"
                    }, this.BufferArray[index],  fileIn);
                }else{
                    this.onDblClickRow(index, this.BufferArray[index]);
                }
            }.bind(this));
            //单击选中
            $('body').on('mousedown','div.viewClickRow',function(e){
                var target = (e.target.nodeName!=='DIV')?e.target.parentNode: e.target;
                // var index = target.getAttribute('index');
                $('.imgRowSelected').removeClass('imgRowSelected');
                target.className += " imgRowSelected";
            }.bind(this));
        };

        //切换模式
        this.switchView = function(){
            this.field = null;
            if(this.nowViewModule === this.IMGVIEW){
                this.table.cdatagrid('getPanel').find('.datagrid-header').hide();
                this.table.cdatagrid('options').view.renderRow = function(target, fields, frozen, rowIndex, rowData) {
                    //如果为空对象直接返回
                    if(JSON.stringify(rowData)==="{}" ){
                        return;
                    }
                    //显示文件名
                    var name;
                    var src = this.filePathImage;
                    if(this.STATE === this.TOPSTATE){
                        name = rowData.osName;
                    }
                    else if(this.STATE === this.CLASSSTATE){
                        name = rowData.OPT_ID;
                    }
                    else if(this.STATE === this.OWNERSTATE){
                        name = rowData.FILE_OWNER;
                    }
                    else if(this.STATE === this.FILESTATE){
                        name = rowData.FILE_NAME;
                        src = this.fileImage;
                    }
                    return '<div  index = '+rowIndex+' class="viewClickRow" >'+
                        '<img   src ="'+src+'" style="width:70px;height:70px;margin-left:10px;">'+
                        '<p style="text-align: center">'+
                        name+
                        '</p>'+
                        '</div>';
                }.bind(this);
            }
            else if(this.nowViewModule === this.LISTVIEW){
                this.table.cdatagrid('getPanel').find('.datagrid-header').show();
                this.table.cdatagrid('options').view.renderRow = this.saveRenderRowFun;
            }
        };

        //双击事件
        this.onDblClickRow = function(index,field){
            //改变状态
            if(this.STATE === this.TOPSTATE) {
                this.STATE = this.CLASSSTATE;
                this.osId = field.osId;
            }
            else if(this.STATE === this.CLASSSTATE) {
                this.STATE = this.OWNERSTATE;
                this.optId = field.OPT_ID;
            }
            else if(this.STATE === this.OWNERSTATE) {
                this.STATE = this.FILESTATE;
                this.owner = field.FILE_OWNER;
            }
            else if(this.STATE === this.FILESTATE) {
                Dialog.open({
                    href: openFileDetail,
                    height:500,
                    width:500,
                    title:this.BufferArray[index].FILE_NAME,
                    buttons:"false"
                }, this.BufferArray[index],  fileIn);
            }
            //重新加载数据
            this.reload();
        }.bind(this);

        //原生
        this.simData = function(data,simData){
            if (this.STATE === this.TOPSTATE) {
                data.forEach(function (value) {
                    simData.push({osName: value.osName, osId: value.osId});
                });
            }
            else if (this.STATE === this.CLASSSTATE) {
                data.forEach(function (value) {
                    simData.push({osName: value.OPT_ID, osId: value.OPT_ID});
                });
            }
            else if(this.STATE === this.OWNERSTATE){
                data.forEach(function (value) {
                    simData.push({osName: value.FILE_OWNER, osId: value.FILE_OWNER});
                });
            }
            else if(this.STATE === this.FILESTATE){
                data.forEach(function (value) {
                    simData.push({osName: value.FILE_NAME, osId: value.FILE_ID});
                });
            }
        }
    });
});
