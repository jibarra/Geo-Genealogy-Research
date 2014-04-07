/**
 *  @author Jose Ibarra, Arizona State University, Jose.Ibarra@asu.edu
 created on November 25, 2013 ane edited into 2014

 Linted on April 7, 2014 using JSHint (https://github.com/jshint/jshint/blob/master/examples/.jshintrc)
 */
var wordleMax;
var wordleMin;

var OrRd = {
	3: ["#fee8c8", "#fdbb84", "#e34a33"],
	4: ["#fef0d9", "#fdcc8a", "#fc8d59", "#d7301f"],
	5: ["#fef0d9", "#fdcc8a", "#fc8d59", "#e34a33", "#b30000"],
	6: ["#fef0d9", "#fdd49e", "#fdbb84", "#fc8d59", "#e34a33", "#b30000"],
	7: ["#fef0d9", "#fdd49e", "#fdbb84", "#fc8d59", "#ef6548", "#d7301f", "#990000"],
	8: ["#fff7ec", "#fee8c8", "#fdd49e", "#fdbb84", "#fc8d59", "#ef6548", "#d7301f", "#990000"],
	9: ["#fff7ec", "#fee8c8", "#fdd49e", "#fdbb84", "#fc8d59", "#ef6548", "#d7301f", "#b30000", "#7f0000"]
};

var maxFontSize = 50;
var minFontSize = 8;
var wordPadding = 1;
var numBins = 6;

function generateWordleColor(data, maxValue, colors){
	"use strict";
	if(data.fontSize <= 0)
		return colors[0];
	return colors[Math.floor((data.fontSize * (colors.length-1))/maxValue)];
}

function generateWordSize(data, maxValue, minValue, fontSizes){
	"use strict";
	if(data.closeness < 0){
		return minFontSize;
	}
	return Math.floor(fontSizes[Math.floor(((data.closeness-minValue) * (fontSizes.length-1))/(maxValue-minValue))]);
}

function positionWordleLoading(){
	"use strict";
	var leftpos = $("#surnameWordleChart").position().left;
	var toppos = $("#surnameWordleChart").position().top;
	leftpos = leftpos + 200;
	toppos = toppos + 150;
	$("#loadingsurnamewordle").css("left", "" + leftpos + "px");
	$("#loadingsurnamewordle").css("top", "" + toppos + "px");
}

