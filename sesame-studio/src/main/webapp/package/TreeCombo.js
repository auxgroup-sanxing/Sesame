Ext.TreeCombo = Ext.extend(Ext.form.ComboBox, {
	initList: function() {
		this.list = new Ext.tree.TreePanel({
			root: new Ext.tree.AsyncTreeNode({
				text: 'root',
				children: [{text: 'Child 1', leaf: true}, {text: 'Child 2', leaf: true}]
			}),
			loader: new Ext.tree.TreeLoader(),
			floating: true,
			autoHeight: true,
			listeners: {
				click: this.onNodeClick,
				scope: this
			},
			alignTo: function(el, pos) {
				this.setPagePosition(this.el.getAlignToXY(el, pos));
			}
		});
	},

	expand: function() {
		if (!this.list.rendered) {
			this.list.render(document.body);
			this.list.setWidth(this.el.getWidth());
			this.innerList = this.list.body;
			this.list.hide();
		}
		this.el.focus();
		Ext.TreeCombo.superclass.expand.apply(this, arguments);
	},

	doQuery: function(q, forceAll) {
		this.expand();
	},

    collapseIf : function(e){
        if(!e.within(this.wrap) && !e.within(this.list.el)){
            this.collapse();
        }
    },

	onNodeClick: function(node, e) {
		this.setRawValue(node.attributes.text);
		if (this.hiddenField) {
			this.hiddenField.value = node.id;
		}
		this.collapse();
	}
});
Ext.reg('treecombo', Ext.TreeCombo);
