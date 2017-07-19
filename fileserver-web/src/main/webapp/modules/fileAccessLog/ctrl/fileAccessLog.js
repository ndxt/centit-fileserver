setI18NText("");
define(function(require) {
//	var Config = require('config');
	var Core = require('core/core');
	var Page = require('core/page');
	var Dialog = require('centit/centit.dialog');
	var fileInformation = require('../ctrl/fileAccessLog.fileInformation');
	// 角色信息列表
	var FileAccessLog = Page.extend(function() {
		var table;
		var SimData = [];
		var i;
		//各类参数
		var osId,optId,owner;
		var ossUrl = 'service/files/oss';
		var osIdUrl = 'service/files/optids/';
		var optIdUrl = 'service/files/owner/';
		var watchDetailUrl = "service/files/";
		var openInformation = 'modules/fileAccessLog/fileAccessLog.fileInformation.html';
		//第一层下拉框
		this.selectOss;
		//第二层
		this.selectOsId;
		//第三层
		this.seletOptId;
		//文件信息
		var fileIn;
		this.injecte([
			fileIn =  new fileInformation('fileInformation'),
		]);
		this.load = function (panel) {
			var vm = this;
			table = vm.table = panel.find('table');
			table.cdatagrid({
				// 必须要加此项!!
				controller: this,
				onDblClickRow : function(index,field){
					Core.ajax(watchDetailUrl+field.fileId).then(function(data){
						Dialog.open({
							href: openInformation,
							height:500,
							width:500,
							title:data.FILE_NAME,
							buttons:"false"
						}, data,  fileIn);
					});
				}
			});
			//读取所有下拉框的ID
			this.selectOss = panel.find("#selectOss");
			this.selectOsId = panel.find("#selectOsId");
			this.seletOptId = panel.find("#seletOptId");
			Core.ajax(ossUrl).then(function(data){
				for(i = 0 ; i < data.length ; i ++){
					SimData[i] = {
						label:data[i].osId,
						value:data[i].osName
					}
				}
				this.selectOss.combobox('loadData',SimData);
				this.selectOss.combobox({
					onChange:ossSelected.bind(this),
				});
			}.bind(this));

		}.bind(this);


		//第一层选择函数
		function ossSelected(newData){
			//请求第二层数据
			SimData = [];
			//获取总的optId;
			osId = newData;
			this.seletOptId.combobox('clear');
			this.seletOptId.combobox('loadData',[]);
			if(!osId){
				this.selectOsId.combobox('clear');
				this.selectOsId.combobox('loadData',[]);
				return;
			}
			Core.ajax(osIdUrl+osId).then(function(data){
				//当没有数据时
				if(data){
					for (i = 0; i < data.length; i++) {
						SimData[i] = {
							label: data[i].OPT_ID,
							value: data[i].OPT_ID
						}
					}
				}
				//加载第二层
				this.selectOsId.combobox('loadData',SimData);
				//绑定第二层时间
				this.selectOsId.combobox({
					onChange : osIdSelected.bind(this),
				});
			}.bind(this))
		}

		function osIdSelected(data){
			//获取全局optId
			SimData = [];
			optId = data;
			if(!optId){
				this.seletOptId.combobox('clear');
				this.seletOptId.combobox('loadData',[]);
				return;
			}
			Core.ajax(optIdUrl+osId+'/'+optId).then(function(data){
				if(data){
					for(i = 0 ; i < data.length ;i++){
						SimData[i] = {
							label : data[i].FILE_OWNER,
							value : data[i].FILE_OWNER
						}
					}
				}
				this.seletOptId.combobox('loadData',SimData);
			}.bind(this))
		}

	});
	
	return FileAccessLog;
});