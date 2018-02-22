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
import java.io.InputStream;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Paths;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
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
import com.linuxense.javadbf.DBFField;
import com.linuxense.javadbf.DBFReader;
import com.linuxense.javadbf.DBFWriter;

/**
 * @author Florian Timm
 *
 */
public class AbschnittsExportTeiler extends JFrame implements ActionListener {
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
		new AbschnittsExportTeiler();
	}

	public AbschnittsExportTeiler() {
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
				while ((line = in.readLine()) != null) {
					System.out.println(line);
					String[] input = line.split("\t");
					Station[] st = new Station[2];
					st[0] = TransAdder.pruefeEingabe(input[0], input[1], input[2], input[3]);
					st[1] = TransAdder.pruefeEingabe(input[4], input[5], input[6], input[7]);
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
			text += station[1].getBST() + "\n\r";
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

	public void export() {

		// Export-Pfad prüfen/anlegen
		File exportF = new File(jtf.getText());
		int geschDatei = 0;
		if (!exportF.exists()) {
			exportF.mkdirs();
		}
		String export = exportF.getAbsolutePath();

		// Sonstige Dateien kopieren
		for (File datei : dateienS) {
			try {
				Files.copy(datei.toPath(), Paths.get(export + "\\" + datei.getName()));
				geschDatei++;
			} catch (IOException e) {
				log(e);
			}
		}

		// Änderungsdatensätze zählen
		int anzZuAendern = zuAendern.size();

		// Jede Datei durchgehen
		for (int i = 0; i < dateien.length; i++) {
			System.out.println("Bearbeite: " + dateien[i].getName());
			try {
				// Reader und Writer init.
				InputStream inputStream = new FileInputStream(dateien[i]);
				DBFReader reader = new DBFReader(inputStream);
				DBFWriter writer = new DBFWriter(new File(export + "\\" + dateien[i].getName()));

				// Felderdefinition der alten Datei kopieren
				int felderAnz = reader.getFieldCount();
				DBFField felder[] = new DBFField[felderAnz];
				for (int j = 0; j < felderAnz; j++) {
					felder[j] = reader.getField(j);
				}
				writer.setFields(felder);

				// Alle Zeilen durchgehen
				Object[] spalten;
				while ((spalten = reader.nextRecord()) != null) {
					boolean schonDrin = false;

					// VNK/NNK/VST/BST gefüllt?
					if (spalten.length > 3 && spalten[0] != null && spalten[1] != null && spalten[2] != null) {

						String vnkA = ((String) spalten[0]).trim();
						String nnkA = ((String) spalten[1]).trim();
						int vstA = ((BigDecimal) spalten[2]).intValue();
						int bstA = ((BigDecimal) spalten[3]).intValue();
						// System.out.print("\n" + vnkA + "\t" + nnkA + "\t" +
						// vstA + "\t" + bstA + "\t->\t");

						// jeder Änderungsdatensatz
						for (int j = 0; j < anzZuAendern; j++) {
							Station[] aenderung = zuAendern.get(j);
							String vnkN = aenderung[0].getABS().getVNK();
							String nnkN = aenderung[0].getABS().getNNK();

							// VNK/NNK gleich?
							if (vnkA.equals(vnkN) && nnkA.equals(nnkN)) {
								schonDrin = true;
								int vstN = aenderung[0].getVST();
								int bstN = aenderung[0].getBST();

								String vnkE = aenderung[1].getABS().getVNK();
								String nnkE = aenderung[1].getABS().getNNK();

								int vstE = aenderung[1].getVST();
								int bstE = aenderung[1].getBST();

								if (bstA != vstA) {
									// Streckenbezogene Objekte
									if (vstN >= vstA && bstN <= bstA) {
										// neuer Abschnitt vollständig in altem
										// Attribut
										int vst = vstE;
										int bst = bstE; // = LEN
										writer.addRecord(changeRecord(spalten.clone(), vnkE, nnkE, vst, bst));
									} else if (vstN <= vstA && bstN >= bstA) {
										// neuer Abschnitt beginnt vor altem
										// Attribut und
										// endet danach - Attribut vollständig
										// umschlossen von neuem Abschnitt
										double faktor = 1.0 * (bstE - vstE) / (bstN - vstN);
										int vst = (int) Math.round(vstE + (vstA - vstN) * faktor);
										int bst = (int) Math.round(vstE + (bstA - vstN) * faktor);
										writer.addRecord(changeRecord(spalten.clone(), vnkE, nnkE, vst, bst));
									} else if (vstN <= vstA && bstN < bstA && bstN > vstA) {
										// neuer Abschnitt beginnt vorher und
										// endet in altem Abschnitt
										double faktor = 1.0 * (bstE - vstE) / (bstN - vstN);
										int vst = (int) Math.round(vstE + (vstA - vstN) * faktor);
										int bst = bstE; // = LEN
										writer.addRecord(changeRecord(spalten.clone(), vnkE, nnkE, vst, bst));
									} else if (vstN >= vstA && vstN <= bstA && bstN > bstA && bstA != vstN) {
										// neuer Abschnitt beginnt in altem
										// Abschnitt und endet danach
										int vst = vstE;
										double faktor = 1.0 * (bstE - vstE) / (bstN - vstN);
										int bst = (int) Math.round(vstE + (bstA - vstN) * faktor);
										writer.addRecord(changeRecord(spalten.clone(), vnkE, nnkE, vst, bst));
									}
								} else {
									// Punktuelle Objekte
									int stA = vstA;
									if (vstN < stA && bstN >= stA) {
										// Punkt liegt in neuem Abschnitt (aber
										// nicht an seinem Anfang)
										double faktor = 1.0 * (bstE - vstE) / (bstN - vstN);
										int st = (int) Math.round(vstE + (stA - vstN) * faktor);
										// int st = vstE + stA - vstN;
										writer.addRecord(changeRecord(spalten.clone(), vnkE, nnkE, st, st));
									} else if (vstN == 0 && stA == 0) {
										// neuer Abschnitt beginnt am Anfang des
										// alten
										int st = vstE;
										writer.addRecord(changeRecord(spalten.clone(), vnkE, nnkE, st, st));
									}
								}

							}
						}
						if (!schonDrin) {
							writer.addRecord(spalten);
						}

						/*
						 * if (!schonDrin) { System.out.print("\n" + vnkA + "\t"
						 * + nnkA + "\t" + vstA + "\t" + bstA + "\t->\t"); }
						 */
					}
				}

				// System.out.println("Speichere nach: " + export + "\\" +
				// dateien[i].getName());
				writer.close();
				reader.close();
				geschDatei++;
			} catch (Exception e) {
				log(e);
			}
		}
		// System.out.println("DBXXXXX erfolgreich bearbeitet");
		try {
			// System.out.println("DBABSCHN kopieren:");
			DBFReader dbabschnR = new DBFReader(new FileInputStream(dbabschnF));
			DBFWriter dbabschnW = new DBFWriter(new File(export + "//" + dbabschnF.getName()));
			// System.out.println("DBABSCHN eingelesen");
			int felderAnz = dbabschnR.getFieldCount();
			DBFField felder[] = new DBFField[felderAnz];
			// System.out.println("DBABSCHN-Felder eingelesen");
			for (int j = 0; j < felderAnz; j++) {
				felder[j] = dbabschnR.getField(j);
			}
			dbabschnW.setFields(felder);
			// System.out.println("Felder kopiert");

			Map<String, Object[]> dbab_alt = new HashMap<String, Object[]>();
			Map<String, Object[]> dbab_org = new HashMap<String, Object[]>();
			Map<String, Object[]> dbab_neu = new HashMap<String, Object[]>();
			Object[] spalten;
			while ((spalten = dbabschnR.nextRecord()) != null) {
				String key = ((String) spalten[0]).trim() + " " + ((String) spalten[1]).trim();
				//System.out.println(key);
				dbab_org.put(key, spalten);
				dbab_alt.put(key, spalten);
			}

			for (int j = 0; j < anzZuAendern; j++) {
				//System.out.println("0");
				Station[] aenderung = zuAendern.get(j);
				String key = aenderung[0].getABS().getVNK() + " " + aenderung[0].getABS().getNNK();
				//System.out.println(key);
				if (dbab_org.containsKey(key)) {
					//System.out.println("1");
					// Object[] rec = dbab.get(key);
					dbab_alt.remove(key);
					String vnk = aenderung[1].getABS().getVNK();
					String nnk = aenderung[1].getABS().getNNK();
					double len = aenderung[1].getBST();
					String key2 = vnk + " " + nnk;
					if (dbab_neu.containsKey(key2)) {
						//System.out.println("2");
						if (len > (double) dbab_neu.get(key2)[2]) {
							Object[] ch = dbab_neu.get(key2);
							ch[2] = len;
							dbab_neu.put(key2, ch);
						}
					} else if (dbab_alt.containsKey(key2)) {
						//System.out.println("3");
						if (len > (double) dbab_alt.get(key2)[2]) {
							Object[] ch = dbab_alt.get(key2);
							ch[2] = len;
							dbab_neu.put(key2, ch);
							dbab_alt.remove(key2);
						}
						
					} else {
						//System.out.println("4");
						Object[] entry = new Object[4];
						entry[0] = vnk;
						entry[1] = nnk;
						entry[2] = len;
						entry[3] = "";
						dbab_neu.put(key2, entry);
					}

				}
			}

			for (Entry<String, Object[]> z : dbab_neu.entrySet()) {
				dbabschnW.addRecord(z.getValue());
				//System.out.println("5");
			}
			for (Entry<String, Object[]> z : dbab_alt.entrySet()) {
				dbabschnW.addRecord(z.getValue());
			}

			dbabschnW.close();
			dbabschnR.close();
		} catch (Exception e) {
			log(e);
		}

		JOptionPane.showMessageDialog(null,
				"Es wurden " + geschDatei + " Dateien erfolgreich in " + export + " geschrieben!");
	}

	private Object[] changeRecord(Object[] spalten, String vnk, String nnk, int vst, int bst) {
		spalten[0] = vnk.trim();
		spalten[1] = nnk.trim();
		spalten[2] = (double) vst;
		spalten[3] = (double) bst;
		// System.out.print(vnk + " " + nnk + " " + vst + " " + bst);
		return spalten;
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
