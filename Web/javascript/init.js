/**
 * @author Feng Wang, Arizona State University, fwang49@asu.edu
 * created on March 19, 2013
 *
 * @author Jose Ibarra, Jose.Ibarra@asu.edu, Arizona State University
 * Modified on August 28, 2013 and after into 2014
 *
 * Initialize the page, map, location
 * 
 * Linted on April 8, 2014 using JSHint (https://github.com/jshint/jshint/blob/master/examples/.jshintrc)
 */

//kde object for the surname
var kde;
//kde object for the forename
var forenamekde;

function onClickShowKDE(nametype) {
	"use strict";
	positionMapLoadingIndicators();
	if(nametype == "surname"){
		state.kdeSurname = $("#surname_input").val().toUpperCase();
		$("#surnamehistogram").hide();
		$("#surname_histogram_options").hide();
		if(state.kdeSurname){
			if($("#surnameMapType").val() == "regular")
				kdeQuery(nametype, "Regular");
			else if($("#surnameMapType").val() == "probabilistic")
				kdeQuery(nametype, "Probabilistic");
			$("#mapLegend").show();
			incomeRangesQuery("surname", $("#incomeDataType").val());
			if($("#surnameWordleType").val() == "specificIncomeRange"){
				$("#surnameWordleType").val("income");
				$("option[value='specificIncomeRange']").remove();
			}
			if($("#surnameWordleType").val() == "l2")
				updateSimilarMapWordle(nametype, "l2");
			else if($("#surnameWordleType").val() == "core")
				updateSimilarMapWordle(nametype, "core");
			else if($("#surnameWordleType").val() == "income")
				updateSimilarIncomeWordle(nametype);
			else if($("#surnameWordleType").val() == "distributiontool")
				updateSimilarIncomeWordleForTool(nametype);
		}
	}
	else if(nametype == "forename"){
		state.kdeForename = $("#forename_input").val().toUpperCase();
		$("#forename_distribution_bar").hide();
		$("#forename_histogram_options").hide();
		if(state.kdeForename){
			if($("#forenameMapType").val() == "regular")
				kdeQuery(nametype, "Regular");
			else if($("#forenameMapType").val() == "probabilistic")
				kdeQuery(nametype, "Probabilistic");
			incomeRangesQuery("forename");
			if($("#forenameWordleType").val() == "regularmap")
				updateSimilarMapWordle(nametype);
			else if($("#forenameWordleType").val() == "income")
				updateSimilarIncomeWordle(nametype);
		}
	}
}

function onClickCaptureButton(nametype) {
	"use strict";
	positionMapLoadingIndicators();
	if(nametype == "surname"){
		state.kdeSurname = $("#surname_input").val().toUpperCase();
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
	"use strict";
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
	};

	this.map = new google.maps.Map(document.getElementById(this.mapCanvasId),
			mapOptions);
	this.overlay = null;

	google.maps.event.addListenerOnce(this.map, "bounds_changed", function() {
		if (that.overlay !== null) {
			that.overlay.setMap(null);
			delete that.overlay;
		}
		that.overlay = new KDEOverlay(that.map, nameType);
	});
	// if(nameType == "surname"){
	// 	google.maps.event.addListener(this.map, "idle", function() {
	// 		if (state.kdeSurname !== "") {
	// 			kdeQuery(nameType);
	// 		}
	// 	});
	// }
}


