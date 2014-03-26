/**
 * @author Feng Wang
 * @author Jose Ibarra, Jose.Ibarra@asu.edu, Arizona State University
 * Modified on September 10, 2013 and after into 2014
 */
var disableQueries = false;
var isCaptured = false;
function kdeQuery(nameType, mapType) {
	if(disableQueries)
		return;
	//resetView();

	// the coordinates for the bounds
	var boundsOnEarth = null;
	var width = null;
	var height = null;
	var queryData = null;
	if(nameType == "surname"){
		if (typeof (kde.overlay) == 'undefined' || kde.overlay == null) {
			return;
		}
		var width = document.getElementById("surnamemap").clientWidth;
		var height = document.getElementById("surnamemap").clientHeight;
		boundsOnEarth = kde.map.getBounds();

		queryData = {
			zoom_level : kde.map.getZoom(),
			surname : state.kdeSurname,
			latsw : boundsOnEarth.getSouthWest().lat(),
			lngsw : boundsOnEarth.getSouthWest().lng(),
			latne : boundsOnEarth.getNorthEast().lat(),
			lngne : boundsOnEarth.getNorthEast().lng(),
			latcenter : boundsOnEarth.getCenter().lat(),
			lngcenter : boundsOnEarth.getCenter().lng(),
			width : width,
			height : height
		};
	}
	else if(nameType == "forename"){
		if (typeof (forenamekde.overlay) == 'undefined' || forenamekde.overlay == null) {
			return;
		}
		var width = document.getElementById("forenamemap").clientWidth;
		var height = document.getElementById("forenamemap").clientHeight;
		boundsOnEarth = forenamekde.map.getBounds();

		queryData = {
			zoom_level : forenamekde.map.getZoom(),
			forename : state.kdeForename,
			latsw : boundsOnEarth.getSouthWest().lat(),
			lngsw : boundsOnEarth.getSouthWest().lng(),
			latne : boundsOnEarth.getNorthEast().lat(),
			lngne : boundsOnEarth.getNorthEast().lng(),
			latcenter : boundsOnEarth.getCenter().lat(),
			lngcenter : boundsOnEarth.getCenter().lng(),
			width : width,
			height : height,
		};
	}
	kdeQueryImp(queryData, nameType, mapType);
}

function resetView(){
	$('#captureButton').hide();
	$('#capturedimagelink').hide();
	$('#surnameMedianIncomeText').hide();
	$('#surnameMeanIncomeText').hide();
	var curURL = document.URL;
	var completeURL = "https://twitter.com/share?url=" + curURL + "&text=I found my surname density on " + curURL;
	$('#twitter').attr('href', completeURL);
	if(isCaptured){
		$('#footer').css('height', '10%');
		$('#centercontainer').css('height', '90%');
		$('#surnamemap').css('height', '100%');
		var body=document.getElementsByTagName('body')[0];
		body.style.height='100%';
		disableQueries = true;
		kde.map.setCenter(new google.maps.LatLng(state.centerLat, state.centerLon));
		setTimeout(function(){ 	disableQueries = false }, 1000);
		isCaptured=false;
	}
}

function kdeQueryImp(configuredData, nameType, mapType) {
	if(nameType == "surname"){
		var scriptLocation = state.serviceBase + 'services/querySurname' + mapType +'?callback=?';
		$('#loadingsurname').show();
	}
	else if(nameType == "forename"){
		var scriptLocation = state.serviceBase + 'services/queryForename?callback=?';
		$('#loadingforename').show();
	}

	$.ajax(scriptLocation, {
		data : configuredData,
		dataType : 'jsonp',
		global: true,

		error : function(jqXHR, textStatus, errorThrown) {
			console.log(textStatus);
			console.log(jqXHR);
			console.log(errorThrown);
			$('#loadingsurname').hide();
			alert('Error');
		},
		success : function(data, textStatus, jqXHR) {
			updateKDEMap(data, nameType, mapType);
			// updateIncome(data, nameType);
			if(nameType == "surname"){
				$('#loadingsurname').hide();
				state.surnameLoaded = true;
			}
			else if (nameType == "forename"){
				$('#loadingforename').hide();
				state.forenameLoaded = true;
			}

			if(state.surnameOverlayImage != null && state.forenameOverlayImage != null
					&& state.surnameOverlayImage != 'blank.png' && state.forenameOverlayImage != 'blank.png'
					&& state.surnameLoaded == true && state.forenameLoaded == true){
				$( "#tabs" ).tabs("enable", 2);
			}
		}
	});
}

