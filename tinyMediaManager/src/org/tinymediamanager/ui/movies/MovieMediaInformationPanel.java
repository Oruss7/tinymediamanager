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
import java.util.List;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.jdesktop.beansbinding.AutoBinding;
import org.jdesktop.beansbinding.AutoBinding.UpdateStrategy;
import org.jdesktop.beansbinding.BeanProperty;
import org.jdesktop.beansbinding.Bindings;
import org.jdesktop.swingbinding.JTableBinding;
import org.jdesktop.swingbinding.SwingBindings;
import org.tinymediamanager.core.MediaFile;
import org.tinymediamanager.ui.LinkLabel;

import com.jgoodies.forms.factories.FormFactory;
import com.jgoodies.forms.layout.ColumnSpec;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.RowSpec;

// TODO: Auto-generated Javadoc
/**
 * The Class MovieMediaInformationPanel.
 */
public class MovieMediaInformationPanel extends JPanel {

  /** The Constant serialVersionUID. */
  private static final long   serialVersionUID = 1L;

  /** The logger. */
  private final static Logger LOGGER           = Logger.getLogger(MovieMediaInformationPanel.class);

  /** The movie selection model. */
  private MovieSelectionModel movieSelectionModel;

  /** The lbl files t. */
  private JLabel              lblFilesT;

  /** The scroll pane files. */
  private JScrollPane         scrollPaneFiles;

  /** The table files. */
  private JTable              tableFiles;

  /** The lbl movie path. */
  private LinkLabel           lblMoviePath;

  /** The lbl date added t. */
  private JLabel              lblDateAddedT;

  /** The lbl date added. */
  private JLabel              lblDateAdded;

  /** The cb watched. */
  private JCheckBox           cbWatched;

  /** The lbl watched t. */
  private JLabel              lblWatchedT;

  /** The lbl movie path t. */
  private JLabel              lblMoviePathT;
  private JButton             btnPlay;

