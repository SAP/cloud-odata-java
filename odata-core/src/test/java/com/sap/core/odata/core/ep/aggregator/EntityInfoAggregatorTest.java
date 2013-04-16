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
package com.sap.core.odata.core.ep.aggregator;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import com.sap.core.odata.api.edm.EdmEntitySet;
import com.sap.core.odata.api.edm.EdmTypeKind;
import com.sap.core.odata.api.uri.ExpandSelectTreeNode;
import com.sap.core.odata.core.ep.AbstractProviderTest;
import com.sap.core.odata.testutil.mock.MockFacade;

/**
 * @author SAP AG
 */
public class EntityInfoAggregatorTest extends AbstractProviderTest {

  public EntityInfoAggregatorTest(final StreamWriterImplType type) {
    super(type);
  }

  @Test
  public void testEntitySet() throws Exception {
    EdmEntitySet entitySet = MockFacade.getMockEdm().getDefaultEntityContainer().getEntitySet("Employees");

    ExpandSelectTreeNode epProperties = null;
    EntityInfoAggregator eia = EntityInfoAggregator.create(entitySet, epProperties);

    assertNotNull(eia);
    EntityPropertyInfo propertyInfoAge = eia.getPropertyInfo("Age");
    assertFalse(propertyInfoAge.isComplex());
    assertEquals("Age", propertyInfoAge.getName());
    assertEquals("Int32", propertyInfoAge.getType().getName());
    EntityPropertyInfo propertyInfoLocation = eia.getPropertyInfo("Location");
    assertTrue(propertyInfoLocation.isComplex());
    assertNull(eia.getPropertyInfo("City"));
    assertEquals("Location", propertyInfoLocation.getName());
    EntityComplexPropertyInfo locationInfo = (EntityComplexPropertyInfo) propertyInfoLocation;
    assertEquals(2, locationInfo.getPropertyInfos().size());

    assertEquals("Country", locationInfo.getPropertyInfo("Country").getName());
    assertEquals("String", locationInfo.getPropertyInfo("Country").getType().getName());
    assertEquals(EdmTypeKind.SIMPLE, locationInfo.getPropertyInfo("Country").getType().getKind());

    EntityComplexPropertyInfo cityInfo = (EntityComplexPropertyInfo) locationInfo.getPropertyInfo("City");
    assertTrue(cityInfo.isComplex());
    assertEquals("City", cityInfo.getName());
    assertEquals("c_City", cityInfo.getType().getName());
    assertEquals(EdmTypeKind.COMPLEX, cityInfo.getType().getKind());
    assertEquals("CityName", cityInfo.getPropertyInfo("CityName").getName());
    assertFalse(cityInfo.getPropertyInfo("CityName").isComplex());
    assertEquals("String", cityInfo.getPropertyInfo("CityName").getType().getName());
    assertEquals("PostalCode", cityInfo.getPropertyInfo("PostalCode").getName());
    assertFalse(cityInfo.getPropertyInfo("PostalCode").isComplex());
    assertEquals("String", cityInfo.getPropertyInfo("PostalCode").getType().getName());
  }
}
