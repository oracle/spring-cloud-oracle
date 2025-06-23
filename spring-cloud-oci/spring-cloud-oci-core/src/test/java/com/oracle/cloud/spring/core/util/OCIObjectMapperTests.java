/*
 ** Copyright (c) 2023, 2025, Oracle and/or its affiliates.
 ** Licensed under the Universal Permissive License v 1.0 as shown at https://oss.oracle.com/licenses/upl/
 */

package com.oracle.cloud.spring.core.util;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.jupiter.api.Test;


import static org.junit.jupiter.api.Assertions.*;

class OCIObjectMapperTests {

    @Test
    void testObjectMapper() throws JSONException {
        TestJsonObject inputJson = new TestJsonObject(12, "fee", "foo");
        String jsonToStr = OCIObjectMapper.toPrintableString(inputJson);
        JSONObject strToJson = new JSONObject(jsonToStr);

        assertEquals(strToJson.get("id"), inputJson.getId());
        assertEquals(strToJson.getString("firstName"), inputJson.getFirstName());
        assertEquals(strToJson.getString("lastName"), inputJson.getLastName());
    }

    static final class TestJsonObject {
        public TestJsonObject() {}

        public TestJsonObject(int id, String firstName, String lastName) {
            this.id = id;
            this.firstName = firstName;
            this.lastName = lastName;
        }

        private int id;
        private String firstName;
        private String lastName;

        public int getId() {
            return id;
        }

        public void setId(int id) {
            this.id = id;
        }

        public String getFirstName() {
            return firstName;
        }

        public void setFirstName(String firstName) {
            this.firstName = firstName;
        }

        public String getLastName() {
            return lastName;
        }

        public void setLastName(String lastName) {
            this.lastName = lastName;
        }

        @Override
        public String toString() {
            return "TestJsonObject{" +
                    "id=" + id +
                    ", firstName='" + firstName + '\'' +
                    ", lastName='" + lastName + '\'' +
                    '}';
        }
    }
}