function updateSimilarIncomeWordleForBin(nametype, binNumber) {
	"use strict";
	var scriptLocation;
	var configuredData;
	if(nametype == "surname"){
		positionWordleLoading();
		$("#loadingsurnamewordle").show();
		scriptLocation = state.serviceBase + "services/SurnameIncomeBinWordle?callback=?";
		configuredData = {
			surname : state.kdeSurname,
			limit : 100,
			bin : binNumber
		};
		$("option[value='specificIncomeRange']").remove();
		$("#surnameWordleType").append($("<option value='specificIncomeRange'>Income Range "+binNumber+"</option>"));
		$("#surnameWordleType").val("specificIncomeRange");
	}
	else if (nametype == "forename"){
		scriptLocation = state.serviceBase + "services/ForenameIncomeWordle?callback=?";
		configuredData = {
			forename : state.kdeForename,
			limit : 100,
			bin : binNumber
		};
	}
		
	$.ajax(scriptLocation, {
		data: configuredData,
		dataType: "jsonp",

		error: function (jqXHR, textStatus, errorThrown) {
			//do something when error...
			(errorThrown);
		},
		success: function (data, textStatus, jqXHR) {
			$("#wordleLegend").show();
			if(data === null || typeof(data) == "undefined" || data.length < 1){
				if (document.getElementById("wordleSVG")) {
				 var el = document.getElementById("wordleSVG");
				 el.parentNode.removeChild(el);
				}
				if(nametype == "surname"){
					$("#surnameWordleChart").width(0);
					$("#surnameWordleChart").height(0);
				}
				else if (nametype == "forename"){
					$("#forenameWordleChart").width(0);
					$("#forenameWordleChart").height(0);
				}
				return;
			}
			var width;
			var height;
			if(nametype == "surname"){
				$("#surnameWordleChart").width(500);
				$("#surnameWordleChart").height(393);
				width = $("#surnameWordleChart").width();
				height = $("#surnameWordleChart").height();
			}
			else if (nametype == "forename"){
				$("#forenameWordleChart").width(500);
				$("#forenameWordleChart").height(393);
				width = $("#forenameWordleChart").width();
				height = $("#forenameWordleChart").height();
			}
			
			if (document.getElementById("wordleSVG")) {
				 var ele = document.getElementById("wordleSVG");
				 ele.parentNode.removeChild(ele);
			}
			

			wordleMin = data[0].closeness;
			wordleMax = data[0].closeness;

			data.forEach(function(d) {
				if(d.closeness < wordleMin){
					wordleMin = d.closeness;
				}
				else if(d.closeness > wordleMax){
					wordleMax = d.closeness;
				}
			});

			//range from min closeness to max closeness
			//this is an inverse scale so smaller closeness means they are more similar
			//thus the text size will be bigger
			var fontSizes = [];
			var fontDifference = (maxFontSize-minFontSize) / (numBins-1);
			for(var i =0; i < numBins; i++){
				fontSizes[i] = i*fontDifference+minFontSize;
			}
			
			var x = d3.scale.linear()
				.domain([wordleMax, wordleMin])
				.range([wordleMin, wordleMax]);
			data.forEach(function(d) {
				d.closeness = x(d.closeness);
				d.fontSize = generateWordSize(d, wordleMax, wordleMin, fontSizes);
			});

			if(nametype == "surname"){
				d3.layout.cloud().size([width, height]).timeInterval(10).padding(wordPadding).words(data)
						.text(function(d){
					return d.name;
				})
				.rotate(function() {
					return 0;
				}).font("Impact")
					.fontSize(function(d){
						return d.fontSize;
				}).on("end", drawSurnameWordle).start();
			}
			else if (nametype == "forename"){
				d3.layout.cloud().size([width, height]).timeInterval(10).padding(wordPadding).words(data)
						.text(function(d){
					return d.name;
				})
				.rotate(function() {
					return 0;
				}).font("Impact")
					.fontSize(function(d){
						return d.fontSize;
				}).on("end", drawForenameWordle).start();
			}
		}
	});
}

