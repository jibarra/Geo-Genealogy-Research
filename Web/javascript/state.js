var state = {
	dateFormat : "%d/%m/%z %H:%i",
	//These are the variables for the maps' center lat and lon
	centerLat : 38.513788,
	centerLon : -97.078629,
	//This is the variable for the maps' zoom level
	//Lower numbers show more of the world (more zoomed out)
	zoomLevel : 4,
	surnameOverlayImage : 'blank.png',
	forenameOverlayImage : 'blank.png',
	combinedOverlayImage : 'blank.png',
	surnameCaptureImage : 'blank.png',
	forenameCaptureImage : 'blank.png',
	combineCaptureImage : 'blank.png',
	surnameLoaded : false,
	forenameLoaded : false,
	kdeSurname : '',
	kdeForename: '',
	surnameData : null,
	forenameData : null,
	kdeOpacity : 0.4,
	kdeKRatio : 1,
	kdeMaxBandwidth : 120000,
	showMarker : false,
	markers : [],
	kdeColor : 'quantile',
	//This is the location for the web services. If this is set up
	//using your own computer as the server, the address should be something 'like http://localhost:8080/nameservice/'
	// serviceBase: 'http://localhost:8080/nameservice/',
	// httpServiceBase: 'http://localhost:8080/nameservice/'
	serviceBase: 'http://ec2-54-201-211-102.us-west-2.compute.amazonaws.com:8080/surnameservice/',
	httpServiceBase: 'http://ec2-54-201-211-102.us-west-2.compute.amazonaws.com:8080/surnameservice/'
}