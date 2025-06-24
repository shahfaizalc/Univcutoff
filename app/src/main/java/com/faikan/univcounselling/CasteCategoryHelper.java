package com.faikan.univcounselling;

/**
 * Helper class to manage caste category conversions between display names and codes
 */
public class CasteCategoryHelper {
    
    // Display names in the spinner (from strings.xml)
    private static final String[] DISPLAY_NAMES = {
        "Open Category (OC)",
        "Backward Class (BC)",
        "Backward Class Muslim (BCM)",
        "Most Backward Class (MBC)",
        "Scheduled Caste (SC)",
        "Scheduled Caste Arunthathiyar (SCA)"
    };
    
    // Corresponding codes used in the CutoffDetail class
    private static final String[] CODES = {
        "OC", "BC", "BCM", "MBC", "SC", "SCA"
    };
    
    /**
     * Convert spinner position to category code
     */
    public static String getCodeFromPosition(int position) {
        if (position >= 0 && position < CODES.length) {
            return CODES[position];
        }
        return "OC"; // Default
    }
    
    /**
     * Convert category code to spinner position
     */
    public static int getPositionFromCode(String code) {
        for (int i = 0; i < CODES.length; i++) {
            if (CODES[i].equals(code)) {
                return i;
            }
        }
        return 0; // Default to first position (OC)
    }
}
