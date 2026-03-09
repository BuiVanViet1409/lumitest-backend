package com.lumitest.constant;

public class TestConstants {

    public static class Action {
        public static final String OPEN_URL = "goto";
        public static final String INPUT_TEXT = "type";
        public static final String CLICK = "click";
        public static final String WAIT = "wait";
        public static final String ASSERT_TEXT = "assert_text";
        public static final String SCREENSHOT = "screenshot";
        // New recorder actions
        public static final String SELECT_DROPDOWN = "select";
        public static final String CHECKBOX_TOGGLE = "checkbox_toggle";
        public static final String FILE_UPLOAD = "file_upload";

        // Verification Engine Actions
        public static final String VERIFY_TEXT = "verify_text";
        public static final String VERIFY_VISIBLE = "verify_visible";
        public static final String VERIFY_VALUE = "verify_value";
        public static final String VERIFY_URL_CONTAINS = "verify_url_contains";
        public static final String VERIFY_ELEMENT_EXISTS = "verify_element_exists";
    }

    public static class Status {
        public static final String PASS = "PASS";
        public static final String FAIL = "FAIL";
        public static final String RUNNING = "RUNNING";
        public static final String PASSED = "PASSED";
        public static final String FAILED = "FAILED";
    }
}
