/**
* @class Ext.form.RestrictEdit
* @extends Ext.form.TriggerField
* @constructor
* Create a new RestrictEdit
* @param {Object} config
 */

Ext.form.RestrictEdit = Ext.extend(Ext.form.TriggerField,  {
	
	format: '{name:"value"}',
    /**
     * @cfg {String} invalidText
     * The error text to display when the date in the field is invalid (defaults to
     * '{value} is not a valid date - it must be in the format {format}').
     */
    invalidText : "{0} 是非法的约束写法 - 正确的格式为 {1}",
    /**
     * @cfg {String} triggerClass
     * An additional CSS class used to style the trigger button.  The trigger will always get the
     * class 'x-form-trigger' and triggerClass will be <b>appended</b> if specified (defaults to 'x-form-date-trigger'
     * which displays a calendar icon).
     */
    //triggerClass : 'x-form-date-trigger',

	editable: true,
	
    // private
    defaultAutoCreate : {tag: "input", type: "text", size: "10", autocomplete: "off"},

    initComponent: function(){
		Ext.form.RestrictEdit.superclass.initComponent.call(this);

		this.addEvents(
            'select'
        );
    },
    
    // private
    validateValue : function(value){
        value = this.formatValue(value);
        if(!Ext.form.RestrictEdit.superclass.validateValue.call(this, value)){
            return false;
        }
        if(value.length < 1){
             return true;
        }
        var svalue = value;
        value = this.parseValue(value);
        if(!value){
            this.markInvalid(String.format(this.invalidText, svalue, this.format));
            return false;
        }
        return true;
    },

    // private
    // Provides logic to override the default TriggerField.validateBlur which just returns true
    validateBlur : function(){
        return !this.menu || !this.menu.isVisible();
    },

    /**
     * Returns the current date value of the date field.
     * @return {Date} The date value
     */
    getValue : function(){
        return Ext.form.RestrictEdit.superclass.getValue.call(this) || "";
    },

    /**
     * Sets the value of the field.  You can pass a date object or any string that can be parsed into a valid object
     * <br />Usage:
     * @param {String/Date} date The date or valid date string
     */
    setValue : function(value){
        Ext.form.RestrictEdit.superclass.setValue.call(this, this.formatValue(value));
    },

    // private
    parseValue : function(value){
        if(!value || value instanceof Object){
            return value;
        }
        try
		{
	        return Ext.decode(value);
		}
		catch(e)
		{
			return null;
		}
    },
	
    // private
    onDestroy : function(){
        Ext.destroy(this.menu, this.wrap);
        Ext.form.RestrictEdit.superclass.onDestroy.call(this);
    },

    // private
    formatValue : function(value){
        return (!value || !(value instanceof Object)) ?
               value : Ext.encode(value);
    },

    // private
    // Implements the default empty TriggerField.onTriggerClick function
    onTriggerClick : function(){
        if(this.disabled){
            return;
        }
		if(this.isExpanded())
		{
            //this.collapse();
            this.el.focus();
			return;
		}
        if(this.menu == null){
            this.menu = new Ext.menu.RestrictMenu({
            	hideOnClick: false
            });
        }
        this.onFocus();

        Ext.apply(this.menu.picker,  {
            format : this.format,
            invalidText : this.invalidText
        });
        this.menu.picker.setValue(this.parseValue(this.getValue()) || new Object());
        this.menu.show(this.el, "tl-bl?");
        this.menuEvents('on');
    },

    //private
    menuEvents: function(method){
        this.menu[method]('select', this.onSelect, this);
        this.menu[method]('hide', this.onMenuHide, this);
        this.menu[method]('show', this.onFocus, this);
    },

    onSelect: function(m, d){
        this.setValue(d);
        this.fireEvent('select', this, d);
        this.menu.hide();
    },
    
    onMenuHide: function(){
        this.focus(false, 60);
        this.menuEvents('un');
    },

//    setEditable : function(value){
//        if(value == this.editable){
//            return;
//        }
//        this.editable = value;
//        if(!value){
//            this.el.dom.setAttribute('readOnly', true);
//            this.el.on('mousedown', this.onTriggerClick,  this);
//            this.el.addClass('x-combo-noedit');
//        }else{
//            this.el.dom.setAttribute('readOnly', false);
//            this.el.un('mousedown', this.onTriggerClick,  this);
//            this.el.removeClass('x-combo-noedit');
//        }
//    },
//
//    onRender : function(ct, position){
//        Ext.form.RestrictEdit.superclass.onRender.call(this, ct, position);
//        if(!this.editable){
//            this.editable = true;
//            this.setEditable(false);
//        }
//	},

	// private
	initEvents : function(){
	    Ext.form.RestrictEdit.superclass.initEvents.call(this);
	
	    this.keyNav = new Ext.KeyNav(this.el, {
	        "down" : function(e){
	            if(!this.isExpanded()){
	                this.onTriggerClick();
	            }
	        },
	        "esc" : function(e){
	            this.collapse();
	        },
	        scope : this,
	        doRelay : function(foo, bar, hname){
	            if(hname == 'down' || this.scope.isExpanded()){
	               return Ext.KeyNav.prototype.doRelay.apply(this, arguments);
	            }
	            return true;
	        },
	        forceKeyDown: true
	    });
	},

    isExpanded : function(){
        return (this.menu && this.menu.isVisible());
    },
	
    beforeBlur : function(){
        var v = this.parseValue(this.getRawValue());
        if(v){
            this.setValue(v);
        }
    }
});

