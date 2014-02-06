/**
 * @author Feng Wang, Arizona State University, fwang49@asu.edu
 * created on March 19, 2013
 *
 * @author Jose Ibarra, Jose.Ibarra@asu.edu, Arizona State University
 * Modified on August 28, 2013 and after
 *
 * Initialize the page, map, location
 */

//kde object for the surname
var kde;

function onClickShowKDE(nametype) {
	positionMapLoadingIndicators();
	if(nametype == "surname"){
		state.kdeSurname = $('#surname_input').val().toUpperCase();
		$('#surname_distribution_bar').hide();
		$('#surname_histogram_options').hide();
		if(state.kdeSurname){
			histogramQuery(nametype);
			if($('#surnameMapType').val() == 'regular')
				kdeQuery(nametype, '');
			else if($('#surnameMapType').val() == 'probabilistic')
				kdeQuery(nametype, 'Probabilistic');
		}
	}
}

function onClickCaptureButton(nametype) {
	positionMapLoadingIndicators();
	if(nametype == "surname"){
		state.kdeSurname = $('#surname_input').val().toUpperCase();
		if(state.kdeSurname)
			captureUpload(nametype);
	}
}

/*
	Sets up a default Google Map.
	The Google Map API javascript must be imported before calling this. Additionally, 
	the map canvas (div) must be on the page before this is called.
	See https://developers.google.com/maps/documentation/javascript/tutorial for reference.
*/
function KDEWindow(nameType) {
	this.mapCanvasId = nameType+"map";

	this.map = null;
	var that = this;
	this.overlay = null;

	//create a map based on the desired center, as a road map.
	//zooming is disabled, street view is disabled and panning
	//is disabled
	var mapOptions = {
		center : new google.maps.LatLng(state.centerLat, state.centerLon),  
		mapTypeId : google.maps.MapTypeId.ROADMAP,
		disableDoubleClickZoom : true,
		scrollwheel : false,
		zoom : state.zoomLevel,
		zoomControl : false,
		streetViewControl : false,
		panControl : false,
		draggable : false
	}

	this.map = new google.maps.Map(document.getElementById(this.mapCanvasId),
			mapOptions);
	this.overlay = null;

	google.maps.event.addListenerOnce(this.map, "bounds_changed", function() {
		if (that.overlay != null) {
			that.overlay.setMap(null);
			delete that.overlay;
		}
		that.overlay = new KDEOverlay(that.map, nameType);
	});
	if(nameType == "surname"){
		google.maps.event.addListener(this.map, "idle", function() {
			if (state.kdeSurname != '') {
				kdeQuery(nameType);
			}
		});
	}
}


var requestCount = 0;
$(window).load(function() {
	kde = new KDEWindow("surname");
	requestCount = 0;
	
	positionSocialMedia();
	positionMapLoadingIndicators();
	$('#loadingsurname').hide();
	$('#captureButton').hide();
	$('#capturedimagelink').hide();
	$('#surnameMedianIncomeText').hide();
	$('#surnameMeanIncomeText').hide();
	$('#surname_distribution_bar').hide();
	$('#surname_histogram_options').hide();
	$('#loadingsurnamehistogram').hide();
	var curURL = document.URL;
	var completeURL = "https://twitter.com/share?url=" + curURL + "&text=I found my surname and forename density at " + curURL;
	$('#twitter').attr('href', completeURL);

	$(function() {
	    $( "#tabs" ).tabs({heightStyle: "fill", widthStyle: "auto", 
	      activate: function(event, ui){
	      	if(ui.newTab.index() == 0){
	      		var center = kde.map.getCenter();
	      		google.maps.event.trigger(kde.map, 'resize');
	      		kde.map.setCenter(center);
	      	}
	      }
	    });
  	});

  	
});

function positionMapLoadingIndicators(){
	var leftpos = $('#surnamemap').width();
	var toppos = $('#surnamemap').height();
	leftpos = leftpos/2;
	toppos = toppos/2 + $('#surnameContainer').height();
	$('#loadingsurname').css('left', '' + leftpos + 'px');
	$('#loadingsurname').css('top', '' + toppos + 'px');
}

function positionSocialMedia(){
	if($(document).width() < 800){
		$('#socialMedia').hide();
	}
	else{
		$('#socialMedia').show();
		var socialPos = $('#surnameContainer').width() - ($('#socialMedia').width() / 2);
		$('#socialMedia').css('left', ''+ socialPos + 'px');
	}
}

window.onresize = function(event) {
	positionSocialMedia();
}