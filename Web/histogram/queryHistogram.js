function histogramQuery(nameType){
	if(typeof (nameType) == 'undefined' || name == null){
			return
		}
	if(nameType == 'surname'){
		if(typeof (state.kdeSurname) == 'undefined' || state.kdeSurname == null){
			return;
		}
		queryData = {
			surname : state.kdeSurname
		};
		positionLoading();
		$('#loadingsurname').show();
	}
	else if(nameType == 'forename'){
		if(typeof (state.kdeForename) == 'undefined' || state.kdeForename == null){
			return;
		}
		queryData = {
			forename : state.kdeForename
		};
		positionLoading();
		$('#loadingforename').show();
	}
	else{
		return;
	}
	
	histogramQueryImp(queryData, nameType);
}

function positionLoading(){
	var leftpos = $('#surname_histogram').width();
	var height = $('#surname_histogram').height();
	var xPos = $('#surnameInputs').height() + $('#surnameInputs').position().top;
	var toppos = (height/2) + xPos;
	$('#loadingsurname').css('left', '' + leftpos/4 + 'px');
	$('#loadingsurname').css('top', '' + toppos + 'px');
	leftpos = $('#forename_histogram').width();
	height = $('#forename_histogram').height();
	xPos = $('#forenameInputs').height() + $('#forenameInputs').position().top;
	toppos = (height/2) + xPos;
	// alert(height);
	// alert(xPos);
	$('#loadingforename').css('left', '' + leftpos/4 + 'px');
	$('#loadingforename').css('top', '' + toppos + 'px');
}

function histogramQueryImp(configuredData, nameType){
	if(typeof (nameType) == 'undefined' || name == null){
		return;
	}
	if(typeof (configuredData) == 'undefined' || configuredData == null){
		return;
	}
	var scriptLocation;
	if(nameType == "surname"){
		scriptLocation = state.serviceBase + 'services/queryIncomeSurname?callback=?';
	}
	else if(nameType == "forename"){
		scriptLocation = state.serviceBase + 'services/queryIncomeForename?callback=?';
	}
	else{
		return;
	}
	$.ajax(scriptLocation, {
		data : configuredData,
		dataType : 'jsonp',
		global: true,

		error : function(jqXHR, textStatus, errorThrown) {
			console.log(textStatus);
			console.log(jqXHR);
			console.log(errorThrown);
			alert('Error');
			$('#loadingsurname').hide();
		},
		success : function(data, textStatus, jqXHR) {
			addHistogram(data, nameType);
		}
	});
}