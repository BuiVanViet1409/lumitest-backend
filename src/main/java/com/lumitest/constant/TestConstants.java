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

        // API Testing Actions
        public static final String API_GET = "api_get";
        public static final String API_POST = "api_post";
        public static final String API_PUT = "api_put";
        public static final String API_DELETE = "api_delete";
    }

    public static class Status {
        public static final String NOT_RUN = "NOT_RUN";
        public static final String PASSED = "PASSED";
        public static final String FAILED = "FAILED";
        public static final String BLOCKED = "BLOCKED";

        // Legacy support (to be removed later)
        public static final String PASS = "PASSED";
        public static final String FAIL = "FAILED";
        public static final String RUNNING = "RUNNING";
    }

    public static class VerificationType {
        public static final String UI = "UI";
        public static final String API = "API";
        public static final String DATABASE = "DATABASE";
        public static final String MESSAGE = "MESSAGE";
    }
}
