import React, {Component} from 'react';
import ReactRethinkdb from 'react-rethinkdb';
import reactMixin from 'react-mixin';

const r = ReactRethinkdb.r;

// Open a react-rethinkdb session (a WebSocket connection to the server)
ReactRethinkdb.DefaultSession.connect({
	host: 'localhost', // hostname of the websocket server
	port: 3000,        // port number of the websocket server
	path: '/db',       // HTTP path to websocket route
	secure: false,     // set true to use secure TLS websockets
	db: 'test',        // default database, passed to rethinkdb.connect
});

class Turtles extends Component {

	constructor(props) {
		super(props);
	}

	componentDidMount() {
		this.nameInput.focus();
	}

	handleSubmit = (event) => {
		event.preventDefault();
		const name = this.nameInput.value.trim();
		const query = r.table('turtles').insert({name: name});
		this.nameInput.value = '';
		ReactRethinkdb.DefaultSession.runQuery(query);
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

	render() {
		const icon = (name) => {
			return require(`../svg/${name}.svg`)
		};
		const items = this.data.turtles.value().map(x =>
			<div key={x.id}>{x.name}</div>
		);
		return (
			<div>
				<form onSubmit={this.handleSubmit}>
					<div className="input-control text">
						<img src={icon("manager")} className="prepend-icon" />
						<input type="text" ref={(ref) => this.nameInput = ref} />
					</div>
					<input type="submit" value="Submit" />
				</form>
				{items}
			</div>
		);
	}

}

// Enable RethinkDB query subscriptions in this component
reactMixin.onClass(Turtles, ReactRethinkdb.DefaultMixin);

export default Turtles;
