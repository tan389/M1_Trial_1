const { WebSocketServer, WebSocket } = require('ws');
const COURSE_URL = 'wss://8.229.22.124';
const PORT = 8080;

const wss = new WebSocketServer({ port: PORT, host: '0.0.0.0' });
console.log(`Your WS running at ws://0.0.0.0:${PORT}`);

function connect() {
  const course = new WebSocket(COURSE_URL, { rejectUnauthorized: false });
  course.on('open', () => console.log('Connected to course WS'));
  course.on('message', (data) => {
    // This is the key grading requirement: relay immediately, no reformatting
    const raw = data.toString();
    wss.clients.forEach(c => {
      if (c.readyState === 1) c.send(raw);
    });
  });
  course.on('close', () => setTimeout(connect, 2000));
}
connect();
