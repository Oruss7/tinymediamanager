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
package org.tinymediamanager.ui.movies;

import java.awt.Desktop;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.net.URI;

import javax.swing.JLabel;
import javax.swing.JPanel;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.jdesktop.beansbinding.AutoBinding;
import org.jdesktop.beansbinding.AutoBinding.UpdateStrategy;
import org.jdesktop.beansbinding.BeanProperty;
import org.jdesktop.beansbinding.Bindings;
import org.tinymediamanager.core.movie.MovieSet;
import org.tinymediamanager.ui.LinkLabel;

import com.jgoodies.forms.factories.FormFactory;
import com.jgoodies.forms.layout.ColumnSpec;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.RowSpec;
import com.jgoodies.forms.layout.Sizes;

// TODO: Auto-generated Javadoc
/**
 * The Class MovieDetailsPanel.
 */
public class MovieDetailsPanel extends JPanel {

  /** The Constant serialVersionUID. */
  private static final long   serialVersionUID = 1L;

  /** The logger. */
  private final static Logger LOGGER           = Logger.getLogger(MovieDetailsPanel.class);

  /** The movie selection model. */
  private MovieSelectionModel movieSelectionModel;

  /** The lbl original title t. */
  private JLabel              lblOriginalTitleT;

  /** The lbl original title. */
  private JLabel              lblOriginalTitle;

  /** The lbl production t. */
  private JLabel              lblProductionT;

  /** The lbl production. */
  // private JTextArea lblProduction;
  private JLabel              lblProduction;

  /** The lbl genres t. */
  private JLabel              lblGenresT;

  /** The lbl genres. */
  private JLabel              lblGenres;

  /** The lbl certification t. */
  private JLabel              lblCertificationT;

  /** The lbl certification. */
  private JLabel              lblCertification;

  /** The lbl imdb id t. */
  private JLabel              lblImdbIdT;

  /** The lbl tmdb id t. */
  private JLabel              lblTmdbIdT;

  /** The lbl imdb id. */
  private LinkLabel           lblImdbId;

  /** The lbl tmdb id. */
  private LinkLabel           lblTmdbId;

  /** The lbl runtime t. */
  private JLabel              lblRuntimeT;

  /** The lbl runtime. */
  private JLabel              lblRuntime;

  /** The lbl minutes. */
  private JLabel              lblMinutes;

  /** The lbl tags t. */
  private JLabel              lblTagsT;

  /** The lbl tags. */
  private JLabel              lblTags;

  /** The lbl movie path t. */
  private JLabel              lblMoviePathT;

  /** The lbl movie path. */
  private LinkLabel           lblMoviePath;
  private JLabel              lblMoviesetT;
  private JLabel              lblMovieSet;

