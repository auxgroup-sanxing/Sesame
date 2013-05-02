GraphEditor = {};

function main()
{
    Ext.QuickTips.init();

	// Disables browser context menu
	mxEvent.disableContextMenu(document.body);		
    
	// Creates the graph and loads the default stylesheet
    var graph = new mxGraph();
    
    // Creates the command history (undo/redo)
    var history = new mxUndoManager();

    // Loads the default stylesheet into the graph
    var node = mxUtils.load('resources/default-style.xml').getDocumentElement();
		var dec = new mxCodec(node.ownerDocument);
		dec.decode(node, graph.getStylesheet());
	
	// Sets the style to be used when an elbow edge is double clicked
	graph.alternateEdgeStyle = 'vertical';
	
	// Creates the main containers
	var mainPanel = new MainPanel(graph, history);
	var library = new LibraryPanel();

    // Creates the container for the outline
	var outlinePanel = new Ext.Panel({
		id:'outlinePanel',
		layout: 'fit',
		split: true,
		height: 200,
        region:'south'
    });

	var sourcePanel = new Ext.tree.TreePanel({
		region: 'center',
		split: true,
		//width: 200,
		minSize: 175,	
		collapsible: false,
		margins: '0 0 0 0',
		//iconCls: 'xsd-palette-category',
		title: '源文档',
		animate: false,
		border: true,
		loader: new Ext.tree.TreeLoader(),
		id: 'source-tree',
		rootVisible: true,
		lines: false,
		autoScroll: true,
		enableDrag: true,
		trackMouseOver: false,
		root: new Ext.tree.AsyncTreeNode({
		    text: 'Context',
			iconCls: 'xsd-icon-element',
			uiProvider: Ext.ux.ContentUI,
		    children: getContext()
		})
	});
	var targetPanel = new Ext.tree.TreePanel({
		region: 'east',
		split: true,
		width: 200,
		minSize: 175,	
		collapsible: false,
		margins: '0 0 0 0',
		//iconCls: 'xsd-palette-category',
		title: '目标文档',
		cls: 'rtl-tree',
		animate: false,
		border: true,
		loader: new Ext.tree.TreeLoader(),
		id: 'target-tree',
		rootVisible: true,
		lines: false,
		autoScroll: true,
		enableDrop: true,
		trackMouseOver: false,
		root: new Ext.tree.AsyncTreeNode({
		    text: 'Context',
			iconCls: 'xsd-icon-element',
			uiProvider: Ext.ux.ContentUI,
		    children: []//this.getStandardActions()
		}),
		listeners: {
			beforenodedrop : function(e){
				if (e.dropNode.getOwnerTree() == e.tree) {
					return true;
				}
				else if (e.dropNode.getOwnerTree() == sourcePanel) {
					e.cancel = true;
					e.dropStatus = true;
	
					var a = e.dropNode.attributes;
					var dropEl = Ext.fly(e.dropNode.ui.elNode);
					dropEl.addClass('line-start');
					var y1 = dropEl.getTop(true);
					var targetEl = Ext.fly(e.target.ui.elNode);
					targetEl.addClass('line-end');
					var y2 = targetEl.getTop(true);
					
					e.target.attributes.value = a.text;
					var w = mainPanel.getInnerWidth();
					//alert(w);
					
					var parent = graph.getDefaultParent();
					var v1 = graph.insertVertex(parent, null, '', 0, y1, 5, 18);
					var v2 = graph.insertVertex(parent, null, '', w-25, y2, 5, 18);
					var e1 = graph.insertEdge(parent, null, '-->', v1, v2);
				}
			}
		}
	});
	// Creates the enclosing viewport
    var viewport = new Ext.Viewport(
    {
    	layout:'border',
    	items: [
            new Ext.Panel({
		        region:'west',
		        layout:'border',
		        split:true,
		        width: 180,
		        collapsible: true,
		        border: false,
		        items:
		        [
		         	library,
		        	sourcePanel
				]
	    	}),
			targetPanel,
        	mainPanel
       	] // end viewport items
    }); // end of new Viewport

    // Enables scrollbars for the graph container to make it more
    // native looking, this will affect the panning to use the
    // scrollbars rather than moving the container contents inline
   	mainPanel.graphPanel.body.dom.style.overflow = 'auto';
   	
    // FIXME: For some reason the auto value is reset to hidden in
    // Safari on the Mac, this is probably caused by ExtJs
   	if (mxClient.IS_MAC && mxClient.IS_SF)
	{
   		graph.addListener('size', function(graph)
   		{
   			graph.container.style.overflow = 'auto';
   		});
	}

	// Initializes the graph as the DOM for the panel has now been created	
    graph.init(mainPanel.graphPanel.body.dom);
    graph.setConnectable(true);
	graph.setDropEnabled(true);
    graph.setPanning(true);
    graph.setTooltips(true);
    //graph.connectionHandler.createTarget = true;

	// Creates rubberband selection
    var rubberband = new mxRubberband(graph);

	// Adds some example cells into the graph
    var parent = graph.getDefaultParent();
	graph.getModel().beginUpdate();
	try
	{
		//var v1 = graph.insertVertex(parent, null, '哈哈,', 20, 20, 80, 40);
		//var v2 = graph.insertVertex(parent, null, 'World!', 200, 100, 80, 40);
		//var e1 = graph.insertEdge(parent, null, 'Hello, World!', v1, v2);
	}
	finally
	{
		// Updates the display
		graph.getModel().endUpdate();
	}
		    
    // Installs the command history after the initial graph
    // has been created
	var listener = function(sender, edit)
	{
		history.undoableEditHappened(edit);
	};
	
	graph.getModel().addListener('undo', listener);
	graph.getView().addListener('undo', listener);

	// Toolbar object for updating buttons in listeners
	var toolbarItems = mainPanel.graphPanel.getTopToolbar().items;
		    
    // Updates the states of all buttons that require a selection
    var selectionListener = function()
    {
    	var selected = !graph.isSelectionEmpty();
    	
    	toolbarItems.get('cut').setDisabled(!selected);
    	toolbarItems.get('copy').setDisabled(!selected);
    	toolbarItems.get('delete').setDisabled(!selected);
    	toolbarItems.get('fontcolor').setDisabled(!selected);
    	toolbarItems.get('linecolor').setDisabled(!selected);
    	toolbarItems.get('align').setDisabled(!selected);
    };
    
    graph.selection.addListener('change', selectionListener);

    // Updates the states of the undo/redo buttons in the toolbar
    var historyListener = function() {
    	toolbarItems.get('undo').setDisabled(!history.canUndo());
    	toolbarItems.get('redo').setDisabled(!history.canRedo());
    };

	history.addListener('add', historyListener);
	history.addListener('undo', historyListener);
	history.addListener('redo', historyListener);
	
	// Updates the button states once
	selectionListener();
	historyListener();
	
    // Adds the entries into the library
    insertVertexTemplate(library, graph, 'trim', 'images/swimlane.gif', 'swimlane', 120, 120, 'Container');
    insertVertexTemplate(library, graph, 'sub-string', 'images/rectangle.gif', null, 100, 40);
    insertVertexTemplate(library, graph, 'round', 'images/rounded.gif', 'rounded=1', 100, 40);
    insertVertexTemplate(library, graph, 'upper-case', 'images/ellipse.gif', 'ellipse', 60, 60);

    insertImageTemplate(library, graph, 'Bell', 'images/bell.png', false);
    insertImageTemplate(library, graph, 'Box', 'images/box.png', false);
    insertImageTemplate(library, graph, 'Cube', 'images/cube_green.png', false);
    insertImageTemplate(library, graph, 'User', 'images/dude3.png', true);

    insertSymbolTemplate(library, graph, 'Cancel', 'images/symbols/cancel_end.png', false);
    insertSymbolTemplate(library, graph, 'Error', 'images/symbols/error.png', false);

	insertEdgeTemplate(library, graph, '直线', 'images/straight.gif', 'straight', 100, 100);
	insertEdgeTemplate(library, graph, '水平连线', 'images/connect.gif', null, 100, 100);
    insertEdgeTemplate(library, graph, 'Vertical Connector', 'images/vertical.gif', 'vertical', 100, 100);
    insertEdgeTemplate(library, graph, 'Entity Relation', 'images/entity.gif', 'entity', 100, 100);
	insertEdgeTemplate(library, graph, 'Arrow', 'images/arrow.gif', 'arrow', 100, 100);
    
    // Overrides createGroupCell to set the group style for new groups to 'group'
    var previousCreateGroupCell = graph.createGroupCell;
    
    graph.createGroupCell = function()
    {
    	var group = previousCreateGroupCell.apply(this, arguments);
    	group.setStyle('group');
    	
    	return group;
    };
    
    // Uses the last inserted edge as a template for new edges
    var previousCreateEdge = graph.connectionHandler.createEdge;
    
    graph.connectionHandler.createEdge = function()
    {
		if (GraphEditor.edgeTemplate != null)
		{
    		var clone = graph.cloneCells([GraphEditor.edgeTemplate])[0];
    		
    		return clone;
    	}
    	else
    	{
    		return previousCreateEdge.apply(this, arguments);
    	}
    };
    
    // Uses the selected edge in the library as a template for new edges
    library.getSelectionModel().on('selectionchange', function(sm, node)
    {
    	if (node != null &&
    			node.attributes.cells != null)
    	{
    		var cell = node.attributes.cells[0];
    		
    		if (cell != null &&
    				graph.getModel().isEdge(cell))
    		{
    			GraphEditor.edgeTemplate = cell;
    		}
    	}
    });
    
    // Redirects tooltips to ExtJs tooltips. First a tooltip object
    // is created that will act as the tooltip for all cells.
  	var tooltip = new Ext.ToolTip(
		{
        target: graph.container,
        html: ''
    });
    
    // Disables the built-in event handling
    tooltip.disabled = true;
    
    // Installs the tooltip by overriding the hooks in mxGraph to
    // show and hide the tooltip.
    graph.tooltipHandler.show = function(tip, x, y)
    {
    	if (tip != null &&
    		tip.length > 0)
    	{
    		// Changes the DOM of the tooltip in-place if
    		// it has already been rendered
	    	if (tooltip.body != null)
	    	{
	    		// TODO: Use mxUtils.isNode(tip) and handle as markup,
	    		// problem is dom contains some other markup so the
	    		// innerHTML is not a good place to put the markup
	    		// and this method can also not be applied in
	    		// pre-rendered state (see below)
	    		//tooltip.body.dom.innerHTML = tip.replace(/\n/g, '<br>');
					tooltip.body.dom.firstChild.nodeValue = tip;
	    	}
	    	
	    	// Changes the html config value if the tooltip
	    	// has not yet been rendered, in which case it
	    	// has no DOM nodes associated
	    	else
	    	{
	    		tooltip.html = tip;
	    	}
	    	
	    	tooltip.showAt([x, y + mxConstants.TOOLTIP_VERTICAL_OFFSET]);
	    }
    };
    
    graph.tooltipHandler.hide = function()
    {
    	tooltip.hide();
    };

    // Updates the document title if the current root changes (drilling)
	var drillHandler = function(sender)
	{
		var model = graph.getModel();
		var cell = graph.getCurrentRoot();
		var title = '';
		
		while (cell != null &&  model.getParent(model.getParent(cell)) != null) {
			// Append each label of a valid root
			if (graph.isValidRoot(cell))
			{
				title = ' > ' +
				graph.convertValueToString(cell) + title;
			}
			
			cell = graph.getModel().getParent(cell);
		}
		
		document.title = 'Graph Editor' + title;
	};
		
	graph.getView().addListener('down', drillHandler);
	graph.getView().addListener('up', drillHandler);

	// Keeps the selection in sync with the history
	var undoHandler = function(sender, edit)
	{
		graph.selectCellsForEdit(edit);
	};
	
	history.addListener('undo', undoHandler);
	history.addListener('redo', undoHandler);

	// Transfer initial focus to graph container for keystroke handling
	graph.container.focus();
	    
    // Handles keystroke events
    var keyHandler = new mxKeyHandler(graph);
    
    // Ignores enter keystroke. Remove this line if you want the
    // enter keystroke to stop editing
    keyHandler.enter = function() {};
    
    keyHandler.bindKey(8, function()
    {
    	graph.collapse();
    });
    
    keyHandler.bindKey(13, function()
    {
    	graph.expand();
    });
    
    keyHandler.bindKey(33, function()
    {
    	graph.exitGroup();
    });
    
    keyHandler.bindKey(34, function()
    {
    	graph.enterGroup();
    });
    
    keyHandler.bindKey(36, function()
    {
    	graph.home();
    });

    keyHandler.bindKey(35, function()
    {
    	graph.refresh();
    });
    
    keyHandler.bindKey(37, function()
    {
    	graph.selectPrevious();
    });
        
    keyHandler.bindKey(38, function()
    {
    	graph.selectParent();
    });

    keyHandler.bindKey(39, function()
    {
    	graph.selectNext();
    });
    
    keyHandler.bindKey(40, function()
    {
    	graph.selectChild();
    });
    
    keyHandler.bindKey(46, function()
    {
    	graph.remove();
    });
    
    keyHandler.bindKey(113, function()
    {
    	graph.edit();
    });
  
    keyHandler.bindControlKey(65, function()
    {
    	graph.selectAll();
    });

    keyHandler.bindControlKey(89, function()
    {
    	history.redo();
    });
    
    keyHandler.bindControlKey(90, function()
    {
    	history.undo();
    });
    
    keyHandler.bindControlKey(88, function()
    {
    	mxClipboard.cut(graph);
    });
    
    keyHandler.bindControlKey(67, function()
    {
    	mxClipboard.copy(graph);
    });
    
    keyHandler.bindControlKey(86, function()
    {
    	mxClipboard.paste(graph);
    });
    
    keyHandler.bindControlKey(71, function()
    {
    	graph.group(null, 20);
    });
    
    keyHandler.bindControlKey(85, function()
    {
    	graph.ungroup();
    });
}; // end of main

