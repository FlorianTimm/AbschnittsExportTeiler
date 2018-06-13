package de.hamburg.gv.s2.abschnittsExportTeiler;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.AffineTransform;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.regex.Pattern;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import com.linuxense.javadbf.DBFException;
import com.linuxense.javadbf.DBFReader;

/**
 * @author Florian Timm
 *
 */
public class AbschnittsExportTeilerGUI extends JFrame implements ActionListener {
	private static final long serialVersionUID = 1L;

	File dbabschnF = null;
	File[] dateien = null, dateienS = null;
	JTextArea jta;
	JTable transTable;
	JTextField jtf;
	JButton trans, export, loeschen, importT, exportT;
	ArrayList<Abschnitt> abschn;
	ArrayList<Station[]> zuAendern;
	TransTabelle transTableModell;

	public static void main(String[] args) {
		new AbschnittsExportTeilerGUI();
	}

	public AbschnittsExportTeilerGUI() {
		super("AbschnittsExportTeiler");
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (Exception e) {
		}
		this.setPreferredSize(new Dimension(600, 400));
		this.setLocationRelativeTo(null);

		abschn = new ArrayList<Abschnitt>();
		zuAendern = new ArrayList<Station[]>();

		Container cp = this.getContentPane();
		cp.setLayout(new GridLayout(2, 1));

		JPanel oben = new JPanel();
		oben.setLayout(new BorderLayout());
		cp.add(oben);
		JPanel mitte = new JPanel();
		mitte.setLayout(new BorderLayout());
		mitte.setSize(20, 20);
		oben.add(mitte, BorderLayout.SOUTH);
		JPanel unten = new JPanel();
		unten.setLayout(new BorderLayout());
		cp.add(unten);

		JButton oeffnen = new JButton("Öffnen");
		oeffnen.addActionListener(this);
		oben.add(oeffnen, BorderLayout.NORTH);

		jta = new JTextArea();
		jta.setEditable(false);
		JScrollPane jsp = new JScrollPane(jta);
		oben.add(jsp, BorderLayout.CENTER);

		jtf = new JTextField("");
		mitte.add(jtf, BorderLayout.CENTER);

		JButton speichern = new JButton("Export-Pfad");
		speichern.setActionCommand("speichern");
		speichern.addActionListener(this);
		mitte.add(speichern, BorderLayout.EAST);

		trans = new JButton("Umformung hinzufügen");
		trans.setActionCommand("trans");
		trans.addActionListener(this);
		trans.setEnabled(false);
		unten.add(trans, BorderLayout.NORTH);

		transTableModell = new TransTabelle(zuAendern);
		transTable = new JTable(transTableModell);
		JScrollPane jsp2 = new JScrollPane(transTable);
		unten.add(jsp2, BorderLayout.CENTER);

		loeschen = new JButton("löschen");
		loeschen.addActionListener(this);
		Font lvButtonFont = loeschen.getFont();
		AffineTransform at = new AffineTransform();
		at.rotate(-1.57d);
		loeschen.setFont(lvButtonFont.deriveFont(at));
		loeschen.setVerticalAlignment(SwingConstants.BOTTOM);
		loeschen.setEnabled(false);
		unten.add(loeschen, BorderLayout.EAST);

		JPanel imexportT = new JPanel();
		imexportT.setLayout(new GridLayout(2, 1));
		unten.add(imexportT, BorderLayout.WEST);

		importT = new JButton("import");
		importT.setActionCommand("importT");
		importT.addActionListener(this);
		importT.setFont(lvButtonFont.deriveFont(at));
		importT.setVerticalAlignment(SwingConstants.BOTTOM);
		importT.setEnabled(false);
		imexportT.add(importT);

		exportT = new JButton("export");
		exportT.setActionCommand("exportT");
		exportT.addActionListener(this);
		exportT.setFont(lvButtonFont.deriveFont(at));
		exportT.setVerticalAlignment(SwingConstants.BOTTOM);
		exportT.setEnabled(false);
		imexportT.add(exportT);

		export = new JButton("Export");
		export.setActionCommand("export");
		export.addActionListener(this);
		export.setEnabled(false);
		unten.add(export, BorderLayout.SOUTH);

		this.setDefaultCloseOperation(EXIT_ON_CLOSE);
		this.pack();
		this.setVisible(true);

	}

