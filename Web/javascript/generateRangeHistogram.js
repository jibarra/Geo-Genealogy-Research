 // @author Jose Ibarra, Arizona State University, Jose.Ibarra@asu.edu
 // created on November 25, 2013 ane edited into 2014

 /*
 Generates the histogram for the income ranges. This works for census and zillow.
 */

 //Linted on April 7, 2014 using JSHint (https://github.com/jshint/jshint/blob/master/examples/.jshintrc)

var OrRd = {
3: ["#fee8c8","#fdbb84","#e34a33"],
4: ["#fef0d9","#fdcc8a","#fc8d59","#d7301f"],
5: ["#fef0d9","#fdcc8a","#fc8d59","#e34a33","#b30000"],
6: ["#fef0d9","#fdd49e","#fdbb84","#fc8d59","#e34a33","#b30000"],
7: ["#fef0d9","#fdd49e","#fdbb84","#fc8d59","#ef6548","#d7301f","#990000"],
8: ["#fff7ec","#fee8c8","#fdd49e","#fdbb84","#fc8d59","#ef6548","#d7301f","#990000"],
9: ["#fff7ec","#fee8c8","#fdd49e","#fdbb84","#fc8d59","#ef6548","#d7301f","#b30000","#7f0000"]
};

var Greens = {
3: ["#e5f5e0","#a1d99b","#31a354"],
4: ["#edf8e9","#bae4b3","#74c476","#238b45"],
5: ["#edf8e9","#bae4b3","#74c476","#31a354","#006d2c"],
6: ["#edf8e9","#c7e9c0","#a1d99b","#74c476","#31a354","#006d2c"],
7: ["#edf8e9","#c7e9c0","#a1d99b","#74c476","#41ab5d","#238b45","#005a32"],
8: ["#f7fcf5","#e5f5e0","#c7e9c0","#a1d99b","#74c476","#41ab5d","#238b45","#005a32"],
9: ["#f7fcf5","#e5f5e0","#c7e9c0","#a1d99b","#74c476","#41ab5d","#238b45","#006d2c","#00441b"]
};

var surnameHistogramSVG = null;
var forenameHistogramSVG = null;
var histogramRangeMax = 20;
// var zillowRangeMax = 83.33;
var zillowRangeMax = 20;
var numIncomeBins = 10;

var incomeRanges = ["0", "10,000", "10,001", "14,999", "15,000", "24,999", "25,000", "34,999", "35,000", "49,999",
					"50,000", "74,999", "75,000", "99,999", "100,000", "149,999", "150,000", "199,999", "200,000+"];

var zillowRanges = ["0", "50,000", "50,0001", "100,000", "100,001", "150,000", "150,001", "200,000",
					"200,001", "250,000", "250,001", "300,000", "300,001", "350,000",
					"350,001", "400,000", "400,001", "499,999", "500,000+"];

var zillowEdges = ["$0", "$50,000", "$100,000", "$150,000", "$200,000", "$250,000", "$300,000", 
					"$350,000", "$400,000", "$500,000", "$500,000+"];

var censusEdges = ["$0", "$10,000", "$15,000", "$25,000", "$35,000", "$50,000", "$75,000", 
					"$100,000", "$150,000", "$200,000", "200,000+"];

String.prototype.toTitleCase = function(){
	"use strict";
	return this.replace(/\w\S*/g, function(txt){
		return txt.charAt(0).toUpperCase() + txt.substr(1).toLowerCase();
	});
};

function generateHistogramColor(value, maxValue, colors){
	"use strict";
	if(value <= 0)
		return colors[0];
	return colors[Math.floor((value * (colors.length-1))/maxValue)];
}

function generateHistogramColorFixed(value, maxValue, colors){
	"use strict";
	if(value <= 0)
		return colors[0];
	if(value >= maxValue){
		return colors[colors.length-1];
	}
	return colors[Math.floor(value / (maxValue / (colors.length-1)))];
}

String.prototype.width = function(font) {
	"use strict";
	var f = font || "12px arial",
	o = $("<div>" + this + "</div>")
		.css({"position": "absolute", "float": "left", "white-space": "nowrap", "visibility": "hidden", "font": f})
		.appendTo($("body")),
	w = o.width();

	o.remove();

	return w;
};

