import React, {Component} from 'react';

export default class AppBar extends Component {

	constructor(props) {
		super(props);
	}

	render() {
		return (
			<div className="app-bar fixed-top navy" data-role="appbar">
				<a href="" className="app-bar-element">
					<span id="toggle-tiles-dropdown" className="mif-apps mif-2x"></span>
				</a>
				<a href="" className="app-bar-element brandname">PASCANI</a>
				<span className="app-bar-divider"></span>
				{this.props.children}
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