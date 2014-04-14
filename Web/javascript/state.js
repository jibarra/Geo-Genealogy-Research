/**
 * @author Feng Wang
 * @author Jose Ibarra, Jose.Ibarra@asu.edu, Arizona State University
 * Modified on September 10, 2013 and after into 2014

  Linted on April 7, 2014 using JSHint (https://github.com/jshint/jshint/blob/master/examples/.jshintrc)

 */

var state = {
	dateFormat : "%d/%m/%z %H:%i",
	//These are the variables for the maps' center lat and lon
	centerLat : 38.513788,
	centerLon : -97.078629,
	//This is the variable for the maps' zoom level
	//Lower numbers show more of the world (more zoomed out)
	zoomLevel : 4,
	surnameOverlayImage : "images/blank.png",
	forenameOverlayImage : "images/blank.png",
	combinedOverlayImage : "images/blank.png",
	surnameCaptureImage : "images/blank.png",
	forenameCaptureImage : "images/blank.png",
	combineCaptureImage : "images/blank.png",
	surnameLoaded : false,
	forenameLoaded : false,
	kdeSurname : "",
	kdeForename: "",
	surnameData : null,
	forenameData : null,
	kdeOpacity : 0.4,
	kdeKRatio : 1,
	kdeMaxBandwidth : 120000,
	showMarker : false,
	markers : [],
	kdeColor : "quantile",
	//This is the location for the web services. If this is set up
	//using your own computer as the server, the address should be something "like http://localhost:8080/nameservice/"
	serviceBase: "http://localhost:8080/webservice/",
	// serviceBase: "http://ec2-54-201-211-102.us-west-2.compute.amazonaws.com:8080/surnameservice/",
	incomeServiceBase : "",
};