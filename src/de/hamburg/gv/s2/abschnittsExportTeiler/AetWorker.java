package de.hamburg.gv.s2.abschnittsExportTeiler;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import javax.swing.JOptionPane;

import com.linuxense.javadbf.DBFField;
import com.linuxense.javadbf.DBFReader;
import com.linuxense.javadbf.DBFWriter;

import de.hamburg.gv.s2.ChangeSet;
import de.hamburg.gv.s2.ChangeSetDB;
import de.hamburg.gv.s2.Netzknoten;

public class AetWorker {
	private AetListener gui;
	private int geschDatei = 0;
	private Connection dreher;

	public AetWorker(AetListener gui) {
		this.gui = gui;
		dreher = loadDreher();
	}

	private void copyFilesEtc(String export, File[] dateienS) {
		// Sonstige Dateien kopieren

		for (File datei : dateienS) {
			try {
				Files.copy(datei.toPath(), Paths.get(export + "\\" + datei.getName()));
				geschDatei++;
			} catch (IOException e) {
				gui.showTextLine(e.getMessage());
			}
		}
	}

	private void createExportPath(File exportF) {
		// Export-Pfad pr�fen/anlegen

		if (!exportF.exists()) {
			exportF.mkdirs();
		}
	}

	public void export(File exportF, File[] dateienS, ChangeSetDB changes, File[] dateien, File dbabschnF) {
		String export = exportF.getAbsolutePath();

		createExportPath(exportF);
		copyFilesEtc(export, dateienS);

		// �nderungsdatens�tze z�hlen
		int anzZuAendern = changes.size();

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

					// VNK/NNK/VST/BST gef�llt?
					if (spalten.length > 3 && spalten[0] != null && spalten[1] != null && spalten[2] != null) {

						Netzknoten vnkA = new Netzknoten(((String) spalten[0]).trim());
						Netzknoten nnkA = new Netzknoten(((String) spalten[1]).trim());
						int vstA = ((BigDecimal) spalten[2]).intValue();
						int bstA = ((BigDecimal) spalten[3]).intValue();
						// System.out.print("\n" + vnkA + "\t" + nnkA + "\t" +
						// vstA + "\t" + bstA + "\t->\t");

						// jeder �nderungsdatensatz
						for (int j = 0; j < anzZuAendern; j++) {
							ChangeSet aenderung = changes.get(j);
							Netzknoten vnkN = aenderung.getAlt().getABS().getVNK();
							Netzknoten nnkN = aenderung.getAlt().getABS().getNNK();

							// VNK/NNK gleich?
							if (vnkA.equals(vnkN) && nnkA.equals(nnkN)) {
								schonDrin = true;
								int vstN = aenderung.getAlt().getVST();
								int bstN = aenderung.getAlt().getBST();

								Netzknoten vnkE = aenderung.getNeu().getABS().getVNK();
								Netzknoten nnkE = aenderung.getNeu().getABS().getNNK();

								int vstE = aenderung.getNeu().getVST();
								int bstE = aenderung.getNeu().getBST();

								boolean gedreht = aenderung.isGedreht();

								Object[] eintrag = null;
								if (bstA != vstA) {
									// Streckenbezogene Objekte
									if (vstN >= vstA && bstN <= bstA) {
										// neuer Abschnitt vollst�ndig in altem
										// Attribut
										int vst = vstE;
										int bst = bstE; // = LEN
										eintrag = changeStation(spalten.clone(), vnkE, nnkE, vst, bst);
									} else if (vstN <= vstA && bstN >= bstA) {
										// neuer Abschnitt beginnt vor altem
										// Attribut und
										// endet danach - Attribut vollst�ndig
										// umschlossen von neuem Abschnitt
										double faktor = 1.0 * (bstE - vstE) / (bstN - vstN);
										int vst = (int) Math.round(vstE + (vstA - vstN) * faktor);
										int bst = (int) Math.round(vstE + (bstA - vstN) * faktor);
										eintrag = changeStation(spalten.clone(), vnkE, nnkE, vst, bst);
									} else if (vstN <= vstA && bstN < bstA && bstN > vstA) {
										// neuer Abschnitt beginnt vorher und
										// endet in altem Abschnitt
										double faktor = 1.0 * (bstE - vstE) / (bstN - vstN);
										int vst = (int) Math.round(vstE + (vstA - vstN) * faktor);
										int bst = bstE; // = LEN
										eintrag = changeStation(spalten.clone(), vnkE, nnkE, vst, bst);
									} else if (vstN >= vstA && vstN <= bstA && bstN > bstA && bstA != vstN) {
										// neuer Abschnitt beginnt in altem
										// Abschnitt und endet danach
										int vst = vstE;
										double faktor = 1.0 * (bstE - vstE) / (bstN - vstN);
										int bst = (int) Math.round(vstE + (bstA - vstN) * faktor);
										eintrag = changeStation(spalten.clone(), vnkE, nnkE, vst, bst);
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
										eintrag = changeStation(spalten.clone(), vnkE, nnkE, st, st);
									} else if (vstN == 0 && stA == 0) {
										// neuer Abschnitt beginnt am Anfang des
										// alten
										int st = vstE;
										eintrag = changeStation(spalten.clone(), vnkE, nnkE, st, st);
									}
								}
								if (eintrag != null) {
									if (gedreht) {
										System.out.println("Abschnitt wird gedreht");
										eintrag = wendeAbschnitt(eintrag, felder, dateien[i].getName(), bstE);
									}
									writer.addRecord(eintrag);
								}
							}
						}
						if (!schonDrin) {
							writer.addRecord(spalten);
						}

						/*
						 * if (!schonDrin) { System.out.print("\n" + vnkA + "\t" + nnkA + "\t" + vstA +
						 * "\t" + bstA + "\t->\t"); }
						 */
					}
				}

