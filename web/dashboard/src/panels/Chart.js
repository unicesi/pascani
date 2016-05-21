import React, {Component} from 'react';
import ReactDOM from 'react-dom';
import Highcharts from 'highcharts/highstock';
import styles from '../styles/charts.css';

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
		this.chart = new Highcharts[this.props.type || "Chart"](
			this.props.container,
			this.props.options
		);
	}

	//Destroy chart before unmount.
	componentWillUnmount() {
		this.chart.destroy();
	}

	resize = (width) => {
		console.log(this.chart, width)
		width = width || $(this.props.container).parent().width();
		const chart = $(this.props.container).highcharts();
		chart.setSize(width, chart.chartHeight, false);
	}

	//Create the div which the chart will be rendered to.
	render() {
		return (
			<div id={this.props.container} className="chart"></div>
		);
	}

}

export default Chart;