  /**
   * Instantiates a new movie media information panel.
   * 
   * @param model
   *          the model
   */
  public MovieMediaInformationPanel(MovieSelectionModel model) {
    this.movieSelectionModel = model;

    setLayout(new FormLayout(new ColumnSpec[] { FormFactory.RELATED_GAP_COLSPEC, FormFactory.DEFAULT_COLSPEC, FormFactory.RELATED_GAP_COLSPEC,
        ColumnSpec.decode("default:grow"), FormFactory.RELATED_GAP_COLSPEC, FormFactory.DEFAULT_COLSPEC, FormFactory.RELATED_GAP_COLSPEC,
        ColumnSpec.decode("default:grow"), FormFactory.RELATED_GAP_COLSPEC, FormFactory.DEFAULT_COLSPEC, FormFactory.RELATED_GAP_COLSPEC, },
        new RowSpec[] { FormFactory.RELATED_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC, FormFactory.NARROW_LINE_GAP_ROWSPEC,
            FormFactory.DEFAULT_ROWSPEC, FormFactory.NARROW_LINE_GAP_ROWSPEC, RowSpec.decode("default:grow"), FormFactory.NARROW_LINE_GAP_ROWSPEC, }));

    lblDateAddedT = new JLabel("Added on");
    add(lblDateAddedT, "2, 2");

    lblDateAdded = new JLabel("");
    add(lblDateAdded, "4, 2");

    lblWatchedT = new JLabel("Watched");
    add(lblWatchedT, "6, 2");

    cbWatched = new JCheckBox("");
    cbWatched.setEnabled(false);
    add(cbWatched, "8, 2");

    btnPlay = new JButton("Play");
    btnPlay.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent arg0) {
        if (!StringUtils.isEmpty(lblMoviePath.getNormalText())) {
          try {
            // get the location from the label
            StringBuilder movieFile = new StringBuilder(lblMoviePath.getNormalText());
            movieFile.append(File.separator);
            movieFile.append(movieSelectionModel.getSelectedMovie().getMediaFiles().get(0).getFilename());
            File path = new File(movieFile.toString());
            // check whether this location exists
            if (path.exists()) {
              System.out.println(movieFile.toString());
              Desktop.getDesktop().open(path);
            }
          }
          catch (Exception ex) {
            LOGGER.error("open filemanager", ex);
          }
        }
      }
    });
    add(btnPlay, "10, 2");

    lblMoviePathT = new JLabel("Path");
    add(lblMoviePathT, "2, 4");

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
    add(lblMoviePath, "4, 4, 5, 1");

    lblFilesT = new JLabel("Files");
    add(lblFilesT, "2, 6, default, top");

    scrollPaneFiles = new JScrollPane();
    add(scrollPaneFiles, "4, 6, 5, 1, fill, fill");

    tableFiles = new JTable();
    lblFilesT.setLabelFor(tableFiles);
    scrollPaneFiles.setViewportView(tableFiles);

    initDataBindings();
  }

  /**
   * Inits the data bindings.
   */
  protected void initDataBindings() {
    BeanProperty<JLabel, String> jLabelBeanProperty = BeanProperty.create("text");
    //
    BeanProperty<MovieSelectionModel, Integer> movieSelectionModelBeanProperty = BeanProperty.create("selectedMovie.dateAdded.date");
    AutoBinding<MovieSelectionModel, Integer, JLabel, String> autoBinding = Bindings.createAutoBinding(UpdateStrategy.READ, movieSelectionModel,
        movieSelectionModelBeanProperty, lblDateAdded, jLabelBeanProperty);
    autoBinding.bind();
    //
    BeanProperty<MovieSelectionModel, Boolean> movieSelectionModelBeanProperty_1 = BeanProperty.create("selectedMovie.watched");
    BeanProperty<JCheckBox, Boolean> jCheckBoxBeanProperty = BeanProperty.create("selected");
    AutoBinding<MovieSelectionModel, Boolean, JCheckBox, Boolean> autoBinding_1 = Bindings.createAutoBinding(UpdateStrategy.READ,
        movieSelectionModel, movieSelectionModelBeanProperty_1, cbWatched, jCheckBoxBeanProperty);
    autoBinding_1.bind();
    //
    BeanProperty<MovieSelectionModel, Integer> movieSelectionModelBeanProperty_2 = BeanProperty.create("selectedMovie.dateAdded.day");
    AutoBinding<MovieSelectionModel, Integer, JLabel, String> autoBinding_2 = Bindings.createAutoBinding(UpdateStrategy.READ, movieSelectionModel,
        movieSelectionModelBeanProperty_2, lblDateAdded, jLabelBeanProperty);
    autoBinding_2.bind();
    //
    BeanProperty<MovieSelectionModel, String> movieSelectionModelBeanProperty_3 = BeanProperty.create("selectedMovie.dateAddedAsString");
    AutoBinding<MovieSelectionModel, String, JLabel, String> autoBinding_3 = Bindings.createAutoBinding(UpdateStrategy.READ, movieSelectionModel,
        movieSelectionModelBeanProperty_3, lblDateAdded, jLabelBeanProperty);
    autoBinding_3.bind();
    //
    BeanProperty<MovieSelectionModel, String> movieSelectionModelBeanProperty_13 = BeanProperty.create("selectedMovie.path");
    AutoBinding<MovieSelectionModel, String, JLabel, String> autoBinding_19 = Bindings.createAutoBinding(UpdateStrategy.READ, movieSelectionModel,
        movieSelectionModelBeanProperty_13, lblMoviePath, jLabelBeanProperty);
    autoBinding_19.bind();
    BeanProperty<MovieSelectionModel, List<MediaFile>> movieSelectionModelBeanProperty_18 = BeanProperty.create("selectedMovie.mediaFiles");
    JTableBinding<MediaFile, MovieSelectionModel, JTable> jTableBinding_1 = SwingBindings.createJTableBinding(UpdateStrategy.READ,
        movieSelectionModel, movieSelectionModelBeanProperty_18, tableFiles);
    //
    BeanProperty<MediaFile, String> mediaFileBeanProperty = BeanProperty.create("filename");
    jTableBinding_1.addColumnBinding(mediaFileBeanProperty).setColumnName("Filename").setEditable(false);
    //
    BeanProperty<MediaFile, String> mediaFileBeanProperty_1 = BeanProperty.create("filesizeInMegabytes");
    jTableBinding_1.addColumnBinding(mediaFileBeanProperty_1).setColumnName("Size").setEditable(false);
    //
    jTableBinding_1.setEditable(false);
    jTableBinding_1.bind();
    //
  }
}
