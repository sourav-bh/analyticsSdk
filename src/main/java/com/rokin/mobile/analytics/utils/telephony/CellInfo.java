package com.rokin.mobile.analytics.utils.telephony;

/**
 * 
 * @author Sourav
 *
 */
public class CellInfo 
{
	private int MCC, MNC, LAC, cellID, signalStrength;
	
	public CellInfo()
	{
		
	}

	/**
	 * @return the mCC
	 */
	public int getMCC() {
		return MCC;
	}

	/**
	 * @param mCC the mCC to set
	 */
	public void setMCC(int mCC) {
		MCC = mCC;
	}

	/**
	 * @return the mNC
	 */
	public int getMNC() {
		return MNC;
	}

	/**
	 * @param mNC the mNC to set
	 */
	public void setMNC(int mNC) {
		MNC = mNC;
	}

	/**
	 * @return the lAC
	 */
	public int getLAC() {
		return LAC;
	}

	/**
	 * @param lAC the lAC to set
	 */
	public void setLAC(int lAC) {
		LAC = lAC;
	}

	/**
	 * @return the cellID
	 */
	public int getCellID() {
		return cellID;
	}

	/**
	 * @param cellID the cellID to set
	 */
	public void setCellID(int cellID) {
		this.cellID = cellID;
	}

	/**
	 * @return the signalStrength
	 */
	public int getSignalStrength() {
		return signalStrength;
	}

	/**
	 * @param signalStrength the signalStrength to set
	 */
	public void setSignalStrength(int signalStrength) {
		this.signalStrength = signalStrength;
	}
}
