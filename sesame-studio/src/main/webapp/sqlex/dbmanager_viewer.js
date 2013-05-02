document.oncontextmenu = function(e) {return false;};
Ext.BLANK_IMAGE_URL='../images/s.gif';

var Viewer = function() {
	var viewport, centerRegion;
	return {
		init : function() {
			Ext.state.Manager.setProvider(new Ext.state.CookieProvider());
			Ext.QuickTips.init();
			
			var height = Ext.getBody().getHeight();
	
			// 数据库操作树
			var treePanel = new Ext.ux.SQLPanel({
				region : 'west',
				autoScroll : true, 
				closable : false,
				width: 240,
				
				collapseMode: 'mini',
				collapsible: true,
				split: true,
				animFloat: false,
				floatable: false,
				
				height: (height == 0)? 422 : height,
				header : false,
				bodyStyle : {
					'padding-top' : '3px',
					'border' : 'none',
					'border-top' : '1px solid #99BBE8',
					'border-right' : '1px solid #99BBE8'
				}
			});
			treePanel.getRootNode().expand();
			
			viewport = new Ext.Viewport({
				layout: 'border',
				items: [treePanel,{
						region: 'center',
						id: 'editor_region',
						border: false,
						layout: 'fit',
						bodyStyle : {'visibility' : 'hidden'},
						items: [{
							xtype: 'tabpanel',
							border: true,
							header : false,
							headerStyle : {
								'border' : 'none',
								'border-left' : '1px solid #99BBE8',
								'padding' : '2px 0px'
							},
							bodyStyle : {
								'padding-top' : '0px',
								'border' : 'none',
								'border-left' : '1px solid #99BBE8'
							},
							activeTab: 0,
							listeners: {
								add: function(container, component, index){
									if (container==this && this.items.getCount()==1) {
										this.show();
										viewport.doLayout(false, true);
									}
								},
								remove: function(container, component){
									if (container==this && this.items.getCount()==0) {
										this.hide();
									}
								}
							}
						}]
					}]
			});
			viewport.doLayout(true);
		},
		
		getCenterRegion : function() {
			centerRegion = viewport.layout.center.panel.getComponent(0);
			return centerRegion;
		},
		
		getCenterTabPanel : function() {
			return viewport.layout.center.panel;
		},
		
		showException : function(response){
			var statusText = response.responseText;
			if (!statusText)
			{
				switch (response.status) 
				{
					case -1:
						statusText = '通讯超时，事务终止';  break;
					case 0:
						statusText = '通讯错误，连接服务器失败';   break;
					default:
						alert (response.statusText+'('+response.status+')');  return;
				}
				statusText = '<table style="width:100%;">'+
					'<tr><td style="background: url(\'../images/top_bg.gif\') no-repeat; vertical-align:middle; font: bold 24px \'Times New Roman\', Verdana, Arial; height:54px; color:white;">Application Management Center</td></tr>'+
					'<tr><td><table align="center" border="0" cellpadding="0" cellspacing="8" width="100%"><tbody><tr>'+
			        '<td style="text-align: center; width:50px;"><img src="../images/notice.png"/></td>'+
			        '<td style="white-space: normal; overflow: hidden; font-size:12px;">'+statusText+'</td>'+
				    '</tr></tbody></table></td></tr></table>';
			}
	
			Ext.MessageBox.show({
				title: '错误提示',
				msg: '<div style="width:100%; height:auto; background-color:white; overflow:auto;">'+statusText+'</div>',
				buttons: Ext.MessageBox.OK,
				width: 492
			});
		}
	}
}();

Ext.onReady(Viewer.init, Viewer, true);
