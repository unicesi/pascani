/*
 * Copyright © 2015 Universidad Icesi
 * 
 * This file is part of the Pascani library.
 * 
 * The Pascani library is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or (at your
 * option) any later version.
 * 
 * The Pascani library is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License
 * for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with The Pascani library. If not, see <http://www.gnu.org/licenses/>.
 */
package org.pascani.dsl.lib.sca.explorer;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;

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

	public ResumablePanel() {
		super();
		initialize();
		makeUI();
	}

	private void initialize() {
		this.pause = new JButton("Pause");
		this.pause.addActionListener(new ActionListener() {
			@Override public void actionPerformed(ActionEvent event) {
				pause.setEnabled(false);
				resume.setEnabled(true);
				selected.pause();
			}
		});

		this.resume = new JButton("Resume");
		this.resume.addActionListener(new ActionListener() {
			@Override public void actionPerformed(ActionEvent event) {
				resume.setEnabled(false);
				pause.setEnabled(true);
				selected.resume();
			}
		});

		if (selected != null && selected.isPaused())
			this.pause.setEnabled(false);
		else
			this.resume.setEnabled(false);
	}

	private void makeUI() {
		add(this.pause);
		add(this.resume);
	}

}
