import React, {Component} from 'react';

// Layout & Styles
import AppBar from './layout/AppBar'
import Sidebar from './layout/Sidebar'
import SidebarItem from './layout/SidebarItem'
import ReactPanels from './layout/ReactPanels'
import ReactPanel from './layout/ReactPanel'
import styles from './styles/App.css';

import Turtles from './panels/Turtles';

class App extends Component {

	constructor(props) {
		super(props);
	}

	render() {
		const icon = (name) => {
			return require(`./svg/${name}.svg`)
		}
		return (
			<span>
				<AppBar>
					<a href="">Ejemplo 1</a>
				</AppBar>
				<div className="page-content">
					<div className="flex-grid no-responsive-future v100" styles="height: 100%;">
						<div className="row cells12 v100">
							
							<div id="sidebar-container" className="cell">
								<Sidebar>
									<SidebarItem active={true} text="Monitors"
										icon={icon("fine_print")} />
									<SidebarItem text="Namespaces"
										icon={icon("combo_chart")} />
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

							<div className="cell colspan2 bg-white">
								<ReactPanels>
									<ReactPanel>
										<Turtles />
									</ReactPanel>
								</ReactPanels>
							</div>

						</div>
					</div>
				</div>
			</span>
		);
	}
}

export default App;
