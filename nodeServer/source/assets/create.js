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
</li>
`

$("#waypoint_list").append(newWaypoint);

}

function submitTour()
{
	var data = $("#tour_form").serializeArray();
	//Add ajax here
	var parsed = {};

	data.forEach(function(i){
		parsed[i.name] = i.value;
	});

	parsed["waypoint_count"] = $("#waypoint_list li").length;

	$.post("/actions/submitTour", parsed)
	.done(function( data ) {
		alert( "(Done) Got back:" + data );
	})
	.fail(function( data ) {
		alert( "(Fail) Got back:" + data );
	});

}
