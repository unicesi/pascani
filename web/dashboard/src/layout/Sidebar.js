import React, {Component} from 'react';

export default class Sidebar extends Component {

	static defaultProps = {
		bottom: false,
	}

	static PropTypes = {
		bottom: React.PropTypes.bool,
	}

	constructor(props) {
		super(props);
	}

	render() {
		const classes = "sidebar" + (this.props.bottom ? " bottom" : "");
		return (
			<ul className={classes}>
				{this.props.children}
			</ul>
		);
	}

}