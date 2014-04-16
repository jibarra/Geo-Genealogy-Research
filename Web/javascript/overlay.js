/**
 * @author Feng Wang, fwang49@asu.edu, Arizona State University
 * @author Jose Ibarra, Jose.Ibarra@asu.edu, Arizona State University
 * Modified on September 9, 2013 and after into 2014
 Linted on April 8, 2014 using JSHint (https://github.com/jshint/jshint/blob/master/examples/.jshintrc)
 */

function KDEOverlay(map, nametype) {
	"use strict";
	this.map_ = map;
	this.bounds_ = this.map_.getBounds();
	this.div_ = null;
	this.setMap(this.map_);
	this.overlayNameType = nametype;
}

KDEOverlay.prototype = new google.maps.OverlayView();

KDEOverlay.prototype.onAdd = function() {
	"use strict";
	var div = document.createElement("div");
	div.style.border = "none";
	div.style.borderWidth = "0px";
	div.style.position = "absolute";

	var img = document.createElement("img");

	if(this.overlayNameType == "surname"){
		if (typeof (state.surnameOverlayImage) == "undefined" ||
				state.surnameOverlayImage === null) {
			img.src = state.serviceBase + "image/kdecachesurname/" + "blank.png";
		} else {
			img.src = state.surnameOverlayImage;
		}
	}
	else if(this.overlayNameType == "forename"){
		if (typeof (state.forenameOverlayImage) == "undefined" ||
				state.forenameOverlayImage === null) {
			img.src = state.serviceBase + "image/kdecacheforename/" + "blank.png";
		} else {
			img.src = state.forenameOverlayImage;
		}
		// alert(img.src);
	}
	else if(this.overlayNameType == "combined"){
		if (typeof (state.forenameOverlayImage) == "undefined" ||
				state.forenameOverlayImage === null) {
			img.src = state.serviceBase + "image/kdecachecombined/" + "blank.png";
		} else {
			img.src = state.serviceBase + "image/kdecachecombined/" + state.combinedOverlayImage;
		}
		// alert(img.src);
	}

	img.style.width = "100%";
	img.style.height = "100%";
	

	img.style.opacity = state.kdeOpacity;
	
	div.appendChild(img);

	this.div_ = div;

	var panes = this.getPanes();
	panes.overlayImage.appendChild(div);
};

KDEOverlay.prototype.draw = function() {
	"use strict";
	var overlayProjection = this.getProjection();
	var sw = overlayProjection
			.fromLatLngToDivPixel(this.bounds_.getSouthWest());
	var ne = overlayProjection
			.fromLatLngToDivPixel(this.bounds_.getNorthEast());

	var div = this.div_;
	div.style.left = sw.x + "px";
	div.style.top = ne.y + "px";
	div.style.width = (ne.x - sw.x) + "px";
	div.style.height = (sw.y - ne.y) + "px";

	positionMapLoadingIndicators();
};

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
	$("#loadingforename").css("left", "" + leftpos + "px");
	$("#loadingforename").css("top", "" + toppos + "px");
}

KDEOverlay.prototype.show = function(nametype) {
	"use strict";
	if (this.div_) {
		this.div_.style.visibility = "visible";
	}
};

KDEOverlay.prototype.onRemove = function(nametype) {
	"use strict";
	this.div_.parentNode.removeChild(this.div_);
	this.div_ = null;
};