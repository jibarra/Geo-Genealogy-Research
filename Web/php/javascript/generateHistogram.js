String.prototype.width = function(font) {
  var f = font || '12px arial',
      o = $('<div>' + this + '</div>')
            .css({'position': 'absolute', 'float': 'left', 'white-space': 'nowrap', 'visibility': 'hidden', 'font': f})
            .appendTo($('body')),
      w = o.width();

  o.remove();

  return w;
}

String.prototype.capitalize = function() {
    return this.charAt(0).toUpperCase() + this.slice(1);
}

String.prototype.toTitleCase = function(){
	return this.replace(/\w\S*/g, function(txt){return txt.charAt(0).toUpperCase() + txt.substr(1).toLowerCase();});
}

var YlOrRd =  {
3: ["#ffeda0","#feb24c","#f03b20"],
4: ["#ffffb2","#fecc5c","#fd8d3c","#e31a1c"],
5: ["#ffffb2","#fecc5c","#fd8d3c","#f03b20","#bd0026"],
6: ["#ffffb2","#fed976","#feb24c","#fd8d3c","#f03b20","#bd0026"],
7: ["#ffffb2","#fed976","#feb24c","#fd8d3c","#fc4e2a","#e31a1c","#b10026"],
8: ["#ffffcc","#ffeda0","#fed976","#feb24c","#fd8d3c","#fc4e2a","#e31a1c","#b10026"],
9: ["#ffffcc","#ffeda0","#fed976","#feb24c","#fd8d3c","#fc4e2a","#e31a1c","#bd0026","#800026"]
};

var OrRd = {
3: ["#fee8c8","#fdbb84","#e34a33"],
4: ["#fef0d9","#fdcc8a","#fc8d59","#d7301f"],
5: ["#fef0d9","#fdcc8a","#fc8d59","#e34a33","#b30000"],
6: ["#fef0d9","#fdd49e","#fdbb84","#fc8d59","#e34a33","#b30000"],
7: ["#fef0d9","#fdd49e","#fdbb84","#fc8d59","#ef6548","#d7301f","#990000"],
8: ["#fff7ec","#fee8c8","#fdd49e","#fdbb84","#fc8d59","#ef6548","#d7301f","#990000"],
9: ["#fff7ec","#fee8c8","#fdd49e","#fdbb84","#fc8d59","#ef6548","#d7301f","#b30000","#7f0000"]
};

var set1 = {
3: ["#e41a1c","#377eb8","#4daf4a"],
4: ["#e41a1c","#377eb8","#4daf4a","#984ea3"],
5: ["#e41a1c","#377eb8","#4daf4a","#984ea3","#ff7f00"],
6: ["#e41a1c","#377eb8","#4daf4a","#984ea3","#ff7f00","#ffff33"],
7: ["#e41a1c","#377eb8","#4daf4a","#984ea3","#ff7f00","#ffff33","#a65628"],
8: ["#e41a1c","#377eb8","#4daf4a","#984ea3","#ff7f00","#ffff33","#a65628","#f781bf"],
9: ["#e41a1c","#377eb8","#4daf4a","#984ea3","#ff7f00","#ffff33","#a65628","#f781bf","#999999"]
};

function generateSurnameColor(value, maxValue, colors){
	if(value <= 0)
		return colors[0];
	return colors[Math.floor((value * colors.length-1)/maxValue)];
}

var surnameHistogramSVG = null;
var forenameHistogramSVG = null;


