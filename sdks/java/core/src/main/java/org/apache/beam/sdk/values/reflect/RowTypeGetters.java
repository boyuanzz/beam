/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.beam.sdk.values.reflect;

import java.util.List;
import org.apache.beam.sdk.schemas.Schema;
import org.apache.beam.sdk.values.Row;

/**
 * Helper class to hold {@link Schema} and {@link FieldValueGetter}s which were used to
 * create it.
 *
 * <p>This is used in {@link RowFactory} to create instances of {@link Row}s.
 */
class RowTypeGetters {
  private Schema schema;
  private List<FieldValueGetter> fieldValueGetters;

  RowTypeGetters(Schema schema, List<FieldValueGetter> fieldValueGetters) {
    this.schema = schema;
    this.fieldValueGetters = fieldValueGetters;
  }

  /**
   * Returns a {@link Schema}.
   */
  Schema rowType() {
    return schema;
  }

  /**
   * Returns the list of {@link FieldValueGetter}s which
   * were used to create {@link RowTypeGetters#rowType()}.
   */
  List<FieldValueGetter> valueGetters() {
    return fieldValueGetters;
  }
}
