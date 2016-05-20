import React, { Component } from 'react';
import ReactDOM from 'react-dom';

require("../img/close.png");
require("../img/expand.png");
require("../img/collapse.png");

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
		width: undefined,
	}

	constructor(props) {
		super(props);
	}

	componentWillMount() {
		this.props.onBeforeOpen(this);
	}

	onMouseDown = (e) => {
		e.stopPropagation();
		e.preventDefault();
		// only left mouse button
		if (e.button !== 0) return;
		document.addEventListener('mousemove', this.onMouseMove);
		document.addEventListener('mouseup', this.onMouseUp);
	}

	onMouseMove = (e) => {
		e.stopPropagation();
		e.preventDefault();
		const offset = $(this.refs.container).offset();
		this.setState({ width: e.pageX - offset.left + 1 });
	}

	onMouseUp = (e) => {
		e.stopPropagation();
		e.preventDefault();
		document.removeEventListener('mousemove', this.onMouseMove);
	}

	componentWillUnmount() {
		this.props.onBeforeClose(this);
	}

	close = () => {
		this.props.closePanel(this);
		this.setState({ expanded: false });
	}

	expand = () => {
		this.setState({ expanded: true });
		this.props.claimActiveState(this);
	}

	collapse = () => {
		this.setState({ expanded: false });
		this.props.claimActiveState(this);
	}

	handlePanelClick = (e) => {
		if (e.target.className.indexOf("close") > -1) {
			this.close();
		} else if (e.target.className.indexOf("expand") > -1) {
			this.expand();
		} else if (e.target.className.indexOf("collapse") > -1) {
			this.collapse();
		}
	}

	render() {
		const classes = (!this.state.width ? `${this.props.width}` : '')
			+ (this.props.floating ? " floating" : "")
			+ (this.props.active ? " active" : "")
			+ (this.state.expanded ? " expanded" : "");
		const style = {};
		if (this.state.width)
			style.width = `${this.state.width}px`;
		return (
			<section id={this.props.id} className={"react-panel " + classes}
				onClick={this.handlePanelClick} style={style} ref="container">
				{this.props.children}
				<div className="dragbar" onMouseDown={this.onMouseDown}></div>
			</section>
		);
	}

}

export default ReactPanel;