function insertSymbolTemplate(panel, graph, name, icon, rhombus)
{
    var imagesNode = panel.symbols;
    var style = (rhombus) ? 'rhombusImage' : 'roundImage';
    return insertVertexTemplate(panel, graph, name, icon, style+';image='+icon, 50, 50, '', imagesNode);
};

function insertImageTemplate(panel, graph, name, icon, round)
{
    var imagesNode = panel.images;
    var style = (round) ? 'roundImage' : 'image';
    return insertVertexTemplate(panel, graph, name, icon, style+';image='+icon, 50, 50, name, imagesNode);
};

function insertVertexTemplate(panel, graph, name, icon, style, width, height, value, parentNode)
{
		var cells = [new mxCell((value != null) ? value : '', new mxGeometry(0, 0, width, height), style)];
		cells[0].vertex = true;
		
		var funct = function(graph, evt, target)
		{
			var validDropTarget = (target != null) ?
				graph.isValidDropTarget(target, cells, evt) : false;
			var select = null;
			
			if (target != null &&
				!validDropTarget &&
				graph.getModel().getChildCount(target) == 0 &&
				graph.getModel().isVertex(target) == cells[0].vertex)
			{
				graph.getModel().setStyle(target, style);
				select = [target];
			}
			else
			{
				if (target != null &&
					!validDropTarget)
				{
					target = null;
				}
				
				var pt = graph.getPointForEvent(evt);
				select = graph.move(cells, pt.x, pt.y, true, target);
			}
			
			graph.setSelectionCells(select);
		};
		
		// Small hack to install the drag listener on the node's DOM element
		// after it has been created. The DOM node does not exist if the parent
		// is not expanded.
		var node = panel.addTemplate(name, icon, parentNode, cells);
		var installDrag = function(expandedNode)
		{
			if (node.ui.elNode != null)
			{
				// Creates the element that is being shown while the drag is in progress
				var dragPreview = document.createElement('div');
				dragPreview.style.border = 'dashed black 1px';
				dragPreview.style.width = width+'px';
				dragPreview.style.height = height+'px';
				
				mxUtils.makeDraggable(node.ui.elNode, graph, funct, dragPreview, 0, 0);
			}
		};
		
		if (!node.parentNode.isExpanded())
		{
			panel.on('expandnode', installDrag);
		}
		else
		{
			installDrag(node.parentNode);
		}
		
		return node;
};

