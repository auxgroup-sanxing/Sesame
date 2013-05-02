Ext.ux.Canvas = Ext.extend(Ext.Component, {
    setSize: function(size){
        this.canvas.height = size.height;
        this.canvas.width = size.width;
        this.draw();
    },
    
    autoEl: new Ext.Template('<canvas></canvas>'),
    
    onRender: function(ct, position){
        Ext.ux.Canvas.superclass.onRender.call(this, ct, position);
        this.canvas = this.el.child("canvas").dom;
        this.ctx = this.canvas.getContext("2d");
        this.draw();
    },
    
    draw: function(){
        // Nothing yet
    }
    
});

Ext.namespace("Tramada");
Tramada.ExampleCanvas = Ext.extend(Ext.ux.Canvas, {

    draw: function(){
        var c = this.ctx;
        {
            var grad = c.createLinearGradient(0, 0, 0, this.canvas.height);
            grad.addColorStop(0, 'rgba(250,250,250,100)');
            grad.addColorStop(1, 'rgba(200,0,200,100)');
            c.fillStyle = grad;
            c.fillRect(0, 0, this.canvas.width, this.canvas.height);
        }
        {
        
            var grad = c.createLinearGradient(0, 0, 0, this.canvas.height);
            grad.addColorStop(0, 'rgba(200,200,200,100)');
            grad.addColorStop(1, 'rgba(250,250,250,100)');
            c.fillStyle = grad;
            var boxSize = (this.canvas.width > this.canvas.height ? this.canvas.height : this.canvas.width) / 2 | 0;
            c.fillRect((this.canvas.width / 2) - (boxSize / 2) | 0, (this.canvas.height / 2) - (boxSize / 2) | 0, boxSize, boxSize);
        }
    }
    
});


Tramada.MochaWindow = Ext.extend(Ext.ux.Canvas, {
    options : {
        cornerRadius : 5,
        headerHeight : 20
    },

    draw : function() {
        var shadows = true;
        var mochaWidth = this.canvas.width;
        var mochaHeight = this.canvas.height;
        var ctx = this.ctx;

        // Draw shapes
        ctx.clearRect(0,0,mochaWidth,mochaHeight);
        if (shadows == null || shadows == false && window.ie != true){
            this.roundedRect(ctx,0,0,mochaWidth,mochaHeight,this.options.cornerRadius,0,0,0,0.06); //shadow
            this.roundedRect(ctx,1,1,mochaWidth-2,mochaHeight-2,this.options.cornerRadius,0,0,0,0.08); //shadow
            this.roundedRect(ctx,2,2,mochaWidth-4,mochaHeight-4,this.options.cornerRadius,0,0,0,0.3); //shadow
        }        
        this.roundedRect(ctx,3,2,mochaWidth-6,mochaHeight-6,this.options.cornerRadius,246,246,246,1.0);    //mocha body
        this.topRoundedRect(ctx,3,2,mochaWidth-6,this.options.headerHeight,this.options.cornerRadius); //mocha header
        this.triangle(ctx,mochaWidth-20,mochaHeight-20,12,12,209,209,209,1.0); //resize handle
        this.restorebutton(ctx,mochaWidth-34,15,217,228,217,1.0);    
        this.closebutton(ctx,mochaWidth-15,15,228,217,217,1.0);
        this.triangle(ctx,mochaWidth-20,mochaHeight-20,10,10,0,0,0,0); //invisible dummocha. The last element drawn is not rendered consistently while resizing in IE6 and IE7.
    },

    //mocha body
    roundedRect: function(ctx,x,y,width,height,radius,r,g,b,a){
        ctx.fillStyle = 'rgba(' + r +',' + g + ',' + b + ',' + a + ')';
        ctx.beginPath();
        ctx.moveTo(x,y+radius);
        ctx.lineTo(x,y+height-radius);
        ctx.quadraticCurveTo(x,y+height,x+radius,y+height);
        ctx.lineTo(x+width-radius,y+height);
        ctx.quadraticCurveTo(x+width,y+height,x+width,y+height-radius);
        ctx.lineTo(x+width,y+radius);
        ctx.quadraticCurveTo(x+width,y,x+width-radius,y);
        ctx.lineTo(x+radius,y);
        ctx.quadraticCurveTo(x,y,x,y+radius);
        ctx.fill(); 
    },

    //mocha header with gradient background
    topRoundedRect: function(ctx,x,y,width,height,radius){
        // Create gradient        
        if (window.opera != null ){
            var lingrad = ctx.createLinearGradient(0,0,0,this.options.headerHeight+2);            
        }
        else {
            var lingrad = ctx.createLinearGradient(0,0,0,this.options.headerHeight);        
        }

        lingrad.addColorStop(0, 'rgba(250,250,250,100)');
        lingrad.addColorStop(1, 'rgba(228,228,228,100)');
        ctx.fillStyle = lingrad;
        // draw header
        ctx.beginPath();
        ctx.moveTo(x,y);
        ctx.lineTo(x,y+height);
        ctx.lineTo(x+width,y+height);
        ctx.lineTo(x+width,y+radius);
        ctx.quadraticCurveTo(x+width,y,x+width-radius,y);
        ctx.lineTo(x+radius,y);
        ctx.quadraticCurveTo(x,y,x,y+radius);    
        ctx.fill(); 
    },

    // resize handle
    triangle: function(ctx,x,y,width,height,r,g,b,a){
        ctx.beginPath();
        ctx.moveTo(x+width,y);
        ctx.lineTo(x,y+height);
        ctx.lineTo(x+width,y+height);
        ctx.closePath();
        ctx.fillStyle = 'rgba(' + r +',' + g + ',' + b + ',' + a + ')';    
        ctx.fill();
    },

    restorebutton: function(ctx,x,y,r,g,b,a){
        //circle
        ctx.beginPath();
        ctx.moveTo(x,y);        
        ctx.arc(x,y,7,0,Math.PI*2,true);
        ctx.fillStyle = 'rgba(' + r +',' + g + ',' + b + ',' + a + ')';    
        ctx.fill();                        
        //plus sign
        ctx.beginPath();
        ctx.moveTo(x,y-4);
        ctx.lineTo(x,y+4);                                
        ctx.stroke();
        ctx.beginPath();
        ctx.moveTo(x-4,y);
        ctx.lineTo(x+4,y);                                
        ctx.stroke();        
    },

    

    closebutton: function(ctx,x,y,r,g,b,a){
        //circle
        ctx.beginPath();
        ctx.moveTo(x,y);                    
        ctx.arc(x,y,7,0,Math.PI*2,true);
        ctx.fillStyle = 'rgba(' + r +',' + g + ',' + b + ',' + a + ')';
        ctx.fill();
        //plus sign
        ctx.beginPath();
        ctx.moveTo(x-3,y-3);
        ctx.lineTo(x+3,y+3);                                
        ctx.stroke();
        ctx.beginPath();
        ctx.moveTo(x+3,y-3);
        ctx.lineTo(x-3,y+3);                                
        ctx.stroke();        
    }
}); 


Ext.reg('examplecanvas', Tramada.MochaWindow);