function updateHistogramIncome(nameType){
	var formatCount = d3.format("$,.2f");
	var meanValues;
	var medianValues;
	if(nameType=="surname"){
		var meanValues = state.surnameIncomeData.meanIncome;
		var medianValues = state.surnameIncomeData.medianIncome;
	}
	else if(nameType == "forename"){
		var meanValues = state.forenameIncomeData.meanIncome;
		var medianValues = state.forenameIncomeData.medianIncome;
	}
	var averageMean = d3.sum(meanValues) / meanValues.length;
	var averageMedian = d3.sum(medianValues) / medianValues.length;
	if(averageMean == null || typeof(averageMean) == 'undefined'){
		averageMean = 0;
	}
	if(averageMedian == null || typeof(averageMedian) == 'undefined'){
		averageMedian = 0;
	}
	
	if(nameType=="surname"){
		$('#surnameMeanIncomeText').text("Average Mean Income: " + formatCount(averageMean));
		$('#surnameMedianIncomeText').text("Average Median Income: " + formatCount(averageMedian));
		$('#surnameMeanIncomeText').show();
		$('#surnameMedianIncomeText').show();
	}
	else if (nameType=="forename"){
		$('#forenameMeanIncomeText').text("Average Mean Income: " + formatCount(averageMean));
		$('#forenameMedianIncomeText').text("Average Median Income: " + formatCount(averageMedian));
		$('#forenameMeanIncomeText').show();
		$('#forenameMedianIncomeText').show();
	}

}

function addHistogram(nameData, nameType){
	if(nameData == null){
		nameData.meanIncome = [0];
		nameData.medianIncome = [0];
	}
	if(nameType == "surname"){
		state.surnameIncomeData = nameData;
		updateHistogramIncome(nameType);
		// generateHistogram($('#surname_bins').val(), "surname", $('#surnameIncomeType').val());
		// generatePieChart($('#surname_bins').val(), "surname", $('#surnameIncomeType').val());
		generateIncomeDistributionBar($('#surname_bins').val(), "surname", $('#surnameIncomeType').val());
		
	}
	else if(nameType == "forename"){
		state.forenameIncomeData = nameData;
		updateHistogramIncome(nameType);
		// generateHistogram($('#forename_bins').val(), "forename", $('#forenameIncomeType').val());
		// generatePieChart($('#forename_bins').val(), "forename", $('#forenameIncomeType').val());\
		generateIncomeDistributionBar($('#forename_bins').val(), "forename", $('#forenameIncomeType').val());
	}
}

