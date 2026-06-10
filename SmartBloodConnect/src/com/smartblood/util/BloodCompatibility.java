package com.smartblood.util;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

public final class BloodCompatibility {
    private static final Map<String, Set<String>> DONOR_GROUPS_BY_RECIPIENT = new LinkedHashMap<>();

    static {
        DONOR_GROUPS_BY_RECIPIENT.put("A+", orderedSet("A+", "A-", "O+", "O-"));
        DONOR_GROUPS_BY_RECIPIENT.put("A-", orderedSet("A-", "O-"));
        DONOR_GROUPS_BY_RECIPIENT.put("B+", orderedSet("B+", "B-", "O+", "O-"));
        DONOR_GROUPS_BY_RECIPIENT.put("B-", orderedSet("B-", "O-"));
        DONOR_GROUPS_BY_RECIPIENT.put("AB+", orderedSet("AB+", "AB-", "A+", "A-", "B+", "B-", "O+", "O-"));
        DONOR_GROUPS_BY_RECIPIENT.put("AB-", orderedSet("AB-", "A-", "B-", "O-"));
        DONOR_GROUPS_BY_RECIPIENT.put("O+", orderedSet("O+", "O-"));
        DONOR_GROUPS_BY_RECIPIENT.put("O-", orderedSet("O-"));
    }

    private BloodCompatibility() {
    }

    public static Set<String> getCompatibleDonorGroups(String recipientBloodGroup) {
        String normalized = normalize(recipientBloodGroup);
        return DONOR_GROUPS_BY_RECIPIENT.getOrDefault(normalized, Collections.emptySet());
    }

    public static boolean isCompatible(String donorBloodGroup, String recipientBloodGroup) {
        return getCompatibleDonorGroups(recipientBloodGroup).contains(normalize(donorBloodGroup));
    }

    public static String normalize(String value) {
        return value == null ? "" : value.trim().toUpperCase(Locale.ROOT);
    }

    private static Set<String> orderedSet(String... values) {
        return new LinkedHashSet<>(Arrays.asList(values));
    }
}
