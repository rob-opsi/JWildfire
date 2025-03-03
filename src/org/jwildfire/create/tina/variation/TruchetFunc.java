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

/*
 * Truchet Plugin by TyrantWave, see http://tyrantwave.deviantart.com/art/Truchet-Plugin-107982844
 */
package org.jwildfire.create.tina.variation;

import org.jwildfire.base.Tools;
import org.jwildfire.create.tina.base.Layer;
import org.jwildfire.create.tina.base.XForm;
import org.jwildfire.create.tina.base.XYZPoint;

import static org.jwildfire.base.mathlib.MathLib.*;

public class TruchetFunc extends VariationFunc implements SupportsGPU {
  private static final long serialVersionUID = 1L;

  private static final String PARAM_EXTENDED = "extended";
  private static final String PARAM_EXPONENT = "exponent";
  private static final String PARAM_ARC_WIDTH = "arc_width";
  private static final String PARAM_ROTATION = "rotation";
  private static final String PARAM_SIZE = "size";
  private static final String PARAM_SEED = "seed";
  private static final String PARAM_DIRECT_COLOR = "direct_color";

  private static final String[] paramNames = {PARAM_EXTENDED, PARAM_EXPONENT, PARAM_ARC_WIDTH, PARAM_ROTATION, PARAM_SIZE, PARAM_SEED, PARAM_DIRECT_COLOR};

  private int extended = 0;
  private double exponent = 2.0;
  private double arc_width = 0.5;
  private double rotation = 0.0;
  private double size = 1.0;
  private double seed = 50.0;
  private int direct_color = 0;

  @Override
  public void transform(FlameTransformationContext pContext, XForm pXForm, XYZPoint pAffineTP, XYZPoint pVarTP, double pAmount) {
    //APO VARIABLES
    double n = exponent;
    double onen = 1.0 / exponent;
    double tdeg = rotation;
    double width = arc_width;
    double seed = fabs(this.seed);
    double seed2 = sqrt(seed + (seed / 2) + SMALL_EPSILON) / ((seed * 0.5) + SMALL_EPSILON) * 0.25;
    //VARIABLES   
    double x, y;
    int intx = 0;
    int inty = 0;
    double r = -tdeg;
    double r0 = 0.0;
    double r1 = 0.0;
    double rmax = 0.5 * (pow(2.0, 1.0 / n) - 1.0) * width;
    double scale = (cos(r) - sin(r)) / pAmount;
    double tiletype = 0.0;
    double randint = 0.0;
    double modbase = 65535.0;
    double multiplier = 32747.0;//1103515245;
    double offset = 12345.0;
    double niter = 0.0;
    int randiter = 0;
    //INITIALISATION   
    x = pAffineTP.x * scale;
    y = pAffineTP.y * scale;
    intx = (int) round(x);
    inty = (int) round(y);

    r = x - intx;
    if (r < 0.0) {
      x = 1.0 + r;
    } else {
      x = r;
    }

    r = y - inty;
    if (r < 0.0) {
      y = 1.0 + r;
    } else {
      y = r;
    }
    //CALCULATE THE TILE TYPE
    if (seed == 0.0) {
      tiletype = 0.0;
    } else if (seed == 1.0) {
      tiletype = 1.0;
    } else {
      if (extended == 0) {
        double xrand = round(pAffineTP.x);
        double yrand = round(pAffineTP.y);
        xrand = xrand * seed2;
        yrand = yrand * seed2;
        niter = xrand + yrand + xrand * yrand;
        randint = (niter + seed) * seed2 / 2.0;
        randint = fmod((randint * multiplier + offset), modbase);
      } else {
        seed = floor(seed);
        int xrand = (int) round(pAffineTP.x);
        int yrand = (int) round(pAffineTP.y);
        niter = fabs(xrand + yrand + xrand * yrand);
        if (niter > 1000)
          niter = 1000;
        randint = seed + niter;
        randiter = 0;
        while (randiter < niter) {
          randiter += 1;
          randint = fmod((randint * multiplier + offset), modbase);
        }
      }
      tiletype = fmod(randint, 2.0);//randint%2;
    }
    //DRAWING THE POINTS
    if (extended == 0) { //Fast drawmode
      if (tiletype < 1.0) {
        r0 = pow((pow(fabs(x), n) + pow(fabs(y), n)), onen);
        r1 = pow((pow(fabs(x - 1.0), n) + pow(fabs(y - 1.0), n)), onen);
      } else {
        r0 = pow((pow(fabs(x - 1.0), n) + pow(fabs(y), n)), onen);
        r1 = pow((pow(fabs(x), n) + pow(fabs(y - 1.0), n)), onen);
      }
    } else {
      if (tiletype == 1.0) { //Slow drawmode 
        r0 = pow((pow(fabs(x), n) + pow(fabs(y), n)), onen);
        r1 = pow((pow(fabs(x - 1.0), n) + pow(fabs(y - 1.0), n)), onen);
      } else {
        r0 = pow((pow(fabs(x - 1.0), n) + pow(fabs(y), n)), onen);
        r1 = pow((pow(fabs(x), n) + pow(fabs(y - 1.0), n)), onen);
      }
    }

    r = fabs(r0 - 0.5) / rmax;
    if (r < 1.0) {
      if (direct_color == 1) {
        pVarTP.color = limitVal(r0, 0.0, 1.0);
      }
      pVarTP.x += size * (x + floor(pAffineTP.x));
      pVarTP.y += size * (y + floor(pAffineTP.y));
    }

    r = fabs(r1 - 0.5) / rmax;
    if (r < 1.0) {
      if (direct_color == 1) {
        pVarTP.color = limitVal(1.0 - r1, 0.0, 1.0);
      }
      pVarTP.x += size * (x + floor(pAffineTP.x));
      pVarTP.y += size * (y + floor(pAffineTP.y));
      if (pContext.isPreserveZCoordinate()) {
        pVarTP.z += pAmount * pAffineTP.z;
      }
    }
  }

