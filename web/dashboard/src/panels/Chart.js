import React, {Component} from 'react';
import ReactDOM from 'react-dom';
import Highcharts from 'highcharts';

class Chart extends Component {

	constructor(props) {
		super(props);
	}

	// When the DOM is ready, create the chart.
	componentDidMount() {
		// Extend Highcharts with modules
		if (this.props.modules) {
			this.props.modules.forEach(function (module) {
				module(Highcharts);
			});
		}
		this.chart = new Constructor(
			this.props.container, 
			this.props.options
		);
	}

	//Destroy chart before unmount.
	componentWillUnmount: function () {
		this.chart.destroy();
	}

	//Create the div which the chart will be rendered to.
	render() {
		return React.createElement('div', { id: this.props.container });
	}

}

export default Chart;
