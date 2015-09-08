+function($) {
	'use strict';

	// UPLOAD CLASS DEFINITION
	// ======================

	var pastePanelButton = document.getElementById('paste-panel-button');
	var fileSelectButton = document.getElementById('file-select-button');
	var dropZone = document.getElementById('drop-zone');
	var uploadFileInput = document.getElementById('js-upload-files');
	var fileGlobal;

	pastePanelButton.onclick = function(e) {
		// hide previous alerts
		jQuery("#response-text").hide();
		jQuery("#submitted-text").hide();
		jQuery("#fail-text").hide();

		// show contents of paste panel under the panel
		var submittedText = jQuery("textarea#paste-panel").val();

		// make POST request to REST API
		postFunction(submittedText);

		// reset form
		clearAllContent();
		fileGlobal = null;
	}

	fileSelectButton.onclick = function(evt) {

		// if a file was chosen via the file input option
		if (uploadFileInput.files.length > 0) {
			// read file and show contents in message below
			fileGlobal = uploadFileInput.files[0];
		}

		if (fileGlobal) {
			// hide previous alerts
			jQuery("#response-text").hide();
			jQuery("#submitted-text").hide();
			jQuery("#fail-text").hide();

			var r = new FileReader();
			r.onload = function(e) {
				var submittedText = e.target.result;

				// make POST request to REST API
				postFunction(submittedText);
			}
			r.readAsText(fileGlobal);

			// reset form
			clearAllContent();
			fileGlobal = null;
		}
	}

	dropZone.ondrop = function(e) {
		// if a file has not already been chosen via the file input
		if (uploadFileInput.files.length > 0) {
			return false;
		} else {

			// stop any further files being dragged into drag zone
			e.preventDefault();

			// color black
			this.className = 'upload-drop-zone complete';

			// stop further upload by the file input
			uploadFileInput.addEventListener("click", listener);

			// read file and show contents below panel
			var dt = e.dataTransfer;
			var files = dt.files;
			if (dt.files.length > 0) {
				var file = dt.files[0];

				if (file) {
					var fileName = file.name;
					fileGlobal = file;
					jQuery("#drop-zone").html(
							"<i class=\"fa fa-file-o\"></i> <b>".concat(
									fileName, "</b>"));
				} else {
					alert("Failed to load file");
				}
			}
		}
	}

	dropZone.ondragover = function() {
		// if a file has not been selected via the file input
		if (uploadFileInput.files.length == 0) {

			// change the styles to black
			this.className = 'upload-drop-zone drop';
		}
		return false;
	}

	dropZone.ondragleave = function() {
		// return style to grey if drop does not occur
		this.className = 'upload-drop-zone';
		return false;
	}

	uploadFileInput.onchange = function(evt) {
		// stop drag events
		$("drop-zone").off("ondrop");
	}

}(jQuery);

function listener(event) {
	event.preventDefault();
}

/**
 * send post request to server and update alert panels
 */
function postFunction(submittedText) {
	
	// show waiting icon
	jQuery("#conversion-wait").show();

	// get to and from document formats froh html select elements
	var fromDoc = document.getElementById("fromDoc");
	var fromValue = fromDoc.options[fromDoc.selectedIndex].value;
	var toDoc = document.getElementById("toDoc");
	var toValue = toDoc.options[toDoc.selectedIndex].value;
	
	var newFromValue = fromValue;
	if (fromValue == "to") {
		newFromValue = "autodetected format"
	}
	
	// show the text submitted by the user
	jQuery("#submitted-text").show();
	jQuery("#fromSpan").text(newFromValue);

	jQuery("#toSpan").text(toValue);
	jQuery("#submitted-text-content").text(submittedText);

	// construct url to post 
	var partUrl = "/omnweb/convert/";
	var url = partUrl.concat(fromValue, "/", toValue);

	// perform post request
	$.post(url, {
		content : submittedText
	}, function(data, status, jqXHR) {
		jQuery("#conversion-wait").hide();
		jQuery("#response-text").show();
		jQuery("#response-text-content").text(jqXHR.responseText);
	}).fail(function(jqXHR, textStatus, errorThrown) {
		jQuery("#conversion-wait").hide();
		jQuery("#fail-text").show();
		
		if(jqXHR.status > 399 && jqXHR.status < 500){
			jQuery("#error-code").text("Your input does not appear to be valid. Please correct your input and try again.");
		}

		if(jqXHR.status > 499 && jqXHR.status < 600){
			jQuery("#error-code").text("Oops! Something went wrong while processing your request. Please try again or contact us if it happens again.");
		}
				
		jQuery("#fail-text-content").text(jqXHR.responseText);
	});
}

/**
 * reset the forms and text alerts
 */
function clearAllContent() {
	document.getElementById('paste-panel').value = "";
	document.getElementById('js-upload-files').value = "";
	document.getElementById('drop-zone').className = 'upload-drop-zone';
	jQuery("#drop-zone").text("Just drag and drop the file here");
	document.getElementById('js-upload-files').removeEventListener("click",
			listener);
}