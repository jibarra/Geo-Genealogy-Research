/**
 * 
 */
function updateNameWordle(nametype) {
	var scriptLocation = state.servicebase + "surnameWordle?callback=?";
	var configuredData = {
			keyword: state.keyword,
			limit: state.hashtagWordleParam.limit
	}
	if (document.getElementById('wordleSVG')) {
  		 var el = document.getElementById('wordleSVG');
  	     el.parentNode.removeChild(el);
  	}
	$("#wordleLoading").show();
	$.ajax(scriptLocation, {
		data: configuredData,
		dataType: "jsonp",

		 error: function (jqXHR, textStatus, errorThrown) {
			 //do something when error...
			 console.log(errorThrown);
		 },
	     success: function (data, textStatus, jqXHR) {
	    	 var width = $("#wordleChart").width() - 100;
	    	 var height = $("#wordleChart").height() - 100;
	    	 
	    	 var size = d3.scale.log().range([20, 200]);
	    	 size.domain(d3.extent(data, function(d){
	    		 return d.count;
	    	 }));
	    	 
	    	 data.forEach(function(d) {
	    		 d.count = Math.floor(size(d.count));
	    	 });
	    	 d3.layout.cloud().size([width, height]).words(data).rotate(function() {
	    		 return ~~(Math.random() * 2) * 90;
	    	 }).font("Impact").fontSize(function(d){
	    		 return d.count;
	    	 }).on("end", drawWordle).start();
	    	 $("#wordleLoading").hide();
	     }
	});
	
	function drawWordle(words) {
		var fill = d3.scale.category20();
		var width = $("#wordleChart").width() - 100;
    	var height = $("#wordleChart").height() - 100;
    	
    	svg = d3.select("#wordleChart").append("svg")
    		.attr("id", "wordleSVG")
    		.attr("width", width)
    		.attr("height",	height);
    	svg.append("rect")
	    	.attr("width", "100%")
	    	.attr("height", "100%")
	    	.attr("fill", "black");
    	svg.append("g")
    		.attr("transform", "translate(" + (width / 2) + ',' + (height / 2) + ")")
    		.selectAll("text")
    		.data(words)
    		.enter()
    		.append("text")
    		.style("font-size", function(d) {
    			return d.count + "px";})
			.style("font-family", "Impact")
			.style("fill", function(d, i) {
					return fill(i);})
			.attr("text-anchor", "middle")
			.attr("transform", function(d) {
					return "translate(" + [ d.x, d.y ] + ")rotate(" + d.rotate + ")";})
			.text(function(d) {
					return d.text;
			});
	}
}