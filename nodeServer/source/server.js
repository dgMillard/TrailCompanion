var express      = require('express')
var actionRouter = express.Router()
var pageRouter   = express.Router()
var apiRouter    = express.Router()
var app          = express()
var	fs           = require('fs') // bring in the file system api
var	mustache     = require('mustache') // bring in mustache template engine
var bodyParser   = require('body-parser')


var	demoData = [{ // dummy data to display
		"name":"Steve Balmer",
		"company": "Microsoft",
			"systems": [{
				"os":"Windows XP"
				},{
				"os":"Vista"
				},{
				"os":"Windows 7"
				},{
				"os":"Windows 8"
			}]
				
		},{
 		"name":"Steve Jobs",
		"company": "Apple",
			"systems": [{
				"os":"OSX Lion"
				},{
				"os":"OSX Leopard"
				},{
				"os":"IOS"
			}]
		},{	
		"name":"Mark Z.",
		"company": "Facebook"
	}];
 

actionRouter.post('/login', function (req, res) {
	console.log(req.body);
});


pageRouter.get('/', function (req, res) {

	// Check if already logged in


	var rData = {}
	var page = fs.readFileSync('login.html', "utf8"); // bring in the HTML file

	var html = mustache.to_html(page, rData); // replace all of the data
	res.send(html);
});
pageRouter.get('/demo', function(req, res){
	var rData = {records:demoData}
	var page = fs.readFileSync('mypage.html', "utf8"); // bring in the HTML file

	var html = mustache.to_html(page, rData); // replace all of the data
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

app.listen(3000, function () {
  console.log('Example app listening on port 3000!')
})

