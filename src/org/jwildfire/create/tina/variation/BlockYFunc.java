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
package org.jwildfire.create.tina.variation;

import org.jwildfire.create.tina.base.Layer;
import org.jwildfire.create.tina.base.XForm;
import org.jwildfire.create.tina.base.XYZPoint;

import static org.jwildfire.base.mathlib.MathLib.*;

public class BlockYFunc extends VariationFunc implements SupportsGPU {
  private static final long serialVersionUID = 1L;

  private static final String PARAM_X = "x";
  private static final String PARAM_Y = "y";
  private static final String PARAM_MP = "mp";
  private static final String[] paramNames = {PARAM_X, PARAM_Y, PARAM_MP};

  private double x = 1.0;
  private double y = 1.0;
  private double mp = 4.0;

  @Override
  public void transform(FlameTransformationContext pContext, XForm pXForm, XYZPoint pAffineTP, XYZPoint pVarTP, double pAmount) {
    /* blocky from FracFx, http://fracfx.deviantart.com/art/FracFx-Plugin-Pack-171806681 */
    double T, r;
    T = ((cos(pAffineTP.x) + cos(pAffineTP.y)) / mp + 1.0);
    r = pAmount / T;
    double tmp = sqr(pAffineTP.y) + sqr(pAffineTP.x) + 1.0;
    double x2 = 2.0 * pAffineTP.x;
    double y2 = 2.0 * pAffineTP.y;
    double xmax = 0.5 * (sqrt(tmp + x2) + sqrt(tmp - x2));
    double ymax = 0.5 * (sqrt(tmp + y2) + sqrt(tmp - y2));
    double a = pAffineTP.x / xmax;
    double b = sqrt_safe(1.0 - sqr(a));
    pVarTP.x += this.v * atan2(a, b) * r * this.x;

    a = pAffineTP.y / ymax;
    b = sqrt_safe(1.0 - sqr(a));
    pVarTP.y += this.v * atan2(a, b) * r * this.y;
    if (pContext.isPreserveZCoordinate()) {
      pVarTP.z += pAmount * pAffineTP.z;
    }
  }

  private double sqrt_safe(double x) {
    if (x < 0.0)
      return 0.0;
    return sqrt(x);
  }

  @Override
  public String[] getParameterNames() {
    return paramNames;
  }

  @Override
  public Object[] getParameterValues() {
    return new Object[]{x, y, mp};
  }

  @Override
  public void setParameter(String pName, double pValue) {
    if (PARAM_X.equalsIgnoreCase(pName))
      x = pValue;
    else if (PARAM_Y.equalsIgnoreCase(pName))
      y = pValue;
    else if (PARAM_MP.equalsIgnoreCase(pName))
      mp = pValue;
    else
      throw new IllegalArgumentException(pName);
  }

  @Override
  public String getName() {
    return "blocky";
  }

  private double v;

  @Override
  public void init(FlameTransformationContext pContext, Layer pLayer, XForm pXForm, double pAmount) {
    v = pAmount / M_PI_2;
  }

  @Override
  public VariationFuncType[] getVariationTypes() {
    return new VariationFuncType[]{VariationFuncType.VARTYPE_2D, VariationFuncType.VARTYPE_SUPPORTS_GPU};
  }

  @Override
  public String getGPUCode(FlameTransformationContext context) {
    return "float v = __blocky / (PI*0.5f);\n"
        + "    float T, r;\n"
        + "    T = ((cosf(__x) + cosf(__y)) / __blocky_mp + 1.0);\n"
        + "    r = __blocky / T;\n"
        + "    float tmp = __y*__y + __x*__x + 1.0;\n"
        + "    float x2 = 2.0 * __x;\n"
        + "    float y2 = 2.0 * __y;\n"
        + "    float xmax = 0.5 * (sqrtf(tmp + x2) + sqrtf(tmp - x2));\n"
        + "    float ymax = 0.5 * (sqrtf(tmp + y2) + sqrtf(tmp - y2));\n"
        + "    float a = __x / xmax;\n"
        + "    float b = sqrtf_safe(1.0 - a*a);\n"
        + "    __px += v * atan2f(a, b) * r * __blocky_x;\n"
        + "\n"
        + "    a = __y / ymax;\n"
        + "    b = sqrtf_safe(1.0 - a*a);\n"
        + "    __py += v * atan2f(a, b) * r * __blocky_y;\n"
        + (context.isPreserveZCoordinate() ? "__pz += __blocky * __z;\n" : "");
  }
}
