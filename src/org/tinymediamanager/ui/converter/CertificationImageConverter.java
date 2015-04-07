/*
 * Copyright 2012 - 2014 Manuel Laggner
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.tinymediamanager.ui.converter;

import org.jdesktop.beansbinding.Converter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tinymediamanager.scraper.Certification;

import javax.swing.*;
import java.net.URL;

/**
 * The Class CertificationImageConverter.
 * 
 * @author Manuel Laggner
 */
public class CertificationImageConverter extends Converter<Certification, Icon> {
  private static final Logger   LOGGER     = LoggerFactory.getLogger(CertificationImageConverter.class);
  public final static ImageIcon emptyImage = new ImageIcon();

  @Override
  public Icon convertForward(Certification cert) {
    if (cert == null){
      return null;
    }

    // try to find an image for this genre
    try {
      StringBuilder sb = new StringBuilder("/images/certifications/");
      sb.append(cert.name().toLowerCase());
      sb.append(".png");

      URL file = CertificationImageConverter.class.getResource(sb.toString());
      if (file == null) {
        // try to find the image without the country name in path
        sb = new StringBuilder("/images/certifications/");
        String certName = cert.name();
        sb.append(certName.replace(cert.getCountry().getAlpha2() + "_", "").toLowerCase());
        sb.append(".png");
        file = CertificationImageConverter.class.getResource(sb.toString());
      }

      if (file != null) {
        return new ImageIcon(file);
      }
    }
    catch (Exception e) {
      LOGGER.warn("cannot convert certification", e);
    }

    return emptyImage;
  }

  @Override
  public Certification convertReverse(Icon arg0) {
    return null;
  }

}
