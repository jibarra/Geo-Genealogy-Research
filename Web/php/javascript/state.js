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
	kdeSurname : '',
	kdeForename: '',
	kdeOpacity : 0.4,
	kdeKRatio : 1,
	kdeMaxBandwidth : 120000,
	showMarker : false,
	markers : [],
	kdeColor : 'quantile',
	//This is the location for the web services. If this is set up
	//using your own computer as the server, this address should remain unchanged
	serviceBase: 'http://localhost:8080/surnameservice/',
	httpServiceBase: 'http://localhost:8080/surnameservice/'
}