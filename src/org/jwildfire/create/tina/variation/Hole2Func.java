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
import org.jwildfire.create.tina.base.XForm;
import org.jwildfire.create.tina.base.XYZPoint;

import static org.jwildfire.base.mathlib.MathLib.*;

public class Hole2Func extends VariationFunc implements SupportsGPU {
  private static final long serialVersionUID = 1L;
  private static final String PARAM_A = "a";
  private static final String PARAM_B = "b";
  private static final String PARAM_C = "c";
  private static final String PARAM_D = "d";
  public static final String PARAM_INSIDE = "inside";
  public static final String PARAM_SHAPE = "shape";
  private static final String[] paramNames = {PARAM_A, PARAM_B, PARAM_C, PARAM_D, PARAM_INSIDE, PARAM_SHAPE};

  private double a = 1.0;
  private double b = 2.0;
  private double c = 1.0;
  private double d = 1.0;

  private int inside = 0;
  private int shape = 0;

  @Override
  public void transform(FlameTransformationContext pContext, XForm pXForm,
                        XYZPoint pAffineTP, XYZPoint pVarTP, double pAmount) {
    // Hole3 by Michael Faber, Brad Stefanov, and Rick Sidwell

    double rhosq = pAffineTP.getPrecalcSumsq();
    double theta = pAffineTP.getPrecalcAtanYX() * d;
    double delta = pow(theta / M_PI + 1.0, a) * c;
    double r1 = 1;


    switch (shape) {
      case 0: // hole
        r1 = sqrt(rhosq) + delta;
        break;
      case 1:// hole1
        r1 = sqrt(rhosq + delta);
        break;
      case 2:// double hole
        r1 = sqrt(rhosq + sin(b * theta) + delta);
        break;
      case 3:// heart
        r1 = sqrt(rhosq + sin(theta) + delta);
        break;
      case 4:// heart2
        r1 = sqrt(rhosq + sqr(theta) - delta + 1);
        break;
      case 5:
        r1 = sqrt(rhosq + fabs(tan(theta)) + delta);
        break;
      case 6:
        r1 = sqrt(rhosq * (1 + sin(b * theta)) + delta);
        break;
      case 7:
        r1 = sqrt(rhosq + fabs(sin(0.5 * b * theta)) + delta);
        break;
      case 8:
        r1 = sqrt(rhosq + sin(M_PI * sin(b * theta)) + delta);
        break;
      case 9:
        r1 = sqrt(rhosq + (sin(b * theta) + sin(2 * b * theta + M_PI_2)) / 2 + delta);
        break;
    }

    if (inside != 0)
      r1 = pAmount / r1;
    else
      r1 = pAmount * r1;

    pVarTP.x += r1 * cos(theta);
    pVarTP.y += r1 * sin(theta);
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
    return new Object[]{a, b, c, d, inside, shape};
  }

  @Override
  public void setParameter(String pName, double pValue) {
    if (PARAM_A.equalsIgnoreCase(pName))
      a = pValue;
    else if (PARAM_B.equalsIgnoreCase(pName))
      b = pValue;
    else if (PARAM_C.equalsIgnoreCase(pName))
      c = pValue;
    else if (PARAM_D.equalsIgnoreCase(pName))
      d = pValue;
    else if (PARAM_INSIDE.equalsIgnoreCase(pName))
      inside = limitIntVal(Tools.FTOI(pValue), 0, 1);
    else if (PARAM_SHAPE.equalsIgnoreCase(pName))
      shape = limitIntVal(Tools.FTOI(pValue), 0, 9);
    else
      throw new IllegalArgumentException(pName);
  }

  @Override
  public String getName() {
    return "hole2";
  }

  @Override
  public VariationFuncType[] getVariationTypes() {
    return new VariationFuncType[]{VariationFuncType.VARTYPE_2D, VariationFuncType.VARTYPE_SUPPORTS_GPU};
  }

  @Override
  public String getGPUCode(FlameTransformationContext context) {
    return "int inside = lroundf(__hole2_inside);\n"
         + "int shape = lroundf(__hole2_shape);\n"
        + "float rhosq = __r2;\n"
        + "    float theta = __theta * __hole2_d;\n"
        + "    float delta = powf(theta / PI + 1.0f, __hole2_a) * __hole2_c;\n"
        + "    float r1 = 1.f;\n"
        + "\n"
        + "\n"
        + "    switch (shape) {\n"
        + "      case 0:\n"
        + "        r1 = sqrtf(rhosq) + delta;\n"
        + "        break;\n"
        + "      case 1:\n"
        + "        r1 = sqrtf(rhosq + delta);\n"
        + "        break;\n"
        + "      case 2:\n"
        + "        r1 = sqrtf(rhosq + sinf(__hole2_b * theta) + delta);\n"
        + "        break;\n"
        + "      case 3:\n"
        + "        r1 = sqrtf(rhosq + sinf(theta) + delta);\n"
        + "        break;\n"
        + "      case 4:\n"
        + "        r1 = sqrtf(rhosq + theta*theta - delta + 1.f);\n"
        + "        break;\n"
        + "      case 5:\n"
        + "        r1 = sqrtf(rhosq + fabsf(tanf(theta)) + delta);\n"
        + "        break;\n"
        + "      case 6:\n"
        + "        r1 = sqrtf(rhosq * (1.f + sinf(__hole2_b * theta)) + delta);\n"
        + "        break;\n"
        + "      case 7:\n"
        + "        r1 = sqrtf(rhosq + fabsf(sinf(0.5f * __hole2_b * theta)) + delta);\n"
        + "        break;\n"
        + "      case 8:\n"
        + "        r1 = sqrtf(rhosq + sinf(PI * sinf(__hole2_b * theta)) + delta);\n"
        + "        break;\n"
        + "      case 9:\n"
        + "        r1 = sqrtf(rhosq + (sinf(__hole2_b * theta) + sinf(2 * __hole2_b * theta + PI*0.5f)) / 2.f + delta);\n"
        + "        break;\n"
        + "    }\n"
        + "\n"
        + "    if (inside != 0)\n"
        + "      r1 = __hole2 / r1;\n"
        + "    else\n"
        + "      r1 = __hole2 * r1;\n"
        + "\n"
        + "    __px += r1 * cosf(theta);\n"
        + "    __py += r1 * sinf(theta);\n"
        + (context.isPreserveZCoordinate() ? "__pz += __hole2 * __z;\n": "");
  }
}