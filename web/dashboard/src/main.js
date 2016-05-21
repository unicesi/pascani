import React from 'react';
import ReactDOM from 'react-dom';
import ReactRethinkdb from 'react-rethinkdb';
import RethinkdbConfig from '../rethinkdb.config';

import 'jquery';
import 'metro';
import App from './App';

// Open a react-rethinkdb session (a WebSocket connection to the server)
ReactRethinkdb.DefaultSession.connect({
	host: 'localhost',       // hostname of the websocket server
	port: 3000,              // port number of the websocket server
	path: '/db',             // HTTP path to websocket route
	secure: false,           // set true to use secure TLS websockets
	db: RethinkdbConfig.db,  // default database, passed to rethinkdb.connect
});

ReactDOM.render(<App />, document.getElementById('root'));
