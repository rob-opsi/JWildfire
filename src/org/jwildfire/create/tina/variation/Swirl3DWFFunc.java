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

import org.jwildfire.create.tina.base.XForm;
import org.jwildfire.create.tina.base.XYZPoint;

public class Swirl3DWFFunc extends VariationFunc implements SupportsGPU {
  private static final long serialVersionUID = 1L;

  private static final String PARAM_N = "n";

  private static final String[] paramNames = {PARAM_N};

  private double N = 0;


  @Override
  public void transform(FlameTransformationContext pContext, XForm pXForm, XYZPoint pAffineTP, XYZPoint pVarTP, double pAmount) {
    double rad = pAffineTP.getPrecalcSqrt();
    double ang = pAffineTP.getPrecalcAtanYX();   // + log(rad)*shift;

    pVarTP.x += pAmount * (rad * Math.cos(ang));
    pVarTP.y += pAmount * (rad * Math.sin(ang));
    pVarTP.z += pAmount * (Math.sin(6.0 * Math.cos(rad) - N * ang));
    pVarTP.color = Math.abs(Math.sin(6.0 * Math.cos(rad) - N * ang));
  }

  @Override
  public String[] getParameterNames() {
    return paramNames;
  }

  @Override
  public Object[] getParameterValues() {
    return new Object[]{N};
  }

  @Override
  public void setParameter(String pName, double pValue) {
    if (PARAM_N.equalsIgnoreCase(pName))
      N = (int) pValue;
    else
      throw new IllegalArgumentException(pName);
  }

  @Override
  public String getName() {
    return "swirl3D_wf";
  }

  @Override
  public VariationFuncType[] getVariationTypes() {
    return new VariationFuncType[]{VariationFuncType.VARTYPE_3D, VariationFuncType.VARTYPE_DC, VariationFuncType.VARTYPE_SUPPORTS_GPU};
  }

  @Override
  public String getGPUCode(FlameTransformationContext context) {
    return "float rad = __r;\n"
            + "float ang = __theta;\n"
            + "__px += __swirl3D_wf * (rad * cosf(ang));\n"
            + "__py += __swirl3D_wf * (rad * sinf(ang));\n"
            + "__pz += __swirl3D_wf * (sinf(6.0f * cosf(rad) - __swirl3D_wf_n * ang));\n"
            + "__pal = fabsf(sinf(6.0 * cosf(rad) - __swirl3D_wf_n * ang));";
  }
}
