$(document).ready(function(){

});

function addWaypoint()
{

var wpID = $("#waypoint_list li").length;

var newWaypoint = `
<li>
Waypoint name:<br>
<input type="text" name="${wpID}_wp_name"></input>
<br>Waypoint description:<br>
<input type="text" name="${wpID}_wp_desc"></input>
<br>Waypoint X loc:<br>
<input type="text" name="${wpID}_wp_xloc"></input>
<br>Waypoint Y loc:<br>
<input type="text" name="${wpID}_wp_yloc"></input>
<br>Attached File:<br>
<input type="file" name="${wpID}_wp_file" id="${wpID}_wp_file"></input>
<br>Upload Progress:<br>
<div class="progress active" >
	<div class="progress-bar" id="${wpID}_wp_progress" role="progressbar" aria-valuenow="0" aria-valuemin="0" aria-valuemax="100"</div>
</div>
</li>
`

$("#waypoint_list").append(newWaypoint);

}

function submitTour()
{
	//Collect form data...
	var data = $("#tour_form").serializeArray();
	var parsed = {};
	var numWaypoints = $("#waypoint_list li").length;
	var formComplete = true;
	data.forEach(function(i){
		parsed[i.name] = i.value;
		console.log("Added " + i.name);
		if( i.value === "" )
		{
			console.log(i.name + " is incomplete.");
			formComplete = false;
		}
	});
	parsed["waypoint_count"] = numWaypoints;	
	//Now, verify file upload names/existence
	for( var i=0; i < numWaypoints; i++)
	{
		if( $("#"+i+"_wp_file")[0].files.length == 0)
		{
			formComplete = false;
			//Add file-less Waypoints later
			console.log("Waypoint " + (i+1) +" does not have a file associated with it.");
		}
		else
		{
			//Add the file into the data sent
			parsed[i+"_wp_filename"] = $("#"+i+"_wp_file")[0].files[0].name;
			console.log(parsed[i+"_wp_filename"]);
		}
	}
	//Eventually need to verify file size / mimetype

	//TODO Verify form data
	if(!formComplete)
	{
		alert("Please complete form entirely.");
		return;
	}



	//Upload each file,
	//Then, if all succeed, submit final form

	for(var i=0; i < numWaypoints; i++) 
	{
		var formData = new FormData();
		//Check for undefined...
		formData.append( 'targetFile' , $("#"+i+"_wp_file")[0].files[0]);
		console.log("Starting " + i);
		$.ajax({
			url: '/actions/upload',
			type: 'POST',
			data: formData, 
		   
			//Disables interpretation by JQuery.,
			contentType: false,
			processData: false,
			cache: false,

			// XMLHttpRequest to monitor progress
			xhr: function() {
				var monitorXHR = $.ajaxSettings.xhr();
				var whichBar = i;
				if (monitorXHR.upload) {
					// For handling the progress of the upload
					monitorXHR.upload.addEventListener('progress', function(evt) {
						//Add updater code here
						if(evt.lengthComputable)
						{
							self.progress = evt.loaded / evt.total;
						}
						else if (this.explicitTotal)
						{
							//If server is not providing a total
							self.progress = Math.min(1, evt.loaded / slef.explicitTotal);
						}
						else
							self.progress = 0;
						console.log("For file: " +whichBar+ ": " + self.progress);
							
						$("#"+whichBar+"_wp_progress").css('width', self.progress*100+'%').attr('aria-valuenow', self.progress*100);
						if( self.progress == 1)
							$("#"+whichBar+"_wp_progress").addClass("progress-bar-success");
						
						
					} , false);
				}
				return monitorXHR;
			},
		});
	}//End of file for loop
	$.post("/actions/submitTour", parsed)
	.done(function( data ) {
		//alert( "(Done) Got back:" + data );
	})
	.fail(function( data ) {
		//alert( "(Fail) Got back:" + data );
	});



}
