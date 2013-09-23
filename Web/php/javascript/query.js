/**
 * @author Feng Wang
 */

function kdeQuery() {
	if (typeof (kde.overlay) == 'undefined' || kde.overlay == null) {
		return;
	}

	// the coordinates for the bounds
	var boundsOnEarth = kde.map.getBounds();
	var width = document.getElementById("main").clientWidth;
	var height = document.getElementById("main").clientHeight;

	var queryData = {
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

	kdeQueryImp(queryData);
}

function kdeQueryImp(configuredData) {
	var scriptLocation = state.serviceBase + 'services/query?callback=?';
	$('#loading').show();
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
			updateKDEMap(data);
			$('#loading').hide();
		}
	});
}

function updateKDEMap(data) {
	state.overlayImage = data.image;
	// refresh
	if (kde.overlay != null) {
		kde.overlay.setMap(null);
		delete kde.overlay;
	}
	kde.overlay = new KDEOverlay(kde.map);
	$('#captureButton').show();
}

function captureUpload() {
	if (typeof (kde.overlay) == 'undefined' || kde.overlay == null) {
		return;
	}

	// the coordinates for the bounds
	var boundsOnEarth = kde.map.getBounds();
	var width = document.getElementById("main").clientWidth;
	var height = document.getElementById("main").clientHeight;

	var queryData = {
		zoom_level : kde.map.getZoom(),
		surname : state.kdeSurname,
		latsw : boundsOnEarth.getSouthWest().lat(),
		lngsw : boundsOnEarth.getSouthWest().lng(),
		latne : boundsOnEarth.getNorthEast().lat(),
		lngne : boundsOnEarth.getNorthEast().lng(),
		latcenter : boundsOnEarth.getCenter().lat(),
		lngcenter : boundsOnEarth.getCenter().lng(),
		width : width,
		height : height,
		image : state.overlayImage
	};
	captureUploadImp(queryData);
}

function captureUploadImp(configuredData) {
	scriptLocation = state.serviceBase + 'services/buildmap?callback=?';
	$('#loading').show();
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
			$('#loading').hide();
			$('#captureButton').hide();
			$('#capturedimagelink').attr('href', completeURL);
			// $('#capturedimagelink').attr('target', '_blank');
			$('#capturedimagelink').show();
		}
	});
}
