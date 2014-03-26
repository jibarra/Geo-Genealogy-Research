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

var mapColors = {
	7: ["#FFFFB2","#FED976","#FEB24C","#FD8D3C","#FC4E2A","#E31A1C","#B10026"]
};

var distributionToolData = [10, 10, 10, 10, 10, 10, 10, 10, 10, 10];
var distributionChange = 4;

var toolClicked;

var incomeRangeLabels = ["0", "10,000", "10,001", "14,999", "15,000", "24,999", "25,000", "34,999", "35,000", "49,999",
			"50,000", "74,999", "75,000", "99,999", "100,000", "149,999", "150,000", "199,999", "200,000+"]

function generateToolColors(value, maxValue, colors){
	if(value <= 0)
		return colors[0];
	if(value >= maxValue){
		return colors[colors.length-1];
	}
	return colors[Math.floor(value / (maxValue / (colors.length-1)))];
}

function reviseDistributionTool(position){
	for(var i = 0; i < distributionToolData.length; i++){
		if(i != position){
			distributionToolData[i] = distributionToolData[i] -(distributionChange/(distributionToolData.length-1));
			if(distributionToolData[i] < 0){
				distributionToolData[i] = 0;
			}
		}
		else{
			distributionToolData[i] += distributionChange;
			if(distributionToolData[i] > 100){
				distributionToolData[i] = 100;
			}
		}
	}
	generateDistributionTool();
	if($('#surnameWordleType').val() == 'distributiontool'){
		window.clearInterval(toolClicked);
		toolClicked = setInterval("updateSimilarIncomeWordleForTool('surname')", 2000);
	}
}

function generateDistributionTool(){
	var numBins = 10;
	var values = distributionToolData;
	var dimensions = {top: 0, right: 0, bottom: 15, left: 0},
	width = 400 - dimensions.left - dimensions.right,
	height = 50 - dimensions.top - dimensions.bottom,
	barWidth = width/numBins;

	d3.select("#distributionTool > svg").remove();
	var svg = d3.select("#distributionTool").append("svg")
	    .attr("width", width + dimensions.left + dimensions.right)
	    .attr("height", height + dimensions.top + dimensions.bottom)
	  	.append("g")
	    .attr("transform", "translate(" + dimensions.left + "," + dimensions.top + ")");

	var max = d3.max(values);
	var min = 0;
	var fakeData = distributionToolData;

   	var coloring = Greens[6];
   	var formatPercents = d3.format(".2f");

	var bar = svg.selectAll(".distributionBar")
	    .data(fakeData)
	  	.enter().append("g")
	    .attr("class", "distributionBar")
	    .on("click", function(d, i) {
	    	reviseDistributionTool(i);
	    })
	    .on("mouseover", function(){
			var nodeSelection = d3.select(this);
			nodeSelection.style("opacity", 0.5);
		})
		.on("mouseout", function(){
			var nodeSelection = d3.select(this);
			nodeSelection.style("opacity", 1.0);
		})
	bar.append("rect")
	    .attr("x", function(d, i){ return i*barWidth;})
	    .attr("width", (barWidth) - 1)
	    .attr("height", height)
	    .style("fill", function(d,i){ 
	    	return generateToolColors(values[i], histogramRangeMax, coloring);
	    })
	    .append("title")
	    .text(function(d, i){ 
	    	if(i < 9)
	    		return "Range $" + incomeRangeLabels[i*2] +" to $" + incomeRangeLabels[i*2+1] + ":\n" + formatPercents(values[i]) + "%" ;
	    	else
	    		return "$" + incomeRangeLabels[18] + ":\n" + formatPercents(values[i]) + "%" ;
	    });

    var minLabel = "$" + incomeRangeLabels[0];
	svg.append("text")
    	.attr("class", "axisLabel")
    	.attr("x", 0)
	    .attr("y", height + dimensions.top + 10)
		.text(minLabel);

	var maxLabel = "$" + incomeRangeLabels[18];
    svg.append("text")
    	.attr("class", "axisLabel")
    	.attr("x", width-maxLabel.width('10px sans-serif'))
	    .attr("y", height + dimensions.top + 10)
		.text(maxLabel);
}


