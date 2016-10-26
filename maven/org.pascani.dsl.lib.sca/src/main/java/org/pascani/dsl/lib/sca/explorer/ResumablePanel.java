/*
 * Copyright © 2015 Universidad Icesi
 * 
 * This file is part of the Pascani project.
 * 
 * The Pascani project is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or (at your
 * option) any later version.
 * 
 * The Pascani project is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License
 * for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with The Pascani project. If not, see <http://www.gnu.org/licenses/>.
 */
package org.pascani.dsl.lib.sca.explorer;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JTextField;

import org.ow2.frascati.explorer.gui.AbstractSelectionPanel;
import org.pascani.dsl.lib.util.Resumable;

/**
 * @author Miguel Jiménez - Initial API and contribution
 */
public class ResumablePanel extends AbstractSelectionPanel<Resumable> {

	/**
	 * The default serial version UID
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * The button to pause the {@link Resumable}
	 */
	private JButton pause;

	/**
	 * The button to resume the {@link Resumable}
	 */
	private JButton resume;
	
	/**
	 * The text field showing the current status
	 */
	private JTextField status;
	
	/**
	 * The button to update the current status
	 */
	private JButton refresh;
	
	/**
	 * The text shown when the {@link Resumable} is paused
	 */
	private final String PAUSED = "Paused";
	
	/**
	 * The text shown when the {@link Resumable} is not paused
	 */
	private final String NOT_PAUSED = "Not Paused";

	public ResumablePanel() {
		super();
		initialize();
		makeUI();
		configure();
	}

	private void initialize() {
		this.pause = new JButton("Pause");
		this.pause.addActionListener(new ActionListener() {
			@Override public void actionPerformed(ActionEvent event) {
				pause.setEnabled(false);
				resume.setEnabled(true);
				status.setText(PAUSED);
				selected.pause();
			}
		});

		this.resume = new JButton("Resume");
		this.resume.addActionListener(new ActionListener() {
			@Override public void actionPerformed(ActionEvent event) {
				resume.setEnabled(false);
				pause.setEnabled(true);
				status.setText(NOT_PAUSED);
				selected.unpause();
			}
		});
		
		this.status = new JTextField(6);
		this.status.setEditable(false);
		
		this.refresh = new JButton("Refresh");
		this.refresh.addActionListener(new ActionListener() {
			@Override public void actionPerformed(ActionEvent e) {
				update();
			}
		});
	}

	private void makeUI() {
		add(this.pause);
		add(this.resume);
		add(this.status);
		add(this.refresh);
	}
	
	private void configure() {
		new Thread() {
			@Override public void run() {
				try {
					Thread.sleep(1000);
				} catch (Exception e) {
					e.printStackTrace();
				}
				update();
			}
		}.start();
	}
	
	private void update() {
		if (this.selected == null)
			this.status.setText("NULL");
		
		if (this.selected.isPaused()) {
			this.pause.setEnabled(false);
			this.status.setText(this.PAUSED);
		} else {
			this.resume.setEnabled(false);
			this.status.setText(this.NOT_PAUSED);
		}
	}

}