  @Override
  public String[] getParameterNames() {
    return paramNames;
  }

  @Override
  public Object[] getParameterValues() {
    return new Object[]{extended, exponent, arc_width, rotation, size, seed, direct_color};
  }

  @Override
  public void setParameter(String pName, double pValue) {
    if (PARAM_EXTENDED.equalsIgnoreCase(pName))
      extended = limitIntVal(Tools.FTOI(pValue), 0, 1);
    else if (PARAM_EXPONENT.equalsIgnoreCase(pName))
      exponent = pValue;
    else if (PARAM_ARC_WIDTH.equalsIgnoreCase(pName))
      arc_width = pValue;
    else if (PARAM_ROTATION.equalsIgnoreCase(pName))
      rotation = pValue;
    else if (PARAM_SIZE.equalsIgnoreCase(pName))
      size = pValue;
    else if (PARAM_SEED.equalsIgnoreCase(pName))
      seed = pValue;
    else if (PARAM_DIRECT_COLOR.equalsIgnoreCase(pName))
      direct_color = limitIntVal(Tools.FTOI(pValue), 0, 1);
    else
      throw new IllegalArgumentException(pName);
  }

  @Override
  public String getName() {
    return "truchet";
  }

  @Override
  public void init(FlameTransformationContext pContext, Layer pLayer, XForm pXForm, double pAmount) {
    if (extended < 0) {
      extended = 0;
    } else if (extended > 1) {
      extended = 1;
    }
    if (exponent < 0.001) {
      exponent = 0.001;
    } else if (exponent > 2.0) {
      exponent = 2.0;
    }
    if (arc_width < 0.001) {
      arc_width = 0.001;
    } else if (arc_width > 1.0) {
      arc_width = 1.0;
    }
    if (size < 0.001) {
      size = 0.001;
    } else if (size > 10.0) {
      size = 10.0;
    }
  }

  @Override
  public VariationFuncType[] getVariationTypes() {
    return new VariationFuncType[]{VariationFuncType.VARTYPE_2D, VariationFuncType.VARTYPE_DC, VariationFuncType.VARTYPE_SIMULATION, VariationFuncType.VARTYPE_SUPPORTS_GPU};
  }