Ext.menu.RestrictMenu = Ext.extend(Ext.menu.Menu, {
	enableScrolling: false,

    cls: 'x-date-menu',
    
    initComponent: function(){
        this.on('beforeshow', this.onBeforeShow, this);
        if(this.strict = (Ext.isIE7 && Ext.isStrict)){
            this.on('show', this.onShow, this, {single: true, delay: 20});
        }
        Ext.apply(this, {
            plain: true,
            showSeparator: false,
            items: this.picker = new Ext.RestrictPicker(Ext.apply({
                internalRender: this.strict || !Ext.isIE,
                ctCls: 'x-menu-date-item'
            }, this.initialConfig))
        });
        this.picker.purgeListeners();
        Ext.menu.RestrictMenu.superclass.initComponent.call(this);
        this.relayEvents(this.picker, ["select"]);
    },

	onClick: function() {
        if(this.hideOnClick){
            this.hide(true);
        }
    },

    onBeforeShow: function(){
        if (this.picker){
//            this.picker.hideMonthPicker(true);
        }
    },

    onShow: function(){
        var el = this.picker.getEl();
        el.setWidth(el.getWidth()); //nasty hack for IE7 strict mode
    }
});

Ext.menu.RestrictItem = Ext.extend(Ext.menu.BaseItem, {
	constructor : function(config){
	    Ext.menu.RestrictItem.superclass.constructor.call(this, new Ext.RestrictPicker(config), config);
	    this.picker = this.component;
	    this.addEvents({select: true});
	    
	    this.picker.on("render", function(picker){
	        picker.getEl().swallowEvent("click");
	        picker.container.addClass("x-menu-date-item");
	    });
	    this.picker.on("select", this.onSelect, this);
	},
	// private
    onSelect : function(picker, obj){
        this.fireEvent("select", this, obj, picker);
        Ext.menu.RestrictItem.superclass.handleClick.call(this);
    }
});

