
var mxClient=
{
VERSION:'0.14.0.1',
IS_IE:navigator.appName.toUpperCase()=='MICROSOFT INTERNET EXPLORER',
IS_IE7:navigator.appName.toUpperCase()=='MICROSOFT INTERNET EXPLORER'&&navigator.userAgent.indexOf('MSIE 7')>=0,
IS_NS:navigator.appName=='Netscape',
IS_FF2:navigator.userAgent.indexOf('Firefox/2')>=0||navigator.userAgent.indexOf('Iceweasel/2')>=0,
IS_FF3:navigator.userAgent.indexOf('Firefox/3')>=0||navigator.userAgent.indexOf('Iceweasel/3')>=0,
IS_OP:navigator.appName=='Opera',
IS_SF:navigator.userAgent.indexOf('AppleWebKit')>=0&&navigator.userAgent.indexOf('Chrome')<0,
IS_GC:navigator.userAgent.indexOf('Chrome')>=0,
IS_SVG:navigator.userAgent.indexOf('Firefox/1.5')>=0||navigator.userAgent.indexOf('Firefox/2')>=0||navigator.userAgent.indexOf('Firefox/3')>=0||navigator.userAgent.indexOf('Iceweasel/1.5')>=0||
navigator.userAgent.indexOf('Iceweasel/2')>=0||
navigator.userAgent.indexOf('Iceweasel/3')>=0||
navigator.userAgent.indexOf('Camino/1')>=0||navigator.userAgent.indexOf('Epiphany/2')>=0||navigator.userAgent.indexOf('Iceape/1')>=0||navigator.userAgent.indexOf('Galeon/2')>=0||navigator.userAgent.indexOf('Opera/9')>=0||navigator.userAgent.indexOf('Gecko/')>=0||
navigator.userAgent.indexOf('AppleWebKit/5')>=0,

IS_VML:navigator.appName.toUpperCase()=='MICROSOFT INTERNET EXPLORER',
IS_CANVAS:navigator.appName=='Netscape',
IS_MAC:navigator.userAgent.toUpperCase().indexOf('MACINTOSH')>0,
IS_LOCAL:document.location.href.indexOf('http://')<0&&document.location.href.indexOf('https://')<0,
FADE_RUBBERBAND:false,
WINDOW_SHADOWS:true,
TOOLTIP_SHADOWS:true,
MENU_SHADOWS:true,
isBrowserSupported:function()
{
return mxClient.IS_VML||mxClient.IS_SVG;
},
link:function(rel,href,doc)
{
doc=doc||document;
if(mxClient.IS_IE&&!mxClient.IS_IE7)
{
doc.write('<link rel="'+rel+'" href="'+href+'" charset="ISO-8859-1" type="text/css"/>');
}
else
{
var link=doc.createElement('link');
link.setAttribute('rel',rel);
link.setAttribute('href',href);
link.setAttribute('charset','ISO-8859-1');
link.setAttribute('type','text/css');
var head=doc.getElementsByTagName('head')[0];
head.appendChild(link);
}
},
addOnloadCallback:function(callback)
{
if(mxClient.loading==null||mxClient.loading==0)
{
callback();
}
else
{
if(mxClient.onloadCallbacks==null)
{
mxClient.onloadCallbacks=new Array();
}
mxClient.onloadCallbacks.push(callback);
}
},
onload:function()
{
if(mxClient.onloadCallbacks!=null)
{
var cb=mxClient.onloadCallbacks;
mxClient.onloadCallbacks=null;
for(var i=0;i<cb.length;i++)
{
cb[i]();
}
}
if(mxClient.IS_IE)
{
window.attachEvent("onunload",function()
{
mxClient.unload();
});
}
},
include:function(src)
{
if(mxClient.IS_SF||mxClient.IS_GC)
{
var req=new XMLHttpRequest();
req.open('GET',src,false);
req.send();
window._mxDynamicCode=req.responseText;
var script=document.createElement('script');
script.type='text/javascript';
script.innerHTML='eval(window._mxDynamicCode)';
var head=document.getElementsByTagName('head')[0];
head.appendChild(script)
delete window._mxDynamicCode;
}
else
{
var script=document.createElement('script');
script.setAttribute('type','text/javascript');
script.setAttribute('src',src);
var onload=function(script)
{
if(script!=null)
{
script.onload=null;
script.onerror=null;
script.onreadystatechange=null;
}
mxClient.loading--;
if(mxClient.loading==0)
{
mxClient.onload();
}
};
script.onload=onload;
script.onerror=onload;
script.onreadystatechange=function(evt)
{
if(script.readyState=='loaded')
{
onload(script);
}
else if(script.readyState=='complete')
{
onload(script);
}
};
var head=document.getElementsByTagName('head')[0];
head.appendChild(script);
if(mxClient.loading==null)
{
mxClient.loading=1;
}
else
{
mxClient.loading++;
}
}
},
unload:function()
{
mxUtils.release(document.documentElement);
mxUtils.release(window);
}
};
mxClient.basePath=(typeof(mxBasePath)!='undefined')?mxBasePath:'';
mxClient.imageBasePath=(typeof(mxImageBasePath)!='undefined')?mxImageBasePath:mxClient.basePath+'images/';
if(typeof(mxLanguage)!='undefined')
{
mxClient.language=mxLanguage;
}
else
{
mxClient.language=(mxClient.IS_IE)?navigator.userLanguage:navigator.language;
var dash=mxClient.language.indexOf('-');
if(dash>0)
{
mxClient.language=mxClient.language.substring(0,dash);
}
}
mxClient.link('stylesheet',mxClient.basePath+'css/common.css');
if(mxClient.IS_IE)
{
document.namespaces.add("v","urn:schemas-microsoft-com:vml");
document.namespaces.add("o","urn:schemas-microsoft-com:office:office");
mxClient.link('stylesheet',mxClient.basePath+'css/explorer.css');
}

var mxLog=
{
consoleResource:(mxClient.language!='none')?'console':'',
TRACE:false,
DEBUG:true,
WARN:true,
buffer:'',
init:function()
{
if(mxLog.window==null&&document.body!=null)
{
var title=(mxResources.get(mxLog.consoleResource)||mxLog.consoleResource)+' - mxGraph '+mxClient.VERSION;
var table=document.createElement('table');
table.setAttribute('width','100%');
table.setAttribute('height','100%');
var tbody=document.createElement('tbody');
var tr=document.createElement('tr');
var td=document.createElement('td');
td.style.verticalAlign='top';
mxLog.textarea=document.createElement('textarea');
mxLog.textarea.setAttribute('readOnly','true');
mxLog.textarea.style.width="100%";
mxLog.textarea.style.height="100%";
mxLog.textarea.value=mxLog.buffer;
td.appendChild(mxLog.textarea);
tr.appendChild(td);
tbody.appendChild(tr);
tr=document.createElement('tr');
mxLog.td=document.createElement('td');
mxLog.td.style.verticalAlign='top';
mxLog.td.setAttribute('height','30px');
tr.appendChild(mxLog.td);
tbody.appendChild(tr);
table.appendChild(tbody);
mxLog.addButton('Info',function(evt)
{
mxLog.writeln(mxUtils.toString(navigator));
});
mxLog.addButton('DOM',function(evt)
{
var content=mxUtils.getInnerHtml(document.body);
mxLog.debug(content);
});
mxLog.addButton('Trace',function(evt)
{
mxLog.TRACE=!mxLog.TRACE;
if(mxLog.TRACE)
{
mxLog.debug('Tracing enabled');
}
else
{
mxLog.debug('Tracing disabled');
}
});
mxLog.addButton('Copy',function(evt)
{
try
{
mxUtils.copy(mxLog.textarea.value);
}
catch(err)
{
mxUtils.alert(err);
}
});
mxLog.addButton('Show',function(evt)
{
try
{
mxUtils.popup(mxLog.textarea.value);
}
catch(err)
{
mxUtils.alert(err);
}
});
mxLog.addButton('Clear',function(evt)
{
mxLog.textarea.value='';
});
var w=document.body.clientWidth;
var h=(document.body.clientHeight||document.documentElement.clientHeight);
mxLog.window=new mxWindow(title,table,w-320,h-210,300,160);
mxLog.window.setMaximizable(true);
mxLog.window.setScrollable(true);
mxLog.window.setSizable(true);
mxLog.window.setClosable(true);
mxLog.window.destroyOnClose=false;
if(mxClient.IS_NS&&document.compatMode!='BackCompat')
{
var resizeHandler=function(sender,evt)
{
var elt=mxLog.window.getElement();
mxLog.textarea.style.height=(elt.offsetHeight-78)+'px';
};
mxLog.window.addListener('resize',resizeHandler);
mxLog.window.addListener('maximize',resizeHandler);
mxLog.window.addListener('normalize',resizeHandler);
var elt=mxLog.window.getElement();
mxLog.textarea.style.height='96px';
}
}
},
addButton:function(lab,funct)
{
var button=document.createElement('button');
mxUtils.write(button,lab);
mxEvent.addListener(button,'click',funct);
mxLog.td.appendChild(button);
},
isVisible:function()
{
if(mxLog.window!=null)
{
return mxLog.window.isVisible();
}
return false;
},
show:function()
{
mxLog.setVisible(true);
},
setVisible:function(visible)
{
if(mxLog.window==null)
{
mxLog.init();
}
if(mxLog.window!=null)
{
mxLog.window.setVisible(visible);
}
},
enter:function(string)
{
if(mxLog.TRACE)
{
mxLog.writeln('Entering '+string);
return new Date().getTime();
}
},
leave:function(string,t0)
{
if(mxLog.TRACE)
{
var dt=(t0!=0)?' ('+(new Date().getTime()-t0)+' ms)':'';
mxLog.writeln('Leaving '+string+dt);
}
},
debug:function(string)
{
if(mxLog.DEBUG)
{
mxLog.writeln(string);
}
},
warn:function(string)
{
if(mxLog.WARN)
{
mxLog.writeln(string);
}
},
write:function(string)
{
if(mxLog.textarea!=null)
{
mxLog.textarea.value=mxLog.textarea.value+string;
mxLog.textarea.scrollTop=mxLog.textarea.scrollHeight;
}
else
{
mxLog.buffer+=string;
}
},
writeln:function(string)
{
mxLog.write(string+'\n');
}
};

var mxObjectIdentity=
{
FIELD_NAME:'mxObjectId',
counter:0,
get:function(obj)
{
if(typeof(obj)=='object'&&obj[mxObjectIdentity.FIELD_NAME]==null)
{
var ctor=mxUtils.getFunctionName(obj.constructor);
obj[mxObjectIdentity.FIELD_NAME]=ctor+'#'+mxObjectIdentity.counter++;
}
return obj[mxObjectIdentity.FIELD_NAME];
},
clear:function(obj)
{
if(typeof(obj)=='object')
{
delete obj[mxObjectIdentity.FIELD_NAME];
}
}
};

{
function mxDictionary()
{
this.clear();
};
mxDictionary.prototype.values=null;
mxDictionary.prototype.clear=function()
{
this.values=new Array();
};
mxDictionary.prototype.get=function(key)
{
var id=mxObjectIdentity.get(key);
return this.values[id];
};
mxDictionary.prototype.put=function(key,value)
{
var id=mxObjectIdentity.get(key);
var previous=this.values[id];
this.values[id]=value;
return previous;
};
mxDictionary.prototype.remove=function(key)
{
var id=mxObjectIdentity.get(key);
var previous=this.values[id];
delete this.values[id];
return previous;
};
}

var mxResources=
{
resources:new Array(),
add:function(basename,lan)
{
lan=(lan!=null)?lan:mxClient.language;
if(lan!='none')
{
try
{
var req=mxUtils.load(basename+'.properties');
if(req.isReady())
{
mxResources.parse(req.getText());
}
}
catch(e)
{
}
try
{
var req=mxUtils.load(basename+'_'+lan+'.properties');
if(req.isReady())
{
mxResources.parse(req.getText());
}
}
catch(e)
{
}
}
},
parse:function(text)
{
var lines=text.split('\n');
for(var i=0;i<lines.length;i++)
{
var index=lines[i].indexOf('=');
if(index>0)
{
var key=lines[i].substring(0,index);
var idx=lines[i].length;
if(lines[i].charCodeAt(idx-1)==13)
{
idx--;
}
var value=lines[i].substring(index+1,idx);
mxResources.resources[key]=unescape(value);
}
}
},
get:function(key,params,defaultValue)
{
var value=mxResources.resources[key];
if(value==null)
{
value=defaultValue;
}
if(value!=null&&params!=null)
{
var result=new Array();
var index=null;
for(var i=0;i<value.length;i++)
{
var c=value.charAt(i);
if(c=='{')
{
index='';
}
else if(index!=null&&c=='}')
{
index=parseInt(index)-1;
if(index>=0&&index<params.length)
{
result.push(params[index]);
}
index=null;
}
else if(index!=null)
{
index+=c;
}
else
{
result.push(c);
}
}
value=result.join('');
}
return value;
}
};

{
function mxPoint(x,y)
{
this.x=(x!=null)?x:0;
this.y=(y!=null)?y:0;
};
mxPoint.prototype.x=null;
mxPoint.prototype.y=null;
mxPoint.prototype.clone=function()
{
return mxUtils.clone(this);
};
}

{
function mxRectangle(x,y,width,height)
{
mxPoint.call(this,x,y);
this.width=(width!=null)?width:0;
this.height=(height!=null)?height:0;
};
mxRectangle.prototype=new mxPoint();
mxRectangle.prototype.constructor=mxRectangle;
mxRectangle.prototype.width=null;
mxRectangle.prototype.height=null;
mxRectangle.prototype.add=function(rect)
{
if(rect!=null)
{
var minX=Math.min(this.x,rect.x);
var minY=Math.min(this.y,rect.y);
var maxX=Math.max(this.x+this.width,rect.x+rect.width);
var maxY=Math.max(this.y+this.height,rect.y+rect.height);
this.x=minX;
this.y=minY;
this.width=maxX-minX;
this.height=maxY-minY;
}
};
mxRectangle.prototype.grow=function(amount)
{
this.x-=amount;
this.y-=amount;
this.width+=2*amount;
this.height+=2*amount;
};
mxRectangle.prototype.getPoint=function()
{
return new mxPoint(this.x,this.y);
};
}

var mxUtils=
{
errorResource:(mxClient.language!='none')?'error':'',
closeResource:(mxClient.language!='none')?'close':'',
errorImage:mxClient.imageBasePath+'error.gif',
release:function(element)
{
if(element!=null)
{
mxEvent.removeAllListeners(element);
var children=element.childNodes;
if(children!=null)
{
var childCount=children.length;
for(var i=0;i<childCount;i+=1)
{
mxUtils.release(children[i]);
}
}
}
},
removeCursors:function(element)
{
if(element.style!=null)
{
element.style.cursor=null;
}
var children=element.childNodes;
if(children!=null)
{
var childCount=children.length;
for(var i=0;i<childCount;i+=1)
{
mxUtils.removeCursors(children[i]);
}
}
},
getCurrentStyle:function()
{
if(mxClient.IS_IE)
{
return function(element)
{
return(element!=null)?element.currentStyle:null;
}
}
else
{
return function(element)
{
return(element!=null)?window.getComputedStyle(element,''):
null;
}
}
}(),
hasScrollbars:function(node)
{
var style=mxUtils.getCurrentStyle(node);
return style!=null&&(style.overflow=='scroll'||style.overflow=='auto');
},
eval:function(expr)
{
var result=null;
if(expr.indexOf('function')>=0&&(mxClient.IS_IE||mxClient.IS_SF||mxClient.IS_FF3||mxClient.IS_OP))
{
try
{
eval('var _mxJavaScriptExpression='+expr);
result=_mxJavaScriptExpression;
delete _mxJavaScriptExpression;
}
catch(e)
{
mxLog.warn(e.message+' while evaluating '+expr);
}
}
else
{
result=eval(expr);
}
return result;
},
selectSingleNode:function()
{
if(mxClient.IS_IE)
{
return function(doc,expr)
{
return doc.selectSingleNode(expr);
}
}
else
{
return function(doc,expr)
{
var result=doc.evaluate(expr,doc,null,XPathResult.ANY_TYPE,null);
return result.iterateNext();
}
}
}(),
getFunctionName:function(f)
{
var str=null;
if(f!=null)
{
if(!mxClient.IS_SF&&mxClient.IS_NS)
{
str=f.name;
}
else
{
var tmp=f.toString();
var idx1=9;
while(tmp.charAt(idx1)==' ')
{
idx1++;
}
var idx2=tmp.indexOf('(',idx1);
str=tmp.substring(idx1,idx2);
}
}
return str;
},
indexOf:function(array,obj)
{
if(array!=null&&obj!=null)
{
for(var i=0;i<array.length;i++)
{
if(array[i]==obj)
{
return i;
}
}
}
return-1;
},
remove:function(obj,array)
{
var result=null;
if(typeof(array)=='object')
{
var index=mxUtils.indexOf(array,obj);
while(index>=0)
{
array.splice(index,1);
result=obj;
index=mxUtils.indexOf(array,obj);
}
}
for(var key in array)
{
if(array[key]==obj)
{
delete array[key];
result=obj;
}
}
return result;
},
isNode:function(value,nodeName,attributeName,attributeValue)
{
if(value!=null&&!isNaN(value.nodeType)&&(nodeName==null||value.nodeName.toLowerCase()==nodeName.toLowerCase()))
{
return attributeName==null||value.getAttribute(attributeName)==attributeValue;
}
return false;
},
getChildNodes:function(node,nodeType)
{
nodeType=nodeType||mxConstants.NODETYPE_ELEMENT;
var children=new Array();
var tmp=node.firstChild;
while(tmp!=null)
{
if(tmp.nodeType==nodeType)
{
children.push(tmp);
}
tmp=tmp.nextSibling;
}
return children;
},
createXmlDocument:function()
{
var doc=null;
if(document.implementation&&document.implementation.createDocument)
{
doc=document.implementation.createDocument("","",null);
}
else if(window.ActiveXObject)
{
doc=new ActiveXObject("Microsoft.XMLDOM");
}
return doc;
},
parseXml:function(xml)
{
if(mxClient.IS_IE)
{
return function(xml)
{
var result=mxUtils.createXmlDocument();
result.async="false";
result.loadXML(xml)
return result;
}
}
else
{
return function(xml)
{
var parser=new DOMParser();
return parser.parseFromString(xml,"text/xml");
}
}
}(),
createXmlElement:function(nodeName)
{
return mxUtils.parseXml('<'+nodeName+'/>').documentElement;
},
getPrettyXml:function(node,tab,indent)
{
var result=new Array();
if(node!=null)
{
tab=tab||'  ';
indent=indent||'';
if(node.nodeType==mxConstants.NODETYPE_TEXT)
{
result.push(node.nodeValue);
}
else
{
result.push(indent+'<'+node.nodeName);

var attrs=node.attributes;
if(attrs!=null)
{
for(var i=0;i<attrs.length;i++)
{
var val=mxUtils.htmlEntities(attrs[i].nodeValue);
result.push(' '+attrs[i].nodeName+'="'+val+'"');
}
}


var tmp=node.firstChild;
if(tmp!=null)
{
result.push('>\n');
while(tmp!=null)
{
result.push(mxUtils.getPrettyXml(tmp,tab,indent+tab));
tmp=tmp.nextSibling;
}
result.push(indent+'</'+node.nodeName+'>\n');
}
else
{
result.push('/>\n');
}
}
}
return result.join('');
},
removeWhitespace:function(node,before)
{
var tmp=(before)?node.previousSibling:node.nextSibling;
while(tmp!=null&&tmp.nodeType==mxConstants.NODETYPE_TEXT)
{
var next=(before)?tmp.previousSibling:tmp.nextSibling;
var text=mxUtils.getTextContent(tmp).replace(/\t/g,'').replace(/\r\n/g,'').replace(/\n/g,'').replace(/^\s+/g,'').replace(/\s+$/g,'');
if(text.length==0)
{
tmp.parentNode.removeChild(tmp);
}
tmp=next;
}
},
htmlEntities:function(s,newline)
{
s=s||'';
s=s.replace(/&/g,'&amp;');
s=s.replace(/"/g,'&quot;');
s=s.replace(/\'/g,'&#39;');
s=s.replace(/</g,'&lt;');
s=s.replace(/>/g,'&gt;');
if(newline==null||newline)
{
s=s.replace(/\n/g,'&#xa;');
}
return s;
},
isVml:function(node)
{
return node!=null&&node.tagUrn=='urn:schemas-microsoft-com:vml';
},
getXml:function(node,linefeed)
{
var xml='';
if(node!=null)
{
xml=node.xml;
if(xml==null)
{
if(mxClient.IS_IE)
{
xml=node.innerHTML;
}
else
{
var xmlSerializer=new XMLSerializer();
xml=xmlSerializer.serializeToString(node);
}
}
else
{
xml=xml.replace(/\r\n\t[\t]*/g,'').replace(/>\r\n/g,'>').replace(/\r\n/g,'\n');
}
}
linefeed=linefeed||'&#xa;';
xml=xml.replace(/\n/g,linefeed);
return xml;
},
getTextContent:function(node)
{
var result='';
if(node!=null)
{
if(node.firstChild!=null)
{
node=node.firstChild;
}
result=node.nodeValue||'';
}
return result;
},
getInnerHtml:function()
{
if(mxClient.IS_IE)
{
return function(node)
{
if(node!=null)
{
return node.innerHTML;
}
return '';
}
}
else
{
return function(node)
{
if(node!=null)
{
var serializer=new XMLSerializer();
return serializer.serializeToString(node);
}
return '';
}
}
}(),
getOuterHtml:function()
{
if(mxClient.IS_IE)
{
return function(node)
{
if(node!=null)
{
var tmp=new Array();
tmp.push('<'+node.nodeName);
var attrs=node.attributes;
for(var i=0;i<attrs.length;i++)
{
var value=attrs[i].nodeValue;
if(value!=null&&value.length>0)
{
tmp.push(' ');
tmp.push(attrs[i].nodeName);
tmp.push('="');
tmp.push(value);
tmp.push('"');
}
}
if(node.innerHTML.length==0)
{
tmp.push('/>');
}
else
{
tmp.push('>');
tmp.push(node.innerHTML);
tmp.push('</'+node.nodeName+'>');
}
return tmp.join('');
}
return '';
}
}
else
{
return function(node)
{
if(node!=null)
{
var serializer=new XMLSerializer();
return serializer.serializeToString(node);
}
return '';
}
}
}(),
write:function(parent,text)
{
var node=document.createTextNode(text);
if(parent!=null)
{
parent.appendChild(node);
}
return node;
},
writeln:function(parent,text)
{
var node=document.createTextNode(text);
if(parent!=null)
{
parent.appendChild(node);
parent.appendChild(document.createElement('br'));
}
return node;
},
br:function(parent,count)
{
count=count||1;
var br;
for(var i=0;i<count;i++)
{
br=document.createElement('br');
if(parent!=null)
{
parent.appendChild(br);
}
}
return br;
},
button:function(label,funct)
{
var button=document.createElement('button');
mxUtils.write(button,label);
mxEvent.addListener(button,'click',function(evt)
{
funct(evt);
});
return button;
},
para:function(parent,text)
{
var p=document.createElement('p');
mxUtils.write(p,text);
if(parent!=null)
{
parent.appendChild(p);
}
return p;
},
linkAction:function(parent,text,editor,action,pad)
{
var a=mxUtils.link(parent,text,function()
{
editor.execute(action)
},pad);
return a;
},
linkInvoke:function(parent,text,editor,functName,arg,pad)
{
var a=mxUtils.link(parent,text,function()
{
editor[functName](arg)
},pad);
return a;
},
link:function(parent,text,funct,pad)
{
var a=document.createElement('span');
a.style.color='blue';
a.style.textDecoration='underline';
a.style.cursor='pointer';
if(pad!=null)
{
a.style.paddingLeft=pad+'px';
}
mxEvent.addListener(a,'click',funct);
mxUtils.write(a,text);
if(parent!=null)
{
parent.appendChild(a);
}
return a;
},
fit:function(node)
{
var left=node.offsetLeft;
var width=node.offsetWidth;
var b=document.body;
var d=document.documentElement;
var right=(b.scrollLeft||d.scrollLeft)+(b.clientWidth||d.clientWidth);
if(left+width>right)
{
node.style.left=Math.max((b.scrollLeft||d.scrollLeft),right-width)+'px';
}
var top=parseInt(node.offsetTop);
var height=parseInt(node.offsetHeight);
var bottom=(b.scrollTop||d.scrollTop)+Math.max(b.clientHeight||0,d.clientHeight);
if(top+height>bottom)
{
node.style.top=Math.max((b.scrollTop||d.scrollTop),bottom-height)+'px';
}
},
open:function(filename)
{
if(mxClient.IS_NS)
{
try
{
netscape.security.PrivilegeManager.enablePrivilege("UniversalXPConnect");
}
catch(e)
{
mxUtils.alert('Permission to read file denied.');
return '';
}
var file=Components.classes["@mozilla.org/file/local;1"].createInstance(Components.interfaces.nsILocalFile);
file.initWithPath(filename);
if(!file.exists())
{
mxUtils.alert('File not found.');
return '';
}
var is=Components.classes["@mozilla.org/network/file-input-stream;1"].createInstance(Components.interfaces.nsIFileInputStream);
is.init(file,0x01,00004,null);
var sis=Components.classes["@mozilla.org/scriptableinputstream;1"].createInstance(Components.interfaces.nsIScriptableInputStream);
sis.init(is);
var output=sis.read(sis.available());
return output;
}
else
{
var activeXObject=new ActiveXObject("Scripting.FileSystemObject");
var newStream=activeXObject.OpenTextFile(filename,1);
var text=newStream.readAll();
newStream.close();
return text;
}
return null;
},
save:function(filename,content)
{
if(mxClient.IS_NS)
{
try
{
netscape.security.PrivilegeManager.enablePrivilege('UniversalXPConnect');
}
catch(e)
{
mxUtils.alert('Permission to write file denied.');
return;
}
var file=Components.classes["@mozilla.org/file/local;1"].createInstance(Components.interfaces.nsILocalFile);
file.initWithPath(filename);
if(!file.exists())
{
file.create(0x00,0644);
}
var outputStream=Components.classes["@mozilla.org/network/file-output-stream;1"].createInstance(Components.interfaces.nsIFileOutputStream);
outputStream.init(file,0x20|0x02,00004,null);
outputStream.write(content,content.length);
outputStream.flush();
outputStream.close();
}
else
{
var fso=new ActiveXObject('Scripting.FileSystemObject');
var file=fso.CreateTextFile(filename,true);
file.Write(content);
file.Close();
}
},
saveAs:function(content)
{
var iframe=document.createElement('iframe');
iframe.setAttribute('src','');
iframe.style.visibility='hidden';
iframe.style.display='none';
document.body.appendChild(iframe);
try
{
if(mxClient.IS_NS)
{
var doc=iframe.contentDocument;
doc.open();
doc.write(content);
doc.close();
try
{
netscape.security.PrivilegeManager.enablePrivilege('UniversalXPConnect');
iframe.focus();
saveDocument(doc);
}
catch(e)
{
mxUtils.alert('Permission to save document denied.');
}
}
else
{
var doc=iframe.contentWindow.document;
doc.write(content);
doc.execCommand('SaveAs',false,document.location);
}
}
finally
{
document.body.removeChild(iframe);
}
},
copy:function(content)
{
if(window.clipboardData)
{
window.clipboardData.setData("Text",content);
}
else
{
netscape.security.PrivilegeManager.enablePrivilege('UniversalXPConnect');
var clip=Components.classes['@mozilla.org/widget/clipboard;1'].createInstance(Components.interfaces.nsIClipboard);
if(!clip)
{
return;
}
var trans=Components.classes['@mozilla.org/widget/transferable;1'].createInstance(Components.interfaces.nsITransferable);
if(!trans)
{
return;
}
trans.addDataFlavor('text/unicode');
var str=new Object();
var len=new Object();
var str=Components.classes["@mozilla.org/supports-string;1"].createInstance(Components.interfaces.nsISupportsString);
var copytext=content;
str.data=copytext;
trans.setTransferData("text/unicode",str,copytext.length*2);
var clipid=Components.interfaces.nsIClipboard;
clip.setData(trans,null,clipid.kGlobalClipboard);
}
},
load:function(url)
{
var req=new mxXmlRequest(url,null,'GET',false);
req.send();
return req;
},
get:function(url,onload,onerror)
{
return new mxXmlRequest(url,null,'GET').send(onload,onerror);
},
post:function(url,params,onload,onerror)
{
return new mxXmlRequest(url,params).send(onload,onerror);
},
submit:function(url,params,doc)
{
return new mxXmlRequest(url,params).simulate(doc);
},
loadInto:function(url,doc,onload)
{
if(mxClient.IS_IE)
{
doc.onreadystatechange=function()
{
if(doc.readyState==4)
{
onload()
}
};
}
else
{
doc.addEventListener("load",onload,false);
}
doc.load(url);
},
getValue:function(array,key,defaultValue)
{
var value=array[key];
if(value==null)
{
value=defaultValue;
}
return value;
},
clone:function(obj,transients,shallow)
{
shallow=(shallow!=null)?shallow:false;
var clone=null;
if(obj!=null&&typeof(obj.constructor)=='function')
{
clone=new obj.constructor();
for(var i in obj)
{
if(i!=mxObjectIdentity.FIELD_NAME&&(transients==null||mxUtils.indexOf(transients,i)<0))
{
if(!shallow&&typeof(obj[i])=='object')
{
clone[i]=mxUtils.clone(obj[i]);
}
else
{
clone[i]=obj[i];
}
}
}
}
return clone;
},
equals:function(a,b)
{
return b!=null&&((a.x==null||a.x==b.x)&&(a.y==null||a.y==b.y)&&(a.width==null||a.width==b.width)&&(a.height==null||a.height==b.height));
},
toString:function(obj)
{
var output='';
for(var i in obj)
{
try
{
if(obj[i]==null)
{
output+=i+' = [null]\n';
}
else if(typeof(obj[i])=='function')
{
output+=i+' => [Function]\n';
}
else if(typeof(obj[i])=='object')
{
var ctor=mxUtils.getFunctionName(obj[i].constructor);
output+=i+' => ['+ctor+']\n';
}
else
{
output+=i+' = '+obj[i]+'\n';
}
}
catch(e)
{
output+=i+'='+e.message;
}
}
return output;
},
toRadians:function(deg)
{
return Math.PI*deg/180;
},
getBoundingBox:function(rect,rotation)
{
var result=null;
if(rect!=null&&rotation!=null&&rotation!=0)
{
var rad=mxUtils.toRadians(rotation);
var cos=Math.cos(rad);
var sin=Math.sin(rad);
var cx=new mxPoint(rect.x+rect.width/2,rect.y+rect.height/2);
var p1=new mxPoint(rect.x,rect.y);
var p2=new mxPoint(rect.x+rect.width,rect.y);
var p3=new mxPoint(p2.x,rect.y+rect.height);
var p4=new mxPoint(rect.x,p3.y);
p1=mxUtils.getRotatedPoint(p1,cos,sin,cx);
p2=mxUtils.getRotatedPoint(p2,cos,sin,cx);
p3=mxUtils.getRotatedPoint(p3,cos,sin,cx);
p4=mxUtils.getRotatedPoint(p4,cos,sin,cx);
result=new mxRectangle(p1.x,p1.y,0,0);
result.add(new mxRectangle(p2.x,p2.y,0,0));
result.add(new mxRectangle(p3.x,p3.y,0,0));
result.add(new mxRectangle(p4.x,p4.Y,0,0));
}
return result;
},
getRotatedPoint:function(pt,cos,sin,cx)
{
cx=(cx!=null)?cx:new mxPoint();
var x=pt.x-c.x;
var y=pt.y-c.y;
var x1=x*cos-y*sin;
var y1=y*cos+x*sin;
return new mxPoint(x1+c.x,y1+c.y);
},
contains:function(bounds,x,y)
{
return(bounds.x<=x&&bounds.x+bounds.width>=x&&bounds.y<=y&&bounds.y+bounds.height>=y);
},
intersects:function(a,b)
{
return mxUtils.contains(a,b.x,b.y)||mxUtils.contains(a,b.x+b.width,b.y+b.height)||mxUtils.contains(a,b.x+b.width,b.y)||mxUtils.contains(a,b.x,b.y+b.height);
},
getOffset:function(container)
{
var offsetLeft=0;
var offsetTop=0;
while(container.offsetParent)
{
offsetLeft+=container.offsetLeft;
offsetTop+=container.offsetTop;
container=container.offsetParent;
}
return new mxPoint(offsetLeft,offsetTop);
},
getScrollOrigin:function(node)
{
var b=document.body;
var d=document.documentElement;
var sl=(b.scrollLeft||d.scrollLeft);
var st=(b.scrollTop||d.scrollTop);
var result=new mxPoint(sl,st);
while(node!=null&&node!=b&&node!=d)
{
result.x+=node.scrollLeft;
result.y+=node.scrollTop;
node=node.parentNode;
}
return result;
},
convertPoint:function(container,x,y)
{
var origin=mxUtils.getScrollOrigin(container);
var offset=mxUtils.getOffset(container);
offset.x-=origin.x;
offset.y-=origin.y;
return new mxPoint(x-offset.x,y-offset.y);
},
isNumeric:function(str)
{
return str!=null&&(str.length==null||(str.length>0&&str.indexOf('0x')<0)&&str.indexOf('0X')<0)&&!isNaN(str);
},
intersection:function(x0,y0,x1,y1,x2,y2,x3,y3)
{
var denom=((y3-y2)*(x1-x0))-((x3-x2)*(y1-y0));
var nume_a=((x3-x2)*(y0-y2))-((y3-y2)*(x0-x2));
var nume_b=((x1-x0)*(y0-y2))-((y1-y0)*(x0-x2));
var ua=nume_a/denom;
var ub=nume_b/denom;
if(ua>=0.0&&ua<=1.0&&ub>=0.0&&ub<=1.0)
{
var intersectionX=x0+ua*(x1-x0);
var intersectionY=y0+ua*(y1-y0);
return new mxPoint(intersectionX,intersectionY);
}
return null;
},
ptSegDistSq:function(x1,y1,x2,y2,px,py)
{
x2-=x1;
y2-=y1;
px-=x1;
py-=y1;
var dotprod=px*x2+py*y2;
var projlenSq;
if(dotprod<=0.0)
{
projlenSq=0.0;
}
else
{
px=x2-px;
py=y2-py;
dotprod=px*x2+py*y2;
if(dotprod<=0.0)
{
projlenSq=0.0;
}
else
{
projlenSq=dotprod*dotprod/(x2*x2+y2*y2);
}
}
var lenSq=px*px+py*py-projlenSq;
if(lenSq<0)
{
lenSq=0;
}
return lenSq;
},
relativeCcw:function(x1,y1,x2,y2,px,py)
{
x2-=x1;
y2-=y1;
px-=x1;
py-=y1;
var ccw=px*y2-py*x2;
if(ccw==0.0)
{
ccw=px*x2+py*y2;
if(ccw>0.0)
{
px-=x2;
py-=y2;
ccw=px*x2+py*y2;
if(ccw<0.0)
{
ccw=0.0;
}
}
}
return(ccw<0.0)?-1:((ccw>0.0)?1:0);
},
animateChanges:function(graph,changes)
{
var self=graph;
var maxStep=10;
var step=0;
var animate=function()
{
var isRequired=false;
for(var i=0;i<changes.length;i++)
{
var change=changes[i];
if(change.constructor==mxGeometryChange||change.constructor==mxTerminalChange||change.constructor==mxValueChange||change.constructor==mxChildChange||change.constructor==mxStyleChange)
{
var state=self.getView().getState(change.cell||change.child,false);
if(state!=null)
{
isRequired=true;
if(change.constructor!=mxGeometryChange||self.model.isEdge(change.cell))
{
mxUtils.setOpacity(state.shape.node,100*step/maxStep);
}
else
{
var scale=graph.getView().scale;
var dx=(change.geometry.x-change.previous.x)*scale;
var dy=(change.geometry.y-change.previous.y)*scale;
var sx=(change.geometry.width-change.previous.width)*scale;
var sy=(change.geometry.height-change.previous.height)*scale;
if(step==0)
{
state.x-=dx;
state.y-=dy;
state.width-=sx;
state.height-=sy;
}
else
{
state.x+=dx/maxStep;
state.y+=dy/maxStep;
state.width+=sx/maxStep;
state.height+=sy/maxStep;
}
self.cellRenderer.redraw(state);
mxUtils.cascadeOpacity(graph,change.cell,100*step/maxStep);
}
}
}
}
if(step<maxStep&&isRequired)
{
step++;
window.setTimeout(animate, delay);
}
}
var delay=30;
animate();
},
cascadeOpacity:function(graph,cell,opacity)
{
var childCount=graph.model.getChildCount(cell);
for(var i=0;i<childCount;i++)
{
var child=graph.model.getChildAt(cell,i);
var childState=graph.getView().getState(child);
if(childState!=null)
{
mxUtils.setOpacity(childState.shape.node,opacity);
mxUtils.cascadeOpacity(graph,child,opacity);
}
}
var edges=graph.model.getEdges(cell);
if(edges!=null)
{
for(var i=0;i<edges.length;i++)
{
var edgeState=graph.getView().getState(edges[i]);
if(edgeState!=null)
{
mxUtils.setOpacity(edgeState.shape.node,opacity);
}
}
}
},
morph:function(graph,cells,dx,dy,step,delay)
{
step=step||30;
delay=delay||30;
var current=0;
var f=function()
{
var model=graph.getModel();
current=Math.min(100,current+step);
for(var i=0;i<cells.length;i++)
{
if(!model.isEdge(!cells[i]))
{
var state=graph.getCellBounds(cells[i]);
state.x+=step*dx/100;
state.y+=step*dy/100;
graph.cellRenderer.redraw(state);
}
}
if(current<100)
{
window.setTimeout(f, delay);
}
else
{
graph.move(cells,dx,dy);
}
};
window.setTimeout(f, delay);
},
fadeIn:function(node,to,step,delay,isEnabled)
{
to=(to!=null)?to:100;
step=step||40;
delay=delay||30;
var opacity=0;
mxUtils.setOpacity(node,opacity);
node.style.visibility='visible';
if(isEnabled||isEnabled==null)
{
var f=function()
{
opacity=Math.min(opacity+step,to);
mxUtils.setOpacity(node,opacity);
if(opacity<to)
{
window.setTimeout(f, delay);
}
};
window.setTimeout(f, delay);
}
else
{
mxUtils.setOpacity(node,to);
}
},
fadeOut:function(node,from,remove,step,delay,isEnabled)
{
step=step||40;
delay=delay||30;
var opacity=from||100;
mxUtils.setOpacity(node,opacity);
if(isEnabled||isEnabled==null)
{
var f=function()
{
opacity=Math.max(opacity-step,0);
mxUtils.setOpacity(node,opacity);
if(opacity>0)
{
window.setTimeout(f, delay);
}
else
{
node.style.visibility='hidden';
if(remove&&node.parentNode)
{
node.parentNode.removeChild(node);
}
}
};
window.setTimeout(f, delay);
}
else
{
node.style.visibility='hidden';
if(remove&&node.parentNode)
{
node.parentNode.removeChild(node);
}
}
},
setOpacity:function(node,value)
{
if(mxUtils.isVml(node))
{
if(value>=100)
{
node.style.filter=null;
}
else
{
node.style.filter="alpha(opacity="+(value/5)+")";
}
}
else if(mxClient.IS_IE)
{
if(value>=100)
{
node.style.filter=null;
}
else
{
node.style.filter="alpha(opacity="+value+")";
}
}
else
{
node.style.opacity=(value/100);
}
},
createImage:function(src)
{
var imgName=src.toUpperCase()
var imageNode=null;
if(imgName.substring(imgName.length-3,imgName.length).toUpperCase()=="PNG"&&mxClient.IS_IE&&!mxClient.IS_IE7)
{
imageNode=document.createElement('DIV');
imageNode.style.filter='progid:DXImageTransform.Microsoft.AlphaImageLoader (src=\''+src+'\', sizingMethod=\'scale\')';
}
else
{
imageNode=document.createElement('image');
imageNode.setAttribute('src',src);
}
return imageNode;
},
getStylename:function(style)
{
if(style!=null)
{
var pairs=style.split(';');
var stylename=pairs[0];
if(stylename.indexOf('=')<0)
{
return stylename;
}
}
return '';
},
getStylenames:function(style)
{
var result=new Array();
if(style!=null)
{
var pairs=style.split(';');
for(var i=0;i<pairs.length;i++)
{
if(pairs[i].indexOf('=')<0)
{
result.push(pairs[i]);
}
}
}
return result;
},
indexOfStylename:function(style,stylename)
{
if(style!=null&&stylename!=null)
{
var tokens=style.split(';');
var pos=0;
for(var i=0;i<tokens.length;i++)
{
if(tokens[i]==stylename)
{
return pos;
}
pos+=tokens[i].length+1;
}
}
return-1;
},
addStylename:function(style,stylename)
{
if(mxUtils.indexOfStylename(style,stylename)<0)
{
if(style==null)
{
style="";
}
else if(style.length>0&&style.charAt(style.length-1)!=';')
{
style+=';';
}
style+=stylename;
}
return style;
},
removeStylename:function(style,stylename)
{
var result=new Array();
if(style!=null)
{
var tokens=style.split(';');
for(var i=0;i<tokens.length;i++)
{
if(tokens[i]!=stylename)
{
result.push(tokens[i]);
}
}
}
return result.join(';');
},
removeAllStylenames:function(style)
{
var result=new Array();
if(style!=null)
{
var tokens=style.split(';');
for(var i=0;i<tokens.length;i++)
{
if(tokens[i].indexOf('=')>=0)
{
result.push(tokens[i]);
}
}
}
return result.join(';');
},
setCellStyles:function(model,cells,key,value)
{
if(cells!=null&&cells.length>0)
{
model.beginUpdate();
try
{
for(var i=0;i<cells.length;i++)
{
if(cells[i]!=null)
{
var style=mxUtils.setStyle(model.getStyle(cells[i]),key,value);
model.setStyle(cells[i],style);
}
}
}
finally
{
model.endUpdate();
}
}
},
setStyle:function(style,key,value)
{
var isValue=value!=null&&(typeof(value.length)=='undefined'||value.length>0);
if(style==null||style.length==0)
{
if(isValue)
{
style=key+'='+value;
}
}
else
{
var index=style.indexOf(key+'=');
if(index<0)
{
if(isValue)
{
var sep=(style.charAt(style.length-1)==';')?'':';';
style=style+sep+key+'='+value;
}
}
else
{
var tmp=(isValue)?key+'='+value:'';
var cont=style.indexOf(';',index);
style=style.substring(0,index)+tmp+((cont>=0)?style.substring(cont):'');
}
}
return style;
},
setCellStyleFlags:function(model,cells,key,flag,value)
{
if(cells!=null&&cells.length>0)
{
model.beginUpdate();
try
{
for(var i=0;i<cells.length;i++)
{
if(cells[i]!=null)
{
var style=mxUtils.setStyleFlag(model.getStyle(cells[i]),key,flag,value);
model.setStyle(cells[i],style);
}
}
}
finally
{
model.endUpdate();
}
}
},
setStyleFlag:function(style,key,flag,value)
{
if(style==null||style.length==0)
{
if(value||value==null)
{
style=key+'='+flag;
}
else
{
style=key+'=0';
}
}
else
{
var index=style.indexOf(key+'=');
if(index<0)
{
var sep=(style.charAt(style.length-1)==';')?'':';';
if(value||value==null)
{
style=style+sep+key+'='+flag;
}
else
{
style=style+sep+key+'=0';
}
}
else
{
var cont=style.indexOf(';',index);
var tmp='';
if(cont<0)
{
tmp=style.substring(index+key.length+1);
}
else
{
tmp=style.substring(index+key.length+1,cont);
}
if(value==null)
{
tmp=parseInt(tmp)^flag;
}
else if(value)
{
tmp=parseInt(tmp)|flag;
}
else
{
tmp=parseInt(tmp)&~flag;
}
style=style.substring(0,index)+key+'='+tmp+((cont>=0)?style.substring(cont):'');
}
}
return style;
},
show:function(graph,doc)
{
if(doc==null)
{
var wnd=window.open();
doc=wnd.document;
}
else
{
doc.open();
}
doc.write('<html xmlns:v="urn:schemas-microsoft-com:vml">');
doc.write('<head>');
var base=document.getElementsByTagName('base');
for(var i=0;i<base.length;i++)
{
doc.write(mxUtils.getOuterHtml(base[i]));
}
var links=document.getElementsByTagName('link');
for(var i=0;i<links.length;i++)
{
doc.write(mxUtils.getOuterHtml(links[i]));
}
var styles=document.getElementsByTagName('style');
for(var i=0;i<styles.length;i++)
{
doc.write(mxUtils.getOuterHtml(styles[i]));
}
doc.write('</head>');
var bounds=graph.getBounds();
var dx=Math.min(bounds.x,0);
var dy=Math.min(bounds.y,0);
if(mxClient.IS_IE)
{
doc.write('<body>');
var tmp=mxUtils.getInnerHtml(graph.container);
doc.write(tmp);
var node=doc.body.getElementsByTagName('DIV')[0];
if(node!=null)
{
node.style.position='absolute';
node.style.left=-dx+'px';
node.style.top=-dy+'px';
}
doc.write('</body>');
doc.write('</html>');
doc.close();
}
else
{
doc.write('</html>');
doc.close();

doc.documentElement.appendChild(doc.createElement('body'));
var bounds=graph.getBounds();
var node=graph.container.firstChild;
while(node!=null)
{
var clone=node.cloneNode(true);
doc.body.appendChild(clone);
node=node.nextSibling;
}
}
mxUtils.removeCursors(doc.documentElement);
if(!mxClient.IS_IE)
{
var bounds=graph.getBounds();
var node=doc.getElementsByTagName('g')[0];
if(node!=null)
{
node.setAttribute('transform','translate('+(-dx)+','+(-dy)+')');
var root=node.ownerSVGElement;
root.setAttribute('width',bounds.width+Math.max(bounds.x,0)+3);
root.setAttribute('height',bounds.height+Math.max(bounds.y,0)+3);
root.style.position='absolute';
root.style.left=dx+'px';
root.style.top=dy+'0px';
}
}
return doc;
},
print:function(graph)
{
var wnd=window.open();
mxUtils.show(graph,wnd.document);
wnd.print();
wnd.close();
},
popup:function(content,isInternalWindow)
{
if(isInternalWindow)
{
var div=document.createElement('div');
div.style.overflow='scroll';
div.style.width='636px';
div.style.height='460px';
var pre=document.createElement('pre');
pre.innerHTML=mxUtils.htmlEntities(content,false).replace(/\n/g,'<br>').replace(/ /g,'&nbsp;');
div.appendChild(pre);
var w=document.body.clientWidth;
var h=(document.body.clientHeight||document.documentElement.clientHeight);
var wnd=new mxWindow('Popup Window',div,w/2-320,h/2-240,640,480,false,true);
wnd.setClosable(true);
wnd.setVisible(true);
}
else
{
if(mxClient.IS_NS)
{
var wnd=window.open();
wnd.document.write('<pre>'+mxUtils.htmlEntities(content)+'</pre');
wnd.document.close();
}
else
{
var wnd=window.open();
var pre=wnd.document.createElement('pre');
pre.innerHTML=mxUtils.htmlEntities(content,false).replace(/\n/g,'<br>').replace(/ /g,'&nbsp;');
wnd.document.body.appendChild(pre);
}
}
},
alert:function(message)
{
alert(message);
},
prompt:function(message,defaultValue)
{
return prompt(message,defaultValue);
},
confirm:function(message)
{
return confirm(message);
},
error:function(message,width,close,icon)
{
var div=document.createElement('div');
div.style.padding='20px';
var img=document.createElement('img');
img.setAttribute('src',icon||mxUtils.errorImage);
img.setAttribute('valign','bottom');
img.style.verticalAlign='middle';
div.appendChild(img);
div.appendChild(document.createTextNode('\u00a0'));
div.appendChild(document.createTextNode('\u00a0'));
div.appendChild(document.createTextNode('\u00a0'));
mxUtils.write(div,message);
var w=document.body.clientWidth;
var h=(document.body.clientHeight||document.documentElement.clientHeight);
var warn=new mxWindow(mxResources.get(mxUtils.errorResource)||mxUtils.errorResource,div,(w-width)/2,h/4,width,null,false,true);
if(close)
{
mxUtils.br(div);
var tmp=document.createElement('p');
var button=document.createElement('button');
if(mxClient.IS_IE)
{
button.style.cssText='float:right';
}
else
{
button.setAttribute('style','float:right');
}
mxEvent.addListener(button,'click',function(evt)
{
warn.destroy();
});
mxUtils.write(button,mxResources.get(mxUtils.closeResource)||mxUtils.closeResource);
tmp.appendChild(button);
div.appendChild(tmp);
mxUtils.br(div);
warn.setClosable(true);
}
warn.setVisible(true);
return warn;
},
makeDraggable:function(element,graph,funct,dragElement,dx,dy)
{
dx=(dx!=null)?dx:0;
dy=(dy!=null)?dy:mxConstants.TOOLTIP_VERTICAL_OFFSET;
mxEvent.addListener(element,'mousedown',function(evt)
{
if(!mxEvent.isConsumed(evt))
{

var sprite=(dragElement!=null)?dragElement.cloneNode(true):
element.cloneNode(true);
sprite.style.zIndex=3;
sprite.style.position='absolute';
mxUtils.setOpacity(sprite,70);



var initialized=false;
var startX=evt.clientX;
var startY=evt.clientY;

var dragHandler=function(evt)
{
var origin=mxUtils.getScrollOrigin();
sprite.style.left=(evt.clientX+origin.x+dx)+'px';
sprite.style.top=(evt.clientY+origin.y+dy)+'px';
if(!initialized)
{
initialized=true;
document.body.appendChild(sprite);
}
mxEvent.consume(evt);
};
var dropHandler=function(evt)
{
mxEvent.removeListener(document,'mousemove',dragHandler);
mxEvent.removeListener(document,'mouseup',dropHandler);
if(sprite.parentNode!=null)
{
sprite.parentNode.removeChild(sprite);
}
try
{

var pt=mxUtils.convertPoint(graph.container,evt.clientX,evt.clientY);

var tol=2*graph.tolerance;
if(pt.x>=graph.container.scrollLeft&&pt.y>=graph.container.scrollTop&&pt.x<=graph.container.scrollLeft+graph.container.clientWidth&&pt.y<=graph.container.scrollTop+graph.container.clientHeight&&(Math.abs(evt.clientX-startX)>tol||Math.abs(evt.clientY-startY)>tol))
{
var target=graph.getCellAt(pt.x,pt.y);
funct(graph,evt,target);
}
}
finally
{
mxEvent.consume(evt);
}
};
mxEvent.addListener(document,'mousemove',dragHandler);
mxEvent.addListener(document,'mouseup',dropHandler);
mxEvent.consume(evt);
}
});
}
};

var mxConstants=
{
DEFAULT_HOTSPOT:0.5,
MIN_HOTSPOT_SIZE:8,
MAX_HOTSPOT_SIZE:0,
DIALECT_SVG:'svg',
DIALECT_VML:'vml',
DIALECT_MIXEDHTML:'mixedHtml',
DIALECT_PREFERHTML:'preferHtml',
DIALECT_STRICTHTML:'strictHtml',
NS_SVG:'http://www.w3.org/2000/svg',
NS_XHTML:'http://www.w3.org/1999/xhtml',
NS_XLINK:'http://www.w3.org/1999/xlink',
SVG_SHADOWCOLOR:'gray',
SVG_CRISP_EDGES:false,
SVG_SHADOWTRANSFORM:'translate(2 3)',
NODETYPE_ELEMENT:1,
NODETYPE_ATTRIBUTE:2,
NODETYPE_TEXT:3,
NODETYPE_CDATA:4,
NODETYPE_ENTITY_REFERENCE:5,
NODETYPE_ENTITY:6,
NODETYPE_PROCESSING_INSTRUCTION:7,
NODETYPE_COMMENT:8,
NODETYPE_DOCUMENT:9,
NODETYPE_DOCUMENTTYPE:10,
NODETYPE_DOCUMENT_FRAGMENT:11,
NODETYPE_NOTATION:12,
TOOLTIP_VERTICAL_OFFSET:16,
DEFAULT_VALID_COLOR:'#00FF00',
DEFAULT_INVALID_COLOR:'#FF0000',
HIGHLIGHT_STROKEWIDTH:3,
HIGHLIGHT_COLOR:'#00FF00',
CONNECT_TARGET_COLOR:'#0000FF',
INVALID_CONNECT_TARGET_COLOR:'#FF0000',
DROP_TARGET_COLOR:'#0000FF',
VALID_COLOR:'#00FF00',
INVALID_COLOR:'#FF0000',
SELECTION_COLOR:'#00FF00',
SELECTION_STROKEWIDTH:1,
SELECTION_DASHED:true,
OUTLINE_COLOR:'#0099FF',
OUTLINE_STROKEWIDTH:(mxClient.IS_IE)?2:3,
HANDLE_FILLCOLOR:'#00FF00',
HANDLE_STROKECOLOR:'black',
LABEL_HANDLE_FILLCOLOR:'yellow',
CONNECT_HANDLE_FILLCOLOR:'#0000FF',
LOCKED_HANDLE_FILLCOLOR:'#0000FF',
OUTLINE_HANDLE_FILLCOLOR:'#00FFFF',
OUTLINE_HANDLE_STROKECOLOR:'#0033FF',
DEFAULT_FONTFAMILY:'Arial,Helvetica',
DEFAULT_FONTSIZE:11,
DEFAULT_MARKERSIZE:6,
DEFAULT_IMAGESIZE:24,
ENTITY_SEGMENT:30,
ARROW_SPACING:10,
ARROW_WIDTH:30,
ARROW_SIZE:30,
NONE:'none',
STYLE_PERIMETER:'perimeter',
STYLE_OPACITY:'opacity',
STYLE_TEXT_OPACITY:'textOpacity',
STYLE_ROTATION:'rotation',
STYLE_FILLCOLOR:'fillColor',
STYLE_GRADIENTCOLOR:'gradientColor',
STYLE_GRADIENT_DIRECTION:'gradientDirection',
STYLE_STROKECOLOR:'strokeColor',
STYLE_SEPARATORCOLOR:'separatorColor',
STYLE_STROKEWIDTH:'strokeWidth',
STYLE_ALIGN:'align',
STYLE_VERTICAL_ALIGN:'verticalAlign',
STYLE_LABEL_POSITION:'labelPosition',
STYLE_VERTICAL_LABEL_POSITION:'verticalLabelPosition',
STYLE_IMAGE_ALIGN:'imageAlign',
STYLE_IMAGE_VERTICAL_ALIGN:'imageVerticalAlign',
STYLE_IMAGE:'image',
STYLE_IMAGE_WIDTH:'imageWidth',
STYLE_IMAGE_HEIGHT:'imageHeight',
STYLE_NOLABEL:'noLabel',
STYLE_NOEDGESTYLE:'noEdgeStyle',
STYLE_LABEL_BACKGROUNDCOLOR:'labelBackgroundColor',
STYLE_LABEL_BORDERCOLOR:'labelBorderColor',
STYLE_INDICATOR_SHAPE:'indicatorShape',
STYLE_INDICATOR_IMAGE:'indicatorImage',
STYLE_INDICATOR_COLOR:'indicatorColor',
STYLE_INDICATOR_STROKECOLOR:'indicatorStrokeColor',
STYLE_INDICATOR_GRADIENTCOLOR:'indicatorGradientColor',
STYLE_INDICATOR_SPACING:'indicatorSpacing',
STYLE_INDICATOR_WIDTH:'indicatorWidth',
STYLE_INDICATOR_HEIGHT:'indicatorHeight',
STYLE_SHADOW:'shadow',
STYLE_ENDARROW:'endArrow',
STYLE_STARTARROW:'startArrow',
STYLE_ENDSIZE:'endSize',
STYLE_STARTSIZE:'startSize',
STYLE_DASHED:'dashed',
STYLE_ROUNDED:'rounded',
STYLE_SOURCE_PERIMETER_SPACING:'sourcePerimeterSpacing',
STYLE_TARGET_PERIMETER_SPACING:'targetPerimeterSpacing',
STYLE_PERIMETER_SPACING:'perimeterSpacing',
STYLE_SPACING:'spacing',
STYLE_SPACING_TOP:'spacingTop',
STYLE_SPACING_LEFT:'spacingLeft',
STYLE_SPACING_BOTTOM:'spacingBottom',
STYLE_SPACING_RIGHT:'spacingRight',
STYLE_HORIZONTAL:'horizontal',
STYLE_DIRECTION:'direction',
STYLE_ELBOW:'elbow',
STYLE_FONTCOLOR:'fontColor',
STYLE_FONTFAMILY:'fontFamily',
STYLE_FONTSIZE:'fontSize',
STYLE_FONTSTYLE:'fontStyle',
STYLE_SHAPE:'shape',
STYLE_EDGE:'edgeStyle',
STYLE_LOOP:'loopStyle',
STYLE_ROUTING_CENTER_X:'routingCenterX',
STYLE_ROUTING_CENTER_Y:'routingCenterY',
FONT_BOLD:1,
FONT_ITALIC:2,
FONT_UNDERLINE:4,
FONT_SHADOW:8,
SHAPE_RECTANGLE:'rectangle',
SHAPE_ELLIPSE:'ellipse',
SHAPE_DOUBLE_ELLIPSE:'doubleEllipse',
SHAPE_RHOMBUS:'rhombus',
SHAPE_LINE:'line',
SHAPE_IMAGE:'image',
SHAPE_ARROW:'arrow',
SHAPE_LABEL:'label',
SHAPE_CYLINDER:'cylinder',
SHAPE_SWIMLANE:'swimlane',
SHAPE_CONNECTOR:'connector',
SHAPE_ACTOR:'actor',
SHAPE_CLOUD:'cloud',
SHAPE_TRIANGLE:'triangle',
SHAPE_HEXAGON:'hexagon',
ARROW_CLASSIC:'classic',
ARROW_BLOCK:'block',
ARROW_OPEN:'open',
ARROW_OVAL:'oval',
ARROW_DIAMOND:'diamond',
ALIGN_LEFT:'left',
ALIGN_CENTER:'center',
ALIGN_RIGHT:'right',
ALIGN_TOP:'top',
ALIGN_MIDDLE:'middle',
ALIGN_BOTTOM:'bottom',
DIRECTION_NORTH:'north',
DIRECTION_SOUTH:'south',
DIRECTION_EAST:'east',
DIRECTION_WEST:'west',
ELBOW_VERTICAL:'vertical',
ELBOW_HORIZONTAL:'horizontal'
};

{
function mxEventSource(){};
mxEventSource.prototype.eventListeners=null;
mxEventSource.prototype.eventsEnabled=true;
mxEventSource.prototype.addListener=function(name,funct)
{
if(this.eventListeners==null)
{
this.eventListeners=new Array();
}
this.eventListeners.push(name);
this.eventListeners.push(funct);
};
mxEventSource.prototype.removeListener=function(funct)
{
if(this.eventListeners!=null)
{
var i=0;
while(i<this.eventListeners.length)
{
if(this.eventListeners[i+1]==funct)
{
this.eventListeners.splice(i,2);
}
else
{
i+=2;
}
}
}
};
mxEventSource.prototype.dispatchEvent=function(name)
{
if(this.eventListeners!=null&&this.eventsEnabled)
{
var args=null;
for(var i=0;i<this.eventListeners.length;i+=2)
{
var listen=this.eventListeners[i];
if(listen==null||listen==name)
{

if(args==null)
{
args=new Array();
var argCount=arguments.length;
for(var j=1;j<argCount;j++)
{
args.push(arguments[j]);
}
}
this.eventListeners[i+1].apply(this,args);
}
}
}
};
}

var mxEvent=
{
addListener:function()
{
var updateListenerList=function(element,eventName,funct)
{
if(element.mxListenerList==null)
{
element.mxListenerList=new Array();
}
var entry={name:eventName,f:funct};
element.mxListenerList.push(entry);
}
if(mxClient.IS_IE)
{
return function(element,eventName,funct)
{
element.attachEvent("on"+eventName,funct);
updateListenerList(element,eventName,funct);
}
}
else
{
return function(element,eventName,funct)
{
element.addEventListener(eventName,funct,false);
updateListenerList(element,eventName,funct);
}
}
}(),
redirectMouseEvents:function(element,graph,cell,index,transparent)
{


var checkCell=function(evt)
{
if(transparent&&mxClient.IS_IE)
{
var pt=mxUtils.convertPoint(graph.container,evt.clientX,evt.clientY);
var tmp=graph.getCellAt(pt.x,pt.y);
if(cell!=tmp&&(!graph.isSwimlane(cell)||!graph.hitsSwimlaneContent(cell,pt.x,pt.y)))
{
return tmp;
}
}
return cell;
};
mxEvent.addListener(element,'mousedown',function(evt)
{
graph.dispatchGraphEvent('mousedown',evt,checkCell(evt),index);
});
mxEvent.addListener(element,'mousemove',function(evt)
{
graph.dispatchGraphEvent('mousemove',evt,checkCell(evt),index);
});
mxEvent.addListener(element,'mouseup',function(evt)
{
graph.dispatchGraphEvent('mouseup',evt,checkCell(evt),index);
});
},
removeListener:function()
{
var updateListener=function(element,eventName,funct)
{
if(element.mxListenerList!=null)
{
var listenerCount=element.mxListenerList.length;
for(var i=0;i<listenerCount;i++)
{
var entry=element.mxListenerList[i];
if(entry.f==funct)
{
element.mxListenerList.splice(i,1);
break;
}
}
if(element.mxListenerList.length==0)
{
element.mxListenerList=null;
}
}
}
if(mxClient.IS_IE)
{
return function(element,eventName,funct)
{
element.detachEvent("on"+eventName,funct);
updateListener(element,eventName,funct);
}
}
else
{
return function(element,eventName,funct)
{
element.removeEventListener(eventName,funct,false);
updateListener(element,eventName,funct);
}
}
}(),
removeAllListeners:function(element)
{
var list=element.mxListenerList;
if(list!=null)
{
while(list.length>0)
{
var entry=list[0];
mxEvent.removeListener(element,entry.name,entry.f);
}
}
},
addMouseWheelListener:function(funct)
{
if(funct!=null)
{
var wheelHandler=function(evt)
{


if(evt==null)
{
evt=window.event;
}
var delta=0;
if(mxClient.IS_NS&&!mxClient.IS_SF&&!mxClient.IS_GC)
{
delta=-evt.detail/2;
}
else
{
delta=evt.wheelDelta/120;
}
if(delta!=0)
{
funct(evt,delta>0);
}
};
if(mxClient.IS_NS)
{
var eventName=(mxClient.IS_SF||mxClient.IS_GC)?'mousewheel':'DOMMouseScroll';
mxEvent.addListener(window,eventName,wheelHandler);
}
else
{

mxEvent.addListener(document,'mousewheel',wheelHandler);
}
}
},
disableContextMenu:function()
{
if(mxClient.IS_IE)
{
return function(element)
{
mxEvent.addListener(element,'contextmenu',function()
{
return false;
});
}
}
else
{
return function(element)
{
element.setAttribute('oncontextmenu','return false;');
}
}
}(),
getSource:function(evt)
{
return(evt.srcElement!=null)?evt.srcElement:evt.target;
},
isConsumed:function(evt)
{
return evt.isConsumed!=null&&evt.isConsumed;
},
isLeftMouseButton:function(evt)
{
return evt.button==((mxClient.IS_IE)?1:0);
},
isPopupTrigger:function(evt)
{
return evt.button==2;
},
isCloneEvent:function(evt)
{
return(evt!=null)?evt.ctrlKey:false;
},
isGridEnabledEvent:function(evt)
{
return(evt!=null)?!evt.altKey:false;
},
isToggleEvent:function(evt)
{
return evt.ctrlKey;
},
isForceMarqueeEvent:function(evt)
{
return evt.altKey||evt.metaKey;
},
consume:function(evt,preventDefault)
{
if(preventDefault==null||preventDefault)
{
if(evt.preventDefault)
{
evt.stopPropagation();
evt.preventDefault();
}
else
{
evt.cancelBubble=true;
}
}
evt.isConsumed=true;
evt.returnValue=false;
}
};

var mxDatatransfer=
{
sourceFunction:null,
setSourceFunction:function(funct)
{
mxDatatransfer.sourceFunction=funct;
},
consumeSourceFunction:function(graph,evt,cell)
{
if(mxDatatransfer.sourceFunction!=null&&graph.isEnabled())
{
var funct=mxDatatransfer.sourceFunction;
mxDatatransfer.sourceFunction=null;
funct(graph,evt,cell);
}
}
};

{
function mxXmlRequest(url,params,method,async,username,password)
{
this.url=url;
this.params=params;
this.method=method||'POST';
this.async=(async!=null)?async:true;
this.username=username;
this.password=password;
};
mxXmlRequest.prototype.url=null;
mxXmlRequest.prototype.params=null;
mxXmlRequest.prototype.method=null;
mxXmlRequest.prototype.async=null;
mxXmlRequest.prototype.username=null;
mxXmlRequest.prototype.password=null;
mxXmlRequest.prototype.request=null;
mxXmlRequest.prototype.isReady=function()
{
return this.request.readyState==4;
}
mxXmlRequest.prototype.getDocumentElement=function()
{
var doc=this.getXml();
if(doc!=null)
{
return doc.documentElement;
}
return null;
};
mxXmlRequest.prototype.getXml=function()
{
var xml=this.request.responseXML;
if(xml==null||xml.documentElement==null)
{
xml=mxUtils.parseXml(this.request.responseText);
}
return xml;
};
mxXmlRequest.prototype.getText=function()
{
return this.request.responseText;
};
mxXmlRequest.prototype.getStatus=function()
{
return this.request.status;
};
mxXmlRequest.prototype.create=function()
{
if(window.XMLHttpRequest)
{
return function()
{
return new XMLHttpRequest();
};
}
else if(typeof(ActiveXObject)!="undefined")
{
return function()
{
return new ActiveXObject("Microsoft.XMLHTTP");
};
}
}();
mxXmlRequest.prototype.send=function(onload,onerror)
{
this.request=this.create();
if(this.request!=null)
{
var self=this;
this.request.onreadystatechange=function()
{
if(self.isReady())
{
if(onload!=null)
{
onload(self);
}
}
}
this.request.open(this.method,this.url,this.async,this.username,this.password);
this.setRequestHeaders(this.request,this.params);
this.request.send(this.params);
}
};
mxXmlRequest.prototype.setRequestHeaders=function(request,params)
{
if(params!=null)
{
request.setRequestHeader('Content-Type','application/x-www-form-urlencoded');
}
};
mxXmlRequest.prototype.simulate=function(doc,target)
{
doc=doc||document;
var old=null;
if(doc==document)
{
old=window.onbeforeunload;
window.onbeforeunload=null;
}
var form=doc.createElement('form');
form.setAttribute('method',this.method);
form.setAttribute('action',this.url);
if(target!=null)
{
form.setAttribute('target',target);
}
form.style.display='none';
form.style.visibility='hidden';
var pars=(this.params.indexOf('&')>0)?this.params.split('&'):
this.params.split();
for(var i=0;i<pars.length;i++)
{
var pos=pars[i].indexOf('=');
if(pos>0)
{
var name=pars[i].substring(0,pos);
var value=pars[i].substring(pos+1);
var textarea=doc.createElement('textarea');
textarea.setAttribute('name',name);
value=value.replace(/\n/g,'&#xa;');
var content=doc.createTextNode(value);
textarea.appendChild(content);
form.appendChild(textarea);
}
}
doc.body.appendChild(form);
form.submit();
doc.body.removeChild(form);
if(old!=null)
{
window.onbeforeunload=old;
}
};
}

var mxClipboard=
{
STEPSIZE:10,
insertCount:1,
cells:null,
isEmpty:function()
{
return mxClipboard.cells==null;
},
cut:function(graph,cells)
{
cells=mxClipboard.copy(graph,cells);
mxClipboard.insertCount=0;
graph.dispatchEvent('beforeCut',graph,cells);
graph.remove(cells);
graph.dispatchEvent('afterCut',graph,cells);
return cells;
},
copy:function(graph,cells)
{
cells=cells||graph.getSelectionCells();
var result=new Array();
graph.dispatchEvent('beforeCopy',graph,cells);
for(var i=0;i<cells.length;i++)
{
var cell=cells[i];
if(cells[i]!=null&&graph.canExport(cells[i]))
{
result.push(cell);
}
}
mxClipboard.insertCount=1;
mxClipboard.cells=graph.cloneCells(result);
graph.dispatchEvent('afterCopy',graph,mxClipboard.cells,result);
return result;
},
paste:function(graph)
{
if(mxClipboard.cells!=null)
{
graph.dispatchEvent('beforePaste',graph,mxClipboard.cells);
var cells=new Array();
for(var i=0;i<mxClipboard.cells.length;i++)
{
if(graph.canImport(mxClipboard.cells[i]))
{
cells.push(mxClipboard.cells[i]);
}
}
cells=graph.move(cells,mxClipboard.insertCount*mxClipboard.STEPSIZE,mxClipboard.insertCount*mxClipboard.STEPSIZE,true);
graph.dispatchEvent('paste',graph,cells);
mxClipboard.insertCount++;
graph.setSelectionCells(cells);
graph.dispatchEvent('afterPaste',graph,cells);
}
}
};

{
function mxWindow(title,content,x,y,width,height,minimizable,movable,replaceNode,style)
{
if(content!=null)
{
minimizable=(minimizable!=null)?minimizable:true;
this.content=content;
this.init(x,y,width,height,style);
this.installMaximizeHandler();
this.installMinimizeHandler();
this.installCloseHandler();
this.setMinimizable(minimizable);
mxUtils.write(this.title,title||'');
if(movable==null||movable)
{
this.installMoveHandler();
}
if(replaceNode!=null&&replaceNode.parentNode!=null)
{
replaceNode.parentNode.replaceChild(this.div,replaceNode);
}
else
{
document.body.appendChild(this.div);
}
}
};
mxWindow.prototype=new mxEventSource();
mxWindow.prototype.constructor=mxWindow;
mxWindow.prototype.closeImage=mxClient.imageBasePath+'close.gif';
mxWindow.prototype.minimizeImage=mxClient.imageBasePath+'minimize.gif';
mxWindow.prototype.normalizeImage=mxClient.imageBasePath+'normalize.gif';
mxWindow.prototype.maximizeImage=mxClient.imageBasePath+'maximize.gif';
mxWindow.prototype.resizeImage=mxClient.imageBasePath+'resize.gif';
mxWindow.prototype.visible=false;
mxWindow.prototype.content=false;
mxWindow.prototype.minimumSize=new mxRectangle(0,0,50,40);
mxWindow.prototype.content=false;
mxWindow.prototype.destroyOnClose=true;
mxWindow.prototype.init=function(x,y,width,height,style)
{
style=(style!=null)?style:'mxWindow';
this.div=document.createElement('div');
this.div.className=style;
this.div.style.left=x+'px';
this.div.style.top=y+'px';
if(!mxClient.IS_IE&&mxClient.WINDOW_SHADOWS)
{
this.shadow=document.createElement('div');
this.shadow.style.background=mxConstants.SVG_SHADOWCOLOR;
mxUtils.setOpacity(this.shadow,70);
this.shadow.style.position='absolute';
this.shadow.style.display='inline';
}
else if(mxClient.IS_IE&&!mxClient.WINDOW_SHADOWS)
{
this.div.style.filter='';
}
this.table=document.createElement('table');
this.table.className=style;
if(width!=null)
{
if(!mxClient.IS_IE)
{
this.div.style.width=width+'px';
}
this.table.style.width=width+'px';
}
if(height!=null)
{
if(!mxClient.IS_IE)
{
this.div.style.height=height+'px';
}
this.table.style.height=height+'px';
}
var tbody=document.createElement('tbody');
var tr=document.createElement('tr');
this.title=document.createElement('td');
this.title.className=style+'Title';
tr.appendChild(this.title);
tbody.appendChild(tr);
tr=document.createElement('tr');
this.td=document.createElement('td');
this.td.className=style+'Pane';
this.contentWrapper=document.createElement('div');
this.contentWrapper.className=style+'Pane';
this.contentWrapper.style.width='100%';
this.contentWrapper.appendChild(this.content);

if(mxClient.IS_IE||this.content.nodeName.toUpperCase()!='DIV')
{
this.contentWrapper.style.height='100%';
}
this.td.appendChild(this.contentWrapper);
tr.appendChild(this.td);
tbody.appendChild(tr);
this.table.appendChild(tbody);
this.div.appendChild(this.table);
var self=this;
var activator=function(evt)
{
self.activate();
};
mxEvent.addListener(this.title,'mousedown',activator);
mxEvent.addListener(this.table,'mousedown',activator);
if(this.shadow!=null)
{
mxEvent.addListener(this.div,'DOMNodeInserted',function(evt)
{
var node=mxEvent.getSource(evt);
var loadHandler=function(evt)
{
mxEvent.removeListener(node,'load',loadHandler);
self.updateShadow();
};
mxEvent.addListener(node,'load',loadHandler);
self.updateShadow();
});
}
this.hide();
};
mxWindow.prototype.setScrollable=function(scrollable)
{
if(scrollable)
{
this.contentWrapper.style.overflow='auto'
}
else
{
this.contentWrapper.style.overflow='hidden'
}
};
mxWindow.prototype.updateShadow=function()
{
if(this.shadow!=null)
{
this.shadow.style.display=this.div.style.display;
this.shadow.style.left=(parseInt(this.div.style.left)+3)+'px';
this.shadow.style.top=(parseInt(this.div.style.top)+3)+'px';
this.shadow.style.width=this.div.offsetWidth+'px';
this.shadow.style.height=this.div.offsetHeight+'px';
if(this.shadow.parentNode!=this.div.parentNode)
{
this.div.parentNode.appendChild(this.shadow);
}
}
};
mxWindow.prototype.activate=function()
{
if(mxWindow.activeWindow!=this)
{
var style=mxUtils.getCurrentStyle(this.getElement());
var index=(style!=null)?style.zIndex:3;
if(mxWindow.activeWindow)
{
var elt=mxWindow.activeWindow.getElement();
if(elt!=null&&elt.style!=null)
{
elt.style.zIndex=index;
}
}
var oldWindow=mxWindow.activeWindow;
this.getElement().style.zIndex=index+1;
mxWindow.activeWindow=this;
this.dispatchEvent('activate',this,oldWindow);
}
};
mxWindow.prototype.getElement=function()
{
return this.div;
};
mxWindow.prototype.fit=function()
{
mxUtils.fit(this.div);
};
mxWindow.prototype.isSizable=function(sizable)
{
if(this.resize!=null)
{
return this.resize.style.display!='none';
}
return false;
};
mxWindow.prototype.setSizable=function(sizable)
{
if(sizable)
{
if(this.resize==null)
{
this.resize=document.createElement('img');
this.resize.style.position='absolute';
this.resize.style.bottom='2px';
this.resize.style.right='2px';
this.resize.setAttribute('src',mxClient.imageBasePath+'resize.gif');
this.resize.style.cursor='nw-resize';
var self=this;
mxEvent.addListener(this.resize,'mousedown',function(evt)
{
self.activate();
var startX=evt.clientX;
var startY=evt.clientY;
var width=self.div.offsetWidth;
var height=self.div.offsetHeight;

var dragHandler=function(evt)
{
var dx=evt.clientX-startX;
var dy=evt.clientY-startY;
self.setSize(width+dx,height+dy);
self.updateShadow();
self.dispatchEvent('resize',self,evt);
mxEvent.consume(evt);
};
var dropHandler=function(evt)
{
mxEvent.removeListener(document,'mousemove',dragHandler);
mxEvent.removeListener(document,'mouseup',dropHandler);
self.dispatchEvent('resizeend',self,evt);
mxEvent.consume(evt);
};
mxEvent.addListener(document,'mousemove',dragHandler);
mxEvent.addListener(document,'mouseup',dropHandler);
self.dispatchEvent('resizestart',self,evt);
mxEvent.consume(evt);
});
this.div.appendChild(this.resize);
}
else
{
this.resize.style.display='inline';
}
}
else if(this.resize!=null)
{
this.resize.style.display='none';
}
};
mxWindow.prototype.setSize=function(width,height)
{
width=Math.max(this.minimumSize.width,width);
height=Math.max(this.minimumSize.height,height);
if(!mxClient.IS_IE)
{
this.div.style.width=width+'px';
this.div.style.height=height+'px';
}
this.table.style.width=width+'px';
this.table.style.height=height+'px';
if(!mxClient.IS_IE)
{
this.contentWrapper.style.height=(this.div.offsetHeight-this.title.offsetHeight-2)+'px';
}
};
mxWindow.prototype.setMinimizable=function(minimizable)
{
this.minimize.style.display=(minimizable)?'':'none';
};
mxWindow.prototype.installMinimizeHandler=function()
{
this.minimize=document.createElement('img');
this.minimize.setAttribute('src',this.minimizeImage);
this.minimize.setAttribute('align','right');
this.minimize.setAttribute('title','Minimize');
this.minimize.style.cursor='pointer';
this.minimize.style.marginRight='1px';
this.minimize.style.display='none';
this.title.appendChild(this.minimize);
var minimized=false;
var maxDisplay=null;
var height=null;
var self=this;
var funct=function(evt)
{
self.activate();
if(!minimized)
{
minimized=true;
self.minimize.setAttribute('src',self.normalizeImage);
self.minimize.setAttribute('title','Normalize');
self.contentWrapper.style.display='none';
maxDisplay=self.maximize.style.display;
self.maximize.style.display='none';
height=self.table.style.height;
if(!mxClient.IS_IE)
{
self.div.style.height=self.title.offsetHeight+'px';
}
self.table.style.height=self.title.offsetHeight+'px';
if(self.resize!=null)
{
self.resize.style.visibility='hidden';
}
self.updateShadow();
self.dispatchEvent('minimize',self,evt);
}
else
{
minimized=false;
self.minimize.setAttribute('src',self.minimizeImage);
self.minimize.setAttribute('title','Minimize');
self.contentWrapper.style.display='';
self.maximize.style.display=maxDisplay;
if(!mxClient.IS_IE)
{
self.div.style.height=height;
}
self.table.style.height=height;
if(self.resize!=null)
{
self.resize.style.visibility='visible';
}
self.updateShadow();
self.dispatchEvent('normalize',self,evt);
}
mxEvent.consume(evt);
};
mxEvent.addListener(self.minimize,'mousedown',funct);
};
mxWindow.prototype.setMaximizable=function(maximizable)
{
this.maximize.style.display=(maximizable)?'':'none';
};
mxWindow.prototype.installMaximizeHandler=function()
{
this.maximize=document.createElement('img');
this.maximize.setAttribute('src',this.maximizeImage);
this.maximize.setAttribute('align','right');
this.maximize.setAttribute('title','Maximize');
this.maximize.style.cursor='default';
this.maximize.style.marginLeft='1px';
this.maximize.style.cursor='pointer';
this.maximize.style.display='none';
this.title.appendChild(this.maximize);
var maximized=false;
var x=null;
var y=null;
var height=null;
var width=null;
var self=this;
var funct=function(evt)
{
self.activate();
if(self.maximize.style.display!='none')
{
if(!maximized)
{
maximized=true;
self.maximize.setAttribute('src',self.normalizeImage);
self.maximize.setAttribute('title','Normalize');
self.contentWrapper.style.display='';
self.minimize.style.visibility='hidden';
x=parseInt(self.div.style.left);
y=parseInt(self.div.style.top);
height=self.table.style.height;
width=self.table.style.width;
self.div.style.left='0px';
self.div.style.top='0px';
if(!mxClient.IS_IE)
{
self.div.style.height=(document.body.clientHeight-2)+'px';
self.div.style.width=(document.body.clientWidth-2)+'px';
}
self.table.style.width=(document.body.clientWidth-2)+'px';
self.table.style.height=(document.body.clientHeight-2)+'px';
if(self.resize!=null)
{
self.resize.style.visibility='hidden';
}
if(self.shadow!=null)
{
self.shadow.style.display='none';
}
if(!mxClient.IS_IE)
{
var style=mxUtils.getCurrentStyle(self.contentWrapper);
if(style.overflow=='auto'||self.resize!=null)
{
self.contentWrapper.style.height=(self.div.offsetHeight-self.title.offsetHeight-2)+'px';
}
}
self.dispatchEvent('maximize',self,evt);
}
else
{
maximized=false;
self.maximize.setAttribute('src',self.maximizeImage);
self.maximize.setAttribute('title','Maximize');
self.contentWrapper.style.display='';
self.minimize.style.visibility='';
self.div.style.left=x+'px';
self.div.style.top=y+'px';
if(!mxClient.IS_IE)
{
self.div.style.height=height;
self.div.style.width=width;
var style=mxUtils.getCurrentStyle(self.contentWrapper);
if(style.overflow=='auto'||self.resize!=null)
{
self.contentWrapper.style.height=(self.div.offsetHeight-self.title.offsetHeight-2)+'px';
}
}
self.table.style.height=height;
self.table.style.width=width;
if(self.resize!=null)
{
self.resize.style.visibility='visible';
}
self.updateShadow();
self.dispatchEvent('normalize',self,evt);
}
mxEvent.consume(evt);
}
};
mxEvent.addListener(this.maximize,'mousedown',funct);
mxEvent.addListener(this.title,'dblclick',funct);
};
mxWindow.prototype.installMoveHandler=function()
{
this.title.style.cursor='move';
var self=this;
mxEvent.addListener(this.title,'mousedown',function(evt)
{
var startX=evt.clientX;
var startY=evt.clientY;
var x=self.getX();
var y=self.getY();

var dragHandler=function(evt)
{
var dx=evt.clientX-startX;
var dy=evt.clientY-startY;
self.setLocation(x+dx,y+dy);
self.dispatchEvent('move',self,evt);
mxEvent.consume(evt);
};
var dropHandler=function(evt)
{
mxEvent.removeListener(document,'mousemove',dragHandler);
mxEvent.removeListener(document,'mouseup',dropHandler);
self.dispatchEvent('moveend',self,evt);
mxEvent.consume(evt);
};
mxEvent.addListener(document,'mousemove',dragHandler);
mxEvent.addListener(document,'mouseup',dropHandler);
self.dispatchEvent('movestart',self,evt);
mxEvent.consume(evt);
});
};
mxWindow.prototype.setLocation=function(x,y)
{
this.div.style.left=x+'px';
this.div.style.top=y+'px';
this.updateShadow();
};
mxWindow.prototype.getX=function()
{
return parseInt(this.div.style.left);
};
mxWindow.prototype.getY=function()
{
return parseInt(this.div.style.top);
};
mxWindow.prototype.installCloseHandler=function()
{
this.closeImg=document.createElement('img');
this.closeImg.setAttribute('src',this.closeImage);
this.closeImg.setAttribute('align','right');
this.closeImg.setAttribute('title','Close');
this.closeImg.style.marginLeft='2px';
this.closeImg.style.cursor='pointer';
this.closeImg.style.display='none';
this.title.insertBefore(this.closeImg,this.title.firstChild);
var self=this;
mxEvent.addListener(this.closeImg,'mousedown',function(evt)
{
self.dispatchEvent('close',self,evt);
if(self.destroyOnClose)
{
self.destroy();
}
else
{
self.setVisible(false);
}
mxEvent.consume(evt);
});
};
mxWindow.prototype.setImage=function(image)
{
this.image=document.createElement('img');
this.image.setAttribute('src',image);
this.image.setAttribute('align','left');
this.image.style.marginRight='4px';
this.image.style.marginLeft='0px';
this.image.style.marginTop='-2px';
this.title.insertBefore(this.image,this.title.firstChild);
};
mxWindow.prototype.setClosable=function(closable)
{
this.closeImg.style.display=(closable)?'':'none';
};
mxWindow.prototype.isVisible=function()
{
if(this.div!=null)
{
return this.div.style.display!='none';
}
return false;
};
mxWindow.prototype.setVisible=function(visible)
{
if(this.div!=null&&this.isVisible()!=visible)
{
if(visible)
{
this.show();
}
else
{
this.hide();
}
}
this.updateShadow();
};
mxWindow.prototype.show=function()
{
this.div.style.display='';
this.activate();
var style=mxUtils.getCurrentStyle(this.contentWrapper);
if(!mxClient.IS_IE&&(style.overflow=='auto'||this.resize!=null))
{
this.contentWrapper.style.height=(this.div.offsetHeight-this.title.offsetHeight-2)+'px';
}
this.dispatchEvent('show',this);
};
mxWindow.prototype.hide=function()
{
this.div.style.display='none';
this.dispatchEvent('hide',this);
};
mxWindow.prototype.destroy=function()
{
this.dispatchEvent('destroy',this);
if(this.div!=null)
{
mxUtils.release(this.div);
this.div.parentNode.removeChild(this.div);
this.div=null;
}
if(this.shadow!=null)
{
this.shadow.parentNode.removeChild(this.shadow);
this.shadow=null;
}
this.title=null;
this.content=null;
this.contentWrapper=null;
};
}

{
function mxForm(className)
{
this.table=document.createElement('table');
this.table.className=className;
this.body=document.createElement('tbody');
this.table.appendChild(this.body);
};
mxForm.prototype.table=null;
mxForm.prototype.body=false;
mxForm.prototype.addButtons=function(okFunct,cancelFunct)
{
var tr=document.createElement('tr');
var td=document.createElement('td');
tr.appendChild(td);
td=document.createElement('td');
var button=document.createElement('button');
mxUtils.write(button,mxResources.get('ok')||'OK');
td.appendChild(button);
var self=this;
mxEvent.addListener(button,'click',function()
{
okFunct();
});
button=document.createElement('button');
mxUtils.write(button,mxResources.get('cancel')||'Cancel');
td.appendChild(button);
mxEvent.addListener(button,'click',function()
{
cancelFunct();
});
tr.appendChild(td);
this.body.appendChild(tr);
};
mxForm.prototype.addText=function(name,value)
{
var input=document.createElement('input');
input.setAttribute('type','text');
input.value=value;
return this.addField(name,input);
};
mxForm.prototype.addCheckbox=function(name,value)
{
var input=document.createElement('input');
input.setAttribute('type','checkbox');
this.addField(name,input);
if(value)
{
input.checked=true;
}
return input;
};
mxForm.prototype.addTextarea=function(name,value,rows)
{
var input=document.createElement('textarea');
if(mxClient.IS_NS)
{
rows--;
}
input.setAttribute('rows',rows||2);
input.value=value;
return this.addField(name,input);
};
mxForm.prototype.addCombo=function(name,isMultiSelect,size)
{
var select=document.createElement('select');
if(size!=null)
{
select.setAttribute('size',size);
}
if(isMultiSelect)
{
select.setAttribute('multiple','true');
}
return this.addField(name,select);
};
mxForm.prototype.addOption=function(combo,label,value,isSelected)
{
var option=document.createElement('option');
mxUtils.writeln(option,label);
option.setAttribute('value',value);
if(isSelected)
{
option.setAttribute('selected',isSelected);
}
combo.appendChild(option);
};
mxForm.prototype.addField=function(name,input)
{
var tr=document.createElement('tr');
var td=document.createElement('td');
mxUtils.write(td,name);
tr.appendChild(td);
td=document.createElement('td');
td.appendChild(input);
tr.appendChild(td);
this.body.appendChild(tr);
return input;
};
}

{
function mxImage(src,width,height)
{
this.src=src;
this.width=width;
this.height=height;
};
mxImage.prototype.src=null;
mxImage.prototype.width=null;
mxImage.prototype.height=null;
}

{
function mxDivResizer(div)
{
if(div.nodeName.toLowerCase()=='div')
{
this.div=div;
var style=mxUtils.getCurrentStyle(div);
if(style!=null)
{
this.resizeWidth=style.width=='auto';
this.resizeHeight=style.height=='auto';
}
var self=this;
mxEvent.addListener(window,'resize',function(evt)
{
self.resize();
});
this.resize();
}
};
mxDivResizer.prototype.resizeWidth=true;
mxDivResizer.prototype.resizeHeight=true;
mxDivResizer.prototype.resize=function()
{
var w=this.getDocumentWidth();
var h=this.getDocumentHeight();
var l=parseInt(this.div.style.left);
var r=parseInt(this.div.style.right);
var t=parseInt(this.div.style.top);
var b=parseInt(this.div.style.bottom);
if(this.resizeWidth&&l>=0&&r>=0)
{
this.div.style.width=(w-r-l)+'px';
}
if(this.resizeHeight&&t>=0&&b>=0)
{
this.div.style.height=(h-t-b)+'px';
}
};
mxDivResizer.prototype.getDocumentWidth=function()
{
return document.body.clientWidth;
};
mxDivResizer.prototype.getDocumentHeight=function()
{
return document.body.clientHeight;
};
}

{
function mxToolbar(container)
{
this.container=container;
};
mxToolbar.prototype=new mxEventSource();
mxToolbar.prototype.constructor=mxToolbar;
mxToolbar.prototype.container=null;
mxToolbar.prototype.enabled=true;
mxToolbar.prototype.noReset=false;
mxToolbar.prototype.updateDefaultMode=true;
mxToolbar.prototype.addItem=function(title,icon,funct,pressedIcon,style,factoryMethod)
{
var img=document.createElement((icon!=null)?'img':'button');
var initialClassName=style||((factoryMethod!=null)?'mxToolbarMode':'mxToolbarItem');
img.className=initialClassName;
img.setAttribute('src',icon);
if(title!=null)
{
if(icon!=null)
{
img.setAttribute('title',title);
}
else
{
mxUtils.write(img,title);
}
}
this.container.appendChild(img);
if(funct!=null)
{
mxEvent.addListener(img,'click',funct);
}
var self=this;

mxEvent.addListener(img,'mousedown',function(evt)
{
if(pressedIcon!=null)
{
img.setAttribute('src',pressedIcon);
}
else
{
img.style.backgroundColor='gray';
}
if(factoryMethod!=null)
{
if(self.menu==null)
{
self.menu=new mxPopupMenu();
self.menu.init();
}
var last=self.currentImg;
if(self.menu.isMenuShowing())
{
self.menu.hideMenu();
}
if(last!=img)
{
self.currentImg=img;
self.menu.factoryMethod=factoryMethod;
var point=new mxPoint(img.offsetLeft,img.offsetTop+img.offsetHeight);
self.menu.popup(point.x,point.y,null,evt);
if(self.menu.isMenuShowing())
{
img.className=initialClassName+'Selected'
self.menu.hideMenu=function()
{
mxPopupMenu.prototype.hideMenu.apply(this);
img.className=initialClassName;
self.currentImg=null;
};
}
}
}
});
var mouseHandler=function(evt)
{
if(pressedIcon!=null)
{
img.setAttribute('src',icon);
}
else
{
img.style.backgroundColor='';
}
}
mxEvent.addListener(img,'mouseup',mouseHandler);
mxEvent.addListener(img,'mouseout',mouseHandler);
return img;
};
mxToolbar.prototype.addCombo=function(style)
{
var div=document.createElement('div');
div.style.display='inline';
div.className='mxToolbarComboContainer';
var select=document.createElement('select');
select.className=style||'mxToolbarCombo';
div.appendChild(select);
this.container.appendChild(div);
return select;
};
mxToolbar.prototype.addActionCombo=function(title,style)
{
var select=document.createElement('select');
select.className=style||'mxToolbarCombo';
this.addOption(select,title,null);
mxEvent.addListener(select,'change',function(evt)
{
var value=select.options[select.selectedIndex];
select.selectedIndex=0;
if(value.funct!=null)
{
value.funct(evt);
}
});
this.container.appendChild(select);
return select;
};
mxToolbar.prototype.addOption=function(combo,title,value)
{
var option=document.createElement('option');
mxUtils.writeln(option,title);
if(typeof(value)=='function')
{
option.funct=value;
}
else
{
option.setAttribute('value',value);
}
combo.appendChild(option);
return option;
};
mxToolbar.prototype.addSwitchMode=function(title,icon,funct,pressedIcon,style)
{
var img=document.createElement('img');
img.initialClassName=style||'mxToolbarMode';
img.className=img.initialClassName;
img.setAttribute('src',icon);
img.altIcon=pressedIcon;
if(title!=null)
{
img.setAttribute('title',title);
}
var self=this;
mxEvent.addListener(img,'click',function(evt)
{
var tmp=self.selectedMode.altIcon;
if(tmp!=null)
{
self.selectedMode.altIcon=self.selectedMode.getAttribute('src');
self.selectedMode.setAttribute('src',tmp);
}
else
{
self.selectedMode.className=self.selectedMode.initialClassName;
}
if(self.updateDefaultMode)
{
self.defaultMode=img;
}
self.selectedMode=img;
var tmp=img.altIcon;
if(tmp!=null)
{
img.altIcon=img.getAttribute('src');
img.setAttribute('src',tmp);
}
else
{
img.className=img.initialClassName+'Selected';
}
self.dispatchEvent('select',this,null);
funct();
});
this.container.appendChild(img);
if(this.defaultMode==null)
{
this.defaultMode=img;
this.selectedMode=img;
var tmp=img.altIcon;
if(tmp!=null)
{
img.altIcon=img.getAttribute('src');
img.setAttribute('src',tmp);
}
else
{
img.className=img.initialClassName+'Selected';
}
funct();
}
return img;
};
mxToolbar.prototype.addMode=function(title,icon,funct,pressedIcon,style)
{
var img=document.createElement('img');
img.initialClassName=style||'mxToolbarMode';
img.className=img.initialClassName;
img.setAttribute('src',icon);
img.altIcon=pressedIcon;
if(title!=null)
{
img.setAttribute('title',title);
}
if(this.enabled)
{
var self=this;
mxEvent.addListener(img,'click',function(evt)
{
self.selectMode(img,funct);
self.noReset=false;
});
mxEvent.addListener(img,'dblclick',function(evt)
{
self.selectMode(img,funct);
self.noReset=true;
});
if(this.defaultMode==null)
{
this.defaultMode=img;
this.selectedMode=img;
var tmp=img.altIcon;
if(tmp!=null)
{
img.altIcon=img.getAttribute('src');
img.setAttribute('src',tmp);
}
else
{
img.className=img.initialClassName+'Selected';
}
}
}
this.container.appendChild(img);
return img;
};
mxToolbar.prototype.selectMode=function(domNode,funct)
{
if(this.selectedMode!=domNode)
{
var tmp=this.selectedMode.altIcon;
if(tmp!=null)
{
this.selectedMode.altIcon=this.selectedMode.getAttribute('src');
this.selectedMode.setAttribute('src',tmp);
}
else
{
this.selectedMode.className=this.selectedMode.initialClassName;
}
this.selectedMode=domNode;
var tmp=this.selectedMode.altIcon;
if(tmp!=null)
{
this.selectedMode.altIcon=this.selectedMode.getAttribute('src');
this.selectedMode.setAttribute('src',tmp);
}
else
{
this.selectedMode.className=this.selectedMode.initialClassName+'Selected';
}
this.dispatchEvent('select',this,funct);
}
};
mxToolbar.prototype.resetMode=function(forced)
{
if((forced||!this.noReset)&&this.selectedMode!=this.defaultMode)
{


this.selectMode(this.defaultMode,null);
}
};
mxToolbar.prototype.addSeparator=function(icon)
{
return this.addItem(null,icon,null);
};
mxToolbar.prototype.addBreak=function()
{
mxUtils.br(this.container);
};
mxToolbar.prototype.addLine=function()
{
var hr=document.createElement('hr');
hr.style.marginRight='6px';
hr.setAttribute('size','1');
this.container.appendChild(hr);
};
mxToolbar.prototype.destroy=function()
{
mxUtils.release(this.container);
this.container=null;
this.defaultMode=null;
this.selectedMode=null;
if(this.menu!=null)
{
this.menu.destroy();
}
};
}

{
function mxSession(model,urlInit,urlPoll,urlNotify)
{
this.model=model;
this.urlInit=urlInit;
this.urlPoll=urlPoll;
this.urlNotify=urlNotify;
if(model!=null)
{
this.codec=new mxCodec();
this.codec.lookup=function(id)
{
return model.getCell(id);
};
}

var self=this;
model.addListener('notify',function(sender,changes)
{
if(changes!=null&&self.debug||(self.connected&&!self.suspended))
{
self.notify(self.encodeChanges(changes));
}
});
};
mxSession.prototype=new mxEventSource();
mxSession.prototype.constructor=mxSession;
mxSession.prototype.model=null;
mxSession.prototype.urlInit=null;
mxSession.prototype.urlPoll=null;
mxSession.prototype.urlNotify=null;
mxSession.prototype.codec=null;
mxSession.prototype.linefeed='\n';
mxSession.prototype.significantRemoteChanges=true;
mxSession.prototype.sent=0;
mxSession.prototype.received=0;
mxSession.prototype.debug=false;
mxSession.prototype.connected=false;
mxSession.prototype.suspended=false;
mxSession.prototype.polling=false;
mxSession.prototype.start=function()
{
if(this.debug)
{
this.connected=true;
this.dispatchEvent('connect',this);
}
else if(!this.connected)
{
var self=this;
this.get(this.urlInit,function(req)
{
self.connected=true;
self.dispatchEvent('connect',self);
self.poll();
});
}
};
mxSession.prototype.suspend=function()
{
if(this.connected&&!this.suspended)
{
this.suspended=true;
this.dispatchEvent('suspend',this);
}
};
mxSession.prototype.resume=function(type,attr,value)
{
if(this.connected&&this.suspended)
{
this.suspended=false;
this.dispatchEvent('resume',this);
if(!this.polling)
{
this.poll();
}
}
};
mxSession.prototype.stop=function(reason)
{
if(this.connected)
{
this.connected=false;
}
this.dispatchEvent('disconnect',this,reason);
};
mxSession.prototype.poll=function()
{
if(this.connected&&!this.suspended&&this.urlPoll!=null)
{
this.polling=true;
var self=this;
this.get(this.urlPoll,function()
{
self.poll()
});
}
else
{
this.polling=false;
}
};
mxSession.prototype.notify=function(xml,onLoad,onError)
{
if(xml!=null&&xml.length>0)
{
if(this.urlNotify!=null)
{
if(this.debug)
{
mxLog.show();
mxLog.debug('mxSession.notify: '+this.urlNotify+' xml='+xml);
}
else
{
mxUtils.post(this.urlNotify,'xml='+xml,onLoad,onError);
}
}
this.sent+=xml.length;
this.dispatchEvent('notify',this,this.urlNotify,xml);
}
};
mxSession.prototype.get=function(url,onLoad,onError)
{


if(typeof(mxUtils)!='undefined')
{
var self=this;
var onErrorWrapper=function(ex)
{
if(onError!=null)
{
onError(ex);
}
else
{
self.stop(ex);
}
};

var req=mxUtils.get(url,function(req)
{
if(typeof(mxUtils)!='undefined')
{
try
{
if(req.isReady()&&req.getStatus()!=404)
{
self.received+=req.getText().length;
self.dispatchEvent('get',self,url,req);
if(self.isValidResponse(req))
{
if(req.getText().length>0)
{
var node=req.getDocumentElement();
if(node==null)
{
onErrorWrapper('Invalid response: '+req.getText());
}
else
{
self.receive(node);
}
}
if(onLoad!=null)
{
onLoad(req);
}
}
}
else
{
onErrorWrapper('Response not ready');
}
}
catch(ex)
{
onErrorWrapper(ex);
throw ex;
}
}
},

function(req)
{
onErrorWrapper('Transmission error');
});
}
};
mxSession.prototype.isValidResponse=function(req)
{

return req.getText().indexOf('<?php')<0;
};
mxSession.prototype.encodeChanges=function(changes)
{
var xml='';
for(var i=0;i<changes.length;i++)
{


var node=this.codec.encode(changes[i]);
xml+=mxUtils.getXml(node,this.linefeed);
}
return xml;
};
mxSession.prototype.receive=function(node)
{
if(node!=null&&node.nodeType==mxConstants.NODETYPE_ELEMENT)
{
var name=node.nodeName.toLowerCase();
if(name=='state')
{
var tmp=node.firstChild;
while(tmp!=null)
{
this.receive(tmp);
tmp=tmp.nextSibling;
}

var sid=node.getAttribute('namespace');
this.model.prefix=sid+'-';
}
else if(name=='delta')
{
var changes=this.decodeChanges(node);
if(changes.length>0)
{
var edit=this.createUndoableEdit(changes);
this.model.dispatchEvent('undo',this.model,edit);
this.model.dispatchEvent('change',this.model,changes);
this.dispatchEvent('dispatched',this,changes);
}
}
this.dispatchEvent('receive',this,node);
}
};
mxSession.prototype.createUndoableEdit=function(changes)
{
var edit=new mxUndoableEdit(this.model,this.significantRemoteChanges);
edit.changes=changes;
edit.notify=function()
{
edit.source.dispatchEvent('change',edit.source,edit.changes);
edit.source.dispatchEvent('notify',edit.source,edit.changes);
}
return edit;
};
mxSession.prototype.decodeChanges=function(node)
{
this.codec.document=node.ownerDocument;
var changes=new Array();
node=node.firstChild;
while(node!=null)
{
if(node.nodeType==mxConstants.NODETYPE_ELEMENT)
{



var change=null;
if(node.nodeName=='mxRootChange')
{
var codec=new mxCodec(node.ownerDocument);
change=codec.decode(node);
}
else
{
change=this.codec.decode(node);
}
if(change!=null)
{
change.model=this.model;
change.execute();
changes.push(change);
}
}
node=node.nextSibling;
}
return changes;
};
}

{
function mxUndoableEdit(source,significant)
{
this.source=source;
this.changes=new Array();
this.significant=(significant!=null)?significant:true;
};
mxUndoableEdit.prototype.source=null;
mxUndoableEdit.prototype.changes=null;
mxUndoableEdit.prototype.significant=null;
mxUndoableEdit.prototype.undone=false;
mxUndoableEdit.prototype.redone=false;
mxUndoableEdit.prototype.isEmpty=function()
{
return this.changes.length==0;
}
mxUndoableEdit.prototype.isSignificant=function()
{
return this.significant;
};
mxUndoableEdit.prototype.add=function(change)
{
this.changes.push(change);
};
mxUndoableEdit.prototype.notify=function(){};
mxUndoableEdit.prototype.die=function(){};
mxUndoableEdit.prototype.undo=function()
{
if(!this.undone)
{
var count=this.changes.length;
for(var i=count-1;i>=0;i--)
{
var change=this.changes[i];
if(change.execute!=null)
{
change.execute();
}
else if(change.undo!=null)
{
change.undo();
}
}
this.undone=true;
this.redone=false;
}
this.notify();
};
mxUndoableEdit.prototype.redo=function()
{
if(!this.redone)
{
var count=this.changes.length;
for(var i=0;i<count;i++)
{
var change=this.changes[i];
if(change.execute!=null)
{
change.execute();
}
else if(change.redo!=null)
{
change.redo();
}
}
this.undone=false;
this.redone=true;
}
this.notify();
};
}

{
function mxUndoManager(size)
{
this.size=size||100;
this.reset();
};
mxUndoManager.prototype=new mxEventSource();
mxUndoManager.prototype.constructor=mxUndoManager;
mxUndoManager.prototype.size=null;
mxUndoManager.prototype.history=null;
mxUndoManager.prototype.indexOfNextAdd=0;
mxUndoManager.prototype.reset=function()
{
this.history=new Array();
this.indexOfNextAdd=0;
};
mxUndoManager.prototype.canUndo=function()
{
return this.indexOfNextAdd>0;
};
mxUndoManager.prototype.undo=function()
{
while(this.indexOfNextAdd>0)
{
var edit=this.history[--this.indexOfNextAdd];
edit.undo();
if(edit.isSignificant())
{
this.dispatchEvent('undo',this,edit);
break;
}
}
};
mxUndoManager.prototype.canRedo=function()
{
return this.indexOfNextAdd<this.history.length;
};
mxUndoManager.prototype.redo=function()
{
var n=this.history.length;
while(this.indexOfNextAdd<n)
{
var edit=this.history[this.indexOfNextAdd++];
edit.redo();
if(edit.isSignificant())
{
this.dispatchEvent('redo',this,edit);
break;
}
}
};
mxUndoManager.prototype.undoableEditHappened=function(undoableEdit)
{
this.trim();
if(this.size==this.history.length)
{
this.history.shift();
}
this.history.push(undoableEdit);
this.indexOfNextAdd=this.history.length;
this.dispatchEvent('add',this,undoableEdit);
};
mxUndoManager.prototype.trim=function()
{
if(this.history.length>this.indexOfNextAdd)
{
var edits=this.history.splice(this.indexOfNextAdd,this.history.length-this.indexOfNextAdd);
for(var i=0;i<edits.length;i++)
{
edits[i].die();
}
}
};
}

{
function mxPath(format)
{
this.format=format;
this.path=new Array();
this.translate=new mxPoint(0,0);
};
mxPath.prototype.format=null;
mxPath.prototype.translate=null;
mxPath.prototype.path=null;
mxPath.prototype.isVml=function()
{
return this.format=='vml';
};
mxPath.prototype.getPath=function()
{
return this.path.join('');
};
mxPath.prototype.setTranslate=function(x,y)
{
this.translate=new mxPoint(x,y);
};
mxPath.prototype.moveTo=function(x,y)
{
if(this.isVml())
{
this.path.push('m ',Math.floor(this.translate.x+x),' ',Math.floor(this.translate.y+y),' ');
}
else
{
this.path.push('M ',Math.floor(this.translate.x+x),' ',Math.floor(this.translate.y+y),' ');
}
};
mxPath.prototype.lineTo=function(x,y)
{
if(this.isVml())
{
this.path.push('l ',Math.floor(this.translate.x+x),' ',Math.floor(this.translate.y+y),' ');
}
else
{
this.path.push('L ',Math.floor(this.translate.x+x),' ',Math.floor(this.translate.y+y),' ');
}
};
mxPath.prototype.curveTo=function(x1,y1,x2,y2,x,y)
{
if(this.isVml())
{
this.path.push('c ',Math.floor(this.translate.x+x1),' ',Math.floor(this.translate.y+y1),' ',Math.floor(this.translate.x+x2),' ',Math.floor(this.translate.y+y2),' ',Math.floor(this.translate.x+x),' ',Math.floor(this.translate.y+y),' ');
}
else
{
this.path.push('C ',(this.translate.x+x1),' ',(this.translate.y+y1),' ',(this.translate.x+x2),' ',(this.translate.y+y2),' ',(this.translate.x+x),' ',(this.translate.y+y),' ');
}
};
mxPath.prototype.write=function(string)
{
this.path.push(string,' ');
};
mxPath.prototype.end=function()
{
if(this.format=='vml')
{
this.path.push('e');
}
};
mxPath.prototype.close=function()
{
if(this.format=='vml')
{
this.path.push('x e');
}
else
{
this.path.push('Z');
}
};
}

{
function mxPopupMenu(factoryMethod)
{
this.factoryMethod=factoryMethod;
};
mxPopupMenu.prototype.submenuImage=mxClient.imageBasePath+'submenu.gif';
mxPopupMenu.prototype.factoryMethod=true;
mxPopupMenu.prototype.useShiftKey=true;
mxPopupMenu.prototype.useLeftButtonForPopup=false;
mxPopupMenu.prototype.enabled=true;
mxPopupMenu.prototype.itemCount=0;
mxPopupMenu.prototype.init=function()
{
this.table=document.createElement('table');
this.table.className='mxPopupMenu';
this.tbody=document.createElement('tbody');
this.table.appendChild(this.tbody);
this.div=document.createElement('div');
this.div.className='mxPopupMenu';
this.div.style.display='inline';
this.div.appendChild(this.table);
if(!mxClient.IS_IE&&mxClient.MENU_SHADOWS)
{
this.shadow=document.createElement('div');
this.shadow.style.background=mxConstants.SVG_SHADOWCOLOR;
mxUtils.setOpacity(this.shadow,70);
this.shadow.style.position='absolute';
this.shadow.style.display='inline';
}
else if(mxClient.IS_IE&&!mxClient.MENU_SHADOWS)
{
this.div.style.filter='';
}
mxEvent.disableContextMenu(this.div);
};
mxPopupMenu.prototype.isEnabled=function()
{
return this.enabled;
};
mxPopupMenu.prototype.setEnabled=function(enabled)
{
this.enabled=enabled;
};
mxPopupMenu.prototype.isPopupTrigger=function(evt,cell)
{
return mxEvent.isPopupTrigger(evt)||(this.useLeftButtonForPopup&&mxEvent.isLeftMouseButton(evt))||(this.useShiftKey&&evt.shiftKey);
};
mxPopupMenu.prototype.addItem=function(title,image,funct,parent)
{
parent=parent||this;
this.itemCount++;
var tr=document.createElement('tr');
tr.className='mxPopupMenuItem';
var col1=document.createElement('td');
col1.className='mxPopupMenuIcon';
if(image!=null)
{
var img=document.createElement('img');
if(!mxClient.IS_IE)
{
if(this.loading==null)
{
this.loading=0;
}
this.loading++;
var self=this;
var loader=function()
{
mxEvent.removeListener(img,'load',loader);
self.loading--;
if(self.loading==0)
{
self.showShadow();
}
};
mxEvent.addListener(img,'load',loader);
}
img.src=image;
col1.appendChild(img);
}
tr.appendChild(col1);
var col2=document.createElement('td');
col2.className='mxPopupMenuItem';
mxUtils.write(col2,title);
col2.align='left';
tr.appendChild(col2);
var col3=document.createElement('td');
col3.style.width='10px';
col3.style.paddingRight='6px';
tr.appendChild(col3);
if(parent.div==null)
{
this.createSubmenu(parent);
}
parent.tbody.appendChild(tr);
var self=this;
mxEvent.addListener(tr,'mousedown',function(evt)
{
self.eventReceiver=tr;
if(parent.activeRow!=tr&&parent.activeRow!=parent)
{
if(parent.activeRow!=null&&parent.activeRow.div.parentNode!=null)
{
self.hideSubmenu(parent);
}
if(tr.div!=null)
{
self.showSubmenu(parent,tr);
parent.activeRow=tr;
}
}
mxEvent.consume(evt);
});
mxEvent.addListener(tr,'mouseup',function(evt)
{

if(self.eventReceiver==tr)
{
if(parent.activeRow!=tr)
{
self.hideMenu();
}
if(funct!=null)
{
funct(evt);
}
}
self.eventReceiver=null;
mxEvent.consume(evt);
});
mxEvent.addListener(tr,'mousemove',function(evt)
{
if(parent.activeRow!=tr&&parent.activeRow!=parent)
{
if(parent.activeRow!=null&&parent.activeRow.div.parentNode!=null)
{
self.hideSubmenu(parent);
}
}
if(mxClient.IS_IE)
{
tr.style.backgroundColor='#000066';
tr.style.color='white';
}
});
if(mxClient.IS_IE)
{
mxEvent.addListener(tr,'mouseout',function(evt)
{
tr.style.backgroundColor='';
tr.style.color='';
});
}
return tr;
};
mxPopupMenu.prototype.createSubmenu=function(parent)
{
parent.table=document.createElement('table');
parent.table.className='mxPopupMenu';
parent.tbody=document.createElement('tbody');
parent.table.appendChild(parent.tbody);
parent.div=document.createElement('div');
parent.div.className='mxPopupMenu';
parent.div.style.position='absolute';
parent.div.style.display='inline';
parent.div.appendChild(parent.table);
var img=document.createElement('img');
img.setAttribute('src',this.submenuImage);
td=parent.firstChild.nextSibling.nextSibling;
td.appendChild(img);
};
mxPopupMenu.prototype.showSubmenu=function(parent,row)
{
if(row.div!=null)
{
row.div.style.left=(parent.div.offsetLeft+row.offsetLeft+row.offsetWidth-1)+'px';
row.div.style.top=(parent.div.offsetTop+row.offsetTop)+'px';
document.body.appendChild(row.div);
var left=parseInt(row.div.offsetLeft);
var width=parseInt(row.div.offsetWidth);
var b=document.body;
var d=document.documentElement;
var right=(b.scrollLeft||d.scrollLeft)+(b.clientWidth||d.clientWidth);
if(left+width>right)
{
row.div.style.left=(parent.div.offsetLeft-width+((mxClient.IS_IE)?6:-6))+'px';
}
mxUtils.fit(row.div);
}
};
mxPopupMenu.prototype.addSeparator=function(parent)
{
parent=parent||this;
var tr=document.createElement('tr');
var col1=document.createElement('td');
col1.className='mxPopupMenuIcon';
col1.style.padding='0 0 0 0px';
tr.appendChild(col1);
var col2=document.createElement('td');
col2.style.padding='0 0 0 0px';
col2.setAttribute('colSpan','2');
var hr=document.createElement('hr');
hr.setAttribute('size','1');
col2.appendChild(hr);
tr.appendChild(col2);
parent.tbody.appendChild(tr);
};
mxPopupMenu.prototype.popup=function(x,y,cell,evt)
{
if(this.div!=null&&this.tbody!=null&&this.factoryMethod!=null)
{
this.div.style.left=x+'px';
this.div.style.top=y+'px';
while(this.tbody.firstChild!=null)
{
mxUtils.release(this.tbody.firstChild);
this.tbody.removeChild(this.tbody.firstChild);
}
this.itemCount=0;
this.factoryMethod(this,cell,evt);
if(this.itemCount>0)
{
this.showMenu();
}
}
};
mxPopupMenu.prototype.isMenuShowing=function()
{
return this.div.parentNode==document.body;
};
mxPopupMenu.prototype.showMenu=function()
{
document.body.appendChild(this.div);
mxUtils.fit(this.div);
if(this.shadow!=null)
{
if(!this.loading)
{
this.showShadow();
}
}
};
mxPopupMenu.prototype.showShadow=function()
{
if(this.shadow!=null&&this.div.parentNode==document.body)
{
this.shadow.style.left=(parseInt(this.div.style.left)+3)+'px';
this.shadow.style.top=(parseInt(this.div.style.top)+3)+'px';
this.shadow.style.width=this.div.offsetWidth+'px';
this.shadow.style.height=this.div.offsetHeight+'px';
document.body.appendChild(this.shadow);
}
};
mxPopupMenu.prototype.hideMenu=function()
{
if(this.div!=null)
{
if(this.div.parentNode!=null)
{
this.div.parentNode.removeChild(this.div);
}
if(this.shadow!=null)
{
if(this.shadow.parentNode!=null)
{
this.shadow.parentNode.removeChild(this.shadow);
}
}
this.hideSubmenu(this);
}
};
mxPopupMenu.prototype.hideSubmenu=function(parent)
{
if(parent.activeRow!=null)
{
this.hideSubmenu(parent.activeRow);
if(parent.activeRow.div.parentNode!=null)
{
parent.activeRow.div.parentNode.removeChild(parent.activeRow.div);
}
parent.activeRow=null;
}
};
mxPopupMenu.prototype.destroy=function()
{
if(this.div!=null)
{
mxUtils.release(this.div);
if(this.div.parentNode!=null)
{
this.div.parentNode.removeChild(this.div);
}
this.div=null;
}
if(this.shadow!=null)
{
mxUtils.release(this.shadow);
if(this.shadow.parentNode!=null)
{
this.shadow.parentNode.removeChild(this.shadow);
}
this.shadow=null;
}
};
}

{
function mxShape(){};
mxShape.prototype.SVG_STROKE_TOLERANCE=8;
mxShape.prototype.scale=1;
mxShape.prototype.dialect=null;
mxShape.prototype.mixedModeHtml=true;
mxShape.prototype.preferModeHtml=true;
mxShape.prototype.bounds=null;
mxShape.prototype.points=null;
mxShape.prototype.node=null;
mxShape.prototype.label=null;
mxShape.prototype.innerNode=null;
mxShape.prototype.style=null;
mxShape.prototype.startOffset=null;
mxShape.prototype.endOffset=null;
mxShape.prototype.init=function(container)
{
if(this.node==null)
{
this.node=this.create(container);
if(container!=null)
{
container.appendChild(this.node);
}
}
this.redraw();
};
mxShape.prototype.isMixedModeHtml=function()
{
return this.mixedModeHtml&&!this.isRounded&&!this.isShadow&&this.gradient==null;
};
mxShape.prototype.create=function(container)
{
var node=null;
if(this.dialect==mxConstants.DIALECT_SVG)
{
node=this.createSvg();
}
else if(this.dialect==mxConstants.DIALECT_STRICTHTML||(this.preferModeHtml&&this.dialect==mxConstants.DIALECT_PREFERHTML)||(this.isMixedModeHtml()&&this.dialect==mxConstants.DIALECT_MIXEDHTML))
{
node=this.createHtml();
}
else
{
node=this.createVml();
}
return node;
};
mxShape.prototype.createHtml=function()
{
var node=document.createElement('DIV');
this.configureHtmlShape(node);
return node;
};
mxShape.prototype.destroy=function()
{
if(this.node!=null)
{
mxUtils.release(this.node);
if(this.node.parentNode!=null)
{
this.node.parentNode.removeChild(this.node);
}
this.node=null;
}
};
mxShape.prototype.apply=function(state)
{
var style=state.style;
this.style=style;
if(style!=null)
{
this.fill=mxUtils.getValue(style,mxConstants.STYLE_FILLCOLOR,this.fill);
this.gradient=mxUtils.getValue(style,mxConstants.STYLE_GRADIENTCOLOR,this.gradient);
this.gradientDirection=mxUtils.getValue(style,mxConstants.STYLE_GRADIENT_DIRECTION,this.gradientDirection);
this.opacity=mxUtils.getValue(style,mxConstants.STYLE_OPACITY,this.opacity);
this.stroke=mxUtils.getValue(style,mxConstants.STYLE_STROKECOLOR,this.stroke);
this.strokewidth=mxUtils.getValue(style,mxConstants.STYLE_STROKEWIDTH,this.strokewidth);
this.isShadow=mxUtils.getValue(style,mxConstants.STYLE_SHADOW,this.isShadow);
this.isDashed=mxUtils.getValue(style,mxConstants.STYLE_DASHED,this.isDashed);
this.spacing=mxUtils.getValue(style,mxConstants.STYLE_SPACING,this.spacing);
this.startSize=mxUtils.getValue(style,mxConstants.STYLE_STARTSIZE,this.startSize);
this.endSize=mxUtils.getValue(style,mxConstants.STYLE_ENDSIZE,this.endSize);
this.isRounded=mxUtils.getValue(style,mxConstants.STYLE_ROUNDED,this.isRounded);
this.startArrow=mxUtils.getValue(style,mxConstants.STYLE_STARTARROW,this.startArrow);
this.endArrow=mxUtils.getValue(style,mxConstants.STYLE_ENDARROW,this.endArrow);
this.rotation=mxUtils.getValue(style,mxConstants.STYLE_ROTATION,this.rotation);
}
};
mxShape.prototype.createSvgGroup=function(shape)
{
var g=document.createElementNS(mxConstants.NS_SVG,'g');
this.innerNode=document.createElementNS(mxConstants.NS_SVG,shape);
this.configureSvgShape(this.innerNode);
this.shadowNode=this.createSvgShadow(this.innerNode);
if(this.shadowNode!=null)
{
g.appendChild(this.shadowNode);
}
g.appendChild(this.innerNode);
return g;
};
mxShape.prototype.createSvgShadow=function(node)
{
if(this.isShadow&&this.fill!=null)
{
var shadow=node.cloneNode(true);
shadow.setAttribute('stroke',mxConstants.SVG_SHADOWCOLOR);
shadow.setAttribute('fill',mxConstants.SVG_SHADOWCOLOR);
shadow.setAttribute('transform',mxConstants.SVG_SHADOWTRANSFORM);
return shadow;
}
return null;
};
mxShape.prototype.configureHtmlShape=function(node)
{
if(mxUtils.isVml(node))
{
this.configureVmlShape(node);
}
else
{
node.style.position='absolute';
node.style.overflow='hidden';
var color=this.stroke;
if(color!=null)
{
node.style.borderColor=color;
if(this.isDashed)
{
node.style.borderStyle='dashed';
}
else if(this.strokewidth>0)
{
node.style.borderStyle='solid';
}
node.style.borderWidth=this.strokewidth+'px';
}
color=this.fill;
if(color!=null)
{
node.style.backgroundColor=color;
}
else
{
node.style.background='url(\''+mxClient.imageBasePath+'transparent.gif\')';
}
if(this.opacity!=null)
{
mxUtils.setOpacity(node,this.opacity);
}
}
};
mxShape.prototype.configureVmlShape=function(node)
{
node.style.position='absolute';
var color=this.stroke;
if(color!=null)
{
node.setAttribute('strokecolor',color);
}
else
{
node.setAttribute('stroked','false');
}
color=this.fill;
if(color!=null)
{
node.setAttribute('fillcolor',color);
if(node.fillNode==null)
{
node.fillNode=document.createElement('v:fill');
node.appendChild(node.fillNode);
}
node.fillNode.setAttribute('color',color);
if(this.gradient!=null)
{
node.fillNode.setAttribute('type','gradient');
node.fillNode.setAttribute('color2',this.gradient);
var angle='180';
if(this.gradientDirection==mxConstants.DIRECTION_EAST)
{
angle='270';
}
else if(this.gradientDirection==mxConstants.DIRECTION_WEST)
{
angle='90';
}
else if(this.gradientDirection==mxConstants.DIRECTION_NORTH)
{
angle='0';
}
node.fillNode.setAttribute('angle',angle);
}
if(this.opacity!=null)
{
node.fillNode.setAttribute('opacity',this.opacity+'%');
if(this.gradient!=null)
{
node.fillNode.setAttribute('o:opacity2',this.opacity+'%');
}
}
}
else
{
node.setAttribute('filled','false');
if(node.fillNode!=null)
{
mxUtils.release(node.fillNode);
node.removeChild(node.fillNode);
node.fillNode=null;
}
}
if((this.isDashed||this.opacity!=null)&&this.strokeNode==null)
{
this.strokeNode=document.createElement('v:stroke');
node.appendChild(this.strokeNode);
}
if(this.strokeNode!=null)
{
if(this.isDashed)
{
this.strokeNode.setAttribute('dashstyle','2 2');
}
else
{
this.strokeNode.setAttribute('dashstyle','solid');
}
if(this.opacity!=null)
{
this.strokeNode.setAttribute('opacity',this.opacity+'%');
}
}
if(this.isShadow&&this.fill!=null)
{
if(this.shadowNode==null)
{
this.shadowNode=document.createElement('v:shadow');
this.shadowNode.setAttribute('on','true');
node.appendChild(this.shadowNode);
}
}
if(node.nodeName=='v:rect'&&this.isRounded)
{
node.setAttribute('arcsize','15%');
}
}
mxShape.prototype.configureSvgShape=function(node)
{
var color=this.stroke;
if(color!=null)
{
node.setAttribute('stroke',color);
}
else
{
node.setAttribute('stroke','none');
}
color=this.fill;
if(color!=null)
{
if(this.gradient!=null)
{
var id=this.getGradientId(color,this.gradient,this.opacity);
if(this.gradientNode!=null&&this.gradientNode.getAttribute('id')!=id)
{
this.gradientNode=null;
node.setAttribute('fill','');
}
if(this.gradientNode==null)
{
this.gradientNode=this.createSvgGradient(id,color,this.gradient,this.opacity);
node.setAttribute('fill','url(#'+id+')');
}
}
else
{

this.gradientNode=null;
node.setAttribute('fill',color);
}
}
else
{
node.setAttribute('fill','none');
}
if(this.isDashed)
{
node.setAttribute('stroke-dasharray','3, 3');
}
if(this.opacity!=null)
{
node.setAttribute('fill-opacity',this.opacity/100);
node.setAttribute('stroke-opacity',this.opacity/100);
}
};
mxShape.prototype.getGradientId=function(start,end,opacity)
{
var op=(opacity!=null)?opacity:100;
var dir=null;
if(this.gradientDirection==null||this.gradientDirection==mxConstants.DIRECTION_SOUTH)
{
dir='south';
}
else if(this.gradientDirection==mxConstants.DIRECTION_EAST)
{
dir='east';
}
else if(this.gradientDirection==mxConstants.DIRECTION_NORTH)
{
dir='north';
}
else if(this.gradientDirection==mxConstants.DIRECTION_WEST)
{
dir='west';
}
return 'mxGradient-'+start+'-'+end+'-'+op+'-'+dir;
};
mxShape.prototype.createSvgGradient=function(id,start,end,opacity)
{
var op=(opacity!=null)?opacity:100;
var gradient=document.getElementById(id);
if(gradient==null)
{
var gradient=document.createElementNS(mxConstants.NS_SVG,'linearGradient');
gradient.setAttribute('id',id);
gradient.setAttribute('x1','0%');
gradient.setAttribute('y1','0%');
gradient.setAttribute('x2','0%');
gradient.setAttribute('y2','0%');
if(this.gradientDirection==null||this.gradientDirection==mxConstants.DIRECTION_SOUTH)
{
gradient.setAttribute('y2','100%');
}
else if(this.gradientDirection==mxConstants.DIRECTION_EAST)
{
gradient.setAttribute('x2','100%');
}
else if(this.gradientDirection==mxConstants.DIRECTION_NORTH)
{
gradient.setAttribute('y1','100%');
}
else if(this.gradientDirection==mxConstants.DIRECTION_WEST)
{
gradient.setAttribute('x1','100%');
}
var stop=document.createElementNS(mxConstants.NS_SVG,'stop');
stop.setAttribute('offset','0%');
stop.setAttribute('style','stop-color:'+start+';stop-opacity:'+(op/100));
gradient.appendChild(stop);
stop=document.createElementNS(mxConstants.NS_SVG,'stop');
stop.setAttribute('offset','100%');
stop.setAttribute('style','stop-color:'+end+';stop-opacity:'+(op/100));
gradient.appendChild(stop);


var svg=document.getElementsByTagName('svg')[0];
svg.appendChild(gradient);
}
return gradient;
};
mxShape.prototype.createPoints=function(moveCmd,lineCmd,curveCmd,isRelative)
{
var offsetX=(isRelative)?this.bounds.x:0;
var offsetY=(isRelative)?this.bounds.y:0;
var size=20*this.scale;
var points=moveCmd+' '+Math.floor(this.points[0].x-offsetX)+' '+Math.floor(this.points[0].y-offsetY)+' ';
for(var i=1;i<this.points.length;i++)
{
var pt=this.points[i];
var p0=this.points[i-1];
if(i==1&&this.startOffset!=null)
{
p0=p0.clone();
p0.x+=this.startOffset.x;
p0.y+=this.startOffset.y;
}
else if(i==this.points.length-1&&this.endOffset!=null)
{
pt=pt.clone();
pt.x+=this.endOffset.x;
pt.y+=this.endOffset.y;
}
var dx=p0.x-pt.x;
var dy=p0.y-pt.y;
if((this.isRounded&&i<this.points.length-1)&&(dx!=0||dy!=0)&&this.scale>0.3)
{



var dist=Math.sqrt(dx*dx+dy*dy);
var nx1=dx*Math.min(size,dist/2)/dist;
var ny1=dy*Math.min(size,dist/2)/dist;
points+=lineCmd+' '+Math.floor(pt.x+nx1-offsetX)+' '+Math.floor(pt.y+ny1-offsetY)+' ';



var pe=this.points[i+1];
dx=pe.x-pt.x;
dy=pe.y-pt.y;
dist=Math.max(1,Math.sqrt(dx*dx+dy*dy));
var nx2=dx*Math.min(size,dist/2)/dist;
var ny2=dy*Math.min(size,dist/2)/dist;
points+=curveCmd+' '+Math.floor(pt.x-offsetX)+' '+Math.floor(pt.y-offsetY)+' '+Math.floor(pt.x-offsetX)+','+Math.floor(pt.y-offsetY)+' '+Math.floor(pt.x+nx2-offsetX)+' '+Math.floor(pt.y+ny2-offsetY)+' ';
}
else
{
points+=lineCmd+' '+Math.floor(pt.x-offsetX)+' '+Math.floor(pt.y-offsetY)+' ';
}
}
return points;
};
mxShape.prototype.updateHtmlShape=function(node)
{
if(node!=null)
{
if(mxUtils.isVml(node))
{
this.updateVmlShape(node);
}
else
{
node.style.borderWidth=Math.max(1,Math.floor(this.strokewidth*this.scale))+'px';
if(this.bounds!=null)
{
node.style.left=Math.floor(this.bounds.x)+'px';
node.style.top=Math.floor(this.bounds.y)+'px';
node.style.width=Math.floor(this.bounds.width)+'px';
node.style.height=Math.floor(this.bounds.height)+'px';
}
}
if(this.points!=null&&this.bounds!=null&&!mxUtils.isVml(node))
{
if(this.divContainer==null)
{
this.divContainer=document.createElement('div');
node.appendChild(this.divContainer);
}
node.style.borderStyle='none';
while(this.divContainer.firstChild!=null)
{
mxUtils.release(this.divContainer.firstChild);
this.divContainer.removeChild(this.divContainer.firstChild);
}
if(this.points.length==2)
{
var p0=this.points[0];
var pe=this.points[1];
var dx=pe.x-p0.x;
var dy=pe.y-p0.y;
if(dx==0||dy==0)
{
node.style.borderStyle='solid';
}
else
{
node.style.width=Math.floor(this.bounds.width+1)+'px';
node.style.height=Math.floor(this.bounds.height+1)+'px';
var length=Math.sqrt(dx*dx+dy*dy);
var dotCount=1+(length/(20*this.scale));
var nx=dx/dotCount;
var ny=dy/dotCount;
var x=p0.x-this.bounds.x;
var y=p0.y-this.bounds.y;
for(var i=0;i<dotCount;i++)
{
var tmp=document.createElement('DIV');
tmp.style.position='absolute';
tmp.style.overflow='hidden';
tmp.style.left=Math.floor(x)+'px';
tmp.style.top=Math.floor(y)+'px';
tmp.style.width=Math.max(1,2*this.scale)+'px';
tmp.style.height=Math.max(1,2*this.scale)+'px';
tmp.style.backgroundColor=this.stroke;
this.divContainer.appendChild(tmp);
x+=nx;
y+=ny;
}
}
}
else if(this.points.length==3)
{
var mid=this.points[1];
var n='0';
var s='1';
var w='0';
var e='1';
if(mid.x==this.bounds.x)
{
e='0';
w='1';
}
if(mid.y==this.bounds.y)
{
n='1';
s='0';
}
node.style.borderStyle='solid';
node.style.borderWidth=n+' '+e+' '+s+' '+w+'px';
}
else
{
node.style.width=Math.floor(this.bounds.width+1)+'px';
node.style.height=Math.floor(this.bounds.height+1)+'px';
var last=this.points[0];
for(var i=1;i<this.points.length;i++)
{
var next=this.points[i];
var tmp=document.createElement('DIV');
tmp.style.position='absolute';
tmp.style.overflow='hidden';
tmp.style.borderColor=this.stroke;
tmp.style.borderStyle='solid';
tmp.style.borderWidth='1 0 0 1px';
var x=Math.min(next.x,last.x)-this.bounds.x;
var y=Math.min(next.y,last.y)-this.bounds.y;
var w=Math.max(1,Math.abs(next.x-last.x));
var h=Math.max(1,Math.abs(next.y-last.y));
tmp.style.left=x+'px';
tmp.style.top=y+'px';
tmp.style.width=w+'px';
tmp.style.height=h+'px';
this.divContainer.appendChild(tmp);
last=next;
}
}
}
}
};
mxShape.prototype.updateVmlShape=function(node)
{
node.setAttribute('strokeweight',this.strokewidth*this.scale);
if(this.bounds!=null)
{
node.style.left=Math.floor(this.bounds.x)+'px';
node.style.top=Math.floor(this.bounds.y)+'px';
node.style.width=Math.floor(this.bounds.width)+'px';
node.style.height=Math.floor(this.bounds.height)+'px';
if(this.points==null)
{
if(this.rotation!=null&&this.rotation!=0)
{
node.style.rotation=this.rotation;
}
else
{
node.style.rotation=null;
}
}
}
if(this.points!=null)
{
if(node.nodeName=='polyline'&&node.points!=null)
{
var points='';
for(var i=0;i<this.points.length;i++)
{
points+=this.points[i].x+','+this.points[i].y+' ';
}
node.points.value=points;
node.style.left=null;
node.style.top=null;
node.style.width=null;
node.style.height=null;
}
else if(this.bounds!=null)
{
this.node.setAttribute('coordsize',Math.floor(this.bounds.width)+','+Math.floor(this.bounds.height));
var points=this.createPoints('m','l','c',true);



{



{

}

}
node.setAttribute('path',points+' e');
}
}
};
mxShape.prototype.updateSvgShape=function(node)
{
var strokeWidth=Math.max(1,this.strokewidth*this.scale);
node.setAttribute('stroke-width',strokeWidth);
if(this.points!=null&&this.points[0]!=null)
{
node.setAttribute('d',this.createPoints('M','L','C',false));



{



{
}
}
node.removeAttribute('x');
node.removeAttribute('y');
node.removeAttribute('width');
node.removeAttribute('height');
}
else if(this.bounds!=null)
{
node.setAttribute('x',this.bounds.x);
node.setAttribute('y',this.bounds.y);
var w=this.bounds.width;
var h=this.bounds.height;
node.setAttribute('width',w);
node.setAttribute('height',h);
if(this.isRounded)
{
var r=Math.min(w/5,h/5);
node.setAttribute('rx',r);
node.setAttribute('ry',r);
}
this.updateSvgTransform(node,node==this.shadowNode);
}
};
mxShape.prototype.updateSvgTransform=function(node,shadow)
{
if(this.rotation!=null&&this.rotation!=0)
{
var cx=this.bounds.x+this.bounds.width/2;
var cy=this.bounds.y+this.bounds.height/2;
if(shadow)
{
node.setAttribute('transform','rotate('+this.rotation+','+cx+','+cy+') '+mxConstants.SVG_SHADOWTRANSFORM);
}
else
{
node.setAttribute('transform','rotate('+this.rotation+','+cx+','+cy+')');
}
}
else
{
if(shadow)
{
node.setAttribute('transform',mxConstants.SVG_SHADOWTRANSFORM);
}
else
{
node.removeAttribute('transform');
}
}
};
mxShape.prototype.reconfigure=function()
{
if(this.dialect==mxConstants.DIALECT_SVG)
{
if(this.innerNode!=null)
{
this.configureSvgShape(this.innerNode);
}
else
{
this.configureSvgShape(this.node);
}
}
else if(mxUtils.isVml(this.node))
{
this.configureVmlShape(this.node);
}
else
{
this.configureHtmlShape(this.node);
}
};
mxShape.prototype.redraw=function()
{
if(this.dialect==mxConstants.DIALECT_SVG)
{
this.redrawSvg();
}
else if(mxUtils.isVml(this.node))
{
this.redrawVml();
}
else
{
this.redrawHtml();
}
};
mxShape.prototype.redrawSvg=function()
{
if(this.innerNode!=null)
{
this.updateSvgShape(this.innerNode);
if(this.shadowNode!=null)
{
this.updateSvgShape(this.shadowNode);
}
}
else
{
this.updateSvgShape(this.node);
}
};
mxShape.prototype.redrawVml=function()
{
this.updateVmlShape(this.node);
};
mxShape.prototype.redrawHtml=function()
{
this.updateHtmlShape(this.node);
};
mxShape.prototype.createPath=function(arg)
{
var x=this.bounds.x;
var y=this.bounds.y;
var w=this.bounds.width;
var h=this.bounds.height;
var path=null;
if(this.dialect==mxConstants.DIALECT_SVG)
{
path=new mxPath('svg');
path.setTranslate(x,y);
}
else
{
path=new mxPath('vml');
}
this.redrawPath(path,x,y,w,h,arg);
return path.getPath();
};
mxShape.prototype.redrawPath=function(path,x,y,w,h)
{
};
}

{
function mxActor(bounds,fill,stroke,strokewidth)
{
this.bounds=bounds;
this.fill=fill;
this.stroke=stroke;
this.strokewidth=strokewidth||1;
};
mxActor.prototype=new mxShape();
mxActor.prototype.constructor=mxActor;
mxActor.prototype.mixedModeHtml=false;
mxActor.prototype.preferModeHtml=false;
mxActor.prototype.createVml=function()
{
var node=document.createElement('v:shape');
this.configureVmlShape(node);
return node;
};
mxActor.prototype.redrawVml=function()
{
this.updateVmlShape(this.node);
var w=Math.floor(this.bounds.width);
var h=Math.floor(this.bounds.height);
var s=this.strokewidth*this.scale;
this.node.setAttribute('coordsize',w+','+h);
this.node.setAttribute('strokeweight',s);
var d=this.createPath();
this.node.setAttribute('path',d);
};
mxActor.prototype.createSvg=function()
{
return this.createSvgGroup('path');
};
mxActor.prototype.redrawSvg=function()
{
var strokeWidth=Math.max(1,this.strokewidth*this.scale);
this.innerNode.setAttribute('stroke-width',strokeWidth);
var d=this.createPath();
this.innerNode.setAttribute('d',d);
this.updateSvgTransform(this.innerNode,false);
if(this.shadowNode!=null)
{
this.shadowNode.setAttribute('stroke-width',strokeWidth);
this.shadowNode.setAttribute('d',d);
this.updateSvgTransform(this.shadowNode,true);
}
};
mxActor.prototype.redrawPath=function(path,x,y,w,h)
{
var width=w/3;
path.moveTo(0,h);
path.curveTo(0,3*h/5,0,2*h/5,w/2,2*h/5);
path.curveTo(w/2-width,2*h/5,w/2-width,0,w/2,0);
path.curveTo(w/2+width,0,w/2+width,2*h/5,w/2,2*h/5);
path.curveTo(w,2*h/5,w,3*h/5,w,h);
path.close();
};
}

{
function mxCloud(bounds,fill,stroke,strokewidth)
{
this.bounds=bounds;
this.fill=fill;
this.stroke=stroke;
this.strokewidth=strokewidth||1;
};
mxCloud.prototype=new mxActor();
mxCloud.prototype.constructor=mxActor;
mxCloud.prototype.redrawPath=function(path,x,y,w,h)
{
path.moveTo(0.25*w,0.25*h);
path.curveTo(0.05*w,0.25*h,0,0.5*h,0.16*w,0.55*h);
path.curveTo(0,0.66*h,0.18*w,0.9*h,0.31*w,0.8*h);
path.curveTo(0.4*w,h,0.7*w,h,0.8*w,0.8*h);
path.curveTo(w,0.8*h,w,0.6*h,0.875*w,0.5*h);
path.curveTo(w,0.3*h,0.8*w,0.1*h,0.625*w,0.2*h);
path.curveTo(0.5*w,0.05*h,0.3*w,0.05*h,0.25*w,0.25*h);
path.close();
};
}

{
function mxRectangleShape(bounds,fill,stroke,strokewidth)
{
this.bounds=bounds;
this.fill=fill;
this.stroke=stroke;
this.strokewidth=strokewidth||1;
};
mxRectangleShape.prototype=new mxShape();
mxRectangleShape.prototype.constructor=mxRectangleShape;
mxRectangleShape.prototype.createHtml=function()
{
var node=document.createElement('DIV');
this.configureHtmlShape(node);
return node;
};
mxRectangleShape.prototype.createVml=function()
{
var name=(this.isRounded)?'v:roundrect':'v:rect';
var node=document.createElement(name);
this.configureVmlShape(node);
return node;
};
mxRectangleShape.prototype.createSvg=function()
{
var node=this.createSvgGroup('rect');

if(this.strokewidth*this.scale>=1&&!this.isRounded)
{
this.innerNode.setAttribute('shape-rendering','optimizeSpeed');
}
return node;
};
}

{
function mxEllipse(bounds,fill,stroke,strokewidth)
{
this.bounds=bounds;
this.fill=fill;
this.stroke=stroke;
this.strokewidth=strokewidth||1;
};
mxEllipse.prototype=new mxShape();
mxEllipse.prototype.constructor=mxEllipse;
mxEllipse.prototype.mixedModeHtml=false;
mxEllipse.prototype.preferModeHtml=false;
mxEllipse.prototype.createVml=function()
{


var node=document.createElement('v:arc');
node.setAttribute('startangle','0');
node.setAttribute('endangle','360');
this.configureVmlShape(node);
return node;
};
mxEllipse.prototype.createSvg=function()
{
return this.createSvgGroup('ellipse');
};
mxEllipse.prototype.redrawSvg=function()
{
this.updateSvgNode(this.innerNode);
this.updateSvgNode(this.shadowNode);
};
mxEllipse.prototype.updateSvgNode=function(node)
{
if(node!=null)
{
var strokeWidth=Math.max(1,this.strokewidth*this.scale);
node.setAttribute('stroke-width',strokeWidth);
node.setAttribute('cx',this.bounds.x+this.bounds.width/2);
node.setAttribute('cy',this.bounds.y+this.bounds.height/2);
node.setAttribute('rx',this.bounds.width/2);
node.setAttribute('ry',this.bounds.height/2);
}
};
}

{
function mxDoubleEllipse(bounds,fill,stroke,strokewidth)
{
this.bounds=bounds;
this.fill=fill;
this.stroke=stroke;
this.strokewidth=strokewidth||1;
};
mxDoubleEllipse.prototype=new mxShape();
mxDoubleEllipse.prototype.constructor=mxDoubleEllipse;
mxDoubleEllipse.prototype.mixedModeHtml=false;
mxDoubleEllipse.prototype.preferModeHtml=false;
mxDoubleEllipse.prototype.createVml=function()
{
var node=document.createElement('v:group');
this.background=document.createElement('v:arc');
this.background.setAttribute('startangle','0');
this.background.setAttribute('endangle','360');
this.configureVmlShape(this.background);
node.appendChild(this.background);
this.label=this.background;
this.isShadow=false;
this.fill=null;
this.foreground=document.createElement('v:oval');
this.configureVmlShape(this.foreground);
node.appendChild(this.foreground);
this.stroke=null;
this.configureVmlShape(node);
return node;
};
mxDoubleEllipse.prototype.redrawVml=function()
{
var x=Math.floor(this.bounds.x);
var y=Math.floor(this.bounds.y);
var w=Math.floor(this.bounds.width);
var h=Math.floor(this.bounds.height);
var s=this.strokewidth*this.scale;
this.updateVmlShape(this.node);
this.node.setAttribute('coordsize',w+','+h);
this.updateVmlShape(this.background);
this.background.setAttribute('strokeweight',s);
this.background.style.top='0px';
this.background.style.left='0px';
this.updateVmlShape(this.foreground);
this.foreground.setAttribute('strokeweight',s);
var inset=3+s;
this.foreground.style.top=inset+'px';
this.foreground.style.left=inset+'px';
this.foreground.style.width=Math.max(0,w-2*inset)+'px';
this.foreground.style.height=Math.max(0,h-2*inset)+'px';
};
mxDoubleEllipse.prototype.createSvg=function()
{
var g=this.createSvgGroup('ellipse');
this.foreground=document.createElementNS(mxConstants.NS_SVG,'ellipse');
if(this.stroke!=null)
{
this.foreground.setAttribute('stroke',this.stroke);
}
else
{
this.foreground.setAttribute('stroke','none');
}
this.foreground.setAttribute('fill','none');
g.appendChild(this.foreground);
return g;
};
mxDoubleEllipse.prototype.redrawSvg=function()
{
var s=this.strokewidth*this.scale;
this.updateSvgNode(this.innerNode);
this.updateSvgNode(this.shadowNode);
this.updateSvgNode(this.foreground,3*this.scale+s);
};
mxDoubleEllipse.prototype.updateSvgNode=function(node,inset)
{
inset=(inset!=null)?inset:0;
if(node!=null)
{
var strokeWidth=Math.max(1,this.strokewidth*this.scale);
node.setAttribute('stroke-width',strokeWidth);
node.setAttribute('cx',this.bounds.x+this.bounds.width/2);
node.setAttribute('cy',this.bounds.y+this.bounds.height/2);
node.setAttribute('rx',this.bounds.width/2-inset);
node.setAttribute('ry',this.bounds.height/2-inset);
}
};
}

{
function mxRhombus(bounds,fill,stroke,strokewidth)
{
this.bounds=bounds;
this.fill=fill;
this.stroke=stroke;
this.strokewidth=strokewidth||1;
};
mxRhombus.prototype=new mxShape();
mxRhombus.prototype.constructor=mxRhombus;
mxRhombus.prototype.mixedModeHtml=false;
mxRhombus.prototype.preferModeHtml=false;
mxRhombus.prototype.createHtml=function()
{
var node=null;
if(mxClient.IS_CANVAS)
{
node=document.createElement('CANVAS');
this.configureHtmlShape(node);
node.style.borderStyle='none';
}
else
{
node=document.createElement('DIV');
this.configureHtmlShape(node);
}
return node;
};
mxRhombus.prototype.createVml=function()
{
var node=document.createElement('v:shape');
this.configureVmlShape(node);
return node;
};
mxRhombus.prototype.createSvg=function()
{
return this.createSvgGroup('path');
}



mxRhombus.prototype.redrawVml=function()
{
this.node.setAttribute('strokeweight',this.strokewidth*this.scale);
this.updateVmlShape(this.node);
var x=0;
var y=0;
var w=Math.floor(this.bounds.width);
var h=Math.floor(this.bounds.height);
this.node.setAttribute('coordsize',w+','+h);
var points='m '+Math.floor(x+w/2)+' '+y+' l '+(x+w)+' '+Math.floor(y+h/2)+' l '+Math.floor(x+w/2)+' '+(y+h)+' l '+x+' '+Math.floor(y+h/2);
this.node.setAttribute('path',points+' x e');
};
mxRhombus.prototype.redrawHtml=function()
{
if(this.node.nodeName=='CANVAS')
{
this.redrawCanvas();
}
else
{
this.updateHtmlShape(this.node);
}
};
mxRhombus.prototype.redrawCanvas=function()
{
this.updateHtmlShape(this.node);
var x=0;
var y=0;
var w=this.bounds.width;
var h=this.bounds.height;
this.node.setAttribute('width',w);
this.node.setAttribute('height',h);
if(!this.isRepaintNeeded)
{
var ctx=this.node.getContext('2d');
ctx.clearRect(0,0,w,h);
ctx.beginPath();
ctx.moveTo(x+w/2,y);
ctx.lineTo(x+w,y+h/2);
ctx.lineTo(x+w/2,y+h);
ctx.lineTo(x,y+h/2);
ctx.lineTo(x+w/2,y);
if(this.node.style.backgroundColor!='transparent')
{
ctx.fillStyle=this.node.style.backgroundColor;
ctx.fill();
}
if(this.node.style.borderColor!=null)
{
ctx.strokeStyle=this.node.style.borderColor;
ctx.stroke();
}
this.isRepaintNeeded=false;
}
};
mxRhombus.prototype.redrawSvg=function()
{
this.updateSvgNode(this.innerNode);
if(this.shadowNode!=null)
{
this.updateSvgNode(this.shadowNode);
}
};
mxRhombus.prototype.updateSvgNode=function(node)
{
var strokeWidth=Math.max(1,this.strokewidth*this.scale);
node.setAttribute('stroke-width',strokeWidth);
var x=this.bounds.x;
var y=this.bounds.y;
var w=this.bounds.width;
var h=this.bounds.height;
var d='M '+(x+w/2)+' '+y+' L '+(x+w)+' '+(y+h/2)+' L '+(x+w/2)+' '+(y+h)+' L '+x+' '+(y+h/2)+' Z ';
node.setAttribute('d',d);
this.updateSvgTransform(node,node==this.shadowNode);
};
}

{
function mxPolyline(points,stroke,strokewidth)
{
this.points=points;
this.stroke=stroke||'black';
this.strokewidth=strokewidth||1;
};
mxPolyline.prototype=new mxShape();
mxPolyline.prototype.constructor=mxPolyline;
mxPolyline.prototype.create=function()
{
var node=null;
if(this.dialect==mxConstants.DIALECT_SVG)
{
node=this.createSvg();
}
else if(this.dialect==mxConstants.DIALECT_STRICTHTML||(this.dialect==mxConstants.DIALECT_PREFERHTML&&this.points!=null&&this.points.length>0))
{
node=document.createElement('DIV');
this.configureHtmlShape(node);
node.style.borderStyle='none';
node.style.background='';
}
else
{
node=document.createElement('v:polyline');
this.configureVmlShape(node);
var strokeNode=document.createElement('v:stroke');
if(this.opacity!=null)
{
strokeNode.setAttribute('opacity',this.opacity+'%');
}
node.appendChild(strokeNode);
}
return node;
};
mxPolyline.prototype.createSvg=function()
{
var g=this.createSvgGroup('path');



var color=this.innerNode.getAttribute('stroke');
this.pipe=document.createElementNS(mxConstants.NS_SVG,'path');
this.pipe.setAttribute('stroke',color);
this.pipe.setAttribute('visibility','hidden');
this.pipe.setAttribute('pointer-events','stroke');
g.appendChild(this.pipe);
return g;
};
mxPolyline.prototype.redrawSvg=function()
{
this.updateSvgShape(this.innerNode);
this.pipe.setAttribute('d',this.innerNode.getAttribute('d'));
var strokeWidth=this.strokewidth*this.scale;
if(mxConstants.SVG_CRISP_EDGES&&strokeWidth==Math.floor(strokeWidth)&&!this.isRounded)
{
this.innerNode.setAttribute('shape-rendering','optimizeSpeed');
}
else
{
this.innerNode.setAttribute('shape-rendering','default');
}
this.pipe.setAttribute('stroke-width',strokeWidth+mxShape.prototype.SVG_STROKE_TOLERANCE);
};
}

{
function mxArrow(points,fill,stroke,strokewidth,arrowWidth,spacing,endSize)
{
this.points=points;
this.fill=fill;
this.stroke=stroke;
this.strokewidth=strokewidth||1;
this.arrowWidth=arrowWidth||mxConstants.ARROW_WIDTH;
this.spacing=spacing||mxConstants.ARROW_SPACING;
this.endSize=endSize||mxConstants.ARROW_SIZE;
};
mxArrow.prototype=new mxShape();
mxArrow.prototype.constructor=mxArrow;
mxArrow.prototype.mixedModeHtml=false;
mxArrow.prototype.preferModeHtml=false;
mxArrow.prototype.DEG_PER_RAD=57.2957795;
mxArrow.prototype.createVml=function()
{
var node=document.createElement('v:polyline');
this.configureVmlShape(node);
return node;
};
mxArrow.prototype.redrawVml=function()
{
this.node.setAttribute('strokeweight',this.strokewidth*this.scale);
if(this.points!=null)
{
var spacing=this.spacing*this.scale;
var width=this.arrowWidth*this.scale;
var arrow=this.endSize*this.scale;
var p0=this.points[0];
var pe=this.points[this.points.length-1];
var dx=pe.x-p0.x;
var dy=pe.y-p0.y;
var dist=Math.sqrt(dx*dx+dy*dy);
var length=dist-2*spacing-arrow;
var nx=dx/dist;
var ny=dy/dist;
var basex=length*nx;
var basey=length*ny;
var floorx=width*ny/3;
var floory=-width*nx/3;
var p0x=p0.x-floorx/2+spacing*nx;
var p0y=p0.y-floory/2+spacing*ny;
var p1x=p0x+floorx;
var p1y=p0y+floory;
var p2x=p1x+basex;
var p2y=p1y+basey;
var p3x=p2x+floorx;
var p3y=p2y+floory;
var p5x=p3x-3*floorx;
var p5y=p3y-3*floory;
this.node.points.value=p0x+','+p0y+','+p1x+','+p1y+','+p2x+','+p2y+','+p3x+','+p3y+','+(pe.x-spacing*nx)+','+(pe.y-spacing*ny)+','+p5x+','+p5y+','+(p5x+floorx)+','+(p5y+floory)+','+p0x+','+p0y;
}
};
mxArrow.prototype.createSvg=function()
{
var node=document.createElementNS(mxConstants.NS_SVG,'polygon');
this.configureSvgShape(node);
return node;
};
mxArrow.prototype.redrawSvg=function()
{
if(this.points!=null)
{
var strokeWidth=Math.max(1,this.strokewidth*this.scale);
this.node.setAttribute('stroke-width',strokeWidth);
var p0=this.points[0];
var pe=this.points[this.points.length-1];
var tdx=pe.x-p0.x;
var tdy=pe.y-p0.y;
var dist=Math.sqrt(tdx*tdx+tdy*tdy);
var offset=this.spacing*this.scale;
var h=Math.min(25,Math.max(20,dist/5))*this.scale;
var w=dist-2*offset;
var x=p0.x+offset;
var y=p0.y-h/2;
var dx=h;
var dy=h*0.3;
var right=x+w;
var bottom=y+h;
var points=x+','+(y+dy)+' '+(right-dx)+','+(y+dy)+' '+(right-dx)+','+y+' '+right+','+(y+h/2)+' '+(right-dx)+','+bottom+' '+(right-dx)+','+(bottom-dy)+' '+x+','+(bottom-dy);
this.node.setAttribute('points',points);
var dx=pe.x-p0.x;
var dy=pe.y-p0.y;
var theta=Math.atan(dy/dx)*this.DEG_PER_RAD;
if(dx<0)
{
theta-=180;
}
this.node.setAttribute('transform','rotate('+theta+','+p0.x+','+p0.y+')');
}
};
}

{
function mxText(value,bounds,align,valign,color,family,size,fontStyle,spacing,spacingTop,spacingRight,spacingBottom,spacingLeft,isRotate,background,border,useTableBounds,isAbsolute,isWrapping,isClipping)
{
this.value=value;
this.bounds=bounds;
this.color=color||'black';
this.align=align||0;
this.valign=valign||0;
this.family=family||mxConstants.DEFAULT_FONTFAMILY;
this.size=size||mxConstants.DEFAULT_FONTSIZE;
this.fontStyle=fontStyle||0;
this.spacing=parseInt(spacing||2);
this.spacingTop=this.spacing+parseInt(spacingTop||0);
this.spacingRight=this.spacing+parseInt(spacingRight||0);
this.spacingBottom=this.spacing+parseInt(spacingBottom||0);
this.spacingLeft=this.spacing+parseInt(spacingLeft||0);
this.isRotate=isRotate||false;
this.background=background;
this.border=border;
this.useTableBounds=(useTableBounds!=null)?useTableBounds:true;
this.isAbsolute=(isAbsolute!=null)?isAbsolute:false;
this.isWrapping=(isWrapping!=null)?isWrapping:false;
this.isClipping=(isClipping!=null)?isClipping:false;
};
mxText.prototype=new mxShape();
mxText.prototype.constructor=mxText;
mxText.prototype.ENABLE_FOREIGNOBJECT=false;
mxText.prototype.isStyleSet=function(style)
{
return(this.fontStyle&style)==style;
}
mxText.prototype.create=function(container)
{
var node=null;
if(this.dialect==mxConstants.DIALECT_SVG&&(!mxClient.IS_FF3||!this.ENABLE_FOREIGNOBJECT))
{
node=this.createSvg();
}
else if(this.dialect==mxConstants.DIALECT_STRICTHTML||this.dialect==mxConstants.DIALECT_PREFERHTML||!mxUtils.isVml(container))
{
if(this.dialect==mxConstants.DIALECT_SVG)
{
node=this.createForeignObject();
}
else
{
container.style.overflow='visible';
node=this.createHtml();
}
}
else
{
node=this.createVml();
}
return node;
};
mxText.prototype.createForeignObject=function()
{
var node=document.createElementNS(mxConstants.NS_SVG,'foreignObject');
node.style.cursor='default';
return node;
};
mxText.prototype.createHtml=function()
{
var node=document.createElement('DIV');
node.style.cursor='default';
return node;
};
mxText.prototype.createVml=function()
{
var node=document.createElement('v:textbox');
node.inset='0px,0px,0px,0px';
return node;
};
mxText.prototype.redrawHtml=function()
{
this.redrawVml();
};
mxText.prototype.redrawVml=function()
{
if(this.value!=null)
{
var scale=(mxClient.IS_IE)?1:this.scale;
var table=document.createElement('table');
table.style.borderCollapse='collapse';
var tbody=document.createElement('tbody');
var row=document.createElement('tr');
var td=document.createElement('td');
this.node.style.overflow=(this.isClipping)?'hidden':'visible';
if(!mxUtils.isVml(this.node))
{
this.node.style.marginLeft='0px';
this.node.style.marginTop='0px';
}
else
{
this.node.inset='0px,0px,0px,0px';
}
if(this.isAbsolute||(this.bounds.width==0&&this.bounds.height==0))
{
if(mxUtils.isVml(this.node))
{
var x0=parseInt(this.node.parentNode.style.left);
var y0=parseInt(this.node.parentNode.style.top);
this.node.inset=(this.bounds.x-x0)+'px,'+(this.bounds.y-y0)+'px,0px,0px';
}
else
{
this.node.style.position='absolute';
this.node.style.left=this.bounds.x+'px';
this.node.style.top=this.bounds.y+'px';
if(mxUtils.isVml(this.node.parentNode)||mxClient.IS_IE)
{
this.node.style.left=(this.bounds.x-parseInt(this.node.parentNode.style.left)||0)+'px';
this.node.style.top=(this.bounds.y-parseInt(this.node.parentNode.style.top)||0)+'px';
}
if(this.bounds.width>0||this.bounds.height>0)
{
this.node.style.width=this.bounds.width+'px';
this.node.style.height=this.bounds.height+'px';
table.setAttribute('height','100%');
table.setAttribute('width','100%');
}
}
}
else
{
if(!mxUtils.isVml(this.node))
{
this.node.style.width=this.bounds.width+'px';
this.node.style.height=this.bounds.height+'px';
}
table.setAttribute('height','100%');
table.setAttribute('width','100%');
}
td.style.textAlign=(this.align==mxConstants.ALIGN_RIGHT)?'right':((this.align==mxConstants.ALIGN_CENTER)?'center':
'left');
td.style.verticalAlign=(this.valign==mxConstants.ALIGN_BOTTOM)?'bottom':((this.valign==mxConstants.ALIGN_MIDDLE)?'middle':
'top');
var container=td;


if(!this.useTableBounds&&(this.background!=null||this.border!=null))
{
var tbl=document.createElement('table');
tbl.style.borderCollapse='collapse';
var tb=document.createElement('tbody');
var tr=document.createElement('tr');
container=document.createElement('td');
container.style.textAlign=td.style.textAlign;
container.style.verticalAlign=td.style.verticalAlign;
tr.appendChild(container);
tb.appendChild(tr);
tbl.appendChild(tb);
td.appendChild(tbl);
if(mxClient.IS_MAC)
{
tbl.setAttribute('align',td.style.textAlign);
}
}
container.style.zoom=this.scale;
container.style.color=this.color;
container.style.fontSize=(this.size*scale)+'px';
container.style.fontFamily=this.family;
if(this.isRotate)
{
if(container!=td)
{
td.style.verticalAlign=(this.align==mxConstants.ALIGN_RIGHT)?'top':((this.align==mxConstants.ALIGN_CENTER)?'middle':
'bottom');
td.style.textAlign=(this.valign==mxConstants.ALIGN_BOTTOM)?'right':((this.valign==mxConstants.ALIGN_MIDDLE)?'center':
'left');
}
container.style.writingMode='tb-rl';
container.style.filter='flipv fliph';
td.style.paddingTop=(this.spacingRight*this.scale)+'px';
td.style.paddingRight=(this.spacingBottom*this.scale)+'px';
td.style.paddingBottom=(this.spacingLeft*this.scale)+'px';
td.style.paddingLeft=(this.spacingTop*this.scale)+'px';
}
else
{
td.style.paddingTop=(this.spacingTop*this.scale)+'px';
td.style.paddingRight=(this.spacingRight*this.scale)+'px';
td.style.paddingBottom=(this.spacingBottom*this.scale)+'px';
td.style.paddingLeft=(this.spacingLeft*this.scale)+'px';
}
if(this.isStyleSet(mxConstants.FONT_BOLD))
{
container.style.fontWeight='bold';
}
else
{
container.style.fontWeight='normal';
}
if(this.isStyleSet(mxConstants.FONT_ITALIC))
{
container.style.fontStyle='italic';
}
if(this.isStyleSet(mxConstants.FONT_UNDERLINE))
{
container.style.textDecoration='underline';
}
if(!this.isWrapping)
{
container.style.whiteSpace='nowrap';
}
if(this.background!=null)
{
container.style.background=this.background;
}
if(this.border!=null)
{
container.style.borderColor=this.border;
container.style.borderWidth='1px';
container.style.borderStyle='solid';
}
if(!mxUtils.isNode(this.value))
{
var value=this.value.replace(/\n/g,'<br/>');
if(mxClient.IS_IE&&this.isStyleSet(mxConstants.FONT_SHADOW))
{
value='<p style=\"height:1em;filter:Shadow(Color=#666666,'+'Direction=135,Strength=%);\">'+value+'</p>';
}
container.innerHTML=value;
}
else
{
container.appendChild(this.value);
}
row.appendChild(td);
tbody.appendChild(row);
table.appendChild(tbody);
if(this.node.nodeName=='foreignObject')
{


if(this.node.firstChild!=null)
{
table=this.node.firstChild.firstChild;
var oldTd=table.firstChild.firstChild.firstChild;
oldTd.style.cssText=td.getAttribute('style');
}
else
{
var body=document.createElementNS(mxConstants.NS_XHTML,'body');
body.style.overflow=this.node.style.overflow;
table.setAttribute('width','100%');
table.setAttribute('height','100%');
body.appendChild(table);
this.node.appendChild(body);
}
}
else
{
this.node.innerHTML='';
this.node.appendChild(table);
}
var xdiff=0;
var ydiff=0;
var tmpalign=(this.isRotate)?this.valign:this.align;
var tmpvalign=(this.isRotate)?this.align:this.valign;
if(this.node.style.overflow!='hidden')
{
if(this.bounds.width>0||this.useTableBounds)
{
xdiff=Math.floor(Math.max(0,table.offsetWidth-this.bounds.width));
if(tmpalign==mxConstants.ALIGN_CENTER||tmpalign==mxConstants.ALIGN_MIDDLE)
{
xdiff=Math.floor(xdiff/2);
}
else if(tmpalign!=mxConstants.ALIGN_RIGHT&&tmpalign!=mxConstants.ALIGN_BOTTOM)
{
xdiff=0;
}
}
if(this.bounds.height>0||this.useTableBounds)
{
ydiff=Math.floor(Math.max(0,table.offsetHeight-this.bounds.height));
if(tmpvalign==mxConstants.ALIGN_MIDDLE||tmpvalign==mxConstants.ALIGN_CENTER)
{
ydiff=Math.floor(ydiff/2);
}
else if((!this.isRotate&&tmpvalign!=mxConstants.ALIGN_BOTTOM)||(this.isRotate&&tmpvalign!=mxConstants.ALIGN_LEFT))
{
ydiff=0;
}
}
if(xdiff>0||ydiff>0)
{
if(!mxUtils.isVml(this.node))
{
this.node.style.marginLeft=-xdiff+'px';
this.node.style.marginTop=-ydiff+'px';
}
else
{
var x0=parseInt(this.node.parentNode.style.left)||0;
var y0=parseInt(this.node.parentNode.style.top)||0;
xdiff-=this.bounds.x-x0;
ydiff-=this.bounds.y-y0;
this.node.inset=(-xdiff)+'px,'+(-ydiff)+'px,0px,0px';
}
}
else if(mxUtils.isVml(this.node))
{
var x0=parseInt(this.node.parentNode.style.left);
var y0=parseInt(this.node.parentNode.style.top);
this.node.inset=(this.bounds.x-x0)+'px,'+(this.bounds.y-y0)+'px,'+(y0-this.bounds.y)+'px,'+(x0-this.bounds.x)+'px';
}
}
if(this.opacity!=null)
{
mxUtils.setOpacity(this.node,this.opacity);
}
var x=this.bounds.x-xdiff;
var y=this.bounds.y-ydiff;
var width=Math.max(this.bounds.width,table.offsetWidth||0);
var height=Math.max(this.bounds.height,table.offsetHeight||0);
this.boundingBox=new mxRectangle(x,y,width,height);
}
else
{
this.node.innerHTML='<div style=\'width:100%;height:100%;\'></div>';
this.boundingBox=this.bounds.clone();
if(!mxUtils.isVml(this.node))
{
this.node.style.position='absolute';
this.node.style.left=this.bounds.x+'px';
this.node.style.top=this.bounds.y+'px';
this.node.style.width=this.bounds.width+'px';
this.node.style.height=this.bounds.height+'px';
}
}


if(this.node.nodeName=='foreignObject')
{
this.node.setAttribute('x',parseInt(this.node.style.left)+parseInt(this.node.style.marginLeft));
this.node.setAttribute('y',parseInt(this.node.style.top)+parseInt(this.node.style.marginTop));
var w=parseInt(this.node.style.width);
if(!isNaN(w))
{
this.node.setAttribute('width',w);
}
var h=parseInt(this.node.style.height);
if(!isNaN(h))
{
this.node.setAttribute('height',h);
}
}
};
mxText.prototype.createSvg=function()
{

var node=document.createElementNS(mxConstants.NS_SVG,'g');
var uline=this.isStyleSet(mxConstants.FONT_UNDERLINE)?'underline':'none';
var weight=this.isStyleSet(mxConstants.FONT_BOLD)?'bold':'normal';
var s=this.isStyleSet(mxConstants.FONT_ITALIC)?'italic':null;
var align=(this.align==mxConstants.ALIGN_RIGHT)?'end':(this.align==mxConstants.ALIGN_CENTER)?'middle':
'start';

node.setAttribute('text-decoration',uline);
node.setAttribute('text-anchor',align);
node.setAttribute('font-family',this.family);
node.setAttribute('font-weight',weight);
node.setAttribute('font-size',Math.floor(this.size*this.scale)+'px');
node.setAttribute('fill',this.color);
if(s!=null)
{
node.setAttribute('font-style',s);
}
if(this.background!=null||this.border!=null)
{
this.backgroundNode=document.createElementNS(mxConstants.NS_SVG,'rect');
this.backgroundNode.setAttribute('shape-rendering','optimizeSpeed');
if(this.background!=null)
{
this.backgroundNode.setAttribute('fill',this.background);
}
else
{
this.backgroundNode.setAttribute('fill','none');
}
if(this.border!=null)
{
this.backgroundNode.setAttribute('stroke',this.border);
}
else
{
this.backgroundNode.setAttribute('stroke','none');
}
}
this.updateSvgValue(node);
return node;
};
mxText.prototype.updateSvgValue=function(node)
{
if(this.currentValue!=this.value)
{
while(node.firstChild!=null)
{
node.removeChild(node.firstChild);
}
if(this.value!=null)
{
var lines=this.value.split('\n');
for(var i=0;i<lines.length;i++)
{
var tspan=this.createSvgSpan(lines[i]);
node.appendChild(tspan);
}
}
this.currentValue=this.value;
}
};
mxText.prototype.redrawSvg=function()
{
if(this.node.nodeName=='foreignObject')
{
this.redrawHtml()
return;
}
this.updateSvgValue(this.node);
this.node.setAttribute('font-size',Math.floor(this.size*this.scale)+'px');
if(this.opacity!=null)
{
this.node.setAttribute('fill-opacity',this.opacity/100);
this.node.setAttribute('stroke-opacity',this.opacity/100);
}
var dy=this.size*1.3*this.scale;
var childCount=this.node.childNodes.length;
if(this.backgroundNode!=null)
{
childCount--;
}
var x=this.bounds.x;
var y=this.bounds.y;
x+=(this.align==mxConstants.ALIGN_RIGHT)?((this.isRotate)?this.bounds.height:this.bounds.width)-this.spacingRight*this.scale:(this.align==mxConstants.ALIGN_CENTER)?this.spacingLeft+(((this.isRotate)?this.bounds.height:this.bounds.width)-this.spacingLeft-this.spacingRight)/2:
this.spacingLeft*this.scale;
var y0=(this.valign==mxConstants.ALIGN_BOTTOM)?((this.isRotate)?this.bounds.width:this.bounds.height)-(childCount-1)*dy-this.spacingBottom*this.scale-3:(this.valign==mxConstants.ALIGN_MIDDLE)?(this.spacingTop*this.scale+((this.isRotate)?this.bounds.width:this.bounds.height)-this.spacingBottom*this.scale-(childCount-1.5)*dy)/2+1:
this.spacingTop*this.scale+dy-2;
y+=y0;
this.node.setAttribute('x',x);
this.node.setAttribute('y',y);
if(this.isRotate)
{
var cx=this.bounds.x+this.bounds.width/2;
var cy=this.bounds.y+this.bounds.height/2;
var offsetX=(this.bounds.width-this.bounds.height)/2;
var offsetY=(this.bounds.height-this.bounds.width)/2;
this.node.setAttribute('transform','rotate(-90 '+cx+' '+cy+') '+'translate('+-offsetY+' '+(-offsetX)+')');
}
if(this.backgroundNode!=null&&this.backgroundNode.parentNode==this.node)
{
this.node.removeChild(this.backgroundNode);
}
for(var i=0;i<childCount;i++)
{
var node=this.node.childNodes[i];
node.setAttribute('x',x);
node.setAttribute('y',y);
y+=dy;
node.setAttribute('style','pointer-events: all');
}
this.boundingBox=this.bounds.clone();
if(this.value!=null&&this.value.length>0)
{
try
{
var box=this.node.getBBox();
this.boundingBox=new mxRectangle(Math.min(this.bounds.x,box.x-4*this.scale||0),Math.min(this.bounds.y,box.y-4*this.scale||0),Math.max(this.bounds.width,box.width+8*this.scale||0),Math.max(this.bounds.height,box.height+10*this.scale||0));
if(this.backgroundNode!=null&&this.node.firstChild!=null)
{
this.node.insertBefore(this.backgroundNode,this.node.firstChild);
this.backgroundNode.setAttribute('x',box.x-4*this.scale||0);
this.backgroundNode.setAttribute('y',box.y-4*this.scale||0);
this.backgroundNode.setAttribute('width',box.width+8*this.scale||0);
this.backgroundNode.setAttribute('height',box.height+8*this.scale||0);
var strokeWidth=Math.floor(Math.max(1,this.scale));
this.backgroundNode.setAttribute('stroke-width',strokeWidth);
}
}
catch(ex)
{
}
}
};
mxText.prototype.createSvgSpan=function(text)
{



var node=document.createElementNS(mxConstants.NS_SVG,'text');
mxUtils.write(node,text);
return node;
};
}

{
function mxTriangle(){};
mxTriangle.prototype=new mxActor();
mxTriangle.prototype.constructor=mxTriangle;
mxTriangle.prototype.redrawPath=function(path,x,y,w,h)
{
var dir=this.style[mxConstants.STYLE_DIRECTION];
if(dir==mxConstants.DIRECTION_NORTH)
{
path.moveTo(0,h);
path.lineTo(0.5*w,0);
path.lineTo(w,h);
}
else if(dir==mxConstants.DIRECTION_SOUTH)
{
path.moveTo(0,0);
path.lineTo(0.5*w,h);
path.lineTo(w,0);
}
else if(dir==mxConstants.DIRECTION_WEST)
{
path.moveTo(w,0);
path.lineTo(0,0.5*h);
path.lineTo(w,h);
}
else 
{
path.moveTo(0,0);
path.lineTo(w,0.5*h);
path.lineTo(0,h);
}
path.close();
};
}

{
function mxHexagon(){};
mxHexagon.prototype=new mxActor();
mxHexagon.prototype.constructor=mxHexagon;
mxHexagon.prototype.redrawPath=function(path,x,y,w,h)
{
var dir=this.style[mxConstants.STYLE_DIRECTION];
if(dir==mxConstants.DIRECTION_NORTH||dir==mxConstants.DIRECTION_SOUTH)
{
path.moveTo(0.5*w,0);
path.lineTo(w,0.25*h);
path.lineTo(w,0.75*h);
path.lineTo(0.5*w,h);
path.lineTo(0,0.75*h);
path.lineTo(0,0.25*h);
}
else
{
path.moveTo(0.25*w,0);
path.lineTo(0.75*w,0);
path.lineTo(w,0.5*h);
path.lineTo(0.75*w,h);
path.lineTo(0.25*w,h);
path.lineTo(0,0.5*h);
}
path.close();
};
}

{
function mxLine(bounds,stroke,strokewidth)
{
this.bounds=bounds;
this.stroke=stroke||'black';
this.strokewidth=strokewidth||'1';
};
mxLine.prototype=new mxShape();
mxLine.prototype.constructor=mxLine;
mxLine.prototype.mixedModeHtml=false;
mxLine.prototype.preferModeHtml=false;
mxLine.prototype.clone=function()
{
var clone=new mxLine(this.bounds,this.stroke,this.strokewidth);
clone.isDashed=this.isDashed;
return clone;
};
mxLine.prototype.createVml=function()
{
var node=document.createElement('v:group');
node.setAttribute('coordorigin','0,0');
node.style.position='absolute';
node.style.overflow='visible';
this.label=document.createElement('v:rect');
this.configureVmlShape(this.label);
this.label.setAttribute('stroked','false');
this.label.setAttribute('filled','false');
node.appendChild(this.label);
this.innerNode=document.createElement('v:polyline');
this.configureVmlShape(this.innerNode);
node.appendChild(this.innerNode);
return node;
};
mxLine.prototype.redrawVml=function()
{
var x=Math.floor(this.bounds.x);
var y=Math.floor(this.bounds.y);
var w=Math.floor(this.bounds.width);
var h=Math.floor(this.bounds.height);
this.updateVmlShape(this.node);
this.node.setAttribute('coordsize',w+','+h);
this.updateVmlShape(this.label);
this.label.style.left='0px';
this.label.style.top='0px';
this.innerNode.setAttribute('strokeweight',this.strokewidth*this.scale);
var direction=this.style[mxConstants.STYLE_DIRECTION];
if(direction==mxConstants.DIRECTION_NORTH||direction==mxConstants.DIRECTION_SOUTH)
{
this.innerNode.points.value=(w/2)+',0 '+(w/2)+','+(h);
}
else
{
this.innerNode.points.value='0,'+(h/2)+' '+(w)+','+(h/2);
}
};
mxLine.prototype.createSvg=function()
{
var g=this.createSvgGroup('path');



var color=this.innerNode.getAttribute('stroke');
this.pipe=document.createElementNS(mxConstants.NS_SVG,'path');
this.pipe.setAttribute('stroke',color);
this.pipe.setAttribute('visibility','hidden');
this.pipe.setAttribute('pointer-events','stroke');
g.appendChild(this.pipe);
return g;
};
mxLine.prototype.redrawSvg=function()
{
var strokeWidth=Math.max(1,this.strokewidth*this.scale);
this.innerNode.setAttribute('stroke-width',strokeWidth);
if(this.bounds!=null)
{
var x=this.bounds.x;
var y=this.bounds.y;
var w=this.bounds.width;
var h=this.bounds.height;
var d=null;
var direction=this.style[mxConstants.STYLE_DIRECTION];
if(direction==mxConstants.DIRECTION_NORTH||direction==mxConstants.DIRECTION_SOUTH)
{
d='M '+(x+w/2)+' '+y+' L '+(x+w/2)+' '+(y+h);
}
else
{
d='M '+x+' '+(y+h/2)+' L '+(x+w)+' '+(y+h/2);
}
this.innerNode.setAttribute('d',d);
this.pipe.setAttribute('d',d);
this.pipe.setAttribute('stroke-width',this.strokewidth*this.scale+mxShape.prototype.SVG_STROKE_TOLERANCE);
this.updateSvgTransform(this.innerNode,false);
this.updateSvgTransform(this.pipe,false);
}
};
}

{
function mxImageShape(bounds,image,fill,stroke,strokewidth)
{
this.bounds=bounds;
this.image=image;
this.fill=fill;
this.stroke=stroke;
this.strokewidth=strokewidth||0;
this.isShadow=false;
};
mxImageShape.prototype=new mxShape();
mxImageShape.prototype.constructor=mxImageShape;
mxImageShape.prototype.create=function()
{
var node=null;
if(this.dialect==mxConstants.DIALECT_SVG)
{




node=this.createSvgGroup('rect');
this.innerNode.setAttribute('fill',this.fill);
this.innerNode.setAttribute('visibility','hidden');
this.innerNode.setAttribute('pointer-events','fill');
this.imageNode=document.createElementNS(mxConstants.NS_SVG,'image');
this.imageNode.setAttributeNS(mxConstants.NS_XLINK,'href',this.image);
this.imageNode.setAttribute('style','pointer-events:none');
this.configureSvgShape(this.imageNode);
node.insertBefore(this.imageNode,this.innerNode);
}
else
{
if(this.dialect==mxConstants.DIALECT_STRICTHTML||this.dialect==mxConstants.DIALECT_PREFERHTML)
{
node=document.createElement('DIV');
this.configureHtmlShape(node);
var imgName=this.image.toUpperCase()
if(imgName.substring(imgName.length-3,imgName.length)=="PNG"&&mxClient.IS_IE&&!mxClient.IS_IE7)
{
node.style.filter='progid:DXImageTransform.Microsoft.AlphaImageLoader (src=\''+this.image+'\', sizingMethod=\'scale\')';
}
else
{
var img=document.createElement('img');
img.setAttribute('src',this.image);
img.style.width='100%';
img.style.height='100%';
img.setAttribute('border','0');
node.appendChild(img);
}
}
else
{
node=document.createElement('v:image');
node.setAttribute('src',this.image);
this.configureVmlShape(node);
}
}
return node;
};
mxImageShape.prototype.redrawSvg=function()
{
this.updateSvgShape(this.innerNode);
this.updateSvgShape(this.imageNode);
};
}

{
function mxLabel(bounds,fill,stroke,strokewidth)
{
this.bounds=bounds;
this.fill=fill;
this.stroke=stroke;
this.strokewidth=strokewidth||1;
};
mxLabel.prototype=new mxShape();
mxLabel.prototype.constructor=mxLabel;
mxLabel.prototype.imageSize=mxConstants.DEFAULT_IMAGESIZE;
mxLabel.prototype.spacing=2;
mxLabel.prototype.indicatorSize=10;
mxLabel.prototype.indicatorSpacing=2;
mxLabel.prototype.createHtml=function()
{
var name='DIV';
var node=document.createElement(name);
this.configureHtmlShape(node);
if(this.indicatorColor!=null&&this.indicatorShape!=null)
{
this.indicator=new this.indicatorShape(this.bounds);
this.indicator.dialect=this.dialect;
this.indicator.fill=this.indicatorColor;
this.indicator.gradient=this.indicatorGradientColor;
this.indicator.init(node);
}
else if(this.indicatorImage!=null)
{
this.indicatorImageNode=mxUtils.createImage(this.indicatorImage);
this.indicatorImageNode.style.position='absolute';
node.appendChild(this.indicatorImageNode);
}
if(this.image!=null)
{
this.imageNode=mxUtils.createImage(this.image);
this.stroke=null;
this.configureHtmlShape(this.imageNode);
node.appendChild(this.imageNode);
}
return node;
};
mxLabel.prototype.createVml=function()
{
var node=document.createElement('v:group');
var name=(this.isRounded)?'v:roundrect':'v:rect';
this.rectNode=document.createElement(name);
this.configureVmlShape(this.rectNode);
this.isShadow=false;
this.configureVmlShape(node);
node.setAttribute('coordorigin','0,0');
node.appendChild(this.rectNode);
if(this.indicatorColor!=null&&this.indicatorShape!=null)
{
this.indicator=new this.indicatorShape(this.bounds);
this.indicator.dialect=this.dialect;
this.indicator.fill=this.indicatorColor;
this.indicator.gradient=this.indicatorGradientColor;
this.indicator.init(node);
}
else if(this.indicatorImage!=null)
{
this.indicatorImageNode=document.createElement('v:image');
this.indicatorImageNode.setAttribute('src',this.indicatorImage);
node.appendChild(this.indicatorImageNode);
}
if(this.image!=null)
{
this.imageNode=document.createElement('v:image');
this.imageNode.setAttribute('src',this.image);
this.configureVmlShape(this.imageNode);
node.appendChild(this.imageNode);
}
this.label=document.createElement('v:rect');
this.label.style.top='0px';
this.label.style.left='0px';
this.label.setAttribute('filled','false');
this.label.setAttribute('stroked','false');
node.appendChild(this.label);
return node;
};
mxLabel.prototype.createSvg=function()
{
var g=this.createSvgGroup('rect');
if(this.strokewidth*this.scale>=1&&!this.isRounded)
{
this.innerNode.setAttribute('shape-rendering','optimizeSpeed');
}
if(this.indicatorColor!=null&&this.indicatorShape!=null)
{
this.indicator=new this.indicatorShape(this.bounds);
this.indicator.dialect=this.dialect;
this.indicator.fill=this.indicatorColor;
this.indicator.gradient=this.indicatorGradientColor;
this.indicator.init(g);
}
else if(this.indicatorImage!=null)
{
this.indicatorImageNode=document.createElementNS(mxConstants.NS_SVG,'image');
this.indicatorImageNode.setAttributeNS(mxConstants.NS_XLINK,'href',this.indicatorImage);
g.appendChild(this.indicatorImageNode);
}
if(this.image!=null)
{
this.imageNode=document.createElementNS(mxConstants.NS_SVG,'image');
this.imageNode.setAttributeNS(mxConstants.NS_XLINK,'href',this.image);
this.imageNode.setAttribute('style','pointer-events:none');
this.configureSvgShape(this.imageNode);
g.appendChild(this.imageNode);
}
return g;
};
mxLabel.prototype.redraw=function()
{
var isSvg=(this.dialect==mxConstants.DIALECT_SVG);
var isVml=mxUtils.isVml(this.node);
if(isSvg)
{
this.updateSvgShape(this.innerNode);
if(this.shadowNode!=null)
{
this.updateSvgShape(this.shadowNode);
}
}
else if(isVml)
{
this.updateVmlShape(this.node);
this.node.setAttribute('coordsize',this.bounds.width+','+this.bounds.height);
this.updateVmlShape(this.rectNode);
this.rectNode.style.top='0px';
this.rectNode.style.left='0px';
this.label.style.width=this.bounds.width+'px';
this.label.style.height=this.bounds.height+'px';
}
else
{
this.updateHtmlShape(this.node);
}
var imageWidth=0;
var imageHeight=0;
if(this.imageNode!=null)
{
imageWidth=(this.style[mxConstants.STYLE_IMAGE_WIDTH]||this.imageSize)*this.scale;
imageHeight=(this.style[mxConstants.STYLE_IMAGE_HEIGHT]||this.imageSize)*this.scale;
}
var indicatorSpacing=0;
var indicatorWidth=0;
var indicatorHeight=0;
if(this.indicator!=null||this.indicatorImageNode!=null)
{
indicatorSpacing=(this.style[mxConstants.STYLE_INDICATOR_SPACING]||this.indicatorSpacing)*this.scale;
indicatorWidth=(this.style[mxConstants.STYLE_INDICATOR_WIDTH]||this.indicatorSize)*this.scale;
indicatorHeight=(this.style[mxConstants.STYLE_INDICATOR_HEIGHT]||this.indicatorSize)*this.scale;
}
var align=this.style[mxConstants.STYLE_IMAGE_ALIGN];
var valign=this.style[mxConstants.STYLE_IMAGE_VERTICAL_ALIGN];
var inset=this.spacing*this.scale;
var width=Math.max(imageWidth,indicatorWidth);
var height=imageHeight+indicatorSpacing+indicatorHeight;
var x=(isSvg)?this.bounds.x:0;
if(align==mxConstants.ALIGN_RIGHT)
{
x+=this.bounds.width-width-inset;
}
else if(align==mxConstants.ALIGN_CENTER)
{
x+=(this.bounds.width-width)/2;
}
else 
{
x+=inset;
}
var y=(isSvg)?this.bounds.y:0;
if(valign==mxConstants.ALIGN_BOTTOM)
{
y+=this.bounds.height-height-inset;
}
else if(valign==mxConstants.ALIGN_TOP)
{
y+=inset;
}
else 
{
y+=(this.bounds.height-height)/2;
}
if(this.imageNode!=null)
{
if(isSvg)
{
this.imageNode.setAttribute('x',(x+(width-imageWidth)/2)+'px');
this.imageNode.setAttribute('y',y+'px');
this.imageNode.setAttribute('width',imageWidth+'px');
this.imageNode.setAttribute('height',imageHeight+'px');
}
else
{
this.imageNode.style.left=(x+width-imageWidth)+'px';
this.imageNode.style.top=y+'px';
this.imageNode.style.width=imageWidth+'px';
this.imageNode.style.height=imageHeight+'px';
}
}
if(this.indicator!=null)
{
this.indicator.bounds=new mxRectangle(x+(width-indicatorWidth)/2,y+imageHeight+indicatorSpacing,indicatorWidth,indicatorHeight);
this.indicator.redraw();
}
else if(this.indicatorImageNode!=null)
{
if(isSvg)
{
this.indicatorImageNode.setAttribute('x',(x+(width-indicatorWidth)/2)+'px');
this.indicatorImageNode.setAttribute('y',(y+imageHeight+indicatorSpacing)+'px');
this.indicatorImageNode.setAttribute('width',indicatorWidth+'px');
this.indicatorImageNode.setAttribute('height',indicatorHeight+'px');
}
else
{
this.indicatorImageNode.style.left=(x+(width-indicatorWidth)/2)+'px';
this.indicatorImageNode.style.top=(y+imageHeight+indicatorSpacing)+'px';
this.indicatorImageNode.style.width=indicatorWidth+'px';
this.indicatorImageNode.style.height=indicatorHeight+'px';
}
}
};
}

{
function mxCylinder(bounds,fill,stroke,strokewidth)
{
this.bounds=bounds;
this.fill=fill;
this.stroke=stroke;
this.strokewidth=strokewidth||1;
};
mxCylinder.prototype=new mxShape();
mxCylinder.prototype.constructor=mxCylinder;
mxCylinder.prototype.mixedModeHtml=false;
mxCylinder.prototype.preferModeHtml=false;
mxCylinder.prototype.maxHeight=40;
mxCylinder.prototype.create=function(container)
{
if(this.stroke==null)
{
this.stroke=this.fill;
}
return mxShape.prototype.create.apply(this,arguments);
};
mxCylinder.prototype.createVml=function()
{
var node=document.createElement('v:group');
this.background=document.createElement('v:shape');
this.label=this.background;
this.configureVmlShape(this.background);
node.appendChild(this.background);
this.fill=null;
this.isShadow=false;
this.configureVmlShape(node);
this.foreground=document.createElement('v:shape');
this.configureVmlShape(this.foreground);
node.appendChild(this.foreground);
return node;
};
mxCylinder.prototype.redrawVml=function()
{
var x=Math.floor(this.bounds.x);
var y=Math.floor(this.bounds.y);
var w=Math.floor(this.bounds.width);
var h=Math.floor(this.bounds.height);
var s=this.strokewidth*this.scale;
this.node.setAttribute('coordsize',w+','+h);
this.background.setAttribute('coordsize',w+','+h);
this.foreground.setAttribute('coordsize',w+','+h);
this.updateVmlShape(this.node);
this.updateVmlShape(this.background);
this.background.style.top='0px';
this.background.style.left='0px';
this.background.style.rotation=null;
this.updateVmlShape(this.foreground);
this.foreground.style.top='0px';
this.foreground.style.left='0px';
this.foreground.style.rotation=null;
this.background.setAttribute('strokeweight',s);
this.foreground.setAttribute('strokeweight',s);
var d=this.createPath(false);
this.background.setAttribute('path',d);
var d=this.createPath(true);
this.foreground.setAttribute('path',d);
};
mxCylinder.prototype.createSvg=function()
{
var g=this.createSvgGroup('path');
this.foreground=document.createElementNS(mxConstants.NS_SVG,'path');
if(this.stroke!=null)
{
this.foreground.setAttribute('stroke',this.stroke);
}
else
{
this.foreground.setAttribute('stroke','none');
}
this.foreground.setAttribute('fill','none');
g.appendChild(this.foreground);
return g;
};
mxCylinder.prototype.redrawSvg=function()
{
var strokeWidth=Math.max(1,this.strokewidth*this.scale);
this.innerNode.setAttribute('stroke-width',strokeWidth);
var d=this.createPath(false);
this.innerNode.setAttribute('d',d);
this.updateSvgTransform(this.innerNode,false);
if(this.shadowNode!=null)
{
this.shadowNode.setAttribute('stroke-width',strokeWidth);
this.shadowNode.setAttribute('d',d);
this.updateSvgTransform(this.shadowNode,true);
}
d=this.createPath(true);
this.foreground.setAttribute('stroke-width',strokeWidth);
this.foreground.setAttribute('d',d);
this.updateSvgTransform(this.foreground,false);
};
mxCylinder.prototype.redrawPath=function(path,x,y,w,h,isForeground)
{
var dy=Math.min(this.maxHeight,Math.floor(h/5));
if(isForeground)
{
path.moveTo(0,dy);
path.curveTo(0,2*dy,w,2*dy,w,dy);
}
else
{
path.moveTo(0,dy);
path.curveTo(0,-dy/3,w,-dy/3,w,dy);
path.lineTo(w,h-dy);
path.curveTo(w,h+dy/3,0,h+dy/3,0,(h-dy));
path.close();
}
};
}

{
function mxConnector(points,stroke,strokewidth)
{
this.points=points;
this.stroke=stroke||'black';
this.strokewidth=strokewidth||1;
};
mxConnector.prototype=new mxShape();
mxConnector.prototype.constructor=mxConnector;
mxConnector.prototype.mixedModeHtml=false;
mxConnector.prototype.preferModeHtml=false;
mxConnector.prototype.createHtml=function()
{
var node=document.createElement('DIV');
this.configureHtmlShape(node);
node.style.borderStyle='none';
node.style.background='';
return node;
};
mxConnector.prototype.createVml=function()
{
var node=document.createElement('v:shape');
this.strokeNode=document.createElement('v:stroke');
this.configureVmlShape(node);
this.strokeNode.setAttribute('endarrow',this.endArrow);
this.strokeNode.setAttribute('startarrow',this.startArrow);
if(this.opacity!=null)
{
this.strokeNode.setAttribute('opacity',this.opacity+'%');
}
node.appendChild(this.strokeNode);
return node;
};
mxConnector.prototype.redrawVml=function()
{
if(this.node!=null&&this.strokeNode!=null)
{
var startSize=mxUtils.getValue(this.style,mxConstants.STYLE_STARTSIZE,mxConstants.DEFAULT_MARKERSIZE)*this.scale;
var endSize=mxUtils.getValue(this.style,mxConstants.STYLE_ENDSIZE,mxConstants.DEFAULT_MARKERSIZE)*this.scale;
var startWidth='medium';
var startLength='medium';
var endWidth='medium';
var endLength='medium';
if(startSize<6)
{
startWidth='narrow';
startLength='short';
}
else if(startSize>10)
{
startWidth='wide';
startLength='long';
}
if(endSize<6)
{
endWidth='narrow';
endLength='short';
}
else if(endSize>10)
{
endWidth='wide';
endLength='long';
}
this.strokeNode.setAttribute('startarrowwidth',startWidth);
this.strokeNode.setAttribute('startarrowlength',startLength);
this.strokeNode.setAttribute('endarrowwidth',endWidth);
this.strokeNode.setAttribute('endarrowlength',endLength);
this.updateVmlShape(this.node);
}
};
mxConnector.prototype.createSvg=function()
{
var g=this.createSvgGroup('path');
var color=this.innerNode.getAttribute('stroke');
if(this.startArrow!=null)
{
this.start=document.createElementNS(mxConstants.NS_SVG,'path');
g.appendChild(this.start);
}
if(this.endArrow!=null)
{
this.end=document.createElementNS(mxConstants.NS_SVG,'path');
g.appendChild(this.end);
}



this.pipe=document.createElementNS(mxConstants.NS_SVG,'path');
this.pipe.setAttribute('stroke',color);
this.pipe.setAttribute('visibility','hidden');
this.pipe.setAttribute('pointer-events','stroke');
g.appendChild(this.pipe);
return g;
};
mxConnector.prototype.redrawSvg=function()
{
mxShape.prototype.redrawSvg.apply(this,arguments);
var strokeWidth=this.strokewidth*this.scale;
var color=this.innerNode.getAttribute('stroke');
if(mxConstants.SVG_CRISP_EDGES&&strokeWidth==Math.floor(strokeWidth)&&!this.isRounded)
{
this.node.setAttribute('shape-rendering','optimizeSpeed');
}
else
{
this.node.setAttribute('shape-rendering','default');
}



if(this.points!=null&&this.points[0]!=null)
{
if(this.start!=null)
{
var p0=this.points[1];
var pe=this.points[0];
var size=mxUtils.getValue(this.style,mxConstants.STYLE_STARTSIZE,mxConstants.DEFAULT_MARKERSIZE);
this.startOffset=this.redrawSvgMarker(this.start,this.startArrow,p0,pe,color,size);
}
if(this.end!=null)
{
var n=this.points.length;
var p0=this.points[n-2];
var pe=this.points[n-1];
var size=mxUtils.getValue(this.style,mxConstants.STYLE_ENDSIZE,mxConstants.DEFAULT_MARKERSIZE);
this.endOffset=this.redrawSvgMarker(this.end,this.endArrow,p0,pe,color,size);
}
}
this.updateSvgShape(this.innerNode);
this.pipe.setAttribute('d',this.innerNode.getAttribute('d'));
this.pipe.setAttribute('stroke-width',strokeWidth+mxShape.prototype.SVG_STROKE_TOLERANCE);
this.innerNode.setAttribute('fill','none');
};
mxConnector.prototype.redrawSvgMarker=function(node,type,p0,pe,color,size)
{
var offset=null;
var dx=pe.x-p0.x;
var dy=pe.y-p0.y;
var dist=Math.max(1,Math.sqrt(dx*dx+dy*dy));
var absSize=size*this.scale;
var nx=dx*absSize/dist;
var ny=dy*absSize/dist;
pe=pe.clone();
pe.x-=nx*this.strokewidth/(2*size);
pe.y-=ny*this.strokewidth/(2*size);
nx*=0.5+this.strokewidth/2;
ny*=0.5+this.strokewidth/2;
if(type=='classic'||type=='block')
{
var d='M '+pe.x+' '+pe.y+' L '+(pe.x-nx-ny/2)+' '+(pe.y-ny+nx/2)+((type!='classic')?'':
' L '+(pe.x-nx*3/4)+' '+(pe.y-ny*3/4))+' L '+(pe.x+ny/2-nx)+' '+(pe.y-ny-nx/2)+' z';
node.setAttribute('d',d);
offset=new mxPoint(-nx*3/4,-ny*3/4);
}
else if(type=='open')
{
nx*=1.2;
ny*=1.2;
var d='M '+(pe.x-nx-ny/2)+' '+(pe.y-ny+nx/2)+' L '+(pe.x-nx/6)+' '+(pe.y-ny/6)+' L '+(pe.x+ny/2-nx)+' '+(pe.y-ny-nx/2)+' M '+pe.x+' '+pe.y;
node.setAttribute('d',d);
node.setAttribute('fill','none');
node.setAttribute('stroke-width',this.scale*this.strokewidth);
offset=new mxPoint(-nx/4,-ny/4);
}
else if(type=='oval')
{
nx*=1.2;
ny*=1.2;
absSize*=1.2;
var d='M '+(pe.x-ny/2)+' '+(pe.y+nx/2)+' a '+(absSize/2)+' '+(absSize/2)+' 0  1,1 '+(nx/8)+' '+(ny/8)+' z';
node.setAttribute('d',d);
}
else if(type=='diamond')
{
var d='M '+(pe.x+nx/2)+' '+(pe.y+ny/2)+' L '+(pe.x-ny/2)+' '+(pe.y+nx/2)+' L '+(pe.x-nx/2)+' '+(pe.y-ny/2)+' L '+(pe.x+ny/2)+' '+(pe.y-nx/2)+' z';
node.setAttribute('d',d);
}
node.setAttribute('stroke',color);
if(type!='open')
{
node.setAttribute('fill',color);
}
else
{
node.setAttribute('stroke-linecap','round');
}
if(this.opacity!=null)
{
node.setAttribute('fill-opacity',this.opacity/100);
node.setAttribute('stroke-opacity',this.opacity/100);
}
return offset;
};
}

{
function mxSwimlane(bounds,fill,stroke,strokewidth)
{
this.bounds=bounds;
this.fill=fill;
this.stroke=stroke;
this.strokewidth=strokewidth||1;
};
mxSwimlane.prototype=new mxShape();
mxSwimlane.prototype.constructor=mxSwimlane;
mxSwimlane.prototype.imageSize=16;
mxSwimlane.prototype.defaultStartSize=40;
mxSwimlane.prototype.mixedModeHtml=false;
mxRhombus.prototype.preferModeHtml=false;
mxSwimlane.prototype.createHtml=function()
{
var node=document.createElement('DIV');
this.configureHtmlShape(node);
node.style.background='';
node.style.backgroundColor='';
node.style.borderStyle='none';
this.label=document.createElement('DIV');
this.configureHtmlShape(this.label);
node.appendChild(this.label);
this.content=document.createElement('DIV');
var tmp=this.fill;
this.configureHtmlShape(this.content);
this.content.style.background='';
this.content.style.backgroundColor='';
if(mxUtils.getValue(this.style,mxConstants.STYLE_HORIZONTAL,true))
{
this.content.style.borderTopStyle='none';
}
else
{
this.content.style.borderLeftStyle='none';
}
this.content.style.cursor='default';
node.appendChild(this.content);
var color=this.style[mxConstants.STYLE_SEPARATORCOLOR];
if(color!=null)
{
this.separator=document.createElement('DIV');
this.separator.style.borderColor=color;
this.separator.style.borderLeftStyle='dashed';
node.appendChild(this.separator);
}
if(this.image!=null)
{
this.imageNode=mxUtils.createImage(this.image);
this.configureHtmlShape(this.imageNode);
this.imageNode.style.borderStyle='none';
node.appendChild(this.imageNode);
}
return node;
};
mxSwimlane.prototype.redrawHtml=function()
{
this.updateHtmlShape(this.node);
this.startSize=parseInt(this.style[mxConstants.STYLE_STARTSIZE])||this.defaultStartSize;
this.updateHtmlShape(this.label);
this.label.style.top='0px';
this.label.style.left='0px';
if(mxUtils.getValue(this.style,mxConstants.STYLE_HORIZONTAL,true))
{
this.startSize=Math.min(this.startSize,this.bounds.height);
this.label.style.height=(this.startSize*this.scale)+'px';
this.updateHtmlShape(this.content);
var h=this.startSize*this.scale;
this.content.style.top=h+'px';
this.content.style.left='0px';
this.content.style.height=Math.max(1,this.bounds.height-h)+'px';
if(this.separator!=null)
{
this.separator.style.left=Math.floor(this.bounds.width)+'px';
this.separator.style.top=Math.floor(this.startSize*this.scale)+'px';
this.separator.style.width='1px';
this.separator.style.height=Math.floor(this.bounds.height)+'px';
this.separator.style.borderWidth=Math.floor(this.scale)+'px';
}
if(this.imageNode!=null)
{
this.imageNode.style.left=(this.bounds.width-this.imageSize-4)+'px';
this.imageNode.style.top='0px';
this.imageNode.style.width=Math.floor(this.imageSize*this.scale)+'px';
this.imageNode.style.height=Math.floor(this.imageSize*this.scale)+'px';
}
}
else
{
this.startSize=Math.min(this.startSize,this.bounds.width);
this.label.style.width=(this.startSize*this.scale)+'px';
this.updateHtmlShape(this.content);
var w=this.startSize*this.scale;
this.content.style.top='0px';
this.content.style.left=w+'px';
this.content.style.width=Math.max(0,this.bounds.width-w)+'px';
if(this.separator!=null)
{
this.separator.style.left=Math.floor(this.startSize*this.scale)+'px';
this.separator.style.top=Math.floor(this.bounds.height)+'px';
this.separator.style.width=Math.floor(this.bounds.width)+'px';
this.separator.style.height='1px';
}
if(this.imageNode!=null)
{
this.imageNode.style.left=(this.bounds.width-this.imageSize-4)+'px';
this.imageNode.style.top='0px';
this.imageNode.style.width=this.imageSize*this.scale+'px';
this.imageNode.style.height=this.imageSize*this.scale+'px';
}
}
};
mxSwimlane.prototype.createVml=function()
{
var node=document.createElement('v:group');
var name=(this.isRounded)?'v:roundrect':'v:rect';
this.label=document.createElement(name);
this.configureVmlShape(this.label);
if(this.isRounded)
{
this.label.setAttribute('arcsize','20%');
}
this.isShadow=false;
this.configureVmlShape(node);
node.setAttribute('coordorigin','0,0');
node.appendChild(this.label);
this.content=document.createElement(name);
var tmp=this.fill;
this.fill=null;
this.configureVmlShape(this.content);
if(this.isRounded)
{
this.content.setAttribute('arcsize','4%');
}
this.fill=tmp;
this.content.style.borderBottom='0px';
node.appendChild(this.content);
var color=this.style[mxConstants.STYLE_SEPARATORCOLOR];
if(color!=null)
{
this.separator=document.createElement('v:polyline');
this.separator.setAttribute('strokecolor',color);
var strokeNode=document.createElement('v:stroke');
strokeNode.setAttribute('dashstyle','2 2');
this.separator.appendChild(strokeNode);
node.appendChild(this.separator);
}
if(this.image!=null)
{
this.imageNode=document.createElement('v:image');
this.imageNode.setAttribute('src',this.image);
this.configureVmlShape(this.imageNode);
node.appendChild(this.imageNode);
}
return node;
};
mxSwimlane.prototype.redrawVml=function()
{
var x=Math.floor(this.bounds.x);
var y=Math.floor(this.bounds.y);
var w=Math.floor(this.bounds.width);
var h=Math.floor(this.bounds.height);
this.updateVmlShape(this.node);
this.node.setAttribute('coordsize',w+','+h);
this.updateVmlShape(this.label);
this.label.style.top='0px';
this.label.style.left='0px';
this.label.style.rotation=null;
this.startSize=parseInt(this.style[mxConstants.STYLE_STARTSIZE])||this.defaultStartSize;
var start=Math.floor(this.startSize*this.scale);
if(mxUtils.getValue(this.style,mxConstants.STYLE_HORIZONTAL,true))
{
start=Math.min(start,this.bounds.height);
this.label.style.height=start+'px';
this.updateVmlShape(this.content);
this.content.style.top=start+'px';
this.content.style.left='0px';
this.content.style.height=Math.max(0,h-start)+'px';
if(this.separator!=null)
{
this.separator.points.value=w+','+start+' '+w+','+h;
}
if(this.imageNode!=null)
{
var img=Math.floor(this.imageSize*this.scale);
this.imageNode.style.left=(w-img-4)+'px';
this.imageNode.style.top='0px';
this.imageNode.style.width=img+'px';
this.imageNode.style.height=img+'px';
}
}
else
{
start=Math.min(start,this.bounds.width);
this.label.style.width=start+'px';
this.updateVmlShape(this.content);
this.content.style.top='0px';
this.content.style.left=start+'px';
this.content.style.width=Math.max(0,w-start)+'px';
if(this.separator!=null)
{
this.separator.points.value='0,'+h+' '+(w+start)+','+h;
}
if(this.imageNode!=null)
{
var img=Math.floor(this.imageSize*this.scale);
this.imageNode.style.left=(w-img-4)+'px';
this.imageNode.style.top='0px';
this.imageNode.style.width=img+'px';
this.imageNode.style.height=img+'px';
}
}
this.content.style.rotation=null;
};
mxSwimlane.prototype.createSvg=function()
{
var node=this.createSvgGroup('rect');
if(this.strokewidth*this.scale>=1&&!this.isRounded)
{
this.innerNode.setAttribute('shape-rendering','optimizeSpeed');
}
if(this.isRounded)
{
this.innerNode.setAttribute('rx',10);
this.innerNode.setAttribute('ry',10);
}
this.content=document.createElementNS(mxConstants.NS_SVG,'path');
this.configureSvgShape(this.content);
this.content.setAttribute('fill','none');
if(this.strokewidth*this.scale>=1&&!this.isRounded)
{
this.content.setAttribute('shape-rendering','optimizeSpeed');
}
if(this.isRounded)
{
this.content.setAttribute('rx',10);
this.content.setAttribute('ry',10);
}
node.appendChild(this.content);
var color=this.style[mxConstants.STYLE_SEPARATORCOLOR];
if(color!=null)
{
this.separator=document.createElementNS(mxConstants.NS_SVG,'line');
this.separator.setAttribute('stroke',color);
this.separator.setAttribute('fill','none');
this.separator.setAttribute('stroke-dasharray','2, 2');
this.separator.setAttribute('shape-rendering','optimizeSpeed');
node.appendChild(this.separator);
}
if(this.image!=null)
{
this.imageNode=document.createElementNS(mxConstants.NS_SVG,'image');
this.imageNode.setAttributeNS(mxConstants.NS_XLINK,'href',this.image);
this.configureSvgShape(this.imageNode);
node.appendChild(this.imageNode);
}
return node;
};
mxSwimlane.prototype.redrawSvg=function(){
var tmp=this.isRounded;
this.isRounded=false;
this.updateSvgShape(this.innerNode);
this.updateSvgShape(this.content);
if(this.shadowNode!=null)
{
this.updateSvgShape(this.shadowNode);
if(this.style[mxConstants.STYLE_HORIZONTAL])
{
this.shadowNode.setAttribute('width',this.startSize*this.scale);
}
else
{
this.shadowNode.setAttribute('height',this.startSize*this.scale);
}
}
this.isRounded=tmp;
this.startSize=parseInt(this.style[mxConstants.STYLE_STARTSIZE])||this.defaultStartSize;
if(mxUtils.getValue(this.style,mxConstants.STYLE_HORIZONTAL,true))
{
this.startSize=Math.min(this.startSize,this.bounds.height);
this.innerNode.setAttribute('height',this.startSize*this.scale);
var h=this.startSize*this.scale;
var points='M '+this.bounds.x+' '+(this.bounds.y+h)+' l 0 '+(this.bounds.height-h)+' l '+this.bounds.width+' 0'+' l 0 '+(-this.bounds.height+h);
this.content.setAttribute('d',points);
this.content.removeAttribute('x');
this.content.removeAttribute('y');
this.content.removeAttribute('width');
this.content.removeAttribute('height');
if(this.separator!=null)
{
this.separator.setAttribute('x1',this.bounds.x+this.bounds.width);
this.separator.setAttribute('y1',this.bounds.y+this.startSize*this.scale);
this.separator.setAttribute('x2',this.bounds.x+this.bounds.width);
this.separator.setAttribute('y2',this.bounds.y+this.bounds.height);
}
if(this.imageNode!=null)
{
this.imageNode.setAttribute('x',this.bounds.x+this.bounds.width-this.imageSize-4);
this.imageNode.setAttribute('y',this.bounds.y);
this.imageNode.setAttribute('width',this.imageSize*this.scale+'px');
this.imageNode.setAttribute('height',this.imageSize*this.scale+'px');
}
}
else
{
this.startSize=Math.min(this.startSize,this.bounds.width);
this.innerNode.setAttribute('width',this.startSize*this.scale);
var w=this.startSize*this.scale;
var points='M '+(this.bounds.x+w)+' '+this.bounds.y+' l '+(this.bounds.width-w)+' 0'+' l 0 '+this.bounds.height+' l '+(-this.bounds.width+w)+' 0';
this.content.setAttribute('d',points);
this.content.removeAttribute('x');
this.content.removeAttribute('y');
this.content.removeAttribute('width');
this.content.removeAttribute('height');
if(this.separator!=null)
{
this.separator.setAttribute('x1',this.bounds.x+this.startSize*this.scale);
this.separator.setAttribute('y1',this.bounds.y+this.bounds.height);
this.separator.setAttribute('x2',this.bounds.x+this.bounds.width);
this.separator.setAttribute('y2',this.bounds.y+this.bounds.height);
}
if(this.imageNode!=null)
{
this.imageNode.setAttribute('x',this.bounds.x+this.bounds.width-this.imageSize-4);
this.imageNode.setAttribute('y',this.bounds.y);
this.imageNode.setAttribute('width',this.imageSize*this.scale+'px');
this.imageNode.setAttribute('height',this.imageSize*this.scale+'px');
}
}
};
}

{
function mxGraphLayout(graph)
{
this.graph=graph;
};
mxGraphLayout.prototype.graph=null;
mxGraphLayout.prototype.useBoundingBox=true;
mxGraphLayout.prototype.move=function(cell,x,y){};
mxGraphLayout.prototype.execute=function(parent){};
mxGraphLayout.prototype.getGraph=function()
{
return this.graph;
};
mxGraphLayout.prototype.isVertexMovable=function(cell)
{
return this.graph.isMovable(cell);
};
mxGraphLayout.prototype.isVertexIgnored=function(vertex)
{
return!this.graph.getModel().isVertex(vertex)||!this.graph.isCellVisible(vertex);
};
mxGraphLayout.prototype.isEdgeIgnored=function(edge)
{
var model=this.graph.getModel();
return!model.isEdge(edge)||!this.graph.isCellVisible(edge)||model.getTerminal(edge,true)==null||model.getTerminal(edge,false)==null;
};
mxGraphLayout.prototype.setVertexLocation=function(cell,x,y)
{
var model=this.graph.getModel();
var geometry=model.getGeometry(cell);
var result=null;
if(geometry!=null)
{
result=new mxRectangle(x,y,geometry.width,geometry.height);
if(this.useBoundingBox)
{
var state=this.graph.getView().getState(cell);
if(state!=null&&state.text!=null&&state.text.boundingBox!=null&&state.text.boundingBox.x<state.x)
{
var scale=this.graph.getView().scale;
var box=state.text.boundingBox;
x+=(state.x-box.x)/scale;
result.width=box.width;
}
}
if(geometry.x!=x||geometry.y!=y)
{
geometry=geometry.clone();
geometry.x=x;
geometry.y=y;
model.setGeometry(cell,geometry);
}
}
return result;
};
mxGraphLayout.prototype.setEdgePoints=function(edge,points)
{
if(edge!=null)
{
var model=this.graph.model;
var geometry=model.getGeometry(edge);
if(geometry==null)
{
geometry=new mxGeometry();
geometry.setRelative(true);
}
else
{
geometry=geometry.clone();
}
geometry.points=points;
model.setGeometry(edge,geometry);
}
};
mxGraphLayout.prototype.getVertexBounds=function(cell)
{
var geo=this.graph.getModel().getGeometry(cell);

if(this.useBoundingBox)
{
var state=this.graph.getView().getState(cell);
if(state!=null&&state.text!=null&&state.text.boundingBox!=null)
{
var scale=this.graph.getView().scale;
var tmp=state.text.boundingBox;
var dx0=(tmp.x-state.x)/scale;
var dy0=(tmp.y-state.y)/scale;
var dx1=(tmp.x+tmp.width-state.x-state.width)/scale;
var dy1=(tmp.y+tmp.height-state.y-state.height)/scale;
geo=new mxRectangle(geo.x+dx0,geo.y+dy0,geo.width-dx0+dx1,geo.height-dy0+dy1);
}
}
return new mxRectangle(geo.x,geo.y,geo.width,geo.height);
};
}

{
function mxStackLayout(graph,horizontal,spacing,x0,y0)
{
mxGraphLayout.call(this,graph);
this.horizontal=(horizontal!=null)?horizontal:true;
this.spacing=(spacing!=null)?spacing:graph.gridSize;
this.x0=(x0!=null)?x0:this.spacing;
this.y0=(y0!=null)?y0:this.spacing;
};
mxStackLayout.prototype=new mxGraphLayout();
mxStackLayout.prototype.constructor=mxStackLayout;
mxStackLayout.prototype.horizontal=null;
mxStackLayout.prototype.spacing=null;
mxStackLayout.prototype.x0=null;
mxStackLayout.prototype.y0=null;
mxStackLayout.prototype.fill=false;
mxStackLayout.prototype.resizeParent=false;
mxStackLayout.prototype.wrap=null;
mxStackLayout.prototype.move=function(cell,x,y)
{
var model=this.graph.getModel();
var parent=model.getParent(cell);
if(cell!=null&&parent!=null)
{
var i=0;
var last=0;
var childCount=model.getChildCount(parent);
var value=(this.horizontal)?x:y;
var pstate=this.graph.getView().getState(parent);
if(pstate!=null)
{
value-=(this.horizontal)?pstate.x:pstate.y;
}
for(i=0;i<childCount;i++)
{
var child=model.getChildAt(parent,i);
if(child!=cell)
{
var bounds=model.getGeometry(child);
if(bounds!=null)
{
var tmp=(this.horizontal)?bounds.x+bounds.width/2:
bounds.y+bounds.height/2;
if(last<value&&tmp>value)
{
break;
}
last=tmp;
}
}
}
var idx=parent.getIndex(cell);
idx=Math.max(0,i-((i>idx)?1:0));
model.add(parent,cell,idx);
}
};
mxStackLayout.prototype.execute=function(parent)
{
if(parent!=null)
{
var x0=this.x0+1;
var y0=this.y0;
var model=this.graph.getModel();
var pgeo=model.getGeometry(parent);


if(this.graph.container!=null&&((pgeo==null&&model.isLayer(parent))||parent==this.graph.getView().currentRoot))
{
var width=this.graph.container.offsetWidth;
var height=this.graph.container.offsetHeight;
pgeo=new mxRectangle(0,0,width,height);
}
var fillValue=0;
if(pgeo!=null)
{
fillValue=(this.horizontal)?pgeo.height:pgeo.width;
}
fillValue-=2*this.spacing;
var size=this.graph.getStartSize(parent);
fillValue-=(this.horizontal)?size.height:size.width;
x0=this.x0+size.width;
y0=this.y0+size.height;
model.beginUpdate();
try
{
var tmp=0;
var last=null;
var childCount=model.getChildCount(parent);
for(var i=0;i<childCount;i++)
{
var child=model.getChildAt(parent,i);
if(!this.isVertexIgnored(child)&&this.isVertexMovable(child))
{
var geo=model.getGeometry(child);
if(geo!=null)
{
geo=geo.clone();
if(this.wrap!=null&&last!=null)
{
if((this.horizontal&&last.x+last.width+geo.width+2*this.spacing>this.wrap)||(!this.horizontal&&last.y+last.height+geo.height+2*this.spacing>this.wrap))
{
last=null;
if(this.horizontal)
{
y0+=tmp+this.spacing;
}
else
{
x0+=tmp+this.spacing;
}
tmp=0;
}
}
tmp=Math.max(tmp,(this.horizontal)?geo.height:geo.width);
if(last!=null)
{
if(this.horizontal)
{
geo.x=last.x+last.width+this.spacing;
}
else
{
geo.y=last.y+last.height+this.spacing;
}
}
else
{
if(this.horizontal)
{
geo.x=x0;
}
else
{
geo.y=y0;
}
}
if(this.horizontal)
{
geo.y=y0;
}
else
{
geo.x=x0;
}
if(this.fill&&fillValue>0)
{
if(this.horizontal)
{
geo.height=fillValue;
}
else
{
geo.width=fillValue;
}
}
model.setGeometry(child,geo);
last=geo;
}
}
}
if(this.resizeParent&&pgeo!=null&&last!=null&&!this.graph.isCellCollapsed(parent))
{
pgeo=pgeo.clone();
if(this.horizontal)
{
pgeo.width=last.x+last.width+this.spacing;
}
else
{
pgeo.height=last.y+last.height+this.spacing;
}
model.setGeometry(parent,pgeo);
}
}
finally
{
model.endUpdate();
}
}
};
}

{
function mxPartitionLayout(graph,horizontal,spacing,border)
{
mxGraphLayout.call(this,graph);
this.horizontal=(horizontal!=null)?horizontal:true;
this.spacing=spacing||0;
this.border=border||0;
};
mxPartitionLayout.prototype=new mxGraphLayout();
mxPartitionLayout.prototype.constructor=mxPartitionLayout;
mxPartitionLayout.prototype.horizontal=null;
mxPartitionLayout.prototype.spacing=null;
mxPartitionLayout.prototype.border=null;
mxPartitionLayout.prototype.resizeVertices=true;
mxPartitionLayout.prototype.move=function(cell,x,y)
{
var model=this.graph.getModel();
var parent=model.getParent(cell);
if(cell!=null&&parent!=null)
{
var i=0;
var last=0;
var childCount=model.getChildCount(parent);

for(i=0;i<childCount;i++)
{
var child=model.getChildAt(parent,i);
var bounds=this.getVertexBounds(child);
if(bounds!=null)
{
var tmp=bounds.x+bounds.width/2;
if(last<x&&tmp>x)
{
break;
}
last=tmp;
}
}
var idx=parent.getIndex(cell);
idx=Math.max(0,i-((i>idx)?1:0));
model.add(parent,cell,idx);
}
};
mxPartitionLayout.prototype.execute=function(parent)
{
var model=this.graph.getModel();
var pgeo=model.getGeometry(parent);


if(this.graph.container!=null&&((pgeo==null&&model.isLayer(parent))||parent==this.graph.getView().currentRoot))
{
var width=this.graph.container.offsetWidth;
var height=this.graph.container.offsetHeight;
pgeo=new mxRectangle(0,0,width,height);
}
if(pgeo!=null)
{
var children=new Array();
var childCount=model.getChildCount(parent);
for(var i=0;i<childCount;i++)
{
var child=model.getChildAt(parent,i);
if(!this.isVertexIgnored(child)&&this.isVertexMovable(child))
{
children.push(child);
}
}
var n=children.length;
if(n>0)
{
var x0=this.border;
var y0=this.border;
var other=(this.horizontal)?pgeo.height:pgeo.width;
other-=2*this.border;
var size=this.graph.getStartSize(parent);
other-=(this.horizontal)?size.height:size.width;
x0=x0+size.width;
y0=y0+size.height;
var tmp=this.border+(n-1)*this.spacing
var value=(this.horizontal)?((pgeo.width-x0-tmp)/n):((pgeo.height-y0-tmp)/n);

if(value>0)
{
model.beginUpdate();
try
{
for(var i=0;i<n;i++)
{
var child=children[i];
var geo=model.getGeometry(child);
if(geo!=null)
{
geo=geo.clone();
geo.x=x0;
geo.y=y0;
if(this.horizontal)
{
if(this.resizeVertices)
{
geo.width=value;
geo.height=other;
}
x0+=value+this.spacing;
}
else
{
if(this.resizeVertices)
{
geo.height=value;
geo.width=other;
}
y0+=value+this.spacing;
}
model.setGeometry(child,geo);
}
}
}
finally
{
model.endUpdate();
}
}
}
}
};
}

{
function mxCompactTreeLayout(graph,horizontal,invert)
{
mxGraphLayout.call(this,graph);
this.horizontal=(horizontal!=null)?horizontal:true;
this.invert=(invert!=null)?invert:false;
};
mxCompactTreeLayout.prototype=new mxGraphLayout();
mxCompactTreeLayout.prototype.constructor=mxCompactTreeLayout;
mxCompactTreeLayout.prototype.horizontal=null;
mxCompactTreeLayout.prototype.invert=null;
mxCompactTreeLayout.prototype.resizeParent=true;
mxCompactTreeLayout.prototype.moveTree=true;
mxCompactTreeLayout.prototype.levelDistance=10;
mxCompactTreeLayout.prototype.nodeDistance=20;
mxCompactTreeLayout.prototype.resetEdges=true;
mxCompactTreeLayout.prototype.isVertexIgnored=function(vertex)
{
return mxGraphLayout.prototype.isVertexIgnored.apply(this,arguments)||this.graph.getConnections(vertex).length==0;
};
mxCompactTreeLayout.prototype.execute=function(parent,root)
{
var model=this.graph.getModel();
if(root==null)
{
if(this.graph.getEdges(parent,model.getParent(parent),this.invert,!this.invert,false).length>0)
{
root=parent;
}

else
{
var roots=this.graph.findTreeRoots(parent,true,this.invert);
if(roots.length>0)
{
for(var i=0;i<roots.length;i++)
{
if(!this.isVertexIgnored(roots[i])&&this.graph.getEdges(roots[i],null,this.invert,!this.invert,false).length>0)
{
root=roots[i];
break;
}
}
}
}
}
if(root!=null)
{
parent=model.getParent(root);
model.beginUpdate();
try
{
var node=this.dfs(root,parent);
if(node!=null)
{
this.layout(node);
var x0=this.graph.gridSize;
var y0=x0;
if(!this.moveTree||model.isLayer(parent))
{
var g=model.getGeometry(root);
if(g!=null)
{
x0=g.x;
y0=g.y;
}
}
var bounds=null;
if(this.horizontal)
{
bounds=this.horizontalLayout(node,x0,y0);
}
else
{
bounds=this.verticalLayout(node,null,x0,y0);
}
if(bounds!=null)
{
var dx=0;
var dy=0;
if(bounds.x<0)
{
dx=Math.abs(x0-bounds.x);
}
if(bounds.y<0)
{
dy=Math.abs(y0-bounds.y);
}
if(parent!=null)
{
var size=this.graph.getStartSize(parent);
dx+=size.width;
dy+=size.height;
if(this.resizeParent&&!this.graph.isCellCollapsed(parent))
{
var g=model.getGeometry(parent);
if(g!=null)
{
var width=bounds.width+size.width-bounds.x+2*x0;
var height=bounds.height+size.height-bounds.y+2*y0;
g=g.clone();
if(g.width>width)
{
dx+=(g.width-width)/2;
}
else
{
g.width=width;
}
if(g.height>height)
{
if(this.horizontal)
{
dy+=(g.height-height)/2;
}
}
else
{
g.height=height;
}
model.setGeometry(parent,g);
}
}
}
this.moveNode(node,dx,dy);
}
}
}
finally
{
model.endUpdate();
}
}
};
mxCompactTreeLayout.prototype.moveNode=function(node,dx,dy)
{
node.x+=dx;
node.y+=dy;
this.apply(node);
var child=node.child;
while(child!=null)
{
this.moveNode(child,dx,dy);
child=child.next;
}
};
mxCompactTreeLayout.prototype.dfs=function(cell,parent,visited)
{
visited=visited||new Array();
var id=mxCellPath.create(cell);
var node=null;
if(cell!=null&&visited[id]==null&&!this.isVertexIgnored(cell))
{
visited[id]=cell;
node=this.createNode(cell);
var model=this.graph.getModel();
var prev=null;
var out=this.graph.getEdges(cell,parent,this.invert,!this.invert,false);
for(var i=0;i<out.length;i++)
{
var edge=out[i];
if(!this.isEdgeIgnored(edge))
{
if(this.resetEdges)
{
this.setEdgePoints(edge,null);
}
var target=this.graph.getView().getVisibleTerminal(edge,this.invert);
var tmp=this.dfs(target,parent,visited);
if(tmp!=null&&model.getGeometry(target)!=null)
{
if(prev==null)
{
node.child=tmp;
}
else
{
prev.next=tmp;
}
prev=tmp;
}
}
}
}
return node;
};
mxCompactTreeLayout.prototype.layout=function(node)
{
if(node!=null)
{
var child=node.child;
while(child!=null)
{
this.layout(child);
child=child.next;
}
if(node.child!=null)
{
this.attachParent(node,this.join(node));
}
else
{
this.layoutLeaf(node);
}
}
};
mxCompactTreeLayout.prototype.horizontalLayout=function(node,x0,y0,bounds)
{
node.x+=x0+node.offsetX;
node.y+=y0+node.offsetY;
bounds=this.apply(node,bounds);
var child=node.child;
if(child!=null)
{
bounds=this.horizontalLayout(child,node.x,node.y,bounds);
var siblingOffset=node.y+child.offsetY;
var s=child.next;
while(s!=null)
{
bounds=this.horizontalLayout(s,node.x+child.offsetX,siblingOffset,bounds);
siblingOffset+=s.offsetY;
s=s.next;
}
}
return bounds;
};
mxCompactTreeLayout.prototype.verticalLayout=function(node,parent,x0,y0,bounds)
{
node.x+=x0+node.offsetY;
node.y+=y0+node.offsetX;
bounds=this.apply(node,bounds);
var child=node.child;
if(child!=null)
{
bounds=this.verticalLayout(child,node,node.x,node.y,bounds);
var siblingOffset=node.x+child.offsetY;
var s=child.next;
while(s!=null)
{
bounds=this.verticalLayout(s,node,siblingOffset,node.y+child.offsetX,bounds);
siblingOffset+=s.offsetY;
s=s.next;
}
}
return bounds;
};
mxCompactTreeLayout.prototype.attachParent=function(node,height)
{
var x=this.nodeDistance+this.levelDistance;
var y2=(height-node.width)/2-this.nodeDistance;
var y1=y2+node.width+2*this.nodeDistance-height;
node.child.offsetX=x+node.height;
node.child.offsetY=y1;
node.contour.upperHead=this.createLine(node.height,0,this.createLine(x,y1,node.contour.upperHead));
node.contour.lowerHead=this.createLine(node.height,0,this.createLine(x,y2,node.contour.lowerHead));
};
mxCompactTreeLayout.prototype.layoutLeaf=function(node)
{
var dist=2*this.nodeDistance;
node.contour.upperTail=this.createLine(node.height+dist,0);
node.contour.upperHead=node.contour.upperTail;
node.contour.lowerTail=this.createLine(0,-node.width-dist);
node.contour.lowerHead=this.createLine(node.height+dist,0,node.contour.lowerTail);
};
mxCompactTreeLayout.prototype.join=function(node)
{
var dist=2*this.nodeDistance;
var child=node.child;
node.contour=child.contour;
var h=child.width+dist;
var sum=h;
child=child.next;
while(child!=null)
{
var d=this.merge(node.contour,child.contour);
child.offsetY=d+h;
child.offsetX=0;
h=child.width+dist;
sum+=d+h;
child=child.next;
}
return sum;
};
mxCompactTreeLayout.prototype.merge=function(p1,p2)
{
var x=0;
var y=0;
var total=0;
var upper=p1.lowerHead;
var lower=p2.upperHead;
while(lower!=null&&upper!=null)
{
var d=this.offset(x,y,lower.dx,lower.dy,upper.dx,upper.dy);
y+=d;
total+=d;
if(x+lower.dx<=upper.dx)
{
x+=lower.dx;
y+=lower.dy;
lower=lower.next;
}
else
{
x-=upper.dx;
y-=upper.dy;
upper=upper.next;
}
}
if(lower!=null)
{
var b=this.bridge(p1.upperTail,0,0,lower,x,y);
p1.upperTail=(b.next!=null)?p2.upperTail:b;
p1.lowerTail=p2.lowerTail;
}
else
{
var b=this.bridge(p2.lowerTail,x,y,upper,0,0);
if(b.next==null)
{
p1.lowerTail=b;
}
}
p1.lowerHead=p2.lowerHead;
return total;
};
mxCompactTreeLayout.prototype.offset=function(p1,p2,a1,a2,b1,b2)
{
var d=0;
if(b1<=p1||p1+a1<=0)
{
return 0;
}
var t=b1*a2-a1*b2;
if(t>0)
{
if(p1<0)
{
var s=p1*a2;
d=s/a1-p2;
}
else if(p1>0)
{
var s=p1*b2;
d=s/b1-p2;
}
else
{
d=-p2;
}
}
else if(b1<p1+a1)
{
var s=(b1-p1)*a2;
d=b2-(p2+s/a1);
}
else if(b1>p1+a1)
{
var s=(a1+p1)*b2;
d=s/b1-(p2+a2);
}
else
{
d=b2-(p2+a2);
}
if(d>0)
{
return d;
}
else
{
return 0;
}
};
mxCompactTreeLayout.prototype.bridge=function(line1,x1,y1,line2,x2,y2)
{
var dx=x2+line2.dx-x1;
var dy=0;
var s=0;
if(line2.dx==0)
{
dy=line2.dy;
}
else
{
var s=dx*line2.dy;
dy=s/line2.dx;
}
var r=this.createLine(dx,dy,line2.next);
line1.next=this.createLine(0,y2+line2.dy-dy-y1,r);
return r;
};
mxCompactTreeLayout.prototype.createNode=function(cell)
{
var node=new Object();
node.cell=cell;
node.x=0;
node.y=0;
node.width=0;
node.height=0;
var geo=this.getVertexBounds(cell);
if(geo!=null)
{
if(this.horizontal)
{
node.width=geo.height;
node.height=geo.width;
}
else
{
node.width=geo.width;
node.height=geo.height;
}
}
node.offsetX=0;
node.offsetY=0;
node.contour=new Object();
return node;
};
mxCompactTreeLayout.prototype.apply=function(node,bounds)
{
var g=this.graph.getModel().getGeometry(node.cell);
if(node.cell!=null&&g!=null)
{
if(this.isVertexMovable(node.cell))
{
g=this.setVertexLocation(node.cell,node.x,node.y);
}
if(bounds==null)
{
bounds=new mxRectangle(g.x,g.y,g.width,g.height);
}
else
{
bounds=new mxRectangle(Math.min(bounds.x,g.x),Math.min(bounds.y,g.y),Math.max(bounds.x+bounds.width,g.x+g.width),Math.max(bounds.y+bounds.height,g.y+g.height));
}
}
return bounds;
};
mxCompactTreeLayout.prototype.createLine=function(dx,dy,next)
{
var line=new Object();
line.dx=dx;
line.dy=dy;
line.next=next;
return line;
};
}

{
function mxFastOrganicLayout(graph)
{
mxGraphLayout.call(this,graph);
};
mxFastOrganicLayout.prototype=new mxGraphLayout();
mxFastOrganicLayout.prototype.constructor=mxFastOrganicLayout;
mxFastOrganicLayout.prototype.useInputOrigin=true;
mxFastOrganicLayout.prototype.resetEdges=true;
mxFastOrganicLayout.prototype.forceConstant=50;
mxFastOrganicLayout.prototype.forceConstantSquared=0;
mxFastOrganicLayout.prototype.minDistanceLimit=2;
mxFastOrganicLayout.prototype.minDistanceLimitSquared=4;
mxFastOrganicLayout.prototype.initialTemp=200;
mxFastOrganicLayout.prototype.temperature=0;
mxFastOrganicLayout.prototype.maxIterations=0;
mxFastOrganicLayout.prototype.iteration=0;
mxFastOrganicLayout.prototype.vertexArray;
mxFastOrganicLayout.prototype.dispX;
mxFastOrganicLayout.prototype.dispY;
mxFastOrganicLayout.prototype.cellLocation;
mxFastOrganicLayout.prototype.radius;
mxFastOrganicLayout.prototype.radiusSquared;
mxFastOrganicLayout.prototype.isMoveable;
mxFastOrganicLayout.prototype.neighbours;
mxFastOrganicLayout.prototype.indices;
mxFastOrganicLayout.prototype.allowedToRun=true;
mxFastOrganicLayout.prototype.isVertexIgnored=function(vertex)
{
return mxGraphLayout.prototype.isVertexIgnored.apply(this,arguments)||this.graph.getConnections(vertex).length==0;
};
mxFastOrganicLayout.prototype.execute=function(parent)
{
var model=this.graph.getModel();
this.vertexArray=new Array();
var cells=this.graph.getChildVertices(parent);
for(var i=0;i<cells.length;i++)
{
if(!this.isVertexIgnored(cells[i]))
{
this.vertexArray.push(cells[i]);
}
}
var initialBounds=(this.useInputOrigin)?this.graph.view.getBounds(this.vertexArray):
null;
var n=this.vertexArray.length;
this.indices=new Array();
this.dispX=new Array();
this.dispY=new Array();
this.cellLocation=new Array();
this.isMoveable=new Array();
this.neighbours=new Array();
this.radius=new Array();
this.radiusSquared=new Array();
if(this.forceConstant<0.001)
{
this.forceConstant=0.001;
}
this.forceConstantSquared=this.forceConstant*this.forceConstant;



for(var i=0;i<this.vertexArray.length;i++)
{
var vertex=this.vertexArray[i];
this.cellLocation[i]=new Array();
var id=mxCellPath.create(vertex);
this.indices[id]=i;
var bounds=this.getVertexBounds(vertex);

var width=bounds.width;
var height=bounds.height;
var x=bounds.x;
var y=bounds.y;
this.cellLocation[i][0]=x+width/2.0;
this.cellLocation[i][1]=y+height/2.0;
this.radius[i]=Math.min(width,height);
this.radiusSquared[i]=this.radius[i]*this.radius[i];
}
for(var i=0;i<n;i++)
{
this.dispX[i]=0;
this.dispY[i]=0;
this.isMoveable[i]=this.isVertexMovable(this.vertexArray[i]);


var edges=this.graph.getConnections(this.vertexArray[i],parent);
var cells=this.graph.getOpposites(edges,this.vertexArray[i]);
this.neighbours[i]=new Array();
for(var j=0;j<cells.length;j++)
{
if(this.resetEdges)
{
this.setEdgePoints(edges[j],null);
}
var id=mxCellPath.create(cells[j]);
var index=this.indices[id];

if(index!=null)
{
this.neighbours[i][j]=index;
}



else
{
this.neighbours[i][j]=i;
}
}
}
this.temperature=this.initialTemp;
if(this.maxIterations==0)
{
this.maxIterations=20*Math.sqrt(n);
}
for(this.iteration=0;this.iteration<this.maxIterations;this.iteration++)
{
if(!this.allowedToRun)
{
return;
}
this.calcRepulsion();
this.calcAttraction();
this.calcPositions();
this.reduceTemperature();
}

model.beginUpdate();
try
{
var minx=null;
var miny=null;
for(var i=0;i<this.vertexArray.length;i++)
{
var vertex=this.vertexArray[i];
if(this.isVertexMovable(vertex))
{
var bounds=this.getVertexBounds(vertex);
if(bounds!=null)
{
this.cellLocation[i][0]-=bounds.width/2.0;
this.cellLocation[i][1]-=bounds.height/2.0;
var x=this.graph.snap(this.cellLocation[i][0]);
var y=this.graph.snap(this.cellLocation[i][1]);
this.setVertexLocation(vertex,x,y);
if(minx==null)
{
minx=x;
}
else
{
minx=Math.min(minx,x);
}
if(miny==null)
{
miny=y;
}
else
{
miny=Math.min(miny,y);
}
}
}
}


var dx=-(minx||0)+1;
var dy=-(miny||0)+1;
if(initialBounds!=null)
{
dx+=initialBounds.x;
dy+=initialBounds.y;
}
this.graph.move(this.vertexArray,dx,dy);
}
finally
{
model.endUpdate();
}
};
mxFastOrganicLayout.prototype.calcPositions=function()
{
for(var index=0;index<this.vertexArray.length;index++)
{
if(this.isMoveable[index])
{

var deltaLength=Math.sqrt(this.dispX[index]*this.dispX[index]+this.dispY[index]*this.dispY[index]);
if(deltaLength<0.001)
{
deltaLength=0.001;
}

var newXDisp=this.dispX[index]/deltaLength*Math.min(deltaLength,this.temperature);
var newYDisp=this.dispY[index]/deltaLength*Math.min(deltaLength,this.temperature);
this.dispX[index]=0;
this.dispY[index]=0;
this.cellLocation[index][0]+=newXDisp;
this.cellLocation[index][1]+=newYDisp;
}
}
};
mxFastOrganicLayout.prototype.calcAttraction=function()
{

for(var i=0;i<this.vertexArray.length;i++)
{
for(var k=0;k<this.neighbours[i].length;k++)
{
var j=this.neighbours[i][k];
if(i!=j&&this.isMoveable[i]&&this.isMoveable[j])
{
var xDelta=this.cellLocation[i][0]-this.cellLocation[j][0];
var yDelta=this.cellLocation[i][1]-this.cellLocation[j][1];
var deltaLengthSquared=xDelta*xDelta+yDelta*yDelta-this.radiusSquared[i]-this.radiusSquared[j];
if(deltaLengthSquared<this.minDistanceLimitSquared)
{
deltaLengthSquared=this.minDistanceLimitSquared;
}
var deltaLength=Math.sqrt(deltaLengthSquared);
var force=(deltaLengthSquared)/this.forceConstant;
var displacementX=(xDelta/deltaLength)*force;
var displacementY=(yDelta/deltaLength)*force;
this.dispX[i]-=displacementX;
this.dispY[i]-=displacementY;
this.dispX[j]+=displacementX;
this.dispY[j]+=displacementY;
}
}
}
};
mxFastOrganicLayout.prototype.calcRepulsion=function()
{
var vertexCount=this.vertexArray.length;
for(var i=0;i<vertexCount;i++)
{
for(var j=i;j<vertexCount;j++)
{
if(!this.allowedToRun)
{
return;
}
if(j!=i&&this.isMoveable[i]&&this.isMoveable[j])
{
var xDelta=this.cellLocation[i][0]-this.cellLocation[j][0];
var yDelta=this.cellLocation[i][1]-this.cellLocation[j][1];
if(xDelta==0)
{
xDelta=0.01+Math.random();
}
if(yDelta==0)
{
yDelta=0.01+Math.random();
}
var deltaLength=Math.sqrt((xDelta*xDelta)+(yDelta*yDelta));
var deltaLengthWithRadius=deltaLength-this.radius[i]-this.radius[j];
if(deltaLengthWithRadius<this.minDistanceLimit)
{
deltaLengthWithRadius=this.minDistanceLimit;
}
var force=this.forceConstantSquared/deltaLengthWithRadius;
var displacementX=(xDelta/deltaLength)*force;
var displacementY=(yDelta/deltaLength)*force;
this.dispX[i]+=displacementX;
this.dispY[i]+=displacementY;
this.dispX[j]-=displacementX;
this.dispY[j]-=displacementY;
}
}
}
};
mxFastOrganicLayout.prototype.reduceTemperature=function()
{
this.temperature=this.initialTemp*(1.0-this.iteration/this.maxIterations);
};
}

{
function mxCircleLayout(graph,radius)
{
mxGraphLayout.call(this,graph);
this.radius=(radius!=null)?radius:100;
};
mxCircleLayout.prototype=new mxGraphLayout();
mxCircleLayout.prototype.constructor=mxCircleLayout;
mxCircleLayout.prototype.radius=null;
mxCircleLayout.prototype.execute=function(parent)
{
var model=this.graph.getModel();

var max=0;
var vertices=new Array();
var childCount=model.getChildCount(parent);
for(var i=0;i<childCount;i++)
{
var cell=model.getChildAt(parent,i);
if(!this.isVertexIgnored(cell))
{
vertices.push(cell);
var bounds=this.getVertexBounds(cell);
max=Math.max(max,Math.max(bounds.width,bounds.height));
}
}
var vertexCount=vertices.length;
var r=Math.max(vertexCount*max/Math.PI,this.radius);
this.circle(vertices,r);
};
mxCircleLayout.prototype.circle=function(vertices,r)
{
var model=this.graph.getModel();


model.beginUpdate();
try
{
var vertexCount=vertices.length;
var phi=2*Math.PI/vertexCount;
for(var i=0;i<vertexCount;i++)
{
if(this.isVertexMovable(vertices[i]))
{
this.setVertexLocation(vertices[i],r+r*Math.sin(i*phi),r+r*Math.cos(i*phi));
}
}
}
finally
{
model.endUpdate();
}
};
}

{
function mxParallelEdgeLayout(graph)
{
mxGraphLayout.call(this,graph);
};
mxParallelEdgeLayout.prototype=new mxGraphLayout();
mxParallelEdgeLayout.prototype.constructor=mxParallelEdgeLayout;
mxParallelEdgeLayout.prototype.spacing=20;
mxParallelEdgeLayout.prototype.execute=function(parent)
{
var lookup=this.findParallels(parent);
this.graph.model.beginUpdate();
try
{
for(var i in lookup)
{
var parallels=lookup[i];
if(parallels.length>1)
{
this.layout(parallels);
}
}
}
finally
{
this.graph.model.endUpdate();
}
};
mxParallelEdgeLayout.prototype.findParallels=function(parent)
{
var view=this.graph.getView();
var model=this.graph.getModel();
var lookup=new Array();
var childCount=model.getChildCount(parent);
for(var i=0;i<childCount;i++)
{
var child=model.getChildAt(parent,i);
if(!this.isEdgeIgnored(child))
{
var src=view.getVisibleTerminal(child,true);
var trg=view.getVisibleTerminal(child,false);
if(src!=null&&trg!=null)
{
src=mxCellPath.create(src);
trg=mxCellPath.create(trg);
var id=(src>trg)?trg+'-'+src:src+'-'+trg;
if(lookup[id]==null)
{
lookup[id]=new Array();
}
lookup[id].push(child);
}
}
}
return lookup;
};
mxParallelEdgeLayout.prototype.layout=function(parallels)
{
var edge=parallels[0];
var view=this.graph.getView();
var model=this.graph.getModel();
var src=model.getGeometry(model.getTerminal(edge,true));
var trg=model.getGeometry(model.getTerminal(edge,false));
if(src==trg)
{
var x0=src.x+src.width+this.spacing;
var y0=src.y+src.height/2;
for(var i=0;i<parallels.length;i++)
{
this.route(parallels[i],x0,y0);
x0+=this.spacing;
}
}
else if(src!=null&&trg!=null)
{
var scx=src.x+src.width/2;
var scy=src.y+src.height/2;
var tcx=trg.x+trg.width/2;
var tcy=trg.y+trg.height/2;
var dx=tcx-scx;
var dy=tcy-scy;
var len=Math.sqrt(dx*dx+dy*dy);
var x0=scx+dx/2;
var y0=scy+dy/2;
var nx=dy*this.spacing/len;
var ny=dx*this.spacing/len;
x0+=nx*(parallels.length-1)/2;
y0-=ny*(parallels.length-1)/2;
for(var i=0;i<parallels.length;i++)
{
this.route(parallels[i],x0,y0);
x0-=nx;
y0+=ny;
}
}
};
mxParallelEdgeLayout.prototype.route=function(edge,x,y)
{
if(this.graph.isMovable(edge))
{
this.setEdgePoints(edge,[new mxPoint(x,y)]);
}
};
}

{
function mxCompositeLayout(graph,layouts,master)
{
mxGraphLayout.call(this,graph);
this.layouts=layouts;
this.master=master;
};
mxCompositeLayout.prototype=new mxGraphLayout();
mxCompositeLayout.prototype.constructor=mxCompositeLayout;
mxCompositeLayout.prototype.layouts=null;
mxCompositeLayout.prototype.master=null;
mxCompositeLayout.prototype.move=function(cell,x,y)
{
if(this.master!=null)
{
this.master.move.apply(this.master,arguments);
}
else
{
this.layouts[0].move.apply(this.layouts[0],arguments);
}
};
mxCompositeLayout.prototype.execute=function(parent)
{
var model=this.graph.getModel();
model.beginUpdate();
try
{
for(var i=0;i<this.layouts.length;i++)
{
this.layouts[i].execute.apply(this.layouts[i],arguments);
}
}
finally
{
model.endUpdate();
}
};
}

{
function mxGraphAbstractHierarchyCell()
{
this.x=new Array();
this.y=new Array();
this.temp=new Array();
};
mxGraphAbstractHierarchyCell.prototype.maxRank=-1;
mxGraphAbstractHierarchyCell.prototype.minRank=-1;
mxGraphAbstractHierarchyCell.prototype.x=null;
mxGraphAbstractHierarchyCell.prototype.y=null;
mxGraphAbstractHierarchyCell.prototype.width=0;
mxGraphAbstractHierarchyCell.prototype.height=0;
mxGraphAbstractHierarchyCell.prototype.nextLayerConnectedCells=null;
mxGraphAbstractHierarchyCell.prototype.previousLayerConnectedCells=null;
mxGraphAbstractHierarchyCell.prototype.temp=null;
mxGraphAbstractHierarchyCell.prototype.getNextLayerConnectedCells=function(layer)
{
return null;
};
mxGraphAbstractHierarchyCell.prototype.getPreviousLayerConnectedCells=function(layer)
{
return null;
};
mxGraphAbstractHierarchyCell.prototype.isEdge=function()
{
return false;
};
mxGraphAbstractHierarchyCell.prototype.isVertex=function()
{
return false;
};
mxGraphAbstractHierarchyCell.prototype.getGeneralPurposeVariable=function(layer)
{
return null;
};
mxGraphAbstractHierarchyCell.prototype.setGeneralPurposeVariable=function(layer,value)
{
return null;
};
mxGraphAbstractHierarchyCell.prototype.setX=function(layer,value)
{
if(this.isVertex())
{
this.x[0]=value;
}
else if(this.isEdge())
{
this.x[layer-this.minRank-1]=value;
}
};
mxGraphAbstractHierarchyCell.prototype.getX=function(layer)
{
if(this.isVertex())
{
return this.x[0];
}
else if(this.isEdge())
{
return this.x[layer-this.minRank-1];
}
return 0.0;
};
mxGraphAbstractHierarchyCell.prototype.setY=function(layer,value)
{
if(this.isVertex())
{
this.y[0]=value;
}
else if(this.isEdge())
{
this.y[layer-this.minRank-1]=value;
}
};
}

{
function mxGraphHierarchyNode(cell)
{
mxGraphAbstractHierarchyCell.apply(this,arguments);
this.cell=cell;
};
mxGraphHierarchyNode.prototype=new mxGraphAbstractHierarchyCell();
mxGraphHierarchyNode.prototype.constructor=mxGraphHierarchyNode;
mxGraphHierarchyNode.prototype.cell=null;
mxGraphHierarchyNode.prototype.connectsAsTarget=new Array();
mxGraphHierarchyNode.prototype.connectsAsSource=new Array();
mxGraphHierarchyNode.prototype.hashCode=false;
mxGraphHierarchyNode.prototype.getRankValue=function(layer)
{
return this.maxRank;
};
mxGraphHierarchyNode.prototype.getNextLayerConnectedCells=function(layer)
{
if(this.nextLayerConnectedCells==null)
{
this.nextLayerConnectedCells=new Array();
this.nextLayerConnectedCells[0]=new Array();
for(var i=0;i<this.connectsAsTarget.length;i++)
{
var edge=this.connectsAsTarget[i];
if(edge.maxRank==-1||edge.maxRank==layer+1)
{

this.nextLayerConnectedCells[0].push(edge.source);
}
else
{
this.nextLayerConnectedCells[0].push(edge);
}
}
}
return this.nextLayerConnectedCells[0];
};
mxGraphHierarchyNode.prototype.getPreviousLayerConnectedCells=function(layer)
{
if(this.previousLayerConnectedCells==null)
{
this.previousLayerConnectedCells=new Array();
this.previousLayerConnectedCells[0]=new Array();
for(var i=0;i<this.connectsAsSource.length;i++)
{
var edge=this.connectsAsSource[i];
if(edge.minRank==-1||edge.minRank==layer-1)
{
this.previousLayerConnectedCells[0].push(edge.target);
}
else
{
this.previousLayerConnectedCells[0].push(edge);
}
}
}
return this.previousLayerConnectedCells[0];
};
mxGraphHierarchyNode.prototype.isVertex=function()
{
return true;
};
mxGraphHierarchyNode.prototype.getGeneralPurposeVariable=function(layer)
{
return this.temp[0];
};
mxGraphHierarchyNode.prototype.setGeneralPurposeVariable=function(layer,value)
{
this.temp[0]=value;
};
mxGraphHierarchyNode.prototype.isAncestor=function(otherNode)
{

if(otherNode!=null&&this.hashCode!=null&&otherNode.hashCode!=null&&this.hashCode.length<otherNode.hashCode.length)
{
if(this.hashCode==otherNode.hashCode)
{
return true;
}
if(this.hashCode==null||this.hashCode==null)
{
return false;
}



for(var i=0;i<this.hashCode.length;i++)
{
if(this.hashCode[i]!=otherNode.hashCode[i])
{
return false;
}
}
return true;
}
return false;
};
}

{
function mxGraphHierarchyEdge(edges)
{
mxGraphAbstractHierarchyCell.apply(this,arguments);
this.edges=edges;
};
mxGraphHierarchyEdge.prototype=new mxGraphAbstractHierarchyCell();
mxGraphHierarchyEdge.prototype.constructor=mxGraphHierarchyEdge;
mxGraphHierarchyEdge.prototype.edges=null;
mxGraphHierarchyEdge.prototype.source=null;
mxGraphHierarchyEdge.prototype.target=null;
mxGraphHierarchyEdge.prototype.isReversed=false;
mxGraphHierarchyEdge.prototype.invert=function(layer)
{
var temp=this.source;
this.source=this.target;
this.target=temp;
this.isReversed=!this.isReversed;
};
mxGraphHierarchyEdge.prototype.getNextLayerConnectedCells=function(layer)
{
if(this.nextLayerConnectedCells==null)
{
this.nextLayerConnectedCells=new Array();
for(var i=0;i<this.temp.length;i++)
{
this.nextLayerConnectedCells[i]=new Array();
if(i==this.nextLayerConnectedCells.length-1)
{
this.nextLayerConnectedCells[i].push(this.source);
}
else
{
this.nextLayerConnectedCells[i].push(this);
}
}
}
return this.nextLayerConnectedCells[layer-this.minRank-1];
};
mxGraphHierarchyEdge.prototype.getPreviousLayerConnectedCells=function(layer)
{
if(this.previousLayerConnectedCells==null)
{
this.previousLayerConnectedCells=new Array();
for(var i=0;i<this.temp.length;i++)
{
this.previousLayerConnectedCells[i]=new Array();
if(i==0)
{
this.previousLayerConnectedCells[i].push(this.target);
}
else
{
this.previousLayerConnectedCells[i].push(this);
}
}
}
return this.previousLayerConnectedCells[layer-this.minRank-1];
};
mxGraphHierarchyEdge.prototype.isEdge=function()
{
return true;
};
mxGraphHierarchyEdge.prototype.getGeneralPurposeVariable=function(layer)
{
return this.temp[layer-this.minRank-1];
};
mxGraphHierarchyEdge.prototype.setGeneralPurposeVariable=function(layer,value)
{
this.temp[layer-this.minRank-1]=value;
};
}

{
function mxGraphHierarchyModel(layout,vertices,roots,parent,ordered,deterministic)
{
var graph=layout.getGraph();
this.deterministic=deterministic;
this.roots=roots;

this.vertexMapper=new Object();
this.edgeMapper=new Object();
this.maxRank=0;
var internalVertices=new Array();
if(vertices==null)
{
vertices=this.graph.getChildVertices(parent);
}
if(ordered)
{
this.formOrderedHierarchy(layout,vertices,parent);
}
else
{


this.createInternalCells(layout,vertices,internalVertices);

for(var i=0;i<vertices.length;i++)
{
var edges=internalVertices[i].connectsAsSource;
for(var j=0;j<edges.length;j++)
{
var internalEdge=edges[j];
var realEdges=internalEdge.edges;
for(var k=0;k<realEdges.length;k++)
{
var realEdge=realEdges[k];
var targetCell=graph.getView().getVisibleTerminal(realEdge,false);
var targetCellId=mxCellPath.create(targetCell);
var internalTargetCell=this.vertexMapper[targetCellId];
if(internalTargetCell!=null&&internalVertices[i]!=internalTargetCell)
{
internalEdge.target=internalTargetCell;
if(internalTargetCell.connectsAsTarget.length==0)
{
internalTargetCell.connectsAsTarget=new Array();
}
if(mxUtils.indexOf(internalTargetCell.connectsAsTarget,internalEdge)<0)
{
internalTargetCell.connectsAsTarget.push(internalEdge);
}
}
}
}

internalVertices[i].temp[0]=1;
}
}
};
mxGraphHierarchyModel.prototype.sinksAtLayerZero=true;
mxGraphHierarchyModel.prototype.maxRank=null;
mxGraphHierarchyModel.prototype.vertexMapper=null;
mxGraphHierarchyModel.prototype.edgeMapper=null;
mxGraphHierarchyModel.prototype.ranks=null;
mxGraphHierarchyModel.prototype.roots=null;
mxGraphHierarchyModel.prototype.parent=null;
mxGraphHierarchyModel.prototype.dfsCount=0;
mxGraphHierarchyModel.prototype.deterministic;
mxGraphHierarchyModel.prototype.formOrderedHierarchy=function(layout,vertices,parent)
{
var graph=layout.getGraph();
this.createInternalCells(layout,vertices,internalVertices);





var tempList=new Array();
for(var i=0;i<vertices.length;i++)
{
var edges=internalVertices[i].connectsAsSource;
for(var j=0;j<edges.length;j++)
{
var internalEdge=edges[j];
var realEdges=internalEdge.edges;
for(var k=0;k<realEdges.length;k++)
{
var realEdge=realEdges[k];
var targetCell=this.graph.getView().getVisibleTerminal(realEdge,false);
var targetCellId=mxCellPath.create(targetCell);
var internalTargetCell=vertexMapper[targetCellId];
if(internalTargetCell!=null&&internalVertices[i]!=internalTargetCell)
{
internalEdge.target=internalTargetCell;
if(internalTargetCell.connectsAsTarget.length==0)
{
internalTargetCell.connectsAsTarget=new Array();
}

if(internalTargetCell.temp[0]==1)
{
internalEdge.invert();
internalTargetCell.connectsAsSource.push(internalEdge);
tempList.push(internalEdge);
if(mxUtils.indexOf(internalVertices[i].connectsAsTarget,internalEdge)<0)
{
internalVertices[i].connectsAsTarget.push(internalEdge);
}
}
else
{
if(mxUtils.indexOf(internalTargetCell.connectsAsTarget,internalEdge)<0)
{
internalTargetCell.connectsAsTarget.push(internalEdge);
}
}
}
}
}
for(var j=0;j<tempList.length;j++)
{
var tmp=tempList[j];
mxUtils.remove(tmp,internalVertices[i].connectsAsSource);
}
tempList=new Array();

internalVertices[i].temp[0]=1;
}
};
mxGraphHierarchyModel.prototype.createInternalCells=function(layout,vertices,internalVertices)
{
var graph=layout.getGraph();
for(var i=0;i<vertices.length;i++)
{
internalVertices[i]=new mxGraphHierarchyNode(vertices[i]);
var vertexId=mxCellPath.create(vertices[i]);
this.vertexMapper[vertexId]=internalVertices[i];

var conns=graph.getConnections(vertices[i],this.parent);
var outgoingCells=graph.getOpposites(conns,vertices[i]);
internalVertices[i].connectsAsSource=new Array();


for(var j=0;j<outgoingCells.length;j++)
{
var cell=outgoingCells[j];
if(cell!=vertices[i]&&!layout.isVertexIgnored(cell))
{


var edges=graph.getEdgesBetween(vertices[i],cell,true);
if(edges!=null&&edges.length>0)
{
var internalEdge=new mxGraphHierarchyEdge(edges);
for(var k=0;k<edges.length;k++)
{
var edge=edges[k];
var edgeId=mxCellPath.create(edge);
this.edgeMapper[edgeId]=internalEdge;
}
internalEdge.source=internalVertices[i];
if(mxUtils.indexOf(internalVertices[i].connectsAsSource,internalEdge)<0)
{
internalVertices[i].connectsAsSource.push(internalEdge);
}
}
}
}
internalVertices[i].temp[0]=0;
}
};
mxGraphHierarchyModel.prototype.initialRank=function(startAtSinks)
{
sinksAtLayerZero=startAtSinks;
var startNodes=null;
if(!startAtSinks&&this.roots!=null)
{
startNodes=this.roots.slice();
}
else
{
startNodes=new Array();
}
if(startAtSinks)
{
for(var key in this.vertexMapper)
{
var internalNode=this.vertexMapper[key];
if(internalNode.connectsAsSource==null||internalNode.connectsAsSource.length==0)
{
startNodes.push(internalNode);
}
internalNode.temp[0]=-1;
}
if(startNodes.length==0)
{
startAtSinks=false;
}
}
var startNodesCopy=startNodes.slice();
while(startNodes.length>0)
{
var internalNode=startNodes[0];
var layerDeterminingEdges;
var edgesToBeMarked;
if(startAtSinks)
{
layerDeterminingEdges=internalNode.connectsAsSource;
edgesToBeMarked=internalNode.connectsAsTarget;
}
else
{
layerDeterminingEdges=internalNode.connectsAsTarget;
edgesToBeMarked=internalNode.connectsAsSource;
}

var allEdgesScanned=true;


var minimumLayer=0;
for(var i=0;i<layerDeterminingEdges.length;i++)
{
var internalEdge=layerDeterminingEdges[i];
if(internalEdge.temp[0]==5270620)
{

var otherNode;
if(startAtSinks)
{
otherNode=internalEdge.target;
}
else
{
otherNode=internalEdge.source;
}
minimumLayer=Math.max(minimumLayer,otherNode.temp[0]+1);
}
else
{
allEdgesScanned=false;
break;
}
}

if(allEdgesScanned)
{
internalNode.temp[0]=minimumLayer;
this.maxRank=Math.max(this.maxRank,minimumLayer);
if(edgesToBeMarked!=null)
{
for(var i=0;i<edgesToBeMarked.length;i++)
{
var internalEdge=edgesToBeMarked[i];
internalEdge.temp[0]=5270620;

var otherNode;
if(startAtSinks)
{
otherNode=internalEdge.source;
}
else
{
otherNode=internalEdge.target;
}
if(otherNode.temp[0]==-1)
{
startNodes.push(otherNode);



otherNode.temp[0]=-2;
}
}
}
startNodes.shift();
}
else
{

var removedCell=startNodes.shift();
startNodes.push(internalNode);
if(removedCell==internalNode&&startNodes.length==1)
{



break;
}
}
}
sinksAtLayerZero=startAtSinks;
if(startAtSinks)
{
for(var i=0;i<startNodesCopy.length;i++)
{
var internalNode=startNodesCopy[i];
var currentMinLayer=1000000;
var layerDeterminingEdges=internalNode.connectsAsTarget;
for(var j=0;j<internalNode.connectsAsTarget.length;j++)
{
var internalEdge=internalNode.connectsAsTarget[j];
var otherNode=internalEdge.source;
internalNode.temp[0]=Math.min(currentMinLayer,otherNode.temp[0]-1);
currentMinLayer=internalNode.temp[0];
}
}
}
};
mxGraphHierarchyModel.prototype.fixRanks=function()
{
var rankList=new Array();
this.ranks=new Array();
for(var i=0;i<this.maxRank+1;i++)
{
rankList[i]=new Array();
this.ranks[i]=rankList[i];
}


var rootsArray=null;
if(this.roots!=null)
{
var oldRootsArray=this.roots;
rootsArray=new Array();
for(var i=0;i<oldRootsArray.length;i++)
{
var cell=oldRootsArray[i];
var cellId=mxCellPath.create(cell);
var internalNode=this.vertexMapper[cellId];
rootsArray[i]=internalNode;
}
}
this.visit(function(parent,node,edge,layer,seen)
{
if(seen==0&&node.maxRank<0&&node.minRank<0)
{
rankList[node.temp[0]].push(node);
node.maxRank=node.temp[0];
node.minRank=node.temp[0];
node.temp[0]=rankList[node.maxRank].length-1;
}
if(parent!=null&&edge!=null)
{
var parentToCellRankDifference=parent.maxRank-node.maxRank;
if(parentToCellRankDifference>1)
{
edge.maxRank=parent.maxRank;
edge.minRank=node.maxRank;
edge.temp=new Array();
edge.x=new Array();
edge.y=new Array();
for(var i=edge.minRank+1;i<edge.maxRank;i++)
{

rankList[i].push(edge);
edge.setGeneralPurposeVariable(i,rankList[i].length-1);
}
}
}
},rootsArray,false,null);
};
mxGraphHierarchyModel.prototype.visit=function(visitor,dfsRoots,trackAncestors,seenNodes)
{
if(dfsRoots!=null)
{
for(var i=0;i<dfsRoots.length;i++)
{
var internalNode=dfsRoots[i];
if(internalNode!=null)
{
if(seenNodes==null)
{
seenNodes=new Object();
}
if(trackAncestors)
{
internalNode.hashCode=new Array();
internalNode.hashCode[0]=this.dfsCount;
internalNode.hashCode[1]=i;
this.extendedDfs(null,internalNode,null,visitor,seenNodes,internalNode.hashCode,i,0);
}
else
{
this.dfs(null,internalNode,null,visitor,seenNodes,0);
}
}
}
this.dfsCount++;
}
};
mxGraphHierarchyModel.prototype.dfs=function(parent,root,connectingEdge,visitor,seen,layer)
{
if(root!=null)
{
var rootId=mxCellPath.create(root.cell);
if(seen[rootId]==null)
{
seen[rootId]=root;
visitor(parent,root,connectingEdge,layer,0);

for(var i=0;i<root.connectsAsSource.length;i++)
{
var internalEdge=root.connectsAsSource[i];
var targetNode=internalEdge.target;
this.dfs(root,targetNode,internalEdge,visitor,seen,layer+1);
}
}
else
{
visitor(parent,root,connectingEdge,layer,1);
}
}
};
mxGraphHierarchyModel.prototype.extendedDfs=function(parent,root,connectingEdge,visitor,seen,ancestors,childHash,layer)
{



















if(root!=null)
{
if(parent!=null)
{




if(root.hashCode==null||root.hashCode[0]!=parent.hashCode[0])
{
var hashCodeLength=parent.hashCode.length+1;
root.hashCode=parent.hashCode.slice();
root.hashCode[hashCodeLength-1]=childHash;
}
}
var rootId=mxCellPath.create(root.cell);
if(seen[rootId]==null)
{
seen[rootId]=root;
visitor(parent,root,connectingEdge,layer,0);

var outgoingEdges=root.connectsAsSource.slice();
for(var i=0;i<root.connectsAsSource.length;i++)
{
var internalEdge=root.connectsAsSource[i];
var targetNode=internalEdge.target;
this.extendedDfs(root,targetNode,internalEdge,visitor,seen,root.hashCode,i,layer+1);
}
}
else
{
visitor(parent,root,connectingEdge,layer,1);
}
}
};
}

{
function mxHierarchicalLayoutStage(){};
mxHierarchicalLayoutStage.prototype.execute=function(parent){};
}

{
function mxMedianHybridCrossingReduction(layout)
{
this.layout=layout;
};
mxMedianHybridCrossingReduction.prototype=new mxHierarchicalLayoutStage();
mxMedianHybridCrossingReduction.prototype.constructor=mxMedianHybridCrossingReduction;
mxMedianHybridCrossingReduction.prototype.layout=null;
mxMedianHybridCrossingReduction.prototype.maxIterations=24;
mxMedianHybridCrossingReduction.prototype.nestedBestRanks=null;
mxMedianHybridCrossingReduction.prototype.currentBestCrossings=0;
mxMedianHybridCrossingReduction.prototype.iterationsWithoutImprovement=0;
mxMedianHybridCrossingReduction.prototype.maxNoImprovementIterations=2;
mxMedianHybridCrossingReduction.prototype.execute=function(parent)
{
var model=this.layout.getModel();
this.nestedBestRanks=new Array();
for(var i=0;i<model.ranks.length;i++)
{
this.nestedBestRanks[i]=model.ranks[i].slice();
}
var iterationsWithoutImprovement=0;
var currentBestCrossings=this.calculateCrossings(model);
for(var i=0;i<this.maxIterations&&iterationsWithoutImprovement<this.maxNoImprovementIterations;i++)
{
this.weightedMedian(i,model);
this.transpose(i,model);
var candidateCrossings=this.calculateCrossings(model);
if(candidateCrossings<currentBestCrossings)
{
currentBestCrossings=candidateCrossings;
iterationsWithoutImprovement=0;
for(var j=0;j<this.nestedBestRanks.length;j++)
{
var rank=model.ranks[j];
for(var key in rank)
{
var cell=rank[key];
this.nestedBestRanks[j][cell.getGeneralPurposeVariable(j)]=cell;
}
}
}
else
{

iterationsWithoutImprovement++;
for(var j=0;j<this.nestedBestRanks.length;j++)
{
var rank=model.ranks[j];
for(var k=0;k<rank.length;k++)
{
var cell=rank[k];
cell.setGeneralPurposeVariable(j,key);
}
}
}
if(currentBestCrossings==0)
{
break;
}
}
var ranks=new Array();
var rankList=new Array();
for(var i=0;i<model.maxRank+1;i++)
{
rankList[i]=new Array();
ranks[i]=rankList[i];
}
for(var i=0;i<this.nestedBestRanks.length;i++)
{
for(var j=0;j<this.nestedBestRanks[i].length;j++)
{
rankList[i].push(this.nestedBestRanks[i][j]);
}
}
model.ranks=ranks;
};
mxMedianHybridCrossingReduction.prototype.calculateCrossings=function(model)
{
var numRanks=model.ranks.length;
var totalCrossings=0;
for(var i=1;i<numRanks;i++)
{
totalCrossings+=this.calculateRankCrossing(i,model);
}
return totalCrossings;
};
mxMedianHybridCrossingReduction.prototype.calculateRankCrossing=function(i,model)
{
var totalCrossings=0;
var rank=model.ranks[i];
var previousRank=model.ranks[i-1];
var currentRankSize=rank.length;
var previousRankSize=previousRank.length;
var connections=new Array();
for(var j=0;j<currentRankSize;j++)
{
connections[j]=new Array();
}
for(var j=0;j<rank.length;j++)
{
var node=rank[j];
var rankPosition=node.getGeneralPurposeVariable(i);
var connectedCells=node.getPreviousLayerConnectedCells(i);
for(var k=0;k<connectedCells.length;k++)
{
var connectedNode=connectedCells[k];
var otherCellRankPosition=connectedNode.getGeneralPurposeVariable(i-1);
connections[rankPosition][otherCellRankPosition]=201207;
}
}


for(var j=0;j<currentRankSize;j++)
{
for(var k=0;k<previousRankSize;k++)
{
if(connections[j][k]==201207)
{

for(var j2=j+1;j2<currentRankSize;j2++)
{
for(var k2=0;k2<k;k2++)
{
if(connections[j2][k2]==201207)
{
totalCrossings++;
}
}
}
for(var j2=0;j2<j;j2++)
{
for(var k2=k+1;k2<previousRankSize;k2++)
{
if(connections[j2][k2]==201207)
{
totalCrossings++;
}
}
}
}
}
}
return totalCrossings/2;
};
mxMedianHybridCrossingReduction.prototype.transpose=function(mainLoopIteration,model)
{
var improved=true;
var count=0;
var maxCount=10;
while(improved&&count++<maxCount)
{


var nudge=mainLoopIteration%2==1&&count%2==1;
improved=false;
for(var i=0;i<model.ranks.length;i++)
{
var rank=model.ranks[i];
var orderedCells=new Array();
for(var j=0;j<rank.length;j++)
{
var cell=rank[j];
var tempRank=cell.getGeneralPurposeVariable(i);
if(tempRank<0)
{
tempRank=j;
}
orderedCells[tempRank]=cell;
}
var leftCellAboveConnections=null;
var leftCellBelowConnections=null;
var rightCellAboveConnections=null;
var rightCellBelowConnections=null;
var leftAbovePositions=null;
var leftBelowPositions=null;
var rightAbovePositions=null;
var rightBelowPositions=null;
var leftCell=null;
var rightCell=null;
for(var j=0;j<(rank.length-1);j++)
{





if(j==0)
{
leftCell=orderedCells[j];
leftCellAboveConnections=leftCell.getNextLayerConnectedCells(i);
leftCellBelowConnections=leftCell.getPreviousLayerConnectedCells(i);
leftAbovePositions=new Array();
leftBelowPositions=new Array();
for(var k=0;k<leftAbovePositions.length;k++)
{
leftAbovePositions[k]=leftCellAboveConnections[k].getGeneralPurposeVariable(i+1);
}
for(var k=0;k<leftBelowPositions.length;k++)
{
leftBelowPositions[k]=leftCellBelowConnections[k].getGeneralPurposeVariable(i-1);
}
}
else
{
leftCellAboveConnections=rightCellAboveConnections;
leftCellBelowConnections=rightCellBelowConnections;
leftAbovePositions=rightAbovePositions;
leftBelowPositions=rightBelowPositions;
leftCell=rightCell;
}
rightCell=orderedCells[j+1];
rightCellAboveConnections=rightCell.getNextLayerConnectedCells(i);
rightCellBelowConnections=rightCell.getPreviousLayerConnectedCells(i);
rightAbovePositions=new Array();
rightBelowPositions=new Array();
for(var k=0;k<rightAbovePositions.length;k++)
{
rightAbovePositions[k]=rightCellAboveConnections[k].getGeneralPurposeVariable(i+1);
}
for(var k=0;k<rightBelowPositions.length;k++)
{
rightBelowPositions[k]=rightCellBelowConnections[k].getGeneralPurposeVariable(i-1);
}
var totalCurrentCrossings=0;
var totalSwitchedCrossings=0;
for(var k=0;k<leftAbovePositions.length;k++)
{
for(var ik=0;ik<rightAbovePositions.length;ik++)
{
if(leftAbovePositions[k]>rightAbovePositions[ik])
{
totalCurrentCrossings++;
}
if(leftAbovePositions[k]<rightAbovePositions[ik])
{
totalSwitchedCrossings++;
}
}
}
for(var k=0;k<leftBelowPositions.length;k++)
{
for(var ik=0;ik<rightBelowPositions.length;ik++)
{
if(leftBelowPositions[k]>rightBelowPositions[ik])
{
totalCurrentCrossings++;
}
if(leftBelowPositions[k]<rightBelowPositions[ik])
{
totalSwitchedCrossings++;
}
}
}
if((totalSwitchedCrossings<totalCurrentCrossings)||(totalSwitchedCrossings==totalCurrentCrossings&&nudge))
{
var temp=leftCell.getGeneralPurposeVariable(i);
leftCell.setGeneralPurposeVariable(i,rightCell.getGeneralPurposeVariable(i));
rightCell.setGeneralPurposeVariable(i,temp);



rightCellAboveConnections=leftCellAboveConnections;
rightCellBelowConnections=leftCellBelowConnections;
rightAbovePositions=leftAbovePositions;
rightBelowPositions=leftBelowPositions;
rightCell=leftCell;
if(!nudge)
{


improved=true;
}
}
}
}
}
};
mxMedianHybridCrossingReduction.prototype.weightedMedian=function(iteration,model)
{
var downwardSweep=(iteration%2==0);
if(downwardSweep)
{
for(var j=model.maxRank-1;j>=0;j--)
{
this.medianRank(j,downwardSweep);
}
}
else
{
for(var j=1;j<model.maxRank;j++)
{
this.medianRank(j,downwardSweep);
}
}
};
mxMedianHybridCrossingReduction.prototype.medianRank=function(rankValue,downwardSweep)
{
var numCellsForRank=this.nestedBestRanks[rankValue].length;
var medianValues=new Array();
for(var i=0;i<numCellsForRank;i++)
{
var cell=this.nestedBestRanks[rankValue][i];
medianValues[i]=new MedianCellSorter();
medianValues[i].cell=cell;

medianValues[i].nudge=!downwardSweep;
var nextLevelConnectedCells;
if(downwardSweep)
{
nextLevelConnectedCells=cell.getNextLayerConnectedCells(rankValue);
}
else
{
nextLevelConnectedCells=cell.getPreviousLayerConnectedCells(rankValue);
}
var nextRankValue;
if(downwardSweep)
{
nextRankValue=rankValue+1;
}
else
{
nextRankValue=rankValue-1;
}
if(nextLevelConnectedCells!=null&&nextLevelConnectedCells.length!=0)
{
medianValues[i].medianValue=this.medianValue(nextLevelConnectedCells,nextRankValue);
}
else
{


medianValues[i].medianValue=-1.0;

}
}
medianValues.sort(MedianCellSorter.prototype.compare);

for(var i=0;i<numCellsForRank;i++)
{
medianValues[i].cell.setGeneralPurposeVariable(rankValue,i);
}
};
mxMedianHybridCrossingReduction.prototype.medianValue=function(connectedCells,rankValue)
{
var medianValues=new Array();
var arrayCount=0;
for(var i=0;i<connectedCells.length;i++)
{
var cell=connectedCells[i];
medianValues[arrayCount++]=cell.getGeneralPurposeVariable(rankValue);
}
medianValues.sort(MedianCellSorter.prototype.compare);
if(arrayCount%2==1)
{
return medianValues[arrayCount/2];
}
else if(arrayCount==2)
{
return((medianValues[0]+medianValues[1])/2.0);
}
else
{
var medianPoint=arrayCount/2;
var leftMedian=medianValues[medianPoint-1]-medianValues[0];
var rightMedian=medianValues[arrayCount-1]-medianValues[medianPoint];
return(medianValues[medianPoint-1]*rightMedian+medianValues[medianPoint]*leftMedian)/(leftMedian+rightMedian);
}
};
{
function MedianCellSorter()
{
};
MedianCellSorter.prototype.medianValue=0;
MedianCellSorter.prototype.nudge=false;
MedianCellSorter.prototype.cell=false;
MedianCellSorter.prototype.compare=function(a,b)
{
if(a!=null&&b!=null)
{
if(b.medianValue>a.medianValue)
{
return-1;
}
else if(b.medianValue<a.medianValue)
{
return 1;
}
else
{
if(b.nudge)
{
return-1;
}
else
{
return 1;
}
}
}
else
{
return 0;
}
};
}
}

{
function mxMinimumCycleRemover(layout)
{
this.layout=layout;
};
mxMinimumCycleRemover.prototype=new mxHierarchicalLayoutStage();
mxMinimumCycleRemover.prototype.constructor=mxMinimumCycleRemover;
mxMinimumCycleRemover.prototype.layout=null;
mxMinimumCycleRemover.prototype.execute=function(parent)
{
var model=this.layout.getModel();
var seenNodes=new Object();
var unseenNodes=mxUtils.clone(model.vertexMapper,null,true);

var rootsArray=null;
if(model.roots!=null)
{
var modelRoots=model.roots;
rootsArray=new Array();
for(var i=0;i<modelRoots.length;i++)
{
var nodeId=mxCellPath.create(modelRoots[i]);
rootsArray[i]=model.vertexMapper[nodeId];
}
}
model.visit(function(parent,node,connectingEdge,layer,seen)
{


if(node.isAncestor(parent))
{
connectingEdge.invert();
mxUtils.remove(connectingEdge,parent.connectsAsSource);
parent.connectsAsTarget.push(connectingEdge);
mxUtils.remove(connectingEdge,node.connectsAsTarget);
node.connectsAsSource.push(connectingEdge);
}
var cellId=mxCellPath.create(node.cell);
seenNodes[cellId]=node;
delete unseenNodes[cellId];
},rootsArray,true,null);
var possibleNewRoots=null;
if(unseenNodes.lenth>0)
{
possibleNewRoots=mxUtils.clone(unseenNodes,null,true);
}


var seenNodesCopy=mxUtils.clone(seenNodes,null,true);
model.visit(function(parent,node,connectingEdge,layer,seen)
{


if(node.isAncestor(parent))
{
connectingEdge.invert();
mxUtils.remove(connectingEdge,parent.connectsAsSource);
node.connectsAsSource.push(connectingEdge);
parent.connectsAsTarget.push(connectingEdge);
mxUtils.remove(connectingEdge,node.connectsAsTarget);
}
var cellId=mxCellPath.create(node.cell);
seenNodes[cellId]=node;
delete unseenNodes[cellId];
},unseenNodes,true,seenNodesCopy);
var graph=this.layout.getGraph();
if(possibleNewRoots!=null&&possibleNewRoots.length>0)
{
var roots=model.roots;
for(var i=0;i<possibleNewRoots.length;i++)
{
var node=possibleNewRoots[i];
var realNode=node.cell;
var numIncomingEdges=graph.getIncomingEdges(realNode).length;
if(numIncomingEdges==0)
{
roots.push(realNode);
}
}
}
};
}

{
function mxCoordinateAssignment(layout,intraCellSpacing,interRankCellSpacing,orientation,initialX,parallelEdgeSpacing)
{
this.layout=layout;
this.intraCellSpacing=intraCellSpacing;
this.interRankCellSpacing=interRankCellSpacing;
this.orientation=orientation;
this.initialX=initialX;
this.parallelEdgeSpacing=parallelEdgeSpacing;
};
mxCoordinateAssignment.prototype=new mxHierarchicalLayoutStage();
mxCoordinateAssignment.prototype.constructor=mxCoordinateAssignment;
mxCoordinateAssignment.prototype.layout=null;
mxCoordinateAssignment.prototype.intraCellSpacing=30;
mxCoordinateAssignment.prototype.interRankCellSpacing=10;
mxCoordinateAssignment.prototype.parallelEdgeSpacing=10;
mxCoordinateAssignment.prototype.maxIterations=8;
mxCoordinateAssignment.prototype.orientation=mxConstants.DIRECTION_NORTH;
mxCoordinateAssignment.prototype.initialX=null;
mxCoordinateAssignment.prototype.limitX=null;
mxCoordinateAssignment.prototype.currentXDelta=null;
mxCoordinateAssignment.prototype.widestRank=null;
mxCoordinateAssignment.prototype.widestRankValue=null;
mxCoordinateAssignment.prototype.rankWidths=null;
mxCoordinateAssignment.prototype.rankY=null;
mxCoordinateAssignment.prototype.fineTuning=true;
mxCoordinateAssignment.prototype.disableEdgeStyle=true;
mxCoordinateAssignment.prototype.nextLayerConnectedCache=null;
mxCoordinateAssignment.prototype.previousLayerConnectedCache=null;
mxCoordinateAssignment.prototype.execute=function(parent)
{
var model=this.layout.getModel();
this.currentXDelta=0.0;
this.initialCoords(this.layout.getGraph(),model);
if(this.fineTuning)
{
this.minNode(model);
}
var bestXDelta=100000000.0;
if(this.fineTuning)
{
for(var i=0;i<this.maxIterations;i++)
{
if(i!=0)
{
this.medianPos(i,model);
this.minNode(model);
}


if(this.currentXDelta<bestXDelta)
{
for(var j=0;j<model.ranks.length;j++)
{
var rank=model.ranks[j];
for(var k=0;k<rank.length;k++)
{
var cell=rank[k];
cell.setX(j,cell.getGeneralPurposeVariable(j));
}
}
bestXDelta=this.currentXDelta;
}
else
{
for(var j=0;j<model.ranks.length;j++)
{
var rank=model.ranks[j];
for(var k=0;k<rank.length;k++)
{
var cell=rank[k];
cell.setGeneralPurposeVariable(j,cell.getX(j));
}
}
}
this.currentXDelta=0;
}
}
this.setCellLocations(this.layout.getGraph(),model);
};
mxCoordinateAssignment.prototype.minNode=function(model)
{
var nodeList=new Array();
var map=new Array();
var rank=new Array();
for(var i=0;i<=model.maxRank;i++)
{
rank[i]=model.ranks[i];
for(var j=0;j<rank[i].length;j++)
{

var node=rank[i][j];
var nodeWrapper=new WeightedCellSorter(node,i);
nodeWrapper.rankIndex=j;
nodeWrapper.visited=true;
nodeList.push(nodeWrapper);
var cellId=mxCellPath.create(node.cell);
map[cellId]=nodeWrapper;
}
}

var maxTries=nodeList.length*10;
var count=0;
var tolerance=1;
while(nodeList.length>0&&count<=maxTries)
{
var cellWrapper=nodeList.shift();
var cell=cellWrapper.cell;
var rankValue=cellWrapper.weightedValue;
var rankIndex=parseInt(cellWrapper.rankIndex);
var nextLayerConnectedCells=cell.getNextLayerConnectedCells(rankValue);
var previousLayerConnectedCells=cell.getPreviousLayerConnectedCells(rankValue);
var numNextLayerConnected=nextLayerConnectedCells.length;
var numPreviousLayerConnected=previousLayerConnectedCells.length;
var medianNextLevel=this.medianXValue(nextLayerConnectedCells,rankValue+1);
var medianPreviousLevel=this.medianXValue(previousLayerConnectedCells,rankValue-1);
var numConnectedNeighbours=numNextLayerConnected+numPreviousLayerConnected;
var currentPosition=cell.getGeneralPurposeVariable(rankValue);
var cellMedian=currentPosition;
if(numConnectedNeighbours>0)
{
cellMedian=(medianNextLevel*numNextLayerConnected+medianPreviousLevel*numPreviousLayerConnected)/numConnectedNeighbours;
}
var positionChanged=false;
if(cellMedian<currentPosition-tolerance)
{
if(rankIndex==0)
{
cell.setGeneralPurposeVariable(rankValue,cellMedian);
positionChanged=true;
}
else
{
var leftCell=rank[rankValue][rankIndex-1];
var leftLimit=leftCell.getGeneralPurposeVariable(rankValue);
leftLimit=leftLimit+leftCell.width/2+this.intraCellSpacing+cell.width/2;
if(leftLimit<cellMedian)
{
cell.setGeneralPurposeVariable(rankValue,cellMedian);
positionChanged=true;
}
else if(leftLimit<cell.getGeneralPurposeVariable(rankValue)-tolerance)
{
cell.setGeneralPurposeVariable(rankValue,leftLimit);
positionChanged=true;
}
}
}
else if(cellMedian>currentPosition+tolerance)
{
var rankSize=rank[rankValue].length;
if(rankIndex==rankSize-1)
{
cell.setGeneralPurposeVariable(rankValue,cellMedian);
positionChanged=true;
}
else
{
var rightCell=rank[rankValue][rankIndex+1];
var rightLimit=rightCell.getGeneralPurposeVariable(rankValue);
rightLimit=rightLimit-rightCell.width/2-this.intraCellSpacing-cell.width/2;
if(rightLimit>cellMedian)
{
cell.setGeneralPurposeVariable(rankValue,cellMedian);
positionChanged=true;
}
else if(rightLimit>cell.getGeneralPurposeVariable(rankValue)+tolerance)
{
cell.setGeneralPurposeVariable(rankValue,rightLimit);
positionChanged=true;
}
}
}
if(positionChanged)
{
for(var i=0;i<nextLayerConnectedCells.length;i++)
{
var connectedCell=nextLayerConnectedCells[i];
var connectedCellId=mxCellPath.create(connectedCell.cell);
var connectedCellWrapper=map[connectedCellId];
if(connectedCellWrapper!=null)
{
if(connectedCellWrapper.visited==false)
{
connectedCellWrapper.visited=true;
nodeList.push(connectedCellWrapper);
}
}
}
for(var i=0;i<previousLayerConnectedCells.length;i++)
{
var connectedCell=previousLayerConnectedCells[i];
var connectedCellId=mxCellPath.create(connectedCell.cell);
var connectedCellWrapper=map[connectedCellId];
if(connectedCellWrapper!=null)
{
if(connectedCellWrapper.visited==false)
{
connectedCellWrapper.visited=true;
nodeList.push(connectedCellWrapper);
}
}
}
}
cellWrapper.visited=false;
count++;
}
};
mxCoordinateAssignment.prototype.medianPos=function(i,model)
{
var downwardSweep=(i%2==0);
if(downwardSweep)
{
for(var j=model.maxRank;j>0;j--)
{
this.rankMedianPosition(j-1,model,j);
}
}
else
{
for(var j=0;j<model.maxRank-1;j++)
{
this.rankMedianPosition(j+1,model,j);
}
}
};
mxCoordinateAssignment.prototype.rankMedianPosition=function(rankValue,model,nextRankValue)
{
var rank=model.ranks[rankValue];


var weightedValues=new Array();
var cellMap=new Array();
for(var i=0;i<rank.length;i++)
{
var currentCell=rank[i];
weightedValues[i]=new WeightedCellSorter();
weightedValues[i].cell=currentCell;
weightedValues[i].rankIndex=i;
var currentCellId=mxCellPath.create(currentCell.cell);
cellMap[currentCellId]=weightedValues[i];
var nextLayerConnectedCells=null;
if(nextRankValue<rankValue)
{
nextLayerConnectedCells=currentCell.getPreviousLayerConnectedCells(rankValue);
}
else
{
nextLayerConnectedCells=currentCell.getNextLayerConnectedCells(rankValue);
}

weightedValues[i].weightedValue=this.calculatedWeightedValue(currentCell,nextLayerConnectedCells);
}
weightedValues.sort(WeightedCellSorter.prototype.compare);

for(var i=0;i<weightedValues.length;i++)
{
var numConnectionsNextLevel=0;
var cell=weightedValues[i].cell;
var nextLayerConnectedCells=null;
var medianNextLevel=0;
if(nextRankValue<rankValue)
{
nextLayerConnectedCells=cell.getPreviousLayerConnectedCells(rankValue).slice();
}
else
{
nextLayerConnectedCells=cell.getNextLayerConnectedCells(rankValue).slice();
}
if(nextLayerConnectedCells!=null)
{
numConnectionsNextLevel=nextLayerConnectedCells.length;
if(numConnectionsNextLevel>0)
{
medianNextLevel=this.medianXValue(nextLayerConnectedCells,nextRankValue);
}
else
{


medianNextLevel=cell.getGeneralPurposeVariable(rankValue);
}
}
var leftBuffer=0.0;
var leftLimit=-100000000.0;
for(var j=weightedValues[i].rankIndex-1;j>=0;)
{
var rankId=mxCellPath.create(rank[j].cell);
var weightedValue=cellMap[rankId];
if(weightedValue!=null)
{
var leftCell=weightedValue.cell;
if(weightedValue.visited)
{


leftLimit=leftCell.getGeneralPurposeVariable(rankValue)+leftCell.width/2.0+this.intraCellSpacing+leftBuffer+cell.width/2.0;
j=-1;
}
else
{
leftBuffer+=leftCell.width+this.intraCellSpacing;
j--;
}
}
}
var rightBuffer=0.0;
var rightLimit=100000000.0;
for(var j=weightedValues[i].rankIndex+1;j<weightedValues.length;)
{
var rankId=mxCellPath.create(rank[j].cell);
var weightedValue=cellMap[rankId];
if(weightedValue!=null)
{
var rightCell=weightedValue.cell;
if(weightedValue.visited)
{


rightLimit=rightCell.getGeneralPurposeVariable(rankValue)-rightCell.width/2.0-this.intraCellSpacing-rightBuffer-cell.width/2.0;
j=weightedValues.length;
}
else
{
rightBuffer+=rightCell.width+this.intraCellSpacing;
j++;
}
}
}
if(medianNextLevel>=leftLimit&&medianNextLevel<=rightLimit)
{
cell.setGeneralPurposeVariable(rankValue,medianNextLevel);
}
else if(medianNextLevel<leftLimit)
{

cell.setGeneralPurposeVariable(rankValue,leftLimit);
this.currentXDelta+=leftLimit-medianNextLevel;
}
else if(medianNextLevel>rightLimit)
{

cell.setGeneralPurposeVariable(rankValue,rightLimit);
this.currentXDelta+=medianNextLevel-rightLimit;
}
weightedValues[i].visited=true;
}
};
mxCoordinateAssignment.prototype.calculatedWeightedValue=function(currentCell,collection)
{
var totalWeight=0;
for(var i=0;i<collection.length;i++)
{
var cell=collection[i];
if(currentCell.isVertex()&&cell.isVertex())
{
totalWeight++;
}
else if(currentCell.isEdge()&&cell.isEdge())
{
totalWeight+=8;
}
else
{
totalWeight+=2;
}
}
return totalWeight;
};
mxCoordinateAssignment.prototype.medianXValue=function(connectedCells,rankValue)
{
if(connectedCells.length==0)
{
return 0;
}
var medianValues=new Array();
for(var i=0;i<connectedCells.length;i++)
{
medianValues[i]=connectedCells[i].getGeneralPurposeVariable(rankValue);
}
medianValues.sort(MedianCellSorter.prototype.compare);
if(connectedCells.length%2==1)
{
return medianValues[connectedCells.length/2];
}
else
{
var medianPoint=connectedCells.length/2;
var leftMedian=medianValues[medianPoint-1];
var rightMedian=medianValues[medianPoint];
return((leftMedian+rightMedian)/2);
}
};
mxCoordinateAssignment.prototype.initialCoords=function(facade,model)
{
this.calculateWidestRank(facade,model);
for(var i=this.widestRank;i>0;i--)
{
if(i<model.maxRank)
{
this.rankCoordinates(i,facade,model);
}
}
for(var i=this.widestRank;i<=model.maxRank;i++)
{
if(i>0)
{
this.rankCoordinates(i,facade,model);
}
}
};
mxCoordinateAssignment.prototype.rankCoordinates=function(rankValue,graph,model)
{
var rank=model.ranks[rankValue];
var maxY=0.0;
var localX=this.initialX+(this.widestRankValue-this.rankWidths[rankValue])/2;

var boundsWarning=false;
for(var i=0;i<rank.length;i++)
{
var node=rank[i];
if(node.isVertex())
{
var bounds=this.layout.getVertexBounds(node.cell);
if(bounds!=null)
{
if(this.orientation==mxConstants.DIRECTION_NORTH||this.orientation==mxConstants.DIRECTION_SOUTH)
{
node.width=bounds.width;
node.height=bounds.height;
}
else
{
node.width=bounds.height;
node.height=bounds.width;
}
}
else
{
boundsWarning=true;
}
maxY=Math.max(maxY,node.height);
}
else if(node.isEdge())
{

var numEdges=1;
if(node.edges!=null)
{
numEdges=node.edges.length;
}
else
{
mxLog.warn('edge.edges is null');
}
node.width=(numEdges-1)*this.parallelEdgeSpacing;
}
localX+=node.width/2.0;
node.setX(rankValue,localX);
node.setGeneralPurposeVariable(rankValue,localX);
localX+=node.width/2.0;
localX+=this.intraCellSpacing;
}
if(boundsWarning==true)
{
mxLog.warn('At least one cell has no bounds');
}
};
mxCoordinateAssignment.prototype.calculateWidestRank=function(graph,model)
{
var y=-this.interRankCellSpacing;

var lastRankMaxCellHeight=0.0;
this.rankWidths=new Array();
this.rankY=new Array();
for(var rankValue=model.maxRank;rankValue>=0;rankValue--)
{
var maxCellHeight=0.0;
var rank=model.ranks[rankValue];
var localX=this.initialX;

var boundsWarning=false;
for(var i=0;i<rank.length;i++)
{
var node=rank[i];
if(node.isVertex())
{
var bounds=this.layout.getVertexBounds(node.cell);
if(bounds!=null)
{
if(this.orientation==mxConstants.DIRECTION_NORTH||this.orientation==mxConstants.DIRECTION_SOUTH)
{
node.width=bounds.width;
node.height=bounds.height;
}
else
{
node.width=bounds.height;
node.height=bounds.width;
}
}
else
{
boundsWarning=true;
}
maxCellHeight=Math.max(maxCellHeight,node.height);
}
else if(node.isEdge())
{

var numEdges=1;
if(node.edges!=null)
{
numEdges=node.edges.length;
}
else
{
mxLog.warn('edge.edges is null');
}
node.width=(numEdges-1)*this.parallelEdgeSpacing;
}
localX+=node.width/2.0;
node.setX(rankValue,localX);
node.setGeneralPurposeVariable(rankValue,localX);
localX+=node.width/2.0;
localX+=this.intraCellSpacing;
if(localX>this.widestRankValue)
{
this.widestRankValue=localX;
widestRank=rankValue;
}
this.rankWidths[rankValue]=localX;
}
if(boundsWarning==true)
{
mxLog.warn('At least one cell has no bounds');
}
this.rankY[rankValue]=y;
var distanceToNextRank=maxCellHeight/2.0+lastRankMaxCellHeight/2.0+this.interRankCellSpacing;
lastRankMaxCellHeight=maxCellHeight;
if(this.orientation==mxConstants.DIRECTION_NORTH||this.orientation==mxConstants.DIRECTION_WEST)
{
y+=distanceToNextRank;
}
else
{
y-=distanceToNextRank;
}
for(var i=0;i<rank.length;i++)
{
var cell=rank[i];
cell.setY(rankValue,y);
}
}
};
mxCoordinateAssignment.prototype.setCellLocations=function(graph,model)
{
for(var i=0;i<model.ranks.length;i++)
{
var rank=model.ranks[i];
for(var h=0;h<rank.length;h++)
{
var node=rank[h];
if(node.isVertex())
{
var realCell=node.cell;
var positionX=node.x[0]-node.width/2;
var positionY=node.y[0]-node.height/2;
if(this.orientation==mxConstants.DIRECTION_NORTH||this.orientation==mxConstants.DIRECTION_SOUTH)
{
this.layout.setVertexLocation(realCell,positionX,positionY);
}
else
{
this.layout.setVertexLocation(realCell,positionY,positionX);
}
limitX=Math.max(this.limitX,positionX+node.width);
}
else if(node.isEdge())
{

var offsetX=0.0;
if(node.temp[0]!=101207)
{
for(var j=0;j<node.edges.length;j++)
{
var realEdge=node.edges[j];
var newPoints=new Array();
if(node.isReversed)
{

for(var k=0;k<node.x.length;k++)
{
var positionX=node.x[k]+offsetX;
if(this.orientation==mxConstants.DIRECTION_NORTH||this.orientation==mxConstants.DIRECTION_SOUTH)
{
newPoints.push(new mxPoint(positionX,node.y[k]));
}
else
{
newPoints.push(new mxPoint(node.y[k],positionX));
}
limitX=Math.max(limitX,positionX);
}
this.processReversedEdge(node,realEdge);
}
else
{
for(var k=node.x.length-1;k>=0;k--)
{
var positionX=node.x[k]+offsetX;
if(this.orientation==mxConstants.DIRECTION_NORTH||this.orientation==mxConstants.DIRECTION_SOUTH)
{
newPoints.push(new mxPoint(positionX,node.y[k]));
}
else
{
newPoints.push(new mxPoint(node.y[k],positionX));
}
limitX=Math.max(limitX,positionX);
}
}
this.layout.setEdgePoints(realEdge,newPoints);
if(this.disableEdgeStyle)
{
graph.setCellStyles(mxConstants.STYLE_NOEDGESTYLE,"1",[realEdge]);
}

if(offsetX==0.0)
{
offsetX=this.parallelEdgeSpacing;
}
else if(offsetX>0)
{
offsetX=-offsetX;
}
else
{
offsetX=-offsetX+this.parallelEdgeSpacing;
}
}
node.temp[0]=101207;
}
}
}
}
};
mxCoordinateAssignment.prototype.processReversedEdge=function(graph,model)
{
};
{
function WeightedCellSorter(cell,weightedValue)
{
this.cell=cell;
this.weightedValue=weightedValue;
};
WeightedCellSorter.prototype.weightedValue=0;
WeightedCellSorter.prototype.nudge=false;
WeightedCellSorter.prototype.visited=false;
WeightedCellSorter.prototype.rankIndex=null;
WeightedCellSorter.prototype.cell=null;
WeightedCellSorter.prototype.compare=function(a,b)
{
if(a!=null&&b!=null)
{
if(b.weightedValue>a.weightedValue)
{
return-1;
}
else if(b.weightedValue<a.weightedValue)
{
return 1;
}
else
{
if(b.nudge)
{
return-1;
}
else
{
return 1;
}
}
}
else
{
return 0;
}
};
}
}

{
function mxHierarchicalLayout(graph,deterministic)
{
mxGraphLayout.call(this,graph);
this.deterministic=(deterministic!=null)?deterministic:true;
};
mxHierarchicalLayout.prototype=new mxGraphLayout();
mxHierarchicalLayout.prototype.constructor=mxHierarchicalLayout;
mxHierarchicalLayout.prototype.INITIAL_X_POSITION=100;
mxHierarchicalLayout.prototype.roots=null;
mxHierarchicalLayout.prototype.intraCellSpacing=30;
mxHierarchicalLayout.prototype.interRankCellSpacing=50;
mxHierarchicalLayout.prototype.interHierarchySpacing=60;
mxHierarchicalLayout.prototype.parallelEdgeSpacing=10;
mxHierarchicalLayout.prototype.orientation=mxConstants.DIRECTION_NORTH;
mxHierarchicalLayout.prototype.fineTuning=true;
mxHierarchicalLayout.prototype.deterministic;
mxHierarchicalLayout.prototype.fixRoots=false;
mxHierarchicalLayout.prototype.model=null;
mxHierarchicalLayout.prototype.getModel=function()
{
return this.model;
};
mxHierarchicalLayout.prototype.execute=function(parent,roots)
{
if(roots==null)
{
roots=this.graph.findTreeRoots(parent);
}
this.roots=roots;
if(this.roots!=null)
{
var model=this.graph.getModel();
model.beginUpdate();
try
{
this.run(parent);
}
finally
{
model.endUpdate();
}
}
};
mxHierarchicalLayout.prototype.run=function(parent)
{
var hierarchyVertices=new Array();
var fixedRoots=null;
var rootLocations=null;
var affectedEdges=null;
if(this.fixRoots)
{
fixedRoots=new Array();
rootLocations=new Array();
affectedEdges=new Array();
}
for(var i=0;i<this.roots.length;i++)
{

var newHierarchy=true;
for(var j=0;newHierarchy&&j<hierarchyVertices.length;j++)
{
var rootId=mxCellPath.create(this.roots[i]);
if(hierarchyVertices[j][rootId]!=null)
{
newHierarchy=false;
}
}
if(newHierarchy)
{
var cellsStack=new Array();
cellsStack.push(this.roots[i]);
var edgeSet=null;
if(this.fixRoots)
{
fixedRoots.push(this.roots[i]);
var location=this.getVertexBounds(this.roots[i]).getPoint();
rootLocations.push(location);
edgeSet=new Array();
}
var vertexSet=new Object();
while(cellsStack.length>0)
{
var cell=cellsStack.shift();
var cellId=mxCellPath.create(cell);
if(vertexSet[cellId]==null)
{
vertexSet[cellId]=cell;
if(this.fixRoots)
{
var tmp=this.graph.getIncomingEdges(cell,parent);
for(var k=0;k<tmp.length;k++)
{
edgeSet.push(tmp[k]);
}
}
var conns=this.graph.getConnections(cell,parent);
var cells=this.graph.getOpposites(conns,cell);
for(var k=0;k<cells.length;k++)
{
var tmpId=mxCellPath.create(cells[k]);
if(vertexSet[tmpId]==null)
{
cellsStack.push(cells[k]);
}
}
}
}
hierarchyVertices.push(vertexSet);
if(this.fixRoots)
{
affectedEdges.push(edgeSet);
}
}
}

var initialX=this.INITIAL_X_POSITION;
for(var i=0;i<hierarchyVertices.length;i++)
{
var vertexSet=hierarchyVertices[i];
var tmp=new Array();
for(var key in vertexSet)
{
tmp.push(vertexSet[key]);
}
this.model=new mxGraphHierarchyModel(this,tmp,this.roots,parent,false,this.deterministic);
this.cycleStage(parent);
this.layeringStage();
this.crossingStage(parent);
initialX=this.placementStage(initialX,parent);
if(this.fixRoots)
{

var root=fixedRoots[i];
var oldLocation=rootLocations[i];
var newLocation=this.getVertexBounds(root).getPoint();
var diffX=oldLocation.x-newLocation.x;
var diffY=oldLocation.y-newLocation.y;
this.graph.move(vertexSet,diffX,diffY);
var connectedEdges=affectedEdges[i+1];
this.graph.move(connectedEdges,diffX,diffY);
}
}
};
mxHierarchicalLayout.prototype.cycleStage=function(parent)
{
var cycleStage=new mxMinimumCycleRemover(this);
cycleStage.execute(parent);
};
mxHierarchicalLayout.prototype.layeringStage=function()
{
this.model.initialRank(true);
this.model.fixRanks();
};
mxHierarchicalLayout.prototype.crossingStage=function(parent)
{
var crossingStage=new mxMedianHybridCrossingReduction(this);
crossingStage.execute(parent);
};
mxHierarchicalLayout.prototype.placementStage=function(initialX,parent)
{
var placementStage=new mxCoordinateAssignment(this,this.intraCellSpacing,this.interRankCellSpacing,this.orientation,initialX,this.parallelEdgeSpacing);
placementStage.fineTuning=this.fineTuning;
placementStage.execute(parent);
return placementStage.limitX+this.interHierarchySpacing;
};
}

{
function mxGraphModel(root)
{
this.currentEdit=this.createUndoableEdit();
if(root==null)
{
root=this.createRoot();
}
this.setRoot(root);
};
mxGraphModel.prototype=new mxEventSource();
mxGraphModel.prototype.constructor=mxGraphModel;
mxGraphModel.prototype.root=null;
mxGraphModel.prototype.cells=null;
mxGraphModel.prototype.maintainEdgeParent=true;
mxGraphModel.prototype.createIds=true;
mxGraphModel.prototype.prefix='';
mxGraphModel.prototype.postfix='';
mxGraphModel.prototype.nextId=0;
mxGraphModel.prototype.currentEdit=null;
mxGraphModel.prototype.updateLevel=0;
mxGraphModel.prototype.createRoot=function()
{
var cell=new mxCell();
cell.insert(new mxCell());
return cell;
};
mxGraphModel.prototype.getCell=function(id)
{
return(this.cells!=null)?this.cells[id]:null;
};
mxGraphModel.prototype.getCells=function(filter,parent,result)
{
result=result||new Array();
if(typeof(filter)=='function')
{
parent=parent||this.getRoot();

if(filter(parent))
{
result.push(parent);
}
var childCount=this.getChildCount(parent);
for(var i=0;i<childCount;i++)
{
var child=this.getChildAt(parent,i);
this.getCells(filter,child,result);
}
}
return result;
};
mxGraphModel.prototype.getRoot=function(cell)
{
var root=cell||this.root;
if(cell!=null)
{
while(cell!=null)
{
root=cell;
cell=this.getParent(cell);
}
}
return root;
};
mxGraphModel.prototype.setRoot=function(root)
{
this.execute(new mxRootChange(this,root));
return root;
};
mxGraphModel.prototype.rootChanged=function(root)
{
var oldRoot=this.root;
this.root=root;
this.nextId=0;
this.cells=null;
this.cellAdded(root);
return oldRoot;
};
mxGraphModel.prototype.isRoot=function(cell)
{
return cell!=null&&this.root==cell;
};
mxGraphModel.prototype.isLayer=function(cell)
{
return this.isRoot(this.getParent(cell));
};
mxGraphModel.prototype.isAncestor=function(parent,child)
{
while(child!=null&&child!=parent)
{
child=this.getParent(child);
}
return child==parent;
};
mxGraphModel.prototype.contains=function(cell)
{
return this.isAncestor(this.root,cell);
};
mxGraphModel.prototype.getParent=function(cell)
{
return(cell!=null)?cell.getParent():null;
};
mxGraphModel.prototype.add=function(parent,child,index)
{
if(parent!=null&&child!=null)
{
if(index==null)
{
index=this.getChildCount(parent);
}
var parentChanged=parent!=this.getParent(child);
this.execute(new mxChildChange(this,parent,child,index));


if(this.maintainEdgeParent&&parentChanged)
{
this.updateEdgeParents(child);
}
}
return child;
};
mxGraphModel.prototype.cellAdded=function(cell)
{
if(cell!=null)
{
if(cell.getId()==null&&this.createIds)
{
cell.setId(this.createId(cell));
}
if(cell.getId()!=null)
{
var collision=this.getCell(cell.getId());
if(collision!=cell)
{

while(collision!=null)
{
cell.setId(this.createId(cell));
collision=this.getCell(cell.getId());
}
if(this.cells==null)
{
this.cells=new Object();
}
this.cells[cell.getId()]=cell;
}
}
if(mxUtils.isNumeric(cell.getId()))
{
this.nextId=Math.max(this.nextId,cell.getId());
}
var childCount=this.getChildCount(cell);
for(var i=0;i<childCount;i++)
{
this.cellAdded(this.getChildAt(cell,i));
}
}
};
mxGraphModel.prototype.createId=function(cell)
{
var id=this.nextId;
this.nextId++;
return this.prefix+id+this.postfix;
};
mxGraphModel.prototype.updateEdgeParents=function(cell,root)
{
root=root||this.getRoot(cell);
var childCount=this.getChildCount(cell);
for(var i=0;i<childCount;i++)
{
var child=this.getChildAt(cell,i);
this.updateEdgeParents(child,root);
}
var edgeCount=this.getEdgeCount(cell);
for(var i=0;i<edgeCount;i++)
{
var edge=this.getEdgeAt(cell,i);


if(this.isAncestor(root,edge))
{
this.updateEdgeParent(edge);
}
}
if(this.isEdge(cell))
{
this.updateEdgeParent(cell);
}
};
mxGraphModel.prototype.updateEdgeParent=function(edge)
{
var source=this.getTerminal(edge,true);
var target=this.getTerminal(edge,false);
var cell=null;
if(source==target)
{
cell=this.getParent(source);
}
else
{
cell=this.getNearestCommonAncestor(source,target);
}
if(cell!=null&&this.getParent(cell)!=this.root&&this.getParent(edge)!=cell)
{
var geo=this.getGeometry(edge);
if(geo!=null)
{
var origin1=this.getOrigin(this.getParent(edge));
var origin2=this.getOrigin(cell);
var dx=origin2.x-origin1.x;
var dy=origin2.y-origin1.y;
geo=geo.translate(-dx,-dy);
this.setGeometry(edge,geo);
}
this.add(cell,edge,this.getChildCount(cell));
}
};
mxGraphModel.prototype.getOrigin=function(cell)
{
var result=null;
if(cell!=null)
{
result=this.getOrigin(this.getParent(cell));
if(!this.isEdge(cell))
{
var geo=this.getGeometry(cell);
if(geo!=null)
{
result.x+=geo.x;
result.y+=geo.y;
}
}
}
else
{
result=new mxPoint();
}
return result;
};
mxGraphModel.prototype.getNearestCommonAncestor=function(cell1,cell2)
{
if(cell1!=null&&cell2!=null)
{
var path=mxCellPath.create(cell2);
if(path!=null&&path.length>0)
{

var cell=cell1;
var current=mxCellPath.create(cell)+mxCellPath.PATH_SEPARATOR;
while(cell!=null)
{
var parent=this.getParent(cell);

if(path.indexOf(current)==0&&parent!=null)
{
return cell;
}
current=mxCellPath.getParentPath(current)+mxCellPath.PATH_SEPARATOR;
cell=parent;
}
}
}
return null;
};
mxGraphModel.prototype.remove=function(cell)
{
if(cell==this.root)
{
this.setRoot(null);
}
else if(this.getParent(cell)!=null)
{
this.execute(new mxChildChange(this,null,cell));
}
return cell;
};
mxGraphModel.prototype.cellRemoved=function(cell)
{
if(cell!=null&&this.cells!=null)
{
var childCount=this.getChildCount(cell);
for(var i=childCount-1;i>=0;i--)
{
this.cellRemoved(this.getChildAt(cell,i));
}
if(this.cells!=null&&cell.getId()!=null)
{
delete this.cells[cell.getId()];
}
}
};
mxGraphModel.prototype.parentForCellChanged=function(cell,parent,index)
{
var previous=this.getParent(cell);
if(parent!=null)
{
if(parent!=previous||previous.getIndex(cell)!=index)
{
parent.insert(cell,index);
}
}
else if(previous!=null)
{
var oldIndex=previous.getIndex(cell);
previous.remove(oldIndex);
}

if(!this.contains(previous)&&parent!=null)
{
this.cellAdded(cell);
}
else if(parent==null)
{
this.cellRemoved(cell);
}
return previous;
};
mxGraphModel.prototype.getChildCount=function(cell)
{
return(cell!=null)?cell.getChildCount():0;
};
mxGraphModel.prototype.getChildAt=function(cell,index)
{
return(cell!=null)?cell.getChildAt(index):null;
};
mxGraphModel.prototype.getChildren=function(cell)
{
return(cell!=null)?cell.children:null;
};
mxGraphModel.prototype.getChildVertices=function(parent)
{
return this.getChildCells(parent,true,false);
};
mxGraphModel.prototype.getChildEdges=function(parent)
{
return this.getChildCells(parent,false,true);
};
mxGraphModel.prototype.getChildCells=function(parent,vertices,edges)
{
vertices=(vertices!=null)?vertices:false;
edges=(edges!=null)?edges:false;
var childCount=this.getChildCount(parent);
var result=new Array();
for(var i=0;i<childCount;i++)
{
var child=this.getChildAt(parent,i);
if((!edges&&!vertices)||(edges&&this.isEdge(child))||(vertices&&this.isVertex(child)))
{
result.push(child);
}
}
return result;
};
mxGraphModel.prototype.getTerminal=function(edge,isSource)
{
return edge.getTerminal(isSource);
};
mxGraphModel.prototype.setTerminal=function(edge,terminal,isSource)
{
if(terminal!=this.getTerminal(edge,isSource))
{
this.execute(new mxTerminalChange(this,edge,terminal,isSource));
if(this.maintainEdgeParent)
{
this.updateEdgeParent(edge);
}
}
return terminal;
};
mxGraphModel.prototype.setTerminals=function(edge,source,target)
{
this.beginUpdate();
try
{
this.setTerminal(edge,source,true);
this.setTerminal(edge,target,false);
}
finally
{
this.endUpdate();
}
};
mxGraphModel.prototype.terminalForCellChanged=function(edge,terminal,isSource)
{
var previous=this.getTerminal(edge,isSource);
if(terminal!=null)
{
terminal.insertEdge(edge,isSource);
}
else if(previous!=null)
{
previous.removeEdge(edge,isSource);
}
return previous;
};
mxGraphModel.prototype.getEdgeCount=function(cell)
{
return(cell!=null)?cell.getEdgeCount():0;
};
mxGraphModel.prototype.getEdgeAt=function(cell,index)
{
return(cell!=null)?cell.getEdgeAt(index):null;
};
mxGraphModel.prototype.getDirectedEdgeCount=function(cell,outgoing,ignoredEdge)
{
var count=0;
var edgeCount=this.getEdgeCount(cell);
for(var i=0;i<edgeCount;i++)
{
var edge=this.getEdgeAt(cell,i);
if(edge!=ignoredEdge&&this.getTerminal(edge,outgoing)==cell)
{
count++;
}
}
return count;
};
mxGraphModel.prototype.getConnections=function(cell)
{
return this.getEdges(cell,true,true,false);
};
mxGraphModel.prototype.getIncomingEdges=function(cell)
{
return this.getEdges(cell,true,false,false);
};
mxGraphModel.prototype.getOutgoingEdges=function(cell)
{
return this.getEdges(cell,false,true,false);
};
mxGraphModel.prototype.getEdges=function(cell,incoming,outgoing,includeLoops)
{
incoming=(incoming!=null)?incoming:true;
outgoing=(outgoing!=null)?outgoing:true;
includeLoops=(includeLoops!=null)?includeLoops:true;
var edgeCount=this.getEdgeCount(cell);
var result=new Array();
for(var i=0;i<edgeCount;i++)
{
var edge=this.getEdgeAt(cell,i);
var source=this.getTerminal(edge,true);
var target=this.getTerminal(edge,false);
if(includeLoops||((source!=target)&&((incoming&&target==cell)||(outgoing&&source==cell))))
{
result.push(edge);
}
}
return result;
};
mxGraphModel.prototype.getEdgesBetween=function(source,target,directed)
{
directed=(directed!=null)?directed:false;
var tmp1=this.getEdgeCount(source);
var tmp2=this.getEdgeCount(target);
var terminal=source;
var edgeCount=tmp1;

if(tmp2<tmp1)
{
edgeCount=tmp2;
terminal=target;
}
var result=new Array();

for(var i=0;i<edgeCount;i++)
{
var edge=this.getEdgeAt(terminal,i);
var src=this.getTerminal(edge,true);
var trg=this.getTerminal(edge,false);
var isSource=src==source;
if(isSource&&trg==target||(!directed&&this.getTerminal(edge,!isSource)==target))
{
result.push(edge);
}
}
return result;
};
mxGraphModel.prototype.getOpposites=function(edges,terminal,sources,targets)
{
sources=(sources!=null)?sources:true;
targets=(targets!=null)?targets:true;
var terminals=new Array();
if(edges!=null)
{
for(var i=0;i<edges.length;i++)
{
var source=this.getTerminal(edges[i],true);
var target=this.getTerminal(edges[i],false);


if(source==terminal&&target!=null&&target!=terminal&&targets)
{
terminals.push(target);
}


else if(target==terminal&&source!=null&&source!=terminal&&sources)
{
terminals.push(source);
}
}
}
return terminals;
};
mxGraphModel.prototype.getTopmostCells=function(cells)
{
var tmp=new Array();
for(var i=0;i<cells.length;i++)
{
var cell=cells[i];
var topmost=true;
var parent=this.getParent(cell);
while(parent!=null)
{
if(mxUtils.indexOf(cells,parent)>=0)
{
topmost=false;
break;
}
parent=this.getParent(parent);
}
if(topmost)
{
tmp.push(cell);
}
}
return tmp;
};
mxGraphModel.prototype.isVertex=function(cell)
{
return cell.isVertex();
};
mxGraphModel.prototype.isEdge=function(cell)
{
return cell.isEdge();
};
mxGraphModel.prototype.isConnectable=function(cell)
{
return cell.isConnectable();
};
mxGraphModel.prototype.getValue=function(cell)
{
return(cell!=null)?cell.getValue():null;
};
mxGraphModel.prototype.setValue=function(cell,value)
{
this.execute(new mxValueChange(this,cell,value));
return value;
};
mxGraphModel.prototype.valueForCellChanged=function(cell,value)
{
return cell.valueChanged(value);
};
mxGraphModel.prototype.getGeometry=function(cell,geometry)
{
return(cell!=null)?cell.getGeometry():null;
};
mxGraphModel.prototype.setGeometry=function(cell,geometry)
{
if(geometry!=this.getGeometry(cell))
{
this.execute(new mxGeometryChange(this,cell,geometry));
}
return geometry;
};
mxGraphModel.prototype.geometryForCellChanged=function(cell,geometry)
{
var previous=this.getGeometry(cell);
cell.setGeometry(geometry);
return previous;
};
mxGraphModel.prototype.getStyle=function(cell)
{
return cell.getStyle();
};
mxGraphModel.prototype.setStyle=function(cell,style)
{
if(style!=this.getStyle(cell))
{
this.execute(new mxStyleChange(this,cell,style));
}
return style;
};
mxGraphModel.prototype.styleForCellChanged=function(cell,style)
{
var previous=this.getStyle(cell);
cell.setStyle(style);
return previous;
};
mxGraphModel.prototype.isCollapsed=function(cell)
{
return cell.isCollapsed();
};
mxGraphModel.prototype.setCollapsed=function(cell,collapsed)
{
if(collapsed!=this.isCollapsed(cell))
{
this.execute(new mxCollapseChange(this,cell,collapsed));
}
return collapsed;
};
mxGraphModel.prototype.collapsedStateForCellChanged=function(cell,collapsed)
{
var previous=this.isCollapsed(cell);
cell.setCollapsed(collapsed);
return previous;
};
mxGraphModel.prototype.isVisible=function(cell)
{
return cell.isVisible();
};
mxGraphModel.prototype.setVisible=function(cell,visible)
{
if(visible!=this.isVisible(cell))
{
this.execute(new mxVisibleChange(this,cell,visible));
}
return visible;
};
mxGraphModel.prototype.visibleStateForCellChanged=function(cell,visible)
{
var previous=this.isVisible(cell);
cell.setVisible(visible);
return previous;
};
mxGraphModel.prototype.execute=function(change)
{
this.dispatchEvent('beforeExecute',this,change);
change.execute();
this.dispatchEvent('execute',this,change);
this.beginUpdate();
this.currentEdit.add(change);
this.endUpdate();
this.dispatchEvent('afterExecute',this,change);
};
mxGraphModel.prototype.beginUpdate=function()
{
this.updateLevel++;
this.dispatchEvent('beginUpdate',this);
};
mxGraphModel.prototype.endUpdate=function()
{
this.updateLevel--;
this.dispatchEvent('endUpdate',this);
if(this.updateLevel==0)
{
if(!this.currentEdit.isEmpty())
{
var tmp=this.currentEdit;
this.dispatchEvent('beforeUndo',this,tmp);
this.currentEdit=this.createUndoableEdit();
tmp.notify();
this.dispatchEvent('undo',this,tmp);
}
}
};
mxGraphModel.prototype.createUndoableEdit=function()
{
var edit=new mxUndoableEdit(this,true);
edit.notify=function()
{
edit.source.dispatchEvent('change',edit.source,edit.changes);
edit.source.dispatchEvent('notify',edit.source,edit.changes);
}
return edit;
};
mxGraphModel.prototype.mergeChildren=function(from,to,cloneAllEdges)
{
cloneAllEdges=(cloneAllEdges!=null)?cloneAllEdges:true;
this.beginUpdate();
try
{
var mapping=new Object();
this.mergeChildrenImpl(from,to,cloneAllEdges,mapping);


for(var key in mapping)
{
var cell=mapping[key];
var terminal=this.getTerminal(cell,true);
if(terminal!=null)
{
terminal=mapping[mxCellPath.create(terminal)];
this.setTerminal(cell,terminal,true);
}
terminal=this.getTerminal(cell,false);
if(terminal!=null)
{
terminal=mapping[mxCellPath.create(terminal)];
this.setTerminal(cell,terminal,false);
}
}
}
finally
{
this.endUpdate();
}
};
mxGraphModel.prototype.mergeChildrenImpl=function(from,to,cloneAllEdges,mapping)
{
this.beginUpdate();
try
{
var childCount=from.getChildCount();
for(var i=0;i<childCount;i++)
{
var cell=from.getChildAt(i);
if(typeof(cell.getId)=='function')
{
var id=cell.getId();
var target=(id!=null&&(!this.isEdge(cell)||!cloneAllEdges))?this.getCell(id):null;
if(target==null)
{
var clone=cell.clone();
clone.setId(id);

clone.setTerminal(cell.getTerminal(true),true);
clone.setTerminal(cell.getTerminal(false),false);
target=this.add(to,clone,this.getChildCount(to));
}
mapping[mxCellPath.create(cell)]=target;
this.mergeChildrenImpl(cell,target,cloneAllEdges,mapping);
}
}
}
finally
{
this.endUpdate();
}
};



mxGraphModel.prototype.cloneCell=function(cell)
{
if(cell!=null)
{
return this.cloneCells([cell],true)[0];
}
return null;
};
mxGraphModel.prototype.cloneCells=function(cells,includeChildren)
{
var mapping=new Object();
var clones=new Array();
for(var i=0;i<cells.length;i++)
{
if(cells[i]!=null)
{
clones.push(this.cloneCellImpl(cells[i],mapping,includeChildren));
}
else
{
clones.push(null);
}
}
for(var i=0;i<clones.length;i++)
{
if(clones[i]!=null)
{
this.restoreClone(clones[i],cells[i],mapping);
}
}
return clones;
};
mxGraphModel.prototype.cloneCellImpl=function(cell,mapping,includeChildren)
{
var clone=this.cellCloned(cell);

mapping[mxObjectIdentity.get(cell)]=clone;
if(includeChildren)
{
var childCount=this.getChildCount(cell);
for(var i=0;i<childCount;i++)
{
var cloneChild=this.cloneCellImpl(this.getChildAt(cell,i),mapping,true);
clone.insert(cloneChild);
}
}
return clone;
};
mxGraphModel.prototype.cellCloned=function(cell)
{
return cell.clone();
};
mxGraphModel.prototype.restoreClone=function(clone,cell,mapping)
{
var source=this.getTerminal(cell,true);
if(source!=null)
{
var tmp=mapping[mxObjectIdentity.get(source)];
if(tmp!=null)
{
tmp.insertEdge(clone,true);
}
}
var target=this.getTerminal(cell,false);
if(target!=null)
{
var tmp=mapping[mxObjectIdentity.get(target)];
if(tmp!=null)
{
tmp.insertEdge(clone,false);
}
}
var childCount=this.getChildCount(clone);
for(var i=0;i<childCount;i++)
{
this.restoreClone(this.getChildAt(clone,i),this.getChildAt(cell,i),mapping);
}
};



function mxRootChange(model,root)
{
this.model=model;
this.root=root;
this.previous=root;
};
mxRootChange.prototype.execute=function()
{
this.root=this.previous;
this.previous=this.model.rootChanged(this.previous);
};
function mxChildChange(model,parent,child,index)
{
this.model=model;
this.parent=parent;
this.previous=parent;
this.child=child;
this.index=index;
this.previousIndex=index;
this.isAdded=(parent==null);
};
mxChildChange.prototype.execute=function()
{
var tmp=this.model.getParent(this.child);
var tmp2=(tmp!=null)?tmp.getIndex(this.child):0;
if(this.previous==null)
{
this.connect(this.child,false);
}
tmp=this.model.parentForCellChanged(this.child,this.previous,this.previousIndex);
if(this.previous!=null)
{
this.connect(this.child,true);
}
this.parent=this.previous;
this.previous=tmp;
this.index=this.previousIndex;
this.previousIndex=tmp2;
this.isAdded=!this.isAdded;
};
mxChildChange.prototype.connect=function(cell,isConnect)
{
isConnect=(isConnect!=null)?isConnect:true;
var source=cell.getTerminal(true);
var target=cell.getTerminal(false);
if(source!=null)
{
if(isConnect)
{
this.model.terminalForCellChanged(cell,source,true);
}
else
{
this.model.terminalForCellChanged(cell,null,true);
}
}
if(target!=null)
{
if(isConnect)
{
this.model.terminalForCellChanged(cell,target,false);
}
else
{
this.model.terminalForCellChanged(cell,null,false);
}
}
cell.setTerminal(source,true);
cell.setTerminal(target,false);
var childCount=this.model.getChildCount(cell);
for(var i=0;i<childCount;i++)
{
this.connect(this.model.getChildAt(cell,i),isConnect);
}
};
function mxTerminalChange(model,cell,terminal,isSource)
{
this.model=model;
this.cell=cell;
this.terminal=terminal;
this.previous=terminal;
this.isSource=isSource;
};
mxTerminalChange.prototype.execute=function()
{
this.terminal=this.previous;
this.previous=this.model.terminalForCellChanged(this.cell,this.previous,this.isSource);
};
function mxValueChange(model,cell,value)
{
this.model=model;
this.cell=cell;
this.value=value;
this.previous=value;
};
mxValueChange.prototype.execute=function()
{
this.value=this.previous;
this.previous=this.model.valueForCellChanged(this.cell,this.previous);
};
function mxStyleChange(model,cell,style)
{
this.model=model;
this.cell=cell;
this.style=style;
this.previous=style;
};
mxStyleChange.prototype.execute=function()
{
this.style=this.previous;
this.previous=this.model.styleForCellChanged(this.cell,this.previous);
};
function mxGeometryChange(model,cell,geometry)
{
this.model=model;
this.cell=cell;
this.geometry=geometry;
this.previous=geometry;
};
mxGeometryChange.prototype.execute=function()
{
this.geometry=this.previous;
this.previous=this.model.geometryForCellChanged(this.cell,this.previous);
};
function mxCollapseChange(model,cell,collapsed)
{
this.model=model;
this.cell=cell;
this.collapsed=collapsed;
this.previous=collapsed;
};
mxCollapseChange.prototype.execute=function()
{
this.collapsed=this.previous;
this.previous=this.model.collapsedStateForCellChanged(this.cell,this.previous);
};
function mxVisibleChange(model,cell,visible)
{
this.model=model;
this.cell=cell;
this.visible=visible;
this.previous=visible;
};
mxVisibleChange.prototype.execute=function()
{
this.visible=this.previous;
this.previous=this.model.visibleStateForCellChanged(this.cell,this.previous);
};
function mxCellAttributeChange(cell,attribute,value)
{
this.cell=cell;
this.attribute=attribute;
this.value=value;
this.previous=value;
};
mxCellAttributeChange.prototype.execute=function()
{
var tmp=this.cell.getAttribute(this.attribute);
if(this.previous==null)
{
this.cell.value.removeAttribute(this.attribute);
}
else
{
this.cell.setAttribute(this.attribute,this.previous);
}
this.previous=tmp;
};
}

{
function mxCell(value,geometry,style)
{
this.value=value;
this.setGeometry(geometry);
this.setStyle(style);
if(this.onInit!=null)
{
this.onInit();
}
};
mxCell.prototype.id=null;
mxCell.prototype.value=null;
mxCell.prototype.geometry=null;
mxCell.prototype.style=null;
mxCell.prototype.vertex=false;
mxCell.prototype.edge=false;
mxCell.prototype.connectable=true;
mxCell.prototype.visible=true;
mxCell.prototype.collapsed=false;
mxCell.prototype.parent=null;
mxCell.prototype.source=null;
mxCell.prototype.target=null;
mxCell.prototype.children=null;
mxCell.prototype.edges=null;
mxCell.prototype.mxTransient=[mxObjectIdentity.FIELD_NAME,'id','value','parent','source','target','children','edges'];
mxCell.prototype.getId=function()
{
return this.id;
};
mxCell.prototype.setId=function(id)
{
this.id=id;
};
mxCell.prototype.getValue=function()
{
return this.value;
};
mxCell.prototype.setValue=function(value)
{
this.value=value;
};
mxCell.prototype.valueChanged=function(newValue)
{
var previous=this.getValue();
this.setValue(newValue);
return previous;
};
mxCell.prototype.getGeometry=function()
{
return this.geometry;
};
mxCell.prototype.setGeometry=function(geometry)
{
this.geometry=geometry;
};
mxCell.prototype.getStyle=function()
{
return this.style;
};
mxCell.prototype.setStyle=function(style)
{
this.style=style;
};
mxCell.prototype.isVertex=function()
{
return this.vertex;
};
mxCell.prototype.setVertex=function(vertex)
{
this.vertex=vertex;
};
mxCell.prototype.isEdge=function()
{
return this.edge;
};
mxCell.prototype.setEdge=function(edge)
{
this.edge=edge;
};
mxCell.prototype.isConnectable=function()
{
return this.connectable;
};
mxCell.prototype.setConnectable=function(connectable)
{
this.connectable=connectable;
};
mxCell.prototype.isVisible=function()
{
return this.visible;
};
mxCell.prototype.setVisible=function(visible)
{
this.visible=visible;
};
mxCell.prototype.isCollapsed=function()
{
return this.collapsed;
};
mxCell.prototype.setCollapsed=function(collapsed)
{
this.collapsed=collapsed;
};
mxCell.prototype.getParent=function(parent)
{
return this.parent;
};
mxCell.prototype.setParent=function(parent)
{
this.parent=parent;
};
mxCell.prototype.getTerminal=function(source)
{
return(source)?this.source:this.target;
};
mxCell.prototype.setTerminal=function(terminal,isSource)
{
if(isSource)
{
this.source=terminal;
}
else
{
this.target=terminal;
}
return terminal;
};
mxCell.prototype.getChildCount=function()
{
return(this.children==null)?0:this.children.length;
};
mxCell.prototype.getIndex=function(child)
{
return mxUtils.indexOf(this.children,child);
};
mxCell.prototype.getChildAt=function(index)
{
return(this.children==null)?null:this.children[index];
};
mxCell.prototype.insert=function(child,index)
{
if(child!=null)
{
index=(index!=null)?index:this.getChildCount();
child.removeFromParent();
child.setParent(this);
if(this.children==null)
{
this.children=new Array();
this.children.push(child);
}
else
{
this.children.splice(index,0,child);
}
}
return child;
};
mxCell.prototype.remove=function(index)
{
var child=null;
if(this.children!=null&&index>=0)
{
child=this.getChildAt(index);
if(child!=null)
{
this.children.splice(index,1);
child.setParent(null);
}
}
return child;
};
mxCell.prototype.removeFromParent=function()
{
if(this.parent!=null)
{
var index=this.parent.getIndex(this);
this.parent.remove(index);
}
};
mxCell.prototype.getEdgeCount=function()
{
return(this.edges==null)?0:this.edges.length;
};
mxCell.prototype.getEdgeIndex=function(edge)
{
return mxUtils.indexOf(this.edges,edge);
};
mxCell.prototype.getEdgeAt=function(index)
{
return(this.edges==null)?null:this.edges[index];
};
mxCell.prototype.insertEdge=function(edge,isOutgoing)
{
if(edge!=null)
{
edge.removeFromTerminal(isOutgoing);
edge.setTerminal(this,isOutgoing);
if(this.edges==null||edge.getTerminal(!isOutgoing)!=this||mxUtils.indexOf(this.edges,edge)<0)
{
if(this.edges==null)
{
this.edges=new Array();
}
this.edges.push(edge);
}
}
return edge;
};
mxCell.prototype.removeEdge=function(edge,isOutgoing)
{
if(edge!=null)
{
if(edge.getTerminal(!isOutgoing)!=this&&this.edges!=null)
{
var index=this.getEdgeIndex(edge);
if(index>=0)
{
this.edges.splice(index,1);
}
}
edge.setTerminal(null,isOutgoing);
}
return edge;
};
mxCell.prototype.removeFromTerminal=function(isSource)
{
var terminal=this.getTerminal(isSource);
if(terminal!=null)
{
terminal.removeEdge(this,isSource);
}
};
mxCell.prototype.getAttribute=function(name,defaultValue)
{
var userObject=this.getValue();
var value=(userObject!=null&&userObject.nodeType==mxConstants.NODETYPE_ELEMENT)?userObject.getAttribute(name):null;
return value||defaultValue;
};
mxCell.prototype.setAttribute=function(name,value)
{
var userObject=this.getValue();
if(userObject!=null&&userObject.nodeType==mxConstants.NODETYPE_ELEMENT)
{
userObject.setAttribute(name,value);
}
};
mxCell.prototype.clone=function()
{
var clone=mxUtils.clone(this,this.mxTransient);
clone.setValue(this.cloneValue());
return clone;
};
mxCell.prototype.cloneValue=function()
{
var value=this.getValue();
if(value!=null)
{
if(typeof(value.clone)=='function')
{
value=value.clone();
}
else if(!isNaN(value.nodeType))
{
value=value.cloneNode(true);
}
}
return value;
};
}

{
function mxGeometry(x,y,width,height)
{
mxRectangle.call(this,x,y,width,height);
};
mxGeometry.prototype=new mxRectangle();
mxGeometry.prototype.constructor=mxGeometry;
mxGeometry.prototype.alternateBounds=null;
mxGeometry.prototype.sourcePoint=null;
mxGeometry.prototype.targetPoint=null;
mxGeometry.prototype.points=null;
mxGeometry.prototype.offset=null;
mxGeometry.prototype.relative=false;
mxGeometry.prototype.swap=function()
{
if(this.alternateBounds!=null)
{
var old=new mxRectangle(this.x,this.y,this.width,this.height);
this.x=this.alternateBounds.x;
this.y=this.alternateBounds.y;
this.width=this.alternateBounds.width;
this.height=this.alternateBounds.height;
this.alternateBounds=old;
}
};
mxGeometry.prototype.getTerminalPoint=function(isSource)
{
return(isSource)?this.sourcePoint:this.targetPoint;
};
mxGeometry.prototype.setTerminalPoint=function(point,isSource)
{
if(isSource)
{
this.sourcePoint=point;
}
else
{
this.targetPoint=point;
}
return point;
};
mxGeometry.prototype.translate=function(dx,dy)
{
var clone=this.clone();
if(!clone.relative)
{
clone.x+=dx;
clone.y+=dy;
}
if(clone.sourcePoint!=null)
{
clone.sourcePoint.x+=dx;
clone.sourcePoint.y+=dy;
}
if(clone.targetPoint!=null)
{
clone.targetPoint.x+=dx;
clone.targetPoint.y+=dy;
}
if(clone.points!=null)
{
var count=clone.points.length;
for(var i=0;i<count;i++)
{
var pt=clone.points[i];
pt.x+=dx;
pt.y+=dy;
}
}
return clone;
};
}

var mxCellPath=
{
PATH_SEPARATOR:'.',
create:function(cell)
{
var result='';
if(cell!=null)
{
var parent=cell.getParent();
while(parent!=null)
{
var index=parent.getIndex(cell);
result=index+mxCellPath.PATH_SEPARATOR+result;
cell=parent;
parent=cell.getParent();
}
}
var n=result.length;
if(n>1)
{
result=result.substring(0,n-1);
}
return result;
},
getParentPath:function(path)
{
if(path!=null)
{
var index=path.lastIndexOf(mxCellPath.PATH_SEPARATOR);
if(index>=0)
{
return path.substring(0,index);
}
else if(path.length>0)
{
return '';
}
}
return null;
},
resolve:function(root,path)
{
var parent=root;
if(path!=null)
{
var tokens=path.split(mxCellPath.PATH_SEPARATOR);
for(var i=0;i<tokens.length;i++)
{
parent=parent.getChildAt(parseInt(tokens[i]));
}
}
return parent;
}
};

var mxPerimeter=
{
RectanglePerimeter:function(bounds,edgeState,terminalState,isSource,next)
{
var cx=bounds.x+bounds.width/2;
var cy=bounds.y+bounds.height/2;
var dx=next.x-cx;
var dy=next.y-cy;
var alpha=Math.atan2(dy,dx);
var p=new mxPoint(0,0);
var pi=Math.PI;
var pi2=Math.PI/2;
var beta=pi2-alpha;
var t=Math.atan2(bounds.height,bounds.width);
if(alpha<-pi+t||alpha>pi-t)
{
p.x=bounds.x;
p.y=cy-bounds.width*Math.tan(alpha)/2;
}
else if(alpha<-t)
{
p.y=bounds.y;
p.x=cx-bounds.height*Math.tan(beta)/2;
}
else if(alpha<t)
{
p.x=bounds.x+bounds.width;
p.y=cy+bounds.width*Math.tan(alpha)/2;
}
else
{
p.y=bounds.y+bounds.height;
p.x=cx+bounds.height*Math.tan(beta)/2;
}
if(edgeState!=null&&edgeState.view.graph.isOrthogonal(edgeState,terminalState))
{
if(next.x>=bounds.x&&next.x<=bounds.x+bounds.width)
{
p.x=next.x;
}
else if(next.y>=bounds.y&&next.y<=bounds.y+bounds.height)
{
p.y=next.y;
}
if(next.x<bounds.x)
{
p.x=bounds.x;
}
else if(next.x>bounds.x+bounds.width)
{
p.x=bounds.x+bounds.width;
}
if(next.y<bounds.y)
{
p.y=bounds.y;
}
else if(next.y>bounds.y+bounds.height)
{
p.y=bounds.y+bounds.height;
}
}
return p;
},
EllipsePerimeter:function(bounds,edgeState,terminalState,isSource,next)
{
var x=bounds.x;
var y=bounds.y;
var a=bounds.width/2;
var b=bounds.height/2;
var cx=x+a;
var cy=y+b;
var px=next.x;
var py=next.y;

var dx=px-cx;
var dy=py-cy;
if(dx==0&&dy!=0)
{
return new mxPoint(cx,cy+b*dy/Math.abs(dy));
}
var orthogonal=edgeState!=null&&edgeState.view.graph.isOrthogonal(edgeState,terminalState);
if(orthogonal)
{
if(py>=y&&py<=y+bounds.height)
{
var ty=py-cy;
var tx=Math.sqrt(a*a*(1-(ty*ty)/(b*b)))||0;
if(px<=x)
{
tx=-tx;
}
return new mxPoint(cx+tx,py);
}
if(px>=x&&px<=x+bounds.width)
{
var tx=px-cx;
var ty=Math.sqrt(b*b*(1-(tx*tx)/(a*a)))||0;
if(py<=y)
{
ty=-ty;
}
return new mxPoint(px,cy+ty);
}
}
var d=dy/dx;
var h=cy-d*cx;
var e=a*a*d*d+b*b;
var f=-2*cx*e;
var g=a*a*d*d*cx*cx+b*b*cx*cx-a*a*b*b;
var det=Math.sqrt(f*f-4*e*g);
var xout1=(-f+det)/(2*e);
var xout2=(-f-det)/(2*e);
var yout1=d*xout1+h;
var yout2=d*xout2+h;
var dist1=Math.sqrt(Math.pow((xout1-px),2)+Math.pow((yout1-py),2));
var dist2=Math.sqrt(Math.pow((xout2-px),2)+Math.pow((yout2-py),2));
var xout=0;
var yout=0;
if(dist1<dist2)
{
xout=xout1;
yout=yout1;
}
else
{
xout=xout2;
yout=yout2;
}
return new mxPoint(xout,yout);
},
RhombusPerimeter:function(bounds,edgeState,terminalState,isSource,next)
{
var x=bounds.x;
var y=bounds.y;
var w=bounds.width;
var h=bounds.height;
var cx=x+w/2;
var cy=y+h/2;
var px=next.x;
var py=next.y;
if(cx==px)
{
if(cy>py)
{
return new mxPoint(cx,y);
}
else
{
return new mxPoint(cx,y+h);
}
}
else if(cy==py)
{
if(cx>px)
{
return new mxPoint(x,cy);
}
else
{
return new mxPoint(x+w,cy);
}
}
var tx=cx;
var ty=cy;
if(edgeState!=null&&edgeState.view.graph.isOrthogonal(edgeState,terminalState))
{
if(px>=x&&px<=x+w)
{
tx=px;
}
else if(py>=y&&py<=y+h)
{
ty=py;
}
}

if(px<cx)
{
if(py<cy)
{
return mxUtils.intersection(px,py,tx,ty,cx,y,x,cy);
}
else
{
return mxUtils.intersection(px,py,tx,ty,cx,y+h,x,cy);
}
}
else if(py<cy)
{
return mxUtils.intersection(px,py,tx,ty,cx,y,x+w,cy);
}
else
{
return mxUtils.intersection(px,py,tx,ty,cx,y+h,x+w,cy);
}
},
TrianglePerimeter:function(bounds,edgeState,terminalState,isSource,next)
{
var orthogonal=edgeState!=null&&edgeState.view.graph.isOrthogonal(edgeState,terminalState);
var direction=(terminalState!=null)?terminalState.style[mxConstants.STYLE_DIRECTION]:null;
var vertical=direction==mxConstants.DIRECTION_NORTH||direction==mxConstants.DIRECTION_SOUTH;
var x=bounds.x;
var y=bounds.y;
var w=bounds.width;
var h=bounds.height;
var cx=x+w/2;
var cy=y+h/2;
var start=new mxPoint(x,y);
var corner=new mxPoint(x+w,cy);
var end=new mxPoint(x,y+h);
if(direction==mxConstants.DIRECTION_NORTH)
{
start=end;
corner=new mxPoint(cx,y);
end=new mxPoint(x+w,y+h);
}
else if(direction==mxConstants.DIRECTION_SOUTH)
{
corner=new mxPoint(cx,y+h);
end=new mxPoint(x+w,y);
}
else if(direction==mxConstants.DIRECTION_WEST)
{
start=new mxPoint(x+w,y);
corner=new mxPoint(x,cy);
end=new mxPoint(x+w,y+h);
}
var dx=next.x-cx;
var dy=next.y-cy;
var alpha=(vertical)?Math.atan2(dx,dy):Math.atan2(dy,dx);
var t=(vertical)?Math.atan2(w,h):Math.atan2(h,w);
var base=false;
if(direction==mxConstants.DIRECTION_NORTH||direction==mxConstants.DIRECTION_WEST)
{
base=alpha>-t&&alpha<t;
}
else
{
base=alpha<-Math.PI+t||alpha>Math.PI-t;
}
var result=null;
if(base)
{
if(orthogonal&&((vertical&&next.x>=start.x&&next.x<=end.x)||(!vertical&&next.y>=start.y&&next.y<=end.y)))
{
if(vertical)
{
result=new mxPoint(next.x,start.y);
}
else
{
result=new mxPoint(start.x,next.y);
}
}
else
{
if(direction==mxConstants.DIRECTION_NORTH)
{
result=new mxPoint(x+w/2+h*Math.tan(alpha)/2,y+h);
}
else if(direction==mxConstants.DIRECTION_SOUTH)
{
result=new mxPoint(x+w/2-h*Math.tan(alpha)/2,y);
}
else if(direction==mxConstants.DIRECTION_WEST)
{
result=new mxPoint(x+w,y+h/2+w*Math.tan(alpha)/2);
}
else
{
result=new mxPoint(x,y+h/2-w*Math.tan(alpha)/2);
}
}
}
else
{
if(orthogonal)
{
var pt=new mxPoint(cx,cy);
if(next.y>=y&&next.y<=y+h)
{
pt.x=(vertical)?cx:((direction==mxConstants.DIRECTION_WEST)?x+w:x);
pt.y=next.y;
}
else if(next.x>=x&&next.x<=x+w)
{
pt.x=next.x;
pt.y=(!vertical)?cy:((direction==mxConstants.DIRECTION_NORTH)?y+h:y);
}
dx=next.x-pt.x;
dy=next.y-pt.y;
cx=pt.x;
cy=pt.y;
}
if((vertical&&next.x<=x+w/2)||(!vertical&&next.y<=y+h/2))
{
result=mxUtils.intersection(next.x,next.y,cx,cy,start.x,start.y,corner.x,corner.y);
}
else
{
result=mxUtils.intersection(next.x,next.y,cx,cy,corner.x,corner.y,end.x,end.y);
}
}
if(result==null)
{
result=new mxPoint(cx,cy);
}
return result;
}
};

{
function mxStylesheet()
{
this.styles=new Object();
this.putDefaultVertexStyle(this.createDefaultVertexStyle());
this.putDefaultEdgeStyle(this.createDefaultEdgeStyle());
};
mxStylesheet.prototype.styles;
mxStylesheet.prototype.createDefaultVertexStyle=function()
{
var style=new Object();
style[mxConstants.STYLE_SHAPE]=mxConstants.SHAPE_RECTANGLE;
style[mxConstants.STYLE_PERIMETER]=mxPerimeter.RectanglePerimeter;
style[mxConstants.STYLE_VERTICAL_ALIGN]=mxConstants.ALIGN_MIDDLE;
style[mxConstants.STYLE_ALIGN]=mxConstants.ALIGN_CENTER;
style[mxConstants.STYLE_FILLCOLOR]='#C3D9FF';
style[mxConstants.STYLE_STROKECOLOR]='#6482B9';
style[mxConstants.STYLE_FONTCOLOR]='#774400';
return style;
};
mxStylesheet.prototype.createDefaultEdgeStyle=function()
{
var style=new Object();
style[mxConstants.STYLE_SHAPE]=mxConstants.SHAPE_CONNECTOR;
style[mxConstants.STYLE_ENDARROW]=mxConstants.ARROW_CLASSIC;
style[mxConstants.STYLE_VERTICAL_ALIGN]=mxConstants.ALIGN_MIDDLE;
style[mxConstants.STYLE_ALIGN]=mxConstants.ALIGN_CENTER;
style[mxConstants.STYLE_STROKECOLOR]='#6482B9';
style[mxConstants.STYLE_FONTCOLOR]='#446299';
return style;
};
mxStylesheet.prototype.putDefaultVertexStyle=function(style)
{
this.putCellStyle('defaultVertex',style);
};
mxStylesheet.prototype.putDefaultEdgeStyle=function(style)
{
this.putCellStyle('defaultEdge',style);
};
mxStylesheet.prototype.getDefaultVertexStyle=function()
{
return this.styles['defaultVertex'];
};
mxStylesheet.prototype.getDefaultEdgeStyle=function()
{
return this.styles['defaultEdge'];
};
mxStylesheet.prototype.putCellStyle=function(name,style)
{
this.styles[name]=style;
};
mxStylesheet.prototype.getCellStyle=function(name,defaultStyle)
{
var style=defaultStyle;
if(name!=null&&name.length>0)
{
var pairs=name.split(';');
if(pairs!=null&&pairs.length>0)
{
if(style!=null&&pairs[0].indexOf('=')>=0)
{
style=mxUtils.clone(style);
}
else
{
style=new Object();
}
for(var i=0;i<pairs.length;i++)
{
var tmp=pairs[i];
var pos=tmp.indexOf('=');
if(pos>=0)
{
var key=tmp.substring(0,pos);
var value=tmp.substring(pos+1);
if(value==mxConstants.NONE)
{
delete style[key];
}
else if(mxUtils.isNumeric(value))
{
style[key]=parseFloat(value);
}
else
{

if(value.indexOf('.')>0)
{
try
{
var tmp=mxUtils.eval(value);
if(typeof(tmp)=='function')
{
value=tmp;
}
}
catch(e)
{
}
}
style[key]=value;
}
}
else
{
var tmpStyle=this.styles[tmp];
if(tmpStyle!=null)
{
for(var key in tmpStyle)
{
style[key]=tmpStyle[key];
}
}
}
}
}
}
return style;
};
}

{
function mxCellState(view,cell,style)
{
this.view=view;
this.cell=cell;
this.style=style;
this.origin=new mxPoint();
this.absoluteOffset=new mxPoint();
};
mxCellState.prototype.view=null;
mxCellState.prototype.cell=null;
mxCellState.prototype.style=null;
mxCellState.prototype.x=0;
mxCellState.prototype.y=0;
mxCellState.prototype.width=0;
mxCellState.prototype.height=0;
mxCellState.prototype.invalid=true;
mxCellState.prototype.origin=null;
mxCellState.prototype.absolutePoints=null;
mxCellState.prototype.absoluteOffset=null;
mxCellState.prototype.terminalDistance=0;
mxCellState.prototype.length=0;
mxCellState.prototype.segments=null;
mxCellState.prototype.shape=null;
mxCellState.prototype.text=null;
mxCellState.prototype.getCenterX=function()
{
return this.x+this.width/2;
};
mxCellState.prototype.getCenterY=function()
{
return this.y+this.height/2;
};
mxCellState.prototype.getRoutingCenterX=function()
{
if(this.style!=null)
{
var f=parseFloat(this.style[mxConstants.STYLE_ROUTING_CENTER_X])||0;
return this.getCenterX()+f*this.width;
}
return this.getCenterX();
};
mxCellState.prototype.getRoutingCenterY=function()
{
if(this.style!=null)
{
var f=parseFloat(this.style[mxConstants.STYLE_ROUTING_CENTER_Y])||0;
return this.getCenterY()+f*this.height;
}
return this.getCenterY();
};
mxCellState.prototype.getPerimeterBounds=function(border)
{
border=border||0;
var bounds=new mxRectangle(this.x,this.y,this.width,this.height);
bounds.grow(border);
return bounds;
};
mxCellState.prototype.setAbsoluteTerminalPoint=function(point,isSource)
{
if(isSource)
{
if(this.absolutePoints==null||this.absolutePoints.length==0)
{
this.absolutePoints=new Array();
this.absolutePoints.push(point);
}
else
{
this.absolutePoints[0]=point;
}
}
else
{
if(this.absolutePoints==null)
{
this.absolutePoints=new Array();
this.absolutePoints.push(null);
this.absolutePoints.push(point);
}
else if(this.absolutePoints.length==1)
{
this.absolutePoints.push(point);
}
else
{
this.absolutePoints[this.absolutePoints.length-1]=point;
}
}
};
mxCellState.prototype.destroy=function()
{
this.view.graph.cellRenderer.destroy(this);
this.view.graph.destroyHandler(this);
};
mxCellState.prototype.clone=function()
{
var clone=new mxCellState(this.view,this.cell,this.style);
if(this.absolutePoints!=null)
{
clone.absolutePoints=new Array();
for(i=0;i<this.absolutePoints.length;i++)
{
clone.absolutePoints.push(this.absolutePoints[i].clone());
}
}
if(this.origin!=null)
{
clone.origin=this.origin.clone();
}
if(this.absoluteOffset!=null)
{
clone.absoluteOffset=this.absoluteOffset.clone();
}
if(this.sourcePoint!=null)
{
clone.sourcePoint=this.sourcePoint.clone();
}
if(this.boundingBox!=null)
{
clone.boundingBox=this.boundingBox.clone();
}
clone.terminalDistance=this.terminalDistance;
clone.segments=this.segments;
clone.length=this.length;
clone.x=this.x;
clone.y=this.y;
clone.width=this.width;
clone.height=this.height;
return clone;
};
}

{
function mxGraphSelection(graph)
{
this.graph=graph;
this.cells=new Array();
};
mxGraphSelection.prototype=new mxEventSource();
mxGraphSelection.prototype.constructor=mxGraphSelection;
mxGraphSelection.prototype.doneResource=(mxClient.language!='none')?'done':'';
mxGraphSelection.prototype.updatingSelectionResource=(mxClient.language!='none')?'updatingSelection':'';
mxGraphSelection.prototype.graph=null;
mxGraphSelection.prototype.singleSelection=false;
mxGraphSelection.prototype.isSingleSelection=function()
{
return this.singleSelection;
};
mxGraphSelection.prototype.setSingleSelection=function(singleSelection)
{
this.singleSelection=singleSelection;
};
mxGraphSelection.prototype.isSelected=function(cell)
{
if(cell==null)
{
return false;
}
else
{
var state=this.graph.getView().getState(cell);
return this.graph.hasHandler(state);
}
};
mxGraphSelection.prototype.clear=function()
{
this.changeSelection(null,this.cells);
};
mxGraphSelection.prototype.setCell=function(cell)
{
if(cell!=null)
{
this.setCells([cell]);
}
};
mxGraphSelection.prototype.setCells=function(cells)
{
if(cells!=null)
{
if(this.singleSelection)
{
cells=[this.getFirstSelectableCell(cells)];
}
var tmp=new Array();
for(var i=0;i<cells.length;i++)
{
if(this.graph.isSelectable(cells[i]))
{
tmp.push(cells[i]);
}
}
this.changeSelection(tmp,this.cells);
}
};
mxGraphSelection.prototype.getFirstSelectableCell=function(cells)
{
if(cells!=null)
{
for(var i=0;i<cells.length;i++)
{
if(this.graph.isSelectable(cells[i]))
{
return cells[i];
}
}
}
return null;
};
mxGraphSelection.prototype.addCell=function(cell)
{
if(cell!=null)
{
this.addCells([cell]);
}
};
mxGraphSelection.prototype.addCells=function(cells)
{
if(cells!=null)
{
var remove=null;
if(this.singleSelection)
{
remove=this.cells;
cells=[this.getFirstSelectableCell(cells)];
}
var tmp=new Array();
for(var i=0;i<cells.length;i++)
{
if(!this.isSelected(cells[i])&&this.graph.isSelectable(cells[i]))
{
tmp.push(cells[i]);
}
}
this.changeSelection(tmp,remove);
}
};
mxGraphSelection.prototype.removeCell=function(cell)
{
if(cell!=null)
{
this.removeCells([cell]);
}
};
mxGraphSelection.prototype.removeCells=function(cells)
{
if(cells!=null)
{
var tmp=new Array();
for(var i=0;i<cells.length;i++)
{
if(this.isSelected(cells[i]))
{
tmp.push(cells[i]);
}
}
this.changeSelection(null,tmp);
}
};
mxGraphSelection.prototype.changeSelection=function(added,removed)
{
if((added!=null&&added.length>0&&added[0]!=null)||(removed!=null&&removed.length>0&&removed[0]!=null))
{
var change=new mxSelectionChange(this,added,removed);
change.execute();
var edit=new mxUndoableEdit(this,false);
edit.add(change);
this.dispatchEvent('undo',this,edit);
}
};
mxGraphSelection.prototype.cellAdded=function(cell)
{
if(cell!=null)
{
var state=this.graph.getView().getState(cell);
if(state!=null&&!this.graph.hasHandler(state))
{
this.graph.createHandler(state);
this.cells.push(cell);
}
}
};
mxGraphSelection.prototype.cellRemoved=function(cell)
{
if(cell!=null)
{
var index=mxUtils.indexOf(this.cells,cell);
if(index>=0)
{
var state=this.graph.getView().getState(cell);
if(state!=null)
{
this.graph.destroyHandler(state);
}
this.cells.splice(index,1);
}
}
};
function mxSelectionChange(selection,added,removed)
{
this.selection=selection;
this.added=(added!=null)?added.slice():null;
this.removed=(removed!=null)?removed.slice():null;
};
mxSelectionChange.prototype.execute=function()
{
var t0=mxLog.enter('mxSelectionChange.execute');
window.status=mxResources.get(this.selection.updatingSelectionResource)||this.selection.updatingSelectionResource;
if(this.removed!=null)
{
for(var i=0;i<this.removed.length;i++)
{
this.selection.cellRemoved(this.removed[i]);
}
}
if(this.added!=null)
{
for(var i=0;i<this.added.length;i++)
{
this.selection.cellAdded(this.added[i]);
}
}
var tmp=this.added;
this.added=this.removed;
this.removed=tmp;
window.status=mxResources.get(this.selection.doneResource)||this.selection.doneResource;
mxLog.leave('mxSelectionChange.execute',t0);
this.selection.dispatchEvent('change',this,this.removed,this.added);
};
}

{
function mxCellEditor(graph)
{
this.graph=graph;
this.textarea=document.createElement('textarea');
this.textarea.className='mxCellEditor';
this.textarea.style.position='absolute';
this.textarea.style.overflow='visible';
this.textarea.setAttribute('cols','20');
this.textarea.setAttribute('rows','4');

this.init();
};
mxCellEditor.prototype.graph=null;
mxCellEditor.prototype.textarea=null;
mxCellEditor.prototype.cell=null;
mxCellEditor.prototype.emptyLabelText='';
mxCellEditor.prototype.init=function()
{
var self=this;
mxEvent.addListener(this.textarea,'blur',function(evt)
{
self.stopEditing(false);
});
mxEvent.addListener(this.textarea,'keydown',function(evt)
{
if(self.clearOnChange)
{
self.clearOnChange=false;
self.textarea.value='';
}
self.modified=true;
});
};
mxCellEditor.prototype.startEditing=function(cell,trigger)
{
this.stopEditing(true);
var state=this.graph.getView().getState(cell);
if(state!=null)
{
this.cell=cell;
this.trigger=trigger;
this.textNode=null;
if(state.text!=null)
{
if(this.isHideLabel(state))
{
this.textNode=state.text.node;
this.textNode.style.visibility='hidden';
}
var scale=this.graph.getView().scale;
this.textarea.style.fontSize=state.text.size*scale;
this.textarea.style.fontFamily=state.text.family;
this.textarea.style.color=state.text.color;
if(this.textarea.style.color=='white')
{
this.textarea.style.color='black';
}
this.textarea.style.textAlign=(this.graph.model.isEdge(state.cell))?'left':(state.text.align||'left');
this.textarea.style.fontWeight=
state.text.isStyleSet(mxConstants.FONT_BOLD)?'bold':'normal';
}
var bounds=this.getEditorBounds(state);
this.textarea.style.left=bounds.x+'px';
this.textarea.style.top=bounds.y+'px';
this.textarea.style.width=bounds.width+'px';
this.textarea.style.height=bounds.height+'px';
this.textarea.style.zIndex=5;
var value=this.graph.getEditingValue(cell,trigger);


if(value==null||value.length==0)
{
value=this.getEmptyLabelText();
this.clearOnChange=true;
}
else
{
this.clearOnChange=false;
}
this.modified=false;
this.textarea.value=value;
this.graph.container.appendChild(this.textarea);
this.textarea.focus();
this.textarea.select();
}
};
mxCellEditor.prototype.isHideLabel=function(state)
{
return true;
};
mxCellEditor.prototype.getEditorBounds=function(state)
{
var scale=this.graph.getView().scale;
var minHeight=(state.text==null)?30:state.text.size*scale+20;
var minWidth=(this.textarea.style.textAlign=='left')?120:40;
var spacing=parseInt(state.style[mxConstants.STYLE_SPACING]||2)*scale;
var spacingTop=(parseInt(state.style[mxConstants.STYLE_SPACING_TOP]||0))*scale+spacing;
var spacingRight=(parseInt(state.style[mxConstants.STYLE_SPACING_RIGHT]||0))*scale+spacing;
var spacingBottom=(parseInt(state.style[mxConstants.STYLE_SPACING_BOTTOM]||0))*scale+spacing;
var spacingLeft=(parseInt(state.style[mxConstants.STYLE_SPACING_LEFT]||0))*scale+spacing;
var result=new mxRectangle(state.x,state.y,Math.max(minWidth,state.width-spacingLeft-spacingRight),Math.max(minHeight,state.height-spacingTop-spacingBottom));
if(this.graph.getModel().isEdge(state.cell))
{
result.x=state.absoluteOffset.x;
result.y=state.absoluteOffset.y;
if(state.text!=null&&state.text.boundingBox!=null)
{
result.x=state.text.boundingBox.x;
result.y=state.text.boundingBox.y;
}
}
else if(state.text!=null&&state.text.boundingBox!=null)
{
result.x=Math.min(result.x,state.text.boundingBox.x);
result.y=Math.min(result.y,state.text.boundingBox.y);
}
result.x+=spacingLeft;
result.y+=spacingTop;
if(state.text!=null&&state.text.boundingBox!=null)
{
result.width=Math.max(result.width,state.text.boundingBox.width);
result.height=Math.max(result.height,state.text.boundingBox.height);
}
return result;
};
mxCellEditor.prototype.getEmptyLabelText=function(cell)
{
return this.emptyLabelText;
};
mxCellEditor.prototype.isEditing=function(cell)
{
return(cell==null)?this.cell!=null:this.cell==cell;
};
mxCellEditor.prototype.stopEditing=function(cancel)
{
cancel=cancel||false;
if(this.cell!=null)
{
if(this.textNode!=null)
{
this.textNode.style.visibility='visible';
this.textNode=null;
}
if(!cancel&&this.modified)
{
this.graph.labelChanged(this.cell,this.textarea.value.replace(/\r/g,''),this.trigger);
}
this.cell=null;
this.trigger=null;
this.textarea.blur();
this.textarea.parentNode.removeChild(this.textarea);
}
};
mxCellEditor.prototype.destroy=function()
{
mxUtils.release(this.textarea);
if(this.textarea.parentNode!=null)
{
this.textarea.parentNode.removeChild(this.textarea);
}
this.textarea=null;
};
}

{
function mxCellRenderer()
{
this.shapes=mxUtils.clone(this.defaultShapes);
};
mxCellRenderer.prototype.collapseExpandResource=(mxClient.language!='none')?'collapse-expand':'';
mxCellRenderer.prototype.shapes=null;
mxCellRenderer.prototype.checkStyleChanged=true;
mxCellRenderer.prototype.defaultShapes=new Object();
mxCellRenderer.prototype.defaultShapes[mxConstants.SHAPE_ARROW]=mxArrow;
mxCellRenderer.prototype.defaultShapes[mxConstants.SHAPE_RECTANGLE]=mxRectangleShape;
mxCellRenderer.prototype.defaultShapes[mxConstants.SHAPE_ELLIPSE]=mxEllipse;
mxCellRenderer.prototype.defaultShapes[mxConstants.SHAPE_DOUBLE_ELLIPSE]=mxDoubleEllipse;
mxCellRenderer.prototype.defaultShapes[mxConstants.SHAPE_RHOMBUS]=mxRhombus;
mxCellRenderer.prototype.defaultShapes[mxConstants.SHAPE_IMAGE]=mxImageShape;
mxCellRenderer.prototype.defaultShapes[mxConstants.SHAPE_LINE]=mxLine;
mxCellRenderer.prototype.defaultShapes[mxConstants.SHAPE_LABEL]=mxLabel;
mxCellRenderer.prototype.defaultShapes[mxConstants.SHAPE_CYLINDER]=mxCylinder;
mxCellRenderer.prototype.defaultShapes[mxConstants.SHAPE_SWIMLANE]=mxSwimlane;
mxCellRenderer.prototype.defaultShapes[mxConstants.SHAPE_CONNECTOR]=mxConnector;
mxCellRenderer.prototype.defaultShapes[mxConstants.SHAPE_ACTOR]=mxActor;
mxCellRenderer.prototype.defaultShapes[mxConstants.SHAPE_CLOUD]=mxCloud;
mxCellRenderer.prototype.defaultShapes[mxConstants.SHAPE_TRIANGLE]=mxTriangle;
mxCellRenderer.prototype.defaultShapes[mxConstants.SHAPE_HEXAGON]=mxHexagon;
mxCellRenderer.prototype.registerShape=function(key,shape)
{
this.shapes[key]=shape;
};
mxCellRenderer.prototype.initialize=function(state)
{
var model=state.view.graph.getModel();
if(state.view.graph.container!=null&&state.shape==null&&state.cell!=state.view.currentRoot&&(model.isVertex(state.cell)||model.isEdge(state.cell)))
{
this.createShape(state);
if(state.shape!=null)
{
state.shape.init(state.view.getDrawPane());
if(state.view.graph.ordered)
{
this.order(state);
}
else if(model.isEdge(state.cell))
{
this.orderEdge(state);
}
else if(state.view.graph.keepEdgesInForeground&&this.firstEdge!=null)
{
if(this.firstEdge.parentNode==
state.shape.node.parentNode)
{
state.shape.node.parentNode.insertBefore(state.shape.node,this.firstEdge);
}
else
{
this.firstEdge=null;
}
}
state.shape.scale=state.view.scale;
this.createLabel(state);
this.createOverlay(state);
this.createControl(state);
this.installListeners(state);
}

var cells=state.view.graph.getSelectionCells();
for(var i=0;i<cells.length;i++)
{
if(cells[i]==state.cell)
{
state.doCreateHandler=true;
break;
}
}
}
};
mxCellRenderer.prototype.order=function(state)
{

var view=state.view;
var model=view.graph.getModel();
var previous=model.getParent(state.cell);
var index=previous.getIndex(state.cell);
var previousState=null;
while(index>0)
{
previous=model.getChildAt(previous,index-1);
previousState=view.getState(previous);
if(previousState!=null)
{
break;
}
index--;
}
if(previousState!=null)
{
var childCount=model.getChildCount(previous);
while(childCount>0)
{
previousState=null;
var tmp=model.getChildAt(previous,childCount-1);
var tmpState=view.getState(tmp);
if(tmpState==null)
{
break;
}
else
{
previousState=tmpState;
}
previous=tmp;
childCount=model.getChildCount(previous);
}
}
if(previousState==null)
{
previousState=view.getState(previous);
}
var node=null;
if(previousState!=null&&previousState.shape!=null)
{
if(previousState.text!=null)
{
node=previousState.text.node;
}

if(node==null||node.parentNode==previousState.shape.node||node.parentNode!=previousState.shape.node.parentNode)
{
node=previousState.shape.node;
}
if(node!=null)
{
node=node.nextSibling;
}
}
else
{
node=state.shape.node.parentNode.firstChild;
}
if(node!=null)
{
node.parentNode.insertBefore(state.shape.node,node);
}
};
mxCellRenderer.prototype.orderEdge=function(state)
{
var view=state.view;
var model=view.graph.getModel();
if(view.graph.keepEdgesInForeground)
{
var node=state.shape.node;
if(this.firstEdge==null||this.firstEdge.parentNode==null||this.firstEdge.parentNode!=state.shape.node.parentNode)
{
this.firstEdge=state.shape.node;
}
}
else if(view.graph.keepEdgesInBackground)
{
var node=state.shape.node;
var parent=node.parentNode;
var pcell=model.getParent(state.cell);
var pstate=view.getState(pcell);
if(pstate!=null&&pstate.shape!=null&&pstate.shape.node!=null)
{
var child=pstate.shape.node.nextSibling;
if(child!=null&&child!=node)
{
parent.insertBefore(node,child);
}
}
else
{
var child=parent.firstChild;
if(child!=null&&child!=node)
{
parent.insertBefore(node,child);
}
}
}
};
mxCellRenderer.prototype.createShape=function(state)
{
if(state.style!=null)
{
var graph=state.view.graph;
var isEdge=graph.getModel().isEdge(state.cell);
var key=state.style[mxConstants.STYLE_SHAPE];
var ctor=(key!=null)?this.shapes[key]:null;
if(ctor==null)
{
if(isEdge)
{
ctor=mxPolyline;
}
else
{
ctor=mxRectangleShape;
}
}
state.shape=new ctor();
if(isEdge)
{
state.shape.points=state.absolutePoints;
}
else
{
state.shape.bounds=new mxRectangle(state.x,state.y,state.width,state.height);
}
state.shape.dialect=state.view.graph.dialect;
this.configureShape(state);
}
};
mxCellRenderer.prototype.postConfigureShape=function(state)
{
if(state.shape!=null)
{
this.resolveColor(state,'indicatorColor',mxConstants.STYLE_FILLCOLOR);
this.resolveColor(state,'indicatorGradientColor',mxConstants.STYLE_GRADIENTCOLOR);
this.resolveColor(state,'fill',mxConstants.STYLE_FILLCOLOR);
this.resolveColor(state,'stroke',mxConstants.STYLE_STROKECOLOR);
this.resolveColor(state,'gradient',mxConstants.STYLE_GRADIENTCOLOR);
}
};
mxCellRenderer.prototype.resolveColor=function(state,field,key)
{
var value=state.shape[field];
var graph=state.view.graph;
var referenced=null;
if(value=='inherit')
{
referenced=graph.model.getParent(state.cell);
}
else if(value=='swimlane')
{
if(graph.model.getTerminal(state.cell,false)!=null)
{
referenced=graph.model.getTerminal(state.cell,false);
}
else
{
referenced=state.cell;
}
referenced=graph.getSwimlane(referenced);
key=graph.swimlaneIndicatorColorAttribute;
}
else if(value=='indicated')
{
state.shape[field]=state.shape.indicatorColor;
}
if(referenced!=null)
{
var rstate=graph.getView().getState(referenced);
state.shape[field]=null;
if(rstate!=null)
{
if(rstate.shape!=null&&field!='indicatorColor')
{
state.shape[field]=rstate.shape[field];
}
else
{
state.shape[field]=rstate.style[key];
}
}
}
};
mxCellRenderer.prototype.configureShape=function(state)
{
state.shape.apply(state);
var image=state.view.graph.getImage(state);
if(image!=null)
{
state.shape.image=image;
}
var indicator=state.view.graph.getIndicatorColor(state);
var key=state.view.graph.getIndicatorShape(state);
var ctor=(key!=null)?this.shapes[key]:null;
if(indicator!=null)
{
state.shape.indicatorShape=ctor;
state.shape.indicatorColor=indicator;
state.shape.indicatorGradientColor=
state.view.graph.getIndicatorGradientColor(state);
}
else
{
var indicator=state.view.graph.getIndicatorImage(state);
if(indicator!=null)
{
state.shape.indicatorImage=indicator;
}
}
this.postConfigureShape(state);
};
mxCellRenderer.prototype.getLabelValue=function(state)
{
var graph=state.view.graph;
var value=graph.getLabel(state.cell);
if(!graph.isHtmlLabel(state.cell)&&(value!=null&&!mxUtils.isNode(value))&&mxClient.IS_IE)
{
value=mxUtils.htmlEntities(value,false);
}
return value;
};
mxCellRenderer.prototype.createLabel=function(state)
{
var graph=state.view.graph;
var isEdge=graph.getModel().isEdge(state.cell);
if(state.style[mxConstants.STYLE_FONTSIZE]>0||state.style[mxConstants.STYLE_FONTSIZE]==null)
{
var value=this.getLabelValue(state);
if(isEdge&&(value==null||value.length==0))
{
return;
}
var isForceHtml=(graph.isHtmlLabel(state.cell)||(value!=null&&mxUtils.isNode(value)))&&graph.dialect==mxConstants.DIALECT_SVG;
var isRotate=state.style[mxConstants.STYLE_HORIZONTAL]==false;
state.text=new mxText(value,new mxRectangle(),state.style[mxConstants.STYLE_ALIGN],graph.getVerticalAlign(state),state.style[mxConstants.STYLE_FONTCOLOR],state.style[mxConstants.STYLE_FONTFAMILY],state.style[mxConstants.STYLE_FONTSIZE],state.style[mxConstants.STYLE_FONTSTYLE],state.style[mxConstants.STYLE_SPACING],state.style[mxConstants.STYLE_SPACING_TOP],state.style[mxConstants.STYLE_SPACING_RIGHT],state.style[mxConstants.STYLE_SPACING_BOTTOM],state.style[mxConstants.STYLE_SPACING_LEFT],isRotate,state.style[mxConstants.STYLE_LABEL_BACKGROUNDCOLOR],state.style[mxConstants.STYLE_LABEL_BORDERCOLOR],isEdge,isEdge||isForceHtml,graph.isWrapping(state.cell),graph.isClipping(state.cell));
state.text.opacity=state.style[mxConstants.STYLE_TEXT_OPACITY];
state.text.dialect=(isForceHtml)?mxConstants.DIALECT_STRICTHTML:
state.view.graph.dialect;
this.initializeLabel(state);
}
};
mxCellRenderer.prototype.initializeLabel=function(state)
{
var graph=state.view.graph;
if(state.text.dialect!=mxConstants.DIALECT_SVG)
{
if(graph.dialect==mxConstants.DIALECT_SVG)
{



var node=graph.container;
var overflow=node.style.overflow;
state.text.isAbsolute=true;
state.text.init(node);
node.style.overflow=overflow;
return;
}
else if(mxUtils.isVml(state.view.getDrawPane()))
{
if(state.shape.label!=null)
{
state.text.init(state.shape.label);
}
else
{
state.text.init(state.shape.node);
}
return;
}
}
state.text.init(state.view.getDrawPane());
state.text.isAbsolute=true;
if(state.shape!=null&&state.text!=null)
{
state.shape.node.parentNode.insertBefore(state.text.node,state.shape.node.nextSibling);
}
};
mxCellRenderer.prototype.createOverlay=function(state)
{
var graph=state.view.graph;
var overlays=graph.getOverlays(state.cell);
if(overlays!=null)
{
state.overlays=new Array();
for(var i=0;i<overlays.length;i++)
{
var tmp=new mxImageShape(new mxRectangle(),overlays[i].image.src);
tmp.dialect=state.view.graph.dialect;
tmp.init(state.view.getOverlayPane());
tmp.node.style.cursor='help';
this.installOverlayListeners(state,overlays[i],tmp);
state.overlays.push(tmp);
}
}
};
mxCellRenderer.prototype.installOverlayListeners=function(state,overlay,shape)
{
mxEvent.addListener(shape.node,'click',function(evt)
{
overlay.dispatchEvent('click',overlay,evt,state.cell);
});
mxEvent.addListener(shape.node,'mousedown',function(evt)
{
mxEvent.consume(evt);
});
mxEvent.addListener(shape.node,'mousemove',function(evt)
{
state.view.graph.dispatchGraphEvent('mousemove',evt,state.cell,overlay);
});
};
mxCellRenderer.prototype.createControl=function(state)
{
var graph=state.view.graph;
var image=graph.getFoldingImage(state);
if(graph.foldingEnabled&&image!=null)
{
var b=new mxRectangle(0,0,image.width,image.height);
state.control=new mxImageShape(b,image.src);
state.control.dialect=state.view.graph.dialect;




var isForceHtml=graph.isHtmlLabel(state.cell)&&state.view.graph.dialect==mxConstants.DIALECT_SVG;
if(isForceHtml)
{
state.control.dialect=mxConstants.DIALECT_PREFERHTML;
state.control.init(graph.container);
state.control.node.style.zIndex=1;
}
else
{
state.control.init(state.view.getOverlayPane());
}
var node=state.control.innerNode||state.control.node;
if(graph.isEnabled())
{
node.style.cursor='pointer';
}
mxEvent.addListener(node,'click',function(evt)
{
if(graph.isEnabled())
{
var cells=new Array();
cells[0]=state.cell;
if(graph.isCellCollapsed(state.cell))
{
graph.expand(cells);
}
else
{
graph.collapse(cells);
}
mxEvent.consume(evt);
}
});
mxEvent.addListener(node,'mousedown',function(evt)
{
graph.dispatchGraphEvent('mousedown',evt,state.cell);
mxEvent.consume(evt);
});
var self=this;
mxEvent.addListener(node,'mousemove',function(evt)
{
graph.dispatchGraphEvent('mousemove',evt,state.cell,mxResources.get(self.collapseExpandResource)||self.collapseExpandResource);
});
}
};
mxCellRenderer.prototype.installListeners=function(state)
{
var graph=state.view.graph;
if(graph.dialect==mxConstants.DIALECT_SVG)
{
var events='all';
if(graph.getModel().isEdge(state.cell)&&state.shape.stroke!=null&&state.shape.fill==null)
{
events='visibleStroke';
}
if(state.shape.innerNode!=null)
{
state.shape.innerNode.setAttribute('pointer-events',events);
}
else
{
state.shape.node.setAttribute('pointer-events',events);
}
}
var cursor=graph.getCursorForCell(state.cell);
if(cursor!=null||graph.isEnabled())
{
if(cursor==null)
{
if(graph.getModel().isEdge(state.cell))
{
cursor='pointer';
}
else if(graph.isMovable(state.cell))
{
cursor='move';
}
}
if(state.shape.innerNode!=null&&!graph.getModel().isEdge(state.cell))
{
state.shape.innerNode.style.cursor=cursor;
}
else
{
state.shape.node.style.cursor=cursor;
}
}
mxEvent.addListener(state.shape.node,'mousedown',function(evt)
{




if(state.shape!=null&&mxEvent.getSource(evt)==state.shape.content)
{
graph.dispatchGraphEvent('mousedown',evt);
}
else
{
graph.dispatchGraphEvent('mousedown',evt,state.cell);
}
});
mxEvent.addListener(state.shape.node,'mousemove',function(evt)
{
if(state.shape!=null&&mxEvent.getSource(evt)==state.shape.content)
{
graph.dispatchGraphEvent('mousemove',evt);
}
else
{
graph.dispatchGraphEvent('mousemove',evt,state.cell);
}
});
mxEvent.addListener(state.shape.node,'mouseup',function(evt)
{
if(state.shape!=null&&mxEvent.getSource(evt)==state.shape.content)
{
graph.dispatchGraphEvent('mouseup',evt);
}
else
{
graph.dispatchGraphEvent('mouseup',evt,state.cell);
}
});
mxEvent.addListener(state.shape.node,'dblclick',function(evt)
{
if(state.shape!=null&&mxEvent.getSource(evt)==state.shape.content)
{
graph.dblClick(evt);
}
else
{
graph.dblClick(evt,state.cell);
}
mxEvent.consume(evt);
});
if(state.text!=null)
{
var cursor=graph.getCursorForCell(state.cell);
if(cursor!=null||(graph.isEnabled()&&graph.isMovable(state.cell)))
{
state.text.node.style.cursor=cursor||'move';
}
mxEvent.addListener(state.text.node,'mousedown',function(evt)
{

if(graph.getModel().isEdge(state.cell)&&graph.isCellSelected(state.cell))
{
graph.dispatchGraphEvent('mousedown',evt,state.cell,mxEdgeHandler.prototype.LABEL_INDEX);
}
else
{
graph.dispatchGraphEvent('mousedown',evt,state.cell);
}
});
mxEvent.addListener(state.text.node,'mousemove',function(evt)
{
graph.dispatchGraphEvent('mousemove',evt,state.cell);
});
mxEvent.addListener(state.text.node,'mouseup',function(evt)
{
graph.dispatchGraphEvent('mouseup',evt,state.cell);
});
mxEvent.addListener(state.text.node,'dblclick',function(evt)
{
graph.dblClick(evt,state.cell);
mxEvent.consume(evt);
});
}
};
mxCellRenderer.prototype.redrawLabel=function(state)
{
if(state.text!=null)
{
var graph=state.view.graph;
var wrapping=graph.isWrapping(state.cell);
var clipping=graph.isClipping(state.cell);
var value=this.getLabelValue(state);
var bounds=this.getLabelBounds(state);
if(state.text.value!=value||state.text.isWrapping!=wrapping||state.text.isClipping!=clipping||state.text.scale!=state.view.scale||!mxUtils.equals(bounds,state.text.bounds))
{
state.text.value=value;
state.text.bounds=bounds;
state.text.scale=state.view.scale;
state.text.isWrapping=wrapping;
state.text.isClipping=clipping;
state.text.redraw();
}
}
};
mxCellRenderer.prototype.getLabelBounds=function(state)
{
var graph=state.view.graph;
var isEdge=graph.getModel().isEdge(state.cell);
var bounds=new mxRectangle(state.absoluteOffset.x,state.absoluteOffset.y);
if(!isEdge)
{
bounds.x+=state.shape.bounds.x;
bounds.y+=state.shape.bounds.y;
bounds.width=Math.max(1,state.shape.bounds.width);
bounds.height=Math.max(1,state.shape.bounds.height);
var isRotate=state.style[mxConstants.STYLE_HORIZONTAL]==false;
if(graph.isSwimlane(state.cell))
{
var scale=graph.view.scale;
var height=(parseInt(state.style[mxConstants.STYLE_STARTSIZE])||0)*scale;
if(isRotate)
{
bounds.width=height;
}
else
{
bounds.height=height;
}
}
}
return bounds;
};
mxCellRenderer.prototype.redrawOverlays=function(state)
{
var overlays=state.view.graph.getOverlays(state.cell);
var oldCount=(state.overlays!=null)?state.overlays.length:0;
var newCount=(overlays!=null)?overlays.length:0;
if(oldCount!=newCount)
{
if(oldCount>0)
{
for(var i=0;i<state.overlays.length;i++)
{
state.overlays[i].destroy();
}
state.overlays=null;
}
if(newCount>0)
{
this.createOverlay(state);
}
}
if(state.overlays!=null)
{
for(var i=0;i<overlays.length;i++)
{
var bounds=overlays[i].getBounds(state);
if(bounds!=null)
{
state.overlays[i].bounds=bounds;
state.overlays[i].scale=state.view.scale;
state.overlays[i].redraw();
}
}
}
};
mxCellRenderer.prototype.redrawControl=function(state)
{
if(state.control!=null)
{
var isEdge=state.view.graph.getModel().isEdge(state.cell);
var s=state.view.scale;
var bounds=(isEdge)?new mxRectangle(state.x+state.width/2-4*s,state.y+state.height/2-4*s,9*s,9*s)
:new mxRectangle(state.x+4*s,state.y+4*s,9*s,9*s);
state.control.bounds=bounds;
state.control.scale=s;
state.control.redraw();
}
};
mxCellRenderer.prototype.redraw=function(state)
{
var isEdge=state.view.graph.getModel().isEdge(state.cell);
if(state.shape!=null)
{
if(this.checkStyleChanged)
{
var style=state.view.graph.getCellStyle(state.cell);
var styleChanged=false;
if(style.length==state.style.length)
{
for(var key in style)
{
if(style[key]!=state.style[key])
{
styleChanged=true;
break;
}
}
}
else
{
styleChanged=true;
}
if(styleChanged)
{
state.style=style;
state.shape.apply(state);
state.shape.reconfigure();
}
}
var s=state.view.scale;
if(!mxUtils.equals(state,state.shape.bounds)||state.shape.scale!=s||isEdge)
{





if(isEdge)
{
state.shape.points=state.absolutePoints;
state.shape.bounds=new mxRectangle(state.x,state.y,state.width,state.height);
}
else
{
state.shape.bounds=new mxRectangle(state.x,state.y,state.width,state.height);
}
state.shape.scale=s;
state.shape.redraw();
}
this.redrawLabel(state);
this.redrawOverlays(state);
this.redrawControl(state);
}
if(state.doCreateHandler)
{
delete state.doCreateHandler;
state.view.graph.createHandler(state);
}
if(state.view.graph.hasHandler(state))
{
state.view.graph.redrawHandler(state);
}
};
mxCellRenderer.prototype.destroy=function(state)
{
if(state.shape!=null)
{
if(state.text!=null)
{
state.text.destroy();
state.text=null;
}
if(state.overlays!=null)
{
for(var i=0;i<state.overlays.length;i++)
{
state.overlays[i].destroy();
}
state.overlays=null;
}
if(state.control!=null)
{
state.control.destroy();
state.control=null;
}
state.shape.destroy();
state.shape=null;
}
};
}

var mxEdgeStyle=
{
EntityRelation:function(state,source,target,points,result)
{
var graph=state.view.graph;
var segment=mxUtils.getValue(state.style,mxConstants.STYLE_STARTSIZE,mxConstants.ENTITY_SEGMENT)*state.view.scale;
var isSourceLeft=false;
if(source!=null)
{
var sourceGeometry=graph.getCellGeometry(source.cell);
if(sourceGeometry.relative)
{
isSourceLeft=sourceGeometry.x<=0.5;
}
else if(target!=null)
{
isSourceLeft=target.x+target.width<source.x;
}
}
else
{
var tmp=state.absolutePoints[0];
if(tmp==null)
{
return;
}
source=new mxCellState();
source.x=tmp.x;
source.y=tmp.y;
}
var isTargetLeft=true;
if(target!=null)
{
var targetGeometry=graph.getCellGeometry(target.cell);
if(targetGeometry.relative)
{
isTargetLeft=targetGeometry.x<=0.5;
}
else if(source!=null)
{
isTargetLeft=source.x+source.width<target.x;
}
}
else
{
var pts=state.absolutePoints;
var tmp=pts[pts.length-1];
if(tmp==null)
{
return;
}
target=new mxCellState();
target.x=tmp.x;
target.y=tmp.y;
}
var x0=(isSourceLeft)?source.x:source.x+source.width;
var y0=source.getRoutingCenterY();
var xe=(isTargetLeft)?target.x:target.x+target.width;
var ye=target.getRoutingCenterY();
var seg=segment;
var dx=(isSourceLeft)?-seg:seg;
var dep=new mxPoint(x0+dx,y0);
dx=(isTargetLeft)?-seg:seg;
var arr=new mxPoint(xe+dx,ye);
if(isSourceLeft==isTargetLeft)
{
var x=(isSourceLeft)?Math.min(x0,xe)-segment:
Math.max(x0,xe)+segment;
result.push(new mxPoint(x,y0));
result.push(new mxPoint(x,ye));
}
else if((dep.x<arr.x)==isSourceLeft)
{
var midY=y0+(ye-y0)/2;
result.push(dep);
result.push(new mxPoint(dep.x,midY));
result.push(new mxPoint(arr.x,midY));
result.push(arr);
}
else
{
result.push(dep);
result.push(arr);
}
},
Loop:function(state,source,target,points,result)
{
var view=state.view;
var graph=view.graph;
var pt=(points!=null)?points[0]:null;
var s=view.scale;
if(pt!=null)
{
pt=new mxPoint(s*(view.translate.x+pt.x+state.origin.x),s*(view.translate.y+pt.y+state.origin.y));
if(mxUtils.contains(source,pt.x,pt.y))
{
pt=null;
}
}
var x=0;
var dx=0;
var y=source.getRoutingCenterY();
var dy=s*graph.gridSize;
if(pt==null||pt.x<source.x||pt.x>source.x+source.width)
{
if(pt!=null)
{
x=pt.x;
dy=Math.max(Math.abs(y-pt.y),dy);
}
else
{
x=source.x+source.width+2*dy;
}
}
else if(pt!=null)
{
x=source.getRoutingCenterX();
dx=Math.max(Math.abs(x-pt.x),dy);
y=pt.y;
dy=0;
}
result.push(new mxPoint(x-dx,y-dy));
result.push(new mxPoint(x+dx,y+dy));
},
ElbowConnector:function(state,source,target,points,result)
{
var pt=(points!=null)?points[0]:null;
var vertical=false;
var horizontal=false;
if(source!=null&&target!=null)
{
if(pt!=null)
{
var left=Math.min(source.x,target.x);
var right=Math.max(source.x+source.width,target.x+target.width);
var top=Math.min(source.y,target.y);
var bottom=Math.max(source.y+source.height,target.y+target.height);
var view=state.view;
pt=new mxPoint(view.scale*(view.translate.x+pt.x+state.origin.x),view.scale*(view.translate.y+pt.y+state.origin.y));
vertical=pt.y<top||pt.y>bottom;
horizontal=pt.x<left||pt.x>right;
}
else
{
var left=Math.max(source.x,target.x);
var right=Math.min(source.x+source.width,target.x+target.width);
vertical=left==right;
if(!vertical)
{
var top=Math.max(source.y,target.y);
var bottom=Math.min(source.y+source.height,target.y+target.height);
horizontal=top==bottom;
}
}
}
if(!horizontal&&(vertical||state.style[mxConstants.STYLE_ELBOW]==mxConstants.ELBOW_VERTICAL))
{
mxEdgeStyle.TopToBottom(state,source,target,points,result);
}
else
{
mxEdgeStyle.SideToSide(state,source,target,points,result);
}
},
SideToSide:function(state,source,target,points,result)
{
var pt=(points!=null)?points[0]:null;
if(pt!=null)
{
var view=state.view;
pt=new mxPoint(view.scale*(view.translate.x+pt.x+state.origin.x),view.scale*(view.translate.y+pt.y+state.origin.y));
}
if(source==null)
{
var tmp=state.absolutePoints[0];
if(tmp==null)
{
return;
}
source=new mxCellState();
source.x=tmp.x;
source.y=tmp.y;
}
if(target==null)
{
var pts=state.absolutePoints;
var tmp=pts[pts.length-1];
if(tmp==null)
{
return;
}
target=new mxCellState();
target.x=tmp.x;
target.y=tmp.y;
}
var l=Math.max(source.x,target.x);
var r=Math.min(source.x+source.width,target.x+target.width);
var x=(pt!=null)?pt.x:r+(l-r)/2;
var y1=source.getRoutingCenterY();
var y2=target.getRoutingCenterY();
if(pt!=null)
{
if(pt.y>=source.y&&pt.y<=source.y+source.height)
{
y1=pt.y;
}
if(pt.y>=target.y&&pt.y<=target.y+target.height)
{
y2=pt.y;
}
}
if(!mxUtils.contains(target,x,y1)&&!mxUtils.contains(source,x,y1))
{
result.push(new mxPoint(x,y1));
}
if(!mxUtils.contains(target,x,y2)&&!mxUtils.contains(source,x,y2))
{
result.push(new mxPoint(x,y2));
}
if(result.length==1)
{
if(pt!=null)
{
if(!mxUtils.contains(target,x,pt.y)&&!mxUtils.contains(source,x,pt.y))
{
result.push(new mxPoint(x,pt.y));
}
}
else
{
var t=Math.max(source.y,target.y);
var b=Math.min(source.y+source.height,target.y+target.height);
result.push(new mxPoint(x,t+(b-t)/2));
}
}
},
TopToBottom:function(state,source,target,points,result)
{
var pt=(points!=null)?points[0]:null;
if(pt!=null)
{
var view=state.view;
pt=new mxPoint(view.scale*(view.translate.x+pt.x+state.origin.x),view.scale*(view.translate.y+pt.y+state.origin.y));
}
if(source==null)
{
var tmp=state.absolutePoints[0];
if(tmp==null)
{
return;
}
source=new mxCellState();
source.x=tmp.x;
source.y=tmp.y;
}
if(target==null)
{
var pts=state.absolutePoints;
var tmp=pts[pts.length-1];
if(tmp==null)
{
return;
}
target=new mxCellState();
target.x=tmp.x;
target.y=tmp.y;
}
var t=Math.max(source.y,target.y);
var b=Math.min(source.y+source.height,target.y+target.height);
var x=source.getRoutingCenterX();
if(pt!=null&&pt.x>=source.x&&pt.x<=source.x+source.width)
{
x=pt.x;
}
var y=(pt!=null)?pt.y:b+(t-b)/2;
if(!mxUtils.contains(target,x,y)&&!mxUtils.contains(source,x,y))
{
result.push(new mxPoint(x,y));
}
if(pt!=null&&pt.x>=target.x&&pt.x<=target.x+target.width)
{
x=pt.x;
}
else
{
x=target.getRoutingCenterX();
}
if(!mxUtils.contains(target,x,y)&&!mxUtils.contains(source,x,y))
{
result.push(new mxPoint(x,y));
}
if(result.length==1)
{
if(pt!=null&&result.length==1)
{
if(!mxUtils.contains(target,pt.x,y)&&!mxUtils.contains(source,pt.x,y))
{
result.push(new mxPoint(pt.x,y));
}
}
else
{
var l=Math.max(source.x,target.x);
var r=Math.min(source.x+source.width,target.x+target.width);
result.push(new mxPoint(l+(r-l)/2,y));
}
}
}
};

{
function mxGraphView(graph)
{
this.graph=graph;
this.translate=new mxPoint();
this.bounds=new mxRectangle();
this.states=new mxDictionary();
};
mxGraphView.prototype=new mxEventSource();
mxGraphView.prototype.constructor=mxGraphView;
mxGraphView.prototype.doneResource=(mxClient.language!='none')?'done':'';
mxGraphView.prototype.updatingDocumentResource=(mxClient.language!='none')?'updatingDocument':'';
mxGraphView.prototype.graph=null;
mxGraphView.prototype.currentRoot=null;
mxGraphView.prototype.captureDocumentGesture=true;
mxGraphView.prototype.bounds=null;
mxGraphView.prototype.scale=1;
mxGraphView.prototype.translate=null;
mxGraphView.prototype.getBounds=function(cells)
{
var result=null;
if(cells!=null&&cells.length>0)
{
for(var i=0;i<cells.length;i++)
{
var state=this.getState(cells[i]);
if(state!=null)
{
if(result==null)
{
result=new mxRectangle(state.x,state.y,state.width,state.height);
}
else
{
result.add(state);
}
}
}
}
return result;
};
mxGraphView.prototype.setCurrentRoot=function(root)
{
if(this.currentRoot!=root)
{
var change=new mxCurrentRootChange(this,root);
change.execute();
var edit=new mxUndoableEdit(this,false);
edit.add(change);
this.dispatchEvent('undo',this,edit);
this.graph.sizeDidChange();
}
return root;
};
mxGraphView.prototype.scaleAndTranslate=function(scale,dx,dy)
{
var oldScale=this.scale;
var oldDx=this.translate.x;
var oldDy=this.translate.y;
if(this.scale!=scale||this.translate.x!=dx||this.translate.y!=dy)
{
this.scale=scale;
this.translate.x=dx;
this.translate.y=dy;
this.revalidate();
this.graph.sizeDidChange();
}
this.dispatchEvent('scaleAndTranslate',this,oldScale,scale,oldDx,oldDy,dx,dy);
};
mxGraphView.prototype.getScale=function()
{
return this.scale;
};
mxGraphView.prototype.setScale=function(scale)
{
var oldScale=this.scale;
if(this.scale!=scale)
{
this.scale=scale;
this.revalidate();
this.graph.sizeDidChange();
}
this.dispatchEvent('scale',this,oldScale,scale);
};
mxGraphView.prototype.getTranslate=function()
{
return this.translate;
};
mxGraphView.prototype.setTranslate=function(dx,dy)
{
var oldDx=this.translate.x;
var oldDy=this.translate.y;
if(this.translate.x!=dx||this.translate.y!=dy)
{
this.translate.x=dx;
this.translate.y=dy;
this.revalidate();
this.graph.sizeDidChange();
}
this.dispatchEvent('translate',this,oldDx,oldDy,dx,dy);
};
mxGraphView.prototype.refresh=function()
{
if(this.currentRoot!=null)
{
this.clear();
}
this.revalidate();
};
mxGraphView.prototype.revalidate=function()
{
this.invalidate();
this.validate();
};
mxGraphView.prototype.clear=function(cell,force,recurse)
{
var model=this.graph.getModel();
cell=cell||model.getRoot();
force=(force!=null)?force:false;
recurse=(recurse!=null)?recurse:true;
this.removeState(cell);
if(recurse&&(force||cell!=this.currentRoot))
{
var childCount=model.getChildCount(cell);
for(var i=0;i<childCount;i++)
{
this.clear(model.getChildAt(cell,i),force);
}
}
else
{
this.invalidate(cell);
}
};
mxGraphView.prototype.invalidate=function(cell)
{
var model=this.graph.getModel();
cell=cell||model.getRoot();
var state=this.getState(cell);
if(state==null||!state.invalid)
{
if(state!=null)
{
state.invalid=true;
}
var childCount=model.getChildCount(cell);
for(var i=0;i<childCount;i++)
{
var child=model.getChildAt(cell,i);
this.invalidate(child);
}
var edgeCount=model.getEdgeCount(cell);
for(var i=0;i<edgeCount;i++)
{
this.invalidate(model.getEdgeAt(cell,i));
}
}
};
mxGraphView.prototype.validate=function(cell)
{
var t0=mxLog.enter('mxGraphView.validate');
window.status=mxResources.get(this.updatingDocumentResource)||this.updatingDocumentResource;
cell=cell||((this.currentRoot!=null)?this.currentRoot:
this.graph.getModel().getRoot());
this.validateBounds(null,cell);
this.bounds=this.validatePoints(null,cell);
this.validateBackground();
window.status=mxResources.get(this.doneResource)||this.doneResource;
mxLog.leave('mxGraphView.validate',t0);
};
mxGraphView.prototype.validateBackground=function()
{
var bg=this.graph.getBackgroundImage();
if(bg!=null)
{
if(this.backgroundImage==null||this.backgroundImage.image!=bg.src)
{
if(this.backgroundImage!=null)
{
this.backgroundImage.destroy();
}
var bounds=new mxRectangle(0,0,1,1);
this.backgroundImage=new mxImageShape(bounds,bg.src);
this.backgroundImage.dialect=this.graph.dialect;
this.backgroundImage.init(this.backgroundPane);
}
this.redrawBackground(this.backgroundImage,bg);
}
else if(this.backgroundImage!=null)
{
this.backgroundImage.destroy();
this.backgroundImage=null;
}
};
mxGraphView.prototype.redrawBackground=function(backgroundImage,bg)
{
backgroundImage.scale=this.scale;
backgroundImage.bounds.x=this.scale*this.translate.x;
backgroundImage.bounds.y=this.scale*this.translate.y;
backgroundImage.bounds.width=this.scale*bg.width;
backgroundImage.bounds.height=this.scale*bg.height;
backgroundImage.redraw();
};
mxGraphView.prototype.validateBounds=function(parentState,cell)
{
var model=this.graph.getModel();
var state=this.getState(cell,true);
if(state!=null&&state.invalid)
{
if(!this.graph.isCellVisible(cell))
{
this.removeState(cell);
}
else if(cell!=this.currentRoot&&parentState!=null)
{
state.origin.x=parentState.origin.x;
state.origin.y=parentState.origin.y;
var geo=this.graph.getCellGeometry(cell);
if(geo!=null)
{
if(!model.isEdge(cell))
{
var offset=geo.offset||new mxPoint();
if(geo.relative)
{
state.origin.x+=geo.x*parentState.width/this.scale+offset.x;
state.origin.y+=geo.y*parentState.height/this.scale+offset.y;
}
else
{
state.absoluteOffset.x=this.scale*offset.x;
state.absoluteOffset.y=this.scale*offset.y;
state.origin.x+=geo.x;
state.origin.y+=geo.y;
}
}
state.x=this.scale*(this.translate.x+state.origin.x);
state.y=this.scale*(this.translate.y+state.origin.y);
state.width=this.scale*geo.width;
state.height=this.scale*geo.height;
if(model.isVertex(cell))
{
this.updateVertexLabelOffset(state);
}
}
}
var offset=this.graph.getChildOffsetForCell(cell);
if(offset!=null)
{
state.origin.x+=offset.x;
state.origin.y+=offset.y;
}
}
if(state!=null&&(!this.graph.isCellCollapsed(cell)||cell==this.currentRoot))
{
var childCount=model.getChildCount(cell);
for(var i=0;i<childCount;i++)
{
var child=model.getChildAt(cell,i);
this.validateBounds(state,child);
}
}
};
mxGraphView.prototype.updateVertexLabelOffset=function(state)
{
var horizontal=mxUtils.getValue(state.style,mxConstants.STYLE_LABEL_POSITION,mxConstants.ALIGN_CENTER);
if(horizontal==mxConstants.ALIGN_LEFT)
{
state.absoluteOffset.x-=state.width;
}
else if(horizontal==mxConstants.ALIGN_RIGHT)
{
state.absoluteOffset.x+=state.width;
}
var vertical=mxUtils.getValue(state.style,mxConstants.STYLE_VERTICAL_LABEL_POSITION,mxConstants.ALIGN_MIDDLE);
if(vertical==mxConstants.ALIGN_TOP)
{
state.absoluteOffset.y-=state.height;
}
else if(vertical==mxConstants.ALIGN_BOTTOM)
{
state.absoluteOffset.y+=state.height;
}
};
mxGraphView.prototype.validatePoints=function(parentState,cell)
{
var minX=null;
var minY=null;
var maxX=0;
var maxY=0;
var model=this.graph.getModel();
var state=this.getState(cell);
if(state!=null)
{
if(state.invalid)
{
var geo=this.graph.getCellGeometry(cell);
if(model.isEdge(cell))
{
var source=this.getVisibleTerminal(cell,true);
if(source!=null&&!model.isAncestor(source,cell))
{
var p=model.getParent(source);
var pstate=this.getState(p);
this.validatePoints(pstate,source);
}
var target=this.getVisibleTerminal(cell,false);
if(target!=null&&!model.isAncestor(target,cell))
{
var p=model.getParent(target);
var pstate=this.getState(p);
this.validatePoints(pstate,target);
}
this.setTerminalPoints(state);
this.updatePoints(state,geo.points,source,target);
this.updateTerminalPoints(state,source,target);
this.updateEdgeBounds(state);
this.updateEdgeLabelOffset(state);
}
else if(geo!=null&&geo.relative&&parentState!=null&&model.isEdge(parentState.cell))
{
var origin=this.getPoint(parentState,geo);
if(origin!=null)
{
state.x=origin.x;
state.y=origin.y;
}
}
state.invalid=false;
if(cell!=this.currentRoot)
{
this.graph.cellRenderer.redraw(state);
}
}
if(model.isEdge(cell)||model.isVertex(cell))
{
var box=(state.text!=null&&(!this.graph.isHtmlLabel(cell)||!this.graph.isClipping(cell)))?state.text.boundingBox:null;
if(box!=null)
{
minX=Math.min(state.x,box.x);
minY=Math.min(state.y,box.y);
maxX=Math.max(state.x+state.width,box.x+box.width);
maxY=Math.max(state.y+state.height,box.y+box.height);
}
else
{
minX=state.x;
minY=state.y;
maxX=state.x+state.width;
maxY=state.y+state.height;
}
}
}
if(state!=null&&(!this.graph.isCellCollapsed(cell)||cell==this.currentRoot))
{
var childCount=model.getChildCount(cell);
for(var i=0;i<childCount;i++)
{
var child=model.getChildAt(cell,i);
var bounds=this.validatePoints(state,child);
minX=(minX!=null)?Math.min(minX,bounds.x):bounds.x;
minY=(minY!=null)?Math.min(minY,bounds.y):bounds.y;
maxX=Math.max(maxX,bounds.x+bounds.width);
maxY=Math.max(maxY,bounds.y+bounds.height);
}
}
return new mxRectangle(minX,minY,maxX-minX,maxY-minY);
};
mxGraphView.prototype.setTerminalPoints=function(state)
{
var tr=this.translate;
var s=this.scale;
var edge=state.cell;
var orig=state.origin;
var geo=this.graph.getCellGeometry(edge);
var pt=geo.getTerminalPoint(true);
if(pt!=null)
{
pt=new mxPoint(s*(tr.x+pt.x+orig.x),s*(tr.y+pt.y+orig.y));
state.setAbsoluteTerminalPoint(pt,true);
}
else
{
state.setAbsoluteTerminalPoint(null,true);
}
pt=geo.getTerminalPoint(false);
if(pt!=null)
{
pt=new mxPoint(s*(tr.x+pt.x+orig.x),s*(tr.y+pt.y+orig.y));
state.setAbsoluteTerminalPoint(pt,false);
}
else
{
state.setAbsoluteTerminalPoint(null,false);
}
};
mxGraphView.prototype.updatePoints=function(state,points,source,target)
{
if(state!=null)
{
var pts=new Array();
pts.push(state.absolutePoints[0]);
var edgeStyle=this.getEdgeStyle(state,source,target);
if(edgeStyle!=null)
{
var src=this.getState(source);
var trg=this.getState(target);
edgeStyle(state,src,trg,points,pts);
}
else if(points!=null)
{
for(var i=0;i<points.length;i++)
{
var pt=mxUtils.clone(points[i]);
pt.x+=this.translate.x+state.origin.x;
pt.y+=this.translate.y+state.origin.y;
pt.x*=this.scale;
pt.y*=this.scale;
pts.push(pt);
}
}
var tmp=state.absolutePoints;
pts.push(tmp[tmp.length-1]);
state.absolutePoints=pts;
}
};
mxGraphView.prototype.getEdgeStyle=function(edgeState,source,target)
{
return(source!=null&&source==target)?mxUtils.getValue(edgeState.style,mxConstants.STYLE_LOOP,this.graph.defaultLoopStyle):(!mxUtils.getValue(edgeState.style,mxConstants.STYLE_NOEDGESTYLE,false)?edgeState.style[mxConstants.STYLE_EDGE]:
null);
};
mxGraphView.prototype.updateTerminalPoints=function(state,source,target)
{
if(target!=null)
{
this.updateTerminalPoint(state,target,source,false);
}
if(source!=null)
{
this.updateTerminalPoint(state,source,target,true);
}
};
mxGraphView.prototype.updateTerminalPoint=function(state,start,end,isSource)
{
var pt=this.getPerimeterPoint(state,start,end,isSource);
state.setAbsoluteTerminalPoint(pt,isSource);
};
mxGraphView.prototype.getPerimeterPoint=function(state,start,end,isSource)
{
var point=null;
var term=this.getState(start);
if(term!=null)
{
var perimeter=this.getPerimeterFunction(term);
var next=this.getNextPoint(state,end,isSource);
if(perimeter!=null&&next!=null)
{
var bounds=this.getPerimeterBounds(term,state,isSource);
point=perimeter(bounds,state,term,isSource,next);
}
if(point==null)
{
point=this.getPoint(term);
}
}
return point;
};
mxGraphView.prototype.getPerimeterBounds=function(terminal,edge,isSource)
{
var border=0;
if(edge!=null)
{
border=parseFloat(edge.style[mxConstants.STYLE_PERIMETER_SPACING]||0)
border+=parseFloat(edge.style[(isSource)?mxConstants.STYLE_SOURCE_PERIMETER_SPACING
:mxConstants.STYLE_TARGET_PERIMETER_SPACING]||0);
}
if(terminal!=null)
{
border+=parseFloat(terminal.style[mxConstants.STYLE_PERIMETER_SPACING]||0);
}
return terminal.getPerimeterBounds(border*this.scale);
};
mxGraphView.prototype.getPerimeterFunction=function(state)
{
return state.style[mxConstants.STYLE_PERIMETER];
};
mxGraphView.prototype.getNextPoint=function(state,opposite,isSource)
{
var point=null;
var pts=state.absolutePoints;
if(pts!=null&&(isSource||pts.length>2||opposite==null))
{
var count=pts.length;
point=pts[(isSource)?Math.min(1,count-1):Math.max(0,count-2)];
}
if(point==null&&opposite!=null)
{
var oppositeState=this.getState(opposite);
if(oppositeState!=null)
{
point=new mxPoint(oppositeState.getCenterX(),oppositeState.getCenterY());
}
}
return point;
};
mxGraphView.prototype.getVisibleTerminal=function(edge,isSource)
{
var model=this.graph.getModel();
var result=model.getTerminal(edge,isSource);
var best=result;
while(result!=null&&result!=this.currentRoot)
{
if(!this.graph.isCellVisible(best)||this.graph.isCellCollapsed(result))
{
best=result;
}
result=model.getParent(result);
}
return best;
};
mxGraphView.prototype.updateEdgeBounds=function(state)
{
var points=state.absolutePoints;
state.length=0;
if(points!=null&&points.length>0)
{
var p0=points[0];
var pe=points[points.length-1];
if(p0==null||pe==null)
{





this.clear(state.cell,true);
}
else
{
if(p0.x!=pe.x||p0.y!=pe.y)
{
var dx=pe.x-p0.x;
var dy=pe.y-p0.y;
state.terminalDistance=Math.sqrt(dx*dx+dy*dy);
}
else
{
state.terminalDistance=0;
}
var length=0;
var segments=new Array();
var pt=p0;
if(pt!=null)
{
var minX=pt.x;
var minY=pt.y;
var maxX=minX;
var maxY=minY;
for(var i=1;i<points.length;i++)
{
var tmp=points[i];
if(tmp!=null)
{
var dx=pt.x-tmp.x;
var dy=pt.y-tmp.y;
var segment=Math.sqrt(dx*dx+dy*dy);
segments.push(segment);
length+=segment;
pt=tmp;
minX=Math.min(pt.x,minX);
minY=Math.min(pt.y,minY);
maxX=Math.max(pt.x,maxX);
maxY=Math.max(pt.y,maxY);
}
}
state.length=length;
state.segments=segments;
var markerSize=1;
state.x=minX;
state.y=minY;
state.width=Math.max(markerSize,maxX-minX);
state.height=Math.max(markerSize,maxY-minY);
}
}
}
};
mxGraphView.prototype.getPoint=function(state,geometry)
{
var x=state.getCenterX();
var y=state.getCenterY();
if(state.segments!=null&&(geometry==null||geometry.relative))
{
var gx=(geometry!=null)?geometry.x/2:0;
var pointCount=state.absolutePoints.length;
var dist=(gx+0.5)*state.length;
var segment=state.segments[0];
var length=0;
var index=1;
while(dist>length+segment&&index<pointCount-1)
{
length+=segment;
segment=state.segments[index++];
}
if(segment!=0)
{
var factor=(dist-length)/segment;
var p0=state.absolutePoints[index-1];
var pe=state.absolutePoints[index];
if(p0!=null&&pe!=null)
{
var gy=0;
var offsetX=0;
var offsetY=0;
if(geometry!=null)
{
gy=geometry.y;
var offset=geometry.offset;
if(offset!=null)
{
offsetX=offset.x;
offsetY=offset.y;
}
}
var dx=pe.x-p0.x;
var dy=pe.y-p0.y;
var nx=dy/segment;
var ny=dx/segment;
x=p0.x+dx*factor+(nx*gy+offsetX)*this.scale;
y=p0.y+dy*factor-(ny*gy-offsetY)*this.scale;
}
}
}
else if(geometry!=null)
{
var offset=geometry.offset;
if(offset!=null)
{
x+=offset.x;
y+=offset.y;
}
}
return new mxPoint(x,y);
};
mxGraphView.prototype.getRelativePoint=function(edgeState,x,y)
{
var model=this.graph.getModel();
var geometry=model.getGeometry(edgeState.cell);
var scale=this.graph.getView().scale;
if(geometry!=null)
{
var pointCount=edgeState.absolutePoints.length;
if(geometry.relative&&pointCount>1)
{
var totalLength=edgeState.length;
var segments=edgeState.segments;
var p0=edgeState.absolutePoints[0];
var pe=edgeState.absolutePoints[1];
var minDist=mxUtils.ptSegDistSq(p0.x,p0.y,pe.x,pe.y,x,y);
var index=0;
var tmp=0;
var length=0;
for(var i=2;i<pointCount;i++)
{
tmp+=segments[i-2];
pe=edgeState.absolutePoints[i];
var dist=mxUtils.ptSegDistSq(p0.x,p0.y,pe.x,pe.y,x,y);
if(dist<=minDist)
{
minDist=dist;
index=i-1;
length=tmp;
}
p0=pe;
}
var seg=segments[index];
p0=edgeState.absolutePoints[index];
pe=edgeState.absolutePoints[index+1];
var x2=p0.x;
var y2=p0.y;
var x1=pe.x;
var y1=pe.y;
var px=x;
var py=y;
var xSegment=x2-x1;
var ySegment=y2-y1;
px-=x1;
py-=y1;
var projlenSq=0;
px=xSegment-px;
py=ySegment-py;
var dotprod=px*xSegment+py*ySegment;
if(dotprod<=0.0)
{
projlenSq=0;
}
else
{
projlenSq=dotprod*dotprod/(xSegment*xSegment+ySegment*ySegment);
}
var projlen=Math.sqrt(projlenSq);
if(projlen>seg)
{
projlen=seg;
}
var yDistance=Math.sqrt(mxUtils.ptSegDistSq(p0.x,p0.y,pe.x,pe.y,x,y));
var direction=mxUtils.relativeCcw(p0.x,p0.y,pe.x,pe.y,x,y);
if(direction==-1)
{
yDistance=-yDistance;
}
return new mxPoint(((totalLength/2-length-projlen)/totalLength)*-2,yDistance/scale);
}
}
return new mxPoint();
};
mxGraphView.prototype.updateEdgeLabelOffset=function(state)
{
var points=state.absolutePoints;
state.absoluteOffset.x=state.getCenterX();
state.absoluteOffset.y=state.getCenterY();
if(points!=null&&points.length>0&&state.segments!=null)
{
var geometry=this.graph.getCellGeometry(state.cell);
if(geometry.relative)
{
var offset=this.getPoint(state,geometry);
if(offset!=null)
{
state.absoluteOffset=offset;
}
}
else
{
var p0=points[0];
var pe=points[points.length-1];
if(p0!=null&&pe!=null)
{
var dx=pe.x-p0.x;
var dy=pe.y-p0.y;
var x0=0;
var y0=0;
var off=geometry.offset;
if(off!=null)
{
x0=off.x;
y0=off.y;
}
var x=p0.x+dx/2+x0*this.scale;
var y=p0.y+dy/2+y0*this.scale;
state.absoluteOffset.x=x;
state.absoluteOffset.y=y;
}
}
}
};
mxGraphView.prototype.getState=function(cell,create)
{
create=create||false;
var state=null;
if(cell!=null)
{
state=this.states.get(cell);
if(state==null&&create&&this.graph.isCellVisible(cell))
{
state=this.createState(cell);
this.states.put(cell,state);
}
}
return state;
};
mxGraphView.prototype.getStates=function(cells)
{
var result=new Array();
for(var i=0;i<cells.length;i++)
{
var state=this.getState(cells[i]);
if(state!=null)
{
result.push(state);
}
}
return result;
};
mxGraphView.prototype.removeState=function(cell)
{
var state=null;
if(cell!=null)
{
state=this.states.remove(cell);
if(state!=null)
{
this.graph.cellRenderer.destroy(state);
state.destroy();
}
}
return state;
};
mxGraphView.prototype.createState=function(cell)
{
var style=this.graph.getCellStyle(cell);
var state=new mxCellState(this,cell,style);
this.graph.cellRenderer.initialize(state);
return state;
};
mxGraphView.prototype.getCanvas=function()
{
return this.canvas;
};
mxGraphView.prototype.getBackgroundPane=function()
{
return this.backgroundPane;
};
mxGraphView.prototype.getDrawPane=function()
{
return this.drawPane;
};
mxGraphView.prototype.getOverlayPane=function()
{
return this.overlayPane;
};
mxGraphView.prototype.isContainerEvent=function(evt)
{
var source=mxEvent.getSource(evt);
var bgNode=(this.backgroundImage!=null)?this.backgroundImage.node:null;
return(source==this.graph.container||source.parentNode==bgNode||source==this.canvas.parentNode||source==this.canvas||source==this.backgroundPane||source==this.drawPane||source==this.overlayPane);
};
mxGraphView.prototype.isScrollEvent=function(evt)
{
var offset=mxUtils.getOffset(this.graph.container);
var pt=new mxPoint(evt.clientX-offset.x,evt.clientY-offset.y);
var outWidth=this.graph.container.offsetWidth;
var inWidth=this.graph.container.clientWidth;
if(outWidth>inWidth&&pt.x>inWidth+2&&pt.x<=outWidth)
{
return true;
}
var outHeight=this.graph.container.offsetHeight;
var inHeight=this.graph.container.clientHeight;
if(outHeight>inHeight&&pt.y>inHeight+2&&pt.y<=outHeight)
{
return true;
}
return false;
};
mxGraphView.prototype.init=function()
{
var graph=this.graph;
var container=graph.container;
if(container!=null)
{
var self=this;
mxEvent.addListener(container,'mousedown',function(evt)
{
if(self.isContainerEvent(evt)&&((!mxClient.IS_IE&&!mxClient.IS_GC&&!mxClient.IS_SF)||!self.isScrollEvent(evt)))
{
graph.dispatchGraphEvent('mousedown',evt);
}
});
mxEvent.addListener(container,'mousemove',function(evt)
{
if(self.isContainerEvent(evt))
{
graph.dispatchGraphEvent('mousemove',evt);
}
});
mxEvent.addListener(container,'mouseup',function(evt)
{
if(self.isContainerEvent(evt))
{
graph.dispatchGraphEvent('mouseup',evt);
}
});
mxEvent.addListener(container,'dblclick',function(evt)
{
graph.dblClick(evt);
mxEvent.consume(evt);
});

mxEvent.addListener(document,'mousedown',function(evt)
{
if(self.isContainerEvent(evt))
{
graph.panningHandler.hideMenu();
}
});
mxEvent.addListener(document,'mousemove',function(evt)
{
graph.tooltipHandler.hide();
if(self.captureDocumentGesture&&graph.gestureHandler!=null)
{
graph.dispatchGraphEvent('mousemove',evt);
}
});
mxEvent.addListener(document,'mouseup',function(evt)
{
if(self.captureDocumentGesture&&graph.gestureHandler!=null)
{
graph.dispatchGraphEvent('mouseup',evt);
}
});
}
if(graph.dialect==mxConstants.DIALECT_SVG)
{
this.createSvg();
}
else if(graph.dialect==mxConstants.DIALECT_VML)
{
this.createVml();
}
else
{
this.createHtml();
}
};
mxGraphView.prototype.createHtml=function()
{
var container=this.graph.container;
if(container!=null)
{
this.canvas=this.createHtmlPane();



this.backgroundPane=this.createHtmlPane(1,1);
this.drawPane=this.createHtmlPane(1,1);
this.overlayPane=this.createHtmlPane(1,1);
this.canvas.appendChild(this.backgroundPane);
this.canvas.appendChild(this.drawPane);
this.canvas.appendChild(this.overlayPane);
container.appendChild(this.canvas);
}
};
mxGraphView.prototype.createHtmlPane=function(width,height)
{
var pane=document.createElement('DIV');
if(width!=null&&height!=null)
{
pane.style.position='absolute';
pane.style.left='0px';
pane.style.top='0px';
pane.style.width=width+'px';
pane.style.height=height+'px';
}
else
{
pane.style.position='relative';
}
return pane;
};
mxGraphView.prototype.createVml=function()
{
var container=this.graph.container;
if(container!=null)
{
var width=container.offsetWidth;
var height=container.offsetHeight;
this.canvas=this.createVmlPane(width,height);
this.backgroundPane=this.createVmlPane(width,height);
this.drawPane=this.createVmlPane(width,height);
this.overlayPane=this.createVmlPane(width,height);
this.canvas.appendChild(this.backgroundPane);
this.canvas.appendChild(this.drawPane);
this.canvas.appendChild(this.overlayPane);
container.appendChild(this.canvas);
}
};
mxGraphView.prototype.createVmlPane=function(width,height)
{
var pane=document.createElement('v:group');

pane.style.position='absolute';
pane.style.left='0px';
pane.style.top='0px';
pane.style.width=width+'px';
pane.style.height=height+'px';
pane.setAttribute('coordsize',width+','+height);
pane.setAttribute('coordorigin','0,0');
return pane;
};
mxGraphView.prototype.createSvg=function()
{
var container=this.graph.container;
this.canvas=document.createElementNS(mxConstants.NS_SVG,'g');
this.backgroundPane=document.createElementNS(mxConstants.NS_SVG,'g');
this.canvas.appendChild(this.backgroundPane);
this.drawPane=document.createElementNS(mxConstants.NS_SVG,'g');
this.canvas.appendChild(this.drawPane);
this.overlayPane=document.createElementNS(mxConstants.NS_SVG,'g');
this.canvas.appendChild(this.overlayPane);
var root=document.createElementNS(mxConstants.NS_SVG,'svg');

var self=this;
var onResize=function(evt)
{
if(self.graph.container!=null)
{
var width=self.graph.container.offsetWidth;
var height=self.graph.container.offsetHeight;
root.setAttribute('width',Math.max(width,self.bounds.width));
root.setAttribute('height',Math.max(height,self.bounds.height));
}
};
mxEvent.addListener(window,'resize',onResize);
if(mxClient.IS_OP)
{
onResize();
}
root.appendChild(this.canvas);
if(container!=null)
{
container.appendChild(root);
var style=mxUtils.getCurrentStyle(container);
if(style.position=='static')
{
container.style.position='relative';
}
}
};
mxGraphView.prototype.destroy=function()
{
var root=this.canvas.ownerSVGElement||this.canvas;
if(root.parentNode!=null)
{
this.clear(this.currentRoot,true);
mxEvent.removeAllListeners(document);
mxUtils.release(this.graph.container);
root.parentNode.removeChild(root);
this.canvas=null;
this.backgroundPane=null;
this.drawPane=null;
this.overlayPane=null;
}
};
function mxCurrentRootChange(view,root)
{
this.view=view;
this.root=root;
this.previous=root;
this.isUp=root==null;
if(!this.isUp)
{
var tmp=this.view.currentRoot;
var model=this.view.graph.getModel();
while(tmp!=null)
{
if(tmp==root)
{
this.isUp=true;
break;
}
tmp=model.getParent(tmp);
}
}
};
mxCurrentRootChange.prototype.execute=function()
{
var tmp=this.view.currentRoot;
this.view.currentRoot=this.previous;
this.previous=tmp;
var translate=this.view.graph.getTranslateForRoot(this.view.currentRoot);
if(translate!=null)
{
this.view.translate=new mxPoint(-translate.x,-translate.y);
}
var name=(this.isUp)?'up':'down';
this.view.dispatchEvent(name,this.view,this.previous,this.view.currentRoot);
if(this.isUp)
{
this.view.clear(this.view.currentRoot,true);
this.view.validate();
}
else
{
this.view.refresh();
}
this.isUp=!this.isUp;
};
}

{
function mxGraph(container,model,renderHint)
{
this.renderHint=renderHint;
if(mxClient.IS_SVG)
{
this.dialect=mxConstants.DIALECT_SVG;
}
else if(renderHint=='exact'&&mxClient.IS_VML)
{
this.dialect=mxConstants.DIALECT_VML;
}
else if(renderHint=='fastest')
{
this.dialect=mxConstants.DIALECT_STRICTHTML;
}
else if(renderHint=='faster')
{
this.dialect=mxConstants.DIALECT_PREFERHTML;
}
else 
{
this.dialect=mxConstants.DIALECT_MIXEDHTML;
}
this.model=(model!=null)?model:new mxGraphModel();
this.stylesheet=new mxStylesheet();
this.cellRenderer=new mxCellRenderer();
this.multiplicities=new Array();
this.view=new mxGraphView(this);
this.selection=new mxGraphSelection(this);
var self=this;
this.model.addListener('change',function(sender,changes)
{
self.graphModelChanged(changes);
});
this.tooltipHandler=new mxTooltipHandler(this);
this.tooltipHandler.setEnabled(false);
this.panningHandler=new mxPanningHandler(this);
this.panningHandler.panningEnabled=false;
this.connectionHandler=new mxConnectionHandler(this);
this.connectionHandler.setEnabled(false);
this.graphHandler=new mxGraphHandler(this);
if(container!=null)
{
this.init(container);
}
this.view.revalidate();
};
mxResources.add(mxClient.basePath+'js/resources/graph');
mxGraph.prototype=new mxEventSource();
mxGraph.prototype.constructor=mxGraph;
mxGraph.prototype.EMPTY_ARRAY=new Array();

mxGraph.prototype.graphListeners=null;
mxGraph.prototype.model=null;
mxGraph.prototype.view=null;
mxGraph.prototype.stylesheet=null;
mxGraph.prototype.selection=null;
mxGraph.prototype.editor=null;
mxGraph.prototype.cellRenderer=null;
mxGraph.prototype.multiplicities=null;
mxGraph.prototype.renderHint=null;
mxGraph.prototype.dialect=null;
mxGraph.prototype.gridSize=10;
mxGraph.prototype.gridEnabled=true;
mxGraph.prototype.tolerance=4;
mxGraph.prototype.defaultOverlap=0.5;
mxGraph.prototype.defaultParent=null;
mxGraph.prototype.alternateEdgeStyle=null;
mxGraph.prototype.backgroundImage=null;
mxGraph.prototype.enabled=true;
mxGraph.prototype.escapeEnabled=true;
mxGraph.prototype.locked=false;
mxGraph.prototype.cloneable=true;
mxGraph.prototype.exportEnabled=true;
mxGraph.prototype.importEnabled=true;
mxGraph.prototype.foldingEnabled=true;
mxGraph.prototype.editable=true;
mxGraph.prototype.deletable=true;
mxGraph.prototype.movable=true;
mxGraph.prototype.edgeLabelsMovable=true;
mxGraph.prototype.vertexLabelsMovable=false;
mxGraph.prototype.dropEnabled=false;
mxGraph.prototype.sizable=true;
mxGraph.prototype.bendable=true;
mxGraph.prototype.disconnectable=true;
mxGraph.prototype.selectable=true;
mxGraph.prototype.autoSize=false;
mxGraph.prototype.autoLayout=true;
mxGraph.prototype.bubbleLayout=true;
mxGraph.prototype.maximumGraphBounds=null;
mxGraph.prototype.minimumContainerSize=null;
mxGraph.prototype.maximumContainerSize=null;
mxGraph.prototype.border=0;
mxGraph.prototype.ordered=true;
mxGraph.prototype.keepEdgesInForeground=false;
mxGraph.prototype.keepEdgesInBackground=true;
mxGraph.prototype.keepInsideParentOnMove=true;
mxGraph.prototype.extendParentOnResize=true;
mxGraph.prototype.shiftDownwards=false;
mxGraph.prototype.shiftRightwards=false;
mxGraph.prototype.collapseToPreferredSize=true;
mxGraph.prototype.zoomFactor=1.2;
mxGraph.prototype.keepSelectionVisibleOnZoom=false;
mxGraph.prototype.centerZoom=true;
mxGraph.prototype.resetViewOnRootChange=true;
mxGraph.prototype.resetEdgesOnResize=false;
mxGraph.prototype.resetEdgesOnMove=false;
mxGraph.prototype.allowLoops=false;
mxGraph.prototype.defaultLoopStyle=mxEdgeStyle.Loop;
mxGraph.prototype.multigraph=true;
mxGraph.prototype.connectableEdges=false;
mxGraph.prototype.allowDanglingEdges=true;
mxGraph.prototype.cloneInvalidEdges=false;
mxGraph.prototype.disconnectOnMove=true;
mxGraph.prototype.labelsVisible=true;
mxGraph.prototype.htmlLabels=mxClient.IS_MAC&&!mxClient.IS_FF3;
mxGraph.prototype.swimlaneSelectionEnabled=true;
mxGraph.prototype.swimlaneNesting=true;
mxGraph.prototype.swimlaneIndicatorColorAttribute=mxConstants.STYLE_FILLCOLOR;
mxGraph.prototype.collapsedImage=new mxImage(mxClient.imageBasePath+'collapsed.gif',9,9);
mxGraph.prototype.expandedImage=new mxImage(mxClient.imageBasePath+'expanded.gif',9,9);
mxGraph.prototype.warningImage=new mxImage(mxClient.imageBasePath+'warning'+((mxClient.IS_MAC)?'.png':'.gif'),16,16);
mxGraph.prototype.alreadyConnectedResource=(mxClient.language!='none')?'alreadyConnected':'';
mxGraph.prototype.containsValidationErrorsResource=(mxClient.language!='none')?'containsValidationErrors':'';
mxGraph.prototype.resizeContainer=false;
mxGraph.prototype.init=function(container)
{
this.container=container;
this.editor=new mxCellEditor(this);
this.view.init();
this.tooltipHandler.init();
this.panningHandler.init();
this.connectionHandler.init();
this.sizeDidChange();

if(mxClient.IS_IE)
{
var self=this;
mxEvent.addListener(window,'unload',function()
{
self.destroy();
});
}
else
{

var self=this;
this.focusHandler=function(evt)
{
self.activeElement=mxEvent.getSource(evt);
};
this.blurHandler=function(evt)
{
self.activeElement=null;
}
mxEvent.addListener(document.body,'focus',this.focusHandler);
mxEvent.addListener(document.body,'blur',this.blurHandler);
}
};
mxGraph.prototype.getModel=function()
{
return this.model;
};
mxGraph.prototype.getView=function()
{
return this.view;
};
mxGraph.prototype.getStylesheet=function()
{
return this.stylesheet;
};
mxGraph.prototype.setStylesheet=function(stylesheet)
{
this.stylesheet=stylesheet;
};

mxGraph.prototype.addOverlay=function(cell,overlay)
{
if(cell.overlays==null)
{
cell.overlays=new Array();
}
cell.overlays.push(overlay);
var state=this.view.getState(cell);
if(state!=null)
{
this.cellRenderer.redraw(state);
}
this.dispatchEvent('addoverlay',this,cell,overlay);
return overlay;
};
mxGraph.prototype.getOverlays=function(cell)
{
return cell.overlays;
};
mxGraph.prototype.removeOverlay=function(cell,overlay)
{
if(overlay==null)
{
this.removeOverlays(cell);
}
else
{
var index=mxUtils.indexOf(cell.overlays,overlay);
if(index>=0)
{
cell.overlays.splice(index,1);
if(cell.overlays.length==0)
{
cell.overlays=null;
}
var state=this.view.getState(cell);
if(state!=null)
{
this.cellRenderer.redraw(state);
}
this.dispatchEvent('removeoverlay',this,cell,overlay);
}
else
{
overlay=null;
}
}
return overlay;
};
mxGraph.prototype.removeOverlays=function(cell)
{
var overlays=cell.overlays;
if(overlays!=null)
{
cell.overlays=null;
var state=this.view.getState(cell);
if(state!=null)
{
this.cellRenderer.redraw(state);
}
for(var i=0;i<overlays.length;i++)
{
this.dispatchEvent('removeoverlay',this,cell,overlays[i]);
}
}
return overlays;
};
mxGraph.prototype.clearOverlays=function(cell)
{
cell=(cell!=null)?cell:this.model.getRoot();
this.removeOverlays(cell);
var childCount=this.model.getChildCount(cell);
for(var i=0;i<childCount;i++)
{
var child=this.model.getChildAt(cell,i);
this.clearOverlays(child);
}
};
mxGraph.prototype.setWarning=function(cell,warning,img,isSelect)
{
if(warning!=null&&warning.length>0)
{
img=(img!=null)?img:this.warningImage;
var overlay=new mxOverlay(img,'<font color=red>'+warning+'</font>');
if(isSelect)
{
var self=this;
overlay.addListener('click',function(sender,evt)
{
self.setSelectionCell(cell);
});
}
return this.addOverlay(cell,overlay);
}
else
{
this.removeOverlays(cell);
}
return null;
};

mxGraph.prototype.edit=function(cell,trigger)
{
cell=cell||this.getSelectionCell();
if(cell!=null&&this.isEditable(cell))
{
this.startEditingAtCell(cell,trigger);
}
};
mxGraph.prototype.startEditingAtCell=function(cell,trigger)
{
this.dispatchEvent('startEditing',this,cell,trigger);
this.editor.startEditing(cell,trigger);
};
mxGraph.prototype.getEditingValue=function(cell,trigger)
{
return this.convertValueToString(cell);
};
mxGraph.prototype.labelChanged=function(cell,newValue,trigger)
{
var oldValue=this.model.getValue(cell);
this.dispatchEvent('beforeLabelChanged',this,cell,oldValue,newValue,trigger);
this.model.beginUpdate();
try
{
this.model.setValue(cell,newValue);
if(this.isUpdateSize(cell))
{
this.updateSize(cell);
}
this.dispatchEvent('labelChanged',this,cell,oldValue,newValue,trigger);
}
finally
{
this.model.endUpdate();
}
this.dispatchEvent('afterLabelChanged',this,cell,oldValue,newValue,trigger);
};

mxGraph.prototype.escape=function(evt)
{
if(this.escapeEnabled)
{
this.editor.stopEditing(true);
this.connectionHandler.reset();
this.graphHandler.reset();
var cells=this.getSelectionCells();
for(var i=0;i<cells.length;i++)
{
var state=this.view.getState(cells[i]);
if(state!=null&&state.handler!=null)
{
state.handler.reset();
}
}
}
};
mxGraph.prototype.click=function(evt,cell)
{
this.dispatchEvent('click',this,evt,cell);
if(this.isEnabled()&&!mxEvent.isConsumed(evt))
{
if(cell!=null)
{
this.selectCellForEvent(cell,evt);
}
else
{
var swimlane=null;
if(this.swimlaneSelectionEnabled)
{

var pt=mxUtils.convertPoint(this.container,evt.clientX,evt.clientY);

swimlane=this.getSwimlaneAt(pt.x,pt.y);
}
if(swimlane!=null)
{
this.selectCellForEvent(swimlane,evt);
}
else if(!mxEvent.isToggleEvent(evt))
{
this.clearSelection();
}
}
}
};
mxGraph.prototype.dblClick=function(evt,cell)
{
this.dispatchEvent('dblclick',this,evt,cell);
if(this.isEnabled()&&!mxEvent.isConsumed(evt)&&cell!=null)
{
this.edit(cell,evt);
}
};
mxGraph.prototype.graphModelChanged=function(changes)
{
for(var i=0;i<changes.length;i++)
{
this.processChange(changes[i]);
}
this.view.validate();
this.sizeDidChange();
};
mxGraph.prototype.processChange=function(change)
{

if(change.constructor==mxRootChange)
{
if(this.resetViewOnRootChange)
{
this.view.scale=1;
this.view.translate.x=0;
this.view.translate.y=0;
}
this.cellRemoved(change.previous);
this.clearSelection();
this.dispatchEvent('root',this);
}


else if(change.constructor==mxChildChange)
{
if(change.isAdded)
{
this.view.clear(change.child);
}
else
{
this.cellRemoved(change.child,true);
}
var newParent=this.model.getParent(change.child);
if(newParent!=change.previous)
{
if(newParent!=null)
{
this.view.clear(newParent,null,false);
}
if(change.previous!=null)
{
this.view.clear(change.previous,null,false);
}
}
}

else if(change.constructor==mxTerminalChange||change.constructor==mxGeometryChange)
{
this.view.invalidate(change.cell);
}

else if(change.constructor==mxValueChange||change.constructor==mxStyleChange)
{
this.view.clear(change.cell,null,false);
}
else if(change.cell!=null&&change.cell.constructor==mxCell)
{
this.cellRemoved(change.cell,change.constructor==mxVisibleChange);
}
};
mxGraph.prototype.cellRemoved=function(cell,clearSelection)
{
if(cell!=null)
{
if(clearSelection&&this.isCellSelected(cell))
{
this.selection.removeCell(cell);
}
var childCount=this.model.getChildCount(cell);
for(var i=0;i<childCount;i++)
{
this.cellRemoved(this.model.getChildAt(cell,i),clearSelection);
}
this.view.removeState(cell);
}
};
mxGraph.prototype.sizeDidChange=function()
{
var bounds=this.getBounds();
if(this.container!=null)
{
var border=this.getBorder();
var width=bounds.x+bounds.width+1+border;
var height=bounds.y+bounds.height+1+border;
if(this.minimumContainerSize!=null)
{
width=Math.max(width,this.minimumContainerSize.width);
height=Math.max(height,this.minimumContainerSize.height);
}
if(this.resizeContainer)
{
var w=width;
var h=height;
if(this.maximumContainerSize!=null)
{
w=Math.min(this.maximumContainerSize.width,w);
h=Math.min(this.maximumContainerSize.height,h);
}
this.container.style.width=w+'px';
this.container.style.height=h+'px';
}
width=Math.max(width,this.container.offsetWidth);
height=Math.max(height,this.container.offsetHeight);
if(this.dialect==mxConstants.DIALECT_SVG)
{
var root=this.view.getDrawPane().ownerSVGElement;

root.setAttribute('width',width);
root.setAttribute('height',height);
}
else
{
var canvas=this.view.getDrawPane();
canvas.style.width=width+'px';
canvas.style.height=height+'px';
}
}
this.dispatchEvent('size',this,bounds);
};

mxGraph.prototype.getCellStyle=function(cell)
{
var stylename=this.model.getStyle(cell);
var style=null;
if(this.model.isEdge(cell))
{
style=this.stylesheet.getDefaultEdgeStyle();
}
else
{
style=this.stylesheet.getDefaultVertexStyle();
}
if(stylename!=null)
{
style=this.stylesheet.getCellStyle(stylename,style);
}
if(style==null)
{
style=mxGraph.prototype.EMPTY_ARRAY;
}
return style;
};
mxGraph.prototype.setCellStyle=function(style,cells)
{
cells=cells||this.getSelectionCells();
if(cells!=null)
{
this.model.beginUpdate();
try
{
for(var i=0;i<cells.length;i++)
{
this.model.setStyle(cells[i],style);
}
}
finally
{
this.model.endUpdate();
}
}
};
mxGraph.prototype.toggleCellStyle=function(key,defaultValue,cell)
{
cell=cell||this.getSelectionCell();
this.toggleCellStyles(key,defaultValue,[cell]);
};
mxGraph.prototype.toggleCellStyles=function(key,defaultValue,cells)
{
defaultValue=(defaultValue!=null)?defaultValue:false;
cells=cells||this.getSelectionCells();
if(cells!=null&&cells.length>0)
{
var state=this.view.getState(cells[0]);
var style=(state!=null)?state.style:this.getCellStyle(cells[0]);
if(style!=null)
{
var val=(mxUtils.getValue(style,key,defaultValue))?0:1;
this.setCellStyles(key,val,cells);
}
}
}
mxGraph.prototype.setCellStyles=function(key,value,cells)
{
cells=cells||this.getSelectionCells();
mxUtils.setCellStyles(this.model,cells,key,value);
};
mxGraph.prototype.toggleCellStyleFlags=function(key,flag,cells)
{
this.setCellStyleFlags(key,flag,null,cells);
};
mxGraph.prototype.setCellStyleFlags=function(key,flag,value,cells)
{
cells=cells||this.getSelectionCells();
if(cells!=null&&cells.length>0)
{
if(value==null)
{
var state=this.view.getState(cells[0]);
var style=(state!=null)?state.style:this.getCellStyle(cells[0]);
if(style!=null)
{
var current=parseInt(style[key]||0);
value=!((current&flag)==flag);
}
}
mxUtils.setCellStyleFlags(this.model,cells,key,flag,value);
}
};

mxGraph.prototype.alignCells=function(align,cells,param)
{
cells=cells||this.getSelectionCells();
if(cells!=null&&cells.length>1)
{
if(param==null)
{
for(var i=0;i<cells.length;i++)
{
var g=this.getCellGeometry(cells[i]);
if(g!=null&&!this.model.isEdge(cells[i]))
{
if(param==null)
{
if(align==mxConstants.ALIGN_CENTER)
{
param=g.x+g.width/2;
break;
}
else if(align==mxConstants.ALIGN_RIGHT)
{
param=g.x+g.width;
}
else if(align==mxConstants.ALIGN_TOP)
{
param=g.y;
}
else if(align==mxConstants.ALIGN_MIDDLE)
{
param=g.y+g.height/2;
break;
}
else if(align==mxConstants.ALIGN_BOTTOM)
{
param=g.y+g.height;
}
else
{
param=g.x;
}
}
else
{
if(align==mxConstants.ALIGN_RIGHT)
{
param=Math.max(param,g.x+g.width);
}
else if(align==mxConstants.ALIGN_TOP)
{
param=Math.min(param,g.y);
}
else if(align==mxConstants.ALIGN_BOTTOM)
{
param=Math.max(param,g.y+g.height);
}
else
{
param=Math.min(param,g.x);
}
}
}
}
}
this.model.beginUpdate();
try
{
for(var i=0;i<cells.length;i++)
{
var g=this.getCellGeometry(cells[i]);
if(g!=null&&!this.model.isEdge(cells[i]))
{
g=g.clone();
if(align==mxConstants.ALIGN_CENTER)
{
g.x=param-g.width/2;
}
else if(align==mxConstants.ALIGN_RIGHT)
{
g.x=param-g.width;
}
else if(align==mxConstants.ALIGN_TOP)
{
g.y=param;
}
else if(align==mxConstants.ALIGN_MIDDLE)
{
g.y=param-g.height/2;
}
else if(align==mxConstants.ALIGN_BOTTOM)
{
g.y=param-g.height;
}
else
{
g.x=param;
}
this.model.setGeometry(cells[i],g);
}
}
}
finally
{
this.model.endUpdate();
}
}
};
mxGraph.prototype.flip=function(edge)
{
if(edge!=null)
{
this.model.beginUpdate();
try
{
if(this.alternateEdgeStyle!=null)
{
var style=this.model.getStyle(edge);
if(style==null||style.length==0)
{
this.model.setStyle(edge,this.alternateEdgeStyle);
}
else
{
this.model.setStyle(edge,null);
}
var geo=this.model.getGeometry(edge);
if(geo!=null)
{
geo=geo.clone();
geo.points=new Array();
this.model.setGeometry(edge,geo);
}
}
this.dispatchEvent('flip',this,edge);
}
finally
{
this.model.endUpdate();
}
}
return edge;
};

mxGraph.prototype.toBack=function(cells)
{
this.setIndexForCells(cells,0);
};
mxGraph.prototype.toFront=function(cells)
{
this.setIndexForCells(cells);
};
mxGraph.prototype.setIndexForCells=function(cells,index)
{
cells=cells||this.getSelectionCells();
if(cells!=null)
{
this.model.beginUpdate();
try
{
for(var i=0;i<cells.length;i++)
{
var parent=this.model.getParent(cells[i]);
this.model.add(parent,cells[i],index);
}
this.dispatchEvent('indexChanged',this,cells,index);
}
finally
{
this.model.endUpdate();
}
}
};

mxGraph.prototype.cloneCells=function(cells)
{
var tmp=null;
if(cells!=null)
{
var hash=new Array();
tmp=new Array();
for(var i=0;i<cells.length;i++)
{
if(this.isCellCloneable(cells[i]))
{
var id=mxCellPath.create(cells[i]);
hash[id]=cells[i];
tmp.push(cells[i]);
}
}
if(tmp.length>0)
{
var scale=this.view.scale;
var trans=this.view.translate;
cells=tmp;
var clones=this.model.cloneCells(cells,true);
for(var i=0;i<cells.length;i++)
{
var g=this.model.getGeometry(clones[i]);
if(g!=null)
{
var state=this.view.getState(cells[i]);
var pstate=this.view.getState(this.model.getParent(cells[i]));
if(state!=null&&pstate!=null)
{
var dx=pstate.origin.x;
var dy=pstate.origin.y;
if(this.model.isEdge(clones[i]))
{
var pts=state.absolutePoints;
var src=this.model.getTerminal(cells[i],true);
var srcId=mxCellPath.create(src);
while(src!=null&&hash[srcId]==null)
{
src=this.model.getParent(src);
srcId=mxCellPath.create(src);
}
if(src==null)
{
g.setTerminalPoint(new mxPoint(pts[0].x/scale-trans.x,pts[0].y/scale-trans.y),true);
}
var trg=this.model.getTerminal(cells[i],false);
var trgId=mxCellPath.create(trg);
while(trg!=null&&hash[trgId]==null)
{
trg=this.model.getParent(trg);
trgId=mxCellPath.create(trg);
}
if(trg==null)
{
var n=pts.length-1;
g.setTerminalPoint(new mxPoint(pts[n].x/scale-trans.x,pts[n].y/scale-trans.y),false);
}
var points=g.points;
if(points!=null)
{
for(var j=0;j<points.length;j++)
{
points[j].x+=dx;
points[j].y+=dy;
}
}
}
else
{
g.x+=dx;
g.y+=dy;
}
}
}
}
return clones;
}
}
return tmp;
};
mxGraph.prototype.insertVertex=function(parent,id,value,x,y,width,height,style)
{
var vertex=this.createVertex(parent,id,value,x,y,width,height,style);
return this.addCell(vertex,parent);
};
mxGraph.prototype.createVertex=function(parent,id,value,x,y,width,height,style)
{
var geometry=new mxGeometry(x,y,width,height);
var vertex=new mxCell(value,geometry,style);
vertex.setId(id);
vertex.setVertex(true);
vertex.setConnectable(true);
return vertex;
};
mxGraph.prototype.insertEdge=function(parent,id,value,source,target,style)
{
var edge=this.createEdge(parent,id,value,source,target,style);
return this.addEdge(edge,parent,source,target);
};
mxGraph.prototype.createEdge=function(parent,id,value,source,target,style)
{
var edge=new mxCell(value,new mxGeometry(),style);
edge.setId(id);
edge.setEdge(true);
edge.geometry.relative=true;
return edge;
};
mxGraph.prototype.addCells=function(cells,parent,index)
{
parent=parent||this.getDefaultParent();
index=(index!=null)?index:this.model.getChildCount(parent);
this.dispatchEvent('beforeAdd',this,cells,parent,index);
this.model.beginUpdate();
try
{
for(var i=0;i<cells.length;i++)
{
this.model.add(parent,cells[i],index+i);
}
this.dispatchEvent('add',this,cells);
this.layoutAfterAdd(parent,cells);
}
finally
{
this.model.endUpdate();
}
this.dispatchEvent('afterAdd',this,cells);
return cells;
};
mxGraph.prototype.addEdge=function(edge,parent,source,target,index)
{
return this.addCell(edge,parent,index,source,target);
};
mxGraph.prototype.addCell=function(cell,parent,index,source,target)
{
parent=parent||this.getDefaultParent();
index=(index!=null)?index:this.model.getChildCount(parent);
this.dispatchEvent('beforeAdd',this,[cell],parent,index,source,target);
this.model.beginUpdate();
try
{
cell=this.model.add(parent,cell,index);
if(cell!=null)
{
if(source!=null)
{
this.model.setTerminal(cell,source,true);
this.dispatchEvent('connect',this,cell,source,true);
}
if(target!=null)
{
this.model.setTerminal(cell,target,false);
this.dispatchEvent('connect',this,cell,target,false);
}
this.dispatchEvent('add',this,[cell]);
this.layoutAfterAdd(parent,[cell]);
}
}
finally
{
this.model.endUpdate();
}
if(cell!=null)
{
this.dispatchEvent('afterAdd',this,[cell]);
}
return cell;
};
mxGraph.prototype.layoutAfterAdd=function(parent,cells)
{
return this.layout([parent]);
};
mxGraph.prototype.splitEdge=function(edge,cell,newEdge)
{
newEdge=newEdge||this.cloneCells([edge])[0];
var parent=this.model.getParent(edge);
var index=this.model.getChildCount(parent);
var source=this.model.getTerminal(edge,true);
this.dispatchEvent('beforeAdd',this,[newEdge],parent,index,source,cell);
this.model.beginUpdate();
try
{
this.model.add(parent,newEdge,index);
this.model.setTerminals(newEdge,source,cell);
this.dispatchEvent('connect',this,newEdge,source,true);
this.dispatchEvent('connect',this,newEdge,cell,false);
this.model.setTerminal(edge,cell,true);
this.dispatchEvent('connect',this,edge,cell,true);
this.dispatchEvent('add',this,[newEdge]);
this.layoutAfterSplit(parent,edge,cell,newEdge);
}
finally
{
this.model.endUpdate();
}
this.dispatchEvent('afterAdd',this,[newEdge]);
return newEdge;
};
mxGraph.prototype.layoutAfterSplit=function(parent,edge,cell,newEdge)
{
return this.layout([parent]);
};

mxGraph.prototype.hide=function(cells,hideEdges)
{
this.remove(cells,hideEdges,true,false);
};
mxGraph.prototype.show=function(cells,showEdges)
{
this.remove(cells,showEdges,false,true);
};
mxGraph.prototype.remove=function(cells,includeEdges,isHide,isShow)
{
cells=cells||this.getSelectionCells();
includeEdges=(includeEdges!=null)?includeEdges:true;
isHide=(isHide!=null)?isHide:false;
isShow=(isShow!=null)?isShow:false;
if(cells.length>0)
{
var evtName=(isShow)?'Show':((isHide)?'Hide':'Remove');
this.dispatchEvent('before'+evtName,this,tmp);
var scale=this.view.scale;
var tr=this.view.translate;
var tmp=new Array();
this.model.beginUpdate();
try
{
var parents=this.getParents(cells);
for(var i=0;i<cells.length;i++)
{
if(isHide||isShow)
{
if(includeEdges)
{
this.removeEdges(cells[i],true,isHide,isShow);
}
this.model.setVisible(cells[i],isShow);
tmp.push(cells[i]);
}
else if(this.isDeletable(cells[i]))
{
if(includeEdges)
{
this.removeEdges(cells[i],true);
}
else
{
var edges=this.getConnections(cells[i]);
for(var j=0;j<edges.length;j++)
{
if(mxUtils.indexOf(cells,edges[j])<0)
{
var g=this.model.getGeometry(edges[j]);
if(g!=null)
{
var state=this.view.getState(edges[j]);
if(state!=null)
{
g=g.clone();
var source=this.view.getVisibleTerminal(edges[j],true)
==cells[i];
var pts=state.absolutePoints;
var n=(source)?0:pts.length-1;
g.setTerminalPoint(new mxPoint(pts[n].x/scale-tr.x,pts[n].y/scale-tr.y),source);
this.model.setTerminal(edges[j],null,source);
this.model.setGeometry(edges[j],g);
}
}
}
}
}
this.model.remove(cells[i]);
tmp.push(cells[i]);
}
}

this.dispatchEvent(evtName.toLowerCase(),this,tmp,parents);
this.layoutAfterRemove(parents,tmp,isHide,isShow);
}
finally
{
this.model.endUpdate();
}
this.dispatchEvent('after'+evtName,this,tmp);
}
};
mxGraph.prototype.layoutAfterRemove=function(parents,cells,isHide,isShow)
{
return this.layout(parents);
};
mxGraph.prototype.hideEdges=function(cell,recurse)
{
this.removeEdges(cell,recurse,true);
};
mxGraph.prototype.showEdges=function(cell,recurse)
{
this.removeEdges(cell,recurse,false,true);
};
mxGraph.prototype.removeEdges=function(cell,recurse,isHide,isShow)
{
recurse=(recurse!=null)?recurse:true;
isHide=(isHide!=null)?isHide:false;
isShow=(isShow!=null)?isShow:false;
if(cell!=null)
{
var tmp=new Array();
this.model.beginUpdate();
try
{
var edges=this.model.getEdges(cell);
if(edges!=null&&edges.length>0)
{
if(isShow)
{
this.dispatchEvent('beforeShow',this,tmp);
}
else if(isHide)
{
this.dispatchEvent('beforeHide',this,tmp);
}
else
{
this.dispatchEvent('beforeRemove',this,tmp);
}
if(isShow||isHide)
{
for(var i=0;i<edges.length;i++)
{
this.model.setVisible(edges[i],isShow);
tmp.push(edges[i]);
}
}
else
{
for(var i=0;i<edges.length;i++)
{
if(this.isDeletable(edges[i]))
{
this.model.remove(edges[i]);
tmp.push(edges[i]);
}
}
}
if(isShow)
{
this.dispatchEvent('show',this,tmp);
}
else if(isHide)
{
this.dispatchEvent('hide',this,tmp);
}
else
{
this.dispatchEvent('remove',this,tmp);
}
}
if(recurse)
{
var children=this.model.getChildren(cell);
if(children!=null)
{
for(var i=0;i<children.length;i++)
{
this.removeEdges(children[i],true,isHide,isShow);
}
}
}
}
finally
{
this.model.endUpdate();
}
if(tmp.length>0)
{
if(isShow)
{
this.dispatchEvent('afterShow',this,tmp);
}
else if(isHide)
{
this.dispatchEvent('afterHide',this,tmp);
}
else
{
this.dispatchEvent('afterRemove',this,tmp);
}
}
}
};

mxGraph.prototype.updateSize=function(cell)
{
if(cell!=null)
{
var size=this.getPreferredSizeForCell(cell);
var geo=this.model.getGeometry(cell);
if(size!=null&&geo!=null)
{
this.dispatchEvent('beforeUpdateSize',this,cell,size);
this.model.beginUpdate();
try
{
var geometry=geo.clone();
if(this.isSwimlane(cell))
{
geometry=this.updateSwimlaneSize(cell,geometry,size);
}
else
{
geometry.width=size.width;
geometry.height=size.height;
}
if(geo.width!=geometry.width||geo.height!=geometry.height)
{
this.resize(cell,geometry);
}
this.dispatchEvent('updateSize',this,cell,geometry);
}
finally
{
this.model.endUpdate();
}
this.dispatchEvent('afterUpdateSize',this,cell);
}
}
return cell;
};
mxGraph.prototype.updateSwimlaneSize=function(swimlane,geometry,pSize)
{
if(swimlane!=null&&geometry!=null&&pSize!=null)
{
this.model.beginUpdate();
try
{
var collapsed=this.isCellCollapsed(swimlane);
var style=this.getCellStyle(swimlane);
var cellStyle=this.model.getStyle(swimlane)||'';
if(mxUtils.getValue(style,mxConstants.STYLE_HORIZONTAL,true))
{
cellStyle=mxUtils.setStyle(cellStyle,mxConstants.STYLE_STARTSIZE,pSize.height);
if(collapsed)
{
geometry.height=pSize.height;
}
geometry.width=pSize.width;
}
else
{
cellStyle=mxUtils.setStyle(cellStyle,mxConstants.STYLE_STARTSIZE,pSize.width);
if(collapsed)
{
geometry.width=pSize.width;
}
geometry.height=pSize.height;
}
this.model.setStyle(swimlane,cellStyle);
if(!collapsed)
{
var cBounds=this.view.getBounds(this.model.getChildren(swimlane));
if(cBounds!=null)
{
var t=this.view.translate;
var s=this.view.scale;
var width=(cBounds.x+cBounds.width)/s-geometry.x-t.x;
var height=(cBounds.y+cBounds.height)/s-geometry.y-t.y;
geometry.width=Math.max(geometry.width,width);
geometry.height=Math.max(geometry.height,height);
}
}
}
finally
{
this.model.endUpdate();
}
}
return geometry;
};
mxGraph.prototype.getPreferredSizeForCell=function(cell)
{
var result=null;
if(cell!=null)
{
var state=this.view.getState(cell);
var style=(state!=null)?state.style:this.getCellStyle(cell);
if(style!=null&&!this.model.isEdge(cell))
{
var fontSize=style[mxConstants.STYLE_FONTSIZE]||10;
var dx=0;
var dy=0;
if(this.getImage(state)!=null||style[mxConstants.STYLE_IMAGE]!=null)
{
if(style[mxConstants.STYLE_SHAPE]==mxConstants.SHAPE_LABEL)
{
if(style[mxConstants.STYLE_VERTICAL_ALIGN]==mxConstants.ALIGN_MIDDLE)
{
dx+=style[mxConstants.STYLE_IMAGE_WIDTH]||mxLabel.prototype.imageSize;
}
if(style[mxConstants.STYLE_ALIGN]!=mxConstants.ALIGN_CENTER)
{
dy+=style[mxConstants.STYLE_IMAGE_HEIGHT]||mxLabel.prototype.imageSize;
}
}
}
dx+=2*(style[mxConstants.STYLE_SPACING]||0);
dx+=style[mxConstants.STYLE_SPACING_LEFT]||0;
dx+=style[mxConstants.STYLE_SPACING_RIGHT]||0;
dy+=2*(style[mxConstants.STYLE_SPACING]||0);
dy+=style[mxConstants.STYLE_SPACING_TOP]||0;
dy+=style[mxConstants.STYLE_SPACING_BOTTOM]||0;


var image=this.getFoldingImage(state);
if(image!=null)
{
dx+=image.width+8;
}
var value=this.getLabel(cell);
if(value!=null&&value.length>0)
{
if(!this.isHtmlLabel(cell))
{
value=value.replace(/\n/g,'<br>');
}
var size=this.getSizeForString(value,fontSize,style[mxConstants.STYLE_FONTFAMILY]);
var width=size.width+dx;
var height=size.height+dy;
if(style[mxConstants.STYLE_HORIZONTAL]==false)
{
var tmp=height;
height=width;
width=tmp;
}
if(this.gridEnabled)
{
width=this.snap(width+this.gridSize/2);
height=this.snap(height+this.gridSize/2);
}
result=new mxRectangle(0,0,width,height);
}
else
{
var gs2=4*this.gridSize;
result=new mxRectangle(0,0,gs2,gs2);
}
}
}
return result;
};
mxGraph.prototype.getSizeForString=function(text,fontSize,fontFamily)
{
var div=document.createElement('div');
div.style.fontSize=fontSize||mxConstants.DEFAULT_FONTSIZE;
div.style.fontFamily=fontFamily||mxConstants.DEFAULT_FONTFAMILY 
div.style.position='absolute';
div.style.display='inline';
div.style.visibility='hidden';
div.innerHTML=text;
document.body.appendChild(div);
var size=new mxRectangle(0,0,div.offsetWidth,div.offsetHeight);
document.body.removeChild(div);
return size;
};
mxGraph.prototype.resize=function(cell,bounds)
{
this.resizeCells([cell],[bounds]);
};
mxGraph.prototype.resizeCells=function(cells,boundsArray)
{
if(cells!=null&&boundsArray!=null)
{
this.dispatchEvent('beforeResize',this,cells,boundsArray);
var tmp=new Array();
var layout=new Array();
var tmpBounds=new Array();
var oldBounds=new Array();
this.model.beginUpdate();
try
{
for(var i=0;i<cells.length;i++)
{
var cell=cells[i];
var bounds=boundsArray[i];
var g=this.model.getGeometry(cell);
if(g.x!=bounds.x||g.y!=bounds.y||g.width!=bounds.width||g.height!=bounds.height)
{
tmp.push(cell);
tmpBounds.push(bounds);
oldBounds.push(g);
g=g.clone();
if(g.relative)
{
if(g.offset!=null)
{
g.offset.x+=bounds.x-g.x;
g.offset.y+=bounds.y-g.y;
}
}
else
{
g.x=bounds.x;
g.y=bounds.y;
}
g.width=bounds.width;
g.height=bounds.height;
this.model.setGeometry(cell,g);
if(this.isExtendParentOnResize(cell))
{
this.extendParent(cell);
}
if(!this.isCellCollapsed(cell))
{
layout.push(cell);
}
}
}
if(tmp.length>0)
{
this.dispatchEvent('resize',this,tmp,tmpBounds,oldBounds);
if(this.resetEdgesOnResize)
{
this.resetEdges(tmp);
}
if(this.layoutAfterResize(this.getParents(tmp),layout).length==0)
{
this.cascadeResize(tmp[0]);
}
}
}
finally
{
this.model.endUpdate();
}
this.dispatchEvent('afterResize',this,tmp);
}
};
mxGraph.prototype.layoutAfterResize=function(parents,cells)
{
return this.layout(cells.concat(parents));
};
mxGraph.prototype.cascadeResize=function(cell)
{
if(cell!=null)
{
var state=this.view.getState(cell);
var pstate=this.view.getState(this.model.getParent(cell));
if(state!=null&&pstate!=null)
{
var cells=this.getCellsToShift(state);
if(cells!=null)
{
var scale=this.view.scale;
var x0=state.x-pstate.origin.x-this.view.translate.x*scale;
var y0=state.y-pstate.origin.y-this.view.translate.y*scale;
var right=state.x+state.width;
var bottom=state.y+state.height;
var geo=this.model.getGeometry(cell);
var dx=state.width-geo.width*scale+x0-geo.x*scale;
var dy=state.height-geo.height*scale+y0-geo.y*scale;
var fx=1-geo.width*scale/state.width;
var fy=1-geo.height*scale/state.height;
this.model.beginUpdate();
try
{
for(var i=0;i<cells.length;i++)
{
var g=this.model.getGeometry(cells[i]);
state=this.view.getState(cells[i]);
if(state!=null&&cells[i]!=cell&&this.isShiftable(cells[i]))
{
if(this.shiftRightwards)
{
if(state.x>=right)
{
g=g.translate(-dx,0);
}
else
{
var tmpDx=Math.max(0,state.x-x0);
g=g.translate(-fx*tmpDx,0);
}
}
if(this.shiftDownwards)
{
if(state.y>=bottom)
{
g=g.translate(0,-dy);
}
else
{
var tmpDy=Math.max(0,state.y-y0);
g=g.translate(0,-fy*tmpDy);
}
if(g!=this.model.getGeometry(cells[i]))
{
this.model.setGeometry(cells[i],g);
if(this.isExtendParentOnResize(cells[i]))
{
this.extendParent(cells[i]);
}
}
}
}
}
}
finally
{
this.model.endUpdate();
}
}
}
}
};
mxGraph.prototype.isExtendParentOnResize=function(cell)
{
return this.extendParentOnResize;
};
mxGraph.prototype.extendParent=function(cell)
{
if(cell!=null)
{
var parent=this.model.getParent(cell);
var p=this.model.getGeometry(parent);
if(parent!=null&&p!=null&&!this.isCellCollapsed(parent))
{
var g=this.model.getGeometry(cell);
if(g!=null&&(p.width<g.x+g.width||p.height<g.y+g.height))
{
p=p.clone();
p.width=Math.max(p.width,g.x+g.width);
p.height=Math.max(p.height,g.y+g.height);
this.resize(parent,p);
}
}
}
};
mxGraph.prototype.getCellsToShift=function(state)
{
return this.getCellsBeyond(state.x+((this.shiftDownwards)?0:state.width),state.y+((this.shiftDownwards&&this.shiftRightwards)?0:state.height),this.model.getParent(state.cell),this.shiftRightwards,this.shiftDownwards);
};

mxGraph.prototype.move=function(cells,dx,dy,clone,target,evt)
{
var clones=cells;
dx=dx||0;
dy=dy||0;
if(clones!=null&&(dx!=0||dy!=0||clone||target!=null))
{
this.dispatchEvent('beforeMove',this,cells,dx,dy,clone,target,evt);
this.model.beginUpdate();
try
{
if(clone)
{
clones=this.cloneCells(cells);
for(var i=0;i<clones.length;i++)
{
if(this.cloneInvalidEdges||!this.model.isEdge(clones[i])||this.getEdgeValidationError(clones[i],this.model.getTerminal(clones[i],true),this.model.getTerminal(clones[i],false))==null)
{
var parent=this.getDefaultParent();
this.model.add(parent,clones[i]);
var pstate=this.view.getState(parent);
var geo=this.model.getGeometry(clones[i]);
if(pstate!=null&&geo!=null)
{
this.model.setGeometry(clones[i],geo.translate(-pstate.origin.x,-pstate.origin.y));
}
}
else
{
clones[i]=null;
}
}
this.dispatchEvent('clone',this,clones,cells);
}
else if(this.disconnectOnMove&&this.allowDanglingEdges)
{
this.disconnect(cells);
}
for(var i=0;i<clones.length;i++)
{
var g=this.model.getGeometry(clones[i]);
if(g!=null&&this.isMovable(clones[i]))
{
g=g.translate(dx,dy);
if(g.relative&&!this.model.isEdge(clones[i]))
{
if(g.offset==null)
{
g.offset=new mxPoint(dx,dy);
}
else
{
g.offset.x+=dx;
g.offset.y+=dy;
}
}
this.model.setGeometry(clones[i],g);
}
}
this.moveInto(clones,target,evt);
}
finally
{
this.model.endUpdate();
}
this.dispatchEvent('afterMove',this,cells,clones,dx,dy,clone,target,evt);
}
return clones;
};
mxGraph.prototype.getMaximumGraphBounds=function()
{
return this.maximumGraphBounds;
};
mxGraph.prototype.getContentArea=function(cell)
{
if(cell!=null&&!this.model.isEdge(cell))
{
var parent=this.model.getParent(cell);
if(parent==this.getDefaultParent()||parent==this.getCurrentRoot())
{
return this.getMaximumGraphBounds();
}
else(parent!=null&&parent!=this.getDefaultParent())
{
var g=this.model.getGeometry(parent);
if(g!=null)
{
var x=0;
var y=0;
var w=g.width;
var h=g.height;
if(this.isSwimlane(parent))
{
var size=this.getStartSize(parent);
x=size.width;
w-=size.width;
y=size.height;
h-=size.height;
}
return new mxRectangle(x,y,w,h);
}
}
}
return null;
};
mxGraph.prototype.removeFromParent=function(cells)
{
cells=cells||this.getSelectionCells();
if(cells!=null)
{
var parent=this.getDefaultParent();

var model=this.getModel();
var tmp=new Array();
for(var i=0;i<cells.length;i++)
{
if(model.getParent(cells[i])!=parent)
{
tmp.push(cells[i]);
}
}
if(tmp.length>0)
{
this.moveInto(tmp,parent);
}
}
};
mxGraph.prototype.moveInto=function(cells,target,evt)
{
if(cells!=null&&cells.length>0)
{
var parents=null;
this.model.beginUpdate();
try
{
if(target!=null)
{
parents=[target];
var cell=cells[0];
if(this.model.isEdge(target)&&this.model.isConnectable(cell))
{
if(this.getEdgeValidationError(target,this.model.getTerminal(target,true),cell)==null)
{
this.splitEdge(target,cell);
}
}
else
{


var hash=new Array();
for(var i=0;i<cells.length;i++)
{
if(cells[i]!=target)
{
var parent=this.model.getParent(cells[i]);
var id=mxCellPath.create(parent);
if(hash[id]==null)
{
hash[id]=parent;
parents.push(parent);
}
if(target!=parent)
{
var state=this.view.getState(target);
var pstate=this.view.getState(parent);
var g=this.model.getGeometry(cells[i]);
if(g!=null&&state!=null&&pstate!=null)
{
g=g.translate(pstate.origin.x-state.origin.x,pstate.origin.y-state.origin.y);
this.model.setGeometry(cells[i],g);
}
}
this.model.add(target,cells[i]);
}
}
}
}
else
{
parents=this.getParents(cells);
}
this.cellsMoved(cells,evt);
this.keepInside(cells);
if(this.resetEdgesOnMove)
{
this.resetEdges(cells);
}
this.dispatchEvent('move',this,cells,parents,target);
this.layoutAfterMove(parents,cells,target);
}
finally
{
this.model.endUpdate();
}
}
};
mxGraph.prototype.cellsMoved=function(cells,evt)
{
if(cells!=null&&evt!=null)
{
var point=mxUtils.convertPoint(this.container,evt.clientX,evt.clientY);
for(var i=0;i<cells.length;i++)
{
var layout=this.getLayout(this.model.getParent(cells[i]));
if(layout!=null&&layout.move!=null)
{
layout.move(cells[i],point.x,point.y);
}
}
}
};
mxGraph.prototype.keepInside=function(cells)
{
if(cells!=null)
{
this.model.beginUpdate();
try
{
for(var i=0;i<cells.length;i++)
{
var cell=cells[i];
var c=(this.isKeepInsideParentOnMove(cell)?this.getContentArea(cell):
this.getMaximumGraphBounds());
if(c!=null)
{
var g=this.model.getGeometry(cell);
if(!g.relative&&(g.x<c.x||g.y<c.y||c.width<g.x+g.width||c.height<g.y+g.height))
{
var overlap=this.getOverlap(cell);
if(c.width>0)
{
g.x=Math.min(g.x,c.x+c.width-(1-overlap)*g.width);
}
if(c.height>0)
{
g.y=Math.min(g.y,c.y+c.height-(1-overlap)*g.height);
}
g.x=Math.max(g.x,c.x-g.width*overlap);
g.y=Math.max(g.y,c.y-g.height*overlap);
}
}
}
}
finally
{
this.model.endUpdate();
}
}
};
mxGraph.prototype.resetEdges=function(cells)
{
if(cells!=null)
{
var hash=new Array();
for(var i=0;i<cells.length;i++)
{
var id=mxCellPath.create(cells[i]);
hash[id]=cells[i];
}
this.model.beginUpdate();
try
{
for(var i=0;i<cells.length;i++)
{
var edges=this.model.getEdges(cells[i]);
if(edges!=null)
{
for(var j=0;j<edges.length;j++)
{
var source=this.view.getVisibleTerminal(edges[j],true);
var sourceId=mxCellPath.create(source);
var target=this.view.getVisibleTerminal(edges[j],false);
var targetId=mxCellPath.create(target);
if(hash[sourceId]==null||hash[targetId]==null)
{
var geo=this.model.getGeometry(edges[j]);
if(geo!=null&&geo.points!=null&&geo.points.length>0)
{
geo=geo.clone();
geo.points=new Array();
this.model.setGeometry(edges[j],geo);
}
}
}
}
this.resetEdges(this.model.getChildren(cells[i]));
}
}
finally
{
this.model.endUpdate();
}
}
};
mxGraph.prototype.layoutAfterMove=function(parents,cells,target)
{
return this.layout(parents);
};

mxGraph.prototype.connect=function(edge,terminal,isSource)
{
if(edge!=null)
{
this.dispatchEvent('beforeConnect',this,edge,terminal,isSource);
this.model.beginUpdate();
try
{
this.model.setTerminal(edge,terminal,isSource);
var geo=this.model.getGeometry(edge);
if(geo!=null&&geo.points!=null)
{
geo=geo.clone();
geo.points=new Array();
this.model.setGeometry(edge,geo);
}
this.dispatchEvent('connect',this,edge,terminal,isSource);
this.layoutAfterConnect(edge,terminal,isSource);
}
finally
{
this.model.endUpdate();
}
this.dispatchEvent('afterConnect',this,edge,terminal,isSource);
}
};
mxGraph.prototype.layoutAfterConnect=function(edge,terminal,isSource)
{
var parent=this.model.getParent(terminal);
if(parent!=null)
{
return this.layout([parent]);
}
return false;
};
mxGraph.prototype.disconnect=function(cells)
{
if(cells!=null)
{
this.dispatchEvent('beforeDisconnect',this,cells);
this.model.beginUpdate();
try
{
var scale=this.view.scale;
var tr=this.view.translate;
var hash=new Array();
for(var i=0;i<cells.length;i++)
{
var id=mxCellPath.create(cells[i]);
hash[id]=cells[i];
}
for(var i=0;i<cells.length;i++)
{
if(this.model.isEdge(cells[i]))
{
var g=this.model.getGeometry(cells[i]);
if(g!=null)
{
var state=this.view.getState(cells[i]);
var pstate=this.view.getState(this.model.getParent(cells[i]));
if(state!=null&&pstate!=null)
{
g=g.clone();
var dx=-pstate.origin.x;
var dy=-pstate.origin.y;
var pts=state.absolutePoints;
var src=this.model.getTerminal(cells[i],true);
if(src!=null&&this.isDisconnectable(cells[i],src,true))
{
var srcId=mxCellPath.create(src);
while(src!=null&&hash[srcId]==null)
{
src=this.model.getParent(src);
srcId=mxCellPath.create(src);
}
if(src==null)
{
g.setTerminalPoint(new mxPoint(pts[0].x/scale-tr.x+dx,pts[0].y/scale-tr.y+dy),true);
this.model.setTerminal(cells[i],null,true);
}
}
var trg=this.model.getTerminal(cells[i],false);
if(trg!=null&&this.isDisconnectable(cells[i],trg,false))
{
var trgId=mxCellPath.create(trg);
while(trg!=null&&hash[trgId]==null)
{
trg=this.model.getParent(trg);
trgId=mxCellPath.create(trg);
}
if(trg==null)
{
var n=pts.length-1;
g.setTerminalPoint(new mxPoint(pts[n].x/scale-tr.x+dx,pts[n].y/scale-tr.y+dy),false);
this.model.setTerminal(cells[i],null,false);
}
}
this.model.setGeometry(cells[i],g);
}
}
}
}
this.dispatchEvent('disconnect',this,cells);
}
finally
{
this.model.endUpdate();
}
this.dispatchEvent('afterDisconnect',this,cells);
}
};

mxGraph.prototype.getBounds=function()
{
return this.view.bounds;
}
mxGraph.prototype.getCellBounds=function(cell,includeEdges,includeDescendants)
{
var cells=[cell];
if(includeEdges)
{
cells=cells.concat(this.model.getEdges(cell));
}
var result=this.view.getBounds(cells);
if(includeDescendants)
{
var childCount=this.model.getChildCount(cell);
for(var i=0;i<childCount;i++)
{
var tmp=this.getCellBounds(this.model.getChildAt(cell,i),includeEdges,true);
if(result!=null)
{
result.add(tmp);
}
else
{
result=tmp;
}
}
}
};
mxGraph.prototype.refresh=function(cell)
{
this.view.clear(cell,cell==null);
this.view.validate();
this.sizeDidChange();
this.dispatchEvent('refresh',this);
};
mxGraph.prototype.snap=function(value)
{
if(this.gridEnabled)
{
value=Math.round(value/this.gridSize)*this.gridSize;
}
return value;
};
mxGraph.prototype.shift=function(dx,dy)
{
var style=mxUtils.getCurrentStyle(this.container);
if(mxUtils.hasScrollbars(this.container))
{
this.container.scrollLeft=-dx;
this.container.scrollTop=-dy;
}
else
{
var canvas=this.view.getCanvas();
if(this.dialect==mxConstants.DIALECT_SVG)
{
canvas.setAttribute('transform','translate('+dx+','+dy+')');
if(dx==0&&dy==0)
{
if(this.shiftPreview!=null)
{
this.shiftPreview.parentNode.removeChild(this.shiftPreview);
this.shiftPreview=null;
var child=this.container.firstChild;
while(child!=null)
{
if(child!=canvas.parentNode)
{
if(child.style!=null)
{
child.style.visibility='visible';
}
}
child=child.nextSibling;
}
}
}
else
{
if(this.shiftPreview==null)
{
this.shiftPreview=document.createElement('div');
var tmp=new Array();
var child=this.container.firstChild;
while(child!=null)
{
if(child!=canvas.parentNode)
{
tmp.push(mxUtils.getInnerHtml(child));
if(child.style!=null)
{
child.style.visibility='hidden';
}
}
child=child.nextSibling;
}
this.shiftPreview.innerHTML=tmp.join('');
this.shiftPreview.style.position='absolute';
this.shiftPreview.style.overflow='visible';
var pt=mxUtils.getOffset(this.container);
this.shiftPreview.style.left=pt.x+'px';
this.shiftPreview.style.top=pt.y+'px';
this.container.appendChild(this.shiftPreview);
}
this.shiftPreview.style.left=dx+'px';
this.shiftPreview.style.top=dy+'px';
}
}
else if(this.dialect==mxConstants.DIALECT_VML)
{
canvas.setAttribute('coordorigin',(-dx)+','+(-dy));
}
else
{
if(dx==0&&dy==0)
{
if(this.shiftPreview!=null)
{
this.shiftPreview.parentNode.removeChild(this.shiftPreview);
canvas.style.visibility='visible';
this.shiftPreview=null;
}
}
else
{
if(this.shiftPreview==null)
{
this.shiftPreview=this.view.getDrawPane().cloneNode(false);
var tmp=mxUtils.getInnerHtml(this.view.getBackgroundPane());
tmp+=mxUtils.getInnerHtml(this.view.getDrawPane());
this.shiftPreview.innerHTML=tmp;
var pt=mxUtils.getOffset(this.container);
this.shiftPreview.style.position='absolute';
this.shiftPreview.style.left=pt.x+'px';
this.shiftPreview.style.top=pt.y+'px';
canvas.style.visibility='hidden';
this.container.appendChild(this.shiftPreview);
}
this.shiftPreview.style.left=dx+'px';
this.shiftPreview.style.top=dy+'px';
}
}
}
};
mxGraph.prototype.zoomIn=function()
{
var scale=this.view.scale*this.zoomFactor;
var state=this.view.getState(this.getSelectionCell());
if(this.keepSelectionVisibleOnZoom&&state!=null)
{
var rect=new mxRectangle(state.x*this.zoomFactor,state.y*this.zoomFactor,state.width*this.zoomFactor,state.height*this.zoomFactor);

this.view.scale=scale;
if(!this.scrollRectToVisible(rect))
{
this.view.setScale(scale);
}
}
else if(this.centerZoom&&!mxUtils.hasScrollbars(this.container))
{
var w=this.container.offsetWidth;
var h=this.container.offsetHeight;
var f=(this.zoomFactor-1)/(scale*2);
this.view.scaleAndTranslate(scale,this.view.translate.x-w*f,this.view.translate.y-h*f);
}
else
{
this.view.setScale(scale);
}
};
mxGraph.prototype.zoomOut=function()
{
var scale=this.view.scale/this.zoomFactor;
var state=this.view.getState(this.getSelectionCell());
if(this.keepSelectionVisibleOnZoom&&state!=null)
{
var rect=new mxRectangle(state.x/this.zoomFactor,state.y/this.zoomFactor,state.width/this.zoomFactor,state.height/this.zoomFactor);

this.view.scale=scale;
if(!this.scrollRectToVisible(rect))
{
this.view.setScale(scale);
}
}
else if(this.centerZoom&&!mxUtils.hasScrollbars(this.container))
{
var w=this.container.offsetWidth;
var h=this.container.offsetHeight;
var f=(this.zoomFactor-1)/(this.view.scale*2);
this.view.scaleAndTranslate(scale,this.view.translate.x+w*f,this.view.translate.y+h*f);
}
else
{
this.view.setScale(scale);
}
};
mxGraph.prototype.zoomActual=function()
{
this.view.translate.x=0;
this.view.translate.y=0;
this.view.setScale(1);
};
mxGraph.prototype.fit=function()
{
var border=10;
var w1=this.container.offsetWidth-30-2*border;
var h1=this.container.offsetHeight-30-2*border;
var bounds=this.view.bounds;
var w2=bounds.width/this.view.scale;
var h2=bounds.height/this.view.scale;
var s=Math.min(w1/w2,h1/h2);
if(s>0.1&&s<8)
{
this.view.translate.x=(bounds.x!=null)?this.view.translate.x-bounds.x/this.view.scale+border:
border;
this.view.translate.y=(bounds.y!=null)?this.view.translate.y-bounds.y/this.view.scale+border:
border;
this.view.setScale(s);
}
};
mxGraph.prototype.scrollCellToVisible=function(cell)
{
var x=-this.view.translate.x;
var y=-this.view.translate.y;
var state=this.view.getState(cell);
if(state!=null)
{
var bounds=new mxRectangle(x+state.x,y+state.y,state.width,state.height);
if(this.scrollRectToVisible(bounds))
{
this.view.setTranslate(this.view.translate.x,this.view.translate.y);
}
}
};
mxGraph.prototype.scrollRectToVisible=function(rect)
{
if(rect!=null)
{
var isChanged=false;
if(this.container.style.overflow=='auto')
{
}
else
{
var x=-this.view.translate.x;
var y=-this.view.translate.y;
var w=this.container.offsetWidth;
var h=this.container.offsetHeight;
var s=this.view.scale;
if(rect.x+rect.width>x+w)
{
this.view.translate.x-=(rect.x+rect.width-w-x)/s;
isChanged=true;
}
if(rect.y+rect.height>y+h)
{
this.view.translate.y-=(rect.y+rect.height-h-y)/s;
isChanged=true;
}
if(rect.x<x)
{
this.view.translate.x+=(x-rect.x)/s;
isChanged=true;
}
if(rect.y<y)
{
this.view.translate.y+=(y-rect.y)/s;
isChanged=true;
}
if(isChanged)
{
this.view.refresh();
}
}
}
return isChanged;
};
mxGraph.prototype.isCellVisible=function(cell)
{
return this.model.isVisible(cell);
};
mxGraph.prototype.isCellCollapsed=function(cell)
{
return this.model.isCollapsed(cell);
};
mxGraph.prototype.isOrthogonal=function(edge,vertex)
{
var edgeStyle=edge.style[mxConstants.STYLE_EDGE];
return edgeStyle==mxEdgeStyle.ElbowConnector||edgeStyle==mxEdgeStyle.SideToSide||edgeStyle==mxEdgeStyle.TopToBottom||edgeStyle==mxEdgeStyle.EntityRelation;
};
mxGraph.prototype.isLoop=function(state)
{
var src=this.view.getVisibleTerminal(state.cell,true);
var trg=this.view.getVisibleTerminal(state.cell,false);
return(src!=null&&src==trg);
};

mxGraph.prototype.collapse=function(cells,recurse)
{
this.setCollapsedState(cells,true,recurse);
};
mxGraph.prototype.expand=function(cells,recurse)
{
this.setCollapsedState(cells,false,recurse);
};
mxGraph.prototype.setCollapsedState=function(cells,collapsed,recurse)
{
cells=cells||this.getSelectionCells();
recurse=(recurse!=null)?recurse:false;
if(cells!=null&&cells.length>0)
{
this.editor.stopEditing(false);
var evtName=(collapsed)?'Collapse':'Expand';
this.dispatchEvent('before'+evtName,this,cells);
this.model.beginUpdate();
try
{
for(var i=0;i<cells.length;i++)
{
if((collapsed&&this.isCollapsable(cells[i])&&!this.isCellCollapsed(cells[i]))||(!collapsed&&this.isExpandable(cells[i])&&this.isCellCollapsed(cells[i])))
{
this.model.setCollapsed(cells[i],collapsed);
this.swapBounds(cells[i],collapsed);
if(this.isExtendParentOnResize(cells[i]))
{
this.extendParent(cells[i]);
}
this.cascadeResize(cells[i]);
if(recurse)
{
var children=this.model.getChildren(cells[i]);
this.setCollapsedState(children,collapsed,true);
}
}
}
this.dispatchEvent(evtName.toLowerCase(),this,cells);
this.layoutAfterCollapsedState(cells,collapsed);
}
finally
{
this.model.endUpdate();
}
this.dispatchEvent('after'+evtName,this,cells);
}
};
mxGraph.prototype.layoutAfterCollapsedState=function(cells,collapsed)
{
return this.layout(this.getParents(cells));
};
mxGraph.prototype.swapBounds=function(cell,willCollapse)
{
if(cell!=null)
{
var g=this.model.getGeometry(cell);
if(g!=null)
{
g=g.clone();
this.updateAlternateBounds(cell,g,willCollapse);
g.swap();
this.model.setGeometry(cell,g);
}
}
};
mxGraph.prototype.updateAlternateBounds=function(cell,g,willCollapse)
{
if(cell!=null&&g!=null)
{
if(g.alternateBounds==null)
{
var bounds=g;
if(this.collapseToPreferredSize)
{
var tmp=this.getPreferredSizeForCell(cell);
if(tmp!=null)
{
bounds=tmp;
var state=this.view.getState(cell);
var style=(state!=null)?state.style:
this.getCellStyle(cell);
var startSize=mxUtils.getValue(style,mxConstants.STYLE_STARTSIZE);
if(startSize>0)
{
bounds.height=Math.max(bounds.height,startSize);
}
}
}
g.alternateBounds=new mxRectangle(g.x,g.y,bounds.width,bounds.height);
}
else
{
g.alternateBounds.x=g.x;
g.alternateBounds.y=g.y;
}
}
};

mxGraph.prototype.getCellGeometry=function(cell)
{
return this.model.getGeometry(cell);
};
mxGraph.prototype.getCurrentRoot=function()
{
return this.view.currentRoot;
};
mxGraph.prototype.getTranslateForRoot=function(cell)
{
return null;
};
mxGraph.prototype.getChildOffsetForCell=function(cell)
{
return null;
};
mxGraph.prototype.enterGroup=function(cell)
{
cell=cell||this.getSelectionCell();
if(cell!=null&&this.isValidRoot(cell))
{
this.view.setCurrentRoot(cell);
this.clearSelection();
}
};
mxGraph.prototype.exitGroup=function()
{
var root=this.model.getRoot();
var current=this.getCurrentRoot();
if(current!=null)
{
var next=this.model.getParent(current);
while(next!=root&&!this.isValidRoot(next)&&this.model.getParent(next)!=root)
{
next=this.model.getParent(next);
}

if(next==root||this.model.getParent(next)==root)
{
this.view.setCurrentRoot(null);
}
else
{
this.view.setCurrentRoot(next);
}
var state=this.view.getState(current);
if(state!=null)
{
this.setSelectionCell(current);
}
}
};
mxGraph.prototype.home=function()
{
var current=this.getCurrentRoot();
if(current!=null)
{
this.view.setCurrentRoot(null);
var state=this.view.getState(current);
if(state!=null)
{
this.setSelectionCell(current);
}
}
};
mxGraph.prototype.isValidRoot=function(cell)
{
return(cell!=null);
};

mxGraph.prototype.group=function(group,border,tmp)
{
tmp=tmp||this.getSelectionCells();
if(tmp!=null&&tmp.length>1)
{
var parent=this.model.getParent(tmp[0]);
var cells=new Array();
cells.push(tmp[0]);
for(var i=1;i<tmp.length;i++)
{
if(this.model.getParent(tmp[i])==parent)
{
cells.push(tmp[i]);
}
}
if(cells.length>1)
{
group=group||this.createGroupCell(cells);
group=this.addGroup(group,cells,border);
if(group!=null)
{
this.setSelectionCell(group);
}
}
}
return group;
};
mxGraph.prototype.createGroupCell=function(cells)
{
var group=new mxCell('');
group.setVertex(true);
group.setConnectable(false);
return group;
};
mxGraph.prototype.ungroup=function(cells)
{
cells=cells||this.getSelectionCells();
if(cells!=null)
{
this.clearSelection();
this.model.beginUpdate();
try
{
for(var i=0;i<cells.length;i++)
{
var children=this.model.getChildren(cells[i]);
if(children!=null&&children.length>0)
{
children=children.slice();
this.moveInto(children,this.model.getParent(cells[i]));
this.selection.addCells(children);
this.remove([cells[i]]);
}
}
}
finally
{
this.model.endUpdate();
}
}
};
mxGraph.prototype.addGroup=function(group,cells,border)
{
border=(border!=null)?border:0;
var parent=this.model.getParent(cells[0]);
var pstate=this.view.getState(parent);
var bounds=this.view.getBounds(cells);
if(bounds!=null)
{
var scale=this.view.scale;
var translate=this.view.translate;
var x=bounds.x-pstate.origin.x*scale;
var y=bounds.y-pstate.origin.y*scale;
var width=bounds.width;
var height=bounds.height;
if(this.isSwimlane(group))
{
var size=this.getStartSize(group);
x-=size.width;
width+=size.width;
y-=size.height;
height+=size.height;
}
var geo=new mxGeometry(x/scale-border-translate.x,y/scale-border-translate.y,width/scale+2*border,height/scale+2*border);
this.model.beginUpdate();
try
{
group=this.groupCells(parent,group,cells,-geo.x,-geo.y);
if(group!=null)
{
this.model.setGeometry(group,geo);
}
}
finally
{
this.model.endUpdate();
}
return group;
}
return null;
};
mxGraph.prototype.groupCells=function(parent,group,cells,dx,dy)
{
this.model.beginUpdate();
try
{
var index=this.model.getChildCount(parent);
group=this.model.add(parent,group,index);
if(group!=null)
{
for(var i=0;i<cells.length;i++)
{
if(this.model.getParent(cells[i])!=group)
{
index=this.model.getChildCount(group);
this.model.add(group,cells[i],index);
}
var geometry=this.model.getGeometry(cells[i]);
if(geometry!=null)
{
geometry=geometry.translate(dx,dy);
this.model.setGeometry(cells[i],geometry);
}
}
}
}
finally
{
this.model.endUpdate();
}
return group;
};

mxGraph.prototype.getLayout=function(cell)
{
return null;
};
mxGraph.prototype.isBubbleLayout=function()
{
return this.bubbleLayout;
};
mxGraph.prototype.setBubbleLayout=function(bubbleLayout)
{
this.bubbleLayout=bubbleLayout;
};
mxGraph.prototype.isAutoLayout=function(cell)
{
return this.autoLayout;
};
mxGraph.prototype.getParents=function(cells)
{
var parents=new Array();
if(cells!=null)
{
var hash=new Array();
for(var i=0;i<cells.length;i++)
{
var parent=this.model.getParent(cells[i]);
if(parent!=null)
{
var id=mxCellPath.create(parent);
if(hash[id]==null)
{
hash[id]=parent;
parents.push(parent);
}
}
}
}
return parents;
};
mxGraph.prototype.layout=function(cells)
{
var result=new Array();
if(cells!=null&&cells.length>0)
{
this.dispatchEvent('beforeLayout',this,cells);
if(this.isBubbleLayout())
{
var tmp=cells;
while(tmp.length>0)
{
cells=cells.concat(tmp);
tmp=this.getParents(tmp);
}
}
var comparator=function(a,b)
{
var acp=mxCellPath.create(a);
var bcp=mxCellPath.create(b);
return(acp==bcp)?0:((acp<bcp)?1:-1);
};
cells.sort(comparator);
var last=null;
this.model.beginUpdate();
try
{
for(var i=0;i<cells.length;i++)
{
var cell=cells[i];
if(cell!=null&&cell!=last&&this.isAutoLayout(cell))
{
var layout=this.getLayout(cell);
if(layout!=null)
{
layout.execute(cell);
result.push(cell);
}
}
last=cell;
}
this.dispatchEvent('layout',this,result);
}
finally
{
this.model.endUpdate();
}
this.dispatchEvent('afterLayout',this,result);
}
return result;
};

mxGraph.prototype.validationAlert=function(message)
{
mxUtils.alert(message);
};
mxGraph.prototype.isEdgeValid=function(edge,source,target)
{
return this.getEdgeValidationError(edge,source,target)==null;
};
mxGraph.prototype.getEdgeValidationError=function(edge,source,target)
{
if(edge!=null&&this.model.getTerminal(edge,true)==null&&this.model.getTerminal(edge,false)==null)
{
return null;
}
if(!this.allowLoops&&source==target&&source!=null)
{
return '';
}
if(!this.isValidConnection(source,target))
{
return '';
}
if(source!=null&&target!=null)
{
var error='';

if(!this.multigraph)
{
var tmp=this.model.getEdgesBetween(source,target,true);
if(tmp.length>1||(tmp.length==1&&tmp[0]!=edge))
{
error+=(mxResources.get(this.alreadyConnectedResource)||this.alreadyConnectedResource)+'\n';
}
}


var sourceOut=this.model.getDirectedEdgeCount(source,true,edge);
var targetIn=this.model.getDirectedEdgeCount(target,false,edge);
for(var i=0;i<this.multiplicities.length;i++)
{
var err=this.multiplicities[i].check(this,edge,source,target,sourceOut,targetIn);
if(err!=null)
{
error+=err;
}
}
var err=this.validateEdge(edge,source,target);
if(err!=null)
{
error+=err;
}
return(error.length>0)?error:null;
}
return(this.allowDanglingEdges)?null:'';
};
mxGraph.prototype.validateEdge=function(edge,source,target)
{
return null;
};
mxGraph.prototype.validate=function(cell,context)
{
cell=(cell!=null)?cell:this.model.getRoot();
context=(context!=null)?context:new Object();
var isValid=true;
var childCount=this.model.getChildCount(cell);
for(var i=0;i<childCount;i++)
{
var tmp=this.model.getChildAt(cell,i);
var ctx=context;
if(this.isValidRoot(tmp))
{
ctx=new Object();
}
var warn=this.validate(tmp,ctx);
if(warn!=null)
{
var html=warn.replace(/\n/g,'<br>');
var len=html.length;
this.setWarning(tmp,html.substring(0,Math.max(0,len-4)));
}
else
{
this.setWarning(tmp,null);
}
isValid=isValid&&warn==null;
}
var warning='';
if(this.isCellCollapsed(cell)&&!isValid)
{
warning+=(mxResources.get(this.containsValidationErrorsResource)||this.containsValidationErrorsResource)+'\n';
}
if(this.model.isEdge(cell))
{
warning+=this.getEdgeValidationError(cell,this.model.getTerminal(cell,true),this.model.getTerminal(cell,false))||'';
}
else
{
warning+=this.getCellValidationError(cell)||'';
}
var err=this.validateCell(cell,context);
if(err!=null)
{
warning+=err;
}



if(this.model.getParent(cell)==null)
{
this.view.validate();
}
return(warning.length>0||!isValid)?warning:null;
};
mxGraph.prototype.getCellValidationError=function(cell)
{
var outCount=this.model.getDirectedEdgeCount(cell,true);
var inCount=this.model.getDirectedEdgeCount(cell,false);
var value=this.model.getValue(cell);
var error='';
for(var i=0;i<this.multiplicities.length;i++)
{
var rule=this.multiplicities[i];
if(rule.source&&mxUtils.isNode(value,rule.type,rule.attr,rule.value)&&((rule.max==0&&outCount>0)||(rule.min==1&&outCount==0)||(rule.max==1&&outCount>1)))
{
error+=rule.countError+'\n';
}
else if(!rule.source&&mxUtils.isNode(cell,type,rule.attr,rule.value)&&((rule.max==0&&inCount>0)||(rule.min==1&&inCount==0)||(rule.max==1&&inCount>1)))
{
error+=rule.countError+'\n';
}
}
return(error.length>0)?error:null;
};
mxGraph.prototype.validateCell=function(cell,context)
{
return null;
};

mxGraph.prototype.getBackgroundImage=function()
{
return this.backgroundImage;
};
mxGraph.prototype.setBackgroundImage=function(image)
{
this.backgroundImage=image;
};
mxGraph.prototype.getFoldingImage=function(state)
{
if(state!=null)
{
var tmp=this.isCellCollapsed(state.cell);
if((tmp&&this.isExpandable(state.cell))||(!tmp&&this.isCollapsable(state.cell)))
{
return(tmp)?this.collapsedImage:this.expandedImage;
}
}
return null;
};
mxGraph.prototype.convertValueToString=function(cell)
{
var value=this.model.getValue(cell);
if(value!=null)
{
if(mxUtils.isNode(value))
{
return value.nodeName;
}
else if(typeof(value.toString)=='function')
{
return value.toString();
}
}
return '';
};
mxGraph.prototype.getLabel=function(cell)
{
var result='';
var style=this.getCellStyle(cell);
if(cell!=null&&this.labelsVisible&&!mxUtils.getValue(style,mxConstants.STYLE_NOLABEL,false))
{
result=this.convertValueToString(cell);
}
return result;
};
mxGraph.prototype.isHtmlLabel=function(cell)
{
return this.htmlLabels;
};
mxGraph.prototype.isWrapping=function(cell)
{
return false;
};
mxGraph.prototype.isClipping=function(cell)
{
return false;
};
mxGraph.prototype.getTooltip=function(cell,index)
{
var tip=null;
if(index!=null)
{
if(parseInt(index)!=index)
{
tip=index.toString();
}
}
else
{
tip=this.getTooltipForCell(cell);
}
return tip;
};
mxGraph.prototype.getTooltipForCell=function(cell)
{
var tip=null;
if(cell.getTooltip!=null)
{
tip=cell.getTooltip();
}
else
{
tip=this.convertValueToString(cell);
}
return tip;
};
mxGraph.prototype.getCursorForCell=function(cell)
{
return null;
};
mxGraph.prototype.getStartSize=function(swimlane)
{
var result=new mxRectangle();
var style=this.getCellStyle(swimlane);
if(style!=null)
{
var size=parseInt(style[mxConstants.STYLE_STARTSIZE])||0;
if(mxUtils.getValue(style,mxConstants.STYLE_HORIZONTAL,true))
{
result.height=size;
}
else
{
result.width=size;
}
}
return result;
};
mxGraph.prototype.getImage=function(state)
{
return(state!=null&&state.style!=null)?state.style[mxConstants.STYLE_IMAGE]:null;
};
mxGraph.prototype.getVerticalAlign=function(state)
{
return(state!=null&&state.style!=null)?state.style[mxConstants.STYLE_VERTICAL_ALIGN]:
null;
};
mxGraph.prototype.getIndicatorColor=function(state)
{
return(state!=null&&state.style!=null)?state.style[mxConstants.STYLE_INDICATOR_COLOR]:null;
};
mxGraph.prototype.getIndicatorGradientColor=function(state)
{
return(state!=null&&state.style!=null)?state.style[mxConstants.STYLE_INDICATOR_GRADIENTCOLOR]:null;
};
mxGraph.prototype.getIndicatorShape=function(state)
{
return(state!=null&&state.style!=null)?state.style[mxConstants.STYLE_INDICATOR_SHAPE]:null;
};
mxGraph.prototype.getIndicatorImage=function(state)
{
return(state!=null&&state.style!=null)?state.style[mxConstants.STYLE_INDICATOR_IMAGE]:null;
};
mxGraph.prototype.getBorder=function()
{
return this.border;
};
mxGraph.prototype.setBorder=function(border)
{
this.border=border;
};
mxGraph.prototype.isSwimlane=function(cell)
{
if(cell!=null)
{
if(this.model.getParent(cell)!=
this.model.getRoot())
{
var state=this.view.getState(cell);
var style=(state!=null)?state.style:this.getCellStyle(cell);
if(style!=null&&!this.model.isEdge(cell))
{
return style[mxConstants.STYLE_SHAPE]==
mxConstants.SHAPE_SWIMLANE;
}
}
}
return false;
};

mxGraph.prototype.isResizeContainer=function()
{
return this.resizeContainer;
};
mxGraph.prototype.setResizeContainer=function(resizeContainer)
{
this.resizeContainer=resizeContainer;
};
mxGraph.prototype.isEnabled=function()
{
return this.enabled;
};
mxGraph.prototype.setEnabled=function(enabled)
{
this.enabled=enabled;
};
mxGraph.prototype.isLocked=function(cell)
{
var geometry=this.model.getGeometry(cell);
return this.locked||(geometry!=null&&this.model.isVertex(cell)&&geometry.relative);
};
mxGraph.prototype.setLocked=function(locked)
{
this.locked=locked;
};
mxGraph.prototype.isCellCloneable=function(cell)
{
return this.isCloneable();
};
mxGraph.prototype.isCloneable=function()
{
return this.cloneable;
}
mxGraph.prototype.setCloneable=function(cloneable)
{
this.cloneable=cloneable;
};
mxGraph.prototype.canExport=function(cell)
{
return this.exportEnabled;
};
mxGraph.prototype.canImport=function(cell)
{
return this.importEnabled;
};
mxGraph.prototype.isSelectable=function(cell)
{
return(cell!=null)?this.selectable:false;
};
mxGraph.prototype.isDeletable=function(cell)
{
return this.deletable;
};
mxGraph.prototype.setDeletable=function(deletable)
{
this.deletable=deletable;
};
mxGraph.prototype.isLabelMovable=function(cell)
{
return!this.isLocked(cell)&&((this.model.isEdge(cell)&&this.edgeLabelsMovable)||(this.model.isVertex(cell)&&this.vertexLabelsMovable));
};
mxGraph.prototype.isMovable=function(cell)
{
return this.movable&&!this.isLocked(cell);
};
mxGraph.prototype.setMovable=function(movable)
{
this.movable=movable;
};
mxGraph.prototype.isGridEnabled=function()
{
return this.gridEnabled;
};
mxGraph.prototype.setGridEnabled=function(gridEnabled)
{
this.gridEnabled=gridEnabled;
};
mxGraph.prototype.isSwimlaneNesting=function()
{
return this.swimlaneNesting;
};
mxGraph.prototype.setSwimlaneNesting=function(swimlaneNesting)
{
this.swimlaneNesting=swimlaneNesting;
};
mxGraph.prototype.isSwimlaneSelectionEnabled=function()
{
return this.swimlaneSelectionEnabled;
};
mxGraph.prototype.setSwimlaneSelectionEnabled=function(swimlaneSelectionEnabled)
{
this.swimlaneSelectionEnabled=swimlaneSelectionEnabled;
};
mxGraph.prototype.isMultigraph=function()
{
return this.multigraph;
};
mxGraph.prototype.setMultigraph=function(multigraph)
{
this.multigraph=multigraph;
};
mxGraph.prototype.isAllowLoops=function()
{
return this.allowLoops;
};
mxGraph.prototype.setAllowDanglingEdges=function(allowDanglingEdges)
{
this.allowDanglingEdges=allowDanglingEdges;
};
mxGraph.prototype.isAllowDanglingEdges=function()
{
return this.allowDanglingEdges;
};
mxGraph.prototype.setConnectableEdges=function(connectableEdges)
{
this.connectableEdges=connectableEdges;
};
mxGraph.prototype.isConnectableEdges=function()
{
return this.connectableEdges;
};
mxGraph.prototype.setCloneInvalidEdges=function(cloneInvalidEdges)
{
this.cloneInvalidEdges=cloneInvalidEdges;
};
mxGraph.prototype.isCloneInvalidEdges=function()
{
return this.cloneInvalidEdges;
};
mxGraph.prototype.setAllowLoops=function(allowLoops)
{
this.allowLoops=allowLoops;
};
mxGraph.prototype.isDisconnectOnMove=function()
{
return this.disconnectOnMove;
};
mxGraph.prototype.setDisconnectOnMove=function(disconnectOnMove)
{
this.disconnectOnMove=disconnectOnMove;
};
mxGraph.prototype.isDropEnabled=function()
{
return this.dropEnabled;
};
mxGraph.prototype.setDropEnabled=function(dropEnabled)
{
this.dropEnabled=dropEnabled;
};
mxGraph.prototype.isSizable=function(cell)
{
return this.sizable&&!this.isLocked(cell);
};
mxGraph.prototype.setSizable=function(sizable)
{
this.sizable=sizable;
};
mxGraph.prototype.isBendable=function(cell)
{
return this.bendable&&!this.isLocked(cell);
}
mxGraph.prototype.setBendable=function(bendable)
{
this.bendable=bendable;
};
mxGraph.prototype.isDisconnectable=function(cell,terminal,source)
{
return this.disconnectable&&!this.isLocked(cell);
}
mxGraph.prototype.setDisconnectable=function(disconnectable)
{
this.disconnectable=disconnectable;
};
mxGraph.prototype.isEditable=function(cell)
{
return this.editable&&!this.isLocked(cell);
};
mxGraph.prototype.setEditable=function(editable)
{
this.editable=editable;
};
mxGraph.prototype.isValidSource=function(cell)
{
return(cell==null&&this.allowDanglingEdges)||(cell!=null&&(!this.model.isEdge(cell)||this.connectableEdges)&&this.model.isConnectable(cell));
};
mxGraph.prototype.isValidTarget=function(cell)
{
return this.isValidSource(cell);
};
mxGraph.prototype.isValidConnection=function(source,target)
{
return this.isValidSource(source)&&this.isValidTarget(target);
};
mxGraph.prototype.setConnectable=function(connectable)
{
this.connectionHandler.setEnabled(connectable);
};
mxGraph.prototype.isConnectable=function(connectable)
{
return this.connectionHandler.isEnabled();
};
mxGraph.prototype.setTooltips=function(enabled)
{
this.tooltipHandler.setEnabled(enabled);
};
mxGraph.prototype.setPanning=function(enabled)
{
this.panningHandler.panningEnabled=enabled;
};
mxGraph.prototype.isEditing=function(cell)
{
return this.editor!=null&&this.editor.isEditing(cell);
};
mxGraph.prototype.setUpdateSize=function(updateSize)
{
this.autoSize=updateSize;
};
mxGraph.prototype.isUpdateSize=function(cell)
{
return this.autoSize;
};
mxGraph.prototype.isKeepInsideParentOnMove=function(cell)
{
return this.keepInsideParentOnMove;
};
mxGraph.prototype.getOverlap=function(cell)
{
return(this.isAllowOverlapParent(cell))?this.defaultOverlap:0;
};
mxGraph.prototype.isAllowOverlapParent=function(cell)
{
return false;
};
mxGraph.prototype.isShiftable=function(cell)
{
return!this.model.isEdge(cell);
};
mxGraph.prototype.isExpandable=function(cell)
{
return this.model.getChildCount(cell)>0;
};
mxGraph.prototype.isCollapsable=function(cell)
{
return this.isExpandable(cell);
};
mxGraph.prototype.isValidDropTarget=function(cell,cells,evt)
{
if(this.model.isEdge(cell))
{
return cells!=null&&cells.length==1&&this.isSplitDropTarget(cell,cells[0],evt);
}
else
{
return this.isParentDropTarget(cell,cells,evt);
}
};
mxGraph.prototype.isSplitDropTarget=function(edge,cell,evt)
{
if(edge!=null)
{
var src=this.model.getTerminal(edge,true);
var trg=this.model.getTerminal(edge,false);
return(!this.model.isAncestor(cell,src)&&!this.model.isAncestor(cell,trg));
}
return false;
};
mxGraph.prototype.isParentDropTarget=function(target,cells,evt)
{
if(target!=null)
{
return this.isSwimlane(target)||(this.model.getChildCount(target)>0&&!this.isCellCollapsed(target));
}
return false;
};
mxGraph.prototype.getDropTarget=function(cells,evt,cell)
{
if(!this.swimlaneNesting)
{
for(var i=0;i<cells.length;i++)
{
if(this.isSwimlane(cells[i]))
{
return null;
}
}
}
var pt=mxUtils.convertPoint(this.container,evt.clientX,evt.clientY);
var swimlane=this.getSwimlaneAt(pt.x,pt.y);
if(cell==null)
{
cell=swimlane;
}
else if(swimlane!=null)
{

var tmp=this.model.getParent(swimlane);
while(tmp!=null&&this.isSwimlane(tmp)&&tmp!=cell)
{
tmp=this.model.getParent(tmp);
}
if(tmp==cell)
{
cell=swimlane;
}
}
while(cell!=null&&!this.isValidDropTarget(cell,cells,evt)&&!this.model.isLayer(cell))
{
cell=this.model.getParent(cell);
}
return(!this.model.isLayer(cell))?cell:null;
};

mxGraph.prototype.getDefaultParent=function()
{
var parent=this.defaultParent;
if(parent==null)
{
parent=this.getCurrentRoot();
if(parent==null)
{
var root=this.model.getRoot();
parent=this.model.getChildAt(root,0);
}
}
return parent;
};
mxGraph.prototype.getSwimlane=function(cell)
{
while(cell!=null&&!this.isSwimlane(cell))
{
cell=this.model.getParent(cell);
}
return cell;
};
mxGraph.prototype.getSwimlaneAt=function(x,y,parent)
{
parent=parent||this.getDefaultParent();
if(parent!=null)
{
var childCount=this.model.getChildCount(parent);
for(var i=0;i<childCount;i++)
{
var child=this.model.getChildAt(parent,i);
var result=this.getSwimlaneAt(x,y,child);
if(result!=null)
{
return result;
}
else if(this.isSwimlane(child))
{
var state=this.view.getState(child);
if(this.isCellVisible(child)&&this.intersects(state,x,y))
{
return child;
}
}
}
}
return null;
};
mxGraph.prototype.getCellAt=function(x,y,parent,vertices,edges)
{
vertices=vertices||true;
edges=edges||true;
parent=parent||this.getDefaultParent();
if(parent!=null)
{
var childCount=this.model.getChildCount(parent);
for(var i=0;i<childCount;i++)
{
var cell=this.model.getChildAt(parent,i);
var result=this.getCellAt(x,y,cell,vertices,edges);
if(result!=null)
{
return result;
}
else
{
if(this.isCellVisible(cell)&&(edges&&this.model.isEdge(cell)||vertices&&this.model.isVertex(cell)))
{
var state=this.view.getState(cell);
if(this.intersects(state,x,y))
{
return cell;
}
}
}
}
}
return null;
};
mxGraph.prototype.intersects=function(state,x,y)
{
if(state!=null)
{
var pts=state.absolutePoints;
if(pts!=null)
{
var t2=this.tolerance*this.tolerance;
var pt=pts[0];
for(var i=1;i<pts.length;i++)
{
var next=pts[i];
var dist=mxUtils.ptSegDistSq(pt.x,pt.y,next.x,next.y,x,y);
if(dist<=t2)
{
return true;
}
pt=next;
}
}
else if(mxUtils.contains(state,x,y))
{
return true;
}
}
return false;
};
mxGraph.prototype.hitsSwimlaneContent=function(swimlane,x,y)
{
var state=this.getView().getState(swimlane);
var size=this.getStartSize(swimlane);
if(state!=null)
{
var scale=this.getView().getScale();
x-=state.x;
y-=state.y;
if(size.width>0&&x>0&&x>size.width*scale)
{
return true;
}
else if(size.height>0&&y>0&&y>size.height*scale)
{
return true;
}
}
return false;
};
mxGraph.prototype.getChildVertices=function(parent)
{
return this.getChildCells(parent,true,false);
}
mxGraph.prototype.getChildEdges=function(parent)
{
return this.getChildCells(parent,false,true);
}
mxGraph.prototype.getChildCells=function(parent,vertices,edges)
{
parent=(parent!=null)?parent:this.getDefaultParent();
vertices=(vertices!=null)?vertices:false;
edges=(edges!=null)?edges:false;
var cells=this.model.getChildCells(parent,vertices,edges);
var result=new Array();
for(var i=0;i<cells.length;i++)
{
if(this.isCellVisible(cells[i]))
{
result.push(cells[i]);
}
}
return result;
}
mxGraph.prototype.getConnections=function(cell,parent)
{
return this.getEdges(cell,parent,true,true,false);
}
mxGraph.prototype.getIncomingEdges=function(cell,parent)
{
return this.getEdges(cell,parent,true,false,false);
}
mxGraph.prototype.getOutgoingEdges=function(cell,parent)
{
return this.getEdges(cell,parent,false,true,false);
}
mxGraph.prototype.getEdges=function(cell,parent,incoming,outgoing,includeLoops)
{
incoming=(incoming!=null)?incoming:true;
outgoing=(outgoing!=null)?outgoing:true;
includeLoops=(includeLoops!=null)?includeLoops:true;
var edges=new Array();
var isCollapsed=this.isCellCollapsed(cell);
var childCount=this.model.getChildCount(cell);
for(var i=0;i<childCount;i++)
{
var child=this.model.getChildAt(cell,i);
if(isCollapsed||!this.isCellVisible(child))
{
edges=edges.concat(this.model.getEdges(child,incoming,outgoing));
}
}
edges=edges.concat(this.model.getEdges(cell,incoming,outgoing));
var result=new Array();
for(var i=0;i<edges.length;i++)
{
var source=this.view.getVisibleTerminal(edges[i],true);
var target=this.view.getVisibleTerminal(edges[i],false);
if(includeLoops||((source!=target)&&(incoming&&target==cell&&(parent==null||this.model.getParent(source)==parent))||(outgoing&&source==cell&&(parent==null||this.model.getParent(target)==parent))))
{
result.push(edges[i]);
}
}
return result;
}
mxGraph.prototype.getOpposites=function(edges,terminal,sources,targets)
{
sources=(sources!=null)?sources:true;
targets=(targets!=null)?targets:true;
var terminals=new Array();

var hash=new Object();
if(edges!=null)
{
for(var i=0;i<edges.length;i++)
{
var source=this.view.getVisibleTerminal(edges[i],true);
var target=this.view.getVisibleTerminal(edges[i],false);


if(source==terminal&&target!=null&&target!=terminal&&targets)
{
var id=mxCellPath.create(target);
if(hash[id]==null)
{
hash[id]=target;
terminals.push(target);
}
}


else if(target==terminal&&source!=null&&source!=terminal&&sources)
{
var id=mxCellPath.create(source);
if(hash[id]==null)
{
hash[id]=source;
terminals.push(source);
}
}
}
}
return terminals;
}
mxGraph.prototype.getEdgesBetween=function(source,target,directed)
{
var edges=this.getEdges(source);
var result=new Array();

for(var i=0;i<edges.length;i++)
{
var src=this.view.getVisibleTerminal(edges[i],true);
var trg=this.view.getVisibleTerminal(edges[i],false);
if(trg==target||(!directed&&src==target))
{
result.push(edges[i]);
}
}
return result;
}
mxGraph.prototype.getPointForEvent=function(evt)
{
var p=mxUtils.convertPoint(this.container,evt.clientX,evt.clientY);
var s=this.view.scale;
var tr=this.view.translate;
p.x=this.snap(p.x/s-tr.x-this.gridSize/2);
p.y=this.snap(p.y/s-tr.y-this.gridSize/2);
return p;
};
mxGraph.prototype.getCells=function(x,y,width,height,parent,result)
{
var result=result||new Array();
if(width>0||height>0)
{
var right=x+width;
var bottom=y+height;
parent=parent||this.getDefaultParent();
if(parent!=null)
{
var childCount=this.model.getChildCount(parent);
for(var i=0;i<childCount;i++)
{
var cell=this.model.getChildAt(parent,i);
var state=this.view.getState(cell);
if(this.isCellVisible(cell)&&state!=null)
{
if(state.x>=x&&state.y>=y&&state.x+state.width<=right&&state.y+state.height<=bottom)
{
result.push(cell);
}
else
{
this.getCells(x,y,width,height,cell,result);
}
}
}
}
}
return result;
};
mxGraph.prototype.getCellsBeyond=function(x0,y0,parent,rightHalfpane,bottomHalfpane)
{
var result=new Array();
if(rightHalfpane||bottomHalfpane)
{
if(parent==null)
{
parent=this.getDefaultParent();
}
if(parent!=null)
{
var childCount=this.model.getChildCount(parent);
for(var i=0;i<childCount;i++)
{
var child=this.model.getChildAt(parent,i);
var state=this.view.getState(child);
if(this.isCellVisible(child)&&state!=null)
{
if((!rightHalfpane||state.x>=x0)&&(!bottomHalfpane||state.y>=y0))
{
result.push(child);
}
}
}
}
}
return result;
};
mxGraph.prototype.findTreeRoots=function(parent,isolate,invert)
{
isolate=(isolate!=null)?isolate:false;
invert=(invert!=null)?invert:false;
var roots=new Array();
if(parent!=null)
{
var model=this.getModel();
var childCount=model.getChildCount(parent);
var best=null;
var maxDiff=0;
for(var i=0;i<childCount;i++)
{
var cell=model.getChildAt(parent,i);
if(this.model.isVertex(cell)&&this.isCellVisible(cell))
{
var conns=this.getConnections(cell,(isolate)?parent:null);
var fanOut=0;
var fanIn=0;
for(var j=0;j<conns.length;j++)
{
var src=this.view.getVisibleTerminal(conns[j],true);
if(src==cell)
{
fanOut++;
}
else
{
fanIn++;
}
}
if((invert&&fanOut==0&&fanIn>0)||(!invert&&fanIn==0&&fanOut>0))
{
roots.push(cell);
}
var diff=(invert)?fanIn-fanOut:fanOut-fanIn;
if(diff>maxDiff)
{
maxDiff=diff;
best=cell;
}
}
}
if(roots.length==0&&best!=null)
{
roots.push(best);
}
}
return roots;
};
mxGraph.prototype.traverse=function(vertex,directed,func,edge,visited)
{
if(func!=null&&vertex!=null)
{
directed=(directed!=null)?directed:true;
visited=visited||new Array();
var id=mxCellPath.create(vertex);
if(visited[id]==null)
{
visited[id]=vertex;
var result=func(vertex,edge);
if(result==null||result)
{
var edgeCount=this.model.getEdgeCount(vertex);
if(edgeCount>0)
{
for(var i=0;i<edgeCount;i++)
{
var e=this.model.getEdgeAt(vertex,i);
var isSource=this.model.getTerminal(e,true)==vertex;
if(!directed||isSource)
{
var next=this.model.getTerminal(e,!isSource);
this.traverse(next,directed,func,e,visited);
}
}
}
}
}
}
};

mxGraph.prototype.isCellSelected=function(cell)
{
return this.selection.isSelected(cell);
};
mxGraph.prototype.isSelectionEmpty=function()
{
return this.selection.cells.length==0;
};
mxGraph.prototype.clearSelection=function()
{
return this.selection.clear();
};
mxGraph.prototype.getSelectionCount=function()
{
return this.selection.cells.length;
};
mxGraph.prototype.getSelectionCell=function()
{
return this.selection.cells[0];
};
mxGraph.prototype.getSelectionCells=function()
{
return this.selection.cells.slice();
};
mxGraph.prototype.setSelectionCell=function(cell)
{
this.selection.setCell(cell);
};
mxGraph.prototype.setSelectionCells=function(cells)
{
this.selection.setCells(cells);
};
mxGraph.prototype.selectRegion=function(rect,evt)
{
var cells=this.getCells(rect.x,rect.y,rect.width,rect.height);
this.selectCellsForEvent(cells,evt);
return cells;
};
mxGraph.prototype.selectNext=function()
{
this.select(true);
}
mxGraph.prototype.selectPrevious=function()
{
this.select();
}
mxGraph.prototype.selectParent=function()
{
this.select(false,true);
}
mxGraph.prototype.selectChild=function()
{
this.select(false,false,true);
}
mxGraph.prototype.select=function(isNext,isParent,isChild)
{
var sel=this.selection;
var cell=(sel.cells.length>0)?sel.cells[0]:null;
if(sel.cells.length>1)
{
sel.clear();
}
var parent=(cell!=null)?this.model.getParent(cell):
this.getDefaultParent();
var childCount=this.model.getChildCount(parent);
if(cell==null&&childCount>0)
{
var child=this.model.getChildAt(parent,0);
this.setSelectionCell(child);
}
else if((cell==null||isParent)&&this.view.getState(parent)!=null&&this.model.getGeometry(parent)!=null)
{
if(this.getCurrentRoot()!=parent)
{
this.setSelectionCell(parent);
}
}
else if(cell!=null&&isChild)
{
var tmp=this.model.getChildCount(cell);
if(tmp>0)
{
var child=this.model.getChildAt(cell,0);
this.setSelectionCell(child);
}
}
else if(childCount>0)
{
var i=parent.getIndex(cell);
if(isNext)
{
i++;
var child=this.model.getChildAt(parent,i%childCount);
this.setSelectionCell(child);
}
else
{
i--;
var index=(i<0)?childCount-1:i;
var child=this.model.getChildAt(parent,index);
this.setSelectionCell(child);
}
}
};
mxGraph.prototype.selectAll=function(parent)
{
parent=parent||this.getDefaultParent();
var children=this.model.getChildren(parent);
if(children!=null)
{
this.setSelectionCells(children);
}
};
mxGraph.prototype.selectVertices=function(parent)
{
this.selectCells(true,false,parent);
};
mxGraph.prototype.selectEdges=function(parent)
{
this.selectCells(false,true,parent);
};
mxGraph.prototype.selectCells=function(vertices,edges,parent)
{
parent=parent||this.getDefaultParent();
var self=this;
var filter=function(cell)
{
return self.view.getState(cell)!=null&&self.model.getChildCount(cell)==0&&((self.model.isVertex(cell)&&vertices)||(self.model.isEdge(cell)&&edges));
}
var cells=this.model.getCells(filter,parent);
this.setSelectionCells(cells);
};
mxGraph.prototype.selectCellForEvent=function(cell,evt)
{
var isSelected=this.isCellSelected(cell);
if(mxEvent.isToggleEvent(evt))
{
if(isSelected)
{
this.selection.removeCell(cell);
}
else
{
this.selection.addCell(cell);
}
}
else if(!isSelected||this.getSelectionCount()!=1)
{
this.setSelectionCell(cell);
}
};
mxGraph.prototype.selectCellsForEvent=function(cells,evt)
{
if(mxEvent.isToggleEvent(evt))
{
this.selection.addCells(cells);
}
else
{
this.setSelectionCells(cells);
}
};
mxGraph.prototype.selectCellsForEdit=function(edit)
{
if(edit!=null&&edit.changes!=null)
{
var changes=edit.changes;
var cells=new Array();
for(var i=0;i<changes.length;i++)
{
var change=changes[i];
if(change.constructor!=mxRootChange)
{
var cell=null;
if(change.constructor==mxChildChange&&change.isAdded)
{
cell=change.child;
}
else if(change.cell!=null&&change.cell.constructor==mxCell)
{
cell=change.cell;
}
if(cell!=null&&mxUtils.indexOf(cells,cell)<0)
{
cells.push(cell);
}
}
}
var tmp=this.getModel().getTopmostCells(cells);
this.setSelectionCells(tmp);
}
};

mxGraph.prototype.createHandler=function(state)
{
if(this.model.isEdge(state.cell))
{
if(this.isLoop(state)||state.style[mxConstants.STYLE_EDGE]==mxEdgeStyle.ElbowConnector||state.style[mxConstants.STYLE_EDGE]==mxEdgeStyle.SideToSide||state.style[mxConstants.STYLE_EDGE]==mxEdgeStyle.TopToBottom)
{
state.handler=new mxElbowEdgeHandler(state);
}
else
{
state.handler=new mxEdgeHandler(state);
}
}
else
{
state.handler=new mxVertexHandler(state);
}
};
mxGraph.prototype.redrawHandler=function(state)
{
if(state!=null&&state.handler!=null)
{
state.handler.redraw();
}
};
mxGraph.prototype.hasHandler=function(state)
{
return state!=null&&state.handler!=null;
};
mxGraph.prototype.destroyHandler=function(state)
{
if(state!=null&&state.handler!=null)
{
state.handler.destroy();
state.handler=null;
}
};

mxGraph.prototype.addGraphListener=function(listener)
{
if(this.graphListeners==null)
{
this.graphListeners=new Array();
}
this.graphListeners.push(listener);
};
mxGraph.prototype.removeGraphListener=function(listener)
{
if(this.graphListeners!=null)
{
for(var i=0;i<this.graphListeners.length;i++)
{
if(this.graphListeners[i]==listener)
{
this.graphListeners.splice(i,1);
break;
}
}
}
};
mxGraph.prototype.dispatchGraphEvent=function(evtName,evt,cell,index)
{






if(!mxClient.IS_IE&&evtName=='mousedown'&&this.activeElement!=null)
{
this.activeElement.blur();
}
if(typeof(mxDatatransfer)!='undefined')
{
mxDatatransfer.consumeSourceFunction(this,evt,cell);
}




if(evtName=='mousedown')
{
this.isMouseDown=true;
}
if((evtName!='mouseup'||this.isMouseDown)&&evt.detail!=2)
{
if(evtName=='mouseup')
{
this.isMouseDown=false;
}
if(!this.isEditing()&&(mxClient.IS_OP||mxClient.IS_SF||mxClient.IS_GC||evt.target!=this.container))
{
if(this.gestureHandler!=null)
{


if(evtName=='mousedown')
{
this.gestureHandler.mouseDown(evt,cell,index);
}
else if(evtName=='mousemove')
{
this.gestureHandler.mouseMove(evt,cell,index);
}
else if(evtName=='mouseup')
{
this.gestureHandler.mouseUp(evt,cell,index);
this.gestureHandler=null;
}

if(mxClient.IS_IE&&document.selection.type!='None'&&evtName!='mousedown')
{
try
{
document.selection.empty();
}
catch(e)
{
}
}
}
else if(this.graphListeners!=null)
{
evt.returnValue=true;
for(var i=0;i<this.graphListeners.length&&!mxEvent.isConsumed(evt);i++)
{
if(evtName=='mousedown')
{
this.graphListeners[i].mouseDown(evt,cell,index);
if(mxEvent.isConsumed(evt))
{
this.gestureHandler=this.graphListeners[i];
break;
}
}
else if(evtName=='mousemove')
{
this.graphListeners[i].mouseMove(evt,cell,index);
}
else if(evtName=='mouseup')
{
this.graphListeners[i].mouseUp(evt,cell,index);
}
}
}
if(evtName=='mouseup')
{
this.click(evt,cell);
}
}
}
};
mxGraph.prototype.destroy=function()
{
if(!this.destroyed)
{
this.destroyed=true;
if(this.tooltipHandler!=null)
{
this.tooltipHandler.destroy();
}
if(this.panningHandler!=null)
{
this.panningHandler.destroy();
}
if(this.connectionHandler!=null)
{
this.connectionHandler.destroy();
}
if(this.graphHandler!=null)
{
this.graphHandler.destroy();
}
if(this.editor!=null)
{
this.editor.destroy();
}
if(this.view!=null)
{
this.view.destroy();
}
if(this.focusHandler!=null)
{
mxEvent.removeListener(document.body,'focus',this.focusHandler);
this.focusHandler=null;
}
if(this.blurHandler!=null)
{
mxEvent.removeListener(document.body,'blur',this.blurHandler);
this.blurHandler=null;
}
this.activeElement=null;
this.container=null;
}
};
}

{
function mxOverlay(image,tooltip,align,verticalAlign)
{
this.image=image;
this.tooltip=tooltip;
this.align=align;
this.verticalAlign=verticalAlign;
};
mxOverlay.prototype=new mxEventSource();
mxOverlay.prototype.constructor=mxOverlay;
mxOverlay.prototype.image=null;
mxOverlay.prototype.tooltip=null;
mxOverlay.prototype.align=null;
mxOverlay.prototype.verticalAlign=null;
mxOverlay.prototype.defaultOverlap=0.5;
mxOverlay.prototype.getBounds=function(state)
{
var isEdge=state.view.graph.getModel().isEdge(state.cell);
var s=state.view.scale;
var pt=null;
var w=this.image.width;
var h=this.image.height;
if(isEdge)
{
var pts=state.absolutePoints;
if(pts.length%2==1)
{
pt=pts[pts.length/2+1];
}
else
{
var idx=pts.length/2;
var p0=pts[idx-1];
var p1=pts[idx];
pt=new mxPoint(p0.x+(p1.x-p0.x)/2,p0.y+(p1.y-p0.y)/2);
}
}
else
{
pt=new mxPoint();
if(this.align==mxConstants.ALIGN_LEFT)
{
pt.x=state.x;
}
else if(this.align==mxConstants.ALIGN_CENTER)
{
pt.x=state.x+state.width/2;
}
else
{
pt.x=state.x+state.width;
}
if(this.verticalAlign==mxConstants.ALIGN_TOP)
{
pt.y=state.y;
}
else if(this.verticalAlign==mxConstants.ALIGN_MIDDLE)
{
pt.y=state.y+state.height/2;
}
else
{
pt.y=state.y+state.height;
}
}
return new mxRectangle(pt.x-w*this.defaultOverlap*s,pt.y-h*this.defaultOverlap*s,w*s,h*s);
};
mxOverlay.prototype.toString=function()
{
return this.tooltip;
};
}

{
function mxOutline(graph,container)
{
this.source=graph;
this.graph=new mxGraph(container,graph.getModel(),this.graphRenderHint);
if(mxClient.IS_SVG)
{
var node=this.graph.getView().getCanvas().parentNode;
node.setAttribute('shape-rendering','optimizeSpeed');
node.setAttribute('image-rendering','optimizeSpeed');
}
this.graph.setStylesheet(graph.getStylesheet());
this.graph.setEnabled(false);
this.graph.labelsVisible=false;
var self=this;
graph.getModel().addListener('change',function(sender,changes)
{
self.update();
});
this.graph.addGraphListener(this);
var self=this;
var funct=function(sender)
{
self.update();
};
this.source.getModel().addListener('change',funct);
var view=this.source.getView();
view.addListener('scale',funct);
view.addListener('translate',funct);
view.addListener('scaleAndTranslate',funct);
view.addListener('scale',funct);
view.addListener('down',funct);
view.addListener('up',funct);
graph.addListener('refresh',function(sender)
{
self.graph.setStylesheet(graph.getStylesheet());
self.graph.refresh();
});
this.bounds=new mxRectangle(0,0,0,0);
this.selectionBorder=new mxRectangleShape(this.bounds,null,mxConstants.OUTLINE_COLOR,mxConstants.OUTLINE_STROKEWIDTH);


this.selectionBorder.dialect=(this.graph.dialect!=mxConstants.DIALECT_SVG)?mxConstants.DIALECT_VML:mxConstants.DIALECT_SVG;
this.selectionBorder.init(this.graph.getView().getOverlayPane());
var s=3;
this.sizer=new mxRectangleShape(this.bounds,mxConstants.OUTLINE_HANDLE_FILLCOLOR,mxConstants.OUTLINE_HANDLE_STROKECOLOR);
this.sizer.dialect=this.graph.dialect;
this.sizer.init(this.graph.getView().getOverlayPane());
if(this.enabled)
{
this.sizer.node.style.cursor='pointer';
}
mxEvent.addListener(this.sizer.node,'mousedown',function(evt)
{
self.graph.dispatchGraphEvent('mousedown',evt,null,0);
});
this.selectionBorder.node.style.display=(this.showViewport)?'':'none';
this.sizer.node.style.display=this.selectionBorder.node.style.display;
this.refresh();
};
mxOutline.prototype.graphRenderHint='faster';
mxOutline.prototype.enabled=true;
mxOutline.prototype.showViewport=true;
mxOutline.prototype.isEnabled=function()
{
return this.enabled;
};
mxOutline.prototype.setEnabled=function(enabled)
{
this.enabled=enabled;
};
mxOutline.prototype.refresh=function(revalidate)
{
this.update();
this.graph.refresh();
};
mxOutline.prototype.update=function(revalidate)
{
var bounds=this.source.getBounds();
var mw=parseInt(this.source.container.clientWidth);
var mh=parseInt(this.source.container.clientHeight);
var c=this.graph.container;
var cw=parseInt(c.clientWidth);
var ch=parseInt(c.clientHeight);
if(cw>0||ch>0)
{
var w=Math.max(mw,bounds.width+Math.abs(bounds.x))+cw*0.1;
var h=Math.max(mh,bounds.height+Math.abs(bounds.y))+ch*0.1;
var scale=Math.min(cw/w,ch/h);
if(this.graph.getView().scale!=scale)
{
this.graph.getView().scale=scale
revalidate=true;
}
}
var navView=this.graph.getView();
if(navView.currentRoot!=this.source.getView().currentRoot)
{
navView.setCurrentRoot(this.source.getView().currentRoot);
}
var t=this.source.view.translate;
var tx=Math.max(0,t.x);
var ty=Math.max(0,t.y);
if(navView.translate.x!=tx||navView.translate.y!=ty)
{
navView.translate.x=tx;
navView.translate.y=ty;
revalidate=true;
}
var t2=navView.translate;
var scale=this.source.getView().scale;
var scale2=scale/navView.scale;
var scale3=1.0/navView.scale;
var container=this.source.container;
this.bounds=new mxRectangle((t2.x-t.x)/scale3,(t2.y-t.y)/scale3,(container.clientWidth/scale2),((container.clientHeight)/scale2));
this.selectionBorder.bounds=this.bounds;
this.selectionBorder.redraw();
var s=3;
this.sizer.bounds=new mxRectangle(this.bounds.x+this.bounds.width-s,this.bounds.y+this.bounds.height-s,2*s,2*s);
this.sizer.redraw();
if(revalidate)
{
this.graph.view.revalidate();
}
};
mxOutline.prototype.mouseDown=function(evt,cell,index)
{
if(this.enabled&&this.showViewport)
{
this.index=index;
this.startX=evt.clientX;
this.startY=evt.clientY;
this.active=true;
}
mxEvent.consume(evt);
};
mxOutline.prototype.mouseMove=function(evt,cell)
{
if(this.active)
{
this.selectionBorder.node.style.display=(this.showViewport)?'':'none';
this.sizer.node.style.display=this.selectionBorder.node.style.display;
var dx=evt.clientX-this.startX;
var dy=evt.clientY-this.startY;
var bounds=null;
if(this.index==null)
{
var scale=this.graph.getView().scale;
bounds=new mxRectangle(this.bounds.x+dx,this.bounds.y+dy,this.bounds.width,this.bounds.height);
this.selectionBorder.bounds=bounds;
this.selectionBorder.redraw();
dx/=scale;
dx*=this.source.getView().scale;
dy/=scale;
dy*=this.source.getView().scale;
this.source.shift(-dx,-dy);
}
else
{
var container=this.source.container;
var viewRatio=container.clientWidth/container.clientHeight;
dy=dx/viewRatio;
bounds=new mxRectangle(this.bounds.x,this.bounds.y,this.bounds.width+dx,this.bounds.height+dy);
this.selectionBorder.bounds=bounds;
this.selectionBorder.redraw();
}
var s=3;
this.sizer.bounds=new mxRectangle(bounds.x+bounds.width-s,bounds.y+bounds.height-s,2*s,2*s);
this.sizer.redraw();
mxEvent.consume(evt);
}
};
mxOutline.prototype.mouseUp=function(evt,cell)
{
if(this.active)
{
var dx=evt.clientX-this.startX;
var dy=evt.clientY-this.startY;
if(Math.abs(dx)>0||Math.abs(dy)>0)
{
if(this.index==null)
{
this.source.shift(0,0);
dx/=this.graph.getView().scale;
dy/=this.graph.getView().scale;
var t=this.source.getView().translate;
this.source.getView().setTranslate(t.x-dx,t.y-dy);
}
else
{
var w=this.selectionBorder.bounds.width;
var h=this.selectionBorder.bounds.height;
var scale=this.source.getView().scale;
this.source.getView().setScale(scale-(dx*scale)/w);
}
this.update();
mxEvent.consume(evt);
}
this.index=null;
this.active=false;
}
};
}

{
function mxMultiplicity(source,type,attr,value,min,max,validNeighbors,countError,typeError,validNeighborsAllowed)
{
this.source=source;
this.type=type;
this.attr=attr;
this.value=value;
this.min=(min!=null)?min:0;
this.max=(max!=null)?max:'n';
this.validNeighbors=validNeighbors;
this.countError=mxResources.get(countError)||countError;
this.typeError=mxResources.get(typeError)||typeError;
this.validNeighborsAllowed=(validNeighborsAllowed!=null)?validNeighborsAllowed:true;
};
mxMultiplicity.prototype.type=null;
mxMultiplicity.prototype.attr=null;
mxMultiplicity.prototype.value=null;
mxMultiplicity.prototype.source=null;
mxMultiplicity.prototype.min=null;
mxMultiplicity.prototype.max=null;
mxMultiplicity.prototype.validNeighbors=null;
mxMultiplicity.prototype.validNeighborsAllowed=true;
mxMultiplicity.prototype.countError=null;
mxMultiplicity.prototype.typeError=null;
mxMultiplicity.prototype.check=function(graph,edge,source,target,sourceOut,targetIn)
{
var error='';
var sourceValue=graph.model.getValue(source);
var targetValue=graph.model.getTarget(source);
if((sourceValue!=null&&targetValue!=null)&&((this.source&&mxUtils.isNode(sourceValue,this.type,this.attr,this.value))||(!this.source&&mxUtils.isNode(targetValue,this.type,this.attr,this.value))))
{
if((this.source&&(this.max==0||(sourceOut>=this.max)))||(!this.source&&(this.max==0||(targetIn>=this.max))))
{
error+=this.countError+'\n';
}
var valid=this.validNeighbors;
var isValid=!this.validNeighborsAllowed;
if(valid!=null&&valid.length>0)
{
for(var j=0;j<valid.length;j++)
{
if(this.source&&mxUtils.isNode(targetValue,valid[j]))
{
isValid=this.validNeighborsAllowed;
break;
}
else if(!this.source&&mxUtils.isNode(sourceValue,valid[j]))
{
isValid=this.validNeighborsAllowed;
break;
}
}
if(!isValid)
{
error+=this.typeError+'\n';
}
}
}
return(error.length>0)?error:null;
};
}

{
function mxGraphHandler(graph)
{
this.graph=graph;
if(document.body!=null)
{
this.graph.addGraphListener(this);
}
};
mxGraphHandler.prototype.graph=null;
mxGraphHandler.prototype.maxCells=(mxClient.IS_IE)?20:50;
mxGraphHandler.prototype.enabled=true;
mxGraphHandler.prototype.cloneEnabled=true;
mxGraphHandler.prototype.minimumSize=6;
mxGraphHandler.prototype.connectOnDrop=false;
mxGraphHandler.prototype.scrollOnMove=true;
mxGraphHandler.prototype.isEnabled=function()
{
return this.enabled;
};
mxGraphHandler.prototype.setEnabled=function(enabled)
{
this.enabled=enabled;
};
mxGraphHandler.prototype.mouseDown=function(evt,cell,index)
{
if(this.isEnabled()&&this.graph.isEnabled()&&!mxEvent.isForceMarqueeEvent(evt)&&index==null&&cell!=null)
{
this.cell=null;
this.delayedSelection=this.graph.isCellSelected(cell);
if(!this.delayedSelection)
{
this.graph.selectCellForEvent(cell,evt);
}
var model=this.graph.model;
var geo=model.getGeometry(cell);
if(this.graph.isMovable(cell)&&((!model.isEdge(cell)||this.graph.getSelectionCount()>1||(geo.points!=null&&geo.points.length>0)||model.getTerminal(cell,true)==null||model.getTerminal(cell,false)==null)||this.graph.allowDanglingEdges||(mxEvent.isCloneEvent(evt)&&this.graph.isCloneable())))
{
this.start(evt,cell,index);
}
this.cellWasClicked=true;
mxEvent.consume(evt);
}
};
mxGraphHandler.prototype.start=function(evt,cell,index)
{
this.startX=evt.clientX;
this.startY=evt.clientY;
var tmp=this.graph.getSelectionCells();
this.cells=new Array();
for(var i=0;i<tmp.length;i++)
{
if(this.graph.isMovable(tmp[i]))
{
this.cells.push(tmp[i]);
}
}
this.bounds=this.graph.getView().getBounds(this.cells);
if(this.bounds!=null)
{
if(this.bounds.width<this.minimumSize)
{
var dx=this.minimumSize-this.bounds.width;
this.bounds.x-=dx/2;
this.bounds.width=this.minimumSize;
}
if(this.bounds.height<this.minimumSize)
{
var dy=this.minimumSize-this.bounds.height;
this.bounds.y-=dy/2;
this.bounds.height=this.minimumSize;
}
this.shape=new mxRectangleShape(this.bounds,null,'black');


this.shape.dialect=(this.graph.dialect!=mxConstants.DIALECT_SVG)?mxConstants.DIALECT_VML:mxConstants.DIALECT_SVG;
this.shape.isDashed=true;
this.shape.init(this.graph.getView().getOverlayPane());
if(this.graph.dialect!=mxConstants.DIALECT_SVG)
{
var self=this;
mxEvent.addListener(this.shape.node,'mousemove',function(evt)
{
self.graph.dispatchGraphEvent('mousemove',evt,self.target||self.cell);
});
}
else
{
this.shape.node.setAttribute('style','pointer-events:none;');
}
this.shape.node.style.visibility='hidden';
this.highlight=new mxRectangleShape(new mxRectangle(0,0,0,0),null,mxConstants.DROP_TARGET_COLOR,'3');
this.highlight.dialect=this.shape.dialect;
this.highlight.init(this.graph.getView().getOverlayPane());
if(this.graph.dialect!=mxConstants.DIALECT_SVG)
{
var self=this;
mxEvent.addListener(this.highlight.node,'mousemove',function(evt)
{
self.graph.dispatchGraphEvent('mousemove',evt,self.target);
});
}
else
{
this.highlight.node.setAttribute('style','pointer-events:none;');
}
this.cell=cell;
this.highlight.node.style.visibility='hidden';
}
};
mxGraphHandler.prototype.mouseMove=function(evt,cell)
{
var graph=this.graph;
if(this.cell!=null)
{
if(this.shape!=null&&this.shape.node!=null)
{
var dx=evt.clientX-this.startX;
var dy=evt.clientY-this.startY;
var trx=graph.getView().translate;
var scale=graph.getView().scale;
if(mxEvent.isGridEnabledEvent(evt))
{
var tx=this.bounds.x-(this.graph.snap(this.bounds.x/scale-trx.x)+trx.x)*scale;
var ty=this.bounds.y-(this.graph.snap(this.bounds.y/scale-trx.y)+trx.y)*scale;
dx=this.graph.snap(dx/scale)*scale-tx;
dy=this.graph.snap(dy/scale)*scale-ty;
}
if(evt.shiftKey)
{
if(Math.abs(dx)>Math.abs(dy))
{
dy=0;
}
else
{
dx=0;
}
}
var bounds=new mxRectangle(this.bounds.x+dx,this.bounds.y+dy,this.bounds.width,this.bounds.height);
this.shape.bounds=bounds;
this.shape.node.style.visibility='visible';
this.shape.redraw();
var target=(graph.isDropEnabled())?graph.getDropTarget(this.cells,evt,cell):null;
var parent=target;
var model=graph.getModel();
while(parent!=null&&parent!=this.cell)
{
parent=model.getParent(parent);
}
var clone=mxEvent.isCloneEvent(evt)&&graph.isCloneable()&&this.cloneEnabled;
var state=graph.getView().getState(target);
if(!graph.isCellSelected(target)&&state!=null&&parent==null&&(model.getParent(this.cell)!=target||clone))
{
if(this.target!=target)
{
this.target=target;
this.setHighlightColor(mxConstants.DROP_TARGET_COLOR);
this.highlight.bounds=state;
this.highlight.node.style.visibility='visible';
this.highlight.redraw();
}
}
else
{
this.target=null;
if(this.connectOnDrop&&cell!=null&&this.cells.length==1&&graph.getModel().isVertex(cell)&&graph.getModel().isConnectable(cell))
{
var state=graph.getView().getState(cell);
if(state!=null)
{
var error=graph.getEdgeValidationError(null,this.cell,cell);
var color=(error==null)?mxConstants.VALID_COLOR:
mxConstants.INVALID_CONNECT_TARGET_COLOR;
this.setHighlightColor(color);
this.highlight.bounds=state;
this.highlight.node.style.visibility='visible';
this.highlight.redraw();
}
else
{
this.highlight.node.style.visibility='hidden';
}
}
else
{
this.highlight.node.style.visibility='hidden';
}
}
}
mxEvent.consume(evt);
}
};
mxGraphHandler.prototype.setHighlightColor=function(color)
{
if(this.highlight.dialect==mxConstants.DIALECT_SVG)
{
this.highlight.innerNode.setAttribute('stroke',color);
}
else
{
this.highlight.node.setAttribute('strokecolor',color);
}
};
mxGraphHandler.prototype.mouseUp=function(evt,cell)
{
var graph=this.graph;
if(this.cell!=null&&this.shape!=null&&(evt.clientX!=this.startX||evt.clientY!=this.startY))
{
var trx=graph.getView().translate;
var scale=graph.getView().scale;
var clone=mxEvent.isCloneEvent(evt)&&graph.isCloneable()&&this.cloneEnabled;
var dx=(evt.clientX-this.startX)/scale;
var dy=(evt.clientY-this.startY)/scale;
if(mxEvent.isGridEnabledEvent(evt))
{
var tx=this.bounds.x-(this.graph.snap(this.bounds.x/scale-trx.x)+trx.x)*scale;
var ty=this.bounds.y-(this.graph.snap(this.bounds.y/scale-trx.y)+trx.y)*scale;
dx=graph.snap(dx)-tx/scale;
dy=graph.snap(dy)-ty/scale;
}
if(evt.shiftKey)
{
if(Math.abs(dx)>Math.abs(dy))
{
dy=0;
}
else
{
dx=0;
}
}
if(this.connectOnDrop&&this.target==null&&cell!=null&&graph.getModel().isVertex(cell)&&graph.getModel().isConnectable(cell)&&graph.isEdgeValid(null,this.cell,cell))
{
this.graph.connectionHandler.connect(this.cell,cell,evt);
}
else
{
this.move(this.graph.getSelectionCells(),dx,dy,clone,this.target,evt);
}
}
else if(this.delayedSelection&&this.cell!=null)
{
this.graph.selectCellForEvent(this.cell,evt);
}
if(this.cellWasClicked)
{
mxEvent.consume(evt);
}
this.reset();
};
mxGraphHandler.prototype.reset=function()
{
if(this.shape!=null)
{
this.shape.destroy();
this.shape=null;
}
if(this.highlight!=null)
{
this.highlight.destroy();
this.highlight=null;
}
this.cellWasClicked=false;
this.delayedSelection=false;
this.cell=null;
this.target=null;
};
mxGraphHandler.prototype.move=function(cells,dx,dy,clone,target,evt)
{

var cells=this.graph.move(cells,dx,dy,clone,target,evt);
if(this.scrollOnMove)
{
this.graph.scrollCellToVisible(cells[0]);
}
if(clone)
{
this.graph.setSelectionCells(cells);
}
};
mxGraphHandler.prototype.destroy=function()
{
this.graph.removeGraphListener(this);
if(this.shape!=null)
{
this.shape.destroy();
this.shape=null;
}
if(this.highlight!=null)
{
this.highlight.destroy();
this.highlight=null;
}
};
}

{
function mxPanningHandler(graph,factoryMethod)
{
if(graph!=null&&document.body!=null)
{
this.graph=graph;
this.factoryMethod=factoryMethod;
this.graph.addGraphListener(this);
}
};
mxPanningHandler.prototype=new mxPopupMenu();
mxPanningHandler.prototype.constructor=mxPanningHandler;
mxPanningHandler.prototype.graph=null;
mxPanningHandler.prototype.usePopupTrigger=true;
mxPanningHandler.prototype.useLeftButtonForPanning=false;
mxPanningHandler.prototype.selectOnPopup=true;
mxPanningHandler.prototype.clearSelectionOnBackground=true;
mxPanningHandler.prototype.ignoreCell=false;
mxPanningHandler.prototype.useGrid=false;
mxPanningHandler.prototype.panningEnabled=true;
mxPanningHandler.prototype.init=function()
{
mxPopupMenu.prototype.init.apply(this);

var self=this;
mxEvent.addListener(this.div,'mousemove',function(evt)
{
self.graph.tooltipHandler.hide();
});
};
mxPanningHandler.prototype.isPanningTrigger=function(evt,cell)
{
return(this.useLeftButtonForPanning&&(this.ignoreCell||(cell==null))&&mxEvent.isLeftMouseButton(evt))||(this.useShiftKey&&evt.shiftKey)||(this.usePopupTrigger&&mxEvent.isPopupTrigger(evt));
};
mxPanningHandler.prototype.mouseDown=function(evt,cell)
{
if(this.isEnabled())
{
this.hideMenu();
this.dx0=-this.graph.container.scrollLeft;
this.dy0=-this.graph.container.scrollTop;
this.popupTrigger=this.isPopupTrigger(evt,cell);
this.panningTrigger=this.panningEnabled&&this.isPanningTrigger(evt,cell);
this.startX=evt.clientX;
this.startY=evt.clientY;

if((this.panningEnabled&&this.panningTrigger)||this.popupTrigger)
{
mxEvent.consume(evt);
}
}
};
mxPanningHandler.prototype.mouseMove=function(evt,cell)
{
var dx=evt.clientX-this.startX;
var dy=evt.clientY-this.startY;
if(this.active)
{
if(this.useGrid)
{
dx=this.graph.snap(dx);
dy=this.graph.snap(dy);
}
this.graph.shift(dx+this.dx0,dy+this.dy0);
mxEvent.consume(evt);
}
else if(this.panningTrigger)
{

this.active=Math.abs(dx)>this.graph.tolerance||Math.abs(dy)>this.graph.tolerance;
}
};
mxPanningHandler.prototype.mouseUp=function(evt,cell)
{
var dx=Math.abs(evt.clientX-this.startX);
var dy=Math.abs(evt.clientY-this.startY);
if(this.active)
{
var style=mxUtils.getCurrentStyle(this.graph.container);
if(!mxUtils.hasScrollbars(this.graph.container))
{
this.graph.shift(0,0);
var dx=evt.clientX-this.startX;
var dy=evt.clientY-this.startY;
var scale=this.graph.getView().scale;
var t=this.graph.getView().translate;
this.pan(t.x+dx/scale,t.y+dy/scale);
}
mxEvent.consume(evt);
}
else if(this.popupTrigger)
{
if(dx<this.graph.tolerance&&dy<this.graph.tolerance)
{
if(this.graph.isEnabled()&&this.selectOnPopup&&cell!=null&&!this.graph.isCellSelected(cell))
{
this.graph.setSelectionCell(cell);
}
if(this.clearSelectionOnBackground&&!this.graph.isCellSelected(cell))
{
this.graph.clearSelection();
}
this.graph.tooltipHandler.hide();
var origin=mxUtils.getScrollOrigin();
var point=new mxPoint(evt.clientX+origin.x,evt.clientY+origin.y);
this.popup(point.x,point.y,cell,evt);
}
mxEvent.consume(evt);
}
this.panningTrigger=false;
this.popupTrigger=false;
this.active=false;
};
mxPanningHandler.prototype.pan=function(dx,dy)
{
this.graph.getView().setTranslate(dx,dy);
};
mxPanningHandler.prototype.destroy=function()
{
this.graph.removeGraphListener(this);
mxPopupMenu.prototype.destroy.apply(this);
};
}

{
function mxCellMarker(graph,validColor,invalidColor,hotspot)
{
if(graph!=null)
{
this.graph=graph;
this.validColor=(validColor!=null)?validColor:mxConstants.DEFAULT_VALID_COLOR;
this.invalidColor=(validColor!=null)?invalidColor:mxConstants.DEFAULT_INVALID_COLOR;
this.hotspot=(hotspot!=null)?hotspot:mxConstants.DEFAULT_HOTSPOT;
var strokeWidth=mxConstants.HIGHLIGHT_STROKEWIDTH;
this.shape=new mxRectangleShape(new mxRectangle(),null,this.validColor,strokeWidth);
this.shape.dialect=(this.graph.dialect!=mxConstants.DIALECT_SVG)?mxConstants.DIALECT_VML:mxConstants.DIALECT_SVG;
this.shape.init(graph.getView().getOverlayPane());
this.shape.node.style.background='';
this.shape.node.style.display='none';
this.edgeShape=new mxPolyline([new mxPoint(),new mxPoint()],this.validColor,strokeWidth);
this.edgeShape.dialect=(this.graph.dialect!=mxConstants.DIALECT_SVG)?mxConstants.DIALECT_VML:mxConstants.DIALECT_SVG;
this.edgeShape.init(graph.getView().getOverlayPane());
this.edgeShape.node.style.display='none';
var self=this;
mxEvent.addListener(this.shape.node,'mousedown',function(evt)
{
var cell=(self.markedState!=null)?self.markedState.cell:null;
graph.dispatchGraphEvent('mousedown',evt,cell);
});
mxEvent.addListener(this.shape.node,'mousemove',function(evt)
{
var cell=(self.markedState!=null)?self.markedState.cell:null;
graph.dispatchGraphEvent('mousemove',evt,cell);
});
mxEvent.addListener(this.shape.node,'mouseup',function(evt)
{
var cell=(self.markedState!=null)?self.markedState.cell:null;
graph.dispatchGraphEvent('mouseup',evt,cell);
});
mxEvent.addListener(this.edgeShape.node,'mousedown',function(evt)
{
var cell=(self.markedState!=null)?self.markedState.cell:null;
graph.dispatchGraphEvent('mousedown',evt,cell);
});
mxEvent.addListener(this.edgeShape.node,'mousemove',function(evt)
{
var cell=(self.markedState!=null)?self.markedState.cell:null;
graph.dispatchGraphEvent('mousemove',evt,cell);
});
mxEvent.addListener(this.edgeShape.node,'mouseup',function(evt)
{
var cell=(self.markedState!=null)?self.markedState.cell:null;
graph.dispatchGraphEvent('mouseup',evt,cell);
});
this.resetHandler=function(sender)
{
self.reset();
};
this.graph.getView().addListener('scale',this.resetHandler);
this.graph.getView().addListener('translate',this.resetHandler);
this.graph.getView().addListener('scaleAndTranslate',this.resetHandler);
this.graph.getView().addListener('down',this.resetHandler);
this.graph.getView().addListener('up',this.resetHandler);
this.graph.getModel().addListener('change',this.resetHandler);
}
};
mxCellMarker.prototype=new mxEventSource();
mxCellMarker.prototype.constructor=mxCellMarker;
mxCellMarker.prototype.graph=null;
mxCellMarker.prototype.enabled=true;
mxCellMarker.prototype.hotspot=mxConstants.DEFAULT_HOTSPOT;
mxCellMarker.prototype.hotspotEnabled=false;
mxCellMarker.prototype.validColor=null;
mxCellMarker.prototype.invalidColor=null;
mxCellMarker.prototype.currentColor=null;
mxCellMarker.prototype.validState=null;
mxCellMarker.prototype.markedState=null;
mxCellMarker.prototype.resetHandler=null;
mxCellMarker.prototype.setEnabled=function(enabled)
{
this.enabled=enabled;
};
mxCellMarker.prototype.isEnabled=function()
{
return this.enabled;
};
mxCellMarker.prototype.setHotspot=function(hotspot)
{
this.hotspot=hotspot;
};
mxCellMarker.prototype.getHotspot=function()
{
return this.hotspot;
};
mxCellMarker.prototype.setHotspotEnabled=function(enabled)
{
this.hotspotEnabled=enabled;
};
mxCellMarker.prototype.isHotspotEnabled=function()
{
return this.hotspotEnabled;
};
mxCellMarker.prototype.hasValidState=function()
{
return this.validState!=null;
};
mxCellMarker.prototype.getValidState=function()
{
return this.validState;
};
mxCellMarker.prototype.getMarkedState=function()
{
return this.markedState;
};
mxCellMarker.prototype.reset=function()
{
this.validState=null;
if(this.markedState!=null)
{
this.markedState=null;
this.unmark();
}
};
mxCellMarker.prototype.process=function(evt,cell)
{
var state=null;
if(this.isEnabled())
{
state=this.getState(evt,cell);
var isValid=(state!=null)?this.isValidState(state):false;
var color=this.getMarkerColor(evt,state,isValid);
if(isValid)
{
this.validState=state;
}
else
{
this.validState=null;
}
if(state!=this.markedState||color!=this.currentColor)
{
this.currentColor=color;
if(state!=null&&this.currentColor!=null)
{
this.markedState=state;
this.mark();
}
else if(this.markedState!=null)
{
this.markedState=null;
this.unmark();
}
}
}
return state;
};
mxCellMarker.prototype.mark=function()
{
if(this.markedState!=null)
{
var shape=null;
if(this.graph.model.isEdge(this.markedState.cell))
{
shape=this.edgeShape;
shape.points=this.markedState.absolutePoints;
this.shape.node.style.display='none';
}
else
{
shape=this.shape;
shape.bounds=new mxRectangle(this.markedState.x-2,this.markedState.y-2,this.markedState.width+4,this.markedState.height+4);
this.edgeShape.node.style.display='none';
}
shape.node.style.display='inline';
shape.redraw();
if(shape.dialect==mxConstants.DIALECT_SVG)
{
shape.innerNode.setAttribute('stroke',this.currentColor);
}
else
{
shape.node.setAttribute('strokecolor',this.currentColor);
}
this.dispatchEvent('mark',this,this.markedState);
}
};
mxCellMarker.prototype.unmark=function()
{
this.edgeShape.node.style.display='none';
this.shape.node.style.display='none';
this.dispatchEvent('mark',this);
};
mxCellMarker.prototype.isValidState=function(state)
{
return true;
};
mxCellMarker.prototype.getMarkerColor=function(evt,state,isValid)
{
return(isValid)?this.validColor:this.invalidColor;
};
mxCellMarker.prototype.getState=function(evt,cell)
{
var view=this.graph.getView();
cell=this.getCell(evt,cell);
var state=this.getStateToMark(view.getState(cell));
return(state!=null&&this.intersects(state,evt))?state:null;
};
mxCellMarker.prototype.getCell=function(evt,cell)
{
return cell;
};
mxCellMarker.prototype.getStateToMark=function(state)
{
return state;
};
mxCellMarker.prototype.intersects=function(state,evt)
{
if(this.hotspotEnabled&&this.hotspot>0)
{
var cx=state.x+state.width/2;
var cy=state.y+state.height/2;
var w=state.width;
var h=state.height;
var start=mxUtils.getValue(state.style,mxConstants.STYLE_STARTSIZE);
if(start>0)
{
if(mxUtils.getValue(state.style,mxConstants.STYLE_HORIZONTAL,true))
{
cy=state.y+start/2;
h=start;
}
else
{
cx=state.x+start/2;
w=start;
}
}
var w=Math.max(mxConstants.MIN_HOTSPOT_SIZE,w*this.hotspot);
var h=Math.max(mxConstants.MIN_HOTSPOT_SIZE,h*this.hotspot);
if(mxConstants.MAX_HOTSPOT_SIZE>0)
{
w=Math.min(w,mxConstants.MAX_HOTSPOT_SIZE);
h=Math.min(h,mxConstants.MAX_HOTSPOT_SIZE);
}
var rect=new mxRectangle(cx-w/2,cy-h/2,w,h);
var point=mxUtils.convertPoint(this.graph.container,evt.clientX,evt.clientY);
return mxUtils.contains(rect,point.x,point.y);
}
return true;
};
mxCellMarker.prototype.destroy=function()
{
this.graph.getView().removeListener(this.resetHandler);
this.graph.getModel().removeListener(this.resetHandler);
if(this.shape!=null)
{
this.shape.destroy();
this.shape=null;
}
if(this.edgeShape!=null)
{
this.edgeShape.destroy();
this.edgeShape=null;
}
};
}

{
function mxConnectionHandler(graph,factoryMethod)
{
if(graph!=null)
{
this.graph=graph;
this.factoryMethod=factoryMethod;
this.init();
if(document.body!=null)
{
this.graph.addGraphListener(this);
}
}
};
mxConnectionHandler.prototype.graph=null;
mxConnectionHandler.prototype.factoryMethod=true;
mxConnectionHandler.prototype.connectImage=null;
mxConnectionHandler.prototype.enabled=true;
mxConnectionHandler.prototype.select=true;
mxConnectionHandler.prototype.createTarget=false;
mxConnectionHandler.prototype.marker=null;
mxConnectionHandler.prototype.error=null;
mxConnectionHandler.prototype.isEnabled=function()
{
return this.enabled;
};
mxConnectionHandler.prototype.setEnabled=function(enabled)
{
this.enabled=enabled;
};
mxConnectionHandler.prototype.init=function()
{
if(this.graph.container!=null)
{
this.marker=this.createMarker();
this.shape=new mxPolyline(new Array(),mxConstants.INVALID_COLOR);
this.shape.isDashed=true;
this.shape.dialect=(this.graph.dialect!=mxConstants.DIALECT_SVG)?mxConstants.DIALECT_VML:mxConstants.DIALECT_SVG;
this.shape.init(this.graph.getView().getOverlayPane());
this.shape.node.style.display='none';
if(this.graph.dialect!=mxConstants.DIALECT_SVG)
{
mxEvent.redirectMouseEvents(this.shape.node,this.graph,null,null,true);
}
else
{

this.shape.pipe.setAttribute('style','pointer-events:none;');
this.shape.innerNode.setAttribute('style','pointer-events:none;');
}
var self=this;
var changeHandler=function(sender)
{
if(self.iconState!=null)
{
self.iconState=self.graph.getView().getState(self.iconState.cell);
}
if(self.iconState!=null)
{
self.redrawIcons(self.icons);
}
else
{
self.destroyIcons(self.icons);
self.previous=null;
}
};
this.graph.getModel().addListener('change',changeHandler);
this.graph.getView().addListener('scale',changeHandler);
this.graph.getView().addListener('translate',changeHandler);
this.graph.getView().addListener('scaleAndTranslate',changeHandler);
var drillHandler=function(sender)
{
self.destroyIcons(self.icons);
};
this.graph.addListener('startEditing',drillHandler);
this.graph.getView().addListener('down',drillHandler);
this.graph.getView().addListener('up',drillHandler);
}
};
mxConnectionHandler.prototype.createMarker=function()
{
var marker=new mxCellMarker(this.graph);
marker.hotspotEnabled=true;
var self=this;


marker.getCell=function(evt,cell)
{
var cell=mxCellMarker.prototype.getCell.apply(this,arguments);
self.error=null;
if(cell!=null)
{
if(self.isConnecting())
{
if(self.previous!=null)
{
self.error=self.validateConnection(self.previous.cell,cell);
if(self.error!=null&&self.error.length==0)
{
cell=null;
if(self.createTarget)
{
self.error=null;
}
}
}
}
else if(!self.isValidSource(cell))
{
cell=null;
}
}
else if(self.isConnecting()&&!self.createTarget&&!self.graph.allowDanglingEdges)
{
self.error='';
}
return cell;
};
marker.isValidState=function(state)
{
if(self.isConnecting())
{
return self.error==null;
}
else
{
return mxCellMarker.prototype.isValidState.apply(this,arguments);
}
};

marker.getMarkerColor=function(evt,state,isValid)
{
return(self.connectImage==null||self.isConnecting())?mxCellMarker.prototype.getMarkerColor.apply(this,arguments):
null;
};

marker.intersects=function(state,evt)
{
if(self.connectImage!=null||self.isConnecting())
{
return true;
}
return mxCellMarker.prototype.intersects.apply(this,arguments);
};
return marker;
};
mxConnectionHandler.prototype.isConnecting=function()
{
return this.start!=null&&this.shape.node.style.display=='inline';
};
mxConnectionHandler.prototype.isValidSource=function(cell)
{
return this.graph.isValidSource(cell);
};
mxConnectionHandler.prototype.isValidTarget=function(cell)
{
return true;
};
mxConnectionHandler.prototype.validateConnection=function(source,target)
{
if(!this.isValidTarget(target))
{
return '';
}
return this.graph.getEdgeValidationError(null,source,target);
};
mxConnectionHandler.prototype.getConnectImage=function(state)
{
return this.connectImage;
};
mxConnectionHandler.prototype.createIcons=function(state)
{
var image=this.getConnectImage(state);
if(image!=null&&state!=null)
{
this.iconState=state;
var icons=new Array();
var bounds=new mxRectangle(0,0,image.width,image.height);
var icon=new mxImageShape(bounds,image.src);
icon.dialect=(this.graph.dialect==mxConstants.DIALECT_SVG)?mxConstants.DIALECT_STRICTHTML:
mxConstants.DIALECT_VML;
icon.init((this.graph.dialect==mxConstants.DIALECT_SVG)?this.graph.container:
this.graph.getView().getOverlayPane());
icon.node.style.cursor=(mxClient.IS_IE)?'all-scroll':'pointer';
var self=this;
mxEvent.addListener(icon.node,'dblclick',function(evt)
{
self.graph.dblClick(evt,state.cell);
mxEvent.consume(evt);
});
mxEvent.addListener(icon.node,'mousedown',function(evt)
{
self.icon=icon;
self.graph.dispatchGraphEvent('mousedown',evt,state.cell);
});
mxEvent.addListener(icon.node,'mousemove',function(evt)
{
self.graph.dispatchGraphEvent('mousemove',evt,state.cell);
});
mxEvent.addListener(icon.node,'mouseup',function(evt)
{
self.graph.dispatchGraphEvent('mouseup',evt,state.cell);
});
icons.push(icon);
this.redrawIcons(icons);
return icons;
}
return null;
};
mxConnectionHandler.prototype.redrawIcons=function(icons)
{
if(icons!=null&&this.iconState!=null)
{
var image=this.getConnectImage(this.iconState);
if(image!=null)
{
var scale=this.graph.getView().scale;
var cx=this.iconState.getCenterX();
var cy=this.iconState.getCenterY();
if(this.graph.isSwimlane(this.iconState.cell))
{
var size=this.graph.getStartSize(this.iconState.cell);
cx=(size.width!=0)?this.iconState.x+size.width*scale/2:cx;
cy=(size.height!=0)?this.iconState.y+size.height*scale/2:cy;
}
var icon=icons[0];
icon.bounds.x=cx-icon.bounds.width/2;
icon.bounds.y=cy-icon.bounds.height/2;
icon.redraw();
}
}
};
mxConnectionHandler.prototype.destroyIcons=function(icons)
{
if(icons!=null)
{
this.iconState=null;
for(var i=0;i<icons.length;i++)
{
icons[i].destroy();
}
}
};
mxConnectionHandler.prototype.mouseDown=function(evt,cell,index)
{
if(this.isEnabled()&&this.graph.isEnabled()&&!mxEvent.isForceMarqueeEvent(evt)&&this.previous!=null&&this.error==null&&((this.icons==null)||(this.icons!=null&&this.icon!=null))&&index==null)
{
this.start=mxUtils.convertPoint(this.graph.container,evt.clientX,evt.clientY);
mxEvent.consume(evt);
}
this.selectedIcon=this.icon;
this.icon=null;
};
mxConnectionHandler.prototype.mouseMove=function(evt,cell,index)
{
if(this.graph.isEnabled()&&this.isEnabled()&&index==null)
{
var state=this.marker.process(evt,cell);
if(this.previous!=null&&this.start!=null)
{
if(this.selectedIcon!=null)
{
var w=this.selectedIcon.bounds.width;
var h=this.selectedIcon.bounds.height;
var pt=mxUtils.convertPoint(this.graph.container,evt.clientX,evt.clientY);
var bounds=new mxRectangle(pt.x,pt.y+mxConstants.TOOLTIP_VERTICAL_OFFSET,w,h);
this.selectedIcon.bounds=bounds;
this.selectedIcon.redraw();
}
var view=this.graph.getView();
var point=mxUtils.convertPoint(this.graph.container,evt.clientX,evt.clientY);
var scale=view.scale;
point.x=this.graph.snap(point.x/scale)*scale;
point.y=this.graph.snap(point.y/scale)*scale;
var current=point;
if(state!=null)
{
var targetPerimeter=view.getPerimeterFunction(state);
if(targetPerimeter!=null)
{
var next=new mxPoint(this.previous.getCenterX(),this.previous.getCenterY());
var tmp=targetPerimeter(view.getPerimeterBounds(state,null,false),null,state,false,next);
if(tmp!=null)
{
current=tmp;
}
}
else
{
current=new mxPoint(state.getCenterX(),state.getCenterY());
}
}
var pt=this.start;
var sourcePerimeter=view.getPerimeterFunction(this.previous);
if(sourcePerimeter!=null)
{
var tmp=sourcePerimeter(view.getPerimeterBounds(this.previous,null,true),null,this.previous,true,current);
if(tmp!=null)
{
pt=tmp;
}
}
else
{
pt=new mxPoint(this.previous.getCenterX(),this.previous.getCenterY());
}
if(state==null)
{



var dx=current.x-pt.x;
var dy=current.y-pt.y;
var len=Math.sqrt(dx*dx+dy*dy);
current.x-=dx*4/len;
current.y-=dy*4/len;
}
this.shape.points=[pt,current];
if(this.shape.node.style.display!='inline')
{
var dx=Math.abs(point.x-this.start.x);
var dy=Math.abs(point.y-this.start.y);
if(dx>this.graph.tolerance||dy>this.graph.tolerance)
{
this.shape.node.style.display='inline';
}
}
this.drawPreview();
mxEvent.consume(evt);
}
else if(this.previous!=state)
{
if(this.previous!=null&&this.previous.shape!=null)
{
this.previous.shape.node.style.cursor=
this.previousCursor;
}
this.destroyIcons(this.icons);
this.icons=null;
if(state!=null&&this.error==null)
{
this.previousCursor=state.shape.node.style.cursor;
state.shape.node.style.cursor=(mxClient.IS_IE)?'all-scroll':'default';
this.icons=this.createIcons(state);
}
this.previous=state;
}
}
};
mxConnectionHandler.prototype.mouseUp=function(evt,cell)
{
if(this.isConnecting())
{
if(this.error==null)
{
var source=this.previous.cell;
var target=this.marker.hasValidState()?this.marker.validState.cell:null;
this.connect(source,target,evt,cell);
}
else
{
if(this.previous!=null&&this.marker.validState!=null&&this.previous.cell==this.marker.validState.cell)
{
this.graph.selectCellForEvent(this.marker.source,evt);
}

if(this.error.length>0)
{
this.graph.validationAlert(this.error);
}
}
this.destroyIcons(this.icons);
mxEvent.consume(evt);
}
else if(this.isEnabled()&&this.graph.isEnabled())
{
this.graph.selectCellForEvent(cell,evt);
}
this.reset();
};
mxConnectionHandler.prototype.reset=function()
{
this.shape.node.style.display='none';
this.marker.reset();
this.selectedIcon=null;
this.previous=null;
this.error=null;
this.start=null;
this.icon=null;
};
mxConnectionHandler.prototype.drawPreview=function()
{
var valid=this.error==null;
var color=this.getEdgeColor(valid);
if(this.shape.dialect==mxConstants.DIALECT_SVG)
{
this.shape.innerNode.setAttribute('stroke',color);
}
else
{
this.shape.node.setAttribute('strokecolor',color);
}
this.shape.strokewidth=this.getEdgeWidth(valid);
this.shape.redraw();
};
mxConnectionHandler.prototype.getEdgeColor=function(valid)
{
return(valid)?mxConstants.VALID_COLOR:mxConstants.INVALID_COLOR;
};
mxConnectionHandler.prototype.getEdgeWidth=function(valid)
{
return(valid)?3:1;
};
mxConnectionHandler.prototype.connect=function(source,target,evt,dropTarget)
{
if(source!=null&&(target!=null||this.createTarget||this.graph.allowDanglingEdges))
{

var model=this.graph.getModel();
var edge=null;
model.beginUpdate();
try
{
if(target==null&&this.createTarget)
{
target=this.createTargetVertex(evt,source);
if(target!=null)
{
dropTarget=this.graph.getDropTarget([target],evt,dropTarget);
if(dropTarget==null||!this.graph.getModel().isEdge(dropTarget))
{
var pstate=this.graph.getView().getState(dropTarget);
if(pstate!=null)
{
var tmp=model.getGeometry(target);
tmp.x-=pstate.origin.x;
tmp.y-=pstate.origin.y;
}
}
else
{
dropTarget=this.graph.getDefaultParent();
}
this.graph.addCell(target,dropTarget);
}
}
var parent=this.graph.getDefaultParent();
if(model.getParent(source)==
model.getParent(target))
{
parent=model.getParent(source);
}
edge=this.insertEdge(parent,null,null,source,target);
var geo=model.getGeometry(edge);
if(geo==null)
{
geo=new mxGeometry();
geo.relative=true;
model.setGeometry(edge,geo);
}
if(target==null)
{
var pt=this.graph.getPointForEvent(evt);
geo.setTerminalPoint(pt,false);
}
}
finally
{
model.endUpdate();
}
if(this.select)
{
this.graph.setSelectionCell(edge);
}
}
};
mxConnectionHandler.prototype.insertEdge=function(parent,id,value,source,target)
{
if(this.factoryMethod==null)
{
return this.graph.insertEdge(parent,id,value,source,target);
}
else
{
var edge=this.createEdge(source,target);
edge=this.graph.addEdge(edge,parent,source,target);
return edge;
}
};
mxConnectionHandler.prototype.createTargetVertex=function(evt,source)
{
var clone=this.graph.cloneCells([source])[0];
var model=this.graph.getModel();
var geo=model.getGeometry(clone);
if(geo!=null)
{
var point=this.graph.getPointForEvent(evt);
geo.x=this.graph.snap(point.x-geo.width/2);
geo.y=this.graph.snap(point.y-geo.height/2);
}
return clone;
};
mxConnectionHandler.prototype.createEdge=function(source,target)
{
var edge=null;
if(this.factoryMethod!=null)
{
edge=this.factoryMethod(source,target);
}
else
{
edge=new mxCell('');
edge.setEdge(true);
}
return edge;
};
mxConnectionHandler.prototype.destroy=function()
{
this.graph.removeGraphListener(this);
this.shape.destroy();
this.shape=null;
this.marker.destroy();
this.marker=null;
};
}

{
function mxRubberband(graph)
{
if(graph!=null)
{
this.graph=graph;
this.graph.addGraphListener(this);
this.div=document.createElement('div');
this.div.className='mxRubberband';
mxUtils.setOpacity(this.div,this.defaultOpacity);

mxEvent.redirectMouseEvents(this.div,this.graph);
if(mxClient.IS_IE)
{
var self=this;
mxEvent.addListener(window,'unload',function()
{
self.destroy();
});
}
}
};
mxRubberband.prototype.defaultOpacity=20;
mxRubberband.prototype.enabled=true;
mxRubberband.prototype.isEnabled=function()
{
return this.enabled;
};
mxRubberband.prototype.setEnabled=function(enabled)
{
this.enabled=enabled;
};
mxRubberband.prototype.mouseDown=function(evt,cell,index)
{
if(this.graph.isEnabled()&&this.isEnabled()&&cell==null&&index==null)
{
var offset=mxUtils.getOffset(this.graph.container);
var origin=mxUtils.getScrollOrigin(this.graph.container);
origin.x-=offset.x;
origin.y-=offset.y;
this.startX=evt.clientX+origin.x;
this.startY=evt.clientY+origin.y;
this.redraw(evt);
this.div.style.visibility='visible';
this.graph.container.appendChild(this.div);
this.active=true;
mxEvent.consume(evt);
}
else
{
this.active=false;
}
};
mxRubberband.prototype.mouseMove=function(evt)
{
if(this.active)
{
this.redraw(evt);
mxEvent.consume(evt);
}
};
mxRubberband.prototype.mouseUp=function(evt)
{
if(this.active)
{
this.reset();
var rect=new mxRectangle(this.x,this.y,this.width,this.height);
if(rect.width>this.graph.tolerance||rect.height>this.graph.tolerance)
{
this.graph.selectRegion(rect,evt);
mxEvent.consume(evt);
}
}
};
mxRubberband.prototype.reset=function(evt)
{
if(mxClient.FADE_RUBBERBAND)
{
mxUtils.fadeOut(this.div,30,true,10,null,mxClient.FADE_RUBBERBAND);
}
else
{
this.div.parentNode.removeChild(this.div);
}
this.active=false;
};
mxRubberband.prototype.redraw=function(evt)
{
var origin=mxUtils.getScrollOrigin(this.graph.container);
var offset=mxUtils.getOffset(this.graph.container);
origin.x-=offset.x;
origin.y-=offset.y;
var x=evt.clientX+origin.x;
var y=evt.clientY+origin.y;
this.x=Math.min(this.startX,x);
this.y=Math.min(this.startY,y);
this.width=Math.max(this.startX,x)-this.x;
this.height=Math.max(this.startY,y)-this.y;
this.div.style.left=this.x+'px';
this.div.style.top=this.y+'px';
this.div.style.width=Math.max(1,this.width)+'px';
this.div.style.height=Math.max(1,this.height)+'px';
};
mxRubberband.prototype.destroy=function()
{
if(!this.destroyed)
{
this.destroyed=true;
this.graph.removeGraphListener(this);
mxUtils.release(this.div);
if(this.div.parentNode!=null)
{
this.div.parentNode.removeChild(this.div);
}
this.div=null;
}
};
}

{
function mxVertexHandler(state)
{
if(state!=null)
{
this.state=state;
this.graph=state.view.graph;
this.graph.addGraphListener(this);
this.init();
this.redraw();
}
};
mxVertexHandler.prototype.graph=null;
mxVertexHandler.prototype.state=null;
mxVertexHandler.prototype.LABEL_INDEX=-1;
mxVertexHandler.prototype.singleSizer=false;
mxVertexHandler.prototype.init=function()
{
this.bounds=new mxRectangle(this.state.x,this.state.y,this.state.width,this.state.height);
this.selectionBorder=new mxRectangleShape(this.bounds,null,mxConstants.SELECTION_COLOR);
this.selectionBorder.strokewidth=mxConstants.SELECTION_STROKEWIDTH;
this.selectionBorder.isDashed=mxConstants.SELECTION_DASHED;


this.selectionBorder.dialect=(this.graph.dialect!=mxConstants.DIALECT_SVG)?mxConstants.DIALECT_VML:mxConstants.DIALECT_SVG;
this.selectionBorder.init(this.graph.getView().getOverlayPane());
if(this.graph.dialect!=mxConstants.DIALECT_SVG)
{
if(this.graph.isMovable(this.state.cell))
{
this.selectionBorder.node.style.cursor='move';
}
mxEvent.redirectMouseEvents(this.selectionBorder.node,this.graph,this.state.cell,null,true);
}
else
{
this.selectionBorder.node.setAttribute('style','pointer-events:none;');
}
if(mxGraphHandler.prototype.maxCells<=0||this.graph.getSelectionCount()<mxGraphHandler.prototype.maxCells)
{
this.sizers=new Array();
if(this.graph.isSizable(this.state.cell))
{
var i=0;
if(!this.singleSizer)
{
this.sizers.push(this.createSizer('nw-resize',i++));
this.sizers.push(this.createSizer('n-resize',i++));
this.sizers.push(this.createSizer('ne-resize',i++));
this.sizers.push(this.createSizer('w-resize',i++));
this.sizers.push(this.createSizer('e-resize',i++));
this.sizers.push(this.createSizer('sw-resize',i++));
this.sizers.push(this.createSizer('s-resize',i++));
}
this.sizers.push(this.createSizer('se-resize',i++));
var geo=this.graph.model.getGeometry(this.state.cell);
if(!geo.relative&&!this.graph.isSwimlane(this.state.cell)&&this.graph.isLabelMovable(this.state.cell))
{
this.sizers.push(this.createSizer('default',this.LABEL_INDEX,(this.graph.dialect==mxConstants.DIALECT_SVG)?4:6,mxConstants.LABEL_HANDLE_FILLCOLOR));
}
}
else if(this.graph.isMovable(this.state.cell)&&!this.graph.isSizable(this.state.cell)&&this.state.width<2&&this.state.height<2)
{
this.sizers.push(this.createSizer('move',null,null,mxConstants.LABEL_HANDLE_FILLCOLOR));
}
}
};
mxVertexHandler.prototype.createSizer=function(cursor,index,size,color)
{
size=size||((this.graph.dialect==mxConstants.DIALECT_SVG)?5:7);
var bounds=new mxRectangle(0,0,size,size);
var sizer=new mxRectangleShape(bounds,color||mxConstants.HANDLE_FILLCOLOR,mxConstants.HANDLE_STROKECOLOR);
if(this.graph.dialect==mxConstants.DIALECT_SVG)
{
sizer.dialect=mxConstants.DIALECT_PREFERHTML;
sizer.init(this.graph.container);
}
else
{
sizer.dialect=this.graph.dialect;
sizer.init(this.graph.getView().getOverlayPane());
}
sizer.node.style.cursor=cursor;
mxEvent.redirectMouseEvents(sizer.node,this.graph,this.state.cell,index);
var self=this;
mxEvent.addListener(sizer.node,'dblclick',function(evt)
{
self.graph.dblClick(evt,self.state.cell);
mxEvent.consume(evt);
});
return sizer;
};
mxVertexHandler.prototype.moveSizerTo=function(shape,x,y)
{
if(shape!=null)
{
shape.bounds.x=x-shape.bounds.width/2;
shape.bounds.y=y-shape.bounds.height/2;
shape.redraw();
}
};
mxVertexHandler.prototype.mouseDown=function(evt,cell,index)
{
if(this.graph.isEnabled()&&this.state.cell==cell&&index!=null)
{
this.start(evt,cell,index);
mxEvent.consume(evt);
}
};
mxVertexHandler.prototype.start=function(evt,cell,index)
{
this.index=index;
this.startX=evt.clientX;
this.startY=evt.clientY;
};
mxVertexHandler.prototype.mouseMove=function(evt)
{
if(this.index==this.LABEL_INDEX)
{
var offset=mxUtils.getOffset(this.graph.container);
var origin=mxUtils.getScrollOrigin(this.graph.container);
origin.x-=offset.x;
origin.y-=offset.y;
var x=evt.clientX+origin.x;
var y=evt.clientY+origin.y;
if(mxEvent.isGridEnabledEvent(evt))
{
x=this.graph.snap(x);
y=this.graph.snap(y);
}
this.moveSizerTo(this.sizers[8],x,y);
mxEvent.consume(evt);
}
else if(this.index!=null)
{
var scale=this.graph.getView().scale;
var dx=evt.clientX-this.startX;
var dy=evt.clientY-this.startY;
if(mxEvent.isGridEnabledEvent(evt))
{
dx=this.graph.snap(dx/scale)*scale;
dy=this.graph.snap(dy/scale)*scale;
}
this.bounds=this.union(this.state,dx,dy,this.index);
this.drawPreview();
mxEvent.consume(evt);
}
};
mxVertexHandler.prototype.mouseUp=function(evt)
{
if(this.index!=null&&this.state!=null)
{
var scale=this.graph.getView().scale;
var dx=(evt.clientX-this.startX)/scale;
var dy=(evt.clientY-this.startY)/scale;
if(mxEvent.isGridEnabledEvent(evt))
{
dx=this.graph.snap(dx);
dy=this.graph.snap(dy);
}
this.resize(this.state.cell,dx,dy,this.index);
this.reset();
mxEvent.consume(evt);
}
};
mxVertexHandler.prototype.reset=function()
{
this.index=null;
this.bounds=new mxRectangle(this.state.x,this.state.y,this.state.width,this.state.height);
this.drawPreview();
};
mxVertexHandler.prototype.resize=function(cell,dx,dy,index)
{
var geo=this.graph.model.getGeometry(cell);
if(index==this.LABEL_INDEX)
{
geo=geo.clone();
if(geo.offset==null)
{
geo.offset=new mxPoint(dx,dy);
}
else
{
geo.offset.x+=dx;
geo.offset.y+=dy;
}
this.graph.model.setGeometry(cell,geo);
}
else
{
var bounds=this.union(geo,dx,dy,index);
this.graph.resize(cell,bounds);
}
};
mxVertexHandler.prototype.union=function(bounds,dx,dy,index)
{
if(this.singleSizer)
{
return new mxRectangle(bounds.x,bounds.y,Math.max(0,bounds.width+dx),Math.max(0,bounds.height+dy));
}
else
{
var left=bounds.x;
var right=left+bounds.width;
var top=bounds.y;
var bottom=top+bounds.height;
if(index>4 )
{
bottom=bottom+dy;
}
else if(index<3 )
{
top=top+dy;
}
if(index==0||index==3||index==5 )
{
left+=dx;
}
else if(index==2||index==4||index==7 )
{
right+=dx;
}
var width=right-left;
var height=bottom-top;
if(width<0)
{
left+=width;
width=Math.abs(width);
}
if(height<0)
{
top+=height;
height=Math.abs(height);
}
return new mxRectangle(left,top,width,height);
}
};
mxVertexHandler.prototype.redraw=function()
{
this.bounds=new mxRectangle(this.state.x,this.state.y,this.state.width,this.state.height);
if(this.sizers!=null)
{
var s=this.state;
var r=s.x+s.width;
var b=s.y+s.height;
if(this.singleSizer)
{
this.moveSizerTo(this.sizers[0],r,b);
}
else
{
var cx=s.x+s.width/2;
var cy=s.y+s.height/2;
this.moveSizerTo(this.sizers[0],s.x,s.y);
if(this.sizers.length>1)
{
this.moveSizerTo(this.sizers[1],cx,s.y);
this.moveSizerTo(this.sizers[2],r,s.y);
this.moveSizerTo(this.sizers[3],s.x,cy);
this.moveSizerTo(this.sizers[4],r,cy);
this.moveSizerTo(this.sizers[5],s.x,b);
this.moveSizerTo(this.sizers[6],cx,b);
this.moveSizerTo(this.sizers[7],r,b);
this.moveSizerTo(this.sizers[8],cx+s.absoluteOffset.x,cy+s.absoluteOffset.y);
}
}
}
this.drawPreview();
};
mxVertexHandler.prototype.drawPreview=function()
{
this.selectionBorder.bounds=this.bounds;
this.selectionBorder.redraw();
};
mxVertexHandler.prototype.destroy=function()
{
this.graph.removeGraphListener(this);
this.selectionBorder.destroy();
this.selectionBorder=null;
if(this.sizers!=null)
{
for(var i=0;i<this.sizers.length;i++)
{
this.sizers[i].destroy();
this.sizers[i]=null;
}
}
};
}

{
function mxEdgeHandler(state)
{
if(state!=null)
{
this.state=state;
this.graph=this.state.view.graph;
this.graph.addGraphListener(this);
this.marker=this.createMarker();
this.init();
}
};
mxEdgeHandler.prototype.LABEL_INDEX=-1;
mxEdgeHandler.prototype.graph=null;
mxEdgeHandler.prototype.state=null;
mxEdgeHandler.prototype.marker=null;
mxEdgeHandler.prototype.error=null;
mxEdgeHandler.prototype.shape=null;
mxEdgeHandler.prototype.bends=null;
mxEdgeHandler.prototype.labelShape=null;
mxEdgeHandler.prototype.cloneEnabled=true;
mxEdgeHandler.prototype.init=function()
{

this.points=new Array();

this.abspoints=this.state.absolutePoints;
this.shape=new mxPolyline(this.abspoints,mxConstants.SELECTION_COLOR);
this.shape.strokewidth=mxConstants.SELECTION_STROKEWIDTH;
this.shape.isDashed=mxConstants.SELECTION_DASHED;
this.shape.dialect=(this.graph.dialect!=mxConstants.DIALECT_SVG)?mxConstants.DIALECT_VML:mxConstants.DIALECT_SVG;
this.shape.init(this.graph.getView().getOverlayPane());
this.shape.node.style.cursor='pointer';
var self=this;
mxEvent.addListener(this.shape.node,'dblclick',function(evt)
{
self.graph.dblClick(evt,self.state.cell);
mxEvent.consume(evt);
});
mxEvent.addListener(this.shape.node,'mousedown',function(evt)
{
self.graph.dispatchGraphEvent('mousedown',evt,self.state.cell);
});
mxEvent.addListener(this.shape.node,'mousemove',function(evt)
{
var cell=self.state.cell;


if(self.index!=null)
{
var pt=mxUtils.convertPoint(self.graph.container,evt.clientX,evt.clientY);
cell=self.graph.getCellAt(pt.x,pt.y);
if(self.graph.isSwimlane(cell)&&self.graph.hitsSwimlaneContent(cell,pt.x,pt.y))
{
cell=null;
}
}
self.graph.dispatchGraphEvent('mousemove',evt,cell);
});
mxEvent.addListener(this.shape.node,'mouseup',function(evt)
{
self.graph.dispatchGraphEvent('mouseup',evt,self.state.cell);
});

if(this.graph.getSelectionCount()<mxGraphHandler.prototype.maxCells||mxGraphHandler.prototype.maxCells<=0)
{
this.bends=this.createBends();
}
this.label=new mxPoint(this.state.absoluteOffset.x,this.state.absoluteOffset.y);
this.labelShape=new mxRectangleShape(new mxRectangle(),mxConstants.LABEL_HANDLE_FILLCOLOR,mxConstants.HANDLE_STROKECOLOR);
this.initBend(this.labelShape);
this.labelShape.node.style.cursor='move';

var cell=this.state.cell;
mxEvent.addListener(this.labelShape.node,'dblclick',function(evt)
{
self.graph.dblClick(evt,cell);
mxEvent.consume(evt);
});
mxEvent.addListener(this.labelShape.node,'mousedown',function(evt)
{
self.graph.dispatchGraphEvent('mousedown',evt,cell,mxEdgeHandler.prototype.LABEL_INDEX);
});
this.redraw();
};
mxEdgeHandler.prototype.createMarker=function()
{
var marker=new mxCellMarker(this.graph);
var self=this;


marker.getCell=function(evt,cell)
{
var cell=mxCellMarker.prototype.getCell.apply(this,arguments);
var model=self.graph.getModel();
if(cell==self.state.cell||(cell!=null&&!self.graph.connectableEdges&&model.isEdge(cell)))
{
cell=null;
}
return cell;
};
marker.isValidState=function(state)
{
var model=self.graph.getModel();
var other=model.getTerminal(self.state.cell,!self.isSource);
var source=(self.isSource)?state.cell:other;
var target=(self.isSource)?other:state.cell;
self.error=self.validateConnection(source,target);
return self.error==null;
};
return marker;
};
mxEdgeHandler.prototype.validateConnection=function(source,target)
{
return this.graph.getEdgeValidationError(this.state.cell,source,target);
};
mxEdgeHandler.prototype.createBends=function()
{
var cell=this.state.cell;
var bends=new Array();
for(var i=0;i<this.abspoints.length;i++)
{
if(!this.abspoints[i].isRouted)
{
var source=i==0;
var target=i==this.abspoints.length-1;
var terminal=source||target;
if(terminal||this.graph.isBendable(cell))
{
var bend=new mxRectangleShape(new mxRectangle(),mxConstants.HANDLE_FILLCOLOR,mxConstants.HANDLE_STROKECOLOR);
this.initBend(bend);
bend.node.style.cursor='all-scroll';
this.installListeners(bend.node,bends.length);
bends.push(bend);
if(!terminal)
{
this.points.push(new mxPoint(0,0));
}
}
}
}
return bends;
};
mxEdgeHandler.prototype.initBend=function(bend)
{
if(this.graph.dialect==mxConstants.DIALECT_SVG)
{
bend.dialect=mxConstants.DIALECT_PREFERHTML;
bend.init(this.graph.container);
}
else
{
bend.dialect=this.graph.dialect;
bend.init(this.graph.getView().getOverlayPane());
}
};
mxEdgeHandler.prototype.mouseDown=function(evt,cell,index)
{
if(index==this.LABEL_INDEX&&!this.graph.isLabelMovable(cell))
{
mxEvent.consume(evt);
}
else if(this.graph.isEnabled()&&this.state.cell==cell&&this.marker!=null&&index!=null)
{
this.start(evt,cell,index);
mxEvent.consume(evt);
}
};
mxEdgeHandler.prototype.start=function(evt,cell,index)
{
this.isSource=index==0;
this.isTarget=index==this.bends.length-1;
this.isLabel=index==this.LABEL_INDEX;
this.startX=evt.clientX;
this.startY=evt.clientY;
if(this.isSource||this.isTarget)
{
var terminal=this.graph.model.getTerminal(cell,this.isSource);
if(terminal==null||this.graph.isDisconnectable(cell,terminal,this.isSource))
{
var p0=this.abspoints[0];
var pe=this.abspoints[this.abspoints.length-1];
this.abspoints=new Array();
this.abspoints.push(p0);
this.abspoints.push(pe);
this.index=index;
}
}
else
{
this.index=index;
}
};
mxEdgeHandler.prototype.mouseMove=function(evt,cell)
{
if(this.index!=null&&this.marker!=null)
{
var view=this.graph.getView();
var point=mxUtils.convertPoint(this.graph.container,evt.clientX,evt.clientY);
var scale=view.scale;
if(mxEvent.isGridEnabledEvent(evt))
{
point.x=this.graph.snap(point.x/scale)*scale;
point.y=this.graph.snap(point.y/scale)*scale;
}
if(this.isLabel)
{
this.label.x=point.x;
this.label.y=point.y;
}
else
{


var clone=this.state.clone();
var geometry=this.graph.getCellGeometry(this.state.cell);
var points=geometry.points;
var source=null;
var target=null;
if(this.isSource||this.isTarget)
{
this.marker.process(evt,cell);
var currentState=this.marker.getValidState();
target=this.graph.getView().getVisibleTerminal(this.state.cell,!this.isSource);
if(currentState!=null)
{
source=currentState.cell;
}
else
{
clone.setAbsoluteTerminalPoint(point,this.isSource);
if(this.marker.getMarkedState()==null)
{
this.error=(this.graph.allowDanglingEdges)?null:'';
}
}
if(!this.isSource)
{
var tmp=source;
source=target;
target=tmp;
}
}
else
{
this.convertPoint(point);
if(points==null)
{
points=[point];
}
else
{
points[this.index-1]=point;
}
this.points=points;
this.active=true;
source=clone.view.getVisibleTerminal(this.state.cell,true);
target=clone.view.getVisibleTerminal(this.state.cell,false);
}
clone.view.updatePoints(clone,points,source,target);
clone.view.updateTerminalPoints(clone,source,target);

var color=(this.error==null)?this.marker.validColor:
this.marker.invalidColor;
this.setPreviewColor(color);
this.abspoints=clone.absolutePoints;
}
this.drawPreview();
mxEvent.consume(evt);
}
};
mxEdgeHandler.prototype.mouseUp=function(evt)
{
if(this.index!=null&&this.marker!=null)
{
var edge=this.state.cell;
if(evt.clientX!=this.startX||evt.clientY!=this.startY)
{

if(this.error!=null)
{
if(this.error.length>0)
{
this.graph.validationAlert(this.error);
}
}
else if(this.isLabel)
{
this.moveLabel(this.state,this.label.x,this.label.y);
}
else if(this.isSource||this.isTarget)
{
if(this.marker.hasValidState())
{
var edge=this.connect(edge,this.marker.getValidState().cell,this.isSource,mxEvent.isCloneEvent(evt)&&this.cloneEnabled&&this.graph.isCloneable());
}
else if(this.graph.allowDanglingEdges)
{
var pt=this.graph.getPointForEvent(evt);
var pstate=this.graph.getView().getState(this.graph.getModel().getParent(edge));
if(pstate!=null)
{
pt.x-=pstate.origin.x;
pt.y-=pstate.origin.y;
}
this.changeTerminalPoint(edge,pt,this.isSource);
}
}
else if(this.active)
{
this.changePoints(edge,this.points);
}
else
{
this.graph.getView().invalidate(this.state.cell);
this.graph.getView().revalidate(this.state.cell);
}
this.abspoints=this.state.absolutePoints;
}

if(this.marker!=null)
{
this.reset();
if(edge!=this.state.cell)
{
this.graph.setSelectionCell(edge);
}
}
mxEvent.consume(evt);
}
};
mxEdgeHandler.prototype.reset=function()
{
this.error=null;
this.index=null;
this.label=null;
this.active=false;
this.isLabel=false;
this.isSource=false;
this.isTarget=false;
this.marker.reset();
this.setPreviewColor(mxConstants.SELECTION_COLOR);
this.redraw();
};
mxEdgeHandler.prototype.setPreviewColor=function(color)
{
if(this.shape!=null&&this.shape.node!=null)
{
if(this.shape.dialect==mxConstants.DIALECT_SVG)
{
this.shape.innerNode.setAttribute('stroke',color);
}
else
{
this.shape.node.setAttribute('strokecolor',color);
}
}
};
mxEdgeHandler.prototype.convertPoint=function(point)
{
var scale=this.graph.getView().scale;
point.x=this.graph.snap(point.x)/scale-this.graph.getView().translate.x;
point.y=this.graph.snap(point.y)/scale-this.graph.getView().translate.y;
return point;
};
mxEdgeHandler.prototype.moveLabel=function(edgeState,x,y)
{
var model=this.graph.getModel();
var geometry=model.getGeometry(edgeState.cell);
if(geometry!=null)
{
geometry=geometry.clone();
var pt=this.graph.getView().getRelativePoint(edgeState,x,y);
geometry.x=pt.x;
geometry.y=pt.y;

var scale=this.graph.getView().scale;
geometry.offset=new mxPoint(0,0);
var pt=this.graph.view.getPoint(edgeState,geometry);
geometry.offset=new mxPoint((x-pt.x)/scale,(y-pt.y)/scale);
model.setGeometry(edgeState.cell,geometry);
}
};
mxEdgeHandler.prototype.connect=function(edge,terminal,isSource,isClone)
{
var model=this.graph.getModel();
var parent=model.getParent(edge);
model.beginUpdate();
try
{
if(isClone)
{
var clone=edge.clone();
model.add(parent,clone,model.getChildCount(parent));
var other=model.getTerminal(edge,!isSource);
model.setTerminal(clone,other,!isSource);
edge=clone;
}
if(terminal==null)
{
var scale=this.graph.getView().scale;
var tr=this.graph.getView().translate;
var pstate=this.graph.getView().getState(parent);
var dx=(pstate!=null)?pstate.origin.x:0;
var dy=(pstate!=null)?pstate.origin.y:0;
var current=this.abspoints[(isSource)?0:this.abspoints.length-1];
var geo=model.getGeometry(edge).clone();
geo.setTerminalPoint(new mxPoint((current.x-dx)/scale-tr.x,(current.y-dy)/scale-tr.y),isSource);
model.setGeometry(edge,geo);
model.setTerminal(edge,null,isSource);
}
else
{
this.graph.connect(edge,terminal,isSource);
}
}
finally
{
model.endUpdate();
}
return edge;
};
mxEdgeHandler.prototype.changeTerminalPoint=function(edge,point,isSource)
{
var model=this.graph.getModel();
var geo=model.getGeometry(edge);
if(geo!=null)
{
model.beginUpdate();
try
{
geo=geo.clone();
geo.setTerminalPoint(point,isSource);
model.setGeometry(edge,geo);
model.setTerminal(edge,null,isSource);
}
finally
{
model.endUpdate();
}
}
};
mxEdgeHandler.prototype.changePoints=function(edge,points)
{
var model=this.graph.getModel();
var geo=model.getGeometry(edge);
if(geo!=null)
{
geo=geo.clone();
geo.points=points;
model.setGeometry(edge,geo);
}
};
mxEdgeHandler.prototype.redraw=function()
{
this.abspoints=this.state.absolutePoints;
var cell=this.state.cell;
if(this.bends!=null&&this.bends.length>0)
{
var model=this.graph.getModel();
var s=(this.graph.dialect==mxConstants.DIALECT_SVG)?3:4;
var n=this.abspoints.length-1;
var p0=this.abspoints[0];
var x0=this.abspoints[0].x;
var y0=this.abspoints[0].y;
this.bends[0].bounds=new mxRectangle(x0-s,y0-s,2*s,2*s);
var terminal=model.getTerminal(cell,true);
var connected=terminal!=null;
var movable=!connected||this.graph.isDisconnectable(cell,terminal,true);
var color=(movable)?((connected)?mxConstants.CONNECT_HANDLE_FILLCOLOR:
mxConstants.HANDLE_FILLCOLOR):
mxConstants.LOCKED_HANDLE_FILLCOLOR;
this.bends[0].fill=color;
this.bends[0].reconfigure();
this.bends[0].redraw();
var pe=this.abspoints[n];
var xn=this.abspoints[n].x;
var yn=this.abspoints[n].y;
var bn=this.bends.length-1;
this.bends[bn].bounds=new mxRectangle(xn-s,yn-s,2*s,2*s);
terminal=model.getTerminal(cell,false);
connected=terminal!=null;
movable=!connected||this.graph.isDisconnectable(cell,terminal,false);
color=(movable)?((connected)?mxConstants.CONNECT_HANDLE_FILLCOLOR:
mxConstants.HANDLE_FILLCOLOR):
mxConstants.LOCKED_HANDLE_FILLCOLOR;
this.bends[bn].fill=color;
this.bends[bn].reconfigure();
this.bends[bn].redraw();
this.redrawInnerBends(p0,pe);
}
var s=(this.graph.dialect==mxConstants.DIALECT_SVG)?2:3;
this.label=new mxPoint(this.state.absoluteOffset.x,this.state.absoluteOffset.y);
var bounds=new mxRectangle(this.label.x-s,this.label.y-s,2*s,2*s);
this.labelShape.bounds=bounds;
this.labelShape.redraw();
var lab=this.graph.getLabel(cell);
if(lab!=null&&lab.length>0&&this.graph.isLabelMovable(cell))
{
this.labelShape.node.style.visibility='visible';
}
else
{
this.labelShape.node.style.visibility='hidden';
}
this.drawPreview();
};
mxEdgeHandler.prototype.redrawInnerBends=function(p0,pe)
{
var s=4;
var g=this.graph.getModel().getGeometry(this.state.cell);
var pts=g.points;
if(pts!=null)
{
for(var i=1;i<this.bends.length-1;i++)
{
if(this.abspoints[i]!=null)
{
var x=this.abspoints[i].x;
var y=this.abspoints[i].y;
this.bends[i].bounds=new mxRectangle(x-s,y-s,2*s,2*s);
this.bends[i].redraw();
this.points[i-1]=pts[i-1];
}
else if(this.bends[i]!=null)
{
this.bends[i].destroy();
this.bends[i]=null;
}
}
}
};
mxEdgeHandler.prototype.drawPreview=function()
{
if(this.isLabel)
{
var s=(this.graph.dialect==mxConstants.DIALECT_SVG)?2:3;
var bounds=new mxRectangle(this.label.x-s,this.label.y-s,2*s,2*s);
this.labelShape.bounds=bounds;
this.labelShape.redraw();
}
else
{
this.shape.points=this.abspoints;
this.shape.redraw();
}
};
mxEdgeHandler.prototype.installListeners=function(node,index)
{
var self=this;
mxEvent.addListener(node,'mousedown',function(evt)
{
self.graph.dispatchGraphEvent('mousedown',evt,self.state.cell,index);
});
mxEvent.addListener(node,'mouseup',function(evt)
{
self.graph.dispatchGraphEvent('mouseup',evt,self.state.cell,index);
});
};
mxEdgeHandler.prototype.destroy=function()
{
this.graph.removeGraphListener(this);
this.marker.destroy();
this.marker=null;
this.shape.destroy();
this.shape=null;
this.labelShape.destroy();
this.labelShape=null;
if(this.bends!=null)
{
for(var i=0;i<this.bends.length;i++)
{
if(this.bends[i]!=null)
{
this.bends[i].destroy();
this.bends[i]=null;
}
}
}
};
}

{
function mxElbowEdgeHandler(state)
{
if(state!=null)
{
this.state=state;
this.graph=this.state.view.graph;
this.graph.addGraphListener(this);
this.marker=this.createMarker();
this.init();
}
};
mxElbowEdgeHandler.prototype=new mxEdgeHandler();
mxElbowEdgeHandler.prototype.constructor=mxElbowEdgeHandler;
mxElbowEdgeHandler.prototype.doubleClickOrientationResource=(mxClient.language!='none')?'doubleClickOrientation':'';
mxElbowEdgeHandler.prototype.createBends=function()
{
var bends=new Array();
var bend=new mxRectangleShape(new mxRectangle(),mxConstants.HANDLE_FILLCOLOR,mxConstants.HANDLE_STROKECOLOR);
this.initBend(bend);
bend.node.style.cursor='all-scroll';
this.installListeners(bend.node,bends.length);
bends.push(bend);
bends.push(this.createVirtualBend());
this.points.push(new mxPoint(0,0));
bend=new mxRectangleShape(new mxRectangle(),mxConstants.HANDLE_FILLCOLOR,mxConstants.HANDLE_STROKECOLOR);
this.initBend(bend);
bend.node.style.cursor='all-scroll';
this.installListeners(bend.node,bends.length);
bends.push(bend);
return bends;
};
mxElbowEdgeHandler.prototype.createVirtualBend=function()
{
var bend=new mxRectangleShape(new mxRectangle(0,0,1,1),mxConstants.HANDLE_FILLCOLOR,mxConstants.HANDLE_STROKECOLOR);
this.initBend(bend);
var crs=this.getCursorForBend();
bend.node.style.cursor=crs;
if(this.graph.isBendable(this.state.cell))
{
this.installListeners(bend.node,1);
}
else
{
bend.node.style.visibility='hidden';
}
var self=this;
mxEvent.addListener(bend.node,'dblclick',function(evt)
{
self.graph.flip(self.state.cell,evt);
mxEvent.consume(evt);
});
mxEvent.addListener(bend.node,'mousemove',function(evt)
{
self.graph.dispatchGraphEvent('mousemove',evt,self.state.cell,mxResources.get(self.doubleClickOrientationResource)||self.doubleClickOrientationResource);
});
return bend;
};
mxElbowEdgeHandler.prototype.getCursorForBend=function()
{
return(this.state.style[mxConstants.STYLE_EDGE]==mxEdgeStyle.TopToBottom||(this.state.style[mxConstants.STYLE_EDGE]==mxEdgeStyle.ElbowConnector&&this.state.style[mxConstants.STYLE_ELBOW]==mxConstants.ELBOW_VERTICAL))?'row-resize':'col-resize';
};
mxElbowEdgeHandler.prototype.convertPoint=function(point)
{
var scale=this.graph.view.scale;
point.x=this.graph.snap(point.x)/scale-this.graph.getView().translate.x-this.state.origin.x;
point.y=this.graph.snap(point.y)/scale-this.graph.getView().translate.y-this.state.origin.y;
};
mxElbowEdgeHandler.prototype.redrawInnerBends=function(p0,pe)
{
var s=(this.graph.dialect==mxConstants.DIALECT_SVG)?3:4;
var g=this.graph.getModel().getGeometry(this.state.cell);
var pts=g.points;
var pt=(pts!=null)?pts[0]:null;
if(pt==null)
{
pt=new mxPoint(p0.x+(pe.x-p0.x)/2,p0.y+(pe.y-p0.y)/2);
}
else
{
pt=new mxPoint(this.graph.getView().scale*(pt.x+this.graph.getView().translate.x+this.state.origin.x),this.graph.getView().scale*(pt.y+this.graph.getView().translate.y+this.state.origin.y));
}
this.bends[1].bounds=new mxRectangle(pt.x-s,pt.y-s,2*s,2*s);
this.bends[1].reconfigure();
this.bends[1].redraw();
};
}

{
function mxKeyHandler(graph,target)
{
if(graph!=null)
{
this.graph=graph;
this.target=target||document.documentElement;
this.normalKeys=new Array();
this.controlKeys=new Array();
var self=this;
mxEvent.addListener(this.target,"keydown",function(evt)
{
self.keyDown(evt);
});
if(mxClient.IS_IE)
{
mxEvent.addListener(window,'unload',function()
{
self.destroy();
});
}
}
};
mxKeyHandler.prototype.graph=null;
mxKeyHandler.prototype.target=null;
mxKeyHandler.prototype.normalKeys=null;
mxKeyHandler.prototype.controlKeys=null;
mxKeyHandler.prototype.enabled=true;
mxKeyHandler.prototype.isEnabled=function()
{
return this.enabled;
};
mxKeyHandler.prototype.setEnabled=function(enabled)
{
this.enabled=enabled;
};
mxKeyHandler.prototype.bindKey=function(code,funct)
{
this.normalKeys[code]=funct;
};
mxKeyHandler.prototype.bindControlKey=function(code,funct)
{
this.controlKeys[code]=funct;
};
mxKeyHandler.prototype.getFunction=function(evt)
{
if(evt!=null)
{
return(evt.ctrlKey)?this.controlKeys[evt.keyCode]:
this.normalKeys[evt.keyCode];
}
return null;
};
mxKeyHandler.prototype.isGraphEvent=function(evt)
{
var source=mxEvent.getSource(evt);

if((source==this.target||source.parentNode==this.target)||(this.graph.editor!=null&&source==this.graph.editor.textarea))
{
return true;
}
var elt=source;
while(elt!=null)
{
if(elt==this.graph.container)
{
return true;
}
elt=elt.parentNode;
}
return false;
};
mxKeyHandler.prototype.keyDown=function(evt)
{
if(this.graph.isEnabled()&&this.isGraphEvent(evt)&&this.isEnabled())
{
if(this.graph.isEditing()&&((evt.keyCode==13 &&!evt.ctrlKey&&!evt.shiftKey)||(evt.keyCode==113 )))
{
this.enter(evt);
}
else if(evt.keyCode==27 )
{
this.escape(evt);
}
else if(!this.graph.isEditing())
{
var boundFunction=this.getFunction(evt);
if(boundFunction!=null)
{
boundFunction(evt);
mxEvent.consume(evt);
}
}
}
};
mxKeyHandler.prototype.enter=function(evt)
{
this.graph.editor.stopEditing(false);
};
mxKeyHandler.prototype.escape=function(evt)
{
this.graph.escape(evt);
};
mxKeyHandler.prototype.destroy=function()
{
this.target=null;
};
}

{
function mxTooltipHandler(graph,delay)
{
if(graph!=null)
{
this.graph=graph;
this.delay=delay||500;
if(document.body!=null)
{
this.graph.addGraphListener(this);
}
}
};
mxTooltipHandler.prototype.zIndex=10005;
mxTooltipHandler.prototype.graph=null;
mxTooltipHandler.prototype.delay=null;
mxTooltipHandler.prototype.hideOnHover=false;
mxTooltipHandler.prototype.enabled=true;
mxTooltipHandler.prototype.init=function()
{
if(document.body!=null)
{
this.div=document.createElement('div');
this.div.className='mxTooltip';
this.div.style.position='absolute';
this.div.style.visibility='hidden';
this.div.style.zIndex=this.zIndex;
if(!mxClient.IS_IE&&mxClient.TOOLTIP_SHADOWS)
{
this.shadow=document.createElement('div');
this.shadow.style.position='absolute';
this.shadow.style.visibility='hidden';
this.shadow.style.background=mxConstants.SVG_SHADOWCOLOR;
this.shadow.style.zIndex=this.zIndex;
mxUtils.setOpacity(this.shadow,70);
document.body.appendChild(this.shadow);
}
else if(mxClient.IS_IE&&!mxClient.TOOLTIP_SHADOWS)
{
this.div.style.filter='';
}
document.body.appendChild(this.div);
var self=this;
mxEvent.addListener(this.div,'mousedown',function(evt)
{
self.hide();
});
}
};
mxTooltipHandler.prototype.isEnabled=function()
{
return this.enabled;
};
mxTooltipHandler.prototype.setEnabled=function(enabled)
{
this.enabled=enabled;
};
mxTooltipHandler.prototype.mouseDown=function(evt,cell,index)
{
this.reset(evt,cell,index,false);
this.hide();
};
mxTooltipHandler.prototype.mouseMove=function(evt,cell,index)
{
if(evt.clientX!=this.lastX||evt.clientY!=this.lastY)
{
this.reset(evt,cell,index,true);
if(this.hideOnHover||cell!=this.cell||index!=this.index)
{
this.hide();
}
}
this.lastX=evt.clientX;
this.lastY=evt.clientY;
};
mxTooltipHandler.prototype.mouseUp=function(evt,cell,index)
{
this.reset(evt,cell,index,true);
this.hide();
};
mxTooltipHandler.prototype.reset=function(evt,cell,index,restart)
{
if(this.thread!=null)
{
window.clearTimeout(this.thread);
this.thread=null;
}
if(restart&&this.isEnabled()&&cell!=null&&this.div.style.visibility=='hidden')
{
var x=evt.clientX;
var y=evt.clientY;
var self=this;
this.thread=window.setTimeout(function()
{
if(!self.graph.isEditing()&&!self.graph.panningHandler.isMenuShowing())
{
var tip=self.graph.getTooltip(cell,index);
self.show(tip,x,y);
self.cell=cell;
self.index=index;
}
},this.delay);
}
};
mxTooltipHandler.prototype.hide=function()
{
if(this.shadow!=null)
{
this.shadow.style.visibility='hidden';
}
if(this.div!=null)
{
this.div.style.visibility='hidden';
}
};
mxTooltipHandler.prototype.show=function(tip,x,y)
{
if(tip!=null&&tip.length>0)
{
var origin=mxUtils.getScrollOrigin();
this.div.style.left=(x+origin.x)+'px';
this.div.style.top=(y+mxConstants.TOOLTIP_VERTICAL_OFFSET+origin.y)+'px';
if(!mxUtils.isNode(tip))
{
this.div.innerHTML=tip.replace(/\n/g,'<br>');
}
else
{
this.div.innerHTML='';
this.div.appendChild(tip);
}
this.div.style.visibility='';
mxUtils.fit(this.div);
if(this.shadow!=null)
{
this.shadow.style.width=this.div.offsetWidth+'px';
this.shadow.style.height=this.div.offsetHeight+'px';
this.shadow.style.left=(parseInt(this.div.style.left)+3)+'px';
this.shadow.style.top=(parseInt(this.div.style.top)+3)+'px';
this.shadow.style.visibility='';
}
}
};
mxTooltipHandler.prototype.destroy=function()
{
this.graph.removeGraphListener(this);
mxUtils.release(this.div);
if(this.div.parentNode!=null)
{
this.div.parentNode.removeChild(this.div);
}
this.div=null;
if(this.shadow!=null)
{
mxUtils.release(this.shadow);
if(this.shadow.parentNode!=null)
{
this.shadow.parentNode.removeChild(this.shadow);
}
this.shadow=null;
}
};
}

{
function mxHighlight(graph,color,funct)
{
mxCellMarker.call(this,graph,color);
this.graph.addGraphListener(this);
if(funct!=null)
{
this.getCell=funct;
}
if(mxClient.IS_IE)
{
var self=this;
mxEvent.addListener(window,'unload',function()
{
self.destroy();
});
}
};
mxHighlight.prototype=new mxCellMarker();
mxHighlight.prototype.constructor=mxHighlight;
mxHighlight.prototype.mouseDown=function(evt,cell){};
mxHighlight.prototype.mouseMove=function(evt,cell)
{
if(this.isEnabled())
{
this.process(evt,cell);
}
};
mxHighlight.prototype.mouseUp=function(evt,cell)
{
this.reset();
};
mxHighlight.prototype.destroy=function()
{
if(!this.destroyed)
{
this.destroyed=true;
this.graph.removeGraphListener(this);
mxCellMarker.prototype.destroy.apply(this);
}
};
}

{
function mxDefaultKeyHandler(editor)
{
if(editor!=null)
{
this.editor=editor;
this.handler=new mxKeyHandler(editor.graph);


var old=this.handler.escape;
this.handler.escape=function(evt)
{
old.apply(this,arguments);
editor.hideProperties();
editor.dispatchEvent('escape',editor,evt);
};
}
};
mxDefaultKeyHandler.prototype.editor=null;
mxDefaultKeyHandler.prototype.handler=null;
mxDefaultKeyHandler.prototype.bindAction=function(code,action,control)
{
var self=this;
var keyHandler=function()
{
self.editor.execute(action);
};
if(control)
{
this.handler.bindControlKey(code,keyHandler);
}
else
{
this.handler.bindKey(code,keyHandler);
}
};
mxDefaultKeyHandler.prototype.destroy=function()
{
this.handler.destroy();
this.handler=null;
};
}

{
function mxDefaultPopupMenu(config)
{
this.config=config;
};
mxDefaultPopupMenu.prototype.config=null;
mxDefaultPopupMenu.prototype.createMenu=function(editor,menu,cell,evt)
{
if(this.config!=null)
{
var conditions=this.createConditions(editor,cell,evt);
var item=this.config.firstChild;
this.addItems(editor,menu,cell,evt,conditions,item,null);
}
};
mxDefaultPopupMenu.prototype.addItems=function(editor,menu,cell,evt,conditions,item,parent)
{
var addSeparator=false;
while(item!=null)
{
if(item.nodeName=='add')
{
var condition=item.getAttribute('if');
if(condition==null||conditions[condition])
{
var as=item.getAttribute('as');
as=mxResources.get(as)||as;
var funct=mxUtils.eval(mxUtils.getTextContent(item));
var action=item.getAttribute('action');
var icon=item.getAttribute('icon');
if(addSeparator)
{
menu.addSeparator(parent);
addSeparator=false;
}
var row=this.addAction(menu,editor,as,icon,funct,action,cell,parent);
this.addItems(editor,menu,cell,evt,conditions,item.firstChild,row);
}
}
else if(item.nodeName=='separator')
{
addSeparator=true;
}
item=item.nextSibling;
}
};
mxDefaultPopupMenu.prototype.addAction=function(menu,editor,lab,icon,funct,action,cell,parent)
{
var clickHandler=function()
{
if(typeof(funct)=='function')
{
funct.call(editor,editor,cell);
}
if(action!=null)
{
editor.execute(action,cell);
}
};
return menu.addItem(lab,icon,clickHandler,parent);
};
mxDefaultPopupMenu.prototype.createConditions=function(editor,cell,evt)
{
var model=editor.graph.getModel();
var childCount=model.getChildCount(cell);
var conditions=new Array();
conditions['nocell']=cell==null;
conditions['ncells']=editor.graph.getSelectionCount()>1;
conditions['notRoot']=model.getRoot()!=
model.getParent(editor.graph.getDefaultParent());
conditions['cell']=cell!=null;
var isCell=cell!=null&&editor.graph.getSelectionCount()==1;
conditions['nonEmpty']=isCell&&childCount>0;
conditions['expandable']=isCell&&editor.graph.isExpandable(cell);
conditions['collapsable']=isCell&&editor.graph.isCollapsable(cell);
conditions['validRoot']=isCell&&editor.graph.isValidRoot(cell);
conditions['emptyValidRoot']=conditions['validRoot']&&childCount==0;
conditions['swimlane']=isCell&&editor.graph.isSwimlane(cell);
var condNodes=this.config.getElementsByTagName('condition');
for(var i=0;i<condNodes.length;i++)
{
var funct=mxUtils.eval(mxUtils.getTextContent(condNodes[i]));
var name=condNodes[i].getAttribute('name');
if(name!=null&&typeof(funct)=='function')
{
conditions[name]=funct(editor,cell,evt);
}
}
return conditions;
};
}

{
function mxDefaultToolbar(container,editor)
{
this.editor=editor;
if(container!=null&&editor!=null)
{
this.init(container);
}
};
mxDefaultToolbar.prototype.editor=null;
mxDefaultToolbar.prototype.toolbar=null;
mxDefaultToolbar.prototype.resetHandler=null;
mxDefaultToolbar.prototype.spacing=4;
mxDefaultToolbar.prototype.connectOnDrop=false;
mxDefaultToolbar.prototype.init=function(container)
{
if(container!=null)
{
this.toolbar=new mxToolbar(container);

var self=this;
this.toolbar.addListener('select',function(sender,funct)
{
if(funct!=null)
{
self.editor.insertFunction=function()
{
funct.apply(self,arguments);
self.toolbar.resetMode();
};
}
else
{
self.editor.insertFunction=null;
}
});
this.resetHandler=function()
{
if(self.toolbar!=null)
{
self.toolbar.resetMode(true);
}
};
this.editor.graph.addListener('dblclick',this.resetHandler);
this.editor.addListener('escape',this.resetHandler);
}
};
mxDefaultToolbar.prototype.addItem=function(title,icon,action,pressed)
{
var self=this;
var clickHandler=function()
{
self.editor.execute(action);
};
this.toolbar.addItem(title,icon,clickHandler,pressed);
};
mxDefaultToolbar.prototype.addSeparator=function(icon)
{
icon=icon||mxClient.imageBasePath+'separator.gif';
this.toolbar.addSeparator(icon);
};
mxDefaultToolbar.prototype.addCombo=function()
{
return this.toolbar.addCombo();
};
mxDefaultToolbar.prototype.addActionCombo=function(title)
{
return this.toolbar.addActionCombo(title);
};
mxDefaultToolbar.prototype.addActionOption=function(combo,title,action)
{
var self=this;
var clickHandler=function()
{
self.editor.execute(action);
};
this.addOption(combo,title,clickHandler);
};
mxDefaultToolbar.prototype.addOption=function(combo,title,value)
{
return this.toolbar.addOption(combo,title,value);
};
mxDefaultToolbar.prototype.addMode=function(title,icon,mode,pressed,funct)
{
var self=this;
var clickHandler=function()
{
self.editor.setMode(mode);
if(funct!=null)
{
funct(self.editor);
}
};
this.toolbar.addSwitchMode(title,icon,clickHandler,pressed);
};
mxDefaultToolbar.prototype.addPrototype=function(title,icon,ptype,pressed,insert)
{
var img=null;
if(ptype==null)
{
img=this.toolbar.addMode(title,icon,null,pressed);
}
else
{

var factory=function()
{
if(typeof(ptype)=='function')
{
return ptype();
}
else
{
return ptype.clone();
}
};

var self=this;
var clickHandler=function(evt,cell)
{
if(typeof(insert)=='function')
{
insert(self.editor,factory(),evt,cell);
}
else
{
self.drop(factory(),evt,cell);
}
self.toolbar.resetMode();
mxEvent.consume(evt);
};
img=this.toolbar.addMode(title,icon,clickHandler,pressed);

var dropHandler=function(graph,evt,cell)
{
clickHandler(evt,cell);
};
this.installDropHandler(img,dropHandler);
}
return img;
};
mxDefaultToolbar.prototype.drop=function(vertex,evt,target)
{
var graph=this.editor.graph;
var model=graph.getModel();
if(target==null||model.isEdge(target)||!this.connectOnDrop||!model.isConnectable(target))
{
while(target!=null&&!graph.isValidDropTarget(target))
{
target=model.getParent(target);
}
this.insert(vertex,evt,target);
}
else
{
this.connect(vertex,evt,target);
}
};
mxDefaultToolbar.prototype.insert=function(vertex,evt,parent)
{
var graph=this.editor.graph;
var pt=mxUtils.convertPoint(graph.container,evt.clientX,evt.clientY);
return this.editor.addVertex(parent,vertex,pt.x,pt.y);
};
mxDefaultToolbar.prototype.connect=function(vertex,evt,source)
{
var graph=this.editor.graph;
var model=graph.getModel();
if(source!=null&&model.isConnectable(vertex)&&graph.isEdgeValid(null,source,vertex))
{
var edge=null;
model.beginUpdate();
try
{
var geo=model.getGeometry(source);
var g=model.getGeometry(vertex).clone();

g.x=geo.x+(geo.width-g.width)/2;
g.y=geo.y+(geo.height-g.height)/2;
var step=this.spacing*graph.gridSize;
var dist=model.getDirectedEdgeCount(source,true)*20;
if(this.editor.horizontalFlow)
{
g.x+=(g.width+geo.width)/2+step+dist;
}
else
{
g.y+=(g.height+geo.height)/2+step+dist;
}
vertex.setGeometry(g);

var parent=model.getParent(source);
graph.addCell(vertex,parent);
graph.keepInside([vertex]);

edge=this.editor.createEdge(source,vertex);
if(model.getGeometry(edge)==null)
{
var edgeGeometry=new mxGeometry();
edgeGeometry.relative=true;
model.setGeometry(edge,edgeGeometry);
}
graph.addEdge(edge,parent,source,vertex);
}
finally
{
model.endUpdate();
}
graph.setSelectionCells([vertex,edge]);
graph.scrollCellToVisible(vertex);
}
};
mxDefaultToolbar.prototype.installDropHandler=function(img,dropHandler)
{
var sprite=document.createElement('img');
sprite.setAttribute('src',img.getAttribute('src'));
var self=this;
var loader=function(evt)
{
sprite.style.width=(2*img.offsetWidth)+'px';
sprite.style.height=(2*img.offsetHeight)+'px';
mxUtils.makeDraggable(img,self.editor.graph,dropHandler,sprite);
mxEvent.removeListener(sprite,'load',loader);
};
if(mxClient.IS_IE)
{
loader();
}
else
{
mxEvent.addListener(sprite,'load',loader);
}
};
mxDefaultToolbar.prototype.destroy=function()
{
if(this.resetHandler!=null)
{
this.editor.graph.removeListener('dblclick',this.resetHandler);
this.editor.removeListener('escape',this.resetHandler);
this.resetHandler=null;
}
if(this.toolbar!=null)
{
this.toolbar.destroy();
this.toolbar=null;
}
};
}

{
function mxEditor(config)
{
this.actions=new Array();
this.addActions();

if(document.body!=null)
{
this.cycleAttributeValues=new Array();
this.popupHandler=new mxDefaultPopupMenu();
this.undoManager=new mxUndoManager();
this.graph=this.createGraph();
this.toolbar=this.createToolbar();
this.keyHandler=new mxDefaultKeyHandler(this);

this.configure(config);
this.graph.swimlaneIndicatorColorAttribute=this.cycleAttributeName;

if(!mxClient.IS_LOCAL&&this.urlInit!=null)
{
this.createSession();
}
if(this.onInit!=null)
{

var tmp=document.cookie;
var isFirstTime=tmp.indexOf('mxgraph=seen')<0;
if(isFirstTime)
{

document.cookie=
'mxgraph=seen; expires=Fri, 27 Jul 2199 02:47:11 UTC; path=/';
}
this.onInit(isFirstTime);
}
if(mxClient.IS_IE)
{
var self=this;
mxEvent.addListener(window,'unload',function()
{
self.destroy();
});
}
}
};
mxResources.add(mxClient.basePath+'js/resources/editor');
mxEditor.prototype=new mxEventSource();
mxEditor.prototype.constructor=mxEditor;

mxEditor.prototype.askZoomResource=(mxClient.language!='none')?'askZoom':'';
mxEditor.prototype.lastSavedResource=(mxClient.language!='none')?'lastSaved':'';
mxEditor.prototype.currentFileResource=(mxClient.language!='none')?'currentFile':'';
mxEditor.prototype.propertiesResource=(mxClient.language!='none')?'properties':'';
mxEditor.prototype.tasksResource=(mxClient.language!='none')?'tasks':'';
mxEditor.prototype.helpResource=(mxClient.language!='none')?'help':'';
mxEditor.prototype.outlineResource=(mxClient.language!='none')?'outline':'';
mxEditor.prototype.outline=null;
mxEditor.prototype.graph=null;
mxEditor.prototype.graphRenderHint=null;
mxEditor.prototype.toolbar=null;
mxEditor.prototype.status=null;
mxEditor.prototype.popupHandler=null;
mxEditor.prototype.undoManager=null;
mxEditor.prototype.keyHandler=null;

mxEditor.prototype.actions=null;
mxEditor.prototype.dblClickAction='edit';
mxEditor.prototype.swimlaneRequired=false;
mxEditor.prototype.disableContextMenu=true;
mxEditor.prototype.extendParentOnAddVertex=true;

mxEditor.prototype.insertFunction=null;
mxEditor.prototype.forcedInserting=false;
mxEditor.prototype.templates=null;
mxEditor.prototype.defaultEdge=null;
mxEditor.prototype.defaultEdgeStyle=null;
mxEditor.prototype.defaultGroup=null;
mxEditor.prototype.groupBorderSize=null;

mxEditor.prototype.filename=null;
mxEditor.prototype.linefeed='&#xa;';
mxEditor.prototype.postParameterName='xml';
mxEditor.prototype.escapePostData=false;
mxEditor.prototype.urlPost=null;
mxEditor.prototype.urlImage=null;
mxEditor.prototype.urlInit=null;
mxEditor.prototype.urlNotify=null;
mxEditor.prototype.urlPoll=null;

mxEditor.prototype.horizontalFlow=false;
mxEditor.prototype.layoutDiagram=false;
mxEditor.prototype.swimlaneSpacing=0;
mxEditor.prototype.maintainSwimlanes=false;
mxEditor.prototype.layoutSwimlanes=false;

mxEditor.prototype.cycleAttributeValues=null;
mxEditor.prototype.cycleAttributeIndex=0;
mxEditor.prototype.cycleAttributeName='fillColor';

mxEditor.prototype.helpWindowImage=null;
mxEditor.prototype.urlHelp=null;
mxEditor.prototype.tasksWindowImage=null;
mxEditor.prototype.tasksTop=20;
mxEditor.prototype.helpWidth=300;
mxEditor.prototype.helpHeight=260;
mxEditor.prototype.propertiesWidth=240;
mxEditor.prototype.propertiesHeight=null;
mxEditor.prototype.movePropertiesDialog=false;

mxEditor.prototype.autoSaving=false;
mxEditor.prototype.validating=false;
mxEditor.prototype.modified=false;
mxEditor.prototype.autoSaveDelay=10;
mxEditor.prototype.autoSaveThrottle=2;
mxEditor.prototype.autoSaveThreshold=5;
mxEditor.prototype.ignoredChanges=0;
mxEditor.prototype.lastSnapshot=0;
mxEditor.prototype.addActions=function()
{
this.addAction('save',function(editor)
{
editor.save();
});
this.addAction('print',function(editor)
{
mxUtils.print(editor.graph);
});
this.addAction('preview',function(editor)
{
mxUtils.show(editor.graph);
});
this.addAction('exportImage',function(editor)
{
if(mxClient.IS_LOCAL)
{
editor.execute('preview');
}
else
{
var url=editor.getUrlImage();
if(url!=null)
{
var enc=new mxCodec();
var node=enc.encode(editor.graph.getView());
var xml=mxUtils.getXml(node,'\n');
mxUtils.submit(url,editor.postParameterName+'='+xml);
}
else
{
mxUtils.alert(mxResources.get('notAvailable'));
}
}
});
this.addAction('refresh',function(editor)
{
editor.graph.refresh();
});
this.addAction('cut',function(editor)
{
mxClipboard.cut(editor.graph);
});
this.addAction('copy',function(editor)
{
mxClipboard.copy(editor.graph);
});
this.addAction('paste',function(editor)
{
mxClipboard.paste(editor.graph);
});
this.addAction('delete',function(editor)
{
if(editor.graph.isEnabled())
{
editor.graph.remove();
}
});
this.addAction('group',function(editor)
{
if(editor.graph.isEnabled())
{
editor.group();
}
});
this.addAction('ungroup',function(editor)
{
if(editor.graph.isEnabled())
{
editor.graph.ungroup();
}
});
this.addAction('removeFromParent',function(editor)
{
if(editor.graph.isEnabled())
{
editor.graph.removeFromParent();
}
});
this.addAction('undo',function(editor)
{
if(editor.graph.isEnabled())
{
editor.undo();
}
});
this.addAction('redo',function(editor)
{
if(editor.graph.isEnabled())
{
editor.redo();
}
});
this.addAction('zoomIn',function(editor)
{
editor.graph.zoomIn();
});
this.addAction('zoomOut',function(editor)
{
editor.graph.zoomOut();
});
this.addAction('actualSize',function(editor)
{
editor.graph.zoomActual();
});
this.addAction('fit',function(editor)
{
editor.graph.fit();
});
this.addAction('showProperties',function(editor,cell)
{
editor.showProperties(cell);
});
this.addAction('selectAll',function(editor)
{
if(editor.graph.isEnabled())
{
editor.graph.selectAll();
}
});
this.addAction('selectNone',function(editor)
{
if(editor.graph.isEnabled())
{
editor.graph.clearSelection();
}
});
this.addAction('selectVertices',function(editor)
{
if(editor.graph.isEnabled())
{
editor.graph.selectVertices();
}
});
this.addAction('selectEdges',function(editor)
{
if(editor.graph.isEnabled())
{
editor.graph.selectEdges();
}
});
this.addAction('edit',function(editor,cell)
{
if(editor.graph.isEnabled())
{
editor.graph.edit(cell);
}
});
this.addAction('toBack',function(editor,cell)
{
editor.graph.toBack();
});
this.addAction('toFront',function(editor,cell)
{
editor.graph.toFront();
});
this.addAction('enterGroup',function(editor,cell)
{
editor.graph.enterGroup(cell);
});
this.addAction('exitGroup',function(editor)
{
editor.graph.exitGroup();
});
this.addAction('home',function(editor)
{
editor.graph.home();
});
this.addAction('selectPrevious',function(editor)
{
if(editor.graph.isEnabled())
{
editor.graph.selectPrevious();
}
});
this.addAction('selectNext',function(editor)
{
if(editor.graph.isEnabled())
{
editor.graph.selectNext();
}
});
this.addAction('selectParent',function(editor)
{
if(editor.graph.isEnabled())
{
editor.graph.selectParent();
}
});
this.addAction('selectChild',function(editor)
{
if(editor.graph.isEnabled())
{
editor.graph.selectChild();
}
});
this.addAction('collapse',function(editor)
{
if(editor.graph.isEnabled())
{
editor.graph.collapse();
}
});
this.addAction('collapseAll',function(editor)
{
if(editor.graph.isEnabled())
{
var cells=editor.graph.getChildVertices();
editor.graph.collapse(cells);
}
});
this.addAction('expand',function(editor)
{
if(editor.graph.isEnabled())
{
editor.graph.expand();
}
});
this.addAction('expandAll',function(editor)
{
if(editor.graph.isEnabled())
{
var cells=editor.graph.getChildVertices();
editor.graph.expand(cells);
}
});
this.addAction('bold',function(editor)
{
editor.graph.toggleCellStyleFlags(mxConstants.STYLE_FONTSTYLE,mxConstants.FONT_BOLD);
});
this.addAction('italic',function(editor)
{
editor.graph.toggleCellStyleFlags(mxConstants.STYLE_FONTSTYLE,mxConstants.FONT_ITALIC);
});
this.addAction('underline',function(editor)
{
editor.graph.toggleCellStyleFlags(mxConstants.STYLE_FONTSTYLE,mxConstants.FONT_UNDERLINE);
});
this.addAction('shadow',function(editor)
{
editor.graph.toggleCellStyleFlags(mxConstants.STYLE_FONTSTYLE,mxConstants.FONT_SHADOW);
});
this.addAction('alignCellsLeft',function(editor)
{
editor.graph.alignCells(mxConstants.ALIGN_LEFT);
});
this.addAction('alignCellsCenter',function(editor)
{
editor.graph.alignCells(mxConstants.ALIGN_CENTER);
});
this.addAction('alignCellsRight',function(editor)
{
editor.graph.alignCells(mxConstants.ALIGN_RIGHT);
});
this.addAction('alignCellsTop',function(editor)
{
editor.graph.alignCells(mxConstants.ALIGN_TOP);
});
this.addAction('alignCellsMiddle',function(editor)
{
editor.graph.alignCells(mxConstants.ALIGN_MIDDLE);
});
this.addAction('alignCellsBottom',function(editor)
{
editor.graph.alignCells(mxConstants.ALIGN_BOTTOM);
});
this.addAction('alignFontLeft',function(editor)
{
editor.graph.setCellStyles(mxConstants.STYLE_ALIGN,mxConstants.ALIGN_LEFT);
});
this.addAction('alignFontCenter',function(editor)
{
editor.graph.setCellStyles(mxConstants.STYLE_ALIGN,mxConstants.ALIGN_CENTER);
});
this.addAction('alignFontRight',function(editor)
{
editor.graph.setCellStyles(mxConstants.STYLE_ALIGN,mxConstants.ALIGN_RIGHT);
});
this.addAction('alignFontTop',function(editor)
{
editor.graph.setCellStyles(mxConstants.STYLE_VERTICAL_ALIGN,mxConstants.ALIGN_TOP);
});
this.addAction('alignFontMiddle',function(editor)
{
editor.graph.setCellStyles(mxConstants.STYLE_VERTICAL_ALIGN,mxConstants.ALIGN_MIDDLE);
});
this.addAction('alignFontBottom',function(editor)
{
editor.graph.setCellStyles(mxConstants.STYLE_VERTICAL_ALIGN,mxConstants.ALIGN_BOTTOM);
});
this.addAction('zoom',function(editor)
{
var current=editor.graph.getView().scale*100;
var scale=parseFloat(mxUtils.prompt(mxResources.get(editor.askZoomResource)||editor.askZoomResource,current))/100;
if(!isNaN(scale))
{
editor.graph.getView().setScale(scale);
}
});
this.addAction('toggleTasks',function(editor)
{
if(editor.tasks!=null)
{
editor.tasks.setVisible(!editor.tasks.isVisible());
}
else
{
editor.showTasks();
}
});
this.addAction('toggleHelp',function(editor)
{
if(editor.help!=null)
{
editor.help.setVisible(!editor.help.isVisible());
}
else
{
editor.showHelp();
}
});
this.addAction('toggleOutline',function(editor)
{
if(editor.outline==null)
{
editor.showOutline();
}
else
{
editor.outline.setVisible(!editor.outline.isVisible());
}
});
this.addAction('toggleConsole',function(editor)
{
mxLog.setVisible(!mxLog.isVisible());
});
};
mxEditor.prototype.createSession=function()
{
var session=null;
var self=this;



var sessionChanged=function(session)
{
self.dispatchEvent('session',self,session);
};
session=this.connect(this.urlInit,this.urlPoll,this.urlNotify,sessionChanged);
session.addListener('dispatched',function(sender,changes)
{
if(changes.length<10)
{
mxUtils.animateChanges(self.graph,changes);
}
});
session.addListener('connect',function(sender,changes)
{
self.resetHistory();
});
};
mxEditor.prototype.configure=function(node)
{
if(node!=null)
{

var dec=new mxCodec(node.ownerDocument);
try
{

this.isConfiguring=true;
dec.decode(node,this);
}
finally
{
this.isConfiguring=null;
}

this.resetHistory();
}
};
mxEditor.prototype.resetFirstTime=function()
{
document.cookie=
'mxgraph=seen; expires=Fri, 27 Jul 2001 02:47:11 UTC; path=/';
};
mxEditor.prototype.resetHistory=function()
{
this.lastSnapshot=new Date().getTime();
this.undoManager.reset();
this.ignoredChanges=0;
this.modified=false;
};
mxEditor.prototype.addAction=function(actionname,funct)
{
this.actions[actionname]=funct;
};
mxEditor.prototype.execute=function(actionname,cell)
{
var action=this.actions[actionname];
if(action!=null)
{
try
{

var args=arguments;
args[0]=this;
action.apply(this,args);
}
catch(err)
{
mxUtils.error('Cannot execute '+actionname+': '+err.message,280,true);
throw err;
}
}
else
{
mxUtils.error('Cannot find action '+actionname,280,true);
}
};
mxEditor.prototype.createGraph=function()
{
var graph=new mxGraph(null,null,this.graphRenderHint);
graph.setTooltips(true);
graph.setPanning(true);


this.installDblClickHandler(graph);

this.installResizeHandler(graph);
this.installUndoHandler(graph);
this.installDrillHandler(graph);

this.installChangeHandler(graph);


this.installAddHandler(graph);
this.installLayoutHandler(graph);


this.installInsertHandler(graph);

var self=this;
graph.panningHandler.factoryMethod=function(menu,cell,evt)
{
return self.createPopupMenu(menu,cell,evt);
};

graph.connectionHandler.factoryMethod=function(source,target)
{
return self.createEdge(source,target)
};
return graph;
};
mxEditor.prototype.setGraphContainer=function(container)
{
if(this.graph.container==null)
{

this.graph.init(container);

this.rubberband=new mxRubberband(this.graph);
if(this.disableContextMenu)
{
mxEvent.disableContextMenu(container);
}
if(mxClient.IS_IE)
{
new mxDivResizer(container);
}
}
};
mxEditor.prototype.installAddHandler=function(graph)
{
var self=this;
var offset=50;
var listener=function(sender,cells)
{
for(var i=0;i<cells.length;i++)
{

var cell=cells[i];
if(self.maintainSwimlanes&&graph.isSwimlane(cell))
{
var model=graph.getModel();
var geo=model.getGeometry(cell);


var param=(self.horizontalFlow)?geo.width:geo.height;
if(param==null||param==0)
{


param=(self.horizontalFlow)?graph.container.offsetWidth-10:
graph.container.offsetHeight;
param-=offset;
}

var parent=graph.getDefaultParent();
var childCount=model.getChildCount(parent);
for(var i=0;i<childCount;i++)
{
var child=model.getChildAt(parent,i);
geo=model.getGeometry(child);
if(cell!=child&&graph.isSwimlane(child))
{

param=(self.horizontalFlow)?geo.width:geo.height;
break;
}
}
geo=model.getGeometry(cell);
if(geo!=null)
{
if(self.horizontalFlow)
{
geo.width=param;
}
else
{
geo.height=param;
}
}
}
}
};
graph.addListener('add',listener);
};
mxEditor.prototype.installDblClickHandler=function(graph)
{
var self=this;
graph.dblClick=function(evt,cell)
{
graph.dispatchEvent('dblclick',graph,evt,cell);
if(cell!=null&&graph.isEnabled())
{
self.execute(self.dblClickAction,cell);
}
}
};
mxEditor.prototype.installUndoHandler=function(graph)
{
var self=this;
var listener=function(sender,edit)
{
self.undoManager.undoableEditHappened(edit);
};
graph.getModel().addListener('undo',listener);
graph.getView().addListener('undo',listener);
listener=function(sender,edit)
{
graph.selectCellsForEdit(edit);
};
this.undoManager.addListener('undo',listener);
this.undoManager.addListener('redo',listener);
};
mxEditor.prototype.installDrillHandler=function(graph)
{
var self=this;
var listener=function(sender)
{
self.dispatchEvent('root',self);
};
graph.getView().addListener('down',listener);
graph.getView().addListener('up',listener);
};
mxEditor.prototype.installResizeHandler=function(graph)
{
var old=graph.resizeCells;
var self=this;
graph.resizeCells=function(cells,boundsArray)
{
if(self.maintainSwimlanes)
{
var model=graph.getModel();
var visited=new Array();
var cellCount=cells.length;
for(var i=0;i<cellCount;i++)
{


if(graph.isSwimlane(cells[i]))
{
var parent=model.getParent(cells[i]);
var parentId=mxCellPath.create(parent);
if(parent!=null&&visited[parentId]==null)
{
visited[parentId]=parent;


var geo=model.getGeometry(cells[i]);
var param=(self.horizontalFlow)?boundsArray[i].width:
boundsArray[i].height;
var childCount=model.getChildCount(parent);
for(var j=0;j<childCount;j++)
{
var child=model.getChildAt(parent,j);
if(graph.isSwimlane(child)&&child!=cells[i])
{

geo=model.getGeometry(child);
if(geo!=null)
{
geo=geo.clone();
if(self.horizontalFlow)
{
geo.width=param;
}
else
{
geo.height=param;
}
cells.push(child);
boundsArray.push(geo);
}
}
}
}
}
}
}
old.apply(this,arguments);
};
};
mxEditor.prototype.installChangeHandler=function(graph)
{

var self=this;
var listener=function(sender,changes)
{
self.modified=true;

if(self.validating==true)
{
graph.validate();
}

if(!this.isConfiguring)
{
if(self.autoSaving)
{
self.autosave(changes);
}
}
for(var i=0;i<changes.length;i++)
{
var change=changes[i];
if(change.constructor==mxRootChange||(change.constructor==mxValueChange&&change.cell==self.graph.model.root)||(change.constructor==mxCellAttributeChange&&change.cell==self.graph.model.root))
{
self.dispatchEvent('root',self);
break;
}
}
};
graph.getModel().addListener('change',listener);
};
mxEditor.prototype.installLayoutHandler=function(graph)
{
var old=graph.getLayout;
var self=this;
graph.getLayout=function(cell)
{
var layout=old.apply(this,arguments);
var model=this.getModel();
if(layout==null&&model.getParent(cell)!=null)
{


if(self.layoutSwimlanes&&graph.isSwimlane(cell))
{
if(self.swimlaneLayout==null)
{
self.swimlaneLayout=self.createSwimlaneLayout();
}
layout=self.swimlaneLayout;
}


else if(self.layoutDiagram&&(graph.isValidRoot(cell)||model.getParent(model.getParent(cell))==null))
{
if(self.diagramLayout==null)
{
self.diagramLayout=self.createDiagramLayout();
}
layout=self.diagramLayout;
}
}
return layout;
};
};
mxEditor.prototype.installInsertHandler=function(graph)
{
var self=this;
var insertHandler=
{
mouseDown:function(evt,cell,index)
{
if(self.insertFunction!=null&&!mxEvent.isPopupTrigger(evt)&&(self.forcedInserting||(cell==null&&index==null)))
{
self.graph.clearSelection();
self.insertFunction(evt,cell);

this.isActive=true;
mxEvent.consume(evt);
}
},mouseMove:function(evt,cell)
{
if(this.isActive)
{
mxEvent.consume(evt);
}
},mouseUp:function(evt,cell)
{
if(this.isActive)
{
this.isActive=false;
mxEvent.consume(evt);
}
}
};
graph.addGraphListener(insertHandler);
};
mxEditor.prototype.createDiagramLayout=function()
{
var gs=this.graph.gridSize;
var layout=new mxStackLayout(this.graph,!this.horizontalFlow,this.swimlaneSpacing,2*gs,2*gs);
layout.isVertexIgnored=function(cell)
{
return!layout.graph.isSwimlane(cell);
};
return layout;
};
mxEditor.prototype.createSwimlaneLayout=function()
{
return new mxCompactTreeLayout(this.graph,this.horizontalFlow);
};
mxEditor.prototype.createToolbar=function()
{
return new mxDefaultToolbar(null,this);
};
mxEditor.prototype.setToolbarContainer=function(container)
{
this.toolbar.init(container);
if(mxClient.IS_IE)
{
new mxDivResizer(container);
}
};
mxEditor.prototype.setStatusContainer=function(container)
{
if(this.status==null)
{
this.status=container;

var self=this;
this.addListener('save',function(sender)
{
var tstamp=new Date().toLocaleString();
self.setStatus((mxResources.get(self.lastSavedResource)||self.lastSavedResource)+': '+tstamp);
});

this.addListener('open',function(sender)
{
self.setStatus((mxResources.get(self.currentFileResource)||self.currentFileResource)+': '+self.filename);
});
if(mxClient.IS_IE)
{
new mxDivResizer(container);
}
}
};
mxEditor.prototype.setStatus=function(message)
{
if(this.status!=null&&message!=null)
{
this.status.innerHTML=message;
}
};
mxEditor.prototype.setTitleContainer=function(container)
{
var self=this;
this.addListener('root',function(sender)
{
container.innerHTML=self.getTitle();
});
if(mxClient.IS_IE)
{
new mxDivResizer(container);
}
};
mxEditor.prototype.treeLayout=function(cell,horizontal)
{
if(cell!=null)
{
var layout=new mxCompactTreeLayout(this.graph,horizontal);
layout.execute(cell);
}
};
mxEditor.prototype.getTitle=function()
{
var title='';
var graph=this.graph;
var cell=graph.getCurrentRoot();
while(cell!=null&&graph.getModel().getParent(graph.getModel().getParent(cell))!=null)
{
if(graph.isValidRoot(cell))
{
title=' > '+graph.convertValueToString(cell)+title;
}
cell=graph.getModel().getParent(cell);
}
var prefix=this.getRootTitle();
return prefix+title;
};
mxEditor.prototype.getRootTitle=function()
{
var root=this.graph.getModel().getRoot();
return this.graph.convertValueToString(root);
};
mxEditor.prototype.undo=function()
{
this.undoManager.undo();
};
mxEditor.prototype.redo=function()
{
this.undoManager.redo();
};
mxEditor.prototype.group=function()
{
var border=(this.groupBorderSize!=null)?this.groupBorderSize:
this.graph.gridSize;
this.graph.group(this.createGroup(),border);
};
mxEditor.prototype.createGroup=function()
{
var model=this.graph.getModel();
return model.cloneCell(this.defaultGroup);
};
mxEditor.prototype.open=function(filename)
{
if(filename!=null)
{
this.dispatchEvent('beforeOpen',this,filename);
var e=null;
try
{
var xml=mxUtils.load(filename).getXml();
this.readGraphModel(xml.documentElement);
this.filename=filename;
this.dispatchEvent('open',this,filename);
}
catch(ex)
{
mxUtils.error('Cannot open '+filename+': '+ex.message,280,true);
e=ex;
}
this.dispatchEvent('afterOpen',this,filename,e);
}
};
mxEditor.prototype.readGraphModel=function(node)
{
var dec=new mxCodec(node.ownerDocument);
dec.decode(node,this.graph.getModel());
this.resetHistory();
};
mxEditor.prototype.save=function(isAutomatic,linefeed,url)
{
if(isAutomatic==null||isAutomatic==this.autoSaving)
{
this.dispatchEvent('beforeSave',this,isAutomatic);
this.lastSnapshot=new Date().getTime();
this.ignoredChanges=0;
this.modified=false;
var e=null;
try
{
url=url||this.getUrlPost(isAutomatic);
if(url!=null&&url.length>0)
{
var data=this.writeGraphModel();
this.postDiagram(url,data);
}
else if(!isAutomatic)
{

var enc=new mxCodec();
var node=enc.encode(this.graph.getModel());
var xml=mxUtils.getPrettyXml(node);
mxUtils.popup(xml);
}
this.dispatchEvent('save',this,isAutomatic,url);
}
catch(ex)
{
e=ex;
}
this.dispatchEvent('afterSave',this,isAutomatic,e);
}
};
mxEditor.prototype.postDiagram=function(url,data)
{
if(this.escapePostData)
{
data=encodeURIComponent(data);
}
var self=this;
mxUtils.post(url,this.postParameterName+'='+data,function(req)
{
self.dispatchEvent('post',self,req,url,data);
});
};
mxEditor.prototype.writeGraphModel=function()
{
var enc=new mxCodec();
var node=enc.encode(this.graph.getModel());
return mxUtils.getXml(node,this.linefeed);
}
mxEditor.prototype.getUrlPost=function(isAutomatic)
{
var url=this.urlPost;
if(url!=null&&url.length>0)
{
if(isAutomatic)
{
url+='?draft=true';
}
}
return url;
};
mxEditor.prototype.getUrlImage=function()
{
return this.urlImage;
};
mxEditor.prototype.autosave=function(changes)
{
var now=new Date().getTime();
var dt=(now-this.lastSnapshot)/1000;
if(dt>this.autoSaveDelay||(this.ignoredChanges>=this.autoSaveThreshold&&dt>this.autoSaveThrottle))
{
this.lastSnapshot=now;
this.ignoredChanges=1;
this.save(true);
}
else
{
this.ignoredChanges++;
}
};
mxEditor.prototype.connect=function(urlInit,urlPoll,urlNotify,onChange)
{
var session=null;
if(!mxClient.IS_LOCAL)
{
var session=new mxSession(this.graph.getModel(),urlInit,urlPoll,urlNotify);


var self=this;
session.addListener('receive',function(sender,node)
{
if(node.nodeName=='mxGraphModel')
{
self.readGraphModel(node);
}
});

session.addListener('disconnect',onChange);
session.addListener('connect',onChange);
session.addListener('notify',onChange);
session.addListener('get',onChange);
session.start();
}
return session;
};
mxEditor.prototype.swapStyles=function(first,second)
{
var style=this.graph.getStylesheet().styles[second];
this.graph.getView().getStylesheet().putCellStyle(second,this.graph.getStylesheet().styles[first]);
this.graph.getStylesheet().putCellStyle(first,style);
this.graph.refresh();
};
mxEditor.prototype.showProperties=function(cell)
{
cell=cell||this.graph.getSelectionCell();


if(cell==null)
{
cell=this.graph.getCurrentRoot();
if(cell==null)
{
cell=this.graph.getModel().getRoot();
}
}
if(cell!=null)
{

this.graph.editor.stopEditing(true);
var offset=mxUtils.getOffset(this.graph.container);
var x=offset.x+10;
var y=offset.y;
if(this.properties!=null&&!this.movePropertiesDialog)
{
x=this.properties.getX();
y=this.properties.getY();
}

else
{
var bounds=this.graph.getCellBounds(cell);
if(bounds!=null)
{
x+=bounds.x+Math.min(200,bounds.width);
y+=bounds.y;
}
}


this.hideProperties();
var node=this.createProperties(cell);
if(node!=null)
{


this.properties=new mxWindow(mxResources.get(this.propertiesResource)||this.propertiesResource,node,x,y,this.propertiesWidth,this.propertiesHeight,false);
this.properties.setVisible(true);
}
}
};
mxEditor.prototype.isPropertiesVisible=function()
{
return this.properties!=null;
};
mxEditor.prototype.createProperties=function(cell)
{
var model=this.graph.getModel();
var value=model.getValue(cell);
if(mxUtils.isNode(value))
{

var form=new mxForm('properties');
var id=form.addText('ID',cell.getId());
id.setAttribute('readonly','true');
var geo=null;
var yField=null;
var xField=null;
var widthField=null;
var heightField=null;
if(model.isVertex(cell))
{
geo=model.getGeometry(cell);
if(geo!=null)
{
yField=form.addText('top',geo.y);
xField=form.addText('left',geo.x);
widthField=form.addText('width',geo.width);
heightField=form.addText('height',geo.height);
}
}
var tmp=model.getStyle(cell);
var style=form.addText('Style',tmp||'');

var attrs=value.attributes;
var texts=new Array();
for(var i=0;i<attrs.length;i++)
{

var val=attrs[i].nodeValue;
texts[i]=form.addTextarea(attrs[i].nodeName,val,(attrs[i].nodeName=='label')?4:2);
}


var self=this;


var okFunction=function()
{
self.hideProperties();

model.beginUpdate();
try
{
if(geo!=null)
{
geo=geo.clone();
geo.x=parseFloat(xField.value);
geo.y=parseFloat(yField.value);
geo.width=parseFloat(widthField.value);
geo.height=parseFloat(heightField.value);
model.setGeometry(cell,geo);
}
if(style.value.length>0)
{
model.setStyle(cell,style.value);
}
else
{
model.setStyle(cell,null);
}



for(var i=0;i<attrs.length;i++)
{
var edit=new mxCellAttributeChange(cell,attrs[i].nodeName,texts[i].value);
model.execute(edit);
}



if(self.graph.isUpdateSize(cell))
{
self.graph.updateSize(cell);
}
}
finally
{
model.endUpdate();
}
}

var cancelFunction=function()
{
self.hideProperties();
}
form.addButtons(okFunction,cancelFunction);
return form.table;
}
return null;
};
mxEditor.prototype.hideProperties=function()
{
if(this.properties!=null)
{
this.properties.destroy();
this.properties=null;
}
};
mxEditor.prototype.showTasks=function(tasks)
{
if(this.tasks==null)
{
var div=document.createElement('div');
div.style.padding='4px';
div.style.paddingLeft='20px';
var w=document.body.clientWidth;
var wnd=new mxWindow(mxResources.get(this.tasksResource)||this.tasksResource,div,w-220,this.tasksTop,200);
wnd.setClosable(true);
wnd.destroyOnClose=false;


var self=this;
var funct=function(sender)
{
mxUtils.release(div);
div.innerHTML='';
self.createTasks(div);
};
this.graph.getModel().addListener('change',funct);
this.graph.selection.addListener('change',funct);
this.graph.addListener('root',funct);
if(this.tasksWindowImage!=null)
{
wnd.setImage(this.tasksWindowImage);
}
this.tasks=wnd;
this.createTasks(div);
}
this.tasks.setVisible(true);
};
mxEditor.prototype.refreshTasks=function(div)
{
if(this.tasks!=null)
{
var div=this.tasks.content;
mxUtils.release(div);
div.innerHTML='';
this.createTasks(div);
}
};
mxEditor.prototype.createTasks=function(div)
{
}
mxEditor.prototype.showHelp=function(tasks)
{
if(this.help==null)
{
var frame=document.createElement('iframe');
frame.setAttribute('src',mxResources.get('urlHelp')||this.urlHelp);
frame.setAttribute('height','100%');
frame.setAttribute('width','100%');
frame.setAttribute('frameborder','0');
frame.style.backgroundColor='white';
var w=document.body.clientWidth;
var h=(document.body.clientHeight||document.documentElement.clientHeight);
var wnd=new mxWindow(mxResources.get(this.helpResource)||this.helpResource,frame,(w-this.helpWidth)/2,(h-this.helpHeight)/3,this.helpWidth,this.helpHeight);
wnd.setMaximizable(true);
wnd.setClosable(true);
wnd.destroyOnClose=false;
wnd.setSizable(true);
if(this.helpWindowImage!=null)
{
wnd.setImage(this.helpWindowImage);
}
if(mxClient.IS_NS)
{
var handler=function(sender)
{
var h=wnd.div.offsetHeight;
frame.setAttribute('height',(h-26)+'px');
};
wnd.addListener('resize',handler);
wnd.addListener('maximize',handler);
wnd.addListener('normalize',handler);
wnd.addListener('show',handler);
}
this.help=wnd;
}
this.help.setVisible(true);
};
mxEditor.prototype.showOutline=function()
{
var create=this.outline==null;
if(create)
{
var div=document.createElement('div');
div.style.width="100%";
div.style.height="100%";
div.style.background='white';
var wnd=new mxWindow(mxResources.get(this.outlineResource)||this.outlineResource,div,600,480,200,200,false);

var outline=new mxOutline(this.graph,div);
wnd.setClosable(true);
wnd.setSizable(true);
wnd.destroyOnClose=false;
wnd.addListener('resizeend',function()
{
outline.update();
});
this.outline=wnd;
this.outline.outline=outline;
}
this.outline.setVisible(true);
this.outline.outline.refresh();
};
mxEditor.prototype.setMode=function(modename)
{
if(modename=='select')
{
this.graph.panningHandler.useLeftButtonForPanning=false;
this.graph.connectionHandler.setEnabled(false);
}
else if(modename=='connect')
{
this.graph.panningHandler.useLeftButtonForPanning=false;
this.graph.connectionHandler.setEnabled(true);
}
else if(modename=='pan')
{
this.graph.panningHandler.useLeftButtonForPanning=true;
this.graph.connectionHandler.setEnabled(false);
}
};
mxEditor.prototype.createPopupMenu=function(menu,cell,evt)
{
this.popupHandler.createMenu(this,menu,cell,evt);
};
mxEditor.prototype.createEdge=function(source,target)
{
var e=null;
if(this.defaultEdge!=null)
{
var model=this.graph.getModel();
e=model.cloneCell(this.defaultEdge);
}
else
{
e=new mxCell('');
e.setEdge(true);
}
var style=this.getEdgeStyle();
if(style!=null)
{
e.setStyle(style);
}
return e;
};
mxEditor.prototype.getEdgeStyle=function()
{
return this.defaultEdgeStyle;
}
mxEditor.prototype.consumeCycleAttribute=function(cell)
{
return(this.cycleAttributeValues!=null&&this.cycleAttributeValues.length>0&&this.graph.isSwimlane(cell))?this.cycleAttributeValues[this.cycleAttributeIndex++%this.cycleAttributeValues.length]:null;
};
mxEditor.prototype.cycleAttribute=function(cell)
{
if(this.cycleAttributeName!=null)
{
var value=this.consumeCycleAttribute(cell);
if(value!=null)
{
cell.setStyle(cell.getStyle()+';'+this.cycleAttributeName+'='+value);
}
}
};
mxEditor.prototype.addVertex=function(parent,vertex,x,y)
{
var model=this.graph.getModel();
while(parent!=null&&!this.graph.isValidDropTarget(parent))
{
parent=model.getParent(parent);
}
parent=(parent!=null)?parent:this.graph.getSwimlaneAt(x,y);
var scale=this.graph.getView().scale;
var geo=model.getGeometry(vertex);
var pgeo=model.getGeometry(parent);
if(this.graph.isSwimlane(vertex)&&!this.graph.swimlaneNesting)
{
parent=null;
}
else if(parent==null&&this.swimlaneRequired)
{
return null;
}
else if(parent!=null&&pgeo!=null)
{
var state=this.graph.getView().getState(parent);
if(state!=null)
{
x-=state.origin.x*scale;
y-=state.origin.y*scale;
if(this.graph.isConstrainedMoving)
{
var width=geo.width;
var height=geo.height;
var tmp=state.x+state.width;
if(x+width>tmp)
{
x-=x+width-tmp;
}
tmp=state.y+state.height;
if(y+height>tmp)
{
y-=y+height-tmp;
}
}
}
else if(pgeo!=null)
{
x-=pgeo.x*scale;
y-=pgeo.y*scale;
}
}
geo=geo.clone();
geo.x=this.graph.snap(x/scale-this.graph.getView().translate.x-this.graph.gridSize/2);
geo.y=this.graph.snap(y/scale-this.graph.getView().translate.y-this.graph.gridSize/2);
vertex.setGeometry(geo);
if(parent==null)
{
parent=this.graph.getDefaultParent();
}
this.cycleAttribute(vertex);
this.dispatchEvent('beforeAddVertex',this,vertex,parent);
model.beginUpdate();
try
{
vertex=this.graph.addCell(vertex,parent);
if(vertex!=null)
{
this.graph.keepInside([vertex]);
if(this.extendParentOnAddVertex)
{
this.graph.extendParent(vertex);
}
this.dispatchEvent('addVertex',this,vertex);
}
}
finally
{
model.endUpdate();
}
if(vertex!=null)
{
this.graph.setSelectionCell(vertex);
this.graph.scrollCellToVisible(vertex);
this.dispatchEvent('afterAddVertex',this,vertex);
}
return vertex;
};
mxEditor.prototype.destroy=function()
{
if(!this.destroyed)
{
this.destroyed=true;
if(this.tasks!=null)
{
this.tasks.destroy();
}
if(this.outline!=null)
{
this.outline.destroy();
}
if(this.properties!=null)
{
this.properties.destroy();
}
if(this.keyHandler!=null)
{
this.keyHandler.destroy();
}
if(this.rubberband!=null)
{
this.rubberband.destroy();
}
if(this.toolbar!=null)
{
this.toolbar.destroy();
}
if(this.graph!=null)
{
this.graph.destroy();
}
this.status=null;
this.templates=null;
}
};
}

var mxCodecRegistry=
{
codecs:new Array(),
register:function(codec)
{
var name=mxUtils.getFunctionName(codec.template.constructor);
mxCodecRegistry.codecs[name]=codec;
},
getCodec:function(ctor)
{
var codec=null;
if(ctor!=null)
{
var name=mxUtils.getFunctionName(ctor);
codec=mxCodecRegistry.codecs[name];

if(codec==null)
{
try
{
codec=new mxObjectCodec(new ctor());
mxCodecRegistry.register(codec);
}
catch(e)
{
}
}
}
return codec;
}
};

{
function mxCodec(document)
{
this.document=document||mxUtils.createXmlDocument();
this.objects=new Array();
};
mxCodec.prototype.document=null;
mxCodec.prototype.objects=null;
mxCodec.prototype.encodeDefaults=false;
mxCodec.prototype.putObject=function(id,obj)
{
this.objects[id]=obj;
return obj;
};
mxCodec.prototype.getObject=function(id)
{
var obj=null;
if(id!=null)
{
obj=this.objects[id];
if(obj==null)
{
obj=this.lookup(id);
if(obj==null)
{
var node=this.getElementById(id);
if(node!=null)
{
obj=this.decode(node);
}
}
}
}
return obj;
};
mxCodec.prototype.lookup=function(id)
{
return null;
}
mxCodec.prototype.getElementById=function(id,attr)
{
attr=attr||'id';
var expr='//*[@'+attr+'=\''+id+'\']';
return mxUtils.selectSingleNode(this.document,expr);
};
mxCodec.prototype.getId=function(obj)
{
var id=null;
if(obj!=null)
{
id=this.reference(obj);
if(id==null&&obj.constructor==mxCell)
{
id=obj.getId();
if(id==null)
{
id=mxCellPath.create(obj);
if(id.length==0)
{
id='root';
}
}
}
}
return id;
};
mxCodec.prototype.reference=function(obj)
{
return null;
};
mxCodec.prototype.encode=function(obj)
{
var node=null;
if(obj!=null&&obj.constructor!=null)
{
var enc=mxCodecRegistry.getCodec(obj.constructor);
if(enc!=null)
{
node=enc.encode(this,obj);
}
else
{
if(isNode(obj))
{
node=(mxClient.IS_IE)?obj.value.cloneNode(true):
this.document.importNode(obj.value,true);
}
else
{
mxLog.warn('mxCodec.encode: No codec for '+mxUtils.getFunctionName(obj.constructor));
}
}
}
return node;
};
mxCodec.prototype.decode=function(node,into)
{
var obj=null;
if(node!=null&&node.nodeType==mxConstants.NODETYPE_ELEMENT)
{
var ctor=null;
try
{
var ctor=eval(node.nodeName);
}
catch(err)
{
}
try
{
var dec=mxCodecRegistry.getCodec(ctor);
if(dec!=null)
{
obj=dec.decode(this,node,into);
}
else
{
obj=node.cloneNode(true);
obj.removeAttribute('as');
}
}
catch(err)
{
mxLog.debug('Cannot decode '+node.nodeName+': '+err.message);
throw err;
}
}
return obj;
};
mxCodec.prototype.encodeCell=function(cell,node,isIncludeChildren)
{
node.appendChild(this.encode(cell));
if(isIncludeChildren==null||isIncludeChildren)
{
var childCount=cell.getChildCount();
for(var i=0;i<childCount;i++)
{
this.encodeCell(cell.getChildAt(i),node);
}
}
};
mxCodec.prototype.decodeCell=function(node,isRestoreStructures)
{
var cell=null;
if(node!=null&&node.nodeType==mxConstants.NODETYPE_ELEMENT)
{


var decoder=mxCodecRegistry.getCodec(mxCell);
cell=decoder.decode(this,node);
if(isRestoreStructures==null||isRestoreStructures)
{
var parent=cell.getParent();
if(parent!=null)
{
parent.insert(cell);
}
var source=cell.getTerminal(true);
if(source!=null)
{
source.insertEdge(cell,true);
}
var target=cell.getTerminal(false);
if(target!=null)
{
target.insertEdge(cell,false);
}
}
}
return cell;
};
mxCodec.prototype.setAttribute=function(node,attribute,value)
{
if(attribute!=null&&value!=null)
{
node.setAttribute(attribute,value);
}
};
}

{
function mxObjectCodec(template,exclude,idrefs,mapping)
{
this.template=template;
this.exclude=exclude||new Array();
this.idrefs=idrefs||new Array();
this.mapping=mapping||new Object();
this.reverse=new Object();
for(var i in this.mapping)
{
this.reverse[this.mapping[i]]=i;
}
};
mxObjectCodec.prototype.template=null;
mxObjectCodec.prototype.exclude=null;
mxObjectCodec.prototype.idrefs=null;
mxObjectCodec.prototype.mapping=null;
mxObjectCodec.prototype.reverse=null;
mxObjectCodec.prototype.getFieldName=function(attributename)
{
if(attributename!=null)
{
var mapped=this.reverse[attributename];
if(mapped!=null)
{
attributename=mapped;
}
}
return attributename;
};
mxObjectCodec.prototype.getAttributeName=function(fieldname)
{
if(fieldname!=null)
{
var mapped=this.mapping[fieldname];
if(mapped!=null)
{
fieldname=mapped;
}
}
return fieldname;
};
mxObjectCodec.prototype.isExcluded=function(obj,attr,value,isWrite)
{
return attr==mxObjectIdentity.FIELD_NAME||mxUtils.indexOf(this.exclude,attr)>=0;
};
mxObjectCodec.prototype.isReference=function(obj,attr,value,isWrite)
{
return mxUtils.indexOf(this.idrefs,attr)>=0;
};
mxObjectCodec.prototype.encode=function(enc,obj)
{
var name=mxUtils.getFunctionName(obj.constructor);
var node=enc.document.createElement(name);
obj=this.beforeEncode(enc,obj,node);
this.encodeObject(enc,obj,node);
return this.afterEncode(enc,obj,node);
};
mxObjectCodec.prototype.encodeObject=function(enc,obj,node)
{
enc.setAttribute(node,'id',enc.getId(obj));
for(var i in obj)
{
var name=i;
var value=obj[name];
if(value!=null&&!this.isExcluded(obj,name,value,true))
{
if(mxUtils.isNumeric(name))
{
name=null;
}
this.encodeValue(enc,obj,name,value,node);
}
}
};
mxObjectCodec.prototype.encodeValue=function(enc,obj,name,value,node)
{
if(value!=null)
{
if(this.isReference(obj,name,value,true))
{
var tmp=enc.getId(value);
if(tmp==null)
{
mxLog.warn('mxObjectCodec.encode: No ID for '+mxUtils.getFunctionName(obj.constructor)+'.'+name+'='+value);
return;
}
value=tmp;
}
var defaultValue=this.template[name];

if(name==null||enc.encodeDefaults||defaultValue!=value)
{
name=this.getAttributeName(name);
this.writeAttribute(enc,obj,name,value,node);
}
}
};
mxObjectCodec.prototype.writeAttribute=function(enc,obj,attr,value,node)
{
if(typeof(value)!='object' )
{
this.writePrimitiveAttribute(enc,obj,attr,value,node);
}
else 
{
this.writeComplexAttribute(enc,obj,attr,value,node);
}
};
mxObjectCodec.prototype.writePrimitiveAttribute=function(enc,obj,attr,value,node)
{
value=this.convertValueToXml(value);
if(attr==null)
{
var child=enc.document.createElement('add');
if(typeof(value)=='function')
{
child.appendChild(enc.document.createTextNode(value));
}
else
{
enc.setAttribute(child,'value',value);
}
node.appendChild(child);
}
else if(typeof(value)!='function')
{
enc.setAttribute(node,attr,value);
}
};
mxObjectCodec.prototype.writeComplexAttribute=function(enc,obj,attr,value,node)
{
var child=enc.encode(value);
if(child!=null)
{
if(attr!=null)
{
child.setAttribute('as',attr);
}
node.appendChild(child);
}
else
{
mxLog.warn('mxObjectCodec.encode: No node for '+mxUtils.getFunctionName(obj.constructor)+'.'+attr+': '+value);
}
};
mxObjectCodec.prototype.convertValueToXml=function(value)
{
if(typeof(value.length)=='undefined'&&(value==true||value==false))
{


value=(value==true)?'1':'0';
}
return value;
};
mxObjectCodec.prototype.convertValueFromXml=function(value)
{
if(mxUtils.isNumeric(value))
{
value=parseFloat(value);
}
return value;
}
mxObjectCodec.prototype.beforeEncode=function(enc,obj,node)
{
return obj;
};
mxObjectCodec.prototype.afterEncode=function(enc,obj,node)
{
return node;
};
mxObjectCodec.prototype.decode=function(dec,node,into)
{
var id=node.getAttribute('id');
var obj=dec.objects[id];
if(obj==null)
{
obj=into||new this.template.constructor();
if(id!=null)
{
dec.putObject(id,obj);
}
}
node=this.beforeDecode(dec,node,obj);
this.decodeNode(dec,node,obj);
return this.afterDecode(dec,node,obj);
};
mxObjectCodec.prototype.decodeNode=function(dec,node,obj)
{
if(node!=null)
{
this.decodeAttributes(dec,node,obj);
this.decodeChildren(dec,node,obj);
}
};
mxObjectCodec.prototype.decodeAttributes=function(dec,node,obj)
{
var type=mxUtils.getFunctionName(obj.constructor);
var isArray=type=='Array';
var attrs=node.attributes;
if(attrs!=null)
{
for(var i=0;i<attrs.length;i++)
{
this.decodeAttribute(dec,attrs[i],obj);
}
}
};
mxObjectCodec.prototype.decodeAttribute=function(dec,attr,obj)
{
var name=attr.nodeName;
if(name!='as'&&name!='id')
{



var value=this.convertValueFromXml(attr.nodeValue);
var fieldname=this.getFieldName(name);
if(this.isReference(obj,fieldname,value,false))
{
var tmp=dec.getObject(value);
if(tmp==null)
{
mxLog.warn('mxObjectCodec.decode: No object for '+mxUtils.getFunctionName(obj.constructor)+'.'+name+'='+value);
return;
}
value=tmp;
}
if(!this.isExcluded(obj,name,value,false))
{
obj[name]=value;
}
}
};
mxObjectCodec.prototype.decodeChildren=function(dec,node,obj)
{
var type=mxUtils.getFunctionName(obj.constructor);
var isArray=type=='Array';
var child=node.firstChild;
while(child!=null)
{
var tmp=child.nextSibling;
if(child.nodeType==mxConstants.NODETYPE_ELEMENT&&!this.processInclude(dec,child,obj))
{
this.decodeChild(dec,child,obj);
}
child=tmp;
}
};
mxObjectCodec.prototype.decodeChild=function(dec,child,obj)
{
var fieldname=this.getFieldName(child.getAttribute('as'));
if(fieldname==null||!this.isExcluded(obj,fieldname,child,false))
{
var value=null;
var template=obj[fieldname];
if(child.nodeName=='add')
{
value=child.getAttribute('value');
if(value==null)
{
value=mxUtils.eval(mxUtils.getTextContent(child));
}
}
else
{
value=dec.decode(child,template);

}
if(value!=null&&value!=template)
{
if(fieldname!=null&&fieldname.length>0)
{
obj[fieldname]=value;
}
else
{
obj.push(value);
}
}
}
};
mxObjectCodec.prototype.processInclude=function(dec,node,into)
{
if(node.nodeName=='include')
{
var name=node.getAttribute('name');
if(name!=null)
{
try
{
var xml=mxUtils.load(name).getDocumentElement();
if(xml!=null)
{
dec.decode(xml,into);
}
}
catch(e)
{
}
}
return true;
}
return false;
};
mxObjectCodec.prototype.beforeDecode=function(dec,node,obj)
{
return node;
};
mxObjectCodec.prototype.afterDecode=function(dec,node,obj)
{
return obj;
};
}

mxCodecRegistry.register(function()
{
var codec=new mxObjectCodec(new mxCell(),['children','edges','states','overlays','mxTransient'],['parent','source','target']);
codec.isExcluded=function(obj,attr,value,isWrite)
{
return mxObjectCodec.prototype.isExcluded.apply(this,arguments)||(isWrite&&attr=='value'&&value.nodeType==mxConstants.NODETYPE_ELEMENT);
};
codec.afterEncode=function(enc,obj,node)
{
if(obj.value!=null&&obj.value.nodeType==mxConstants.NODETYPE_ELEMENT)
{




var tmp=node;
node=(mxClient.IS_IE)?obj.value.cloneNode(true):
enc.document.importNode(obj.value,true);
node.appendChild(tmp);


var id=tmp.getAttribute('id');
node.setAttribute('id',id);
tmp.removeAttribute('id');
}
return node;
};
codec.beforeDecode=function(dec,node,obj)
{
var inner=node;
if(node.nodeName!='mxCell')
{

var tmp=node.getElementsByTagName('mxCell')[0];
if(tmp!=null&&tmp.parentNode==node)
{
mxUtils.removeWhitespace(tmp,true);
mxUtils.removeWhitespace(tmp,false);
tmp.parentNode.removeChild(tmp);
inner=tmp;
}
else
{
inner=null;
}
obj.value=node.cloneNode(true);
var id=obj.value.getAttribute('id');
if(id!=null)
{
obj.setId(id);
obj.value.removeAttribute('id');
}
}
else
{
obj.setId(node.getAttribute('id'));
}


if(inner!=null)
{
for(var i=0;i<this.idrefs.length;i++)
{
var attr=this.idrefs[i];
var ref=inner.getAttribute(attr);
if(ref!=null)
{
inner.removeAttribute(attr);
var object=dec.objects[ref]||dec.lookup(ref);
if(object==null)
{
var element=dec.getElementById(ref);
if(element!=null)
{
var decoder=mxCodecRegistry.codecs[element.nodeName]||this;
object=decoder.decode(dec,element);
}
}
obj[attr]=object;
}
}
}
return inner;
};
return codec;
}());

mxCodecRegistry.register(function()
{
var codec=new mxObjectCodec(new mxGraphModel());
codec.encode=function(enc,obj)
{
var name=mxUtils.getFunctionName(obj.constructor);
var node=enc.document.createElement(name);
var rootNode=enc.document.createElement('root');
enc.encodeCell(obj.getRoot(),rootNode);
node.appendChild(rootNode);
return node;
};
codec.decodeChild=function(dec,child,obj)
{
if(child.nodeName=='root')
{
this.decodeRoot(dec,child,obj);
}
else
{
mxObjectCodec.prototype.decodeChild.apply(this,arguments);
}
};
codec.decodeRoot=function(dec,root,model)
{
var rootCell=null;
var tmp=root.firstChild;
while(tmp!=null)
{
var cell=dec.decodeCell(tmp);
if(cell!=null&&cell.getParent()==null)
{
rootCell=cell;
}
tmp=tmp.nextSibling;
}
if(rootCell!=null)
{
model.setRoot(rootCell);
}
};
return codec;
}());

mxCodecRegistry.register(function()
{
var codec=new mxObjectCodec(new mxRootChange(),['model','previous','root']);
codec.afterEncode=function(enc,obj,node)
{
enc.encodeCell(obj.root,node);
return node;
};
codec.beforeDecode=function(dec,node,obj)
{
if(node.firstChild!=null&&node.firstChild.nodeType==mxConstants.NODETYPE_ELEMENT)
{
var tmp=node.firstChild;
obj.root=dec.decodeCell(tmp,false);
var tmp2=tmp.nextSibling;
tmp.parentNode.removeChild(tmp);
tmp=tmp2;
while(tmp!=null)
{
var tmp2=tmp.nextSibling;
dec.decodeCell(tmp);
tmp.parentNode.removeChild(tmp);
tmp=tmp2;
}
}
return node;
};
codec.afterDecode=function(dec,node,obj)
{
obj.previous=obj.root;
return obj;
};
return codec;
}());

mxCodecRegistry.register(function()
{
var codec=new mxObjectCodec(new mxChildChange(),['model','previous','previousIndex','child'],['parent']);
codec.isReference=function(obj,attr,value,isWrite)
{
if(attr=='child'&&(obj.previous!=null||!isWrite))
{
return true;
}
return mxUtils.indexOf(this.idrefs,attr)>=0;
};
codec.afterEncode=function(enc,obj,node)
{
if(this.isReference(obj,'child',obj.child,true))
{
node.setAttribute('child',enc.getId(obj.child));
}
else
{






enc.encodeCell(obj.child,node);
}
return node;
};
codec.beforeDecode=function(dec,node,obj)
{
if(node.firstChild!=null&&node.firstChild.nodeType==mxConstants.NODETYPE_ELEMENT)
{
var tmp=node.firstChild;
obj.child=dec.decodeCell(tmp,false);




obj.child.setParent(null);
var tmp2=tmp.nextSibling;
tmp.parentNode.removeChild(tmp);
tmp=tmp2;
while(tmp!=null)
{
var tmp2=tmp.nextSibling;
if(tmp.nodeType==mxConstants.NODETYPE_ELEMENT)
{






var id=tmp.getAttribute('id');
if(dec.lookup(id)==null)
{
dec.decodeCell(tmp);
}
}
tmp.parentNode.removeChild(tmp);
tmp=tmp2;
}
}
else
{
var childRef=node.getAttribute('child');
obj.child=dec.getObject(childRef);
}
return node;
};
codec.afterDecode=function(dec,node,obj)
{
obj.previous=obj.parent;
obj.previousIndex=obj.index;
return obj;
};
return codec;
}());

mxCodecRegistry.register(function()
{
var codec=new mxObjectCodec(new mxTerminalChange(),['model','previous'],['cell','terminal']);
codec.afterDecode=function(dec,node,obj)
{
obj.previous=obj.terminal;
return obj;
};
return codec;
}());

{
var mxGenericChangeCodec=function(obj,variable)
{
var codec=new mxObjectCodec(obj,['model','previous'],['cell']);
codec.afterDecode=function(dec,node,obj)
{
if(obj.previous==null)
{
obj.previous=obj[variable];
}
return obj;
}
return codec;
};
mxCodecRegistry.register(mxGenericChangeCodec(new mxValueChange(),'value'));
mxCodecRegistry.register(mxGenericChangeCodec(new mxStyleChange(),'style'));
mxCodecRegistry.register(mxGenericChangeCodec(new mxGeometryChange(),'geometry'));
mxCodecRegistry.register(mxGenericChangeCodec(new mxCollapseChange(),'collapsed'));
mxCodecRegistry.register(mxGenericChangeCodec(new mxVisibleChange(),'visible'));
mxCodecRegistry.register(mxGenericChangeCodec(new mxCellAttributeChange(),'value'));
}

mxCodecRegistry.register(function()
{
return new mxObjectCodec(new mxGraph(),['graphListeners','eventListeners','view','container','cellRenderer','editor','selection','gestureHandler','selection','activeElement','focusHandler','blurHandler']);
}());

mxCodecRegistry.register(function()
{
var codec=new mxObjectCodec(new mxGraphView());
codec.encode=function(enc,view)
{
return this.encodeCell(enc,view,view.graph.getModel().getRoot());
};
codec.encodeCell=function(enc,view,cell)
{
var model=view.graph.getModel();
var state=view.getState(cell);
var childCount=model.getChildCount(cell);
var parent=model.getParent(cell);
var geo=view.graph.getCellGeometry(cell);
var name='layer';
if(parent==null)
{
name='graph';
}
else if(model.isEdge(cell))
{
name='edge';
}
else if(childCount>0&&geo!=null)
{
name='group';
}
else if(model.isVertex(cell))
{
name='vertex';
}
var node=enc.document.createElement(name);
var lab=view.graph.getLabel(cell);
if(lab!=null)
{
node.setAttribute('label',view.graph.getLabel(cell));
}
if(parent==null)
{
var bounds=view.bounds;
if(bounds!=null)
{
node.setAttribute('x',Math.round(bounds.x));
node.setAttribute('y',Math.round(bounds.y));
node.setAttribute('width',Math.round(bounds.width));
node.setAttribute('height',Math.round(bounds.height));
}
node.setAttribute('scale',view.scale);
}
else if(state!=null&&geo!=null)
{
for(var i in state.style)
{
var value=state.style[i];
if(value!=null&&typeof(value)!='function'&&typeof(value)!='object')
{
node.setAttribute(i,value);
}
}
var abs=state.absolutePoints;
if(abs!=null&&abs.length>0)
{
var pts=Math.round(abs[0].x)+','+Math.round(abs[0].y);
for(var i=1;i<abs.length;i++)
{
pts+=' '+Math.round(abs[i].x)+','+Math.round(abs[i].y);
}
node.setAttribute('points',pts);
}
else
{
node.setAttribute('x',Math.round(state.x));
node.setAttribute('y',Math.round(state.y));
node.setAttribute('width',Math.round(state.width));
node.setAttribute('height',Math.round(state.height));
}
var offset=state.absoluteOffset;
if(offset!=null)
{
if(offset.x!=0)
{
node.setAttribute('offsetX',Math.round(offset.x));
}
if(offset.y!=0)
{
node.setAttribute('offsetY',Math.round(offset.y));
}
}
}
for(var i=0;i<childCount;i++)
{
node.appendChild(this.encodeCell(enc,view,model.getChildAt(cell,i)));
}
return node;
};
return codec;
}());

mxCodecRegistry.register(function()
{
var codec=new mxObjectCodec(new mxStylesheet());
codec.encode=function(enc,obj)
{
var node=enc.document.createElement(mxUtils.getFunctionName(obj.constructor));
for(var i in obj.styles)
{
var style=obj.styles[i];
var styleNode=enc.document.createElement('add');
if(i!=null)
{
styleNode.setAttribute('as',i);
for(var j in style)
{
var entry=enc.document.createElement('add');
entry.setAttribute('as',j);
var type=typeof(style[j]);
if(type=='function')
{



var name=null;
for(var k in mxPerimeter)
{
if(mxPerimeter[k]==style[j])
{
name='mxPerimeter.'+k;
}
}
if(name==null)
{
for(var k in mxEdgeStyle)
{
if(mxEdgeStyle[k]==style[j])
{
name='mxEdgeStyle.'+k;
}
}
}
if(name!=null)
{
var tmp=enc.document.createTextNode(name);
entry.appendChild(tmp);
}
}
else if(type!='object')
{
entry.setAttribute('value',style[j]);
}
styleNode.appendChild(entry);
}
if(styleNode.childNodes.length>0)
{
node.appendChild(styleNode);
}
}
}
return node;
};
codec.decode=function(dec,node,into)
{
var obj=into||new this.template.constructor();
var id=node.getAttribute('id');
if(id!=null)
{
dec.objects[id]=obj;
}
node=node.firstChild;
while(node!=null)
{
if(!this.processInclude(dec,node,obj)&&node.nodeName=='add')
{
var as=node.getAttribute('as');
if(as!=null)
{
var extend=node.getAttribute('extend');
var style=(extend!=null)?mxUtils.clone(obj.styles[extend]):null;
if(style==null)
{
if(extend!=null)
{
mxLog.warn('mxStylesheetCodec.decode: stylesheet '+extend+' not found to extend');
}
style=new Object();
}
var entry=node.firstChild;
while(entry!=null)
{
if(entry.nodeType==mxConstants.NODETYPE_ELEMENT)
{
var key=entry.getAttribute('as');
if(entry.nodeName=='add')
{
var value=entry.getAttribute('value');
if(mxUtils.isNumeric(value))
{
style[key]=parseFloat(value);
}
else
{
style[key]=value;
}
var text=mxUtils.getTextContent(entry);
if(text!=null&&text.length>0)
{
try
{
style[key]=eval(text);
}
catch(e)
{
}
}
}
else if(entry.nodeName=='remove')
{
delete style[key];
}
}
entry=entry.nextSibling;
}
obj.putCellStyle(as,style);
}
}
node=node.nextSibling;
}
return obj;
};
return codec;
}());

mxCodecRegistry.register(function()
{
var codec=new mxObjectCodec(new mxDefaultKeyHandler());
codec.encode=function(enc,obj)
{
return null;
};
codec.decode=function(dec,node,into)
{
if(into!=null)
{
var editor=into.editor;
node=node.firstChild;
while(node!=null)
{
if(!this.processInclude(dec,node,into)&&node.nodeName=='add')
{
var as=node.getAttribute('as');
var action=node.getAttribute('action');
var control=node.getAttribute('control');
into.bindAction(as,action,control);
}
node=node.nextSibling;
}
}
return into;
};
return codec;
}());

mxCodecRegistry.register(function()
{
var codec=new mxObjectCodec(new mxDefaultToolbar());
codec.encode=function(enc,obj){
return null;
};
codec.decode=function(dec,node,into)
{
if(into!=null)
{
var editor=into.editor;
var model=editor.graph.getModel();
node=node.firstChild;
while(node!=null)
{
if(node.nodeType==mxConstants.NODETYPE_ELEMENT)
{
if(!this.processInclude(dec,node,into))
{
if(node.nodeName=='separator')
{
into.addSeparator();
}
else if(node.nodeName=='br')
{
into.toolbar.addBreak();
}
else if(node.nodeName=='hr')
{
into.toolbar.addLine();
}
else if(node.nodeName=='add')
{
var as=node.getAttribute('as');
as=mxResources.get(as)||as;
var icon=node.getAttribute('icon');
var pressedIcon=node.getAttribute('pressedIcon');
var action=node.getAttribute('action');
var mode=node.getAttribute('mode');
var template=node.getAttribute('template');
if(action!=null)
{
into.addItem(as,icon,action,pressedIcon);
}
else if(mode!=null)
{
var funct=mxUtils.eval(mxUtils.getTextContent(node));
into.addMode(as,icon,mode,pressedIcon,funct);
}
else if(template!=null)
{
var cell=editor.templates[template];
var style=node.getAttribute('style');
if(style!=null){
cell=cell.clone();
cell.setStyle(style);
}
var insertFunction=null;
var text=mxUtils.getTextContent(node);
if(text!=null)
{
insertFunction=mxUtils.eval(text);
}
into.addPrototype(as,icon,cell,pressedIcon,insertFunction);
}
else
{
var children=mxUtils.getChildNodes(node);
if(children.length>0)
{
if(icon==null)
{
var combo=into.addActionCombo(as);
for(var i=0;i<children.length;i++)
{
var child=children[i];
if(child.nodeName=='separator')
{
into.addOption(combo,'---');
}
else if(child.nodeName=='add')
{
var lab=child.getAttribute('as');
var act=child.getAttribute('action');
into.addActionOption(combo,lab,act);
}
}
}
else
{
var select=null;
var create=function()
{
var template=editor.templates[select.value];
if(template!=null)
{
var clone=template.clone();
var style=select.options[select.selectedIndex].cellStyle;
if(style!=null)
{
clone.setStyle(style);
}
return clone;
}
else
{
mxLog.warn('Template '+template+' not found');
}
return null;
}
var img=into.addPrototype(as,icon,create);
select=into.addCombo();

mxEvent.addListener(select,'change',function()
{
into.toolbar.selectMode(img,function(evt)
{
var pt=mxUtils.convertPoint(editor.graph.container,evt.clientX,evt.clientY);
return editor.addVertex(null,funct(),pt.x,pt.y);
});
into.toolbar.noReset=false;
});
for(var i=0;i<children.length;i++)
{
var child=children[i];
if(child.nodeName=='separator')
{
into.addOption(select,'---');
}
else if(child.nodeName=='add')
{
var lab=child.getAttribute('as');
var tmp=child.getAttribute('template');
var option=into.addOption(select,lab,tmp||template);
option.cellStyle=child.getAttribute('style');
}
}
}
}
}
}
}
}
node=node.nextSibling;
}
}
return into;
};
return codec;
}());

mxCodecRegistry.register(function()
{
var codec=new mxObjectCodec(new mxDefaultPopupMenu());
codec.encode=function(enc,obj)
{
return null;
};
codec.decode=function(dec,node,into)
{
var inc=node.getElementsByTagName('include')[0];
if(inc!=null)
{
this.processInclude(dec,inc,into);
}
else if(into!=null)
{
into.config=node;
}
return into;
};
return codec;
}());

mxCodecRegistry.register(function()
{
var codec=new mxObjectCodec(new mxEditor(),['modified','lastSnapshot','ignoredChanges','undoManager','graphContainer','toolbarContainer']);
codec.afterDecode=function(dec,node,obj)
{
var defaultEdge=node.getAttribute('defaultEdge');
if(defaultEdge!=null)
{
node.removeAttribute('defaultEdge');
obj.defaultEdge=obj.templates[defaultEdge];
}
var defaultGroup=node.getAttribute('defaultGroup');
if(defaultGroup!=null)
{
node.removeAttribute('defaultGroup');
obj.defaultGroup=obj.templates[defaultGroup];
}
return obj;
};
codec.decodeChild=function(dec,child,obj)
{
if(child.nodeName=='Array')
{
var role=child.getAttribute('as');
if(role=='templates')
{
this.decodeTemplates(dec,child,obj);
return;
}
}
else if(child.nodeName=='ui')
{
this.decodeUi(dec,child,obj);
return;
}
mxObjectCodec.prototype.decodeChild.apply(this,arguments);
};
codec.decodeUi=function(dec,node,editor)
{
var tmp=node.firstChild;
while(tmp!=null)
{
if(tmp.nodeName=='add')
{
var as=tmp.getAttribute('as');
var elt=tmp.getAttribute('element');
var style=tmp.getAttribute('style');
var element=null;
if(elt!=null)
{
element=document.getElementById(elt);
if(element!=null&&style!=null)
{
element.style.cssText+=';'+style;
}
}
else
{
var x=parseInt(tmp.getAttribute('x'));
var y=parseInt(tmp.getAttribute('y'));
var width=tmp.getAttribute('width');
var height=tmp.getAttribute('height');
element=document.createElement('div');
element.style.cssText=style;
var wnd=new mxWindow(mxResources.get(as)||as,element,x,y,width,height,false,true);
wnd.setVisible(true);
}
if(as=='graph')
{
editor.setGraphContainer(element);
}
else if(as=='toolbar')
{
editor.setToolbarContainer(element);
}
else if(as=='title')
{
editor.setTitleContainer(element);
}
else if(as=='status')
{
editor.setStatusContainer(element);
}
else if(as=='map')
{
editor.setMapContainer(element);
}
}
else if(tmp.nodeName=='resource')
{
mxResources.add(tmp.getAttribute('basename'));
}
else if(tmp.nodeName=='stylesheet')
{
mxClient.link('stylesheet',tmp.getAttribute('name'));
}
tmp=tmp.nextSibling;
}
};
codec.decodeTemplates=function(dec,node,editor)
{
if(editor.templates==null)
{
editor.templates=new Array();
}
var children=mxUtils.getChildNodes(node);
for(var j=0;j<children.length;j++)
{
var name=children[j].getAttribute('as');
var child=children[j].firstChild;
while(child!=null&&child.nodeType!=1)
{
child=child.nextSibling;
}
if(child!=null)
{




editor.templates[name]=dec.decodeCell(child);
}
}
};
return codec;
}());
if(mxClient.IS_NS)
{
mxClient.include('chrome://global/content/contentAreaUtils.js');
}
if(mxClient.loading==null)
{
mxClient.onload();
}
