import React from 'react';
import ReactRethinkdb from 'react-rethinkdb';
import reactMixin from 'react-mixin';
import styles from './App.css';

const r = ReactRethinkdb.r;

// Open a react-rethinkdb session (a WebSocket connection to the server)
ReactRethinkdb.DefaultSession.connect({
	host: 'localhost', // hostname of the websocket server
	port: 3000,        // port number of the websocket server
	path: '/db',       // HTTP path to websocket route
	secure: false,     // set true to use secure TLS websockets
	db: 'test',        // default database, passed to rethinkdb.connect
});

class App extends React.Component {

	constructor(props) {
		super(props);
		this.state = {};
	}

	componentDidMount() {
		this.nameInput.focus();
	}

	observe(props, state) { // eslint-disable-line no-unused-vars
		return {
			turtles: new ReactRethinkdb.QueryRequest({
				query: r.table('turtles'), // RethinkDB query
				changes: true,             // subscribe to realtime changefeed
				initial: [],               // return [] while loading
			}),
		};
	}

	handleSubmit = (event) => {
		event.preventDefault();
		const name = this.nameInput.value.trim();
		const query = r.table('turtles').insert({name: name});
		this.nameInput.value = '';
		ReactRethinkdb.DefaultSession.runQuery(query);
	}

	render() {
		const turtleDivs = this.data.turtles.value().map(x =>
			<div key={x.id}>{x.name}</div>
		);
		return (
			<div className={styles.app}>
				<form onSubmit={this.handleSubmit}>
					<input type="text" ref={(ref) => this.nameInput = ref} />
					<input type="submit" />
				</form>
			{turtleDivs}
			</div>
		);
	}
}

// Enable RethinkDB query subscriptions in this component
reactMixin.onClass(App, ReactRethinkdb.DefaultMixin);

export default App;