	/**
	 * Wählt und überprüft den Import-Ordner
	 * 
	 * @return Erfolgreich?
	 */
	public boolean importFolder() {

		JFileChooser jfc = new JFileChooser("D:\\TTSIB\\ESS");
		jfc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		jfc.setAcceptAllFileFilterUsed(false);
		int ok = jfc.showOpenDialog(this);

		if (ok == JFileChooser.APPROVE_OPTION) {
			jta.setText("");

			dateien = jfc.getSelectedFile().listFiles(new FilenameFilter() {
				@Override
				public boolean accept(File dir, String name) {
					return Pattern.matches("(DB)\\d{6}\\.((DBF)|(dbf))", name);
				}
			});

			dateienS = jfc.getSelectedFile().listFiles(new FilenameFilter() {
				@Override
				public boolean accept(File dir, String name) {
					return Pattern.matches("(DB)((OBJDEF)|(OBJEKT))\\.((DBF)|(dbf))", name);
				}
			});

			dbabschnF = new File(jfc.getSelectedFile() + "\\DBABSCHN.DBF");
			if (dbabschnF.exists()) {
				abschn.clear();
				try {
					DBFReader dbabschn = new DBFReader(new FileInputStream(dbabschnF));

					Object[] spalten;

					while ((spalten = dbabschn.nextRecord()) != null) {
						if (spalten.length >= 3 && spalten[0] != null && spalten[1] != null && spalten[2] != null) {
							BigDecimal bd = (BigDecimal) spalten[2];
							Abschnitt abs = new Abschnitt((String) spalten[0], (String) spalten[1],
									(int) bd.intValueExact());
							abschn.add(abs);
							// System.out.println(abs.toString());
						}
					}
					Collections.sort(abschn);
					trans.setEnabled(true);
					export.setEnabled(true);
					importT.setEnabled(true);
					exportT.setEnabled(true);
					dbabschn.close();

				} catch (DBFException e1) {
					e1.printStackTrace();
				} catch (FileNotFoundException e1) {
					e1.printStackTrace();
				}

				for (int i = 0; i < dateien.length; i++) {

					File file = dateien[i];

					if (i > 0) {
						jta.append("\n");
					}
					jta.append(file.getName());
					System.out.println(file.getName());

				}
				for (File datei : dateienS) {
					System.out.println(datei.getName());
					jta.append("\n" + datei.getName());
				}
			} else {
				JOptionPane.showMessageDialog(this,
						"Der Ordner enthält keine DBABSCHNITT, die Import-Daten sind ungültig!");
				return false;
			}
		}

		return true;
	}

	/**
	 * Wählt den Export-Ordner
	 * 
	 * @return erfolgreich?
	 */
	public boolean exportFolder() {
		JFileChooser jfc = new JFileChooser("D:\\TTSIB\\ESS");
		jfc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		jfc.setAcceptAllFileFilterUsed(false);
		int ok = jfc.showOpenDialog(this);

		if (ok == JFileChooser.APPROVE_OPTION) {
			jtf.setText(jfc.getSelectedFile().getAbsolutePath());
			return true;
		}
		return false;
	}

	public void addTrans() {
		new TransAdder(this, abschn);
	}

