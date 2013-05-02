Ext.direct.LongPollingProvider = Ext.extend(Ext.direct.JsonProvider, {
    /**
     * @cfg {Number} priority
     * Priority of the request (defaults to <tt>3</tt>). See {@link Ext.direct.Provider#priority}.
     */
    // override default priority
    priority: 3,
    
    /**
     * @cfg {Number} interval
     * How often to poll the server-side in milliseconds (defaults to <tt>3000</tt> - every
     * 3 seconds).
     */
    interval: 3000,

    /**
     * @cfg {Object} baseParams An object containing properties which are to be sent as parameters
     * on every polling request
     */
    
    /**
     * @cfg {String/Function} url
     * The url which the PollingProvider should contact with each request. This can also be
     * an imported Ext.Direct method which will accept the baseParams as its only argument.
     */

    // private
    constructor : function(config){
        Ext.direct.PollingProvider.superclass.constructor.call(this, config);
        this.addEvents(
            /**
             * @event beforepoll
             * Fired immediately before a poll takes place, an event handler can return false
             * in order to cancel the poll.
             * @param {Ext.direct.PollingProvider}
             */
            'beforepoll',            
            /**
             * @event poll
             * This event has not yet been implemented.
             * @param {Ext.direct.PollingProvider}
             */
            'poll'
        );
    },

    // inherited
    isConnected: function(){
        return !!this.isPolling;
    },

    run: function(){
        var _this = this;
    	return (function() {
    		if(!_this.isPolling) return;
    		
	        if(_this.fireEvent('beforepoll', _this) !== false){
	            if(typeof _this.url == 'function'){
	                _this.url(_this.baseParams);
	            }
	            else{
	                Ext.Ajax.request({
	                    url: _this.url,
	                    callback: _this.onData,
	                    scope: _this,
	                    params: _this.baseParams
	                });
	            }
	        }
    	});
    },
                
    /**
     * Connect to the server-side and begin the polling process. To handle each
     * response subscribe to the data event.
     */
    connect: function(){
        if(this.url && !this.isPolling){
           	this.isPolling = true;
            setTimeout(this.run(), this.interval);
            this.fireEvent('connect', this);
        }
        else if(!this.url){
            throw 'Error initializing LongPollingProvider, no url configured.';
        }
    },

    /**
     * Disconnect from the server-side and stop the polling process. The disconnect
     * event will be fired on a successful disconnect.
     */
    disconnect: function(){
        if(this.isPolling){
            delete this.isPolling;
            this.fireEvent('disconnect', this);
        }
    },

    // private
    onData: function(opt, success, xhr){
        if(success){
        	setTimeout(this.run(), this.interval);
        	
            var events = this.getEvents(xhr);
            for(var i = 0, len = events.length; i < len; i++){
                var e = events[i];
                this.fireEvent('data', this, e);
            }
        }
        else{
        	setTimeout(this.run(), 15000);

        	var e = new Ext.Direct.ExceptionEvent({
                data: e,
                code: Ext.Direct.exceptions.TRANSPORT,
                message: 'Unable to connect to the server.',
                xhr: xhr
            });
            this.fireEvent('data', this, e);
        }
    }
});

Ext.Direct.PROVIDERS['longpolling'] = Ext.direct.LongPollingProvider;