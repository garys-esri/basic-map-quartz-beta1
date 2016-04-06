/*******************************************************************************
 * Copyright 2015-2016 Esri
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 ******************************************************************************/
package com.esri.defensese.basicmapquartz;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import com.esri.arcgisruntime.ArcGISRuntimeEnvironment;
import com.esri.arcgisruntime.datasource.arcgis.ArcGISFeatureTable;
import com.esri.arcgisruntime.datasource.arcgis.ServiceFeatureTable;
import com.esri.arcgisruntime.geometry.Envelope;
import com.esri.arcgisruntime.geometry.EnvelopeBuilder;
import com.esri.arcgisruntime.layers.FeatureLayer;
import com.esri.arcgisruntime.loadable.LoadStatus;
import com.esri.arcgisruntime.loadable.LoadStatusChangedEvent;
import com.esri.arcgisruntime.loadable.LoadStatusChangedListener;
import com.esri.arcgisruntime.mapping.Basemap;
import com.esri.arcgisruntime.mapping.Map;
import com.esri.arcgisruntime.mapping.view.MapView;
import com.esri.arcgisruntime.security.AuthenticationManager;
import com.esri.arcgisruntime.security.DefaultAuthenticationChallengeHandler;

/**
 * An app that displays a map with a feature service, demonstrating they key themes
 * of ArcGIS Runtime Quartz.
 */
public class BasicMapQuartzActivity extends AppCompatActivity {

    private MapView mapView = null;
    private Map map = null;
    private TextView layerStatusLabel = null;
    private TextView srLabel = null;

    /**
     * Creates the UI and creates and displays the map.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ArcGISRuntimeEnvironment.setClientId(getString(R.string.client_id));

        super.onCreate(savedInstanceState);
        setContentView(R.layout.content_basic_map_quartz);

        layerStatusLabel = (TextView) findViewById(R.id.textView_layerStatus);
        srLabel = (TextView) findViewById(R.id.textView_spatialReference);

        /**
         * *********************************************************************
         * New in Beta 1: Centralized handling of authentication
         */
        String featureServiceUrl = "https://services1.arcgis.com/63cSRCcqLtJKDSR2/arcgis/rest/services/nhsvc_sites/FeatureServer/0";
        String definitionExpression = "Name LIKE '%Sa%'";
        AuthenticationManager.setAuthenticationChallengeHandler(new DefaultAuthenticationChallengeHandler(this));

        /**
         * *********************************************************************
         * New in Beta 1: Runtime common API
         * New in Beta 1: New mapping API
         */
        mapView = (MapView) findViewById(R.id.mapView);

        this.map = new Map();
        map.setBasemap(Basemap.createTopographic());

        ArcGISFeatureTable featureTable = new ServiceFeatureTable(featureServiceUrl);
        final FeatureLayer featureLayer = new FeatureLayer(featureTable);
        featureLayer.setDefinitionExpression(definitionExpression);
        map.getOperationalLayers().add(featureLayer);

        /**
         * *********************************************************************
         * New in Beta 1: Loadable pattern
         */
        layerStatusLabel.setText("Layer: " + LoadStatus.NOT_LOADED);
        featureLayer.addLoadStatusChangedListener(new LoadStatusChangedListener() {
            @Override
            public void loadStatusChanged(LoadStatusChangedEvent evt) {
                LoadStatus newLoadStatus = evt.getNewLoadStatus();
                layerStatusLabel.setText("Layer: " + newLoadStatus.toString());
                if (LoadStatus.LOADED.equals(newLoadStatus)) {
                    Envelope fullExtent = featureLayer.getFullExtent();

                    /**
                     * *********************************************************
                     * New in Beta 1: Geometry objects
                     */
                    EnvelopeBuilder envBuilder = new EnvelopeBuilder(fullExtent);
                    double halfWidth = envBuilder.getWidth() / 2.0;
                    double halfHeight = envBuilder.getHeight() / 2.0;
                    envBuilder.setXMin(envBuilder.getXMin() - halfWidth);
                    envBuilder.setXMax(envBuilder.getXMax() + halfWidth);
                    envBuilder.setYMin(envBuilder.getYMin() - halfHeight);
                    envBuilder.setYMax(envBuilder.getYMax() + halfHeight);
                    fullExtent = envBuilder.toGeometry();
                    Log.i(getClass().getSimpleName(),
                            "By the way, the spatial reference of this geometry is "
                                    + fullExtent.getSpatialReference().getWKText()
                                    + ". Try doing THAT with ArcGIS Runtime 10.2.x!");

                    mapView.setViewpointGeometryAsync(fullExtent);
                }
            }
        });
        srLabel.setText("SR: " + getSpatialReferenceString(map));
        map.addDoneLoadingListener(new Runnable() {
            public void run() {
                srLabel.setText("SR: " + getSpatialReferenceString(map));
            }
        });

        mapView.setMap(map);
    }

    private static String getSpatialReferenceString(Map map) {
        return (null == map.getSpatialReference() ? "null" : Integer.toString(map.getSpatialReference().getWKID()));
    }

    /**
     * Pauses the MapView and then calls super.onPause().
     */
    @Override
    protected void onPause(){
        if (null != mapView) {
            mapView.pause();
        }
        super.onPause();
    }

    /**
     * Calls super.onResume() and then resumes the MapView.
     */
    @Override
    protected void onResume(){
        super.onResume();
        if (null != mapView) {
            mapView.resume();
        }
    }

    /**
     * Inflates the options menu.
     * @param menu the Menu.
     * @return true on success.
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_basic_map_quartz, menu);
        return true;
    }

    /**
     * Handles the selection of an options menu item.
     * @param item the selected MenuItem.
     * @return true on success.
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}