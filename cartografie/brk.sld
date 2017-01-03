<?xml version="1.0" encoding="UTF-8"?>
<StyledLayerDescriptor version="1.0.0" 
                       xsi:schemaLocation="http://www.opengis.net/sld StyledLayerDescriptor.xsd" 
                       xmlns="http://www.opengis.net/sld" 
                       xmlns:ogc="http://www.opengis.net/ogc" 
                       xmlns:xlink="http://www.w3.org/1999/xlink" 
                       xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance">
  <NamedLayer>
    <Name>BRK</Name>
    <UserStyle>
      <FeatureTypeStyle>
        <Rule>
          <Title>perceelbegrenzing</Title>
          <!-- 
              kadaster stelt dat BRK perceelbegrenzing geldig/nauwkeurig zijn tussen 1:1000 en 1:5000
              maar we gebruiken 1:500 - 1:10000
          -->
          <MinScaleDenominator>499</MinScaleDenominator>
          <MaxScaleDenominator>10001</MaxScaleDenominator>
          <PolygonSymbolizer>
            <Fill>
              <CssParameter name="fill">#888888</CssParameter>
              <CssParameter name="fill-opacity">.3</CssParameter>
            </Fill>
            <Stroke>
              <CssParameter name="stroke">#000000</CssParameter>
              <CssParameter name="stroke-width">0.3</CssParameter>
            </Stroke>
          </PolygonSymbolizer>
        </Rule>
        <Rule>
          <MinScaleDenominator>499</MinScaleDenominator>
          <MaxScaleDenominator>5001</MaxScaleDenominator>
          <!-- voor eigenarenkaart alleen percelen labelen 
          <ogc:Filter>
            <ogc:PropertyIsEqualTo>
              <ogc:PropertyName>type</ogc:PropertyName>
              <ogc:Literal>perceel</ogc:Literal>
            </ogc:PropertyIsEqualTo>
          </ogc:Filter>
          -->
          <TextSymbolizer>
            <Label>
              <!-- kadastrale aanduiding label met halo -->
              <ogc:PropertyName>ka_kad_gemeentecode</ogc:PropertyName>
              <ogc:PropertyName>ka_sectie</ogc:PropertyName><ogc:PropertyName>ka_perceelnummer</ogc:PropertyName>
            </Label>
            <Font>
              <CssParameter name="font-size">10</CssParameter>
              <CssParameter name="font-style">normal</CssParameter>
            </Font>
            <Halo>
              <Radius>2</Radius>
              <Fill>
                <CssParameter name="fill">#FFFFFF</CssParameter>
                <CssParameter name="fill-opacity">.7</CssParameter>
              </Fill>
            </Halo>
          </TextSymbolizer>
        </Rule>
      </FeatureTypeStyle>
    </UserStyle>
  </NamedLayer>
</StyledLayerDescriptor>
