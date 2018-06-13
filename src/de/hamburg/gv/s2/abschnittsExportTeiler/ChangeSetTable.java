package de.hamburg.gv.s2.abschnittsExportTeiler;

import java.util.ArrayList;

import javax.swing.JOptionPane;
import javax.swing.table.AbstractTableModel;

public class TransTabelle extends AbstractTableModel {
	private static final long serialVersionUID = 1L;
	ArrayList<Station[]> trans;
	String[] spalten = { "VNK", "NNK", "VST", "BST", "VNK", "NNK", "VST", "BST", "\u21C4" };

	public TransTabelle(ArrayList<Station[]> trans) {
		this.trans = trans;

	}

	@Override
	public int getColumnCount() {
		// TODO Auto-generated method stub
		return 9;
	}

	@Override
	public int getRowCount() {
		// TODO Auto-generated method stub
		return trans.size();
	}

	@Override
	public Object getValueAt(int reihe, int spalte) {
		// TODO Auto-generated method stub

		Station[] st = trans.get(reihe);

		switch (spalte) {
		case 0:
			return st[0].getABS().getVNK();
		case 1:
			return st[0].getABS().getNNK();
		case 2:
			return st[0].getVST();
		case 3:
			return st[0].getBST();
		case 4:
			return st[1].getABS().getVNK();
		case 5:
			return st[1].getABS().getNNK();
		case 6:
			return st[1].getVST();
		case 7:
			return st[1].getBST();
		case 8:
			return st[1].getDrehung();
		}

		return null;
	}

	public String getColumnName(int column) {
		return spalten[column];
	}

	public boolean isCellEditable(int row, int spalte) {
		switch (spalte) {
		case 0:
			return false;
		case 1:
			return false;
		case 2:
			return true;
		case 3:
			return true;
		case 4:
			return true;
		case 5:
			return true;
		case 6:
			return true;
		case 7:
			return true;
		case 8:
			return true;
		}
		return false;
	}
	
	// Hier kann man die Klasse für eine Spalte ändern.
    @Override
    public Class<?> getColumnClass(int column) {
        if(column == 8){
            return Boolean.class;
        }
        return super.getColumnClass(column);
    }

	public void setValueAt(Object value, int row, int column) {
		if (!(getValueAt(row, column).equals(value))) {
			int result = JOptionPane.showConfirmDialog(null, "Möchten Sie die Daten wirklich ändern?", "Datenänderung",
					JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
			if (result == JOptionPane.OK_OPTION) {

				Station[] st = trans.get(row);
				switch (column) {
				case 2:
					st[0].setVST(Integer.valueOf((String) value));
					break;
				case 3:
					st[0].setBST(Integer.valueOf((String) value));
					break;
				case 4:
					Abschnitt abs = st[1].getABS();
					abs.setVNK((String) value);
					st[1].setABS(abs);
					break;
				case 5:
					Abschnitt abs2 = st[1].getABS();
					abs2.setNNK((String) value);
					st[1].setABS(abs2);
					break;
				case 6:
					st[1].setVST(Integer.valueOf((String) value));
					break;
				case 7:
					st[1].setBST(Integer.valueOf((String) value));
					break;
				case 8:
					st[1].setDrehung((boolean) value);
					break;
				}
				trans.set(row, st);

			}
		}
	}
}
