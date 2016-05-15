import React, {Component} from 'react';

export default class SidebarItem extends Component {

	static defaultProps = {
		icon: undefined,
		link: '#',
		text: '',
	}

	static PropTypes = {
		icon: React.PropTypes.string,
		link: React.PropTypes.string,
		text: React.PropTypes.string
	}

	state = {
		active: false,
	}
	
	constructor(props) {
		super(props);
	}

	render() {
		const classes = this.state.active ? "active" : "";
		const icon = !this.props.icon ? undefined : (
			<span className="icon">
				<img src={this.props.icon} />
			</span>
		);
		return (
			<li className={classes}>
				<a href={this.props.link}>
					{icon}
					<span className="title">{this.props.text}</span>
				</a>
			</li>
		);
	}

}