				// System.out.println("Speichere nach: " + export + "\\" +
				// dateien[i].getName());
				writer.close();
				reader.close();
				geschDatei++;
			} catch (Exception e) {
				gui.showTextLine(e.getMessage());
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
				// System.out.println(key);
				dbab_org.put(key, spalten);
				dbab_alt.put(key, spalten);
			}

			for (int j = 0; j < anzZuAendern; j++) {
				// System.out.println("0");
				ChangeSet aenderung = changes.get(j);
				String key = aenderung.getAlt().getABS().getVNK() + " " + aenderung.getAlt().getABS().getNNK();
				// System.out.println(key);
				if (dbab_org.containsKey(key)) {
					// System.out.println("1");
					// Object[] rec = dbab.get(key);
					dbab_alt.remove(key);
					Netzknoten vnk = aenderung.getNeu().getABS().getVNK();
					Netzknoten nnk = aenderung.getNeu().getABS().getNNK();
					double len = aenderung.getNeu().getBST();
					String key2 = vnk + " " + nnk;
					if (dbab_neu.containsKey(key2)) {
						// System.out.println("2");
						if (len > (double) dbab_neu.get(key2)[2]) {
							Object[] ch = dbab_neu.get(key2);
							ch[2] = len;
							dbab_neu.put(key2, ch);
						}
					} else if (dbab_alt.containsKey(key2)) {
						// System.out.println("3");
						if (((BigDecimal) dbab_alt.get(key2)[2]).compareTo(new BigDecimal(len)) == -1 ) {
							Object[] ch = dbab_alt.get(key2);
							ch[2] = len;
							dbab_neu.put(key2, ch);
							dbab_alt.remove(key2);
						}

					} else {
						// System.out.println("4");
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
				// System.out.println("5");
			}
			for (Entry<String, Object[]> z : dbab_alt.entrySet()) {
				dbabschnW.addRecord(z.getValue());
			}

			dbabschnW.close();
			dbabschnR.close();
		} catch (Exception e) {
			gui.showTextLine(e.getMessage());
		}

		JOptionPane.showMessageDialog(null,
				"Es wurden " + geschDatei + " Dateien erfolgreich in " + export + " geschrieben!");
	}

	private Object[] changeStation(Object[] spalten, Netzknoten vnkE, Netzknoten nnkE, int vst, int bst) {
		spalten[0] = vnkE.toString();
		spalten[1] = nnkE.toString();
		spalten[2] = (double) vst;
		spalten[3] = (double) bst;
		// System.out.print(vnk + " " + nnk + " " + vst + " " + bst);
		return spalten;
	}

	private Connection loadDreher() {
		String klartextFile = "klartexte.csv";
		String transformationFile = "transformation.csv";
		String zeile = "";
		String splitter = ";";

		Connection c = null;
		try {
			Class.forName("org.sqlite.JDBC");
			c = DriverManager.getConnection("jdbc:sqlite::memory:");
			//c = DriverManager.getConnection("jdbc:sqlite:objZusammenfasser.db");

			String t = "CREATE TABLE transformation (obj text, feld text, kt_von text, kt_table, negativ text, positiv text); "
					+ "CREATE TABLE klartexte (klartext text, aus text, wird text);";
			Statement stmt = c.createStatement();
			stmt.executeUpdate(t);

			c.setAutoCommit(false);

			t = "INSERT INTO transformation VALUES ";

			try (BufferedReader br = new BufferedReader(new FileReader(transformationFile))) {
				String[] z_vorlage = new String[6];

				while ((zeile = br.readLine()) != null) {

					// use comma as separator
					String[] z_raw = zeile.split(splitter);

					String[] z = z_vorlage.clone();

					for (int i = 0; i < z_raw.length; i++) {
						z[i] = z_raw[i];
					}

					t += "('" + z[0] + "','" + z[1] + "','" + z[2] + "','" + z[3] + "','" + z[4] + "','" + z[5] + "'),";

				}

			} catch (IOException e) {
				e.printStackTrace();
			}
			t = t.substring(0, t.length() - 1) + ";";

			t += "INSERT INTO klartexte VALUES ";

			try (BufferedReader br = new BufferedReader(new FileReader(klartextFile))) {
				while ((zeile = br.readLine()) != null) {

					// use comma as separator
					String[] z = zeile.split(splitter);
					t += "('" + z[0] + "','" + z[1] + "','" + z[2] + "'),";

				}

			} catch (IOException e) {
				e.printStackTrace();
			}
			t = t.substring(0, t.length() - 1) + ";";
			
			System.out.println(t);

			stmt.executeUpdate(t);

			c.commit();
			stmt.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		// log(k + " erfolgreich importiert\n");

		return c;
	}

	private Object getFeld(Object[] spalten, DBFField[] felder, String feld) {
		for (int i = 0; i < felder.length; i++) {
			if (feld.equals(felder[i].getName())) {
				return spalten[i];
			}
		}
		return null;
	}

	private Object[] wendeAbschnitt(Object[] spalten, DBFField[] felder, String db, int laenge) {
		Object[] result = spalten.clone();
		try {
			Statement stmt = dreher.createStatement();
			System.out.println(db);
			ResultSet rs = stmt.executeQuery("SELECT * FROM transformation WHERE obj = '" + db + "';");
			while (rs.next()) {
				String feld = rs.getString("feld");
				System.out.println(feld);
				for (int i = 0; i < felder.length; i++) {
					if (feld.equals(felder[i].getName())) {
						if (rs.getString("kt_von").length() > 0) {
							String kt_von = rs.getString("kt_von");
							ResultSet rs2 = stmt.executeQuery(
									"SELECT wird FROM klartexte WHERE klartext = '" + rs.getString("kt_table")
											+ "' and aus = '" + getFeld(spalten, felder, kt_von) + "';");
							rs2.next();
							result[i] = rs2.getString("wird");
							System.out.println(rs2.getString("wird"));
						} else if (rs.getString("negativ").length() > 0) {
							Object obj;
							if ((obj = getFeld(spalten, felder, rs.getString("negativ"))) != null) {
								result[i] = -(Double) obj;
							}
							
						} else if (rs.getString("positiv").length() > 0) {
							result[i] = getFeld(spalten, felder, rs.getString("positiv"));
							if (rs.getString("positiv").equals("VST") || rs.getString("positiv").equals("BST")) {
								result[i] = laenge - (double) result[i];
							}
						}
						break;
					}
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}

		return result;
	}

}
