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

package com.oracle.database.spring.cloud.stream.binder.utils;

import java.nio.ByteBuffer;
import java.util.Base64;
import java.util.UUID;

public class Base64UrlNamingStrategy implements AnonymousNamingStrategy {

  private String prefix = "spring.gen-";

  public Base64UrlNamingStrategy() {}

  public Base64UrlNamingStrategy(String prefix) {
    this.prefix = prefix;
  }

  @Override
  public String generateName() {
    return generateName(this.prefix);
  }

  @Override
  public String generateName(String prefix) {
    UUID uuid = UUID.randomUUID();
    ByteBuffer bb = ByteBuffer.wrap(new byte[16]);
    bb
      .putLong(uuid.getMostSignificantBits())
      .putLong(uuid.getLeastSignificantBits());
    // Convert to base64 and remove trailing =
    Base64.Encoder encoder = Base64.getUrlEncoder().withoutPadding();
    return (
      prefix +
      encoder.encodeToString(bb.array()).replace("=", "").replace("-", "\\$")
    );
  }
}
