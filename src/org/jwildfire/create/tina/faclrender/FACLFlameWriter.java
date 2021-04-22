/*
  JWildfire - an image and animation processor written in Java 
  Copyright (C) 1995-2021 Andreas Maschke

  This is free software; you can redistribute it and/or modify it under the terms of the GNU Lesser 
  General Public License as published by the Free Software Foundation; either version 2.1 of the 
  License, or (at your option) any later version.
 
  This software is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without 
  even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU 
  Lesser General Public License for more details.

  You should have received a copy of the GNU Lesser General Public License along with this software; 
  if not, write to the Free Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
  02110-1301 USA, or see the FSF site: http://www.fsf.org.
*/
package org.jwildfire.create.tina.faclrender;

import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

import com.kitfox.svg.A;
import org.jwildfire.base.Tools;
import org.jwildfire.create.tina.base.Flame;
import org.jwildfire.create.tina.base.Layer;
import org.jwildfire.create.tina.base.XForm;
import org.jwildfire.create.tina.io.AbstractFlameWriter;
import org.jwildfire.create.tina.io.SimpleXMLBuilder;
import org.jwildfire.create.tina.io.SimpleXMLBuilder.Attribute;
import org.jwildfire.create.tina.palette.RGBPalette;

public class FACLFlameWriter extends AbstractFlameWriter {

  public void writeFlame(Flame pFlame, String pFilename) throws Exception {
    writeFlame(getFlameXML(pFlame), pFilename);
  }

  public void writeFlame(String flameXML, String pFilename) throws Exception {
    Tools.writeUTF8Textfile(pFilename,flameXML);
  }

  public String getFlameXML(Flame pFlame) throws Exception {
    Flame transformedFlame = transformFlame(pFlame);
    SimpleXMLBuilder xb = new SimpleXMLBuilder();
    List<SimpleXMLBuilder.Attribute<?>> flamesAttrList = new ArrayList<>();
    flamesAttrList.add(new Attribute<String>("name", ""));

    xb.beginElement("Flames", flamesAttrList);

    // Flame
    List<SimpleXMLBuilder.Attribute<?>> attrList = filterFlameAttrList(createFlameAttributes(transformedFlame, xb));
    Layer layer = transformedFlame.getFirstLayer();
    String varSetUuid;
    Set<String> variationNames;
    if (FACLRenderTools.isExtendedFaclRender() && FACLRenderTools.isUsingCUDA()) {
      variationNames = extractVariationNames(transformedFlame);
      varSetUuid =
              UUID.nameUUIDFromBytes(
                      generateVariationsKey(variationNames).getBytes(StandardCharsets.UTF_8))
                      .toString().toUpperCase();
      attrList.add(new SimpleXMLBuilder.Attribute<String>("varset", varSetUuid));
    }
    else {
      varSetUuid = null;
      variationNames = null;
    }

    xb.beginElement("flame", attrList);
    // XForm
    for (XForm xForm : layer.getXForms()) {
      xb.emptyElement("xform", filterXFormAttrList( createXFormAttrList(xb, layer, xForm) ) );
    }
    // FinalXForms
    for (XForm xForm : layer.getFinalXForms()) {
      xb.emptyElement("finalxform", filterXFormAttrList(createXFormAttrList(xb, layer, xForm)));
    }
    // Gradient
    addGradient(xb, layer);
    xb.endElement("flame");
    // VariationSet
    if(varSetUuid!=null) {
      xb.beginElement("variationSet", createVariationSetAttrList(xb, transformedFlame, variationNames, varSetUuid));
      String cudaLibrary = FACLRenderTools.getCudaLibrary();
      for(String varname: variationNames) {
        boolean found = false;
        int startIdx = cudaLibrary.indexOf("<variation name=\""+varname+"\"");
        if(startIdx>0) {
          int endIdx = cudaLibrary.indexOf("</variation>", startIdx);
          if(endIdx>startIdx) {
            String varEntry = cudaLibrary.substring(startIdx, endIdx + 12);
            xb.addContent(varEntry);
            found = true;
          }
        }
        if(!found) {
          System.err.println("Did not find variation code for \"" + varname + "\"");
        }
      }
      xb.endElement("variationSet");
    }
    xb.endElement("Flames");
    return xb.buildXML();
  }