	public void importTrans() {
		JFileChooser jfc = new JFileChooser();
		jfc.setFileSelectionMode(JFileChooser.FILES_ONLY);
		int ok = jfc.showOpenDialog(this);

		if (ok == JFileChooser.APPROVE_OPTION) {
			jfc.getSelectedFile();
			System.out.println(jfc.getSelectedFile());
			try {
				BufferedReader in = new BufferedReader(new FileReader(jfc.getSelectedFile()));

				String line = "";
				while ((line = in.readLine()) != null && line.length() >= 15) {
					System.out.println(line);
					String[] input = line.split("\t");
					Station[] st = new Station[2];
					st[0] = TransAdder.pruefeEingabe(input[0], input[1], input[2], input[3]);
					if (input.length == 8) {
					
					st[1] = TransAdder.pruefeEingabe(input[4], input[5], input[6], input[7]);
					} else if (input.length == 9) {
						st[1] = TransAdder.pruefeEingabe(input[4], input[5], input[6], input[7], Boolean.valueOf(input[8]));
					}
					zuAendern.add(st);
				}
				in.close();
			} catch (IOException e) {
				System.err.println("cat: Fehler beim Verarbeiten");
			}
		}
		transTableModell = new TransTabelle(zuAendern);
		transTable.setModel(transTableModell);
		// jta2.append(text);
		loeschen.setEnabled(true);
	}
	
	public void export() {
		AETWorker w = new AETWorker(this);
		File exportF = new File(jtf.getText());
		w.export(exportF, dateienS,zuAendern, dateien, dbabschnF);
	}

	public void exportTrans() {
		String text = "";
		for (Station[] station : zuAendern) {
			text += station[0].getABS().getVNK() + "\t";
			text += station[0].getABS().getNNK() + "\t";
			text += station[0].getVST() + "\t";
			text += station[0].getBST() + "\t";
			text += station[1].getABS().getVNK() + "\t";
			text += station[1].getABS().getNNK() + "\t";
			text += station[1].getVST() + "\t";
			text += station[1].getBST() + "\t";
			text += station[1].getDrehung() + "\n\r";
		}
		JFileChooser jfc = new JFileChooser();
		jfc.setFileSelectionMode(JFileChooser.FILES_ONLY);
		int ok = jfc.showSaveDialog(this);

		if (ok == JFileChooser.APPROVE_OPTION) {
			System.out.println(jfc.getSelectedFile());
			BufferedWriter writer = null;
			try {
				writer = new BufferedWriter(new FileWriter(jfc.getSelectedFile()));
				writer.write(text);

			} catch (IOException e) {
			} finally {
				try {
					if (writer != null)
						writer.close();
				} catch (IOException e) {
				}
			}
		}
	}

	public void addTransBack(Station[] station) {
		if (station.length == 2) {
			zuAendern.add(station.clone());
			/*
			 * String text = ""; for (Station st : station) { text +=
			 * st.getABS().getVNK() + "\t"; text += st.getABS().getNNK() + "\t";
			 * text += st.getVST() + "\t"; text += st.getBST() + "\t"; } text =
			 * text.substring(0, text.length() - 2) + "\n";
			 */
			transTableModell = new TransTabelle(zuAendern);
			transTable.setModel(transTableModell);
			// jta2.append(text);
			loeschen.setEnabled(true);
		}
	}


	

	@Override
	public void actionPerformed(ActionEvent ae) {
		switch (ae.getActionCommand()) {
		case "Öffnen":
			importFolder();
			break;

		case "speichern":
			exportFolder();
			break;

		case "trans":
			addTrans();
			break;

		case "importT":
			importTrans();
			break;

		case "exportT":
			exportTrans();
			break;

		case "export":
			if (jtf.getText().equals("") || !dbabschnF.exists()) {
				JOptionPane.showMessageDialog(null,
						"Es sind noch keine gültigen Eingabe- und/oder Ausgabeordner gewählt!");
			} else {
				export();
			}
			break;

		case "löschen":
			transLoeschen();
			break;
		}
	}

	private void transLoeschen() {
		int[] select = transTable.getSelectedRows();
		if (select.length > 0) {
			int result = JOptionPane.showConfirmDialog(null, "Möchten Sie die Daten wirklich löschen?", "Zeile löschen",
					JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
			if (result == JOptionPane.OK_OPTION) {
				for (int i = select.length - 1; i >= 0; i--) {
					zuAendern.remove(select[i]);
				}
			}
			transTableModell = new TransTabelle(zuAendern);
			transTable.setModel(transTableModell);
		}
	}

	public void log(Exception e) {
		e.printStackTrace();
	}
}
