define(function(){
//	var Core = require('core/core');
	var Page = require('core/page');
	var Config = require('config');

	var FileAccessLogList = Page.extend(function(){
		this.load=function(panel,data){

			panel.find('table').cdatagrid({
				url:Config.ContextPath+'fileserver/access/list/'+data.fileId,
				controller: this
			});
		};
	});
	return FileAccessLogList;
});
