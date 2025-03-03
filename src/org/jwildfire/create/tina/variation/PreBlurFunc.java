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

public class PreBlurFunc extends SimpleVariationFunc implements SupportsGPU {
  private static final long serialVersionUID = 1L;

  private final double gauss_rnd[] = new double[6];
  private int gauss_N;

  @Override
  public void transform(FlameTransformationContext pContext, XForm pXForm, XYZPoint pAffineTP, XYZPoint pVarTP, double pAmount) {
    double r = pContext.random() * 2 * M_PI;
    double sina = sin(r);
    double cosa = cos(r);
    r = pAmount * (gauss_rnd[0] + gauss_rnd[1] + gauss_rnd[2] + gauss_rnd[3] + gauss_rnd[4] + gauss_rnd[5] - 3);
    gauss_rnd[gauss_N] = pContext.random();
    gauss_N = (gauss_N + 1) & 5;
    pAffineTP.x += r * cosa;
    pAffineTP.y += r * sina;
  }

  @Override
  public String getName() {
    return "pre_blur";
  }

  @Override
  public void init(FlameTransformationContext pContext, Layer pLayer, XForm pXForm, double pAmount) {
    gauss_rnd[0] = pContext.random();
    gauss_rnd[1] = pContext.random();
    gauss_rnd[2] = pContext.random();
    gauss_rnd[3] = pContext.random();
    gauss_rnd[4] = pContext.random();
    gauss_rnd[5] = pContext.random();
    gauss_N = 0;
  }

  @Override
  public int getPriority() {
    return -1;
  }

  @Override
  public VariationFuncType[] getVariationTypes() {
    return new VariationFuncType[]{VariationFuncType.VARTYPE_2D, VariationFuncType.VARTYPE_BLUR, VariationFuncType.VARTYPE_PRE, VariationFuncType.VARTYPE_SUPPORTS_GPU};
  }

  @Override
  public String getGPUCode(FlameTransformationContext context) {
    // based on code from the cudaLibrary.xml compilation, created by Steven Brodhead Sr.
    return "float rndG = __pre_blur*(RANDFLOAT()+RANDFLOAT()+RANDFLOAT()+RANDFLOAT()+RANDFLOAT()+RANDFLOAT()-3.f);\n"
        + "float rndA = RANDFLOAT()*2.f*PI;\n"
        + "__x += rndG*cosf(rndA);\n"
        + "__y += rndG*sinf(rndA);\n"
        + "__r2 = __x*__x+__y*__y;\n"
        + "__r = sqrtf(__r2);\n"
        + "__rinv = 1.f/__r;\n"
        + "__phi = atan2f(__x,__y);\n"
        + "__theta = .5f*PI-__phi;\n"
        + "if (__theta > PI)\n"
        + "    __theta -= 2.f*PI;";
  }
}