function updateSimilarIncomeWordleForTool(nametype) {
	"use strict";
	window.clearInterval(toolClicked);
	var scriptLocation;
	var configuredData;
	if(nametype == "surname"){
		positionWordleLoading();
		$("#loadingsurnamewordle").show();
		scriptLocation = state.serviceBase + "services/IncomeSimilarityWordle?callback=?";
		configuredData = {
			limit : 100,
			bin1 : distributionToolData[0],
			bin2 : distributionToolData[1],
			bin3 : distributionToolData[2],
			bin4 : distributionToolData[3],
			bin5 : distributionToolData[4],
			bin6 : distributionToolData[5],
			bin7 : distributionToolData[6],
			bin8 : distributionToolData[7],
			bin9 : distributionToolData[8],
			bin10 : distributionToolData[9]
		};
	}
	else if (nametype == "forename"){
		scriptLocation = state.serviceBase + "services/ForenameIncomeWordle?callback=?";
		configuredData = {
			forename : state.kdeForename,
			limit : 100
		};
	}
	$.ajax(scriptLocation, {
		data: configuredData,
		dataType: "jsonp",

		error: function (jqXHR, textStatus, errorThrown) {
			console.log(errorThrown);
		},
		success: function (data, textStatus, jqXHR) {
			$("#wordleLegend").show();
			if(data === null || typeof(data) == "undefined" || data.length < 1){
				if (document.getElementById("wordleSVG")) {
					var el = document.getElementById("wordleSVG");
					el.parentNode.removeChild(el);
				}
				if(nametype == "surname"){
					$("#surnameWordleChart").width(0);
					$("#surnameWordleChart").height(0);
				}
				else if (nametype == "forename"){
					$("#forenameWordleChart").width(0);
					$("#forenameWordleChart").height(0);
				}
				return;
			}
			var width;
			var height;
			if(nametype == "surname"){
				$("#surnameWordleChart").width(500);
				$("#surnameWordleChart").height(393);
				width = $("#surnameWordleChart").width();
				height = $("#surnameWordleChart").height();
			}
			else if (nametype == "forename"){
				$("#forenameWordleChart").width(500);
				$("#forenameWordleChart").height(393);
				width = $("#forenameWordleChart").width();
				height = $("#forenameWordleChart").height();
			}
			
			if (document.getElementById("wordleSVG")) {
				 var ele = document.getElementById("wordleSVG");
				 ele.parentNode.removeChild(ele);
			}
			

			wordleMin = data[0].closeness;
			wordleMax = data[0].closeness;

			data.forEach(function(d) {
				if(d.closeness < wordleMin){
					wordleMin = d.closeness;
				}
				else if(d.closeness > wordleMax){
					wordleMax = d.closeness;
				}
			});

			//range from min closeness to max closeness
			//this is an inverse scale so smaller closeness means they are more similar
			//thus the text size will be bigger
			var fontSizes = [];
			var fontDifference = (maxFontSize-minFontSize) / (numBins-1);
			for(var i =0; i < numBins; i++){
				fontSizes[i] = i*fontDifference+minFontSize;
			}
		
			var x = d3.scale.linear()
				.domain([wordleMax, wordleMin])
				.range([wordleMin, wordleMax]);
			data.forEach(function(d) {
				d.closeness = x(d.closeness);
				d.fontSize = generateWordSize(d, wordleMax, wordleMin, fontSizes);
			});
			

			if(nametype == "surname"){
				d3.layout.cloud().size([width, height]).timeInterval(10).padding(wordPadding).words(data)
						.text(function(d){
					return d.name;
				})
				.rotate(function() {
					return 0;
				}).font("Impact")
					.fontSize(function(d){
						return d.fontSize;
				}).on("end", drawSurnameWordle).start();
			}
			else if (nametype == "forename"){
				d3.layout.cloud().size([width, height]).timeInterval(10).padding(wordPadding).words(data)
				.text(function(d){
					return d.name;
				})
				.rotate(function() {
					return 0;
				}).font("Impact")
					.fontSize(function(d){
						return d.fontSize;
				}).on("end", drawForenameWordle).start();
			}
		}
	});
}

