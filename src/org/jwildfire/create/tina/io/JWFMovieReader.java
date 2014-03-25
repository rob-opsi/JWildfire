/*
  JWildfire - an image and animation processor written in Java 
  Copyright (C) 1995-2013 Andreas Maschke

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
package org.jwildfire.create.tina.io;

import static org.jwildfire.create.tina.io.JWFMovieWriter.ATTR_FPS;
import static org.jwildfire.create.tina.io.JWFMovieWriter.ATTR_FRAME_COUNT;
import static org.jwildfire.create.tina.io.JWFMovieWriter.ATTR_FRAME_HEIGHT;
import static org.jwildfire.create.tina.io.JWFMovieWriter.ATTR_FRAME_MORPH_COUNT;
import static org.jwildfire.create.tina.io.JWFMovieWriter.ATTR_FRAME_WIDTH;
import static org.jwildfire.create.tina.io.JWFMovieWriter.ATTR_MOTIONBLUR_LENGTH;
import static org.jwildfire.create.tina.io.JWFMovieWriter.ATTR_MOTIONBLUR_TIMESTEP;
import static org.jwildfire.create.tina.io.JWFMovieWriter.ATTR_NAME;
import static org.jwildfire.create.tina.io.JWFMovieWriter.ATTR_OUTPUT_FORMAT;
import static org.jwildfire.create.tina.io.JWFMovieWriter.ATTR_QUALITY;
import static org.jwildfire.create.tina.io.JWFMovieWriter.ATTR_SCRIPT_GLOBAL;
import static org.jwildfire.create.tina.io.JWFMovieWriter.ATTR_SCRIPT_GLOBAL1;
import static org.jwildfire.create.tina.io.JWFMovieWriter.ATTR_SCRIPT_GLOBAL1_AMPLITUDE;
import static org.jwildfire.create.tina.io.JWFMovieWriter.ATTR_SCRIPT_GLOBAL1_AMPLITUDE_CURVE;
import static org.jwildfire.create.tina.io.JWFMovieWriter.ATTR_SCRIPT_GLOBAL2;
import static org.jwildfire.create.tina.io.JWFMovieWriter.ATTR_SCRIPT_GLOBAL2_AMPLITUDE;
import static org.jwildfire.create.tina.io.JWFMovieWriter.ATTR_SCRIPT_GLOBAL2_AMPLITUDE_CURVE;
import static org.jwildfire.create.tina.io.JWFMovieWriter.ATTR_SCRIPT_GLOBAL3;
import static org.jwildfire.create.tina.io.JWFMovieWriter.ATTR_SCRIPT_GLOBAL3_AMPLITUDE;
import static org.jwildfire.create.tina.io.JWFMovieWriter.ATTR_SCRIPT_GLOBAL3_AMPLITUDE_CURVE;
import static org.jwildfire.create.tina.io.JWFMovieWriter.ATTR_SCRIPT_GLOBAL4;
import static org.jwildfire.create.tina.io.JWFMovieWriter.ATTR_SCRIPT_GLOBAL4_AMPLITUDE;
import static org.jwildfire.create.tina.io.JWFMovieWriter.ATTR_SCRIPT_GLOBAL4_AMPLITUDE_CURVE;
import static org.jwildfire.create.tina.io.JWFMovieWriter.ATTR_SCRIPT_GLOBAL5;
import static org.jwildfire.create.tina.io.JWFMovieWriter.ATTR_SCRIPT_GLOBAL5_AMPLITUDE;
import static org.jwildfire.create.tina.io.JWFMovieWriter.ATTR_SCRIPT_GLOBAL5_AMPLITUDE_CURVE;
import static org.jwildfire.create.tina.io.JWFMovieWriter.ATTR_SCRIPT_XFORM;
import static org.jwildfire.create.tina.io.JWFMovieWriter.ATTR_SCRIPT_XFORM1;
import static org.jwildfire.create.tina.io.JWFMovieWriter.ATTR_SCRIPT_XFORM1_AMPLITUDE;
import static org.jwildfire.create.tina.io.JWFMovieWriter.ATTR_SCRIPT_XFORM1_AMPLITUDE_CURVE;
import static org.jwildfire.create.tina.io.JWFMovieWriter.ATTR_SCRIPT_XFORM2;
import static org.jwildfire.create.tina.io.JWFMovieWriter.ATTR_SCRIPT_XFORM2_AMPLITUDE;
import static org.jwildfire.create.tina.io.JWFMovieWriter.ATTR_SCRIPT_XFORM2_AMPLITUDE_CURVE;
import static org.jwildfire.create.tina.io.JWFMovieWriter.ATTR_SCRIPT_XFORM3;
import static org.jwildfire.create.tina.io.JWFMovieWriter.ATTR_SCRIPT_XFORM3_AMPLITUDE;
import static org.jwildfire.create.tina.io.JWFMovieWriter.ATTR_SCRIPT_XFORM3_AMPLITUDE_CURVE;
import static org.jwildfire.create.tina.io.JWFMovieWriter.ATTR_SCRIPT_XFORM4;
import static org.jwildfire.create.tina.io.JWFMovieWriter.ATTR_SCRIPT_XFORM4_AMPLITUDE;
import static org.jwildfire.create.tina.io.JWFMovieWriter.ATTR_SCRIPT_XFORM4_AMPLITUDE_CURVE;
import static org.jwildfire.create.tina.io.JWFMovieWriter.ATTR_SCRIPT_XFORM5;
import static org.jwildfire.create.tina.io.JWFMovieWriter.ATTR_SCRIPT_XFORM5_AMPLITUDE;
import static org.jwildfire.create.tina.io.JWFMovieWriter.ATTR_SCRIPT_XFORM5_AMPLITUDE_CURVE;
import static org.jwildfire.create.tina.io.JWFMovieWriter.ATTR_SOUND_FILENAME;
import static org.jwildfire.create.tina.io.JWFMovieWriter.TAG_JWF_MOVIE;
import static org.jwildfire.create.tina.io.JWFMovieWriter.TAG_JWF_MOVIE_PART;

import org.jwildfire.base.Prefs;
import org.jwildfire.base.Tools;
import org.jwildfire.base.Tools.XMLAttributes;
import org.jwildfire.create.tina.animate.FlameMovie;
import org.jwildfire.create.tina.animate.FlameMoviePart;
import org.jwildfire.create.tina.animate.GlobalScript;
import org.jwildfire.create.tina.animate.GlobalScriptType;
import org.jwildfire.create.tina.animate.OutputFormat;
import org.jwildfire.create.tina.animate.XFormScript;
import org.jwildfire.create.tina.animate.XFormScriptType;
import org.jwildfire.create.tina.base.Flame;

public class JWFMovieReader {
  private final Prefs prefs;

  public JWFMovieReader(Prefs pPrefs) {
    prefs = pPrefs;
  }

  public FlameMovie readMovie(String pFilename) {
    try {
      String movieXML = Tools.readUTF8Textfile(pFilename);
      return readMovieFromXML(movieXML);
    }
    catch (Exception ex) {
      throw new RuntimeException(ex);
    }
  }

  public FlameMovie readMovieFromXML(String pXML) {
    // extract movie xml
    String movieXML;
    {
      int ps = pXML.indexOf("<" + TAG_JWF_MOVIE + " ");
      if (ps < 0)
        return null;
      int pe = pXML.indexOf("</" + TAG_JWF_MOVIE + ">", ps + 1);
      if (pe < 0)
        return null;
      movieXML = pXML.substring(ps, pe);
    }
    FlameMovie movie = new FlameMovie(prefs);
    // Movie attributes
    {
      int ps = movieXML.indexOf("<" + TAG_JWF_MOVIE + " ");
      int pe = -1;
      boolean qt = false;
      for (int i = ps + 1; i < movieXML.length(); i++) {
        if (movieXML.charAt(i) == '\"') {
          qt = !qt;
        }
        else if (!qt && movieXML.charAt(i) == '>') {
          pe = i;
          break;
        }
      }
      String hs = movieXML.substring(ps + 7, pe);
      parseMovieAttributes(movie, hs);
    }
    // parts
    {
      int p = 0;
      while (true) {
        int ps = movieXML.indexOf("<" + TAG_JWF_MOVIE_PART + " ", p + 1);
        if (ps < 0)
          break;
        int pe = movieXML.indexOf("</" + TAG_JWF_MOVIE_PART + ">", ps + 1);
        if (pe < 0) {
          pe = movieXML.indexOf("/>", ps + 1);
        }
        String hs = movieXML.substring(ps + TAG_JWF_MOVIE_PART.length() + 1, pe);
        FlameMoviePart part = new FlameMoviePart();
        movie.addPart(part);
        int psFlame = hs.indexOf("<flame ");
        if (psFlame > 0) {
          int peFlame = hs.indexOf("</flame>", psFlame + 1);
          String flameXML = hs.substring(psFlame, peFlame + 8);
          Flame flame = new FlameReader(prefs).readFlamesfromXML(flameXML).get(0);
          part.setFlame(flame);
          //System.out.println(flameXML);
          hs = hs.substring(0, psFlame);
        }
        else {
          psFlame = hs.indexOf("<jwf-flame ");
          if (psFlame > 0) {
            int peFlame = hs.indexOf("</jwf-flame>", psFlame + 1);
            String flameXML = hs.substring(psFlame, peFlame + 12);
            Flame flame = new FlameReader(prefs).readFlamesfromXML(flameXML).get(0);
            part.setFlame(flame);
            //System.out.println(flameXML);
            hs = hs.substring(0, psFlame);
          }
        }

        parseMoviePartAttributes(part, hs);
        p = pe + 2;
      }
    }

    return movie;
  }

  private void parseMoviePartAttributes(FlameMoviePart pPart, String pXML) {
    XMLAttributes atts = Tools.parseAttributes(pXML);
    String hs;
    if ((hs = atts.get(ATTR_FRAME_COUNT)) != null) {
      pPart.setFrameCount(Integer.parseInt(hs));
    }
    if ((hs = atts.get(ATTR_FRAME_MORPH_COUNT)) != null) {
      pPart.setFrameMorphCount(Integer.parseInt(hs));
    }
  }

  private void parseMovieAttributes(FlameMovie pMovie, String pXML) {
    XMLAttributes atts = Tools.parseAttributes(pXML);
    String hs;
    if ((hs = atts.get(ATTR_NAME)) != null) {
      pMovie.setName(hs);
    }
    if ((hs = atts.get(ATTR_SOUND_FILENAME)) != null) {
      pMovie.setSoundFilename(hs);
    }
    if ((hs = atts.get(ATTR_SCRIPT_GLOBAL)) != null) { // legacy
      pMovie.setGlobalScript1(new GlobalScript(GlobalScriptType.valueOf(hs), 1.0));
    }
    if ((hs = atts.get(ATTR_SCRIPT_GLOBAL1)) != null) {
      pMovie.setGlobalScript1(readGlobalScript(ATTR_SCRIPT_GLOBAL1, ATTR_SCRIPT_GLOBAL1_AMPLITUDE, ATTR_SCRIPT_GLOBAL1_AMPLITUDE_CURVE, atts));
    }
    if ((hs = atts.get(ATTR_SCRIPT_GLOBAL2)) != null) {
      pMovie.setGlobalScript2(readGlobalScript(ATTR_SCRIPT_GLOBAL2, ATTR_SCRIPT_GLOBAL2_AMPLITUDE, ATTR_SCRIPT_GLOBAL2_AMPLITUDE_CURVE, atts));
    }
    if ((hs = atts.get(ATTR_SCRIPT_GLOBAL3)) != null) {
      pMovie.setGlobalScript3(readGlobalScript(ATTR_SCRIPT_GLOBAL3, ATTR_SCRIPT_GLOBAL3_AMPLITUDE, ATTR_SCRIPT_GLOBAL3_AMPLITUDE_CURVE, atts));
    }
    if ((hs = atts.get(ATTR_SCRIPT_GLOBAL4)) != null) {
      pMovie.setGlobalScript4(readGlobalScript(ATTR_SCRIPT_GLOBAL4, ATTR_SCRIPT_GLOBAL4_AMPLITUDE, ATTR_SCRIPT_GLOBAL4_AMPLITUDE_CURVE, atts));
    }
    if ((hs = atts.get(ATTR_SCRIPT_GLOBAL5)) != null) {
      pMovie.setGlobalScript5(readGlobalScript(ATTR_SCRIPT_GLOBAL5, ATTR_SCRIPT_GLOBAL5_AMPLITUDE, ATTR_SCRIPT_GLOBAL5_AMPLITUDE_CURVE, atts));
    }

    if ((hs = atts.get(ATTR_SCRIPT_XFORM)) != null) { // legacy
      pMovie.setxFormScript1(new XFormScript(XFormScriptType.valueOf(hs), 1.0));
    }
    if ((hs = atts.get(ATTR_SCRIPT_XFORM1)) != null) {
      pMovie.setxFormScript1(readXFormScript(ATTR_SCRIPT_XFORM1, ATTR_SCRIPT_XFORM1_AMPLITUDE, ATTR_SCRIPT_XFORM1_AMPLITUDE_CURVE, atts));
    }
    if ((hs = atts.get(ATTR_SCRIPT_XFORM2)) != null) {
      pMovie.setxFormScript2(readXFormScript(ATTR_SCRIPT_XFORM2, ATTR_SCRIPT_XFORM2_AMPLITUDE, ATTR_SCRIPT_XFORM2_AMPLITUDE_CURVE, atts));
    }
    if ((hs = atts.get(ATTR_SCRIPT_XFORM3)) != null) {
      pMovie.setxFormScript3(readXFormScript(ATTR_SCRIPT_XFORM3, ATTR_SCRIPT_XFORM3_AMPLITUDE, ATTR_SCRIPT_XFORM3_AMPLITUDE_CURVE, atts));
    }
    if ((hs = atts.get(ATTR_SCRIPT_XFORM4)) != null) {
      pMovie.setxFormScript4(readXFormScript(ATTR_SCRIPT_XFORM4, ATTR_SCRIPT_XFORM4_AMPLITUDE, ATTR_SCRIPT_XFORM4_AMPLITUDE_CURVE, atts));
    }
    if ((hs = atts.get(ATTR_SCRIPT_XFORM5)) != null) {
      pMovie.setxFormScript5(readXFormScript(ATTR_SCRIPT_XFORM5, ATTR_SCRIPT_XFORM5_AMPLITUDE, ATTR_SCRIPT_XFORM5_AMPLITUDE_CURVE, atts));
    }

    if ((hs = atts.get(ATTR_FRAME_WIDTH)) != null) {
      pMovie.setFrameWidth(Integer.parseInt(hs));
    }
    if ((hs = atts.get(ATTR_FRAME_HEIGHT)) != null) {
      pMovie.setFrameHeight(Integer.parseInt(hs));
    }
    if ((hs = atts.get(ATTR_FPS)) != null) {
      pMovie.setFramesPerSecond(Double.parseDouble(hs));
    }
    if ((hs = atts.get(ATTR_OUTPUT_FORMAT)) != null) {
      pMovie.setOutputFormat(OutputFormat.valueOf(hs));
    }
    if ((hs = atts.get(ATTR_QUALITY)) != null) {
      pMovie.setQuality(Integer.parseInt(hs));
    }
    if ((hs = atts.get(ATTR_MOTIONBLUR_LENGTH)) != null) {
      pMovie.setMotionBlurLength(Integer.parseInt(hs));
    }
    if ((hs = atts.get(ATTR_MOTIONBLUR_TIMESTEP)) != null) {
      pMovie.setMotionBlurTimeStep(Double.parseDouble(hs));
    }
  }

  private GlobalScript readGlobalScript(String pAttrScript, String pAttrScriptAmplitude, String pAttrScriptAmplitudeCurve, XMLAttributes pAtts) {
    GlobalScript script = new GlobalScript();
    String hs;
    if ((hs = pAtts.get(pAttrScript)) != null) {
      try {
        script.setScriptType(GlobalScriptType.valueOf(hs));
      }
      catch (Exception ex) {
        ex.printStackTrace();
      }
    }
    if ((hs = pAtts.get(pAttrScriptAmplitude)) != null) {
      script.setAmplitude(Double.parseDouble(hs));
    }
    AbstractFlameReader.readMotionCurveAttributes(pAtts, script.getAmplitudeCurve(), pAttrScript + Tools.CURVE_POSTFIX + "_");
    return script;
  }

  private XFormScript readXFormScript(String pAttrScript, String pAttrScriptAmplitude, String pAttrScriptAmplitudeCurve, XMLAttributes pAtts) {
    XFormScript script = new XFormScript();
    String hs;
    if ((hs = pAtts.get(pAttrScript)) != null) {
      try {
        script.setScriptType(XFormScriptType.valueOf(hs));
      }
      catch (Exception ex) {
        ex.printStackTrace();
      }
    }
    if ((hs = pAtts.get(pAttrScriptAmplitude)) != null) {
      script.setAmplitude(Double.parseDouble(hs));
    }
    AbstractFlameReader.readMotionCurveAttributes(pAtts, script.getAmplitudeCurve(), pAttrScript + Tools.CURVE_POSTFIX + "_");
    return script;
  }
}
