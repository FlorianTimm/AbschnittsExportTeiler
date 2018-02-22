package de.hamburg.gv.s2.abschnittsExportTeiler;

public class Station {
	Abschnitt abs;
	int vst = 0, bst;
	boolean gedreht;

	/**
	 * Erzeugt eine Station
	 * 
	 * @param abs Abschnitt
	 * @param vst von Station
	 * @param bst bis Station
	 */
	public Station(Abschnitt abs, int vst, int bst) {
		this.abs = abs;
		setVST(vst);
		setBST(bst);
	}
	
	public Station(Abschnitt abs, int vst, int bst, boolean gedreht) {
		this.abs = abs;
		setVST(vst);
		setBST(bst);
		setDrehung(gedreht);
	}

	public Station(Abschnitt abs) {
		this.abs = abs;
		this.vst = 0;
		this.bst = pruefeST(abs.len);
	}

	public void setVST(int vst) {
		this.vst = pruefeST(vst);
		if (vst > bst) {
			setBST(vst);
		}
	}

	public void setBST(int bst) {
		bst = pruefeST(bst);
		if (bst < this.vst) {
			this.bst = this.vst;
		} else {
			this.bst = bst;
		}
	}
	
	public void setDrehung(boolean gedreht) {
		this.gedreht = gedreht;
	}

	public Abschnitt getABS() {
		return abs;
	}

	public int pruefeST(int st) {
		if (st < 0) {
			return 0;
		} else if (abs.len == -1 || st <= abs.len) {
			return st;
		} else {
			return abs.len;
		}
	}


	public int getVST() {
		return pruefeST(vst);
	}

	public int getBST() {
		return pruefeST(bst);
	}
	
	public void setABS(Abschnitt abs) {
		this.abs = abs;
	}
	
	public boolean getDrehung () {
		return gedreht;
	}

}
