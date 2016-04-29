

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author Bryan
 */
public class Policy {
    private String policyName;
    private String programsList;
    private String sitesList;
    private boolean sitesWhitelist;
    public Policy(String policyName, String programsList, String sitesList, boolean sitesWhitelist)
    {
        this.policyName = policyName;
        this.programsList = programsList;
        this.sitesList = sitesList;
        this.sitesWhitelist = sitesWhitelist;
    }

    /**
     * @return the policyName
     */
    public String getPolicyName() {
        return policyName;
    }

    /**
     * @return the programsList
     */
    public String getProgramsList() {
        return programsList;
    }

    /**
     * @return the sitesList
     */
    public String getSitesList() {
        return sitesList;
    }

    /**
     * @return the sitesWhitelist
     */
    public boolean isSitesWhitelist() {
        return sitesWhitelist;
    }
}
