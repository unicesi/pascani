import React, {Component} from 'react';

// Layout & Styles
import AppBar from './layout/AppBar'
import Sidebar from './layout/Sidebar'
import SidebarItem from './layout/SidebarItem'
import ReactPanels from './layout/ReactPanels'
import ReactPanel from './layout/ReactPanel'
import styles from './styles/app.css';

import Turtles from './panels/Turtles';
import Chart from './panels/Chart';

class App extends Component {

	constructor(props) {
		super(props);
	}

	add = (e) => {
		e.preventDefault();
		const props = {
			width: 'regular',
		};
		const options = {
			xAxis: {
				categories: ['Jan', 'Feb', 'Mar', 'Apr', 'May', 'Jun', 'Jul', 'Aug', 'Sep', 'Oct', 'Nov', 'Dec']
			},
			series: [{
				data: [29.9, 71.5, 106.4, 129.2, 144.0, 176.0, 135.6, 148.5, 216.4, 194.1, 295.6, 454.4]
			}]
		};
		this.refs.panels.push(
			<ReactPanel {...props}>
				<header>
					<nav>
						<a className="close"></a>
						<a className="expand"></a>
						<a className="collapse"></a>
					</nav>
				</header>
				<div>
					<Chart container="chart" options={options} />
				</div>
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
				<AppBar />
				<div className="page-content">
					<div id="sidebar-container" className="v100">
						<Sidebar>
							<SidebarItem text="Monitors" icon={icon("fine_print")} onClick={this.add} />
							<SidebarItem text="Namespaces" icon={icon("combo_chart")} onClick={this.add} />
						</Sidebar>
						<Sidebar bottom={true}>
							<SidebarItem text="Issue Management" link={url} icon={icon("faq")} />
							<SidebarItem text="Documentation" link={url} icon={icon("reading")} />
						</Sidebar>
					</div>
					<div id="main-content" className="bg-white v100">
						<ReactPanels ref="panels">
							<ReactPanel width="thin">
								<Turtles add={this.add} />
							</ReactPanel>
						</ReactPanels>
					</div>
				</div>
			</span>
		);
	}
}

export default App;
