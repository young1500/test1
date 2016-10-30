function log(t){
  window.jsToJava.log(t);
}

function notifyOutSideTitle(t){
window.toWebView.showOrHideTitle(t);
}

function outputSource() {
    var html = document.getElementsByTagName('html')[0];
    var source = html.outerHTML;
    window.jsToJava.outputSource(window.location.href, source);
}

function hideTitle(){
        log('hideTitle');

    var titles = document.getElementsByClassName('ytp-title');

    if (titles == undefined || titles == null || titles.length == 0) {
        return false;
    }

    var  title = titles[0];
    title.style.display = 'none';

}

function hideShareBtn(){
        log('hideShareBtn');

    var titles = document.getElementsByClassName('ytp-chrome-top ytp-share-button-visible');

    if (titles == undefined || titles == null || titles.length == 0) {
        return false;
    }

    var  title = titles[0];
    title.style.display = 'none';

}

function triggerClick(el) {
        log('triggerClick');
    if(el.click) {
        el.click();
    }else{
        try{
            var evt = document.createEvent('Event');
            evt.initEvent('click',true,true);
            el.dispatchEvent(evt);
        }catch(e){
        };
    }
}

function autoPlay(){
var buttons = document.getElementsByClassName('ytp-large-play-button ytp-button');

if (buttons == undefined || buttons == null || buttons.length == 0) {
        return false;
    }
var button = buttons[0];
    if(button.click) {
        button.click();
        log('autoPlay');
    }else{
        try{
            var evt = document.createEvent('Event');
            evt.initEvent('click',true,true);
            button.dispatchEvent(evt);
        }catch(e){

        }
    }
}

function autoHideTitle(){
        log('autoHideTitle');

var controllers = document.getElementsByClassName('html5-video-player playing-mode');


if (controllers == undefined || controllers == null || controllers.length == 0) {
        return false;
    }

var controller = controllers[0];

var ob = new MutationObserver(function (records) {
                                records.forEach(function(record) {
                                  notifyOutSideTitle(record.target.className);
                                })
                              });

ob.observe(controller, {
  attribute: true,
  attributeOldValue: true
});

}


function onDomChanged(ev) {
        log('onDomChanged');

    if (ev.addedNodes) {
        if (ev.addedNodes.length > 0) {
            for (var i =0; i < ev.addedNodes.length; i++) {
                var node = ev.addedNodes[i];
                if (!node){
                    continue;
                }
                var parent_node = findVideo(node);

                if (!parent_node) {
                    continue;
                }
                var info = getInfo(parent_node);

                var url = window.location.href;
                if (url.lastIndexOf('home') > 0) {
                    url = url.substring(0, url.lastIndexOf('home'));
                }
                url = url.substring(url.lastIndexOf('/')+1,url.length);

                if (url.length>0) {
                    return;
                }
                injectNormalBtn(parent_node, info);
            }
        }
    }

    else if (ev.target && ev.type == 'DOMNodeInserted') {

        var node = ev.target;
        if (!node){
            return;
        }
        var parent_node = findVideo(node);
        if (parent_node == null) {
            return;
        }
        var info = getInfo(parent_node);
        var url = window.location.href;
        if (url.lastIndexOf('home') > 0) {
            url = url.substring(0, url.lastIndexOf('home'));
        }
        url = url.substring(url.lastIndexOf('/')+1,url.length);

        if (url.length>0) {
            return;
        }
        injectNormalBtn(parent_node, info);
    }

}