function generateIncomeLegend(){
	var numBins = 6;
	var incomeEdges = ["0%", "4%", "8%", "12%", "16%", "20%", "20+%"];
	var dimensions = {top: 18, right: 30, bottom: 10, left: 10},
	width = 75 - dimensions.left - dimensions.right,
	height = 125 - dimensions.top - dimensions.bottom,
	barWidth = width/numBins;
	barHeight = height/numBins;

	var coloring = Greens[numBins];
	d3.select("#incomeLegend > svg").remove();
	var svg = d3.select("#incomeLegend").append("svg")
	    .attr("width", width + dimensions.left + dimensions.right)
	    .attr("height", height + dimensions.top + dimensions.bottom)
	  	.append("g")
	    .attr("transform", "translate(" + dimensions.left + "," + dimensions.top + ")");

	var fakeData = new Array();
	for(var i = 0; i < numBins; i++){
		fakeData[i] = i;
	}
	var bar = svg.selectAll(".distributionBar")
	    .data(fakeData)
	  	.enter().append("g")
	    .attr("class", "distributionBar")
	bar.append("rect")
	    .attr("y", function(d, i){ return i*barHeight; })
	   	.attr("width", width)
	    .attr("height", barHeight)
	    .style("fill", function(d,i){ return coloring[i]; })
	    .append("title");
	for(var i = 0; i <= numBins; i++){
		svg.append("text")
	    	.attr("class", "axisLabel")
	    	.attr("x", width)
		    .attr("y", 5 + i*barHeight)
			.text(incomeEdges[i]);
	}

	var typeLabel = "% Population";
	svg.append("text")
    	.attr("class", "axisLabel")
    	.attr("x", (width/2)-(typeLabel.width('10px sans-serif')/2)+10)
	    .attr("y", -8)
	    .style("font-weight", "bold")
	    .style("font-style", "italic")
		.text(typeLabel);
}

function generateMapLegend(){
	var numBins = 7;
	var dimensions = {top: 0, right: 0, bottom: 15, left: 0},
	width = 250 - dimensions.left - dimensions.right,
	height = 40 - dimensions.top - dimensions.bottom,
	barWidth = width/numBins;

	var coloring = mapColors[numBins];
	d3.select("#mapLegend > svg").remove();
	var svg = d3.select("#mapLegend").append("svg")
	    .attr("width", width + dimensions.left + dimensions.right)
	    .attr("height", height + dimensions.top + dimensions.bottom)
	  	.append("g")
	    .attr("transform", "translate(" + dimensions.left + "," + dimensions.top + ")");

	var fakeData = new Array();
	for(var i = 0; i < numBins; i++){
		fakeData[i] = i;
	}
	var bar = svg.selectAll(".distributionBar")
	    .data(fakeData)
	  	.enter().append("g")
	    .attr("class", "distributionBar")
	bar.append("rect")
	    .attr("x", function(d, i){ return i*barWidth;})
	    .attr("width", (barWidth) - 1)
	    .attr("height", height)
	    .style("fill", function(d,i){ return coloring[i]; })
	    .append("title");
	
    var minLabel = "Low";
	svg.append("text")
    	.attr("class", "axisLabel")
    	.attr("x", 0)
	    .attr("y", height + dimensions.top + 10)
		.text(minLabel);

	var maxLabel = "High";
    svg.append("text")
    	.attr("class", "axisLabel")
    	.attr("x", width-maxLabel.width('10px sans-serif'))
	    .attr("y", height + dimensions.top + 10)
		.text(maxLabel);

	var typeLabel = "Probability";
	svg.append("text")
    	.attr("class", "axisLabel")
    	.attr("x", (width/2)-(typeLabel.width('10px sans-serif')/2))
	    .attr("y", height + dimensions.top + 10)
	    .style("font-weight", "bold")
	    .style("font-style", "italic")
		.text(typeLabel);
}

function generateWordleLegend(){
	var numBins = 6;
	var dimensions = {top: 0, right: 0, bottom: 15, left: 0},
	width = 250 - dimensions.left - dimensions.right,
	height = 40 - dimensions.top - dimensions.bottom,
	barWidth = width/numBins;

	var coloring = OrRd[numBins];
	d3.select("#wordleLegend > svg").remove();
	var svg = d3.select("#wordleLegend").append("svg")
	    .attr("width", width + dimensions.left + dimensions.right)
	    .attr("height", height + dimensions.top + dimensions.bottom)
	  	.append("g")
	    .attr("transform", "translate(" + dimensions.left + "," + dimensions.top + ")");

	var fakeData = new Array();
	for(var i = 0; i < numBins; i++){
		fakeData[i] = i;
	}
	var bar = svg.selectAll(".distributionBar")
	    .data(fakeData)
	  	.enter().append("g")
	    .attr("class", "distributionBar")
	bar.append("rect")
	    .attr("x", function(d, i){ return i*barWidth;})
	    .attr("width", (barWidth) - 1)
	    .attr("height", height)
	    .style("fill", function(d,i){ return coloring[i]; })
	    .append("title");
	
    var minLabel = "Low";
	svg.append("text")
    	.attr("class", "axisLabel")
    	.attr("x", 0)
	    .attr("y", height + dimensions.top + 10)
		.text(minLabel);

	var maxLabel = "High";
    svg.append("text")
    	.attr("class", "axisLabel")
    	.attr("x", width-maxLabel.width('10px sans-serif'))
	    .attr("y", height + dimensions.top + 10)
		.text(maxLabel);

	var typeLabel = "Similarity";
	svg.append("text")
    	.attr("class", "axisLabel")
    	.attr("x", (width/2)-(typeLabel.width('10px sans-serif')/2))
	    .attr("y", height + dimensions.top + 10)
	    .style("font-weight", "bold")
	    .style("font-style", "italic")
		.text(typeLabel);
}