function generateIncomeDistributionBarZillow(nameType, data){
	"use strict";
	var values = data;

	var dimensions = {top: 15, right: 25, bottom: 15, left: 5},
	width = 430 - dimensions.left - dimensions.right,
	height = 65 - dimensions.top - dimensions.bottom;
	var svg;

	if(nameType == "surname"){
		$("#loadingsurnamehistogram").hide();
		d3.select("#surname_distribution_bar > svg").remove();
		svg = d3.select("#surname_distribution_bar").append("svg")
		.attr("width", width + dimensions.left + dimensions.right)
		.attr("height", height + dimensions.top + dimensions.bottom)
		.append("g")
		.attr("transform", "translate(" + dimensions.left + "," + dimensions.top + ")");
	}
	else if(nameType == "forename"){
		$("#loadingforenamehistogram").hide();
		d3.select("#forename_distribution_bar > svg").remove();
		svg = d3.select("#forename_distribution_bar").append("svg")
		.attr("width", width + dimensions.left + dimensions.right)
		.attr("height", height + dimensions.top + dimensions.bottom)
		.append("g")
		.attr("transform", "translate(" + dimensions.left + "," + dimensions.top + ")");
	}

	var bar = createHistogram(svg);
	createHistogramBar(bar, width, height, values, zillowRanges, zillowRangeMax);
	createHistogramBarAppendTitle(bar, zillowRanges, values);
	appendHistogramLabels(svg, zillowEdges, dimensions, width, height);
	
	if(nameType == "surname"){
		$("#surnamehistogram").show();
		$("#surname_histogram_options").show();
		$("#loadingsurnamehistogram").hide();
		$("#distributionToolContainer").show();
	}
	else if(nameType == "forename"){
		$("#forename_distribution_bar").show();
		$("#forename_histogram_options").show();
		$("#loadingforenamehistogram").hide();
	}
}

function generateIncomeDistributionBar(nameType, data){
	"use strict";
	var values = data;

	var dimensions = {top: 15, right: 25, bottom: 15, left: 5},
	width = 430 - dimensions.left - dimensions.right,
	height = 65 - dimensions.top - dimensions.bottom;
	var svg;

	if(nameType == "surname"){
		$("#loadingsurnamehistogram").hide();
		d3.select("#surname_distribution_bar > svg").remove();
		svg = d3.select("#surname_distribution_bar").append("svg")
		.attr("width", width + dimensions.left + dimensions.right)
		.attr("height", height + dimensions.top + dimensions.bottom)
		.append("g")
		.attr("transform", "translate(" + dimensions.left + "," + dimensions.top + ")");
	}
	else if(nameType == "forename"){
		$("#loadingforenamehistogram").hide();
		d3.select("#forename_distribution_bar > svg").remove();
		svg = d3.select("#forename_distribution_bar").append("svg")
		.attr("width", width + dimensions.left + dimensions.right)
		.attr("height", height + dimensions.top + dimensions.bottom)
		.append("g")
		.attr("transform", "translate(" + dimensions.left + "," + dimensions.top + ")");
	}

	var bar = createHistogram(svg);
	createHistogramBar(bar, width, height, values, incomeRanges, histogramRangeMax);
	createHistogramBarAppendTitle(bar, incomeRanges, values);
	appendHistogramLabels(svg, censusEdges, dimensions, width, height);
	
	if(nameType == "surname"){
		$("#surnamehistogram").show();
		$("#surname_histogram_options").show();
		$("#loadingsurnamehistogram").hide();
		$("#distributionToolContainer").show();
	}
	else if(nameType == "forename"){
		$("#forename_distribution_bar").show();
		$("#forename_histogram_options").show();
		$("#loadingforenamehistogram").hide();
	}
}

function createHistogram(svg){
	var fakeData = [1000, 12000, 20000, 30000, 40000, 60000, 80000, 125000, 175000, 250000];

	var bar = svg.selectAll(".distributionBar")
		.data(fakeData)
		.enter().append("g")
		.attr("class", "distributionBar")
		.on("click", function(d, i) {
			updateSimilarIncomeWordleForBin("surname", i+1);
		})
		.on("mouseover", function(){
			var nodeSelection = d3.select(this);
			nodeSelection.style("opacity", 0.5);
		})
		.on("mouseout", function(){
			var nodeSelection = d3.select(this);
			nodeSelection.style("opacity", 1.0);
	});
	return bar;	
}

function createHistogramBar(bar, width, height, values, ranges, rangeMax){
	var coloring = Greens[6];
	var formatPercents = d3.format(".2f");
	var barWidth = width/numIncomeBins;

	bar.append("rect")
		.attr("x", function(d, i){ return i*barWidth;})
		.attr("width", (barWidth) - 1)
		.attr("height", height)
		.style("fill", function(d,i){ return generateHistogramColorFixed(values[i], rangeMax, coloring);
		});
}

