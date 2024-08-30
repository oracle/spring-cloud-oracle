/*
 ** TxEventQ Support for Spring Cloud Stream
 ** Copyright (c) 2023, 2024 Oracle and/or its affiliates.
 **
 ** This file has been modified by Oracle Corporation.
 */

/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.oracle.database.cstream.plsql;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Types;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import oracle.ucp.jdbc.PoolDataSource;

public class OracleDBUtils {

    private PoolDataSource pds = null;
    private final int dbversion;
    private final Logger logger = LoggerFactory.getLogger(getClass());

    private static final String CREATE_KB2_TEQ =
            "BEGIN "
                    + "dbms_aqadm.create_transactional_event_queue(?, multiple_consumers => true);"
                    + "dbms_aqadm.set_queue_parameter(?, 'KEY_BASED_ENQUEUE', 2); "
                    + "dbms_aqadm.set_queue_parameter(?, 'SHARD_NUM', ?); "
                    + "dbms_aqadm.start_queue(?); "
                    + "END;";

    private static final String CREATE_KB1_TEQ =
            "BEGIN "
                    + "dbms_aqadm.create_sharded_queue(?, multiple_consumers => true);"
                    + "dbms_aqadm.set_queue_parameter(?, 'KEY_BASED_ENQUEUE', 1); "
                    + "dbms_aqadm.set_queue_parameter(?, 'SHARD_NUM', ?); "
                    + "FOR i in 0..?-1 "
                    + "LOOP "
                    + "dbms_aqadm.set_queue_parameter(?, 'AQ$KEY_TO_SHARD_MAP='||i, i*2); "
                    + "END LOOP; "
                    + "dbms_aqadm.start_queue(?); "
                    + "END;";

    private static final String GET_PARTITION_COUNT =
            "BEGIN "
                    + "dbms_aqadm.get_queue_parameter(?, 'SHARD_NUM', ?);"
                    + "END;";

    public OracleDBUtils(PoolDataSource pds, int dbversion) {
        this.pds = pds;
        if (dbversion < 19) {
            logger.error("DB version: {} not supported.", dbversion);
            throw new IllegalArgumentException("The TxEventQ Binder is compatible with database versions >= 19. The current database version is: " + dbversion);
        }
        this.dbversion = dbversion;
    }

    public int getDBVersion() {
        return this.dbversion;
    }

    public void createKBQ(String qname, int pCount) throws SQLException {
        CallableStatement cstmt = null;
        try (Connection conn = this.pds.getConnection()) {
            if (this.dbversion != 19) {
                /* For DB Versions > 19 */
                cstmt = conn.prepareCall(CREATE_KB2_TEQ);
                cstmt.setString(1, qname);
                cstmt.setString(2, qname);
                cstmt.setString(3, qname);
                cstmt.setInt(4, pCount);
                cstmt.setString(5, qname);
            } else {
                /* For DB Version 19*/
                cstmt = conn.prepareCall(CREATE_KB1_TEQ);
                cstmt.setString(1, qname);
                cstmt.setString(2, qname);
                cstmt.setString(3, qname);
                cstmt.setInt(4, pCount);
                cstmt.setInt(5, pCount);
                cstmt.setString(6, qname);
                cstmt.setString(7, qname);
            }
            cstmt.execute();
        } finally {
            try {
                if (cstmt != null)
                    cstmt.close();
            } catch (SQLException e) {
                logger.error("Error while closing callable statement.");
            }
        }
    }

    public int getTopicPartitions(String qname) throws SQLException {
        CallableStatement cstmt = null;
        try (Connection conn = pds.getConnection()) {
            cstmt = conn.prepareCall(GET_PARTITION_COUNT);
            cstmt.setString(1, qname);
            cstmt.registerOutParameter(2, Types.INTEGER);
            cstmt.execute();
            return cstmt.getInt(2);
        } finally {
            try {
                if (cstmt != null)
                    cstmt.close();
            } catch (SQLException e) {
                logger.error("Error while closing callable statement.");
            }
        }
    }
}
