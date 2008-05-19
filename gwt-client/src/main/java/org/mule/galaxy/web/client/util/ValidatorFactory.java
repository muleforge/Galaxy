package org.mule.galaxy.web.client.util;


public class ValidatorFactory {
    public final static Validator createPhonenumberValidator() {
        return new Validator() {
            private final String PATTERN = "\\(?[0-9]{3}\\)?[-. ]?[0-9]{3}[-. ]?[0-9]{4}";

            public String getErrorMessage() {
                return "Invalid PhoneNumber";
            }

            public boolean isInputValid(String input) {
                return PATTERN.matches(input);
            }

        };
    }

    public final static Validator createEmailValidator() {
        return new Validator() {
            private final String PATTERN = "^[A-Z0-9._%-]+@[A-Z0-9.-]+\\.[A-Z] {2,4}$";

            public String getErrorMessage() {
                return "Invalid email address.";
            }

            public boolean isInputValid(String input) {
                return PATTERN.matches(input);
            }
        };
    }

    public final static Validator createMinLengthValidator(int
            minLength_, String fieldName_) {
        final int minLength = minLength_;
        final String fieldName = fieldName_;
        return new Validator() {
            public String getErrorMessage() {
                return fieldName + " can not be less than " + minLength + "characters.";
            }

            public boolean isInputValid(String input) {
                return (input.length() >= minLength);
            }
        };
    }

    public final static Validator createMaxLengthValidator(int
            maxLength_, String fieldName_) {
        final int maxLength = maxLength_;
        final String fieldName = fieldName_;
        return new Validator() {
            public String getErrorMessage() {
                return fieldName + " can not be more than " + maxLength + "characters.";
            }

            public boolean isInputValid(String input) {
                return (input.length() <= maxLength);
            }
        };
    }
}
