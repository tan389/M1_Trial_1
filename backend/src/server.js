const express = require('express');
const https = require('https');
const fs = require('fs');
const os = require('os');
const app = express();

app.use((req, res, next) => {
  res.header('Access-Control-Allow-Origin', '*');
  next();
});

// BUTTON 1 APIs
app.get('/api/server-ip', (req, res) => {
  // real public IP - you'll see it in curl
  res.json({ ip: req.headers['x-forwarded-for'] || req.socket.remoteAddress });
});

app.get('/api/client-ip', (req, res) => {
  res.json({ ip: req.headers['x-forwarded-for'] || req.socket.remoteAddress });
});

app.get('/api/server-time', (req, res) => {
  res.json({ time: new Date().toLocaleTimeString('en-GB', { timeZone: 'America/Vancouver', hour12: false }) + " G>
});

app.get('/api/my-name', (req, res) => {
  res.json({ firstName: "William", lastName: "Tan", fullName: "Min Teck William Tan" });
});

// BUTTON 2 - add your endpoints here
app.get('/api/button2', (req, res) => {
  res.json({ message: "Button 2 backend ready" });
});

const options = { key: fs.readFileSync('key.pem'), cert: fs.readFileSync('cert.pem') };
https.createServer(options, app).listen(3000, '0.0.0.0', () => {
  console.log('HTTPS running on 0.0.0.0:3000');
});
