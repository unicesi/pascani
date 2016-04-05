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

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.ParseException;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JTextField;

import org.ow2.frascati.explorer.gui.AbstractSelectionPanel;
import org.pascani.dsl.lib.util.MonitorEventsService;

/**
 * @author Miguel Jiménez - Initial API and contribution
 */
public class MonitorEventsPanel
		extends AbstractSelectionPanel<MonitorEventsService> {

	/**
	 * The default serial version UID
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * The label for the event name
	 */
	private JLabel eventNameLabel;

	/**
	 * The text field for the event name
	 */
	private JTextField eventNameField;

	/**
	 * The label for the cron expression
	 */
	private JLabel expressionLabel;

	/**
	 * The text field for the cron expression
	 */
	private JTextField expressionField;

	/**
	 * The button to update the cron expression
	 */
	private JButton updateButton;

	public MonitorEventsPanel() {
		super();
		initialize();
		makeUI();
	}

	private void initialize() {
		this.eventNameLabel = new JLabel("Event name:");
		this.eventNameField = new JTextField(20);

		this.expressionLabel = new JLabel("Cron expression:");
		this.expressionField = new JTextField(20);

		this.updateButton = new JButton("Update");
		this.updateButton.addActionListener(new ActionListener() {
			@Override public void actionPerformed(ActionEvent event) {
				try {
					selected.updateCronExpression(eventNameField.getText(),
							expressionField.getText());
					eventNameField.setText("");
					expressionField.setText("");
				} catch (ParseException e) {
					e.printStackTrace();
				}
			}
		});
	}

	private void makeUI() {
		setLayout(new BorderLayout());
		add(this.eventNameLabel, BorderLayout.CENTER);
		add(this.eventNameField, BorderLayout.CENTER);
		add(this.expressionLabel, BorderLayout.CENTER);
		add(this.expressionField, BorderLayout.CENTER);
		add(this.updateButton, BorderLayout.SOUTH);
	}

}