Ext.RestrictPicker = Ext.extend(Ext.BoxComponent, {
    /**
     * @cfg {String} okText
     * The text to display on the ok button
     */
    okText : "确定", // &#160; to give the user extra clicking room
    /**
     * @cfg {String} clearText
     * The text to display on the clear button
     */
    clearText : "清除",
    /**
     * @cfg {Boolean} constrainToViewport
     * True to constrain the picker to the viewport (defaults to true)
     */
    //constrainToViewport : true,

    // private
    initComponent : function(){
        Ext.RestrictPicker.superclass.initComponent.call(this);
        this.value = this.value || {};
        this.addEvents(
            'select'
        );
        if(this.handler){
            this.on("select", this.handler,  this.scope || this);
        }
    },

    /**
     * Sets the value of the value field
     * @param {Object} value The value to set
     */
    setValue : function(value){
        var old = this.value;
        this.value = value;
        if(this.el){
            this.update(this.value);
        }
    },

    /**
     * Gets the current selected value of the value field
     * @return {Object} The value
     */
    getValue : function(){
        return this.value;
    },

    // private
    focus : function(){
        if(this.el){
            //this.update(this.value);
        }
    },

    // private
    onRender : function(container, position){
        var el = document.createElement("div");
        el.className = "x-date-picker";
        el.innerHTML = "<div style='width:220px;'></div>";

        container.dom.insertBefore(el, position);

        this.el = Ext.get(el);
        this.eventEl = this.el;//Ext.get(el.firstChild);

	    var Ed = Ext.grid.GridEditor;

		var cm = new Ext.grid.ColumnModel([{
	           header: "值",
	           dataIndex: 'value',
	           width: 60,
	           editor: new Ed(
				   new Ext.form.TextField({ allowBlank: false }),
				   { afterRender: function(){this.el.setStyle('z-index', '15001');} }
			   )
	        },
			{
	           header: "描述",
	           dataIndex: 'text',
	           width: 140,
	           editor: new Ed(
				   new Ext.form.TextField({ allowBlank: false }),
				   { afterRender: function(){this.el.setStyle('z-index', '15001');} }
			   )
	        }
		]);
	    var EnumItem = Ext.data.Record.create([
           {name: 'value', type: 'string'},
           {name: 'text', type: 'string'}
	    ]);
		this.enumStore = new Ext.data.Store({
		    data : { rows:[]},
			reader: new Ext.data.JsonReader({
				root: "rows"
			}, EnumItem)
		});
	    var grid = new Ext.grid.EditorGridPanel({
	    	title: '枚举',
	        ds: this.enumStore,
	        cm: cm,
	        enableHdMenu: false,
			stripeRows: false,
			autoShow: true,
	        listeners: {
	        }
	    });
		this.subrPanel = new Ext.form.FormPanel({
			title: '子界',
			labelWidth: 55, 
			bodyStyle: 'padding:5px;',
			defaults: {anchor: '100%'},
			items:[{
				xtype: 'numberfield',
		        fieldLabel: '最小值',
		        name: 'minValue'
			},{
				xtype: 'numberfield',
		        fieldLabel: '最大值',
		        name: 'maxValue'
			}]
		});
		this.enumGrid = grid;

		this.tabPanel = new Ext.TabPanel({
			tabPosition:'top',
			renderTo: this.el.first(), //this.el.child("div.inner-layout"),
			height: 200,
			//activeTab: 0,
			border: false,
			layoutOnTabChange: true,
			deferredRender: false,
			items: [
				this.subrPanel, grid
			],
			listeners: {
				tabchange: function(tabpanel, tab) {
					var v = (tab==grid);
					tabpanel.buttons[0].setVisible(v);
					tabpanel.buttons[1].setVisible(v);
				}
			},
			buttons: [
				new Ext.Button({
				id: 'add-btn',
	            text: '添加',
	            width: 40,
	            handler: function(){
					var cell = grid.getSelectionModel().getSelectedCell();
					var row = cell ? cell[0]+1 : grid.store.getCount();
		            var rec = new EnumItem({
		                value: '',
		                text: ''
		            });
		            grid.stopEditing();
		            grid.store.insert(row, rec);
		            grid.startEditing(row, 0);
				}
			}),
			new Ext.Button({
				id: 'del-btn',
	            text: '删除',
	            width: 40,
	            handler: function(){
					var cell = grid.getSelectionModel().getSelectedCell();
					if (!cell) return;
		            grid.stopEditing();
					grid.store.remove(grid.store.getAt(cell[0]));
	            }
			}),
			{
	            text: this.clearText,
	            width: 40,
	            handler: this.clearPressed,
	            scope: this
			},{
	            text: this.okText,
	            width: 40,
	            handler: this.okPressed,
	            scope: this
			}]
		});
		this.tabPanel.syncSize();
		this.tabPanel.setActiveTab(0);
        this.el.unselectable();
        
        if(Ext.isIE){
            this.el.repaint();
        }
        this.update(this.value);
    },

    // private
    okPressed : function(){
		var value = null;
		switch(this.tabPanel.getActiveTab())
		{
		case this.enumGrid:
			var records= new Array();
			this.enumStore.each(function(record){
				if (record.get('value')!='' && record.get('text')!='') 
					records.push(record.data); 
			});
			value = records;
			break;
		case this.subrPanel:
			value = this.subrPanel.form.getValues();
			break;
		default:
			return;
		}
        this.setValue(value);
        this.fireEvent("select", this, this.value);
    },

    // private
    clearPressed : function(){
        this.setValue(null);
        this.fireEvent("select", this, this.value);
    },
	
    // private
    update : function(value){
		this.subrPanel.form.reset();
		this.enumStore.removeAll();
		switch (Ext.type(value))
		{
		case "array":
			this.tabPanel.setActiveTab(1);
			this.enumStore.loadData({rows:value});
			break;
		case "object":
			this.subrPanel.form.setValues(value);
			this.tabPanel.setActiveTab(0);
			break;
		default:
			return;
		}

        if(!this.internalRender){
            var main = this.el.dom.firstChild;
            var w = main.offsetWidth;
            this.el.setWidth(w + this.el.getBorderWidth("lr"));
            Ext.fly(main).setWidth(w);
            this.tabPanel.setWidth(w); ///测试
            this.tabPanel.doLayout(true, true);
            this.internalRender = true;
            // opera does not respect the auto grow header center column
            // then, after it gets a width opera refuses to recalculate
            // without a second pass
            if(Ext.isOpera && !this.secondPass){
                main.rows[0].cells[1].style.width = (w - (main.rows[0].cells[0].offsetWidth+main.rows[0].cells[2].offsetWidth)) + "px";
                this.secondPass = true;
                this.update.defer(10, this, [value]);
            }
        }
    },
	
    destroy: function(){
		Ext.destroy(this.tabPanel);
		Ext.RestrictPicker.superclass.destroy.call(this); 
    }
});