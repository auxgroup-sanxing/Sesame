document.oncontextmenu = function(e){
    return false;
};
Ext.BLANK_IMAGE_URL = '../images/s.gif';

var buttonOnclick = function(value){
	Ext.getBody().mask("正在解锁，请稍后......","x-mask-loading");
	Ext.Ajax.request({
				
				method: 'POST',                                 
				url: 'locks_ctrl.jsp',                                  
				params: {                                       
					operation: "unLock",                         
					unit: value.path                                    
				},                                              
				callback: function(options, success, response){ 
					if (!success) {                               
						                                            
						alert(response);              
					}else {   
						Ext.getBody().unmask();
						var formPanel = Ext.getCmp("project-form");
						var gridPanel = Ext.getCmp("locks-panel");
						var projectName = formPanel.getComponent('projectName').value;
						var store = gridPanel.store;
						store.baseParams.projectName = projectName;
						store.removeAll();
						store.load();
					}                                             
				}                                               
			});     
};

var Viewer = function(){
    return {
        init: function(){
            var _this = this;
            Ext.Ajax.defaultHeaders = { "Content-Type" : "application/x-www-form-urlencoded; charset=UTF-8" };
            Ext.QuickTips.init();
			
            
            
            
            var projectStore = new Ext.data.Store({
            	url:'locks_ctrl.jsp',
            	baseParams:{
            		operation:'getProject'
            	},
            	reader:new Ext.data.JsonReader({
            		root:'projects',
            		fields:[
	            		{name:'text'},
	            		{name:'value'}
            		]
            	}),
            	listeners:{
					loadexception:function(lockstore,a,response){
						_this.showException(response);
					}
				}
            })
//            projectStore.load();
            
            var lock = Ext.data.Record.create([
//             	{name:'unit',type:'string'},
//             	{name:'operation',type:'string'},
            	{name:'type',type:'string'},
            	{name:'path',type:'string'},
             	{name:'owner',type:'string'},
             	{name:'createDate',type:'date'}
            ]);
            
            var lockstore = new Ext.data.Store({
				url: 'locks_ctrl.jsp',
				baseParams: {
					operation: 'getLocks',
					projectName:'neodemo'
				},
				reader: new Ext.data.JsonReader({
					root: 'locks',
					id: 'name',
					fields:lock
				}),
				listeners:{
					loadexception:function(lockstore,a,response){
						_this.showException(response);
					}
				}
//				sortInfo: {field: 'name', direction: 'ASC'}
			});
			
//			var cm = new Ext.grid.Column([
//				{header:'服务单元名',dataIndex:'unit'},
//				{header:'操作名',dataIndex:'operation'},
//				{header:'锁拥有者',dataIndex:'owner'},
//				{header:'创建时间',dataIndex:'createDate',type:'date'},
//				{header:'强制解锁',renderer:renderLock}
//			]);
			function renderLock(value,cellmeta,record){
				var str = "<input id='lock' type='button' value='解锁' onclick='buttonOnclick("+Ext.encode(record.data)+")'>";
				return str;
			}
			
			
			
			var project_form = new Ext.FormPanel({
					region:'north',
					id:'project-form',
                    autoScroll: true,
                    buttonAlign: 'right',
                    stateful: false,
                    height:80,
					bodyStyle: {
						'border': 'none',
						'padding':'8 5 0 10'
					},
                    defaults: {
                        anchor: '-18',
                        stateful: false
                    },
					items:[
            			new Ext.form.ComboBox({
            				store:projectStore,
            				id:'projectName',
//            				forceSelection: 'true',
            				fieldLabel:'项目名',
            				mode:'remote',
            				emptyText:'请选择项目',
            				triggerAction:'all',
            				valueField:'value',
            				displayField:'text',
            				ref:'../combo',
            				listeners:{
            					select: function(combo,record,index) {
            						lockstore.baseParams.projectName = record.data.value;
            						lockstore.removeAll();
            						lockstore.load();
								}
            				}
            			})
            		]
			});
			
            var viewport = new Ext.Viewport({
            	layout:'border',
            	items:[project_form,{
            		region:'center',
            		itemId:'locks-panel',
            		id:'locks-panel',
            		xtype:'grid',
            		border:false,
            		store:lockstore,
            		columns:[
//						{header:'服务单元名',dataIndex:'unit',sortable:true},
//						{header:'操作名',dataIndex:'operation'},
            			{header:'资源类型',dataIndex:'type',sortable:true,align:'center'},
            			{header:'路径',dataIndex:'path',width:250,align:'center'},
						{header:'锁拥有者',dataIndex:'owner',align:'center'},
						{header:'创建时间',dataIndex:'createDate',format:'m/d/Y',xtype:'datecolumn',align:'center'},
						{header:'强制解锁',dataIndex:'',renderer:renderLock,align:'center'}
            		],
            		viewConfig:{
            			forceFit:true
            		}
            	}]
            });
			
			  
            
        },
     
        
        
        //带参数的方法调用
        
        showException: function(response){
            var statusText = response.responseText;
            if (!statusText) {
                switch (response.status) {
                    case -1:
                        statusText = '通讯超时，事务终止';
                        break;
                    case 0:
                        statusText = '通讯错误，连接服务器失败';
                        break;
                    default:
                        alert(response.statusText + '(' + response.status + ')');
                        return;                
				}
                statusText = '<table style="width:100%;">' +
                '<tr><td style="background: url(\'../images/top_bg.gif\') no-repeat; vertical-align:middle; font: bold 24px \'Times New Roman\', Verdana, Arial; height:54px; color:white;">Application Management Center</td></tr>' +
                '<tr><td><table align="center" border="0" cellpadding="0" cellspacing="8" width="100%"><tbody><tr>' +
                '<td style="text-align: center; width:50px;"><img src="../images/notice.png"/></td>' +
                '<td style="white-space: normal; overflow: hidden; font-size:12px;">' +
                statusText +
                '</td>' +
                '</tr></tbody></table></td></tr></table>';
            }
            
            Ext.MessageBox.show({
                title: '错误提示',
                msg: '<div style="width:100%; height:auto; background-color:white; overflow:auto;">' + statusText + '</div>',
                buttons: Ext.MessageBox.OK,
                width: 492
            });
        }
        };
}();

Ext.onReady(Viewer.init, Viewer, true);
