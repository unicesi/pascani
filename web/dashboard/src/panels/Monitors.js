import React, {Component} from 'react';
import ReactRethinkdb from 'react-rethinkdb';
import reactMixin from 'react-mixin';
import ReactPanels from '../layout/ReactPanels'

const r = ReactRethinkdb.r;

class Monitors extends Component {

	static defaultProps = {
		parent: undefined,
	}

	static propTypes = {
		parent: React.PropTypes.instanceOf(ReactPanels),
	}

	constructor(props) {
		super(props);
	}

	observe(props, state) { // eslint-disable-line no-unused-vars
		return {
			monitors: new ReactRethinkdb.QueryRequest({
				// RethinkDB query
				query: r.table('monitors'),
				// subscribe to realtime changefeed
				changes: true,
				// return [] while loading
				initial: [],
			}),
		};
	}

	add = (id) => {
		alert(id);
	}

	render() {
		const icon = (name) => {
			return require(`../svg/${name}.svg`)
		};
		const items = this.data.monitors.value().map(x =>
			<div key={x.id} className="list" onClick={() => { this.add(x.id); }} data-id={x.id}>
				<img className="list-icon" src={icon("package")} />
				<span className="list-title">{x.name}</span>
			</div>
		);
		return (
			<div className="listview">
				{items}
			</div>
		);
	}

}

// Enable RethinkDB query subscriptions in this component
reactMixin.onClass(Monitors, ReactRethinkdb.DefaultMixin);

export default Monitors;
