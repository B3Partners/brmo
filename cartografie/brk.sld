<?xml version="1.0" encoding="ISO-8859-1"?>
<StyledLayerDescriptor version="1.0.0" 
                       xsi:schemaLocation="http://www.opengis.net/sld StyledLayerDescriptor.xsd" 
                       xmlns="http://www.opengis.net/sld" 
                       xmlns:ogc="http://www.opengis.net/ogc" 
                       xmlns:xlink="http://www.w3.org/1999/xlink" 
                       xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance">
  <NamedLayer>
    <Name>brk</Name>
    <UserStyle>
      <Title>generieke BRK stijl</Title>
      <FeatureTypeStyle>
        <Rule>
          <Title>perceelbegrenzing</Title>
          <!-- kadaster stelt dat BRK perceelbegrenzing geldig/nauwkeurig zijn tussen 1:1000 en 1:5000 -->
          <MinScaleDenominator>999</MinScaleDenominator>
          <MaxScaleDenominator>5001</MaxScaleDenominator>
          <PolygonSymbolizer>
            <Fill>
              <CssParameter name="fill">#888888</CssParameter>
              <CssParameter name="fill-opacity">.3</CssParameter>
            </Fill>
            <Stroke>
              <CssParameter name="stroke">#000000</CssParameter>
              <CssParameter name="stroke-width">0.4</CssParameter>
            </Stroke>
          </PolygonSymbolizer>
        </Rule>
        <Rule>
          <MinScaleDenominator>999</MinScaleDenominator>
          <MaxScaleDenominator>5001</MaxScaleDenominator>
          <TextSymbolizer>
            <Label>
              <!-- kadastrale aanduiding label met halo -->
              <ogc:PropertyName>ka_kad_gemeentecode</ogc:PropertyName>
              <ogc:PropertyName>ka_perceelnummer</ogc:PropertyName>
              <ogc:PropertyName>ka_sectie</ogc:PropertyName>
            </Label>
            <Halo>
              <Radius>2</Radius>
              <Fill>
                <CssParameter name="fill">#FFFFFF</CssParameter>
              </Fill>
            </Halo>
          </TextSymbolizer>
        </Rule>
      </FeatureTypeStyle>
    </UserStyle>
  </NamedLayer>
</StyledLayerDescriptor>
