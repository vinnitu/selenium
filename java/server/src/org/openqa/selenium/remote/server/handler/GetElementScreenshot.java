/*
Copyright 2007-2009 Selenium committers

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

     http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
 */

package org.openqa.selenium.remote.server.handler;

import java.io.File;
import static org.openqa.selenium.OutputType.BASE64;
import static org.openqa.selenium.OutputType.FILE;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.channels.FileChannel;
import java.io.IOException;
import org.openqa.selenium.internal.Base64Encoder;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import org.openqa.selenium.Point;
import java.io.ByteArrayOutputStream;

import org.openqa.selenium.Dimension;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.remote.server.Session;

public class GetElementScreenshot extends WebElementHandler<String> {

  public GetElementScreenshot(Session session) {
    super(session);
  }

  private static void copyFileUsingChannel(File source, File dest) throws IOException {
    FileChannel sourceChannel = null;
    FileChannel destChannel = null;
    try {
      sourceChannel = new FileInputStream(source).getChannel();
      destChannel = new FileOutputStream(dest).getChannel();
      destChannel.transferFrom(sourceChannel, 0, sourceChannel.size());
    } finally{
      sourceChannel.close();
      destChannel.close();
    }
  }

  @Override
  public String call() throws Exception {
    WebElement element = getElement();
    WebDriver driver = getUnwrappedDriver();

    try {
      File screenshot = ((TakesScreenshot)driver).getScreenshotAs(FILE);
      BufferedImage fullImg = ImageIO.read(screenshot);

      //Get the location of element on the page
      Point point = element.getLocation();

      //Get width and height of the element
      int elementWidth = element.getSize().getWidth();
      int elementHeight = element.getSize().getHeight();

      //Crop the entire page screenshot to get only element screenshot
      BufferedImage elementScreenshot = fullImg.getSubimage(point.getX(), point.getY(), elementWidth, elementHeight);
      ImageIO.write(elementScreenshot, "png", screenshot);

      ByteArrayOutputStream outStream = new ByteArrayOutputStream();
      ImageIO.write(elementScreenshot, "png", outStream);

      return new Base64Encoder().encode(outStream.toByteArray());
      //copyFileUsingChannel(screenshot, new File("google_page.png"));
    } catch (Exception e) {
      e.printStackTrace();
    }

    return ""; //throw new WebDriverException("no image");
  }


  @Override
  public String toString() {
    return String.format("[get element screenshot: %s]", getElementAsString());
  }
}
