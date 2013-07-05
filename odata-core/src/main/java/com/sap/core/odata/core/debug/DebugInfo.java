/*******************************************************************************
 * Copyright 2013 SAP AG
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package com.sap.core.odata.core.debug;

import java.io.IOException;

import com.sap.core.odata.core.ep.util.JsonStreamWriter;

/**
 * @author SAP AG
 */
public interface DebugInfo {

  /**
   * Gets the name of this debug information part, useful as title.
   * @return the name
   */
  public String getName();

  /**
   * Appends the content of this debug information part
   * to the given JSON stream writer.
   * @param jsonStreamWriter a JSON stream writer
   */
  public void appendJson(JsonStreamWriter jsonStreamWriter) throws IOException;
}
