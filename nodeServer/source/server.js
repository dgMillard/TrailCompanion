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
var fileStore	 = require('session-file-store')(session);



//Startup Config
var port 		 = 3001
var salt = bcrypt.genSaltSync(10);
var securityInfo = require("./security.json") 
var connection = mysql.createConnection({
  host     : 'localhost',
  user     : securityInfo.database.username,
  password : securityInfo.database.password,
  database : securityInfo.database.name
});
connection.connect();


function isUndefined(input)
{
	return !(typeof input != 'undefined');
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
	});
});

actionRouter.post('/upload', function (req, res) {



});


actionRouter.post('/submitTour', function (req, res) {
	//Must verify cookie for post priveledge TODO
//	var tourName = req.body.tourName;
//	var tourDescription = req.body.tourDesc;
	//Verify files exist before inserting into db
	//Incoming data looks like:
	//	
	`{ tour_name: '1',
		tour_desc: '2',
		'0_wp_name': '',
		'0_wp_desc': '',
		'0_wp_xloc': '',
		'0_wp_yloc': '',
		'1_wp_name': '',
		'1_wp_desc': '',
		'1_wp_xloc': '',
		'1_wp_yloc': '',
		'2_wp_name': '',
		'2_wp_desc': '',
		'2_wp_xloc': '',
		'2_wp_yloc': '' }
	`

	var tourName = req.body.tour_name;
	var tourDescription = req.body.tour_desc;
	var numWaypoints = req.body.waypoint_count;

	//Must:
	//
	//	Verify variables
	//	Insert name/description/waypoint_count
	//
	//

	console.log(req.body);
	res.send("Success");
	res.end();

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

		var page = fs.readFileSync('login.html', "utf8"); 

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

		var page = fs.readFileSync('dashboard.html', "utf8"); // bring in the HTML file
		html = page;
		res.send(html);
});




apiRouter.get('/listSpecific', function(req, res){
		//	var rData = {records:demoData}
		var jsonPage = fs.readFileSync('testJson', "ascii");



		res.send(jsonPage);
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

