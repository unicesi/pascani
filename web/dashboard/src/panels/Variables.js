import React, {Component} from 'react';
import ReactRethinkdb from 'react-rethinkdb';
import reactMixin from 'react-mixin';
import ReactPanels from '../layout/ReactPanels'
import ReactPanel from '../layout/ReactPanel'

import Variable from './Variable'

const r = ReactRethinkdb.r;

class Variables extends Component {

	static defaultProps = {
		parent: undefined,
		namespace: undefined,
	}

	static propTypes = {
		parent: React.PropTypes.instanceOf(ReactPanels),
		namespace: React.PropTypes.string,
	}

	constructor(props) {
		super(props);
	}

	observe(props, state) { // eslint-disable-line no-unused-vars
		return {
			variables: new ReactRethinkdb.QueryRequest({
				// RethinkDB query
				query: r.table("variables").filter({namespace: this.props.namespace}),
				// subscribe to realtime changefeed
				changes: true,
				// return [] while loading
				initial: [],
			}),
		};
	}

	openChart = (id, name) => {
		this.props.parent.push(
			<ReactPanel>
				<header>
					<nav className="place-right">
						<a className="close" title="Close this panel"></a>
					</nav>
					<h4>Data from <b>{name}</b></h4>
				</header>
				<section>
					<Variable parent={this.props.parent} variable={id} />
				</section>
			</ReactPanel>
		);
	}

	render() {
		const icon = (name) => {
			return require(`../svg/${name}.svg`)
		};
		const items = this.data.variables.value().map(x =>
			<div key={x.id} className="list" onClick={() => { this.openChart(x.id, x.name); }} data-id={x.id}>
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
reactMixin.onClass(Variables, ReactRethinkdb.DefaultMixin);

export default Variables;
