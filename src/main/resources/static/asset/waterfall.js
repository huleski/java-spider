'use strict';

var columnH = [],
    page = 1,
    selectMode = 'day',
    flag = false, 
    date = "",
    baseUrl = "http://localhost:9000",
    url = 'https://api.pixivic.com/';

function getDay(page, str) {
    var today = new Date();
    var nowTime = today.getTime();
    var ms = 24 * 3600 * 1000 * page;
    today.setTime(parseInt(nowTime + ms));
    var oYear = today.getFullYear();
    var oMoth = (today.getMonth() + 1).toString();
    if (oMoth.length <= 1) oMoth = '0' + oMoth;
    var oDay = today.getDate().toString();
    if (oDay.length <= 1) oDay = '0' + oDay;
    return oYear + str + oMoth + str + oDay;
}

(function () {
    date = getDay(-3, '-');
    document.title = `${date} ${selectMode}排行`;
    
    //这一步是为了阻止右击时系统默认的弹出框
    document.oncontextmenu = function() {
        return false;
    }
    //在这里你就可以自己定义事件的函数啦
    $("#waterfall").on("mousedown", ".box", function(e){
        if(e.which == 3) {  // 右键
            $(this).toggleClass("choosed");
            $('#tip').html($(".choosed").length + "张");
        }
    });
}());
// init();

function addPictures(url){
    let pics = [];
    $('.choosed').each(function(index, item){
        let elem = item.children[0];

        let object = {};
        object.title = elem.title;
        object.illustId = elem.illustId;
        object.caption = elem.caption;
        object.sort = elem.sort;
        object.user = elem.author.name;
        object.userId = elem.author.id;
        object.userAvatar = elem.author.avatar;
        object.originalImg = elem.originalImg;
        object.fixedImg = elem.fixedImg;
        object.pixImg = elem.pixImg;
        object.rankDate = date;
        let tagsArr = new Array();
        elem.tags.forEach(function(item, index){
            tagsArr.push(item.name);
        });
        object.tags = tagsArr.join(",");
        pics.push(object);
    });

    $.ajax({
        url : baseUrl + url,
        type : 'post',
        dataType: 'json',
        contentType: "application/json",
        data : JSON.stringify(pics),//转为json格式
        success : function(e) {
            console.log(e)
        }
    });
};

function init() {
    ajax("get", url + 'ranks', `page=${page}&date=${date}&mode=${selectMode}`, showlist, true);
}

function ajax(method, url, data, callback, flag) {
    var xhr = null,
        method = method.toUpperCase();
    if (window.XMLHttpRequest) {
        xhr = new XMLHttpRequest();
    } else {
        xhr = new ActiveXObject('Microsoft.XMLHttp');
    }
    if (method == "GET") {
        xhr.open(method, url + '?' + data, flag);
        xhr.send();
    } else if (method == "POST") {
        xhr.open(method, url, true);
        xhr.setRequestHeader('Content-type', 'application/x-www-form-urlencoded');
        xhr.send(data);
    }
    xhr.onreadystatechange = function () {
        if (xhr.readyState == 4) {
            if (xhr.status == 200) {
                callback(xhr.responseText);
            }
        }
    }
}

function post(url, data, callback) {
    var xhr = null;
    if (window.XMLHttpRequest) {
        xhr = new XMLHttpRequest();
    } else {
        xhr = new ActiveXObject('Microsoft.XMLHttp');
    }

    xhr.open("POST", url, true);
    xhr.setRequestHeader('Content-type', 'application/json');
    xhr.send(data);
    xhr.onreadystatechange = function () {
        if (xhr.readyState == 4) {
            if (xhr.status == 200) {
                callback(xhr.responseText);
            }
        }
    }
}

/**
 * 修改过后的方法
 * @param {*} data 
 */
