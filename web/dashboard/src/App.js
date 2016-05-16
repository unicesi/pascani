import React, {Component} from 'react';

// Layout & Styles
import AppBar from './layout/AppBar'
import Sidebar from './layout/Sidebar'
import SidebarItem from './layout/SidebarItem'
import ReactPanels from './layout/ReactPanels'
import ReactPanel from './layout/ReactPanel'
import styles from './styles/app.css';

import Turtles from './panels/Turtles';

class App extends Component {

	constructor(props) {
		super(props);
	}

	add = (e) => {
		e.preventDefault();
		const props = {
			width: 'regular',
			closeBtn: <a href="#">Close</a>,
			expandBtn: <a href="#">Expand</a>,
			collapseBtn: <a href="#">Collapse</a>,
		};
		this.refs.panels.push(
			<ReactPanel {...props} />
		);
	}

	render() {
		const icon = (name) => {
			return require(`./svg/${name}.svg`)
		}
		return (
			<span>
				<AppBar />
				<div className="page-content">
					<div id="sidebar-container" className="v100">
						<Sidebar>
							<SidebarItem text="Monitors"
								icon={icon("fine_print")}
								onClick={this.add} />
							<SidebarItem text="Namespaces"
								icon={icon("combo_chart")}
								onClick={this.add} />
						</Sidebar>
						<Sidebar bottom={true}>
							<SidebarItem text="Issue Management"
								link="https://github.com/unicesi/pascani"
								icon={icon("faq")} />
							<SidebarItem text="Documentation"
								link="https://github.com/unicesi/pascani"
								icon={icon("reading")} />
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
