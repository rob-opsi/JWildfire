/*
  JWildfire - an image and animation processor written in Java 
  Copyright (C) 1995-2012 Andreas Maschke

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
package org.jwildfire.create.tina.animate;

import java.io.File;

import javax.imageio.ImageIO;

import org.jwildfire.create.tina.base.Flame;
import org.jwildfire.image.SimpleImage;
import org.jwildfire.io.ImageWriter;

import com.flagstone.transform.Background;
import com.flagstone.transform.Movie;
import com.flagstone.transform.MovieHeader;
import com.flagstone.transform.MovieTag;
import com.flagstone.transform.Place2;
import com.flagstone.transform.ShowFrame;
import com.flagstone.transform.datatype.Color;
import com.flagstone.transform.datatype.WebPalette;
import com.flagstone.transform.image.ImageTag;
import com.flagstone.transform.linestyle.LineStyle1;
import com.flagstone.transform.shape.ShapeTag;
import com.flagstone.transform.util.image.ImageFactory;
import com.flagstone.transform.util.image.ImageShape;
import com.flagstone.transform.util.sound.SoundFactory;

public class SWFAnimationRenderThread implements Runnable {
  private final SWFAnimationRenderThreadController controller;
  private final String outputFilename;
  private final int frameCount;
  private final int frameWidth;
  private final int frameHeight;
  private final double framesPerSecond;
  private boolean cancelSignalled;
  private FlameAnimation animationData;
  private SoundFactory soundFactory;
  private Movie movie;
  private int uid;
  private Throwable lastError;
  private String tmpBasefilename;

  public SWFAnimationRenderThread(SWFAnimationRenderThreadController pController, int pFrameCount, int pFrameWidth, int pFrameHeight, double pFramesPerSecond, FlameAnimation pAnimationData, String pOutputFilename) {
    controller = pController;
    frameCount = pFrameCount;
    frameWidth = pFrameWidth;
    frameHeight = pFrameHeight;
    framesPerSecond = pFramesPerSecond;
    animationData = pAnimationData;
    outputFilename = pOutputFilename;
  }

  @Override
  public void run() {
    try {
      try {
        cancelSignalled = false;
        lastError = null;
        controller.getProgressUpdater().initProgress(frameCount);
        initMovie();
        for (int i = 1; i <= frameCount; i++) {
          if (cancelSignalled) {
            break;
          }
          SimpleImage image = renderImage(i);
          addImageToMovie(image, i);
          controller.getProgressUpdater().updateProgress(i);
        }
        finishMovie();
      }
      catch (Throwable ex) {
        lastError = ex;
        throw new RuntimeException(ex);
      }
    }
    finally {
      controller.onRenderFinished();
    }
  }

  private void prepareFlame(Flame pFlame) {
    pFlame.setSpatialFilterRadius(1.0);
    pFlame.setSpatialOversample(animationData.getQualityProfile().getSpatialOversample());
    pFlame.setColorOversample(animationData.getQualityProfile().getColorOversample());
    pFlame.setSampleDensity(animationData.getQualityProfile().getQuality());

  }

  private SimpleImage renderImage(int pFrame) throws Exception {
    Flame flame1 = animationData.getFlame(pFrame);
    prepareFlame(flame1);
    return AnimationService.renderFrame(pFrame, frameCount, flame1, animationData.getGlobalScript(), animationData.getxFormScript(), frameWidth, frameHeight, controller.getPrefs());
  }

  private void addImageToMovie(SimpleImage pImage, int pFrame) throws Exception {
    final ImageFactory factory = createImageFactory(pImage);
    final ImageTag image = factory.defineImage(uid++);
    final int xOrigin = -image.getWidth() / 2;
    final int yOrigin = -image.getHeight() / 2;
    final int width = 20;
    final Color color = WebPalette.BLACK.color();
    final ShapeTag shape = new ImageShape().defineShape(uid++, image,
        xOrigin, yOrigin, new LineStyle1(width, color));
    movie.add(image);
    movie.add(shape);
    if (pFrame == 1) {
      movie.add(Place2.show(shape.getIdentifier(), 1, 0, 0));
    }
    else {
      movie.add(Place2.replace(shape.getIdentifier(), 1, 0, 0));
    }
    if (soundFactory != null) {
      MovieTag block = soundFactory.streamSound();
      if (block != null) {
        movie.add(block);
      }
    }
    movie.add(ShowFrame.getInstance());
  }

  private void finishMovie() throws Exception {
    movie.encodeToFile(new File(outputFilename));
  }

  /*
   I had to patch the factory.read() method as follows (otherwise the tmp files can not get deleted):
   
    public void read(final File file) throws IOException, DataFormatException {
      ImageInfo info = new ImageInfo();
      RandomAccessFile rf = new RandomAccessFile(file, "r");
      try {
        info.setInput(rf);
        if (!info.check()) {
          throw new DataFormatException("Unsupported format");
        }
        decoder = ImageRegistry.getImageProvider(info.getImageFormat().getMimeType());
      }
      finally {
        rf.close();
      }
      info = null;
      InputStream is = new FileInputStream(file);
      try {
        decoder.read(is);
      }
      finally {
        is.close();
      }
    }  
    
   */

  private ImageFactory createImageFactory(SimpleImage pImage) throws Exception {
    String id = String.valueOf(uid);
    while (id.length() < 6) {
      id = "0" + id;
    }
    String tmpFilename = tmpBasefilename + "." + id + ".jpg";
    ImageIO.setUseCache(false);
    new ImageWriter().saveImage(pImage, tmpFilename);
    final ImageFactory factory = new ImageFactory();
    File f = new File(tmpFilename);
    try {
      int times = 0;
      while (true) {
        try {
          factory.read(f);
          break;
        }
        catch (Exception ex) {
          if (++times > 3) {
            throw ex;
          }
          ex.printStackTrace();
          Thread.sleep(500);
          System.out.println("RETRY...");
        }
      }
    }
    finally {
      if (!f.delete()) {
        f.deleteOnExit();
      }
    }
    return factory;
  }

  private void initMovie() throws Exception {
    {
      File tmpFile = File.createTempFile("jwf", "");
      tmpBasefilename = tmpFile.getAbsolutePath();
      tmpFile.delete();
    }

    uid = 1;
    movie = new Movie();
    final int xOrigin = -frameWidth / 2;
    final int yOrigin = -frameHeight / 2;
    MovieHeader header = new MovieHeader();
    header.setFrameRate((float) framesPerSecond);
    final int width = 20;
    final Color color = WebPalette.BLACK.color();
    final ImageFactory factory = createImageFactory(new SimpleImage(frameWidth, frameHeight));
    final ImageTag image = factory.defineImage(uid++);
    ShapeTag shape = new ImageShape().defineShape(uid++, image,
        xOrigin, yOrigin, new LineStyle1(width, color));
    header.setFrameSize(shape.getBounds());
    movie.add(header);
    movie.add(new Background(WebPalette.LIGHT_BLUE.color()));
    uid = 1;
    // Add sound
    if (animationData.getSoundFilename() != null) {
      soundFactory = new SoundFactory();
      soundFactory.read(new File(animationData.getSoundFilename()));
      movie.add(soundFactory.streamHeader((float) framesPerSecond));
    }
    else {
      soundFactory = null;
    }
  }

  public void setCancelSignalled(boolean cancelSignalled) {
    this.cancelSignalled = cancelSignalled;
  }

  public Throwable getLastError() {
    return lastError;
  }

  public boolean isCancelSignalled() {
    return cancelSignalled;
  }
}