  @Override
  public String getGPUCode(FlameTransformationContext context) {
    return "int extended = lroundf(__truchet_extended);\n"
        + "float exponent = __truchet_exponent;\n"
        + "float arc_width = __truchet_arc_width;\n"
        + "float size = __truchet_size;\n"
        + "if (extended < 0) {\n"
        + "      extended = 0;\n"
        + "    } else if (extended > 1) {\n"
        + "      extended = 1;\n"
        + "    }\n"
        + "    if (exponent < 0.001) {\n"
        + "      exponent = 0.001;\n"
        + "    } else if (exponent > 2.0) {\n"
        + "      exponent = 2.0;\n"
        + "    }\n"
        + "    if (arc_width < 0.001) {\n"
        + "      arc_width = 0.001;\n"
        + "    } else if (arc_width > 1.0) {\n"
        + "      arc_width = 1.0;\n"
        + "    }\n"
        + "    if (size < 0.001) {\n"
        + "      size = 0.001;\n"
        + "    } else if (size > 10.0) {\n"
        + "      size = 10.0;\n"
        + "    }\n\n"
        + " float n = exponent;\n"
        + "    float onen = 1.0f / exponent;\n"
        + "    float tdeg = __truchet_rotation;\n"
        + "    float width = arc_width;\n"
        + "    float seed = fabsf(__truchet_seed);\n"
        + "    float seed2 = sqrtf(seed + (seed / 2.f) + 1.e-6f) / ((seed * 0.5f) + 1.e-6f) * 0.25f;\n"
        + "    float x, y;\n"
        + "    int intx = 0;\n"
        + "    int inty = 0;\n"
        + "    float r = -tdeg;\n"
        + "    float r0 = 0.0;\n"
        + "    float r1 = 0.0;\n"
        + "    float rmax = 0.5 * (powf(2.0f, 1.0f / n) - 1.0f) * width;\n"
        + "    float scale = (cosf(r) - sinf(r)) / __truchet;\n"
        + "    float tiletype = 0.0f;\n"
        + "    float randint = 0.0f;\n"
        + "    float modbase = 65535.0f;\n"
        + "    float multiplier = 32747.0f;\n"
        + "    float offset = 12345.0f;\n"
        + "    float niter = 0.0f;\n"
        + "    int randiter = 0;\n"
        + "    x = __x * scale;\n"
        + "    y = __y * scale;\n"
        + "    intx = (int) lroundf(x);\n"
        + "    inty = (int) lroundf(y);\n"
        + "    r = x - intx;\n"
        + "    if (r < 0.0f) {\n"
        + "      x = 1.0f + r;\n"
        + "    } else {\n"
        + "      x = r;\n"
        + "    }\n"
        + "    r = y - inty;\n"
        + "    if (r < 0.0f) {\n"
        + "      y = 1.0f + r;\n"
        + "    } else {\n"
        + "      y = r;\n"
        + "    }\n"
        + "    if (seed == 0.0f) {\n"
        + "      tiletype = 0.0f;\n"
        + "    } else if (seed == 1.0f) {\n"
        + "      tiletype = 1.0f;\n"
        + "    } else {\n"
        + "      if (extended == 0) {\n"
        + "        float xrand = roundf(__x);\n"
        + "        float yrand = roundf(__y);\n"
        + "        xrand = xrand * seed2;\n"
        + "        yrand = yrand * seed2;\n"
        + "        niter = xrand + yrand + xrand * yrand;\n"
        + "        randint = (niter + seed) * seed2 / 2.0f;\n"
        + "        randint = fmodf((randint * multiplier + offset), modbase);\n"
        + "      } else {\n"
        + "        seed = floorf(seed);\n"
        + "        int xrand = (int) lroundf(__x);\n"
        + "        int yrand = (int) lroundf(__y);\n"
        + "        niter = fabsf(xrand + yrand + xrand * yrand);\n"
        + "        if (niter > 1000)\n"
        + "          niter = 1000;\n"
        + "        randint = seed + niter;\n"
        + "        randiter = 0;\n"
        + "        while (randiter < niter) {\n"
        + "          randiter += 1;\n"
        + "          randint = fmodf((randint * multiplier + offset), modbase);\n"
        + "        }\n"
        + "      }\n"
        + "      tiletype = fmodf(randint, 2.0f);\n"
        + "    }\n"
        + "    if (extended == 0) { \n"
        + "      if (tiletype < 1.0) {\n"
        + "        r0 = powf((powf(fabsf(x), n) + powf(fabsf(y), n)), onen);\n"
        + "        r1 = powf((powf(fabsf(x - 1.0), n) + powf(fabsf(y - 1.0f), n)), onen);\n"
        + "      } else {\n"
        + "        r0 = powf((powf(fabsf(x - 1.0f), n) + powf(fabsf(y), n)), onen);\n"
        + "        r1 = powf((powf(fabsf(x), n) + powf(fabsf(y - 1.0f), n)), onen);\n"
        + "      }\n"
        + "    } else {\n"
        + "      if (tiletype == 1.0f) {\n"
        + "        r0 = powf((powf(fabsf(x), n) + powf(fabsf(y), n)), onen);\n"
        + "        r1 = powf((powf(fabsf(x - 1.0f), n) + powf(fabsf(y - 1.0f), n)), onen);\n"
        + "      } else {\n"
        + "        r0 = powf((powf(fabsf(x - 1.0f), n) + powf(fabsf(y), n)), onen);\n"
        + "        r1 = powf((powf(fabsf(x), n) + powf(fabsf(y - 1.0f), n)), onen);\n"
        + "      }\n"
        + "    }\n"
        + "\n"
        + "    r = fabsf(r0 - 0.5f) / rmax;\n"
        + "    if (r < 1.0f) {\n"
        + "      if (lroundf(__truchet_direct_color) == 1) {\n"
        + "        __pal = r0;\n"
        + "        if(__pal<0.f)\n"
        + "          __pal = 0.f;\n"
        + "        else if(__pal>1.f)\n"
        + "          __pal = 1.f;\n"
        + "      }\n"
        + "      __px += size * (x + floorf(__x));\n"
        + "      __py += size * (y + floorf(__y));\n"
        + "    }\n"
        + "\n"
        + "    r = fabsf(r1 - 0.5f) / rmax;\n"
        + "    if (r < 1.0f) {\n"
        + "      if (__truchet_direct_color == 1) {\n"
        + "        __pal = 1.0f - r1;\n"
        + "        if(__pal<0.f)\n"
        + "          __pal = 0.f;\n"
        + "        else if(__pal>1.f)\n"
        + "          __pal = 1.f;\n"
        + "      }\n"
        + "      __px += size * (x + floorf(__x));\n"
        + "      __py += size * (y + floorf(__y));\n"
        + (context.isPreserveZCoordinate() ? "     __pz += __truchet * __z;" : "")
        + "    }\n";
  }
}
