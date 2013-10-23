/**
 * @author Feng Wang, Arizona State University, fwang49@asu.edu
 * created on March 19, 2013
 *
 * @author Jose Ibarra, Jose.Ibarra@asu.edu, Arizona State University
 * Modified on August 28, 2013 and after
 *
 * Initialize the page, map, location
 */

/**
 * initialize staffs packed in this object
 */
var kde;
var kde2;


$(function() {
	$("#dashboard-tabs").tabs({
		selected : 0
	});
});

function onClickShowKDE(nametype) {
	if(nametype == "surname"){
		state.kdeSurname = $('#surname_input').val().toUpperCase();
		if(state.kdeSurname)
			kdeQuery(nametype);
	}
	else if(nametype == "forename"){
		state.kdeForename = $('#forename_input').val().toUpperCase();
		if(state.kdeForename)
			kdeQuery(nametype);
	}
}

function onClickCaptureButton(nametype) {
	if(nametype == "surname"){
		state.kdeSurname = $('#surname_input').val().toUpperCase();
		if(state.kdeSurname)
			captureUpload(nametype);
	}
	else if(nametype == "forename"){
		state.kdeForename = $('#forename_input').val().toUpperCase();
		if(state.kdeForename)
			captureUpload(nametype);
	}
}

/*
	Sets up a default Google Map.
	The Google Map API javascript must be imported before calling this. Additionally, 
	the map canvas (div) must be on the page before this is called.
	See https://developers.google.com/maps/documentation/javascript/tutorial for reference.
*/
function KDEWindow() {
	this.mapCanvasId = "surnamemap";

	this.map = null;
	var that = this;
	this.overlay = null;
	var mapOptions = {
		// center : new google.maps.LatLng(39.5, -98.35),  
		center : new google.maps.LatLng(state.centerLat, state.centerLon),  
		mapTypeId : google.maps.MapTypeId.ROADMAP,
		disableDoubleClickZoom : true,
		scrollwheel : false,
		zoom : state.zoomLevel,
		zoomControl : false,
		streetViewControl : false
	}

	this.map = new google.maps.Map(document.getElementById(this.mapCanvasId),
			mapOptions);
	this.overlay = null;

	google.maps.event.addListenerOnce(this.map, "bounds_changed", function() {
		if (that.overlay != null) {
			that.overlay.setMap(null);
			delete that.overlay;
		}
		that.overlay = new KDEOverlay(that.map, "surname");
		// if (state.kde != '') {
		// 	kdeQuery("surname");
		// }
	});

	google.maps.event.addListener(this.map, "idle", function() {
		if (state.kdeSurname != '') {
			kdeQuery("surname");
		}
	});
}

function KDEWindow2() {
	var mapOptions = {
		// center : new google.maps.LatLng(39.5, -98.35),  
		center : new google.maps.LatLng(state.centerLat, state.centerLon),  
		mapTypeId : google.maps.MapTypeId.ROADMAP,
		disableDoubleClickZoom : true,
		scrollwheel : false,
		zoom : state.zoomLevel,
		zoomControl : false,
		streetViewControl : false
	}

	this.map2 = null;
	that=this;
	this.overlay2 =null;
	this.map2 = new google.maps.Map(document.getElementById("forenamemap"), mapOptions);

	google.maps.event.addListenerOnce(this.map2, "bounds_changed", function() {
		if (that.overlay2 != null) {
			that.overlay2.setMap(null);
			delete that.overlay2;
		}
		that.overlay2 = new KDEOverlay(that.map2, "forename");
	});

	google.maps.event.addListener(this.map2, "idle", function() {
		if (state.kdeForename != '') {
			kdeQuery("forename");
		}
	});
}


var requestCount = 0;
$(window).load(function() {
	kde = new KDEWindow();
	kde2 = new KDEWindow2();
	requestCount = 0;
	var leftpos = $('#centercontainter').width();
	var toppos = $('#centercontainter').height();
	leftpos = leftpos/2 - 50;
	toppos = toppos/2 + 50;
	$('#loadingsurname').css('left', '' + leftpos/2 + 'px');
	$('#loadingsurname').css('top', '' + toppos + 'px');
	$('#loadingsurname').hide();
	$('#loadingforename').css('left', '' + (leftpos/2)*3 + 'px');
	$('#loadingforename').css('top', '' + toppos + 'px');
	$('#loadingforename').hide();
	$('#captureButton').hide();
	$('#capturedimagelink').hide();
	$('#forenameCaptureButton').hide();
	$('#forenameCapturedimagelink').hide();
	$('#surnameMedianIncomeText').hide();
	$('#surnameMeanIncomeText').hide();
	$('#forenameMedianIncomeText').hide();
	$('#forenameMeanIncomeText').hide();
	var curURL = document.URL;
	var completeURL = "https://twitter.com/share?url=" + curURL + "&text=I found my surname density at " + curURL;
	$('#twitter').attr('href', completeURL);
});