function showlist(result) {
    var dataArr = JSON.parse(result).data.data,
        elem = '',
        imgH = 0,
        content = document.getElementById("waterfall"),
        minIndex;
    var url;
    if (dataArr.length > 0) {
        let index = 0;
        for(let i = 0; i < dataArr.length; i ++){
            let item = dataArr[i];

            if (item.type == "manga" || item.imageUrls.length > 10) {
                continue;
            } 
            let arr = new Array();
            imgH = Math.ceil(228 * item.height / item.width);
            item.imageUrls.map(e => {
                arr.push({
                    orginUrl:e.original,
                    largeUrl:e.large,
                    mediumUrl:e.medium
                });
            });

            arr.forEach(function(e, tmpInd){
                var oDiv = document.createElement("div");
                oDiv.className = "box";
                elem = '<a href="' + e.orginUrl + '" alt ="' + item.title + '"  class="image" rel="https://www.pixiv.net/member_illust.php?mode=medium&illust_id=' + item.id + '">\
                        <img src ="' + e.largeUrl + '" height ="' + imgH + '"  width="228" alt="' + item.title + '" referrerpolicy="no-referrer">\
                        </a>';
                elem += item.title.length == 0 ? "" : '<p>' + item.title + '</p>';

                if (index < 5 && columnH.length != 5) {
                    oDiv.style.left = 240 * index + "px";
                    oDiv.style.top = "0px";
                    oDiv.innerHTML = elem;
                    content.appendChild(oDiv);
                    columnH.push(oDiv.offsetHeight);
                } else {
                    minIndex = minH(columnH);
                    oDiv.style.left = 240 * minIndex + "px";
                    oDiv.style.top = columnH[minIndex] + 20 + "px";
                    oDiv.innerHTML = elem;
                    content.appendChild(oDiv);
                    columnH[minIndex] += oDiv.offsetHeight + 20;
                    document.getElementById('loader').style.top = columnH[minIndex] + 60 + "px";
                }
                index ++;
    
                Object.assign(oDiv.children[0], {
                    originalImg: e.orginUrl,
                    fixedImg: e.largeUrl,
                    pixImg: e.mediumUrl,
                    sort: tmpInd + 1,
                    illustId: item.id,
                    title: item.title,
                    caption: item.caption,
                    tags: item.tags,
                    author: item.artistPreView,
                });
            });
        }
        let lightbox = new Lightbox('.image');
        flag = false;
    } else {
        var info = document.getElementById('info');
        info.innerHTML = '(￣ˇ￣)俺也是有底线的';
        info.style.top = (document.getElementById('loader').offsetTop - 250) + "px";
    }
}

/**
 * 原始方法 已弃用
 * @param {*} data 
 */
