var express      = require('express')
var actionRouter = express.Router()
var pageRouter   = express.Router()
var apiRouter    = express.Router()
var app          = express()
var	fs           = require('fs') // bring in the file system api
var	mustache     = require('mustache') // bring in mustache template engine
var bodyParser   = require('body-parser')
var path		 = require('path')
var bcrypt		 = require('bcrypt')
var mysql		 = require('mysql')
var helmet		 = require('helmet') // Security middleware
var session   	 = require('express-session')
var fileUpload	 = require('express-fileupload')
var fileStore	 = require('session-file-store')(session);
var mime		 = require('mime')
var wait		 = require('wait.for')
var archiver	 = require('archiver')
var fileHash	 = require('hash-files')
var sha1		 = require('sha1')


//Startup Config
var fileVault    = '/var/lib/trailCompanion/fileVault';
var port 		 = 3000
var salt = bcrypt.genSaltSync(10);
var securityInfo = require('./security.json') 
function dbConnect()
{
	var connection = mysql.createConnection({
		host     : 'localhost',
		user     : securityInfo.database.username,
		password : securityInfo.database.password,
		database : securityInfo.database.name
	});
	connection.connect();
	return connection;
}

// Helper function to check if variables are undefined. 
function isUndefined( input )
{
	return !(typeof input != 'undefined');
}

// Translator function to convert form data into desired json format
function genWaypointJson( input )
{
	var numWaypoints = input.waypoint_count;

	var outputJson = new Array(numWaypoints);

	for( var i=0; i < numWaypoints; i++)
	{
		var prefix = i + '_wp_';

		outputJson[i] = {
			'name' : input[prefix + 'name'],
			'desc' : input[prefix + 'desc'],
			'xLoc' : input[prefix + 'xloc'],
			'yLoc' : input[prefix + 'yloc'],
			'asset': input[prefix + 'filename'],
			'index': i
		}
	}
	console.log(outputJson);
	return outputJson;
}

actionRouter.post('/logout', function (req, res) {
	req.session.destroy(function(err){
		if(err){
			console.log('Failed to logout/end user session.');
		}
		else
		{
			res.clearCookie(sessionOptions.name);
			res.redirect('/login.html?logout=true');
			res.end();
		}
	});

});


actionRouter.post('/login', function (req, res) {

	if( isUndefined(req.body))
	{}
	var username = req.body.username;
	var password = req.body.auth;
	if( isUndefined(username) || isUndefined(password))
	{
		res.redirect('/login.html?retry=true');
		res.end();
		return;
	}
	
	var success = false;
	connection = dbConnect();
	connection.query('SELECT password FROM User WHERE username = ?', [username],  function (error, results, fields) {
		if (error) throw error;
		if( !(results.length == 0))
		{
			authToken = results[0].password;

			if(bcrypt.compareSync(password, authToken))
				success = true;
		}
	    if(success)
		{
			req.session.auth = true;
			res.redirect('/dashboard.html');
			res.end();
		}
		else
		{
			res.redirect('/login.html?retry=true');
			res.end();
		}
		connection.end();
	});
});

actionRouter.post('/upload', function (req, res) {

	if(! req.files || isUndefined(req.files.targetFile) || isUndefined(req.files.targetFile.name))
	{
		//No files submitted
		res.status(400);
		res.send('No files selected or invalid parameters provided.');
		res.end();
		return;
	}

	var targetFile = req.files.targetFile;
	var extension = mime.extension(targetFile.mimetype);
	
	var filename = targetFile.name;

	var regex = new RegExp(extension + '$', 'gi');
	
	console.log('Extension is: ' + extension);
	console.log('Before: ' + filename);
	filename = filename.replace(regex, '.' + extension);
	console.log('After: ' + filename);
	targetFile.mv(fileVault + '/staging/' + filename, function(err) {
		if (err)
		{
			res.status(500);
			res.send(err);
			return;
		}

		// All good:
		res.send('Upload Complete');
	});
});