function insertEdgeTemplate(panel, graph, name, icon, style, width, height, value, parentNode)
{
		var cells = [new mxCell((value != null) ? value : '', new mxGeometry(0, 0, width, height), style)];
		cells[0].geometry.setTerminalPoint(new mxPoint(0, height), true);
		cells[0].geometry.setTerminalPoint(new mxPoint(width, 0), false);
		cells[0].edge = true;
		
		var funct = function(graph, evt, target)
		{
			var validDropTarget = (target != null) ? graph.isValidDropTarget(target, cells, evt) : false;
			var select = null;
			
			if (target != null && !validDropTarget)
			{
				target = null;
			}
			
			var pt = graph.getPointForEvent(evt);
			select = graph.move(cells, pt.x, pt.y, true, target);
			
			// Uses this new cell as a template for all new edges
			GraphEditor.edgeTemplate = select[0];
			
			graph.setSelectionCells(select);
		};
		
		// Small hack to install the drag listener on the node's DOM element
		// after it has been created. The DOM node does not exist if the parent
		// is not expanded.
		var node = panel.addTemplate(name, icon, parentNode, cells);
		var installDrag = function(expandedNode)
		{
			if (node.ui.elNode != null)
			{
				// Creates the element that is being shown while the drag is in progress
				var dragPreview = document.createElement('div');
				dragPreview.style.border = 'dashed black 1px';
				dragPreview.style.width = width+'px';
				dragPreview.style.height = height+'px';
				
				mxUtils.makeDraggable(node.ui.elNode, graph, funct, dragPreview, 0, -5);
			}
		};
		
		if (!node.parentNode.isExpanded())
		{
			panel.on('expandnode', installDrag);
		}
		else
		{
			installDrag(node.parentNode);
		}
		
		return node;
};


