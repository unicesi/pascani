import React, {Component} from 'react';
import ReactRethinkdb from 'react-rethinkdb';
import reactMixin from 'react-mixin';
import ReactPanels from '../layout/ReactPanels'

import Chart from './Chart'

const r = ReactRethinkdb.r;

class Variable extends Component {

	static defaultProps = {
		parent: undefined,
		variable: undefined,
	}

	static propTypes = {
		parent: React.PropTypes.instanceOf(ReactPanels),
		variable: React.PropTypes.string,
	}

	constructor(props) {
		super(props);
	}

	observe(props, state) { // eslint-disable-line no-unused-vars
		return {
			values: new ReactRethinkdb.QueryRequest({
				// RethinkDB query
				query: r.table("values")
						.eqJoin("variable", r.table("variables"))
						.map(function(join){return join("left")})
						.filter({ variable: this.props.variable })
						.orderBy("timestamp"),
				// subscribe to realtime changefeed
				changes: false,
				// return [] while loading
				initial: [],
			}),
		};
	}

	render() {
		const data = this.data.values.value();
		const options = {
			xAxis: {
				title: {
					enabled: true,
					text: 'Time'
				},
				type: 'datetime'
			},
			series: [{
				data: data.map(x => [x.timestamp, x.value])
			}]
		};
		return (
			<Chart container={`chart-${this.props.variable}`} options={options} />
		);
	}

}

// Enable RethinkDB query subscriptions in this component
reactMixin.onClass(Variable, ReactRethinkdb.DefaultMixin);

export default Variable;
