/**
 * @author Feng Wang, fwang49@asu.edu, Arizona State University
 * @author Jose Ibarra, Jose.Ibarra@asu.edu, Arizona State University
 * Modified on September 9, 2013 and after
 */

function KDEOverlay(map) {
	this.map_ = map;
	this.bounds_ = this.map_.getBounds();
	this.div_ = null;
	this.setMap(this.map_);
}
KDEOverlay.prototype = new google.maps.OverlayView();
KDEOverlay.prototype.onAdd = function() {
	var div = document.createElement('div');
	div.style.border = "none";
	div.style.borderWidth = "0px";
	div.style.position = "absolute";

	var img = document.createElement("img");
	if (typeof (state.overlayImage) == 'undefined'
			|| state.overlayImage == null) {
		img.src = state.serviceBase + 'image/kdecache/' + 'blank.png';
	} else {
		img.src = state.serviceBase + 'image/kdecache/' + state.overlayImage;
	}
	img.style.width = "100%";
	img.style.height = "100%";
	

	img.style.opacity = state.kdeOpacity;
	
	div.appendChild(img);

	this.div_ = div;

	var panes = this.getPanes();
	panes.overlayImage.appendChild(div);
}
KDEOverlay.prototype.draw = function() {
	var overlayProjection = this.getProjection();
	var sw = overlayProjection
			.fromLatLngToDivPixel(this.bounds_.getSouthWest());
	var ne = overlayProjection
			.fromLatLngToDivPixel(this.bounds_.getNorthEast());

	var div = this.div_;
	div.style.left = sw.x + 'px';
	div.style.top = ne.y + 'px';
	div.style.width = (ne.x - sw.x) + 'px';
	div.style.height = (sw.y - ne.y) + 'px';

	var leftpos = $('#centercontainter').width();
	var toppos = $('#centercontainter').height();
	leftpos = leftpos/2 - 50;
	toppos = toppos/2 + 50;
	$('#loading').css('left', '' + leftpos + 'px');
	$('#loading').css('top', '' + toppos + 'px');
}
KDEOverlay.prototype.show = function() {
	if (this.div_) {
		this.div_.style.visibility = "visible";
	}
}
KDEOverlay.prototype.onRemove = function() {
	this.div_.parentNode.removeChild(this.div_);
	this.div_ = null;
}