  private List<Attribute<?>> createVariationSetAttrList(SimpleXMLBuilder xb, Flame pFlame, Set<String> variationNames, String varSetUuid) {
    ArrayList<Attribute<?>> res = new ArrayList<>();
    res.add(xb.createAttr("name", "JWFVarSet_"+varSetUuid));
    res.add(xb.createAttr("version", "1.0"));
    res.add(xb.createAttr("defaultVariation", "linear"));
    res.add(xb.createAttr("is3Dcapable", "yes"));
    res.add(xb.createAttr("uuid", varSetUuid));
    res.add(xb.createAttr("structName", "varpar"));
    return res;
  }

  private String generateVariationsKey(Set<String> variationNames) {
    return variationNames.stream().sorted().collect(Collectors.joining("#"));
  }

  // remove unnecessary xform-attributes
  private final static Set<String> XFORM_ATTR_BLACKLIST = new HashSet<>(Arrays.asList("color_type", "material", "material_speed", "mod_gamma", "mod_gamma_speed",
          "mod_contrast", "mod_contrast_speed", "mod_saturation", "mod_saturation_speed", "mod_saturation_speed",
          "mod_hue", "mod_hue_speed"));

  private List<Attribute<?>> filterXFormAttrList(List<Attribute<?>> pSrc) {
    return pSrc.stream().filter(a ->  !a.getName().endsWith("fx_priority") && !a.getName().startsWith("wfield_") && !XFORM_ATTR_BLACKLIST.contains(a.getName())).collect(Collectors.toList());
  }

  // remove unnecessary flame-attributes
  private final static Set<String> FLAME_ATTR_BLACKLIST = new HashSet<>(Arrays.asList("smooth_gradient", "version", "filter_type", "filter_indicator",
          "filter_sharpness", "filter_low_density", "ai_post_denoiser", "post_optix_denoiser_blend", "background_type",
          "background_ul", "background_ur", "background_ll", "background_lr", "background_cc", "fg_opacity",
          "post_blur_radius", "post_blur_fade", "post_blur_falloff", "mixer_mode", "frame", "frame_count",
          "fps", "zbuffer_scale", "zbuffer_bias", "zbuffer_filename", "low_density_brightness", "balancing_red",
          "balancing_green", "balancing_blue"));
  private List<Attribute<?>> filterFlameAttrList(List<Attribute<?>> pSrc) {
    return pSrc.stream().filter(a -> !a.getName().startsWith("grad_edit_") && !a.getName().startsWith("cam_dof_") && !a.getName().startsWith("post_symmetry_") && !FLAME_ATTR_BLACKLIST.contains(a.getName())).collect(Collectors.toList());
  }

  // apply some FACLRender-specific changes
  private Flame transformFlame(Flame pFlame) {
    Flame flame = pFlame.makeCopy();
    flame.setPixelsPerUnit(flame.getPixelsPerUnit() * flame.getCamZoom());
    flame.setCamZoom(1.0);
    return flame;
  }

  private void addGradient(SimpleXMLBuilder xb, Layer layer) {
    RGBPalette palette = layer.getPalette();
    for (int i = 0; i < 256; i++) {
      List<SimpleXMLBuilder.Attribute<?>> attrList = new ArrayList<>();
      attrList.add(new Attribute<Integer>("index", i));
      String rgbStr = palette.getColor(i).getRed() + " " + palette.getColor(i).getGreen() + " " + palette.getColor(i).getBlue();
      attrList.add(new Attribute<String>("rgb", rgbStr));
      xb.simpleElement("color", null, attrList);
    }
  }

  private final static Set<String> MANDATORY_VARIATIONS = new HashSet<>(Arrays.asList("pre_matrix2d", "matrix2d", "post_matrix2d",
          "pre_matrix3d", "matrix3d", "post_matrix3d"));

  private static Set<String> extractVariationNames(Flame pFlame) {
    Set<String> res = new HashSet<>();
    pFlame.getFirstLayer().getXForms().forEach(xf -> {
      for(int i=0;i<xf.getVariationCount();i++) {
        res.add(xf.getVariation(i).getFunc().getName());
      }
    });
    pFlame.getFirstLayer().getFinalXForms().forEach(xf -> {
      for(int i=0;i<xf.getVariationCount();i++) {
        res.add(xf.getVariation(i).getFunc().getName());
      }
    });
    res.addAll(MANDATORY_VARIATIONS);
    return res;
  }

  
}
