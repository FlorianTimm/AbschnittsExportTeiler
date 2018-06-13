package de.hamburg.gv.s2.abschnittsExportTeiler;

import java.awt.Container;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.ArrayList;

import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JTextField;

public class TransAdder extends JDialog implements ActionListener, ItemListener, KeyListener {
	
	private static final long serialVersionUID = 1L;
	Station[] station;
	AbschnittsExportTeilerGUI jframe;
	JComboBox<Abschnitt> absAlt;
	JTextField absAltL, vst1, bst1, nnk2, vnk2, vst2, bst2;
	JCheckBox gedreht;

	public TransAdder(AbschnittsExportTeilerGUI jframe, ArrayList<Abschnitt> abschn) {
		super(jframe);
		this.jframe = jframe;
		station = new Station[2];
		Container dcp = this.getContentPane();
		GroupLayout layout = new GroupLayout(dcp);
		dcp.setLayout(layout);
		
		JLabel Lalt = new JLabel("Alter Abschnitt:");
		
		JLabel LAbschnitt = new JLabel("Abschnitt:");
		absAlt = new JComboBox<Abschnitt>();
		absAlt.addItemListener(this);

		//JLabel LLaenge = new JLabel("Länge:");
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

		for (Abschnitt abs : abschn) {
			absAlt.addItem(abs);
		}
		JButton jb = new JButton("hinzufügen");
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

	public Station[] getStation() {
		return station;
	}

	@Override
	public void actionPerformed(ActionEvent arg0) {
		// TODO Auto-generated method stub
		station[0] = pruefeEingabe((Abschnitt) absAlt.getSelectedItem(), vst1.getText(), bst1.getText());
		station[1] = pruefeEingabe(vnk2.getText(), nnk2.getText(), vst2.getText(), bst2.getText(), gedreht.isSelected());
		jframe.addTransBack(station);
	}
	
	public static Station pruefeEingabe(String vnk, String nnk, String vst, String bst) throws NumberFormatException {
		return pruefeEingabe(vnk, nnk, vst, bst, false);
	}

	public static Station pruefeEingabe(String vnk, String nnk, String vst, String bst, boolean gedreht) throws NumberFormatException {
		Abschnitt abschnitt = new Abschnitt();
		abschnitt.setVNK(vnk);
		abschnitt.setNNK(nnk);
		return pruefeEingabe(abschnitt, vst, bst, gedreht);
	}

	public static Station pruefeEingabe(Abschnitt abschnitt, String vst, String bst) throws NumberFormatException {
		return pruefeEingabe(abschnitt, vst, bst, false);
	}
	
	public static Station pruefeEingabe(Abschnitt abschnitt, String vst, String bst, boolean gedreht) throws NumberFormatException {
		Station station = new Station(abschnitt);
		try {
			int newVst = Integer.parseInt(vst);
			station.setVST(newVst);
		} catch (Exception e) {
			
		}
		try {
			int newBst = Integer.parseInt(bst);
			station.setBST(newBst);
		} catch (Exception e) {
			
		}
		station.setDrehung(gedreht);
		return station;
	}

	@Override
	public void itemStateChanged(ItemEvent de) {
		// TODO Auto-generated method stub
		absAltL.setText("" + (((Abschnitt) absAlt.getSelectedItem()).getLEN()));
	}

	public void textFieldChanged() {
		// TODO Auto-generated method stub
		try {
			Station st = pruefeEingabe((Abschnitt) absAlt.getSelectedItem(), vst1.getText(), bst1.getText());
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
		// TODO Auto-generated method stub
		
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
		// TODO Auto-generated method stub
		
	}
}