function generateIncomeDistributionBar(numBins, nameType, incomeType){
	var setup = setupHistogramData(nameType, incomeType);
	var values = setup.values;
	var dimensions = {top: 0, right: 0, bottom: 15, left: 0},
	width = 500 - dimensions.left - dimensions.right,
	height = 65 - dimensions.top - dimensions.bottom;
	var svg;
	if(nameType == "surname"){
		$('#loadingsurnamehistogram').hide();
		d3.select("#surname_distribution_bar > svg").remove();
		svg = d3.select("#surname_distribution_bar").append("svg")
	    .attr("width", width + dimensions.left + dimensions.right)
	    .attr("height", height + dimensions.top + dimensions.bottom)
	  	.append("g")
	    .attr("transform", "translate(" + dimensions.left + "," + dimensions.top + ")");
	}
	else if(nameType == "forename"){
		$('#loadingforenamehistogram').hide();
		d3.select("#forename_distribution_bar > svg").remove();
		svg = d3.select("#forename_distribution_bar").append("svg")
	    .attr("width", width + dimensions.left + dimensions.right)
	    .attr("height", height + dimensions.top + dimensions.bottom)
	  	.append("g")
	    .attr("transform", "translate(" + dimensions.left + "," + dimensions.top + ")");
	}

	var max = d3.max(values);
	var min = d3.min(values);
	//Create the bins for the histogram
	var bins = new Array();
	var i = 0;
	var binDif = (max-min)/numBins;
	//make sure you get bins up to and including the max amount because
	//of the construction of histograms (splits = numBins+1)
	while(i <= numBins){
		bins[i] = i*binDif;
		i++;
	}

	//Setup the x value data. Make it a linear
	//function from the min value to the max value mapped to
	//the pixels of the graph. Make the min and max values
	//rounded, for nice numbers.
	//https://github.com/mbostock/d3/wiki/Quantitative-Scales
	var x = d3.scale.linear()
	    .domain([min, max])
	    .range([0, width]);

    //Generate a histogram of the values and bins created above
	var data = d3.layout.histogram()
	    .bins(bins)
	    (values);
    // var peopleMin = 999999999;
    var peopleMax = -1;
    i=0;
    while(i < data.length){
    	// if(peopleMin > data[i].y)
    	// 	peopleMin = data[i].y;
    	if(peopleMax < data[i].y)
    		peopleMax = data[i].y;
    	i++;
    }

   	var coloring = OrRd[9];
   	var formatCount = d3.format(",.0f");

	var bar = svg.selectAll(".distributionBar")
	    .data(data)
	  .enter().append("g")
	    .attr("class", "distributionBar")
	    .attr("transform", function(d) { return "translate(" + x(d.x) + "," + 0 + ")"; });
	bar.append("rect")
	    .attr("x", 1)
	    .attr("width", x(data[0].x  + data[0].dx) - 1)
	    .attr("height", height)
	    .style("fill", function(d){ return generateSurnameColor(d.y, peopleMax, coloring);})
	    .append("title")
	    .text(function(d){ return "Range $" + formatCount(d.x) + " to $" + formatCount(d.x+binDif) + ":\n" 
	    	+ formatCount(d.y) + " people"; });
	
    var minLabel = "$" + formatCount(min);
	svg.append("text")
    	.attr("class", "axisLabel")
    	.attr("x", 0)
	    .attr("y", height + dimensions.top + 10)
		.text(minLabel);

	var maxLabel = "$" + formatCount(max);
    svg.append("text")
    	.attr("class", "axisLabel")
    	.attr("x", width-maxLabel.width('10px sans-serif'))
	    .attr("y", height + dimensions.top + 10)
		.text(maxLabel);

	if(nameType == "surname"){
		$('#surname_distribution_bar').show();
		$('#surname_histogram_options').show();
		$('#loadingsurnamehistogram').hide();
	}
	else if(nameType == "forename"){
		$('#forename_distribution_bar').show();
		$('#forename_histogram_options').show();
		$('#loadingforenamehistogram').hide();
	}
}

//http://schoolofdata.org/2013/10/01/pie-and-donut-charts-in-d3-js/
function generatePieChart(numBins, nameType, incomeType){
	var cScale = d3.scale.linear().domain([0, 100]).range([0, 2 * Math.PI]);

	var setup = setupHistogramData(nameType, incomeType);
	var values = setup.values;


	var svg;

	if(nameType == "surname"){
		d3.select("#surname_donut_chart > svg").remove();
		svg = d3.select("#surname_donut_chart").append("svg")
	    .attr("width", 200)
	    .attr("height", 200);
	}
	else if(nameType == "forename"){
		d3.select("#forename_donut_chart > svg").remove();
		svg = d3.select("#forename_donut_chart").append("svg")
	    .attr("width", 200)
	    .attr("height", 200);
	}

	var max = d3.max(values);
	var min = d3.min(values);
	//Create the bins for the histogram
	var bins = new Array();
	var i = 0;
	var binDif = (max-min)/numBins;
	//make sure you get bins up to and including the max amount because
	//of the construction of histograms (splits = numBins+1)
	while(i <= numBins){
		bins[i] = i*binDif;
		i++;
	}

	//Generate a histogram of the values and bins created above
	var data = d3.layout.histogram()
	    .bins(bins)
	    (values);

    //Get the total count of data in the histogram
	var i = 0;
	var totalCount = 0;
	while(i < data.length){
		totalCount += data[i].length;
		i++;
	}

	//Setup an array of beginning and end locations for the
	//pie chart along with its coloring.
	//Additionally has the $ range for that piece and the amount
	//of people in it.
	//[beg, end, color, $ range, # people]
	var outputData = new Array();
	var iterData;
	var beg = 0;
	var coloring = set1[9];
	i = 0;
	var formatCount = d3.format(",.0f");
	while(i < data.length){
		var pieceLength = (data[i].length/totalCount) * 100;
		iterData = [beg,beg+pieceLength,coloring[i%coloring.length], 
		"Range $" + formatCount(bins[i]) + " to $" + formatCount(bins[i+1]) + ":\n" + formatCount(data[i].length) + " people"];
		outputData[i] = iterData;
		beg += pieceLength;
		i++;
	}

	var arc = d3.svg.arc()
	//Change inner radius to 0 for full pie chart, larger than 0
	//for donut chart
	.innerRadius(25)
	.outerRadius(100)
	.startAngle(function(d){return cScale(d[0]);})
	.endAngle(function(d){return cScale(d[1]);});

	svg.selectAll("path")
	.data(outputData)
	.enter()
	.append("path")
	.attr("d", arc)
	.style("fill", function(d){return d[2];})
	.attr("transform", "translate(100,100)")
	.append("title")
	.text(function(d){ return d[3]; });
}

