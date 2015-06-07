+function($) {
	'use strict';

	// UPLOAD CLASS DEFINITION
	// ======================

	var pastePanelButton = document.getElementById('paste-panel-button');
	var fileSelectButton = document.getElementById('file-select-button');
	var dropZone = document.getElementById('drop-zone');
	var uploadForm = document.getElementById('js-upload-form');
	var uploadButton = document.getElementById('js-upload-submit');
	var uploadFileInput = document.getElementById('js-upload-files');
	var fileGlobal;

	var startUpload = function(files) {
		console.log(files)
		var data = new FormData();
		data.append('file', files[0], files[0].name);
		console.log(files[0]);
		console.log(files[0].name);

		$.ajax({
			url : uploadForm.action,
			type : 'POST',
			data : data,
			cache : false,
			processData : false, // Don't process the files
			contentType : false, // Set content type to false as jQuery will
			// tell the server its a query string
			// request
			success : function(data, textStatus, jqXHR) {
				console.log(textStatus);
				if (typeof data.error === 'undefined') {
					// Success so call function to process the form
					submitForm(event, data);
				} else {
					// Handle errors here
					console.log('ERRORS: ' + data.error);
				}
			},
			error : function(jqXHR, textStatus, errorThrown) {
				// Handle errors here
				console
						.log('ERRORS: ' + textStatus + ": "
								+ jqXHR.responseText);
				// STOP LOADING SPINNER
			}
		});
	}

	// uploadForm.addEventListener('submit', function(e) {
	// var uploadFiles = uploadFileInput.file;
	// e.preventDefault()
	//
	// startUpload(uploadFiles)
	// })

	pastePanelButton.onclick = function(e) {
		// show contents of paste panel under the panel
		var sumittedText = jQuery("textarea#paste-panel").val();
		jQuery("#submitted-text").show();
		jQuery("#submitted-text-content").text(sumittedText);

		// TODO 
		// startUpload();
		
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

			var r = new FileReader();
			r.onload = function(e) {
				var contents = e.target.result;
				jQuery("#submitted-text").show();
				jQuery("#submitted-text-content").text(contents);
			}
			r.readAsText(fileGlobal);

			// TODO 
			// startUpload();
			
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

function clearAllContent() {
	document.getElementById('paste-panel').value = "";
	document.getElementById('js-upload-files').value = "";
	document.getElementById('drop-zone').className = 'upload-drop-zone';
	jQuery("#drop-zone").text("Just drag and drop the file here");
	document.getElementById('js-upload-files').removeEventListener("click",
			listener);
}