  /**
   * Instantiates a new movie details panel.
   * 
   * @param model
   *          the model
   */
  public MovieDetailsPanel(MovieSelectionModel model) {
    this.movieSelectionModel = model;

    setLayout(new FormLayout(new ColumnSpec[] { FormFactory.RELATED_GAP_COLSPEC, FormFactory.DEFAULT_COLSPEC, FormFactory.UNRELATED_GAP_COLSPEC,
        ColumnSpec.decode("25px"), FormFactory.RELATED_GAP_COLSPEC, ColumnSpec.decode("default:grow"), FormFactory.RELATED_GAP_COLSPEC,
        FormFactory.DEFAULT_COLSPEC, FormFactory.UNRELATED_GAP_COLSPEC, ColumnSpec.decode("default:grow"), FormFactory.RELATED_GAP_COLSPEC, },
        new RowSpec[] { FormFactory.RELATED_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC, FormFactory.NARROW_LINE_GAP_ROWSPEC,
            FormFactory.DEFAULT_ROWSPEC, FormFactory.NARROW_LINE_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC, FormFactory.NARROW_LINE_GAP_ROWSPEC,
            FormFactory.DEFAULT_ROWSPEC, FormFactory.NARROW_LINE_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC, FormFactory.NARROW_LINE_GAP_ROWSPEC,
            new RowSpec(RowSpec.CENTER, Sizes.bounded(Sizes.MINIMUM, Sizes.constant("15px", false), Sizes.constant("50px", false)), 0),
            FormFactory.NARROW_LINE_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC, FormFactory.NARROW_LINE_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC,
            FormFactory.NARROW_LINE_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC, }));

    lblOriginalTitleT = new JLabel("Original Title");
    add(lblOriginalTitleT, "2, 2");

    lblOriginalTitle = new JLabel("");
    add(lblOriginalTitle, "4, 2, 7, 1");

    lblMoviesetT = new JLabel("Movieset");
    add(lblMoviesetT, "2, 4");

    lblMovieSet = new JLabel("");
    add(lblMovieSet, "4, 4, 3, 1");

    lblGenresT = new JLabel("Genres");
    add(lblGenresT, "2, 6");
    lblGenresT.setLabelFor(lblGenres);

    lblGenres = new JLabel("");
    add(lblGenres, "4, 6, 7, 1");

    lblRuntimeT = new JLabel("Runtime");
    add(lblRuntimeT, "2, 8");
    lblRuntimeT.setLabelFor(lblRuntime);

    lblRuntime = new JLabel("");
    add(lblRuntime, "4, 8");

    lblMinutes = new JLabel("min");
    add(lblMinutes, "6, 8");

    lblCertificationT = new JLabel("Certification");
    add(lblCertificationT, "2, 10");
    lblCertificationT.setLabelFor(lblCertification);

    lblCertification = new JLabel("");
    add(lblCertification, "4, 10, 7, 1");

    lblProductionT = new JLabel("Production");
    add(lblProductionT, "2, 12, default, top");
    lblProductionT.setLabelFor(lblProduction);

    // lblProduction = new JTextArea("");
    // lblProduction.setLineWrap(true);
    // lblProduction.setEditable(false);
    // lblProduction.setOpaque(false);
    // lblProduction.setWrapStyleWord(true);
    lblProduction = new JLabel();
    add(lblProduction, "4, 12, 7, 1");

    lblTagsT = new JLabel("Tags");
    add(lblTagsT, "2, 14");

    lblTags = new JLabel("");
    add(lblTags, "4, 14, 7, 1");

    lblImdbIdT = new JLabel("IMDB Id");
    add(lblImdbIdT, "2, 16");

    lblImdbId = new LinkLabel("");
    lblImdbId.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent arg0) {
        try {
          Desktop.getDesktop().browse(new URI("http://www.imdb.com/title/" + lblImdbId.getNormalText()));
        }
        catch (Exception e) {
          LOGGER.error("browse to imdbid", e);
        }
      }
    });

    add(lblImdbId, "4, 16, 3, 1, left, default");
    lblImdbIdT.setLabelFor(lblImdbId);

    lblTmdbIdT = new JLabel("TMDB Id");
    add(lblTmdbIdT, "8, 16");

    lblTmdbId = new LinkLabel("");
    lblTmdbId.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent arg0) {
        try {
          Desktop.getDesktop().browse(new URI("http://www.themoviedb.org/movie/" + lblTmdbId.getNormalText()));
        }
        catch (Exception e) {
          LOGGER.error("browse to tmdbid", e);
        }
      }
    });
    add(lblTmdbId, "10, 16, left, default");
    lblTmdbIdT.setLabelFor(lblTmdbId);

    lblMoviePathT = new JLabel("Path");
    add(lblMoviePathT, "2, 18");

    lblMoviePath = new LinkLabel("");
    lblMoviePath.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent arg0) {
        if (!StringUtils.isEmpty(lblMoviePath.getNormalText())) {
          try {
            // get the location from the label
            File path = new File(lblMoviePath.getNormalText());
            // check whether this location exists
            if (path.exists()) {
              Desktop.getDesktop().open(path);
            }
          }
          catch (Exception ex) {
            LOGGER.error("open filemanager", ex);
          }
        }
      }
    });
    lblMoviePathT.setLabelFor(lblMoviePath);
    lblMoviePathT.setLabelFor(lblMoviePath);
    add(lblMoviePath, "4, 18, 7, 1");

    initDataBindings();
  }

  protected void initDataBindings() {
    BeanProperty<MovieSelectionModel, String> movieSelectionModelBeanProperty_6 = BeanProperty.create("selectedMovie.originalName");
    BeanProperty<JLabel, String> jLabelBeanProperty = BeanProperty.create("text");
    AutoBinding<MovieSelectionModel, String, JLabel, String> autoBinding_7 = Bindings.createAutoBinding(UpdateStrategy.READ, movieSelectionModel,
        movieSelectionModelBeanProperty_6, lblOriginalTitle, jLabelBeanProperty);
    autoBinding_7.bind();
    //
    BeanProperty<MovieSelectionModel, String> movieSelectionModelBeanProperty_7 = BeanProperty.create("selectedMovie.genresAsString");
    AutoBinding<MovieSelectionModel, String, JLabel, String> autoBinding_8 = Bindings.createAutoBinding(UpdateStrategy.READ, movieSelectionModel,
        movieSelectionModelBeanProperty_7, lblGenres, jLabelBeanProperty);
    autoBinding_8.bind();
    //
    BeanProperty<MovieSelectionModel, Integer> movieSelectionModelBeanProperty_8 = BeanProperty.create("selectedMovie.runtime");
    AutoBinding<MovieSelectionModel, Integer, JLabel, String> autoBinding_9 = Bindings.createAutoBinding(UpdateStrategy.READ, movieSelectionModel,
        movieSelectionModelBeanProperty_8, lblRuntime, jLabelBeanProperty);
    autoBinding_9.bind();
    //
    BeanProperty<MovieSelectionModel, String> movieSelectionModelBeanProperty_11 = BeanProperty.create("selectedMovie.imdbId");
    BeanProperty<LinkLabel, String> linkLabelBeanProperty = BeanProperty.create("text");
    AutoBinding<MovieSelectionModel, String, LinkLabel, String> autoBinding_12 = Bindings.createAutoBinding(UpdateStrategy.READ, movieSelectionModel,
        movieSelectionModelBeanProperty_11, lblImdbId, linkLabelBeanProperty);
    autoBinding_12.bind();
    //
    BeanProperty<MovieSelectionModel, Integer> movieSelectionModelBeanProperty_12 = BeanProperty.create("selectedMovie.tmdbId");
    AutoBinding<MovieSelectionModel, Integer, LinkLabel, String> autoBinding_13 = Bindings.createAutoBinding(UpdateStrategy.READ,
        movieSelectionModel, movieSelectionModelBeanProperty_12, lblTmdbId, linkLabelBeanProperty);
    autoBinding_13.bind();
    //
    BeanProperty<MovieSelectionModel, String> movieSelectionModelBeanProperty_9 = BeanProperty.create("selectedMovie.certification.name");
    AutoBinding<MovieSelectionModel, String, JLabel, String> autoBinding_10 = Bindings.createAutoBinding(UpdateStrategy.READ, movieSelectionModel,
        movieSelectionModelBeanProperty_9, lblCertification, jLabelBeanProperty);
    autoBinding_10.bind();
    //
    BeanProperty<MovieSelectionModel, String> movieSelectionModelBeanProperty_4 = BeanProperty.create("selectedMovie.tagAsString");
    AutoBinding<MovieSelectionModel, String, JLabel, String> autoBinding_4 = Bindings.createAutoBinding(UpdateStrategy.READ, movieSelectionModel,
        movieSelectionModelBeanProperty_4, lblTags, jLabelBeanProperty);
    autoBinding_4.bind();
    //
    BeanProperty<MovieSelectionModel, String> movieSelectionModelBeanProperty_5 = BeanProperty.create("selectedMovie.productionCompany");
    AutoBinding<MovieSelectionModel, String, JLabel, String> autoBinding_5 = Bindings.createAutoBinding(UpdateStrategy.READ, movieSelectionModel,
        movieSelectionModelBeanProperty_5, lblProduction, jLabelBeanProperty);
    autoBinding_5.bind();
    //
    BeanProperty<MovieSelectionModel, String> movieSelectionModelBeanProperty = BeanProperty.create("selectedMovie.path");
    AutoBinding<MovieSelectionModel, String, LinkLabel, String> autoBinding = Bindings.createAutoBinding(UpdateStrategy.READ, movieSelectionModel,
        movieSelectionModelBeanProperty, lblMoviePath, linkLabelBeanProperty);
    autoBinding.bind();
    //
    BeanProperty<MovieSelectionModel, MovieSet> movieSelectionModelBeanProperty_1 = BeanProperty.create("selectedMovie.movieSet");
    AutoBinding<MovieSelectionModel, MovieSet, JLabel, String> autoBinding_1 = Bindings.createAutoBinding(UpdateStrategy.READ, movieSelectionModel,
        movieSelectionModelBeanProperty_1, lblMovieSet, jLabelBeanProperty);
    autoBinding_1.setConverter(new MovieSetConverter());
    autoBinding_1.bind();
  }
}
