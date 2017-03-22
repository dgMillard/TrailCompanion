var express = require('express')
var app = express()

app.get('/', function (req, res) {
  res.send('API Functionality Test')
})

app.use(express.static('assets'));

app.listen(3001, function () {
  console.log('Example app listening on port 3001!')
})
