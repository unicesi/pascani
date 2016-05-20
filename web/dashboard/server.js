/* eslint no-console: 0 */

const path = require('path');
const express = require('express');
const logger = require('./logger');
const ngrok = require('ngrok');
const webpack = require('webpack');
const webpackMiddleware = require('webpack-dev-middleware');
const webpackHotMiddleware = require('webpack-hot-middleware');
const config = require('./webpack.config.js');
const RethinkdbWebsocketServer = require('rethinkdb-websocket-server');

const isDev = process.env.NODE_ENV !== 'production';
const port = isDev ? 3000 : process.env.PORT;
const app = express();

if (isDev) {
	const compiler = webpack(config);
	const middleware = webpackMiddleware(compiler, {
		publicPath: config.output.publicPath,
		contentBase: 'src',
		stats: {
			colors: true,
			hash: false,
			timings: true,
			chunks: false,
			chunkModules: false,
			modules: false
		}
	});

	app.use(middleware);
	app.use(webpackHotMiddleware(compiler));
	app.get('*', function response(req, res) {
		res.write(middleware.fileSystem.readFileSync(path.join(__dirname, 'dist/index.html')));
		res.end();
	});
} else {
	app.use(express.static(__dirname + '/dist'));
	app.get('*', function response(req, res) {
		res.sendFile(path.join(__dirname, 'dist/index.html'));
	});
}

const server = app.listen(port, '0.0.0.0', function onStart(err) {
	if (err) {
		console.log(err);
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
	dbHost: 'localhost',
	dbPort: 28015,
	unsafelyAllowAnyQuery: true,
});
