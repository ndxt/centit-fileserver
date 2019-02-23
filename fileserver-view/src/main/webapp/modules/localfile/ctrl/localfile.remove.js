define(function(){
	var Core = require('core/core');
	var Page = require('core/page');

	var FilemanagerRemove = Page.extend(function(){
		this.submit= function(table,data){
			Core.ajax('fileserver/files/'+data.fileId,{
				method:"delete"
			}).then(function(){
				table.datagrid('reload');
			});
		};
	});
	return FilemanagerRemove;
});
