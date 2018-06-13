package de.hamburg.gv.s2.abschnittsExportTeiler;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.geom.AffineTransform;
import java.io.File;
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
import de.hamburg.gv.s2.ChangeSet;
import de.hamburg.gv.s2.ChangeSetDBListener;

/**
 * @author Florian Timm
 *
 */
public class AetGUI extends JFrame implements ActionListener, AetListener, KeyListener, ChangeSetDBListener {
	private static final long serialVersionUID = 1L;

	private JTextArea jta;
	private JTable transTable;
	private JTextField jtf;
	private JButton trans, export, loeschen, importT, exportT, importDB;
	private AetKontrolle kontroll;

	public static void main(String[] args) {
		new AetGUI();
	}

	public AetGUI() {
		super("AbschnittsExportTeiler");
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (Exception e) {
		}

		kontroll = new AetKontrolle(this);
		kontroll.getChangeSetDB().setListener(this);

		this.setPreferredSize(new Dimension(600, 400));
		this.setLocationRelativeTo(null);

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

		transTable = new JTable(kontroll.getTableModell());
		JScrollPane jsp2 = new JScrollPane(transTable);
		unten.add(jsp2, BorderLayout.CENTER);
		
		
	
		
		JPanel rechtsPanel = new JPanel();
		rechtsPanel.setLayout(new GridLayout(2, 1));
		unten.add(rechtsPanel, BorderLayout.EAST);
		
		exportT = new JButton("export");
		Font lvButtonFont = exportT.getFont();
		AffineTransform at = new AffineTransform();
		at.rotate(-1.57d);	
		exportT.setActionCommand("exportT");
		exportT.addActionListener(this);
		exportT.setFont(lvButtonFont.deriveFont(at));
		exportT.setVerticalAlignment(SwingConstants.BOTTOM);
		exportT.setEnabled(false);
		rechtsPanel.add(exportT);

		loeschen = new JButton("löschen");
		loeschen.addActionListener(this);
		loeschen.setFont(lvButtonFont.deriveFont(at));
		loeschen.setVerticalAlignment(SwingConstants.BOTTOM);
		loeschen.setEnabled(false);
		rechtsPanel.add(loeschen);

		JPanel imexportT = new JPanel();
		imexportT.setLayout(new GridLayout(2, 1));
		unten.add(imexportT, BorderLayout.WEST);
		
		importDB = new JButton("...von DB");
		importDB.setActionCommand("importDB");
		importDB.addActionListener(this);
		importDB.setFont(lvButtonFont.deriveFont(at));
		importDB.setVerticalAlignment(SwingConstants.BOTTOM);
		importDB.setEnabled(false);
		imexportT.add(importDB);

		importT = new JButton("import");
		importT.setActionCommand("importT");
		importT.addActionListener(this);
		importT.setFont(lvButtonFont.deriveFont(at));
		importT.setVerticalAlignment(SwingConstants.BOTTOM);
		importT.setEnabled(false);
		imexportT.add(importT);

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
	public void importFolder() {
		JFileChooser jfc = new JFileChooser("D:\\TTSIB\\ESS");
		jfc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		jfc.setAcceptAllFileFilterUsed(false);
		int ok = jfc.showOpenDialog(this);

		if (ok == JFileChooser.APPROVE_OPTION) {
			jta.setText("");
			kontroll.setFolder(jfc.getSelectedFile());
		}
	}

	@Override
	public void actionPerformed(ActionEvent ae) {
		switch (ae.getActionCommand()) {
		case "Öffnen":
			importFolder();
			break;

		case "speichern":
			selectExportFolder();
			break;

		case "trans":
			addTrans();
			break;

		case "importT":
			importTrans();
			break;

		case "exportT":
			exportChangeSets();
			break;

		case "export":
			if (!kontroll.check()) {
				showMessage("Es sind noch keine gültigen Eingabe- und/oder Ausgabeordner gewählt!");
			} else {
				kontroll.export();
			}
			break;

		case "löschen":
			deleteChangeSet();
			break;
		}
	}

	public void importTrans() {
		JFileChooser jfc = new JFileChooser();
		jfc.setFileSelectionMode(JFileChooser.FILES_ONLY);
		int ok = jfc.showOpenDialog(this);

		if (ok == JFileChooser.APPROVE_OPTION) {
			kontroll.getChangeSetDB().importFromFile(jfc.getSelectedFile());
		}
	}

	public void exportChangeSets() {
		JFileChooser jfc = new JFileChooser();
		jfc.setFileSelectionMode(JFileChooser.FILES_ONLY);
		int ok = jfc.showSaveDialog(this);

		if (ok == JFileChooser.APPROVE_OPTION) {
			System.out.println(jfc.getSelectedFile());
			jfc.getSelectedFile();
		}
	}

	private void deleteChangeSet() {
		int[] select = transTable.getSelectedRows();
		if (select.length > 0) {
			int result = JOptionPane.showConfirmDialog(null, "Möchten Sie die Daten wirklich löschen?", "Zeile löschen",
					JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
			if (result == JOptionPane.OK_OPTION) {
				for (int i = select.length - 1; i >= 0; i--) {
					kontroll.getChangeSetDB().remove(select[i]);
				}
			}
		}
	}

	public void log(Exception e) {
		e.printStackTrace();
	}

	@Override
	public void showTextLine(String zeile) {
		// TODO Auto-generated method stub

	}

	public void activateButtons() {
		trans.setEnabled(true);
		export.setEnabled(true);
		importT.setEnabled(true);
		exportT.setEnabled(true);
		//importDB.setEnabled(true);
	}

	@Override
	public void showMessage(String text) {
		JOptionPane.showMessageDialog(this, text);
	}

	public void addTrans() {
		new ChangeSetChooser(this, kontroll);
	}

	/**
	 * Wählt den Export-Ordner
	 * 
	 * @return erfolgreich?
	 */
	public void selectExportFolder() {
		JFileChooser jfc = new JFileChooser("D:\\TTSIB\\ESS");
		jfc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		jfc.setAcceptAllFileFilterUsed(false);
		int ok = jfc.showOpenDialog(this);

		if (ok == JFileChooser.APPROVE_OPTION) {
			jtf.setText(jfc.getSelectedFile().getAbsolutePath());
			kontroll.setExportFolder(jfc.getSelectedFile());
		}

	}

	public void addChangeLog(ChangeSet changeset) {
		kontroll.getChangeSetDB().addSimple(changeset);
	}

	public void changeSetsChanged() {
		transTable.setModel(kontroll.getTableModell());
		loeschen.setEnabled(kontroll.getChangeSetDB().size() > 0);
	}

	@Override
	public void keyPressed(KeyEvent arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void keyReleased(KeyEvent arg0) {
		File exportFolder = new File(jtf.getText());
		kontroll.setExportFolder(exportFolder);
	}

	@Override
	public void keyTyped(KeyEvent arg0) {
		// TODO Auto-generated method stub

	}

}
