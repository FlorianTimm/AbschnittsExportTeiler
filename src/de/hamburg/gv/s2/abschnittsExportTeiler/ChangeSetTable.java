package de.hamburg.gv.s2.abschnittsExportTeiler;

import javax.swing.JOptionPane;
import javax.swing.table.AbstractTableModel;

import de.hamburg.gv.s2.Abschnitt;
import de.hamburg.gv.s2.ChangeSet;
import de.hamburg.gv.s2.ChangeSetDB;

public class ChangeSetTable extends AbstractTableModel {
	private static final long serialVersionUID = 1L;
	private ChangeSetDB trans;
	private String[] spalten = { "VNK", "NNK", "VST", "BST", "VNK", "NNK", "VST", "BST", "\u21C4" };

	public ChangeSetTable(ChangeSetDB trans) {
		this.trans = trans;

	}

	@Override
	public int getColumnCount() {
		return 9;
	}

	@Override
	public int getRowCount() {
		return trans.size();
	}

	@Override
	public Object getValueAt(int reihe, int spalte) {
		// TODO Auto-generated method stub

		ChangeSet cs = trans.get(reihe);

		switch (spalte) {
		case 0:
			if (cs.getAlt() == null || cs.getAlt().getABS() == null)
				return null;
			return cs.getAlt().getABS().getVNK();
		case 1:
			if (cs.getAlt() == null || cs.getAlt().getABS() == null)
				return null;
			return cs.getAlt().getABS().getNNK();
		case 2:
			if (cs.getAlt() == null)
				return null;
			return cs.getAlt().getVST();
		case 3:
			if (cs.getAlt() == null )
				return null;
			return cs.getAlt().getBST();
		case 4:
			if (cs.getNeu() == null || cs.getNeu().getABS() == null)
				return null;
			return cs.getNeu().getABS().getVNK();
		case 5:
			if (cs.getNeu() == null || cs.getNeu().getABS() == null)
				return null;
			return cs.getNeu().getABS().getNNK();
		case 6:
			if (cs.getNeu() == null )
				return null;
			return cs.getNeu().getVST();
		case 7:
			if (cs.getNeu() == null )
				return null;
			return cs.getNeu().getBST();
		case 8:
			return cs.isGedreht();
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

				ChangeSet cs = trans.get(row);
				switch (column) {
				case 2:
					cs.getAlt().setVST(Integer.valueOf((String) value));
					break;
				case 3:
					cs.getAlt().setBST(Integer.valueOf((String) value));
					break;
				case 4:
					Abschnitt abs = cs.getNeu().getABS();
					abs.setVNK((String) value);
					cs.getNeu().setABS(abs);
					break;
				case 5:
					Abschnitt abs2 = cs.getNeu().getABS();
					abs2.setNNK((String) value);
					cs.getNeu().setABS(abs2);
					break;
				case 6:
					cs.getNeu().setVST(Integer.valueOf((String) value));
					break;
				case 7:
					cs.getNeu().setBST(Integer.valueOf((String) value));
					break;
				case 8:
					cs.setGedreht((boolean) value);
					break;
				}
				trans.set(row, cs);

			}
		}
	}
}
