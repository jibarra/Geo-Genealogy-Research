/**
 * @author Feng Wang
 * @author Jose Ibarra, Jose.Ibarra@asu.edu, Arizona State University
 * Modified on September 10, 2013 and after
 */
var disableQueries = false;
var isCaptured = false;
function kdeQuery(nameType) {
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
		if (typeof (kde2.overlay2) == 'undefined' || kde2.overlay2 == null) {
			return;
		}
		var width = document.getElementById("forenamemap").clientWidth;
		var height = document.getElementById("forenamemap").clientHeight;
		boundsOnEarth = kde2.map2.getBounds();

		queryData = {
			zoom_level : kde2.map2.getZoom(),
			forename : state.kdeForename,
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

	kdeQueryImp(queryData, nameType);
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

function kdeQueryImp(configuredData, nameType) {
	if(nameType == "surname"){
		var scriptLocation = state.serviceBase + 'services/querySurname?callback=?';
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
			alert('Error');
		},
		success : function(data, textStatus, jqXHR) {
			updateKDEMap(data, nameType);
			updateIncome(data, nameType);
			if(nameType == "surname")
				$('#loadingsurname').hide();
			else if (nameType == "forename")
				$('#loadingforename').hide();
		}
	});
}

function updateKDEMap(data, nameType) {
	if(nameType == "surname"){
		state.surnameOverlayImage = data.image;
		// refresh
		if (kde.overlay != null) {
			kde.overlay.setMap(null);
			delete kde.overlay;
		}
		kde.overlay = new KDEOverlay(kde.map, "surname");
		$('#captureButton').show();
	}
	else if(nameType == "forename"){
		state.forenameOverlayImage = data.image;
		// refresh
		if (kde2.overlay2 != null) {
			kde2.overlay2.setMap(null);
			delete kde2.overlay2;
		}
		kde2.overlay2 = new KDEOverlay(kde2.map2, "forename");
		$('#forenameCaptureButton').show();
	}
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
		boundsOnEarth = kde2.map2.getBounds();
		name = state.kdeForename;
		imageLoc = state.forenameOverlayImage;
		zoomLevel = kde2.map2.getZoom();
	}

	var width = document.getElementById("surnamemap").clientWidth;
	var height = document.getElementById("surnamemap").clientHeight;

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
		$('#loadingsurname').show();
	}
	else if(nameType=="forename"){
		scriptLocation = state.serviceBase + 'services/forenamebuildmap?callback=?';
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
			// var accessToken = FB.getAuthResponse()['accessToken'];
			// var wallPost = {
			// 	message : 'surname map for : ' + state.kdeSurname,
			// 	status : 'success',
			// 	access_token : accessToken,
			// 	url : completeURL
			// };
			// FB.api('/me/photos', 'post', wallPost, function(response) {
			// 	if(!response || response.error) {
			// 		console.log('Error occured: ' + JSON.stringify(response.error));
			// 	}else {
			// 		console.log('Post ID: ' + response);
			// 		alert('posted');
			// 	}
			// });
			var center = null;

			if(nameType=="surname"){
				center = kde.map.getBounds().getCenter();
				$('#loadingsurname').hide();
				$('#captureButton').hide();
				$('#capturedimagelink').attr('href', completeURL);
				$('#capturedimage').attr('src', completeURL);
				$('#capturedimagelink').show();
				disableQueries = true;
				kde.map.setCenter(new google.maps.LatLng(35.8, -97.51808));
			}
			else if(nameType=="forename"){
				center = kde2.map2.getBounds().getCenter();
				$('#loadingforename').hide();
				$('#forenameCaptureButton').hide();
				$('#forenameCapturedimagelink').attr('href', completeURL);
				$('#forenameCapturedimage').attr('src', completeURL);
				$('#forenameCapturedimagelink').show();
				disableQueries = true;
				kde2.map2.setCenter(new google.maps.LatLng(35.8, -97.51808));
			}
			
			
			$('#footer').css('height', '33%');
			$('#centercontainer').css('height', '67%');
			$('#surnamemap').css('height', '100%');
			var body=document.getElementsByTagName('body')[0];
			body.style.height='81.3%';
			isCaptured=true;
			//Once center is moved to original position, enable queries
			setTimeout(function(){ 	disableQueries = false }, 1000);

			// var curURL = document.URL;
			// var tweetURL = "https://twitter.com/share?url=" + curURL + "&text=I found my surname density at " + curURL
			// 				+ ". Here's a picture: " + completeURL;
			// $('#twitter').attr('href', tweetURL);
		}
	});
}
