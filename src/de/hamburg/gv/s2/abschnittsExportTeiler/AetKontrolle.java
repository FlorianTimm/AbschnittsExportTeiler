package de.hamburg.gv.s2.abschnittsExportTeiler;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.regex.Pattern;

import javax.swing.table.TableModel;

import com.linuxense.javadbf.DBFException;
import com.linuxense.javadbf.DBFReader;

import de.hamburg.gv.s2.Abschnitt;
import de.hamburg.gv.s2.ChangeSetDB;

public class AetKontrolle implements Runnable {
	private ChangeSetDB changes;
	private File exportFolder;
	private File[] dateien = null, dateienS = null;
	private AetListener gui;
	private File dbabschnF = null;
	private ArrayList<Abschnitt> abschn;

	public AetKontrolle(AetListener gui) {
		this.gui = gui;
		abschn = new ArrayList<Abschnitt>();
		changes = new ChangeSetDB();
	}

	public boolean setFolder(File folder) {
		dateien = folder.listFiles(new FilenameFilter() {
			@Override
			public boolean accept(File dir, String name) {
				return Pattern.matches("(DB)\\d{6}\\.((DBF)|(dbf))", name);
			}
		});

		dateienS = folder.listFiles(new FilenameFilter() {
			@Override
			public boolean accept(File dir, String name) {
				return Pattern.matches("(DB)((OBJDEF)|(OBJEKT))\\.((DBF)|(dbf))", name);
			}
		});

		dbabschnF = new File(folder + "\\DBABSCHN.DBF");
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
						//System.out.println(spalten[0] + " " + spalten[1] + " " + abs.toString());
					}
				}
				Collections.sort(abschn);
				gui.activateButtons();
				dbabschn.close();

			} catch (DBFException e1) {
				e1.printStackTrace();
			} catch (FileNotFoundException e1) {
				e1.printStackTrace();
			}

			for (int i = 0; i < dateien.length; i++) {

				File file = dateien[i];

				if (i > 0) {
					gui.showTextLine("\n");
				}
				gui.showTextLine(file.getName());
				System.out.println(file.getName());

			}
			for (File datei : dateienS) {
				System.out.println(datei.getName());
				gui.showTextLine("\n" + datei.getName());
			}
		} else {
			gui.showMessage("Der Ordner enthält keine DBABSCHNITT, die Import-Daten sind ungültig!");
			return false;
		}

		return true;
	}

	public void export() {
		AetWorker w = new AetWorker(gui);
		w.export(exportFolder, dateienS, changes, dateien, dbabschnF);
	}

	public boolean check() {
		return dbabschnF.exists();
	}

	@Override
	public void run() {
		// TODO Auto-generated method stub
	}

	public ArrayList<Abschnitt> getAbschnitte() {
		return abschn;
	}

	public void setExportFolder(File selectedFile) {
		this.exportFolder = selectedFile;
	}

	public TableModel getTableModell() {
		return new ChangeSetTable(changes);
	}

	public ChangeSetDB getChangeSetDB() {
		// TODO Auto-generated method stub
		return changes;
	}
}
