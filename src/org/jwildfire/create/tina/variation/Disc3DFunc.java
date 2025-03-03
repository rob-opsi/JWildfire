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

import static org.jwildfire.base.mathlib.MathLib.*;

public class Disc3DFunc extends VariationFunc implements SupportsGPU {
  private static final long serialVersionUID = 1L;

  private static final String PARAM_PI = "pi";
  private static final String[] paramNames = {PARAM_PI};

  private double pi = M_PI;

  @Override
  public void transform(FlameTransformationContext pContext, XForm pXForm, XYZPoint pAffineTP, XYZPoint pVarTP, double pAmount) {
    /* disc3D by Larry Berlin, http://aporev.deviantart.com/art/3D-Plugins-Collection-One-138514007?q=gallery%3Aaporev%2F8229210&qo=15 */
    double r = sqrt(pAffineTP.y * pAffineTP.y + pAffineTP.x * pAffineTP.x + SMALL_EPSILON);
    double a = this.pi * r;
    double sr = sin(a);
    double cr = cos(a);
    double vv = pAmount * atan2(pAffineTP.x, pAffineTP.y) / (this.pi + SMALL_EPSILON);
    pVarTP.x += vv * sr;
    pVarTP.y += vv * cr;
    pVarTP.z += vv * (r * cos(pAffineTP.z));
  }

  @Override
  public String[] getParameterNames() {
    return paramNames;
  }

  @Override
  public Object[] getParameterValues() {
    return new Object[]{pi};
  }

  @Override
  public void setParameter(String pName, double pValue) {
    if (PARAM_PI.equalsIgnoreCase(pName))
      pi = pValue;
    else
      throw new IllegalArgumentException(pName);
  }

  @Override
  public String getName() {
    return "disc3d";
  }

  @Override
  public VariationFuncType[] getVariationTypes() {
    return new VariationFuncType[]{VariationFuncType.VARTYPE_3D, VariationFuncType.VARTYPE_SUPPORTS_GPU};
  }

  @Override
  public String getGPUCode(FlameTransformationContext context) {
    return "float r = sqrtf(__y * __y + __x * __x + 1.e-6f);\n"
        + "float a = __disc3d_pi * r;\n"
        + "float sr = sinf(a);\n"
        + "float cr = cosf(a);\n"
        + "float vv = __disc3d * atan2f(__x, __y) / (__disc3d_pi + 1.e-6f);\n"
        + "__px += vv * sr;\n"
        + "__py += vv * cr;\n"
        + "__pz += vv * (r * cosf(__z));";
  }
}