function updateKDEMap(data, nameType, mapType) {
	if(nameType == "surname"){
		if(data.image == null || data.image == 'blank.png'){
			alert("The image and income data for this name could not be loaded. The name's " +
		      	"population may be too small or the name may not exist in the database.");
		}
		state.surnameOverlayImage = state.serviceBase + 'resources/' + data.image;
		
		// refresh
		if (kde.overlay != null) {
			kde.overlay.setMap(null);
			delete kde.overlay;
		}
		kde.overlay = new KDEOverlay(kde.map, "surname");
		$('#captureButton').show();
	}
	else if(nameType == "forename"){
		state.forenameOverlayImage = state.serviceBase + "image/kdecacheforename/" + data.image;
		// refresh
		if (forenamekde.overlay != null) {
			forenamekde.overlay.setMap(null);
			delete forenamekde.overlay;
		}
		forenamekde.overlay = new KDEOverlay(forenamekde.map, "forename");
		$('#forenameCaptureButton').show();
	}
	else if(nameType == "combined"){
		state.combinedOverlayImage = data.image;
		// refresh
		if (kde3.overlay != null) {
			kde3.overlay.setMap(null);
			delete kde3.overlay;
		}
		kde3.overlay = new KDEOverlay(kde3.map, "combined");
	}

	if(state.surnameOverlayImage != 'blank.png' && state.forenameOverlayImage != 'blank.png')
		$('#combine_submit').show();
	else
		$('#combine_submit').hide();
	positionSocialMedia();
}

//Formats money for easier reading
Number.prototype.formatMoney = function(c, d, t){
var n = this, 
    c = isNaN(c = Math.abs(c)) ? 2 : c, 
    //if d is undefined, make it the default decimal value (.)
    d = d == undefined ? "." : d, 
    //if t is undefined, make it the default number separator(,)
    t = t == undefined ? "," : t, 
    //Add a - if the number is negative
    s = n < 0 ? "-" : "", 
    i = parseInt(n = Math.abs(+n || 0).toFixed(c)) + "", 
    j = (j = i.length) > 3 ? j % 3 : 0;
   return s + (j ? i.substr(0, j) + t : "") + i.substr(j).replace(/(\d{3})(?=\d)/g, "$1" + t) + (c ? d + Math.abs(n - i).toFixed(c).slice(2) : "");
 };

function updateIncome(data, nameType){
	var mean = data.averageMean.formatMoney(2);
	var median = data.averageMedian.formatMoney(2);
	if(nameType=="surname"){
		$('#surnameMeanIncomeText').text("Average Mean Income: $" + mean);
		$('#surnameMedianIncomeText').text("Average Median Income: $" + median);
		$('#surnameMeanIncomeText').show();
		$('#surnameMedianIncomeText').show();
	}
	else if (nameType=="forename"){
		$('#forenameMeanIncomeText').text("Average Mean Income: $" + mean);
		$('#forenameMedianIncomeText').text("Average Median Income: $" + median);
		$('#forenameMeanIncomeText').show();
		$('#forenameMedianIncomeText').show();
	}

}

