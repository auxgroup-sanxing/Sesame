/**
 * @class Dialog.OpenDialog
 * 打开对话框类. 
 * @constructor
 * @param {Object} config The config object
 */

/**
 * @cfg {String} url
 * 接收对话框的请求，返回文件列表以及其它操作 (缺省为同目录下的 dialog_ctrl.jsp).
 */
/**
 * @cfg {Boolean} disableCreate
 * 禁止创建目录
 */
/**
 * @cfg {String} filter
 * 文件名过滤器，正则表达式。如果不指定则返回所有文件
 */
/**
 * @cfg {Boolean} forceSelection
 * 是否强制选择，强制选择时不能输入文件名
 */

/**
 * @event exception
 * Fires if an error HTTP status was returned from the server.
 * @param {Object} response The XHR object containing the response data.
 */
var Dialog = {

url: (function (){
	var scriptList=document.getElementsByTagName("script");
	var scriptEl=scriptList[scriptList.length-1];
	var href=scriptEl.getAttribute("src");
	var loc= href.substr(0, href.lastIndexOf('/')+1);
	var linkEl = document.createElement("link");
	linkEl.setAttribute('rel', 'stylesheet');
	linkEl.setAttribute('type', 'text/css');
	linkEl.setAttribute('href', loc+'resources/dialog.css');
	scriptEl.parentNode.appendChild(linkEl);
	return loc+'dialog_ctrl.jsp';
})(),

OpenDialog: function(config){
	var a = config || {};

	var fileStore = new Ext.data.JsonStore({
		url: a.url || Dialog.url,
		baseParams: {	operation:'listFiles', path: a.path||'', filter: a.filter },
		root: 'items',
		fields: [
			{name:'cls', type: 'string'},
			{name:'name', type:'string'},
			{name:'content', type:'string'}
		]
	});
	fileStore.on('loadexception', function(proxy, obj, response){ dlg.fireEvent('exception', response); });
	fileStore.on('load', function(sender, records, options){
		sender.sort("cls", "DESC");
	});
	fileStore.on('beforeload', function(sender, options){
		var toolbar = dlg.getTopToolbar();
		toolbar.items.get('up').setDisabled(sender.baseParams.path.indexOf('/')<1);
	});
	
	var tpl = new Ext.XTemplate('<tpl for=".">', '<div class="thumb-wrap x-dialog-item" style="width: 120px; white-space: nowrap; text-overflow: ellipsis; overflow: hidden; float: left;" id="{name}">', 
		'<div class="thumb x-unselectable" unselectable="on" style="line-height:18px;">', 
		'<img style="width:16px; height:16px; vertical-align:middle;" class="{cls}" src="' + Ext.BLANK_IMAGE_URL + '" />', 
		'<span style="vertical-align:middle;">{name}</span></div></div>', '</tpl>', '<div class="x-clear"></div>');

	var dataView = new Ext.DataView({
		store: fileStore,
		tpl: tpl,
		style: 'overflow:auto;',
		autoHeight: false,
		multiSelect: false,
		singleSelect: true,
		overClass: 'x-dialog-over',
		selectedClass: 'x-dialog-selected',
		itemSelector: 'div.thumb-wrap',
		loadingText: '正在载入...',
		listeners: {
			containerclick: function(sender, e){
				var form = dlg.getComponent('filepanel').getForm();
				form.reset();
			},
			beforeselect: function(sender, node, selections){
				var form = dlg.getComponent('filepanel').getForm();
				var record = sender.getRecord(node);
				form.setValues({ filename: record.get('cls')=='x-dialog-file'?record.get('name'):'' });
			},
			dblclick: function( sender, index, node, e){
				dlg.ok.handler();
			},
			selectionchange: function(sender, selections){
				dlg.ok.setDisabled(a.forceSelection && selections.length<1);
			}
		}
	});
	
	var dlg = new Ext.Window(Ext.apply(a, {
		title: a.title||'打开',
		autoCreate: true,
		resizable: true,
		constrain: true,
		constrainHeader: true,
		minimizable: false,
		maximizable: false,
		stateful: false,
		modal: true,
		shim: true,
		buttonAlign: "right",
		defaultButton: 0,
		minHeight: 200,
		minWidth: 300,
		footer: true,
		closable: true,
		layout: 'border',
		tbar: [{
			id: 'up',
			cls: 'x-btn-icon',
			iconCls: 'x-dialog-up',
			tooltip: '向上一级',
			handler: function(){
				var path = fileStore.baseParams.path;
				fileStore.baseParams.path = path.replace(/(\/[^\/]*\/$)|(\/[^\/]*$)/, '');
				fileStore.load();
			}
		},{
			cls: 'x-btn-icon',
			iconCls: 'x-dialog-newfolder',
			tooltip: '创建文件夹',
			disabled: a.disableCreate,
			handler: function(){
				var name = prompt("请输入文件夹名称：");
				if (!name) return;
				Ext.Ajax.request({
					url: fileStore.url,
					params: {operation: "createFolder", path: fileStore.baseParams.path, name: name },
					callback: function(options, success, response){
						if (success) {
							fileStore.loadData({items:[{cls:'x-dialog-folder', name:name, content:''}]}, true);
						}
						else {
							dlg.fireEvent('exception', response);
						}
					}
				});
			}
		}],
		items: [{
			id: 'filepanel',
			region: 'south',
			xtype: 'form',
			split: false,
			collapsible: false,
			margins: '5 5 0 5',
			autoHeight: true,
			border: false,
			closeAction: 'hide',
			labelWidth: 70,
			defaultType: 'textfield',
			bodyStyle: 'background-color: transparent;',
			items: [{
				fieldLabel: '文件名',
				anchor: '100%',
				name: 'filename',
				readOnly: a.forceSelection
			}]
		},{
			region: 'center',
			border: false,
			layout: 'fit',
			items: dataView
		}]
	}));
	dlg.addEvents('exception');
	
	dlg.ok = dlg.addButton({ text: a.buttonText||"打开", disabled: a.forceSelection}, function(){
		var path = fileStore.baseParams.path;
		var s = path.substr(path.length-1)=='/' ? '' : '/';
		var form = dlg.getComponent("filepanel").getForm();
		var name = form.findField('filename').getValue();
		var records = dataView.getSelectedRecords();
		if (records.length > 0) {
			var rec = records[0];
			if (rec.get('cls') == 'x-dialog-folder') {
				fileStore.baseParams.path += s + rec.get('name');
				fileStore.load();
				return;
			}
			if (!name) name=rec.get('name');
		}
		if (name=='') return;
		if (dlg.callback) {
			if (dlg.callback("ok", fileStore.baseParams.path + s + name, rec)==false) return;
		}
		dlg[dlg.closeAction]();
	});
	dlg.addButton(Ext.Msg.buttonText.cancel, function(){
		if (dlg.callback)	dlg.callback("cancel");
		dlg[dlg.closeAction]();
	});
	dlg.on("beforeshow", function(){
		fileStore.reload();
	});
	return dlg;
},

SaveDialog: function(config){
	var a = config || {};
	return new Dialog.OpenDialog(Ext.apply(a, {
		title:a.title||'保存', buttonText:a.buttonText||"保存", forceSelection:false
	}));
}

}