function generateHistogram(numBins, nameType, incomeType){
	if(numBins < 2)
		numBins = 2;
	else if(numBins > 40)
		numBins=40;

	var setup = setupHistogramData(nameType, incomeType);
	var values = setup.values;
	var svg = setup.svg;
   	var titleText = setup.titleText;
	
	if(values == null)
		return;

	var margin = {top: 35, right: 30, bottom: 45, left: 45},
	    width = 960 - margin.left - margin.right,
	    height = 500 - margin.top - margin.bottom;

	if(nameType == "surname"){
		d3.select("#surname_histogram > svg").remove();
		svg = d3.select("#surname_histogram").append("svg")
	    .attr("width", width + margin.left + margin.right)
	    .attr("height", height + margin.top + margin.bottom)
	  	.append("g")
	    .attr("transform", "translate(" + margin.left + "," + margin.top + ")");
	}
	else if(nameType == "forename"){
		d3.select("#forename_histogram > svg").remove();
		svg = d3.select("#forename_histogram").append("svg")
	    .attr("width", width + margin.left + margin.right)
	    .attr("height", height + margin.top + margin.bottom)
	  	.append("g")
	    .attr("transform", "translate(" + margin.left + "," + margin.top + ")");
	}

	var max = d3.max(values);
	var min = d3.min(values);

	//Setup the x value data. Make it a linear
	//function from the min value to the max value mapped to
	//the pixels of the graph. Make the min and max values
	//rounded, for nice numbers.
	//https://github.com/mbostock/d3/wiki/Quantitative-Scales
	var x = d3.scale.linear()
	    .domain([min, max])
	    .range([0, width]);
	//Create the bins for the histogram
	var bins = new Array();
	var i = 0;
	var binDif = (max-min)/numBins;
	//make sure you get bins up to and including the max amount because
	//of the construction of histograms (splits = numBins+1)
	while(i <= numBins){
		bins[i] = i*binDif;
		i++;
	}

	//Generate a histogram of the values and bins created
	//above
	var data = d3.layout.histogram()
	    .bins(bins)
	    (values);
    //Generate the y values
	var y = d3.scale.linear()
	    .domain([0, d3.max(data, function(d) { return d.y; })])
	    .range([height, 0]);



	//Create half bins to limit the amount of numbers on teh x-axis
	var halfBins = new Array();
	i=0;
	while(i < numBins/2){
		halfBins[i] = bins[2*i];
		i++;
	}
	halfBins[i] = max;

	
	//Create the axis values
	//https://github.com/mbostock/d3/wiki/SVG-Axes
	var xAxis = d3.svg.axis()
	    .scale(x)
	    .orient("bottom")
	    .ticks(numBins/2 + 1)
	    .tickValues(halfBins);
    var yAxis = d3.svg.axis()
        .scale(y)
        .orient("left")
        .tickSubdivide(true)
        .tickPadding(5)
        .ticks(10);

	createBars(svg, data, binDif, x, y, height);
	createAxises(svg, xAxis, yAxis, height, nameType);
    createLabels(svg, width, height, titleText, nameType);

	if(nameType == "surname"){
		$('#surname_histogram_options').show();
    	// $('#loadingsurname').hide();
	}
	else if(nameType == "forename"){
		$('#forename_histogram_options').show();
    	// $('#loadingforename').hide();
	}
}

