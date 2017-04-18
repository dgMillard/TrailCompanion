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


pageRouter.get('/', function (req, res) {
		// Check if already logged in
		// redirect to dashboard
		// else
		// login page
		
		res.redirect('/login.html');

});


pageRouter.get('/login.html', function (req, res) {

		
		var failedLogin = false;
		if(!isUndefined( req.query.retry))
		{
			failedLogin = true;
		}

		var rData = {loginFailure: failedLogin}

		var page = fs.readFileSync('login.html', "utf8"); 

		var html = mustache.to_html(page, rData); 
		res.send(html);

});

pageRouter.get('/dashboard.html', function (req, res) {
		// Check if already logged in
		var page = fs.readFileSync('dashboard.html', "utf8"); // bring in the HTML file
		html = page;
		res.send(html);
});


apiRouter.get('/listSpecific', function(req, res){
		//	var rData = {records:demoData}
		var jsonPage = fs.readFileSync('testJson', "ascii");



		res.send(jsonPage);
		});


app.use(session({
	resave: false,
    saveUninitialized: false,	
	store: new fileStore({}),
	secret: securityInfo.session 
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

