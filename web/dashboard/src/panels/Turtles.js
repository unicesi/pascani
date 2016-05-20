import React, {Component} from 'react';
import ReactRethinkdb from 'react-rethinkdb';
import reactMixin from 'react-mixin';

const r = ReactRethinkdb.r;

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
			<div key={x.id} className="list" onClick={this.props.add}>
				<img className="list-icon" src={icon("package")} />
				<span className="list-title">{x.name}</span>
			</div>
		);
		const button = {
			margin: 0
		};
		return (
			<div className="grid">
				<form className="row padding10 no-padding-top no-padding-left no-padding-right" onSubmit={this.handleSubmit}>
					<div className="cell input-control text">
						<img src={icon("manager")} className="prepend-icon" />
						<input type="text" ref={(ref) => this.nameInput = ref} />
					</div>
				</form>
				<div className="listview">
					{items}
				</div>
			</div>
		);
	}

}

// Enable RethinkDB query subscriptions in this component
reactMixin.onClass(Turtles, ReactRethinkdb.DefaultMixin);

export default Turtles;
