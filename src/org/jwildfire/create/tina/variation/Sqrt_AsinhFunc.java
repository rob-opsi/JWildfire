/*
  JWildfire - an image and animation processor written in Java 
  Copyright (C) 1995-2011 Andreas Maschke
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

import org.jwildfire.base.mathlib.Complex;
import static org.jwildfire.base.mathlib.MathLib.M_2_PI;

import org.jwildfire.create.tina.base.XForm;
import org.jwildfire.create.tina.base.XYZPoint;

public class Sqrt_AsinhFunc extends SimpleVariationFunc implements SupportsGPU {
  private static final long serialVersionUID = 1L;

  @Override
  public void transform(FlameTransformationContext pContext, XForm pXForm, XYZPoint pAffineTP, XYZPoint pVarTP, double pAmount) {
//Sqrt AsinH by Whittaker Courtney 12-19-2018

    Complex z = new Complex(pAffineTP.x, pAffineTP.y);

    z.Sqrt();
    z.AsinH();
    z.Scale(pAmount * M_2_PI);

    if (pContext.random() < 0.5){
      pVarTP.y += z.im;
      pVarTP.x += z.re;
    }
    else{
      pVarTP.y += -z.im;
      pVarTP.x += -z.re;
    }

    if (pContext.isPreserveZCoordinate()) {
      pVarTP.z += pAmount * pAffineTP.z;
    }
  }

  @Override
  public String getName() {
    return "sqrt_asinh";
  }

  @Override
  public VariationFuncType[] getVariationTypes() {
    return new VariationFuncType[]{VariationFuncType.VARTYPE_2D,VariationFuncType.VARTYPE_SUPPORTS_GPU};
  }
  @Override
  public String getGPUCode(FlameTransformationContext context) {
    return   "    Complex z;"
    		+"    Complex_Init(&z,__x, __y);"
    		+"    Complex_Sqrt(&z);"
    		+"    Complex_AsinH(&z);"
    		+"    Complex_Scale(&z,__sqrt_asinh * (2.0f / PI));"
    		+"    if (RANDFLOAT() < 0.5){"
    		+"      __py += z.im;"
    		+"      __px += z.re;"
    		+"    }"
    		+"    else{"
    		+"      __py += -z.im;"
    		+"      __px += -z.re;"
    		+"    }"
            + (context.isPreserveZCoordinate() ? "__pz += __sqrt_asinh *__z;" : "");
  }
}