function setupHistogramData(nameType, incomeType){
	var values;
	var svg;
   	var titleText;
	if(nameType == "surname"){
		svg = surnameHistogramSVG;
		if(incomeType == "mean"){
			values = state.surnameIncomeData.meanIncome;
			titleText = "Histogram of Mean Income for Surname " + state.kdeSurname.toLowerCase().toTitleCase();
		}
		else if (incomeType == "median"){
			values = state.surnameIncomeData.medianIncome;
			titleText = "Histogram of Median Income for Surname " + state.kdeSurname.toLowerCase().toTitleCase();
		}
	}
	else if(nameType == "forename"){
		svg = forenameHistogramSVG;
		if(incomeType == "mean"){
			values = state.forenameIncomeData.meanIncome;
    		titleText = "Histogram of Mean Income for Forename " + state.kdeForename.toLowerCase().toTitleCase();
		}
    	else if(incomeType == "median"){
			values = state.forenameIncomeData.medianIncome;
			titleText = "Histogram of Median Income for Forename " + state.kdeForename.toLowerCase().toTitleCase();
    	}
	}
	var returnValues = {
		values : values,
		svg : svg,
		titleText : titleText
	}
	return returnValues;
}

function createBars(svg, data, binDif, x, y, height){
	var formatCount = d3.format(",.0f");
	var bar = svg.selectAll(".bar")
	    .data(data)
	  .enter().append("g")
	    .attr("class", "bar")
	    .attr("transform", function(d) { return "translate(" + x(d.x) + "," + y(d.y) + ")"; });

	bar.append("rect")
	    .attr("x", 1)
	    .attr("width", x(data[0].x  + data[0].dx) - 1)
	    .attr("height", function(d) { return height - y(d.y); })
	    .append("title")
	    .text(function(d){ return "Range $" + formatCount(d.x) + " to $" + formatCount(d.x+binDif) + ":\n" 
	    	+ formatCount(d.y) + " people"; });

	var barMiddle = (x(data[0].x  + data[0].dx) - 1)/2;
	bar.append("text")
	    .attr("dy", ".75em")
	    .attr("y", -10)
	    .attr("x", function(d){ return barMiddle - formatCount(d.y).toString().width('10px sans-serif')/2; } )
	    .attr("text-anchor", "start")
	    .text(function(d) { if(d.y > 0) return formatCount(d.y); else return "" });
}

function createAxises(svg, xAxis, yAxis, height, nameType){
	svg.append("g")
	    .attr("class", "x axis " + nameType)
	    .attr("transform", "translate(0," + height + ")")
	    .call(xAxis);
    svg.append("g")
	    .attr("class", "y axis " + nameType)
	    .call(yAxis)
}

function createLabels(svg, width, height, titleText, nameType){
	svg.append("text")
    	.attr("class", "histogramTitle")
	    .attr("x", width/2 - titleText.width('16px sans-serif')/2)
	    .attr("y", -20)
		.text(titleText);
	
    var axisLabelText = "Income ($)";
	svg.append("text")
    	.attr("class", "axisLabel")
	    .attr("x", width/2 - axisLabelText.width('10px sans-serif')/2)
	    .attr("y", height + 30)
		.text(axisLabelText);

	var axis = d3.select(".y.axis." + nameType);
    axis.append("text")
		.attr("class", "label")
		.attr("y", 6)
		.attr("dy", ".71em")
		.attr("text-anchor", "middle")
		.attr("transform", "rotate(-90)")
		.text("# of People");
}