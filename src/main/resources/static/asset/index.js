var gototop = document.getElementById('gototop');
var timer;
var tf=true;
function index(){
    var ostop=document.documentElement.scrollTop || window.pageYOffset || document.body.scrollTop;
    var ch=document.body.clientHeight||document.documentElement.clientHeight;
    if(ostop>=ch){
        gototop.style.display='inline';
        gototop.className='animated fadeInUp';
    }else{
        gototop.className='animated fadeOutDown';
    }
    if(!tf){
        clearInterval(timer);
    }
    tf=false;
}
gototop.onclick=function(){
    timer=setInterval(function(){
        var ostop=document.documentElement.scrollTop || window.pageYOffset || document.body.scrollTop;
        var speed=Math.ceil(ostop/7);
        document.documentElement.scrollTop=document.body.scrollTop=ostop-speed;
        if(ostop==0){
            clearInterval(timer);
        }
        tf=true;
    },30);
}
window.addEventListener('scroll',choke(showdate, 100));
window.addEventListener('scroll',index);
var mySchedule = new Schedule({
    el: '#schedule-box',
    //date: '2018-9-20',
    clickCb: function (y,m,d) {
        document.querySelector('#h3Ele').innerHTML =y+'-'+m.toString().padStart(2,0)+'-'+d.toString().padStart(2,0);
        date=y+'-'+m.toString().padStart(2,0)+'-'+d.toString().padStart(2,0);
       // select.apply($("span[data-value='day']")[0]);
        restart(changeDateBySelect());
    },
    nextMonthCb: function (y,m,d) {
        document.querySelector('#h3Ele').innerHTML =y+'-'+m+'-'+d
    },
    nextYeayCb: function (y,m,d) {
        document.querySelector('#h3Ele').innerHTML =y+'-'+m+'-'+d
    },
    prevMonthCb: function (y,m,d) {
        document.querySelector('#h3Ele').innerHTML =y+'-'+m+'-'+d
    },
    prevYearCb: function (y,m,d) {
        document.querySelector('#h3Ele').innerHTML =y+'-'+m+'-'+d
    }
});
document.querySelector("#calendaricon").onclick=function () {
    document.querySelector(".mask").style.display = "block";
    document.querySelector("#h3Ele").style.display = "inline";
    document.querySelector("#schedule-box").style.display='inline';
    document.querySelector("#schedule-box").className='animated fadeInUp boxshaw';
    document.querySelector("#h3Ele").className='animated fadeInUp boxshaw';
}
document.onclick = function (e) {
    var id = e.srcElement.id;
    if (id != "mask"&&id!="calendaricon"&&id!="schedule-box"&&id!="prevYear"&&id!="prevMonth"&&id!="nextYear"&&id!="nextMonth") {
        document.querySelector(".mask").style.display = "none";
        document.querySelector("#schedule-box").className='animated fadeOutDown boxshaw';
        document.querySelector("#h3Ele").style.display = "none";
    }
};

document.getElementsByClassName('arrow-left')[0].onclick=function () {
    date=changeDateBySelect(-1, date);
    restart(date)
}

document.getElementsByClassName('arrow-right')[0].onclick=function () {
    date=changeDateBySelect(1, date)
    restart(date)
}

$("#search").keypress(function (e) {
    if (e.which == 13) {
        document.getElementById("waterfall").innerHTML = '';
        document.getElementById('loader').style.top = "0px";
        Lightbox.prototype.index = 0;
        columnH = [],
        page = 1,
        flag = false;
        ajax("get", url + 'illustrations', `illustType=illust&searchType=original&maxSanityLevel=6&page=${page}&pageSize=30&keyword=`+ this.value, showlist, true);
        var info = document.getElementById('info');
        info.innerHTML = '';
    }
});