function postToTours(req, res, metadata)
{
	
	connection = dbConnect();

	var queryString = 'INSERT INTO Tour (uid, organization, name, description, numberOfWaypoints, length, archiveLocation, version, topLeftX, topLeftY, bottomRightX, bottomRightY, published)VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?)';
	var values = [
		metadata.tour_uid,
		metadata.organization,
		metadata.tour_name,
		metadata.tour_desc,
		metadata.waypoint_count,
		1,
		fileVault + '/database/' + metadata.tour_uid + '.zip',
		'1.0.0',
		metadata.mapInfo.topLeft.x,
		metadata.mapInfo.topLeft.y,
		metadata.mapInfo.bottomRight.x,
		metadata.mapInfo.bottomRight.y,
		1	
	];

	//Verify tour does not already exist
	connection.query(queryString, values,  function (error, results, fields) {
		if (error)
		{	
			res.status(400);
			res.end('Failed to add tour to DB.');
			return;
		}
		console.log('INSERTED: ');
		console.log(results);
		postToWaypoints(req, res, metadata);	
	});
	return;
}
function postToWaypoints(req, res, metadata)
{
		connection.end();	
		res.status(200);
		res.end('done early');
}
function finishTourSubmit(req, res)
{
}



function handleTourSubmission(req, res)
{
	//Must verify cookie for post priveledge TODO
	//	var tourName = req.body.tourName;
	//	var tourDescription = req.body.tourDesc;
	//Verify files exist before inserting into db

	var tourName 		= req.body.tour_name;
	var tourDescription = req.body.tour_desc;
	var numWaypoints 	= req.body.waypoint_count;
	var waypointJson 	= genWaypointJson( req.body );
	var tourID = sha1(tourName + tourDescription + JSON.stringify(waypointJson)); 	
	var mapData			= {
		'topLeft' :{
			'x' : req.body.map_topLeft_x,
			'y' : req.body.map_topLeft_y
		},
		'bottomRight' :{
			'x' : req.body.map_bottomRight_x,
			'y' : req.body.map_bottomRight_y
		}
	};
	
	// Meta file:
	var metadata = {
		'organization' 		: 'Santiam Wagon Trail',
		'tour_name'    		: tourName,
		'tour_desc'      	: tourDescription,
		'tour_uid'			: tourID,
		'waypoint_count'    : numWaypoints,
		'mapInfo'			: mapData,
		'waypoints'    		: waypointJson
	}


	// Zip Creation:
	var destinationStream = fs.createWriteStream(fileVault + '/database/' + tourID + '.zip');	
	var archive = archiver('zip' , { store : true } );	
	
		
	archive.on('error', function(err) {
		//Remove the zip and respond to the res object
		throw err;
	});
	
	//For each file
	for( var i=0; i < numWaypoints; i++)
	{
		var filename = waypointJson[i].asset;
		//archive.file('/var/lib/trailCompanion/fileVault/staging/' + filename, { name: 'waypoint' + i + path.extname(filename)});
		archive.file('/var/lib/trailCompanion/fileVault/staging/' + filename, { name: filename });
		
	}
	//Write the metadata
	archive.append(JSON.stringify(metadata), { name: 'metadata.json' });

	archive.pipe(destinationStream);
	archive.finalize();

	// listen for all archive data to be written 
	destinationStream.on('close', function() {
			console.log(archive.pointer() + ' total bytes');
			console.log('archiver has been finalized and the destinationStream file descriptor has closed.');
			console.log('Now I can add myself to the DB....');
			postToTours(req, res, metadata);
	});



//	console.log('received...');
//	console.log(req.body);
//	res.send('Success');
//	res.end();


}


actionRouter.post('/submitTour', function (req, res) {

	//Spins off a fiber to handle this without trashing node mainloop
	wait.launchFiber(handleTourSubmission, req, res);

});

pageRouter.get('/', function (req, res) {
		//If user is already logged in, reroute to dashboard
		if( req.session.auth )
		{
			res.redirect('/dashboard.html');
			res.end();
			return;
		}

		res.redirect('/login.html');
		res.end();
});