function updateSimilarIncomeWordle(nametype) {
	"use strict";
	var scriptLocation;
	var configuredData;
	if(nametype == "surname"){
		positionWordleLoading();
		$("#loadingsurnamewordle").show();
		scriptLocation = state.serviceBase + "services/SurnameIncomeWordle?callback=?";
		configuredData = {
			surname : state.kdeSurname,
			limit : 100
		};
	}
	else if (nametype == "forename"){
		scriptLocation = state.serviceBase + "services/ForenameIncomeWordle?callback=?";
		configuredData = {
			forename : state.kdeForename,
			limit : 100
		};
	}
		
	$.ajax(scriptLocation, {
		data: configuredData,
		dataType: "jsonp",

		error: function (jqXHR, textStatus, errorThrown) {
			 //do something when error...
			 console.log(errorThrown);
		},
		success: function (data, textStatus, jqXHR) {
			$("#wordleLegend").show();
			if(data === null || typeof(data) == "undefined" || data.length < 1){
				if (document.getElementById("wordleSVG")) {
				 var el = document.getElementById("wordleSVG");
				 el.parentNode.removeChild(el);
				}
				if(nametype == "surname"){
					$("#surnameWordleChart").width(0);
					$("#surnameWordleChart").height(0);
				}
				else if (nametype == "forename"){
					$("#forenameWordleChart").width(0);
					$("#forenameWordleChart").height(0);
				}
				return;
			}
			var width;
			var height;
			if(nametype == "surname"){
				$("#surnameWordleChart").width(500);
				$("#surnameWordleChart").height(393);
				width = $("#surnameWordleChart").width();
				height = $("#surnameWordleChart").height();
			}
			else if (nametype == "forename"){
				$("#forenameWordleChart").width(500);
				$("#forenameWordleChart").height(393);
				width = $("#forenameWordleChart").width();
				height = $("#forenameWordleChart").height();
			}
			
			if (document.getElementById("wordleSVG")) {
				 var ele = document.getElementById("wordleSVG");
				 ele.parentNode.removeChild(ele);
			}
			

			wordleMin = data[0].closeness;
			wordleMax = data[0].closeness;

			data.forEach(function(d) {
				if(d.closeness < wordleMin){
					wordleMin = d.closeness;
				}
				else if(d.closeness > wordleMax){
					wordleMax = d.closeness;
				}
			});

			//range from min closeness to max closeness
			//this is an inverse scale so smaller closeness means they are more similar
			//thus the text size will be bigger
			var fontSizes = [];
			var fontDifference = (maxFontSize-minFontSize) / (numBins-1);
			for(var i =0; i < numBins; i++){
				fontSizes[i] = i*fontDifference+minFontSize;
			}
		
			var x = d3.scale.linear()
				.domain([wordleMax, wordleMin])
				.range([wordleMin, wordleMax]);
			data.forEach(function(d) {
				d.closeness = x(d.closeness);
				d.fontSize = generateWordSize(d, wordleMax, wordleMin, fontSizes);
			});
			

			if(nametype == "surname"){
				d3.layout.cloud().size([width, height]).timeInterval(10).padding(wordPadding).words(data)
				.text(function(d){
					return d.name;
				})
				.rotate(function() {
					return 0;
				}).font("Impact")
					.fontSize(function(d){
						return d.fontSize;
				}).on("end", drawSurnameWordle).start();
			}
			else if (nametype == "forename"){
				d3.layout.cloud().size([width, height]).timeInterval(10).padding(wordPadding).words(data)
				.text(function(d){
					return d.name;
				})
				.rotate(function() {
					return 0;
				}).font("Impact")
					.fontSize(function(d){
						return d.fontSize;
				}).on("end", drawForenameWordle).start();
			}
		}
	});
}

