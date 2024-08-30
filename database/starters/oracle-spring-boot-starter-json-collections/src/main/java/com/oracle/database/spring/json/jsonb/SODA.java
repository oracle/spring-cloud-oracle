// Copyright (c) 2024, Oracle and/or its affiliates.
// Licensed under the Universal Permissive License v 1.0 as shown at https://oss.oracle.com/licenses/upl.
package com.oracle.database.spring.json.jsonb;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import jakarta.json.stream.JsonGenerator;
import jakarta.json.stream.JsonParser;
import oracle.soda.OracleDatabase;
import oracle.soda.OracleDocument;
import oracle.soda.OracleException;
import oracle.sql.json.OracleJsonFactory;
import org.eclipse.yasson.YassonJsonb;

public class SODA {
    private final OracleJsonFactory oracleJsonFactory;
    private final YassonJsonb jsonb;

    public SODA(OracleJsonFactory oracleJsonFactory, YassonJsonb jsonb) {
        this.oracleJsonFactory = oracleJsonFactory;
        this.jsonb = jsonb;
    }

    public <T> T fromDocument(OracleDocument document, Class<T> clazz) throws OracleException {
        JsonParser parser = document.getContentAs(JsonParser.class);
        return jsonb.fromJson(parser, clazz);
    }

    public OracleDocument toDocument(OracleDatabase oracleDatabase, Object object) throws OracleException {
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            JsonGenerator gen = oracleJsonFactory.createJsonBinaryGenerator(outputStream)
                    .wrap(JsonGenerator.class);
            jsonb.toJson(object, gen);
            gen.close();
            return oracleDatabase.createDocumentFrom(outputStream.toByteArray());
        } catch (IOException e) {
            throw new OracleException(e);
        }
    }
}
