import React, {Component} from 'react';
import ReactRethinkdb from 'react-rethinkdb';
import reactMixin from 'react-mixin';
import ReactPanels from '../layout/ReactPanels'
import ReactPanel from '../layout/ReactPanel'

import Variables from './Variables'

const r = ReactRethinkdb.r;

class Namespaces extends Component {

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
			namespaces: new ReactRethinkdb.QueryRequest({
				// RethinkDB query
				query: r.table('namespaces'),
				// subscribe to realtime changefeed
				changes: true,
				// return [] while loading
				initial: [],
			}),
		};
	}

	openVariables = (id, name) => {
		this.props.parent.push(
			<ReactPanel width="wide">
				<header>
					<nav className="place-right">
						<a className="close" title="Close this panel"></a>
					</nav>
					<h4>Variables from <b>{name}</b></h4>
				</header>
				<section>
					<Variables parent={this.props.parent} namespace={id} />
				</section>
			</ReactPanel>
		);
	}

	render() {
		const icon = (name) => {
			return require(`../svg/${name}.svg`)
		};
		const items = this.data.namespaces.value().map(x =>
			<div key={x.id} className="list" onClick={() => { this.openVariables(x.id, x.name); }} data-id={x.id}>
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
reactMixin.onClass(Namespaces, ReactRethinkdb.DefaultMixin);

export default Namespaces;
