package artefact.util;

import java.util.Arrays;

public enum ScannedAppType {

    ADDENDUM,
    NEW_REQUEST;

    public static boolean isAllowedType(String inputType) {
        return Arrays.stream(values()).anyMatch(val -> val.toString().equalsIgnoreCase(inputType));
    }

    static public ScannedAppType forTypeIgnoreCase(String value) {
        for (ScannedAppType scanType : ScannedAppType.values()) {
            if ( scanType.name().equalsIgnoreCase(value) ) return scanType;
        }
        return null;
    }
}