pageRouter.get('/login.html', function (req, res) {
	
		//If user is already logged in, reroute to dashboard
		if( req.session.auth )
		{
			res.redirect('/dashboard.html');
			res.end();
			return;
		}
		
		var failedLogin = false;
		if(!isUndefined( req.query.retry) && req.query.retry)
		{
			failedLogin = true;
		}
		var userLogout = false;
		if(!isUndefined( req.query.logout) && req.query.logout)
		{
			userLogout = true;
		}


		var rData = {loginFailure: failedLogin, userLoggedOut: userLogout}

		var page = fs.readFileSync('login.html', 'utf8'); 

		var html = mustache.to_html(page, rData); 
		res.send(html);

});

pageRouter.get('/tours/create.html', function (req, res) {
	if(!req.session.auth )
	{
		res.redirect('/login.html');
		res.end();
		return;
	}

	var page = fs.readFileSync('create.html', 'utf8');

	res.send(page);
});

function populateDashboard(req, res, results)
{
}

pageRouter.get('/dashboard.html', function (req, res) {
		// Check if already logged in
		
		var session = req.session;
		if(! session.auth)
		{
			//User is not logged in, send them to login page
			res.redirect('/login.html');
			res.end();
			return;
		}

		var page = fs.readFileSync('dashboard.html', 'utf8'); // bring in the HTML file
		connection = dbConnect();
		connection.query('SELECT uid, name, description, organization FROM Tour WHERE published = 1', function (error, results, fields) {
			if (error) throw error;
		
			//var pageData = { 'published' : [
				//	'name' : 'demoName',
				//	'desc' : 'demoDesc',
				//	'editLink' : 'revision.html?edit=uidHere' 
			//] }
			var pageData = { 'published' : new Array( results.length ) };
		
			for( var i=0; i < results.length; i++)
			{
				pageData['published'][i] = 
				{
					'name' : results[i].name,
					'desc' : results[i].description,
					'organization' : results[i].organization,
					'editLink' : '/tours/edit.html?tour=' + results[i].uid
				}
			}
		
			var html = mustache.to_html(page, pageData); 
			res.send(html);
			return;
		});

	return;
});




apiRouter.get('/listSpecific', function(req, res){
	var jsonPage = fs.readFileSync('testJson', 'ascii');
	res.send(jsonPage);
});

apiRouter.get('/listAll', function(req, res){

	connection = dbConnect();
	connection.query('SELECT uid, name, description FROM Tour WHERE published = 1', function (error, results, fields) {
		if (error) throw error;
		
		
		var dataJson = { 'tourListings' : new Array(results.length) }

		for(var i=0; i < results.length; i++)
		{
			dataJson.tourListings[i] = {
				'tour_uid' :  	results[i].uid,
				'tour_name':	results[i].name,
				'tour_desc':	results[i].description
			};
		}

		connection.end();

		res.status(200);
		res.send(dataJson);
		return;
	});
	return;
});


apiRouter.get('/download/:uid', function(req, res){

	console.log(req.params.uid);

	if(isUndefined(req.params.uid) || req.params.uid.length != 40)
	{
		res.status(400);
		res.send("Invalid or missing uid");
		return;
	}
	
	var desiredUID = req.params.uid;
	var realUID = '';
	connection = dbConnect();
	connection.query('SELECT uid FROM Tour WHERE published = 1 AND uid = ?', [desiredUID]  , function (error, results, fields) {
		if (error) throw error;
		
		//Handle error gracefully...
		console.log(results.length);
		if(results.length === 1)
		{
			realUID = results[0].uid;
			res.status(200);
			res.sendFile(fileVault + '/database/' + realUID + '.zip');	
		}
		else
		{
			res.status(404);
			res.end();
		}

		connection.end();
		return;
	});

	return;
});

var sessionOptions = {
	resave: false,
    saveUninitialized: false,	
	store: new fileStore({}),
	secret: securityInfo.session,
	name: 'sessionToken.sid'
}

app.use(session(sessionOptions));
app.use(helmet());
app.use(fileUpload({
	
	limits: { fileSize: 256 * 1024 * 1024 },
	safeFileNames: true

}));
app.use( bodyParser.urlencoded({
extended: true
}));

app.use(express.static('assets'));
app.use('/', pageRouter);
app.use('/actions/', actionRouter);
app.use('/api/', apiRouter);

app.listen(port, function () {
		console.log('Example app listening on port ' +port)
		})

