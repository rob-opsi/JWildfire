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

import org.jwildfire.base.Tools;
import org.jwildfire.create.tina.base.Layer;
import org.jwildfire.create.tina.base.XForm;
import org.jwildfire.create.tina.base.XYZPoint;

import static org.jwildfire.base.mathlib.MathLib.*;

public class Julia3DQFunc extends VariationFunc implements SupportsGPU {
  private static final long serialVersionUID = 1L;

  private static final String PARAM_POWER = "power";
  private static final String PARAM_DIVISOR = "divisor";
  private static final String[] paramNames = {PARAM_POWER, PARAM_DIVISOR};

  private int power = genRandomPower();
  private int divisor = 2;

  @Override
  public void transform(FlameTransformationContext pContext, XForm pXForm, XYZPoint pAffineTP, XYZPoint pVarTP, double pAmount) {
    // julia3Dq by Zueuk, http://zueuk.deviantart.com/art/juliaq-Apophysis-plugins-340813357
    double a = atan2(pAffineTP.y, pAffineTP.x) * inv_power + pContext.random(Integer.MAX_VALUE) * inv_power_2pi;
    double sina = sin(a);
    double cosa = cos(a);
    double z = pAffineTP.z * abs_inv_power;
    double r2d = sqr(pAffineTP.x) + sqr(pAffineTP.y);
    double r = pAmount * pow(r2d + sqr(z), half_inv_power);
    pVarTP.z += r * z;
    r *= sqrt(r2d);
    pVarTP.x += r * cosa;
    pVarTP.y += r * sina;
    if (pContext.isPreserveZCoordinate()) {
      pVarTP.z += pAmount * pAffineTP.z;
    }
  }

  @Override
  public String[] getParameterNames() {
    return paramNames;
  }

  @Override
  public Object[] getParameterValues() {
    return new Object[]{power, divisor};
  }

  @Override
  public void setParameter(String pName, double pValue) {
    if (PARAM_POWER.equalsIgnoreCase(pName)) {
      power = Tools.FTOI(pValue);
    } else if (PARAM_DIVISOR.equalsIgnoreCase(pName))
      divisor = Tools.FTOI(pValue);
    else
      throw new IllegalArgumentException(pName);
  }

  @Override
  public String getName() {
    return "julia3Dq";
  }

  private int genRandomPower() {
    int res = (int) (Math.random() * 5.0 + 2.5);
    return Math.random() < 0.5 ? res : -res;
  }

  private double inv_power, abs_inv_power, half_inv_power, inv_power_2pi;

  @Override
  public void init(FlameTransformationContext pContext, Layer pLayer, XForm pXForm, double pAmount) {
    inv_power = (double) divisor / (double) power;
    abs_inv_power = fabs(inv_power);
    half_inv_power = 0.5 * inv_power - 0.5;
    inv_power_2pi = M_2PI / (double) power;
  }

  @Override
  public VariationFuncType[] getVariationTypes() {
    return new VariationFuncType[]{VariationFuncType.VARTYPE_3D, VariationFuncType.VARTYPE_SUPPORTS_GPU};
  }

  @Override
  public String getGPUCode(FlameTransformationContext context) {
    return "int power = lroundf(__julia3Dq_power);\n"
           + "int divisor = lroundf(__julia3Dq_divisor);\n"
           +"float inv_power, abs_inv_power, half_inv_power, inv_power_2pi;\n"
        + "inv_power = (float) divisor / (float) power;\n"
        + "    abs_inv_power = fabsf(inv_power);\n"
        + "    half_inv_power = 0.5 * inv_power - 0.5;\n"
        + "    inv_power_2pi = (2.0f*PI) / (float) power;\n"
        + "    float a = atan2f(__y, __x) * inv_power + (int)(RANDFLOAT() * 0x0001ffff) * inv_power_2pi;\n"
        + "    float sina = sinf(a);\n"
        + "    float cosa = cosf(a);\n"
        + "    float z = __z * abs_inv_power;\n"
        + "    float r2d = __x*__x + __y*__y;\n"
        + "    float r = __julia3Dq * powf(r2d + z*z, half_inv_power);\n"
        + "    __pz += r * z;\n"
        + "    r *= sqrtf(r2d);\n"
        + "    __px += r * cosa;\n"
        + "    __py += r * sina;\n"
        + (context.isPreserveZCoordinate() ? "      __pz += __julia3Dq * __z;\n" : "");
  }}