function getContext() {
	var nodes = [
		{
			text: 'helooo'
		},{
			text: 'prop'
		}
	];
	return nodes;
}

// Defines a global functionality for displaying short information messages
Ext.example = function(){
    var msgCt;

    function createBox(t, s){
        return ['<div class="msg">',
                '<div class="x-box-tl"><div class="x-box-tr"><div class="x-box-tc"></div></div></div>',
                '<div class="x-box-ml"><div class="x-box-mr"><div class="x-box-mc"><h3>', t, '</h3>', s, '</div></div></div>',
                '<div class="x-box-bl"><div class="x-box-br"><div class="x-box-bc"></div></div></div>',
                '</div>'].join('');
    }
    return {
        msg : function(title, format){
            if(!msgCt){
                msgCt = Ext.DomHelper.append(document.body, {id:'msg-div'}, true);
            }
            msgCt.alignTo(document, 't-t');
            var s = String.format.apply(String, Array.prototype.slice.call(arguments, 1));
            var m = Ext.DomHelper.append(msgCt, {html:createBox(title, s)}, true);
            m.slideIn('t').pause(1).ghost("t", {remove:true});
        }
    };
}();

//
Ext.ux.ContentUI = Ext.extend(Ext.tree.TreeNodeUI, {
    // private
    renderElements : function(n, a, targetNode, bulkRender) {
		Ext.ux.ContentUI.superclass.renderElements.apply(this, arguments);
		this.blankNode = Ext.DomHelper.insertHtml("beforeEnd", this.elNode, "<span class='endpoint'>&nbsp;&nbsp;&nbsp;&nbsp;</span>");
	}
});