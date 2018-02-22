package de.hamburg.gv.s2.abschnittsExportTeiler;

import java.util.regex.Pattern;

public class Abschnitt implements Comparable<Abschnitt> {
    String vnk = "", nnk = "";
    int len = -1;

    public Abschnitt(String vnk, String nnk, int len) {
	setVNK(vnk);
	setNNK(nnk);
	setLEN(len);
    }

    public Abschnitt(String vnk, String nnk) {
	setVNK(vnk);
	setNNK(nnk);
    }

    public Abschnitt() {

    }

    public boolean setVNK(String vnk) {
	if (pruefeNK(vnk)) {
	    this.vnk = vnk.trim();
	    return true;
	} else {
	    System.out.println("Nicht gut: " + nnk);
	    return false;
	}
    }

    public boolean setNNK(String nnk) {
	if (pruefeNK(nnk)) {
	    this.nnk = nnk.trim();
	    return true;
	} else {
	    System.out.println("Nicht gut: " + nnk);
	    return false;
	}
    }

    public boolean setLEN(int len) {
	if (len >= 0) {
	    this.len = len;
	    return true;
	} else {
	    return false;
	}
    }

    public boolean pruefeNK(String nk) {
	// return true;
	// System.out.println(nk.length());
	return Pattern.matches("^\\d{9}([A-Z]?|\\s?)$", nk);
    }

    public String getVNK() {
	return vnk;
    }

    public String getNNK() {
	return nnk;
    }

    public int getLEN() {
	return len;
    }

    public String toString() {
	return getVNK() + " " + getNNK();

    }

    @Override
    public int compareTo(Abschnitt abs) {
	// TODO Auto-generated method stub
	if (this.getVNK().equals(abs.getVNK()) && this.getNNK().equals(abs.getNNK()) && this.getLEN() == abs.getLEN()) {
	    return 0;
	} else if (this.getVNK().equals(abs.getVNK()) && this.getNNK().equals(abs.getNNK())) {
	    if (this.getLEN() > abs.getLEN()) {
		return 1;
	    } else {
		return 0;
	    }
	} else if (this.getVNK().equals(abs.getVNK())) {
	    return this.getNNK().compareTo(this.getNNK());
	} else {
	    return this.getVNK().compareTo(this.getVNK());
	}
    }
}
