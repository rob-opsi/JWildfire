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

import org.jwildfire.create.tina.base.XForm;
import org.jwildfire.create.tina.base.XYZPoint;

import static org.jwildfire.base.mathlib.MathLib.*;

public class Sec2_BSFunc extends VariationFunc implements SupportsGPU {
  private static final long serialVersionUID = 1L;

  private static final String PARAM_X1 = "x1";
  private static final String PARAM_X2 = "x2";
  private static final String PARAM_Y1 = "y1";
  private static final String PARAM_Y2 = "y2";
  private static final String[] paramNames = {PARAM_X1, PARAM_X2, PARAM_Y1, PARAM_Y2};
  private double x1 = 1.0;
  private double x2 = 1.0;
  private double y1 = 1.0;
  private double y2 = 1.0;

  @Override
  public void transform(FlameTransformationContext pContext, XForm pXForm, XYZPoint pAffineTP, XYZPoint pVarTP, double pAmount) {
    /* complex vars by cothe */
    /* exp log sin cos tan sec csc cot sinh cosh tanh sech csch coth */
    /* Variables added by Brad Stefanov */
    //Secant SEC
    double secsin = sin(pAffineTP.x * x1);
    double seccos = cos(pAffineTP.x * x2);
    double secsinh = sinh(pAffineTP.y * y1);
    double seccosh = cosh(pAffineTP.y * y2);
    double d = (cos(2.0 * pAffineTP.x) + cosh(2.0 * pAffineTP.y));
    if (d == 0) {
      return;
    }
    double secden = 2.0 / d;
    pVarTP.x += pAmount * secden * seccos * seccosh;
    pVarTP.y += pAmount * secden * secsin * secsinh;
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
    return new Object[]{x1, x2, y1, y2};
  }

  @Override
  public void setParameter(String pName, double pValue) {
    if (PARAM_X1.equalsIgnoreCase(pName))
      x1 = pValue;
    else if (PARAM_X2.equalsIgnoreCase(pName))
      x2 = pValue;
    else if (PARAM_Y1.equalsIgnoreCase(pName))
      y1 = pValue;
    else if (PARAM_Y2.equalsIgnoreCase(pName))
      y2 = pValue;
    else
      throw new IllegalArgumentException(pName);
  }


  @Override
  public String getName() {
    return "sec2_bs";
  }

  @Override
  public VariationFuncType[] getVariationTypes() {
    return new VariationFuncType[]{VariationFuncType.VARTYPE_2D,VariationFuncType.VARTYPE_SUPPORTS_GPU};
  }
  @Override
  public String getGPUCode(FlameTransformationContext context) {
    return   "    float secsin = sinf(__x *  __sec2_bs_x1 );"
    		+"    float seccos = cosf(__x *  __sec2_bs_x2 );"
    		+"    float secsinh = sinhf(__y *  __sec2_bs_y1 );"
    		+"    float seccosh = coshf(__y *  __sec2_bs_y2 );"
    		+"    float d = (cosf(2.0 * __x) + coshf(2.0 * __y));"
    		+"    if (d != 0) {"
    		+"     float secden = 2.0 / d;"
    		+"     __px += __sec2_bs * secden * seccos * seccosh;"
    		+"     __py += __sec2_bs * secden * secsin * secsinh;"
    		+"	}"
            + (context.isPreserveZCoordinate() ? "__pz += __sec2_bs *__z;" : "");
  }

}
