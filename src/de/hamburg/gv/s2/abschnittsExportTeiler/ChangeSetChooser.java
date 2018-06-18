package de.hamburg.gv.s2.abschnittsExportTeiler;

import java.awt.Container;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JTextField;

import de.hamburg.gv.s2.Abschnitt;
import de.hamburg.gv.s2.ChangeSet;
import de.hamburg.gv.s2.Station;

public class ChangeSetChooser extends JDialog implements ActionListener, ItemListener, KeyListener {
	
	private static final long serialVersionUID = 1L;
	private JComboBox<Abschnitt> absAlt;
	private JTextField absAltL, vst1, bst1, nnk2, vnk2, vst2, bst2;
	private JCheckBox gedreht;
	private AetKontrolle kontroll;

	public ChangeSetChooser(JFrame jframe, AetKontrolle kontroll) {
		super(jframe);
		this.kontroll = kontroll;

		Container dcp = this.getContentPane();
		GroupLayout layout = new GroupLayout(dcp);
		dcp.setLayout(layout);
		
		JLabel Lalt = new JLabel("Alter Abschnitt:");
		
		JLabel LAbschnitt = new JLabel("Abschnitt:");
		absAlt = new JComboBox<Abschnitt>();
		absAlt.addItemListener(this);

		//JLabel LLaenge = new JLabel("L�nge:");
		absAltL = new JTextField();
		absAltL.setEditable(false);
		
		JLabel LVST1 = new JLabel("VST:");
		vst1 = new JTextField("0");
		vst1.addKeyListener(this);

		JLabel LBST1 = new JLabel("BST:");
		bst1 = new JTextField("0");
		bst1.addKeyListener(this);

		JLabel Lneu = new JLabel("Neuer Abschnitt:");
		JLabel LVNK2 = new JLabel("VNK:");
		vnk2 = new JTextField();

		JLabel LNNK2 = new JLabel("NNK:");
		nnk2 = new JTextField();

		JLabel LVST2 = new JLabel("VST:");
		vst2 = new JTextField("0");
		vst2.addKeyListener(this);
		
		JLabel LD = new JLabel("Drehung");
		gedreht = new JCheckBox("gedreht");
		
		JLabel LBST2 = new JLabel("BST:");
		bst2 = new JTextField("0");
		//bst2.addKeyListener(this);
		//bst2.setEditable(false);

		for (Abschnitt abs : kontroll.getAbschnitte()) {
			absAlt.addItem(abs);
		}
		JButton jb = new JButton("hinzuf�gen");
		jb.addActionListener(this);
		{ // Spalten
			GroupLayout.ParallelGroup s1 = layout.createParallelGroup().addComponent(Lalt).addComponent(LAbschnitt).addComponent(LVST1).addComponent(Lneu).addComponent(LVNK2).addComponent(LVST2).addComponent(LD);
			GroupLayout.ParallelGroup s2 = layout.createParallelGroup().addComponent(absAlt).addComponent(vst1).addComponent(vnk2).addComponent(vst2).addComponent(gedreht);
			GroupLayout.ParallelGroup s3 = layout.createParallelGroup().addComponent(LBST1).addComponent(LNNK2).addComponent(LBST2);
			GroupLayout.ParallelGroup s4 = layout.createParallelGroup().addComponent(absAltL).addComponent(bst1).addComponent(nnk2).addComponent(bst2).addComponent(jb);
			GroupLayout.SequentialGroup l2r = layout.createSequentialGroup().addGroup(s1).addGroup(s2).addGroup(s3).addGroup(s4);
			layout.setHorizontalGroup(l2r);
		}
		{ // Zeilen
			GroupLayout.ParallelGroup r1 = layout.createParallelGroup().addComponent(Lalt);
			GroupLayout.ParallelGroup r2 = layout.createParallelGroup().addComponent(LAbschnitt).addComponent(absAlt).addComponent(absAltL);
			GroupLayout.ParallelGroup r3 = layout.createParallelGroup().addComponent(LVST1).addComponent(vst1).addComponent(LBST1).addComponent(bst1);
			GroupLayout.ParallelGroup r4 = layout.createParallelGroup().addComponent(Lneu);
			GroupLayout.ParallelGroup r5 = layout.createParallelGroup().addComponent(LVNK2).addComponent(vnk2).addComponent(LNNK2).addComponent(nnk2);
			GroupLayout.ParallelGroup r6 = layout.createParallelGroup().addComponent(LVST2).addComponent(vst2).addComponent(LBST2).addComponent(bst2);
			GroupLayout.ParallelGroup r7 = layout.createParallelGroup().addComponent(LD).addComponent(gedreht);
			GroupLayout.ParallelGroup r8 = layout.createParallelGroup().addComponent(jb);
			GroupLayout.SequentialGroup t2b = layout.createSequentialGroup().addGroup(r1).addGroup(r2).addGroup(r3).addGroup(r4).addGroup(r5).addGroup(r6).addGroup(r7).addGroup(r8);
			layout.setVerticalGroup(t2b);
		}

		this.pack();
		this.setVisible(true);
	}

	@Override
	public void actionPerformed(ActionEvent ae) {
		ChangeSet changeset = new ChangeSet();
		changeset.setAlt(Station.fromString((Abschnitt) absAlt.getSelectedItem(), vst1.getText(), bst1.getText()));
		changeset.setNeu(Station.fromString(vnk2.getText(), nnk2.getText(), vst2.getText(), bst2.getText()));
		changeset.setGedreht(gedreht.isSelected());
		kontroll.getChangeSetDB().addSimple(changeset);
	}
	


	@Override
	public void itemStateChanged(ItemEvent de) {
		absAltL.setText("" + (((Abschnitt) absAlt.getSelectedItem()).getLEN()));
	}

	public void textFieldChanged() {
		try {
			Station st = Station.fromString((Abschnitt) absAlt.getSelectedItem(), vst1.getText(), bst1.getText());
			vst1.setText("" + st.getVST());
			bst1.setText("" + st.getBST());
			bst2.setText("" + (Integer.parseInt(vst2.getText()) + st.getBST() - st.getVST()));
		} catch (NumberFormatException nfe) {
			log(nfe);
		}
	}
	public void log (Exception e) {
		e.printStackTrace();
	}

	@Override
	public void keyPressed(KeyEvent arg0) {
		
	}

	@Override
	public void keyReleased(KeyEvent arg0) {
		try {
			bst2.setText(""+(Integer.parseInt(vst2.getText())+(Integer.parseInt(bst1.getText())-Integer.parseInt(vst1.getText()))));
		} catch (Exception e) {
			
		}
		
	}

	@Override
	public void keyTyped(KeyEvent arg0) {

		
	}
}