function updateSimilarMapWordle(nametype, wordletype) {
	"use strict";
	var scriptLocation;
	var configuredData;
	if(nametype == "surname"){
		scriptLocation = state.serviceBase + "services/SurnameMapWordle?callback=?";
		configuredData = {
			surname : state.kdeSurname,
			type : wordletype,
			limit : 100
		};
	}
	else if (nametype == "forename"){
		scriptLocation = state.serviceBase + "services/ForenameMapWordle?callback=?";
		configuredData = {
			forename : state.kdeForename,
			limit : 100
		};
	}

	$.ajax(scriptLocation, {
		data: configuredData,
		dataType: "jsonp",

		error: function (jqXHR, textStatus, errorThrown) {
			 //do something when error...
			 console.log(errorThrown);
		},
		success: function (data, textStatus, jqXHR) {
			$("#wordleLegend").show();
			if(data === null || typeof(data) == "undefined" || data.length < 1){
				if (document.getElementById("wordleSVG")) {
					var el = document.getElementById("wordleSVG");
					el.parentNode.removeChild(el);
				}
				if(nametype == "surname"){
					$("#surnameWordleChart").width(0);
					$("#surnameWordleChart").height(0);
				}
				else if (nametype == "forename"){
					$("#forenameWordleChart").width(0);
					$("#forenameWordleChart").height(0);
				}
				return;
			}
			var width;
			var height;
			if(nametype == "surname"){
				$("#surnameWordleChart").width(500);
				$("#surnameWordleChart").height(393);
				width = $("#surnameWordleChart").width();
				height = $("#surnameWordleChart").height();
			}
			else if (nametype == "forename"){
				$("#forenameWordleChart").width(500);
				$("#forenameWordleChart").height(393);
				width = $("#forenameWordleChart").width();
				height = $("#forenameWordleChart").height();
			}
			
			if (document.getElementById("wordleSVG")) {
				 var ele = document.getElementById("wordleSVG");
				 ele.parentNode.removeChild(ele);
			}
			

			wordleMin = data[0].closeness;
			wordleMax = data[0].closeness;

			data.forEach(function(d) {
				if(d.closeness < wordleMin){
					wordleMin = d.closeness;
				}
				else if(d.closeness > wordleMax){
					wordleMax = d.closeness;
				}
			});

			//range from min closeness to max closeness
			//this is an inverse scale so smaller closeness means they are more similar
			//thus the text size will be bigger
			var fontSizes = [];
				var fontDifference = (maxFontSize-minFontSize) / (numBins-1);
				for(var i =0; i < numBins; i++){
					fontSizes[i] = i*fontDifference+minFontSize;
				}
			if (wordletype == "l2"){
				var x = d3.scale.linear()
					.domain([wordleMax, wordleMin])
					.range([wordleMin, wordleMax]);
				data.forEach(function(d) {
					d.closeness = x(d.closeness);
					d.fontSize = generateWordSize(d, wordleMax, wordleMin, fontSizes);
				});
			}
			else if(wordletype == "core"){
				var x = d3.scale.linear()
					.domain([wordleMax, wordleMin])
					.range([wordleMin, wordleMax]);
				data.forEach(function(d) {
					d.closeness = x(d.closeness);
					d.fontSize = generateWordSize(d, wordleMax, wordleMin, fontSizes);
				});
			}
			

			if(nametype == "surname"){
				d3.layout.cloud().size([width, height]).timeInterval(10).padding(wordPadding).words(data)
				.text(function(d){
					return d.name;
				})
				.rotate(function() {
					return 0;
				}).font("Impact")
					.fontSize(function(d){
						return d.fontSize;
				}).on("end", drawSurnameWordle).start();
			}
			else if (nametype == "forename"){
				d3.layout.cloud().size([width, height]).timeInterval(10).padding(wordPadding).words(data)
				.text(function(d){
					return d.name;
				})
				.rotate(function() {
					return 0;
				}).font("Impact")
					.fontSize(function(d){
						return d.fontSize;
				}).on("end", drawForenameWordle).start();
			}
		}
	});
}

function drawSurnameWordle(words) {
	"use strict";
	var coloring = OrRd[numBins];
	var width = $("#surnameWordleChart").width();
	var height = $("#surnameWordleChart").height();
	var svg = d3.select("#surnameWordleChart").append("svg")
		.attr("id", "wordleSVG")
		.attr("width", width)
		.attr("height",	height);
	svg.append("rect")
		.attr("width", "100%")
		.attr("height", "100%")
		.attr("fill", "black");
	svg.append("g")
		.attr("transform", "translate(" + (width / 2) + "," + (height / 2) + ")")
		.attr("width", width)
		.attr("height", height)
		.selectAll("text")
		.data(words)
		.enter()
		.append("text")
		.style("font-size", function(d) {
			return d.fontSize + "px";
		})
		.style("font-family", "Impact")
		.style("fill", function(d, i) {
				return generateWordleColor(d, maxFontSize, coloring);
		})
		.attr("text-anchor", "middle")
		.attr("transform", function(d) {
				return "translate(" + [ d.x, d.y ] + ")rotate(" + d.rotate + ")";})
		.text(function(d) {
				return d.name;
		})
		.on("click", function(d) {
			$("#tabSurname").animate({ scrollTop: 0 }, "slow");
			$("#surname_input").val(d.name);
			onClickShowKDE("surname");
		})
		.on("mouseover", function(){
			var nodeSelection = d3.select(this);
			nodeSelection.style("opacity", 0.5);
		})
		.on("mouseout", function(){
			var nodeSelection = d3.select(this);
			nodeSelection.style("opacity", 1.0);
		});
	$("#loadingsurnamewordle").hide();
}