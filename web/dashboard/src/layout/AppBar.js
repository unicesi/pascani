import React, {Component} from 'react';
import isArray from 'isarray'

export default class AppBar extends Component {

	constructor(props) {
		super(props);
	}

	renderChild = (child, i) => {
		return React.cloneElement(child, {
			key: i,
			className: 'app-bar-element',
		});
	}

	render() {
		const children = !this.props.children ?
			undefined :
			isArray(this.props.children) ? 
				this.props.children.map(this.renderChild) : 
				this.renderChild(this.props.children, 0);
		return (
			<div className="app-bar fixed-top navy" data-role="appbar">
				<a href="" className="app-bar-element">
					<span id="toggle-tiles-dropdown" className="mif-apps mif-2x"></span>
				</a>
				<a href="" className="app-bar-element brandname">PASCANI</a>
				<span className="app-bar-divider"></span>
				
				{children}

				<div className="app-bar-element place-right">
					<span className="dropdown-toggle">About</span>
					<ul className="d-menu place-right" data-role="dropdown">
						<li><a href="https://github.com/unicesi/pascani">PASCANI</a></li>
						<li><a href="https://github.com/unicesi/amelia">Amelia</a></li>
					</ul>
				</div>
			</div>
		);
	}

}