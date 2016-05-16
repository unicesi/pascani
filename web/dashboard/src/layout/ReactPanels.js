import React, { Component } from 'react';
import ReactDOM from 'react-dom'
import ReactPanel from './ReactPanel';
import update from 'react-addons-update';
import isArray from 'isarray';
import styles from '../styles/react-panels.css';

class ReactPanels extends Component {

	state = {
		activePanel: undefined,
		panels: [],
	}

	constructor(props) {
		super(props);
	}

	componentDidMount() {
		if (this.props.children) {
			if(isArray(this.props.children)) {
				this.props.children.map(this.push);
			} else {
				this.push(this.props.children);
			}
		}
	}

	componentDidUpdate(prevProps, prevState) {
		this.ensureActivePanelVisible();
	}

	ensureActivePanelVisible = () => {
		if (this.state.activePanel) {
			const domNode = ReactDOM.findDOMNode(this.refs["panel-active"]);
			domNode.scrollIntoView({
				block: "end",
				behavior: "smooth",
			});
		}
	}

	renderPanel = (panel, i) => {
		if (!panel)
			return;
		const active = this.state.activePanel.props.id === panel.props.id;
		const key = active ? "active" : panel.props.id;
		return React.cloneElement(panel, {
			key: key,
			ref: `panel-${key}`,
			active: active,
			closePanel: this.remove,
			claimActiveState: this.updateActivePanel
		});
	}

	updateActivePanel = (panel) => {
		this.setState({ activePanel: panel });
	}

	push = (panel) => {
		const _panel = panel.props.id ? panel : React.cloneElement(panel, {
			id: this.randomId(6)
		});
		this.setState({
			activePanel: _panel,
			panels: this.state.panels.concat([_panel])
		});
	}

	remove = (panel) => {
		const index = this.state.panels.map((p) => { 
			return p.props.id 
		}).indexOf(panel.props.id);
		if (index > -1) {
			this.setState({
				panels: update(this.state.panels, {$splice: [[index, 1]]})
			});
			if (panel.props.active) {
				const index2 = index > 0 ? index - 1 : index + 1;
				this.setState({ activePanel: this.state.panels[index2] });
			}
		}
	}

	render() {
		return (
			<div className="react-panels">
				{this.state.panels.map(this.renderPanel)}
			</div>
		);
	}

	// Source: http://stackoverflow.com/a/6861381/738968
	randomId = (length) => {
		const chars = '0123456789ABCDEFGHIJKLMNOPQRSTUVWXTZabcdefghiklmnopqrstuvwxyz'.split('');
		let str = '';
		if (!length) {
			length = Math.floor(Math.random() * chars.length);
		}
		for (var i = 0; i < length; i++) {
			str += chars[Math.floor(Math.random() * chars.length)];
		}
		return str;
	}
}

export default ReactPanels;
