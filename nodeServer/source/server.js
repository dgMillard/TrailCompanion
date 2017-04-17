var express      = require('express')
var actionRouter = express.Router()
var pageRouter   = express.Router()
var apiRouter    = express.Router()
var app          = express()
var	fs           = require('fs') // bring in the file system api
var	mustache     = require('mustache') // bring in mustache template engine
var bodyParser   = require('body-parser')

var bcrypt		 = require('bcrypt')
var port 		 = 3001
var mysql		 = require('mysql')
var	demoData =[
			"Item 1",
			"Item 2",
			"Item 3",
			"Item 4"
		];
	

//Startup Config
var salt = bcrypt.genSaltSync(10);
var dbInfo = require("./mysql.json") 
var connection = mysql.createConnection({
  host     : 'localhost',
  user     : dbInfo.database.username,
  password : dbInfo.database.password,
  database : dbInfo.database.name
});
connection.connect();
 
function isUndefined(input)
{
	return !(typeof input != 'undefined');
}



actionRouter.post('/login', function (req, res) {
	console.log(req.body);


	if( isUndefined(req.body))
	{}
	var username = req.body.username;
	var password = req.body.auth;
	if( isUndefined(username) || isUndefined(password))
	{
		console.log(username);
		console.log(password);
		console.log('Invalid username or password. Return failure');
		res.status(400);
		res.send("Missing information");
		res.end();
		return;
	}




	console.log(username);
	console.log(password);
	var success = false;

	connection.query('SELECT password FROM User WHERE username = ?', [username],  function (error, results, fields) {
		if (error) throw error;
		if( results.length == 0)
		{
			
			console.log('Username not found.');	
			console.log(error);
		}
		else
		{
			console.log(results);
			authToken = results[0].password;
			console.log(authToken);

			if(bcrypt.compareSync(password, authToken))
			{
				console.log("Got it!");
				success = true;
			}
			else
				console.log("Wrong");
		}
	    if(success)
		{
			res.status(200);
			res.send("Success");
			res.end();
		}
		else
		{
			res.status(401);
			res.send("Bad Auth");
			res.end();
			
		}
	});



});


pageRouter.get('/', function (req, res) {

		// Check if already logged in


		var page = fs.readFileSync('login.html', "utf8"); // bring in the HTML file

		html = page;
		res.send(html);
		});



pageRouter.get('/demo', function(req, res){
		var rData = {elementList: demoData}
		var page = fs.readFileSync('demoPage.html', "utf8"); 

		var html = mustache.to_html(page, rData); 
		res.send(html);
		});




apiRouter.get('/listSpecific', function(req, res){
		//	var rData = {records:demoData}
		var jsonPage = fs.readFileSync('testJson', "ascii");



		res.send(jsonPage);
		});


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

