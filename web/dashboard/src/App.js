import React, {Component} from 'react';

// Layout & Styles
import AppBar from './layout/AppBar';
import Sidebar from './layout/Sidebar';
import SidebarItem from './layout/SidebarItem';
import ReactPanels from './layout/ReactPanels';
import ReactPanel from './layout/ReactPanel';
import styles from './styles/app.css';

import Monitors from './panels/Monitors';
import Namespaces from './panels/Namespaces';
import Turtles from './panels/Turtles';
import Chart from './panels/Chart';

class App extends Component {

	constructor(props) {
		super(props);
	}

	add = (e) => {
		e.preventDefault();
		const options = {
			xAxis: {
				categories: ['Jan', 'Feb', 'Mar', 'Apr', 'May', 'Jun', 'Jul', 'Aug', 'Sep', 'Oct', 'Nov', 'Dec']
			},
			series: [{
				data: [29.9, 71.5, 106.4, 129.2, 144.0, 176.0, 135.6, 148.5, 216.4, 194.1, 295.6, 454.4]
			}]
		};
		this.refs.panels.push(
			this.panelTemplate(
				{
					width: 'wide'
				},
				"Namespace variable", 
				<Chart container="chart" options={options} />
			)
		);
	}

	openNamespaces = (e) => {
		e.preventDefault();
		const namespaces = <Namespaces parent={this.refs.panels} />;
		const panel = this.panelTemplate({}, "Namespaces", namespaces);
		this.refs.panels.push(panel);
	}

	openMonitors = (e) => {
		e.preventDefault();
		const monitors = <Monitors parent={this.refs.panels} />;
		const panel = this.panelTemplate({}, "Monitors", monitors);
		this.refs.panels.push(panel);
	}

	panelTemplate = (props, title, children) => {
		return (
			<ReactPanel {...props}>
				<header>
					<nav className="place-right">
						<a className="close" title="Close this panel"></a>
					</nav>
					<h3>{title}</h3>
				</header>
				<section>
					{children}
				</section>
			</ReactPanel>
		);
	}

	render() {
		const url = "https://github.com/unicesi/pascani";
		const icon = (name) => {
			return require(`./svg/${name}.svg`)
		}
		return (
			<span>
				<div className="page-content">
					<div id="sidebar-container" className="v100">
						<img id="logo" src={require("./img/pascani.png")} />
						<Sidebar>
							<SidebarItem text="Monitors" icon={icon("fine_print")} onClick={this.openMonitors} />
							<SidebarItem text="Namespaces" icon={icon("combo_chart")} onClick={this.openNamespaces} />
						</Sidebar>
						<Sidebar bottom={true}>
							<SidebarItem text="Issue Management" link={url} icon={icon("faq")} />
							<SidebarItem text="Documentation" link={url} icon={icon("reading")} />
						</Sidebar>
					</div>
					<div id="main-content" className="bg-white v100">
						<ReactPanels ref="panels" />
					</div>
				</div>
			</span>
		);
	}
}

export default App;
