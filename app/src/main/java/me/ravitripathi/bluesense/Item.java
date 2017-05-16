package me.ravitripathi.bluesense;

/**
 * Created by Ravi on 15-05-2017.
 */


class Item {
    public String getSNO() {
        return SNO;
    }

    public String getASLIP() {
        return ASLIP;
    }

    public String getBSLIP() {
        return BSLIP;
    }

    public String getCSLIP() {
        return CSLIP;
    }

    public String getDSLIP() {
        return DSLIP;
    }

    public String getSLIPRAT() {
        return SLIPRAT;
    }


    private String SNO, ASLIP, BSLIP, CSLIP, DSLIP, SLIPRAT;

    public Item(String S, String A, String B, String C, String D, String R) {
        SNO = S;
        ASLIP = A;
        BSLIP = B;
        CSLIP = C;
        DSLIP = D;
        SLIPRAT = R;
    }

}