function createHistogramBarAppendTitle(bar, ranges, values){
	var formatPercents = d3.format(".2f");
	bar.append("title")
		.text(function(d, i){
			if(i < values.length-1)
				return "Range $" + ranges[i*2] +" to $" + ranges[i*2+1] + ":\n" + formatPercents(values[i]) + "%";
			else
				return "$" + ranges[ranges.length-1] + ":\n" + formatPercents(values[i]) + "%";
	});
}

function appendHistogramLabels(svg, ranges, dimensions, width, height){
	// var minLabel = ranges[0];
	// svg.append("text")
	// 	.attr("class", "axisLabel")
	// 	.attr("x", 0-dimensions.left)
	// 	.attr("y", height + dimensions.bottom)
	// 	.text(minLabel);

	var binWidth=(width-(ranges.length-1))/(ranges.length-1);

	var i;
	for(i = 0; i < ranges.length; i++){
		if(i % 2 == 0){
			svg.append("text")
				.attr("class", "axisLabel")
				.attr("x", (dimensions.left/2)+binWidth*i-(ranges[i].width("10px sans-serif")/2))
				.attr("y", height + dimensions.bottom-3)
				.text(ranges[i]);
		}
		else if(i % 2 == 1){
			svg.append("text")
				.attr("class", "axisLabel")
				.attr("x", (dimensions.left/2)+binWidth*i-(ranges[i].width("10px sans-serif")/2))
				.attr("y", -3)
				.text(ranges[i]);
		}
	}

	// var maxLabel = ranges[ranges.length-1];
	// svg.append("text")
	// 	.attr("class", "axisLabel")
	// 	.attr("x", width-maxLabel.width("10px sans-serif")+dimensions.right)
	// 	.attr("y", height + dimensions.bottom)
	// 	.text(maxLabel);
}

function incomeRangesQuery(nameType, incomeType){
	"use strict";
	var boundsOnEarth;
	var queryData;
	if(typeof (nameType) == "undefined" || nameType === null){
		return;
	}

	if(typeof(incomeType) == "undefined" || incomeType === null){
		return;
	}

	if(nameType == "surname"){
		if(typeof (state.kdeSurname) == "undefined" || state.kdeSurname === null){
			return;
		}

		if(incomeType == "census"){
			queryData = {
				surname : state.kdeSurname,
				incomeType : incomeType
			};
		}
		else if(incomeType == "zillow"){
			boundsOnEarth = kde.map.getBounds();

			queryData = {
				surname : state.kdeSurname,
				latsw : boundsOnEarth.getSouthWest().lat(),
				lngsw : boundsOnEarth.getSouthWest().lng(),
				latne : boundsOnEarth.getNorthEast().lat(),
				lngne : boundsOnEarth.getNorthEast().lng(),
				incomeType : incomeType
			};
		}
		
	}
	else if(nameType == "forename"){
		if(typeof (state.kdeForename) == "undefined" || state.kdeForename === null){
			return;
		}
		boundsOnEarth = forenamekde.map.getBounds();
		queryData = {
			forename : state.kdeForename
		};
	}
	incomeRangesQueryImp(queryData, nameType, incomeType);
}

function incomeRangesQueryImp(configuredData, nameType, incomeType){
	"use strict";
	if(typeof (nameType) == "undefined" || name === null){
		return;
	}
	if(typeof (configuredData) == "undefined" || configuredData === null){
		return;
	}
	var scriptLocation;
	if(nameType == "surname"){
		$("#loadingsurnamehistogram").show();
		if(incomeType == "census"){
			scriptLocation = state.serviceBase + "services/surname/queryIncomeRanges?callback=?";
		}
		else if (incomeType == "zillow"){
			scriptLocation = state.serviceBase + "services/surname/queryZillowIncome?callback=?";
		}
	}
	else if(nameType == "forename"){
		$("#loadingforenamehistogram").show();
		scriptLocation = state.incomeServiceBase + "services/queryIncomeRangesForename?callback=?";
	}
	else{
		return;
	}
	$.ajax(scriptLocation, {
		data : configuredData,
		dataType : "jsonp",
		global: true,

		error : function(jqXHR, textStatus, errorThrown) {
			console.log(textStatus);
			console.log(jqXHR);
			console.log(errorThrown);
			if(nameType == "surname")
				$("#loadingsurnamehistogram").hide();
			else if(nameType == "forename")
				$("#loadingforenamehistogram").hide();
			alert("Error");
		},
		success : function(data, textStatus, jqXHR) {
			state.surnameIncomeRanges = data;
			if(incomeType == "census"){
				generateIncomeDistributionBar(nameType, data);
			}
			else if(incomeType == "zillow"){
				generateIncomeDistributionBarZillow(nameType, data);
			}
		}
	});
}