function captureUpload(nameType) {
	if (typeof (kde.overlay) == 'undefined' || kde.overlay == null) {
		return;
	}

	// the coordinates for the bounds
	var boundsOnEarth = null;
	var name = null;;
	var imageLoc = null;
	var zoomLevel = null;
	if(nameType=="surname"){
		boundsOnEarth = kde.map.getBounds();
		name = state.kdeSurname;
		imageLoc = state.surnameOverlayImage;
		zoomLevel = kde.map.getZoom();
	}
	else if (nameType=="forename"){
		boundsOnEarth = forenamekde.map.getBounds();
		name = state.kdeForename;
		imageLoc = state.forenameOverlayImage;
		zoomLevel = forenamekde.map.getZoom();
	}

	var width = document.getElementById(nameType+"map").clientWidth;
	var height = document.getElementById(nameType+"map").clientHeight;

	var queryData = {
		zoom_level : zoomLevel,
		surname : name,
		latsw : boundsOnEarth.getSouthWest().lat(),
		lngsw : boundsOnEarth.getSouthWest().lng(),
		latne : boundsOnEarth.getNorthEast().lat(),
		lngne : boundsOnEarth.getNorthEast().lng(),
		latcenter : boundsOnEarth.getCenter().lat(),
		lngcenter : boundsOnEarth.getCenter().lng(),
		width : width,
		height : height,
		image : imageLoc
	};
	captureUploadImp(queryData, nameType);
}

function captureUploadImp(configuredData, nameType) {
	if(nameType=="surname"){
		scriptLocation = state.serviceBase + 'services/surnamebuildmap?callback=?';
		$('#capturedimagelink').hide();
		$('#loadingsurname').show();
	}
	else if(nameType=="forename"){
		scriptLocation = state.serviceBase + 'services/forenamebuildmap?callback=?';
		$('#forenameCapturedimagelink').hide();
		$('#loadingforename').show();
	}

	$.ajax(scriptLocation, {
		data : configuredData,
		dataType : 'jsonp',
		global: true,

		error : function(jqXHR, textStatus, errorThrown) {
			console.log(textStatus);
			console.log(jqXHR);
			console.log(errorThrown);
			alert('Error');
		},
		success : function(data, textStatus, jqXHR) {
			console.log(data.filename);
			console.log(data.url);
			var completeURL = state.httpServiceBase + data.url;
			console.log(completeURL);
			var center = null;

			if(nameType=="surname"){
				state.surnameCaptureImage = completeURL;
				center = kde.map.getBounds().getCenter();
				$('#loadingsurname').hide();
				$('#captureButton').hide();
				$('#capturedimagelink').attr('href', completeURL);
				$('#capturedimagelink').show();
			}
			else if(nameType=="forename"){
				state.forenameCaptureImage = completeURL;
				center = forenamekde.map.getBounds().getCenter();
				$('#loadingforename').hide();
				$('#forenameCaptureButton').hide();
				$('#forenameCapturedimagelink').attr('href', completeURL);
				$('#forenameCapturedimagelink').show();
			}
		}
	});
}

function kdeCombineQuery() {
	if(state.surnameOverlayImage == null || state.forenameOverlayImage == null
			|| state.surnameOverlayImage == 'blank.png' || state.forenameOverlayImage == 'blank.png'){
		return;
	}
	if (typeof (kde3.overlay) == 'undefined' || kde3.overlay == null) {
		return;
	}

	queryData = {
		map1Loc : state.surnameOverlayImage,
		map2Loc : state.forenameOverlayImage
	};

	kdeCombineQueryImp(queryData);
}

function kdeCombineQueryImp(configuredData){
	console.log(configuredData);

	var scriptLocation = state.serviceBase + 'services/queryCombineNames?callback=?';

	$.ajax(scriptLocation, {
		data : configuredData,
		dataType : 'jsonp',
		global: true,

		error : function(jqXHR, textStatus, errorThrown) {
			console.log(textStatus);
			console.log(jqXHR);
			console.log(errorThrown);
			alert('Error');
		},
		success : function(data, textStatus, jqXHR) {
			updateKDEMap(data, "combined");
			console.log(data.image);
		}
		
	});
}

