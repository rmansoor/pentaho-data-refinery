/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2029-07-20
 ******************************************************************************/


package org.pentaho.di.core.refinery.model;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import org.pentaho.agilebi.modeler.ModelerException;

import java.io.IOException;
import java.util.Dictionary;
import java.util.Hashtable;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Created by rfellows on 11/24/15.
 */
@RunWith( MockitoJUnitRunner.class )
public class GeoContextBlueprintConfigProviderTest {

  GeoContextConfigProviderImpl geoContextProvider;

  @Before
  public void setUp() throws Exception {
    geoContextProvider = new GeoContextConfigProviderImpl();
  }

  @Test
  public void testGetDimensionName_noProps() throws Exception {
    geoContextProvider.props = new Hashtable<>();
    assertNull( geoContextProvider.getDimensionName() );
  }

  @Test
  public void testGetDimensionName() throws Exception {
    geoContextProvider.props.put( "geo.dimension.name", "DIM NAME" );

    assertEquals( "DIM NAME", geoContextProvider.getDimensionName() );
  }

  @Test
  public void testGetRoles_noProps() throws Exception {
    geoContextProvider.props = new Hashtable<>();
    assertNull( geoContextProvider.getRoles() );
  }

  @Test
  public void testGetRoles() throws Exception {
    geoContextProvider.props.put( "geo.roles", "country, state, city" );

    assertEquals( "country, state, city", geoContextProvider.getRoles() );
  }

  @Test
  public void testGetRoleAliases() throws Exception {
    geoContextProvider.props.put( "geo.country.aliases", "CTRY, CTR" );

    assertEquals( "CTRY, CTR", geoContextProvider.getRoleAliases( "country" ) );
  }

  @Test( expected = ModelerException.class )
  public void testGetRoleAliases_noMatch() throws Exception {
    geoContextProvider.getRoleAliases( "country" );
  }

  @Test
  public void testGetRoleRequirements() throws Exception {
    geoContextProvider.props.put( "geo.state.required-parents", "country" );

    assertEquals( "country", geoContextProvider.getRoleRequirements( "state" ) );
  }

  @Test
  public void testGetRoleRequirements_noMatch() throws Exception {
    assertNull( geoContextProvider.getRoleRequirements( "state" ) );
  }

  @Test
  public void testInitProps() throws Exception {
    Dictionary<String, Object> testProps = new Hashtable<>();
    testProps.put( "geo.roles", "country, state, city" );

    assertEquals( testProps, geoContextProvider.props );
  }

}