function originalShowlist(data) {
    var str = JSON.parse(data).data.illustrations,
        elem = '',
        imgH = 0,
        content = document.getElementById("waterfall"),
        minIndex;
    var url;
    if (str.length > 0) {
        str.forEach(function (item, index) {
            var oDiv = document.createElement("div");
            oDiv.className = "box";
            imgH = Math.ceil(228 * item.height / item.width);
            var orginUrl = item.meta_single_page.original_image_url;
            var largeUrl = item.meta_single_page.large_image_url;
            if (item.meta_pages.length > 0) {
                var pages = document.createElement('div')
                pages.classList.add('more-page');
                pages.innerHTML = `<svg t="1555333791341" class="icon" style="" viewBox="0 0 1024 1024" version="1.1" xmlns="http://www.w3.org/2000/svg" p-id="710" width="64" height="64" xmlns:xlink="http://www.w3.org/1999/xlink">
                    <defs><style type="text/css"></style></defs>
                    <path d="M914.3296 307.2V849.92a40.96 40.96 0 0 1-40.96 40.96h-542.72a40.96 40.96 0 0 1-40.96-40.96v-542.72a40.96 40.96 0 0 1 40.96-40.96h542.72a40.96 40.96 0 0 1 40.96 40.96z m-122.88-133.12a40.96 40.96 0 0 0-40.96-40.96h-542.72a40.96 40.96 0 0 0-40.96 40.96v542.72a40.96 40.96 0 0 0 81.92 0V215.04h501.76a40.96 40.96 0 0 0 40.96-40.96z" p-id="711"></path></svg>
                    <span>${item.meta_pages.length}</span>`
                orginUrl = item.meta_pages[0].image_urls.original;
                largeUrl = item.meta_pages[0].image_urls.large;
            }
            if (item.type == 'ugoira') {
                var pages = document.createElement('div');
                pages.classList.add('more-page');
                pages.innerHTML = `<svg t="1556422453119" class="icon" style="" viewBox="0 0 1024 1024" version="1.1" xmlns="http://www.w3.org/2000/svg" p-id="12068" xmlns:xlink="http://www.w3.org/1999/xlink" width="64" height="64"><defs><style type="text/css"></style></defs><path d="M512 0C229.2224 0 0 229.2224 0 512s229.2224 512 512 512 512-229.2224 512-512S794.7776 0 512 0z m253.2352 537.9584l-359.168 237.9264a48.4352 48.4352 0 0 1-75.1616-40.3968v-446.976a48.4352 48.4352 0 0 1 72.8064-41.8816L762.88 455.68a48.384 48.384 0 0 1 2.3552 82.2272z" fill="#FFFFFF"p-id="12069"></path></svg>
                    <span>gif</span>`
            }
            elem = '<a href="' + orginUrl + '" alt ="' + item.title + '"  class="image" rel="https://www.pixiv.net/member_illust.php?mode=medium&illust_id=' + item.id + '">\
                        <img src ="' + largeUrl + '" height ="' + imgH + '"  width="228" alt="' + item.title + '" referrerpolicy="no-referrer">\
                        </a>';
            elem += item.title.length == 0 ? "" : '<p>' + item.title + '</p>';
            if (index < 4 && columnH.length != 4) {
                oDiv.style.left = 240 * index + "px";
                oDiv.style.top = "0px";
                oDiv.innerHTML = elem;
                content.appendChild(oDiv);
                columnH.push(oDiv.offsetHeight);
            } else {
                minIndex = minH(columnH);
                oDiv.style.left = 240 * minIndex + "px";
                oDiv.style.top = columnH[minIndex] + 20 + "px";
                oDiv.innerHTML = elem;
                content.appendChild(oDiv);
                columnH[minIndex] += oDiv.offsetHeight + 20;
                document.getElementById('loader').style.top = columnH[minIndex] + 60 + "px";
            }
            pages && oDiv.appendChild(pages)
            Object.assign(oDiv.children[0], {
                meta_pages: item.meta_pages.map(e => e.image_urls.original),
                title: item.title,
                caption: item.caption,
                tags: item.tags,
                author: item.user,
            });
        });
        let lightbox = new Lightbox('.image');
        flag = false;
    } else {
        var info = document.getElementById('info');
        info.innerHTML = '(￣ˇ￣)俺也是有底线的';
        info.style.top = (document.getElementById('loader').offsetTop - 250) + "px";
    }
}

function showdate() {
    var Mtop = getScroll();
    let pageH = parseInt(columnH[minH(columnH)]),
        MaxH = document.documentElement.clientHeight || document.body.clientHeight;
    if (pageH <= MaxH + Mtop.y) {
        if (!flag) {
            flag = true;
            page++;
            ajax("get", url + 'ranks', `page=${page}&date=` + changeDateBySelect() + `&mode=${selectMode}`, showlist, true);
        }
    }
}

function restart(date) {
    document.getElementById("waterfall").innerHTML = '';
    document.getElementById('loader').style.top = "0px";
    document.title = `${date} ${selectMode}排行`;
    Lightbox.prototype.index = 0;
    columnH = [],
        page = 1,
        flag = false;
    console.log(changeDateBySelect(0, date));
    ajax("get", url + 'ranks', `page=${page}&date=${date}&mode=${selectMode}`, showlist, true);
    var info = document.getElementById('info');
    info.innerHTML = '';
}

function minH(arr) {
    var minh = arr[0],
        i = 1,
        index = 0;
    for (; i < arr.length; i++) {
        if (minh > arr[i]) {
            minh = arr[i];
            index = i;
        }
    }
    return index;
}

function getScroll() {
    if (window.pageXoffset) {
        return {
            x: window.pageXoffset,
            y: window.pageYoffset
        }
    } else {
        return {
            x: document.body.scrollLeft + document.documentElement.scrollLeft,
            y: document.body.scrollTop + document.documentElement.scrollTop
        }
    }
}

function choke(func, wait) {
    var lastTime = 0;
    return function () {
        var _self = this,
            _arg = arguments;
        var nowTime = Date.now();
        if (nowTime - lastTime > wait) {
            func.apply(_self, _arg);
            lastTime = nowTime;
        }
    }
}