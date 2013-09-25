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


$(function() {
	$("#dashboard-tabs").tabs({
		selected : 0
	});
});

function onClickShowKDE() {
	state.kdeSurname = $('#surname_input').val().toUpperCase();
	if(state.kdeSurname)
		kdeQuery();
}

function onClickCaptureButton() {
	state.kdeSurname = $('#surname_input').val().toUpperCase();
	captureUpload();
}

/*
	Sets up a default Google Map.
	The Google Map API javascript must be imported before calling this. Additionally, 
	the map canvas (div) must be on the page before this is called.
	See https://developers.google.com/maps/documentation/javascript/tutorial for reference.
*/
function KDEWindow() {
	this.mapCanvasId = "main";

	this.map = null;
	var that = this;
	this.overlay = null;
	var mapOptions = {
		// center : new google.maps.LatLng(39.5, -98.35),  
		center : new google.maps.LatLng(38.513788, -97.078629),  
		mapTypeId : google.maps.MapTypeId.ROADMAP,
		disableDoubleClickZoom : true,
		scrollwheel : true,
		zoom : 5,
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
		that.overlay = new KDEOverlay(that.map);
		if (state.kdeSurname != '') {
			kdeQuery();
		}
	});

	google.maps.event.addListener(this.map, "idle", function() {
		if (state.kdeSurname != '') {
			kdeQuery();
		}
	});
}

var requestCount = 0;
$(window).load(function() {
	kde = new KDEWindow();
	requestCount = 0;
	var leftpos = $('#centercontainter').width();
	var toppos = $('#centercontainter').height();
	leftpos = leftpos/2 - 50;
	toppos = toppos/2 + 50;
	$('#loading').css('left', '' + leftpos + 'px');
	$('#loading').css('top', '' + toppos + 'px');
	$('#loading').hide();
	$('#captureButton').hide();
	$('#capturedimagelink').hide();
});
