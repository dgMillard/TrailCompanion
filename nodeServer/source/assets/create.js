$(document).ready(function(){

});

function addWaypoint()
{

var wpID = $("#waypoint_list").length;

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
alert("Added");


}
