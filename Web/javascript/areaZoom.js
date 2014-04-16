/**
 * @author Jose Ibarra, Jose.Ibarra@asu.edu, Arizona State University
 * Created in 2014

  Linted on April 7, 2014 using JSHint (https://github.com/jshint/jshint/blob/master/examples/.jshintrc)
 */

var largeStateZoom = 5;
var medStateZoom = 6;
var smallStateZoom = 7;

var stateCoords = {
	US : [new google.maps.LatLng(state.centerLat, state.centerLon), state.zoomLevel],
	AL : [new google.maps.LatLng(32.7000, -86.7000), smallStateZoom],
	AK : [new google.maps.LatLng(64.0000, -150.0000), largeStateZoom],
	AZ : [new google.maps.LatLng(34.0000, -112.0000), smallStateZoom],
	AR : [new google.maps.LatLng(34.8000, -92.2000), smallStateZoom],
	CA : [new google.maps.LatLng(37.0000, -120.2000), medStateZoom],
	CO : [new google.maps.LatLng(39.0000, -105.5000), smallStateZoom],
	CT : [new google.maps.LatLng(41.6000, -72.7000), smallStateZoom],
	DE : [new google.maps.LatLng(39.0000, -75.5000), smallStateZoom],
	FL : [new google.maps.LatLng(28.1000, -81.6000), smallStateZoom],
	GA : [new google.maps.LatLng(32.9605, -83.1132), smallStateZoom],
	HI : [new google.maps.LatLng(21.3114, -157.7964), smallStateZoom],
	ID : [new google.maps.LatLng(45.0000, -114.0000), medStateZoom],
	IL : [new google.maps.LatLng(40.0000, -89.0000), smallStateZoom],
	IN : [new google.maps.LatLng(40.0000, -86.0000), smallStateZoom],
	IA : [new google.maps.LatLng(42.0000, -93.0000), smallStateZoom],
	KS : [new google.maps.LatLng(38.5000, -98.0000), smallStateZoom],
	KY : [new google.maps.LatLng(37.5000, -85.0000), smallStateZoom],
	LA : [new google.maps.LatLng(31.0413, -91.8360), smallStateZoom],
	ME : [new google.maps.LatLng(45.5000, -69.0000), smallStateZoom],
	MD : [new google.maps.LatLng(39.0000, -76.7000), smallStateZoom],
	MA : [new google.maps.LatLng(42.3000, -71.8000), smallStateZoom],
	MI : [new google.maps.LatLng(44.6867, -85.0102), medStateZoom],
	MN : [new google.maps.LatLng(46.0000, -94.0000), medStateZoom],
	MS : [new google.maps.LatLng(33.0000, -90.0000), smallStateZoom],
	MO : [new google.maps.LatLng(38.5000, -92.5000), smallStateZoom],
	MT : [new google.maps.LatLng(46.8000, -110.0000), smallStateZoom],
	NE : [new google.maps.LatLng(41.2324, -98.4160), smallStateZoom],
	NV : [new google.maps.LatLng(39.0000, -117.0000), medStateZoom],
	NH : [new google.maps.LatLng(44.0000, -71.5000), smallStateZoom],
	NJ : [new google.maps.LatLng(40.0000, -74.5000), smallStateZoom],
	NM : [new google.maps.LatLng(34.2000, -106.0000), smallStateZoom],
	NY : [new google.maps.LatLng(42.3482, -75.1890), smallStateZoom],
	NC : [new google.maps.LatLng(35.5000, -80.0000), smallStateZoom],
	ND : [new google.maps.LatLng(47.5000, -100.5000), smallStateZoom],
	OH : [new google.maps.LatLng(40.5000, -82.5000), smallStateZoom],
	OK : [new google.maps.LatLng(35.5000, -98.0000), smallStateZoom],
	OR : [new google.maps.LatLng(44.0000, -120.5000), smallStateZoom],
	PA : [new google.maps.LatLng(41.0000, -77.5000), smallStateZoom],
	RI : [new google.maps.LatLng(41.7000, -71.5000), smallStateZoom],
	SC : [new google.maps.LatLng(34.0000, -81.0000), smallStateZoom],
	SD : [new google.maps.LatLng(44.5000, -100.0000), smallStateZoom],
	TN : [new google.maps.LatLng(36.0000, -86.0000), smallStateZoom],
	TX : [new google.maps.LatLng(31.0000, -100.0000), medStateZoom],
	UT : [new google.maps.LatLng(39.5000, -111.5000), smallStateZoom],
	VT : [new google.maps.LatLng(44.2035, -72.5623), smallStateZoom],
	VA : [new google.maps.LatLng(37.5000, -79.0000), smallStateZoom],
	WA : [new google.maps.LatLng(47.5000, -120.5000), smallStateZoom],
	WV : [new google.maps.LatLng(39.0000, -80.5000), smallStateZoom],
	WI : [new google.maps.LatLng(44.5000, -89.5000), smallStateZoom],
	WY : [new google.maps.LatLng(43.0000, -107.5000), smallStateZoom]
};

function zoomToArea(areaName, nameType){
	"use strict";
	state.forenameLoaded = false;
	state.surnameLoaded = false;
	$( "#tabs" ).tabs("disable", 2);
	var area = stateCoords[areaName];
	if(area === null || typeof(area) == "undefined")
		return;
	var coords = area[0];
	
	kde.map.setCenter(area[0]);
	kde.map.setZoom(area[1]);
	kde2.map.setCenter(area[0]);
	kde2.map.setZoom(area[1]);
	kde3.map.setCenter(area[0]);
	kde3.map.setZoom(area[1]);
}