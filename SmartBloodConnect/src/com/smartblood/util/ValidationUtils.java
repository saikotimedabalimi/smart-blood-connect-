package com.smartblood.util;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Pattern;

public final class ValidationUtils {
    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,}$", Pattern.CASE_INSENSITIVE);
    private static final Pattern PHONE_PATTERN = Pattern.compile("^\\+?[0-9]{10,15}$");
    private static final Set<String> BLOOD_GROUPS = Set.of("A+", "A-", "B+", "B-", "AB+", "AB-", "O+", "O-");
    private static final Set<String> ACCOUNT_TYPES = Set.of("DONOR", "RECEIVER");
    private static final Set<String> ISSUER_TYPES = Set.of("GOVERNMENT", "UNIVERSITY", "HOSPITAL");

    private ValidationUtils() {
    }

    public static String trimToEmpty(String value) {
        return value == null ? "" : value.trim();
    }

    public static String normalizeEmail(String value) {
        return trimToEmpty(value).toLowerCase(Locale.ROOT);
    }

    public static String normalizePhone(String value) {
        return trimToEmpty(value).replace(" ", "").replace("-", "");
    }

    public static String normalizeBloodGroup(String value) {
        return trimToEmpty(value).toUpperCase(Locale.ROOT);
    }

    public static String normalizeAccountType(String value) {
        return trimToEmpty(value).toUpperCase(Locale.ROOT);
    }

    public static String normalizeIssuerType(String value) {
        return trimToEmpty(value).toUpperCase(Locale.ROOT);
    }

    public static boolean isValidEmail(String email) {
        return EMAIL_PATTERN.matcher(normalizeEmail(email)).matches();
    }

    public static boolean isValidPhone(String phone) {
        return PHONE_PATTERN.matcher(normalizePhone(phone)).matches();
    }

    public static boolean isValidBloodGroup(String bloodGroup) {
        return BLOOD_GROUPS.contains(normalizeBloodGroup(bloodGroup));
    }

    public static boolean isValidAccountType(String accountType) {
        return ACCOUNT_TYPES.contains(normalizeAccountType(accountType));
    }

    public static boolean isValidIssuerType(String issuerType) {
        return ISSUER_TYPES.contains(normalizeIssuerType(issuerType));
    }

    public static boolean isStrongPassword(String password) {
        if (password == null || password.length() < 8 || password.length() > 100) {
            return false;
        }

        boolean hasUpper = false;
        boolean hasLower = false;
        boolean hasDigit = false;
        boolean hasSpecial = false;

        for (char ch : password.toCharArray()) {
            if (Character.isUpperCase(ch)) {
                hasUpper = true;
            } else if (Character.isLowerCase(ch)) {
                hasLower = true;
            } else if (Character.isDigit(ch)) {
                hasDigit = true;
            } else if (!Character.isWhitespace(ch)) {
                hasSpecial = true;
            }
        }

        return hasUpper && hasLower && hasDigit && hasSpecial;
    }

    public static Integer parsePositiveInteger(String value) {
        try {
            int parsed = Integer.parseInt(trimToEmpty(value));
            return parsed > 0 ? parsed : null;
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    public static String validateLogin(String loginType, String emailOrUsername, String password) {
        String normalizedLoginType = trimToEmpty(loginType).toLowerCase(Locale.ROOT);
        if (!"user".equals(normalizedLoginType) && !"admin".equals(normalizedLoginType)) {
            return "Please choose a valid login type.";
        }
        if (trimToEmpty(emailOrUsername).isEmpty()) {
            return "Email or admin username is required.";
        }
        if (trimToEmpty(password).isEmpty()) {
            return "Password is required.";
        }
        if ("user".equals(normalizedLoginType) && !isValidEmail(emailOrUsername)) {
            return "Please enter a valid email address.";
        }
        return null;
    }

    public static String validateRegistration(
            String name,
            String email,
            String password,
            String phone,
            String city,
            String location,
            String accountType,
            String bloodGroup,
            String lastDonationDate) {

        String error = validateText("Full name", name, 100);
        if (error != null) {
            return error;
        }
        if (!isValidEmail(email) || normalizeEmail(email).length() > 120) {
            return "Please enter a valid email address.";
        }
        if (!isStrongPassword(password)) {
            return "Password must be 8+ characters and include uppercase, lowercase, number, and special character.";
        }
        if (!isValidPhone(phone) || normalizePhone(phone).length() > 20) {
            return "Phone number must be 10 to 15 digits and may start with +.";
        }
        error = validateText("City", city, 100);
        if (error != null) {
            return error;
        }
        error = validateText("Location", location, 150);
        if (error != null) {
            return error;
        }
        if (!isValidAccountType(accountType)) {
            return "Please choose a valid account type.";
        }
        if ("DONOR".equals(normalizeAccountType(accountType)) && !isValidBloodGroup(bloodGroup)) {
            return "Please choose a valid blood group for the donor profile.";
        }
        return validateOptionalDate(lastDonationDate, "Last donation date");
    }

    public static String validateProfileUpdate(String name, String phone, String city, String location, String accountType) {
        String error = validateText("Name", name, 100);
        if (error != null) {
            return error;
        }
        if (!isValidPhone(phone) || normalizePhone(phone).length() > 20) {
            return "Phone number must be 10 to 15 digits and may start with +.";
        }
        error = validateText("City", city, 100);
        if (error != null) {
            return error;
        }
        error = validateText("Location", location, 150);
        if (error != null) {
            return error;
        }
        if (!isValidAccountType(accountType)) {
            return "Please choose a valid account type.";
        }
        return null;
    }

    public static String validateRequest(
            String patientName,
            String bloodGroup,
            String location,
            String city,
            String reason,
            String unitsRequired) {

        String error = validateText("Patient name", patientName, 100);
        if (error != null) {
            return error;
        }
        if (!isValidBloodGroup(bloodGroup)) {
            return "Please choose a valid blood group.";
        }
        error = validateText("Hospital / area", location, 150);
        if (error != null) {
            return error;
        }
        error = validateText("City", city, 100);
        if (error != null) {
            return error;
        }
        if (trimToEmpty(reason).isEmpty()) {
            return "Reason is required.";
        }
        Integer parsedUnits = parsePositiveInteger(unitsRequired);
        if (parsedUnits == null) {
            return "Units required must be a positive whole number.";
        }
        return null;
    }

    public static String validateDonorAvailability(String lastDonationDate) {
        return validateOptionalDate(lastDonationDate, "Last donation date");
    }

    public static String validateProcurementDetails(
            String contactName,
            String contactPhone,
            String hospitalName,
            String requiredBy,
            String formalityNotes) {

        String error = validateText("Contact person name", contactName, 100);
        if (error != null) {
            return error;
        }
        if (!isValidPhone(contactPhone) || normalizePhone(contactPhone).length() > 20) {
            return "Contact phone number must be 10 to 15 digits and may start with +.";
        }
        error = validateText("Hospital name", hospitalName, 150);
        if (error != null) {
            return error;
        }
        error = validateRequiredDateTime(requiredBy, "Required by");
        if (error != null) {
            return error;
        }
        if (!trimToEmpty(formalityNotes).isEmpty() && trimToEmpty(formalityNotes).length() > 1000) {
            return "Formality notes must be at most 1000 characters.";
        }
        return null;
    }

    public static String validateCertificateIssue(String issuerType, String issuerName, String issuedOn) {
        if (!isValidIssuerType(issuerType)) {
            return "Please choose a valid certificate issuer type.";
        }
        String error = validateText("Issuer name", issuerName, 150);
        if (error != null) {
            return error;
        }
        return validateRequiredDate(issuedOn, "Issued on");
    }

    private static String validateText(String fieldName, String value, int maxLength) {
        String trimmed = trimToEmpty(value);
        if (trimmed.isEmpty()) {
            return fieldName + " is required.";
        }
        if (trimmed.length() > maxLength) {
            return fieldName + " must be at most " + maxLength + " characters.";
        }
        return null;
    }

    private static String validateOptionalDate(String value, String fieldName) {
        String trimmed = trimToEmpty(value);
        if (trimmed.isEmpty()) {
            return null;
        }
        try {
            LocalDate parsed = LocalDate.parse(trimmed);
            if (parsed.isAfter(LocalDate.now())) {
                return fieldName + " cannot be in the future.";
            }
            return null;
        } catch (DateTimeParseException ex) {
            return fieldName + " must be a valid date.";
        }
    }

    private static String validateRequiredDate(String value, String fieldName) {
        String trimmed = trimToEmpty(value);
        if (trimmed.isEmpty()) {
            return fieldName + " is required.";
        }
        return validateOptionalDate(trimmed, fieldName);
    }

    private static String validateRequiredDateTime(String value, String fieldName) {
        String trimmed = trimToEmpty(value);
        if (trimmed.isEmpty()) {
            return fieldName + " is required.";
        }
        try {
            if (java.time.LocalDateTime.parse(trimmed).isBefore(java.time.LocalDateTime.now().minusMinutes(1))) {
                return fieldName + " cannot be in the past.";
            }
            return null;
        } catch (DateTimeParseException ex) {
            return fieldName + " must be a valid date and time.";
        }
    }
}