var requestCount = 0;
$(window).load(function() {
	"use strict";
	kde = new KDEWindow("surname");
	forenamekde = new KDEWindow("forename");
	$("#allUSMap").hide();
	requestCount = 0;
	
	positionSocialMedia();
	positionMapLoadingIndicators();
	$("#surnameCaptureButton").hide();
	$("#forenameCaptureButton").hide();
	$("#surnamecapturedimagelink").hide();
	$("#forenamecapturedimagelink").hide();
	$("#surnameMedianIncomeText").hide();
	$("#surnameMeanIncomeText").hide();
	$("#surnamehistogram").hide();
	$("#surname_histogram_options").hide();
	$("#loadingsurname").hide();
	$("#loadingsurnamewordle").hide();
	$("#loadingsurnamehistogram").hide();
	$("#loadingforename").hide();
	$("#loadingforenamehistogram").hide();
	$("#wordleLegend").hide();
	$("#mapLegend").hide();
	$("#incomeZillowLegend").hide();
	$("#distributionToolContainer").hide();
	var curURL = "http://goo.gl/gOGEVJ";
	var completeTweet = "https://twitter.com/share?url=" + curURL + " &text=I found my surname heatmap and similar surnames to mine at " + curURL;
	$("#twitter").attr("href", completeTweet);
	var completeFacebook = "https://www.facebook.com/sharer/sharer.php?u='+encodeURIComponent(" + curURL + ")";
	$("#facebook").attr("href", completeFacebook);

	$( "#tabs" ).tabs( { disabled: [1] } );

	$(function() {
		$( "#tabs" ).tabs({widthStyle: "auto",
			activate: function(event, ui){
				var center;
				if(ui.newTab.index() === 0){
					center = kde.map.getCenter();
					google.maps.event.trigger(kde.map, "resize");
					kde.map.setCenter(center);
				}
				else if(ui.newTab.index() == 1){
					center = forenamekde.map.getCenter();
					google.maps.event.trigger(forenamekde.map, "resize");
					forenamekde.map.setCenter(center);
				}
			}
		});
	});

	if($("#surnamewordlediv").position().left < 30){
		$("#wordlehelp").attr("class", "helpdialog wordlehelpthin");
	}
	else{
		$("#wordlehelp").attr("class", "helpdialog wordlehelpwide");
	}

	generateMapLegend();
	generateIncomeLegend();
	generateIncomeZillowLegend();
	generateWordleLegend();
	generateDistributionTool();
});

function positionMapLoadingIndicators(){
	"use strict";
	var leftpos = $("#surnamemap").width();
	var toppos = $("#surnamemap").height();
	leftpos = leftpos/2;
	toppos = toppos/2 + $("#surnameContainer").height();
	$("#loadingsurname").css("left", "" + leftpos + "px");
	$("#loadingsurname").css("top", "" + toppos + "px");

	leftpos = $("#forenamemap").width();
	toppos = $("#forenamemap").height();
	leftpos = leftpos/2;
	toppos = toppos/2 + $("#forenameContainer").height();
	$("#loadingsurname").css("left", "" + leftpos + "px");
	$("#loadingsurname").css("top", "" + toppos + "px");
}

function positionSocialMedia(){
	"use strict";
	if($(document).width() < 800){
		$("#socialMedia").hide();
	}
	else{
		$("#socialMedia").show();
		var socialPos = $("#surnameContainer").width() - ($("#socialMedia").width() / 2);
		$("#socialMedia").css("left", ""+ socialPos + "px");
	}
}

window.onresize = function(event) {
	"use strict";
	if($("#surnamewordlediv").position().left < 30){
		$("#wordlehelp").attr("class", "helpdialog wordlehelpthin");
		$("#distributionToolContainer").css("margin-top", "0px");
		$("#distributionToolContainer").css("margin-left", "0px");
		$("#surnamewordlediv").css("margin-top", "10px");
		$("#surnamewordlediv").css("margin-left", "0px");
		$("#surnameContainerRight").css("margin-top", "10px");
	}
	else{
		$("#wordlehelp").attr("class", "helpdialog wordlehelpwide");
		$("#distributionToolContainer").css("margin-top", "55px");
		$("#distributionToolContainer").css("margin-left", "15px");
		$("#surnamewordlediv").css("margin-top", "45px");
		$("#surnamewordlediv").css("margin-left", "15px");
		$("#surnameContainerRight").css("margin-top", "0");
	}
	positionSocialMedia();
};

function positionLegends(){
	"use strict";
	var leftpos = $("#forenamemap").position().left;
	var toppos = $("#forenamemap").position().bottom;
	$("#mapLegend").css("left", "" + leftpos + "px");
	$("#mapLegend").css("top", "" + toppos + "px");
}

window.onscroll = function(event){
	"use strict";
	positionLegends();
};