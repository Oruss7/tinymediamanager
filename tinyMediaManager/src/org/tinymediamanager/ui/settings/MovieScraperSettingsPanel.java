/*
 * Copyright 2012 Manuel Laggner
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
package org.tinymediamanager.ui.settings;

import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.ButtonGroup;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.TitledBorder;

import org.jdesktop.beansbinding.AutoBinding;
import org.jdesktop.beansbinding.AutoBinding.UpdateStrategy;
import org.jdesktop.beansbinding.BeanProperty;
import org.jdesktop.beansbinding.Bindings;
import org.tinymediamanager.core.Settings;
import org.tinymediamanager.core.movie.MovieScrapers;
import org.tinymediamanager.scraper.CountryCode;
import org.tinymediamanager.scraper.tmdb.TmdbMetadataProvider;
import org.tinymediamanager.scraper.tmdb.TmdbMetadataProvider.Languages;

import com.jgoodies.forms.factories.FormFactory;
import com.jgoodies.forms.layout.ColumnSpec;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.RowSpec;

// TODO: Auto-generated Javadoc
/**
 * The Class MovieScraperSettingsPanel.
 */
public class MovieScraperSettingsPanel extends JPanel {

  /** The settings. */
  private Settings    settings = Settings.getInstance();

  /** The button group scraper. */
  private ButtonGroup buttonGroupScraper;
  /** The cb scraper tmdb language. */
  private JComboBox   cbScraperTmdbLanguage;

  /** The cb country. */
  private JComboBox   cbCountry;

  /** The cb imdb translateable content. */
  private JCheckBox   cbImdbTranslateableContent;

  /** The cb scraper imdb. */
  private JCheckBox   cbScraperImdb;
  private JCheckBox   chckbxAutomaticallyScrapeImages;

  /**
   * Instantiates a new movie scraper settings panel.
   */
  public MovieScraperSettingsPanel() {
    setLayout(new FormLayout(new ColumnSpec[] { FormFactory.RELATED_GAP_COLSPEC, FormFactory.DEFAULT_COLSPEC, }, new RowSpec[] {
        FormFactory.RELATED_GAP_ROWSPEC, RowSpec.decode("177px"), FormFactory.RELATED_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC, }));
    JPanel panelMovieScrapers = new JPanel();
    panelMovieScrapers.setBorder(new TitledBorder(null, "Scrapers", TitledBorder.LEADING, TitledBorder.TOP, null, null));
    add(panelMovieScrapers, "2, 2, left, top");
    panelMovieScrapers.setLayout(new FormLayout(new ColumnSpec[] { FormFactory.DEFAULT_COLSPEC, FormFactory.RELATED_GAP_COLSPEC,
        FormFactory.DEFAULT_COLSPEC, }, new RowSpec[] { FormFactory.RELATED_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC,
        FormFactory.RELATED_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC, FormFactory.RELATED_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC,
        FormFactory.RELATED_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC, FormFactory.RELATED_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC, }));

    JCheckBox cbScraperTmdb = new JCheckBox("The Movie Database");
    buttonGroupScraper = new ButtonGroup();
    buttonGroupScraper.add(cbScraperTmdb);
    cbScraperTmdb.setSelected(true);
    panelMovieScrapers.add(cbScraperTmdb, "1, 2");

    JLabel lblScraperTmdbLanguage = new JLabel("Language");
    panelMovieScrapers.add(lblScraperTmdbLanguage, "1, 4, right, default");

    cbScraperTmdbLanguage = new JComboBox(TmdbMetadataProvider.Languages.values());
    panelMovieScrapers.add(cbScraperTmdbLanguage, "3, 4");

    JLabel lblCountry = new JLabel("Certification country");
    panelMovieScrapers.add(lblCountry, "1, 6, right, default");

    cbCountry = new JComboBox(CountryCode.values());
    panelMovieScrapers.add(cbCountry, "3, 6, fill, default");

    cbScraperImdb = new JCheckBox("IMDB");
    buttonGroupScraper.add(cbScraperImdb);
    panelMovieScrapers.add(cbScraperImdb, "1, 8");

    JLabel lblEperimental = new JLabel("experimental!");
    panelMovieScrapers.add(lblEperimental, "3, 8");

    cbImdbTranslateableContent = new JCheckBox("Plot/Title/Tagline from TMDB");
    panelMovieScrapers.add(cbImdbTranslateableContent, "3, 10");

    chckbxAutomaticallyScrapeImages = new JCheckBox("automatically scrape images");
    add(chckbxAutomaticallyScrapeImages, "2, 4");

    initDataBindings();

    // set movie Scrapers
    MovieScrapers movieScraper = settings.getMovieScraper();
    switch (movieScraper) {
      case IMDB:
        cbScraperImdb.setSelected(true);
        break;

      case TMDB:
      default:
        cbScraperTmdb.setSelected(true);
    }

    cbScraperImdb.addItemListener(new ItemListener() {
      public void itemStateChanged(ItemEvent e) {
        checkChanges();
      }
    });
    cbScraperTmdb.addItemListener(new ItemListener() {
      public void itemStateChanged(ItemEvent e) {
        checkChanges();
      }
    });

  }

  /**
   * Check changes.
   */
  public void checkChanges() {
    // save scraper
    if (cbScraperImdb.isSelected()) {
      settings.setMovieScraper(MovieScrapers.IMDB);
    }
    else {
      settings.setMovieScraper(MovieScrapers.TMDB);
    }
  }

  protected void initDataBindings() {
    BeanProperty<Settings, Languages> settingsBeanProperty_8 = BeanProperty.create("scraperTmdbLanguage");
    BeanProperty<JComboBox, Object> jComboBoxBeanProperty = BeanProperty.create("selectedItem");
    AutoBinding<Settings, Languages, JComboBox, Object> autoBinding_7 = Bindings.createAutoBinding(UpdateStrategy.READ_WRITE, settings,
        settingsBeanProperty_8, cbScraperTmdbLanguage, jComboBoxBeanProperty);
    autoBinding_7.bind();
    //
    BeanProperty<Settings, CountryCode> settingsBeanProperty_9 = BeanProperty.create("certificationCountry");
    AutoBinding<Settings, CountryCode, JComboBox, Object> autoBinding_8 = Bindings.createAutoBinding(UpdateStrategy.READ_WRITE, settings,
        settingsBeanProperty_9, cbCountry, jComboBoxBeanProperty);
    autoBinding_8.bind();
    //
    BeanProperty<Settings, Boolean> settingsBeanProperty_13 = BeanProperty.create("imdbScrapeForeignLanguage");
    BeanProperty<JCheckBox, Boolean> jCheckBoxBeanProperty = BeanProperty.create("selected");
    AutoBinding<Settings, Boolean, JCheckBox, Boolean> autoBinding_12 = Bindings.createAutoBinding(UpdateStrategy.READ_WRITE, settings,
        settingsBeanProperty_13, cbImdbTranslateableContent, jCheckBoxBeanProperty);
    autoBinding_12.bind();
    //
    BeanProperty<Settings, Boolean> settingsBeanProperty = BeanProperty.create("scrapeBestImage");
    AutoBinding<Settings, Boolean, JCheckBox, Boolean> autoBinding = Bindings.createAutoBinding(UpdateStrategy.READ_WRITE, settings,
        settingsBeanProperty, chckbxAutomaticallyScrapeImages, jCheckBoxBeanProperty);
    autoBinding.bind();
  }
}
