// Define global variables which can be used in all functions
var map, vectors;

// Function called from body tag
function init(){
    var lon = 5;
    var lat = 15;
    var zoom = 2;

    var context = {
        getColour: function(feature) {
            return feature.attributes["colour"];
        }
    };

    var template = {
		fillOpacity: 0.9,
        strokeColor: "#555555",
        strokeWidth: 1,
        fillColor: "${getColour}"
    };

    var style = new OpenLayers.Style(template, {context: context});
    var styleMap = new OpenLayers.StyleMap({'default': style});

	var options = {
        numZoomLevels: 6,
        controls: []  // Remove all controls
    };

    // Create a new map with options defined above
    map = new OpenLayers.Map( 'olmap', options );

    //OpenLayers.Feature.Vector.style['default']['strokeColor'] = "#000000";

    // Create polygon layer as vector features
    // http://dev.openlayers.org/docs/files/OpenLayers/Layer/Vector-js.html
    vectors = new OpenLayers.Layer.GML( "Internet users", "../data/json/internet_users_2005_choropleth_lowres.json",
                                        { format: OpenLayers.Format.GeoJSON,
                                          styleMap: styleMap,
                                          isBaseLayer: true,
                                          projection: new OpenLayers.Projection("EPSG:4326"),
                                          attribution: "<a href='http://data.un.org'>UN Data</a>" } );

    map.addLayer(vectors);
    map.setCenter(new OpenLayers.LonLat(lon, lat), zoom);



    // Add map controls: http://dev.openlayers.org/docs/files/OpenLayers/Control-js.html
    map.addControl(new OpenLayers.Control.LayerSwitcher());
    map.addControl(new OpenLayers.Control.MousePosition());
    map.addControl(new OpenLayers.Control.MouseDefaults());
    map.addControl(new OpenLayers.Control.PanZoomBar());

    var options = {
        hover: true,
        onSelect: serialize
    };

    var select = new OpenLayers.Control.SelectFeature(vectors, options);
    map.addControl(select);
    select.activate();

}



function serialize() {
    var Msg = vectors.selectedFeatures[0].attributes["name"] + ": ";
    Msg    += vectors.selectedFeatures[0].attributes["value"];
    document.getElementById("carte").innerHTML = Msg;
}