function combineMapsCapture(){
	if (typeof (kde.overlay) == 'undefined' || kde.overlay == null) 
		return;
	if (typeof (forenamekde.overlay) == 'undefined' || forenamekde.overlay == null)
		return;
	var boundsOnEarth1 = kde.map.getBounds();
	var boundsOnEarth2 = forenamekde.map.getBounds()
	var latsw1 = boundsOnEarth1.getSouthWest().lat();
	var latsw2 = boundsOnEarth2.getSouthWest().lat();
	var lngsw1 = boundsOnEarth1.getSouthWest().lng();
	var lngsw2 = boundsOnEarth2.getSouthWest().lng();
	var latne1 = boundsOnEarth1.getNorthEast().lat();
	var latne2 = boundsOnEarth2.getNorthEast().lat();
	var lngne1 = boundsOnEarth1.getNorthEast().lng();
	var lngne2 = boundsOnEarth2.getNorthEast().lng();
	if((latsw1 != latsw2) || (lngsw1 != lngsw2) || (latne1 != latne2) || (lngne1 != lngne2))
		return;
	if(kde.map.getZoom() != forenamekde.map.getZoom())
		return;

	var zoomLevel = kde.map.getZoom();

	var queryData = {
		map1Loc : state.surnameOverlayImage,
		mapLoc : state.forenameOverlayImage,
		latsw : latsw1,
		lngsw : lngsw1,
		latne : latne1,
		lngne : lngne1,
		zoom : zoomLevel
	};

	combineMapsCaptureImp(queryData);
}

function combineMapsCaptureImp(configuredData){
	scriptLocation = state.serviceBase + 'services/combinebuildmap?callback=?';
	$('#loadingCombine').show();
	positionSocialMedia();

	$.ajax(scriptLocation, {
		data : configuredData,
		dataType : 'jsonp',
		global: true,

		error : function(jqXHR, textStatus, errorThrown) {
			console.log(textStatus);
			console.log(jqXHR);
			console.log(errorThrown);
			alert('Error');
			$('#loadingCombine').hide()
			positionSocialMedia();
		},
		success : function(data, textStatus, jqXHR) {
			var completeURL = state.httpServiceBase + data.url;
			state.combineCaptureImage = completeURL;
			$('#loadingCombine').hide()
			$('#combine_submit').hide();
			$('#combineCapturedImageLink').attr('href', completeURL);
			$('#combineCapturedImageLink').show();
			positionSocialMedia();
		}
	});
}

function incomeRangesQuery(nameType){
	if(typeof (nameType) == 'undefined' || name == null){
		return;
	}

	if(nameType == 'surname'){
		if(typeof (state.kdeSurname) == 'undefined' || state.kdeSurname == null){
			return;
		}
		boundsOnEarth = kde.map.getBounds();
		queryData = {
			surname : state.kdeSurname
		};
	}
	else if(nameType == 'forename'){
		if(typeof (state.kdeForename) == 'undefined' || state.kdeForename == null){
			return;
		}
		boundsOnEarth = forenamekde.map.getBounds();
		queryData = {
			forename : state.kdeForename
		};
	}
	incomeRangesQueryImp(queryData, nameType);
}

function incomeRangesQueryImp(configuredData, nameType){
	if(typeof (nameType) == 'undefined' || name == null){
		return;
	}
	if(typeof (configuredData) == 'undefined' || configuredData == null){
		return;
	}
	var scriptLocation;
	if(nameType == "surname"){
		$('#loadingsurnamehistogram').show();
		scriptLocation = state.serviceBase + 'services/queryIncomeRangesSurname?callback=?';
	}
	else if(nameType == "forename"){
		$('#loadingforenamehistogram').show();
		scriptLocation = state.serviceBase + 'services/queryIncomeRangesForename?callback=?';
	}
	else{
		return;
	}
	$.ajax(scriptLocation, {
		data : configuredData,
		dataType : 'jsonp',
		global: true,

		error : function(jqXHR, textStatus, errorThrown) {
			console.log(textStatus);
			console.log(jqXHR);
			console.log(errorThrown);
			if(nameType == "surname")
				$('#loadingsurnamehistogram').hide();
			else if(nameType == "forename")
				$('#loadingforenamehistogram').hide();
			alert('Error');
		},
		success : function(data, textStatus, jqXHR) {
			state.surnameIncomeRanges = data;
			generateIncomeDistributionBar(nameType, data);
		}
	});
}