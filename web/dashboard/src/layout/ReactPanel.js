import React, { Component } from 'react';
import ReactDOM from 'react-dom'

class ReactPanel extends Component {

	static defaultProps = {
		active: false,
		floating: false,
		width: 'regular',
		id: undefined,
		onBeforeOpen: (panel) => {},
		onBeforeClose: (panel) => {},
		claimActiveState: (panel) => {},
		closePanel: (panel) => {},
	}

	static propTypes = {
		active: React.PropTypes.bool,
		floating: React.PropTypes.bool,
		width: React.PropTypes.oneOfType([
			React.PropTypes.string,
			React.PropTypes.number,
		]),
		id: React.PropTypes.string,
		onBeforeOpen: React.PropTypes.func,
		onBeforeClose: React.PropTypes.func,
		claimActiveState: React.PropTypes.func,
		closePanel: React.PropTypes.func,
	}

	state = {
		expanded: false,
	}

	constructor(props) {
		super(props);
	}

	componentWillMount() {
		this.props.onBeforeOpen(this);
	}

	componentWillUnmount() {
		this.props.onBeforeClose(this);
	}

	handleClose = (e) => {
		e.preventDefault();
		this.props.closePanel(this);
	}

	handleExpand = (e) => {
		e.preventDefault();
		this.setState({ expanded: true });
		this.props.claimActiveState(this);
	}

	handleCollapse = (e) => {
		e.preventDefault();
		this.setState({ expanded: false });
		this.props.claimActiveState(this);
	}

	render() {
		const buttons = this.bindEventHandlers();
		const classes = `${this.props.width}` 
			+ (this.props.floating ? " floating" : "")
			+ (this.props.active ? " active" : "")
			+ (this.state.expanded ? " expanded" : "");
		return (
			<section id={this.props.id} className={"react-panel " + classes}>
				{buttons}
				{this.props.children}
			</section>
		);
	}

	bindEventHandlers = () => {
		const buttons = [];
		if (this.props.closeBtn) {
			buttons.push(React.cloneElement(this.props.closeBtn, {
				key: 'closeBtn',
				onClick: this.handleClose
			}));
		}
		if (this.props.expandBtn) {
			buttons.push(React.cloneElement(this.props.expandBtn, {
				key: 'expandBtn',
				onClick: this.handleExpand
			}));
		}
		if (this.props.collapseBtn) {
			buttons.push(React.cloneElement(this.props.collapseBtn, {
				key: 'collapseBtn',
				onClick: this.handleCollapse
			}));
		}
		return buttons;
	}

}

export default ReactPanel;
