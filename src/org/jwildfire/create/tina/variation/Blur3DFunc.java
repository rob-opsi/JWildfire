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

import org.jwildfire.base.mathlib.DoubleWrapperWF;
import org.jwildfire.create.tina.base.Layer;
import org.jwildfire.create.tina.base.XForm;
import org.jwildfire.create.tina.base.XYZPoint;

import static org.jwildfire.base.mathlib.MathLib.M_PI;
import static org.jwildfire.base.mathlib.MathLib.sinAndCos;

public class Blur3DFunc extends SimpleVariationFunc implements SupportsGPU {
  private static final long serialVersionUID = 1L;

  private double gauss_rnd[] = new double[4];
  private int gauss_N;

  private DoubleWrapperWF sina = new DoubleWrapperWF();
  private DoubleWrapperWF cosa = new DoubleWrapperWF();
  private DoubleWrapperWF sinb = new DoubleWrapperWF();
  private DoubleWrapperWF cosb = new DoubleWrapperWF();

  @Override
  public void transform(FlameTransformationContext pContext, XForm pXForm, XYZPoint pAffineTP, XYZPoint pVarTP, double pAmount) {
    double angle = pContext.random() * 2 * M_PI;
    sinAndCos(angle, sina, cosa);
    double r = pAmount * (gauss_rnd[0] + gauss_rnd[1] + gauss_rnd[2] + gauss_rnd[3] - 2);
    gauss_rnd[gauss_N] = pContext.random();
    gauss_N = (gauss_N + 1) & 3;
    angle = pContext.random() * M_PI;
    sinAndCos(angle, sinb, cosb);
    pVarTP.x += r * sinb.value * cosa.value;
    pVarTP.y += r * sinb.value * sina.value;
    pVarTP.z += r * cosb.value;
  }

  @Override
  public String getName() {
    return "blur3D";
  }

  @Override
  public void init(FlameTransformationContext pContext, Layer pLayer, XForm pXForm, double pAmount) {
    gauss_rnd[0] = pContext.random();
    gauss_rnd[1] = pContext.random();
    gauss_rnd[2] = pContext.random();
    gauss_rnd[3] = pContext.random();
    gauss_N = 0;
  }

  @Override
  public VariationFuncType[] getVariationTypes() {
    return new VariationFuncType[]{VariationFuncType.VARTYPE_3D, VariationFuncType.VARTYPE_BLUR, VariationFuncType.VARTYPE_SUPPORTS_GPU};
  }

  @Override
  public String getGPUCode(FlameTransformationContext context) {
    // based on code from the cudaLibrary.xml compilation, created by Steven Brodhead Sr.
    return "float cosa;\n"
        + "float sina;\n"
        + "sincosf(RANDFLOAT()*2.f*M_PI_F, &sina, &cosa);\n"
        + "float cosb;\n"
        + "float sinb;\n"
        + "sincosf(RANDFLOAT()*M_PI_F, &sinb, &cosb);\n"
        + "float rndG = __blur3D*(RANDFLOAT()+RANDFLOAT()+RANDFLOAT()+RANDFLOAT()-2.f);\n"
        + "__px += rndG*sinb*cosa;\n"
        + "__py += rndG*sinb*sina;\n"
        + "__pz += rndG*cosb;\n";
  }
}
