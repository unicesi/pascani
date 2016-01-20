package org.pascani.dsl.lib.sca.explorer;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;

import org.ow2.frascati.explorer.gui.AbstractSelectionPanel;
import org.pascani.dsl.lib.util.Resumable;

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
