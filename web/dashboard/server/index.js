/* eslint consistent-return:0 */

const express = require('express');
const logger = require('./logger');
const ngrok = require('ngrok');
const rethinkdbConfig = require('../rethinkdb.config');
const RethinkdbWebsocketServer = require('rethinkdb-websocket-server');

const frontend = require('./middlewares/frontendMiddleware');
const isDev = process.env.NODE_ENV !== 'production';

const app = express();

// If you need a backend, e.g. an API, add your custom backend-specific middleware here
// app.use('/api', myApi);

// Initialize frontend middleware that will serve your JS app
const webpackConfig = isDev
	? require('../webpack.config.js')
	: require('../webpack.production.js');

app.use(frontend(webpackConfig));

const port = process.env.PORT || 3000;

// Start your app.
const server = app.listen(port, (err) => {
	if (err) {
		return logger.error(err);
	}

	// Connect to ngrok in dev mode
	// if (isDev) {
	// 	ngrok.connect(port, (innerErr, url) => {
	// 		if (innerErr) {
	// 			return logger.error(innerErr);
	// 		}
	// 		logger.appStarted(port, url);
	// 	});
	// } else {
	// 	logger.appStarted(port);
	// }
	logger.appStarted(port);
});

// Configure rethinkdb-websocket-server to listen on the /db path and proxy
// incoming WebSocket connections to the RethinkDB server running on localhost
// port 28015. Because unsafelyAllowAnyQuery is true, any incoming query will
// be accepted (not safe in production).
RethinkdbWebsocketServer.listen({
	httpServer: server,
	httpPath: '/db',
	dbHost: rethinkdbConfig.host,
	dbPort: rethinkdbConfig.port,
	unsafelyAllowAnyQuery: true,
});
