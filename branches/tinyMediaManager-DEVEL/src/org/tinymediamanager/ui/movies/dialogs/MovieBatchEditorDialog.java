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
package org.tinymediamanager.ui.movies.dialogs;

import java.awt.BorderLayout;
import java.awt.Cursor;
import java.awt.FlowLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.List;
import java.util.ResourceBundle;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.tinymediamanager.core.movie.MovieList;
import org.tinymediamanager.core.movie.entities.Movie;
import org.tinymediamanager.core.movie.entities.MovieSet;
import org.tinymediamanager.scraper.MediaGenres;
import org.tinymediamanager.ui.IconManager;
import org.tinymediamanager.ui.MainWindow;
import org.tinymediamanager.ui.TmmWindowSaver;
import org.tinymediamanager.ui.UTF8Control;
import org.tinymediamanager.ui.components.AutocompleteComboBox;

import com.jgoodies.forms.factories.FormFactory;
import com.jgoodies.forms.layout.ColumnSpec;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.RowSpec;

/**
 * The Class MovieBatchEditor.
 * 
 * @author Manuel Laggner
 */
public class MovieBatchEditorDialog extends JDialog {
  private static final long           serialVersionUID = -8515248604267310279L;
  private static final ResourceBundle BUNDLE           = ResourceBundle.getBundle("messages", new UTF8Control()); //$NON-NLS-1$

  private MovieList                   movieList        = MovieList.getInstance();
  private List<Movie>                 moviesToEdit;
  private boolean                     changed          = false;

  private JComboBox                   cbGenres;
  private JComboBox                   cbTags;
  private JComboBox                   cbMovieSet;
  private JCheckBox                   chckbxWatched;

  /**
   * Instantiates a new movie batch editor.
   * 
   * @param movies
   *          the movies
   */
  public MovieBatchEditorDialog(final List<Movie> movies) {
    setModal(true);
    setIconImage(MainWindow.LOGO);
    setTitle(BUNDLE.getString("movie.edit")); //$NON-NLS-1$
    setName("movieBatchEditor");
    setBounds(5, 5, 350, 230);
    TmmWindowSaver.loadSettings(this);
    getContentPane().setLayout(new BorderLayout(0, 0));

    {
      JPanel panelContent = new JPanel();
      getContentPane().add(panelContent, BorderLayout.CENTER);
      panelContent.setLayout(new FormLayout(new ColumnSpec[] { FormFactory.RELATED_GAP_COLSPEC, FormFactory.DEFAULT_COLSPEC,
          FormFactory.RELATED_GAP_COLSPEC, ColumnSpec.decode("default:grow"), FormFactory.RELATED_GAP_COLSPEC, FormFactory.DEFAULT_COLSPEC,
          FormFactory.RELATED_GAP_COLSPEC, FormFactory.DEFAULT_COLSPEC, FormFactory.RELATED_GAP_COLSPEC, }, new RowSpec[] {
          FormFactory.RELATED_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC, FormFactory.RELATED_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC,
          FormFactory.RELATED_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC, FormFactory.RELATED_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC, }));

      JLabel lblGenres = new JLabel(BUNDLE.getString("metatag.genre")); //$NON-NLS-1$
      panelContent.add(lblGenres, "2, 2, right, default");

      // cbGenres = new JComboBox(MediaGenres2.values());
      cbGenres = new AutocompleteComboBox(MediaGenres.values());
      cbGenres.setEditable(true);
      panelContent.add(cbGenres, "4, 2, fill, default");

      JButton btnAddGenre = new JButton("");
      btnAddGenre.setIcon(IconManager.LIST_ADD);
      btnAddGenre.setMargin(new Insets(2, 2, 2, 2));
      btnAddGenre.addActionListener(new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent e) {
          changed = true;
          setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
          MediaGenres genre = null;
          Object item = cbGenres.getSelectedItem();

          // genre
          if (item instanceof MediaGenres) {
            genre = (MediaGenres) item;
          }

          // newly created genre?
          if (item instanceof String) {
            genre = MediaGenres.getGenre((String) item);
          }
          // MediaGenres2 genre = (MediaGenres2) cbGenres.getSelectedItem();
          if (genre != null) {
            for (Movie movie : moviesToEdit) {
              movie.addGenre(genre);
            }
          }
          setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
        }
      });
      panelContent.add(btnAddGenre, "6, 2");

      JButton btnRemoveGenre = new JButton("");
      btnRemoveGenre.setIcon(IconManager.LIST_REMOVE);
      btnRemoveGenre.setMargin(new Insets(2, 2, 2, 2));
      btnRemoveGenre.addActionListener(new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent e) {
          changed = true;
          setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
          MediaGenres genre = (MediaGenres) cbGenres.getSelectedItem();
          for (Movie movie : moviesToEdit) {
            movie.removeGenre(genre);
          }
          setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
        }
      });
      panelContent.add(btnRemoveGenre, "8, 2");

      JLabel lblTags = new JLabel("Tag");
      panelContent.add(lblTags, "2, 4, right, default");

      cbTags = new AutocompleteComboBox(movieList.getTagsInMovies().toArray());
      cbTags.setEditable(true);
      panelContent.add(cbTags, "4, 4, fill, default");

      JButton btnAddTag = new JButton("");
      btnAddTag.setIcon(IconManager.LIST_ADD);
      btnAddTag.setMargin(new Insets(2, 2, 2, 2));
      btnAddTag.addActionListener(new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent e) {
          changed = true;
          setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
          String tag = (String) cbTags.getSelectedItem();
          for (Movie movie : moviesToEdit) {
            movie.addToTags(tag);
          }
          setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
        }
      });
      panelContent.add(btnAddTag, "6, 4");

      JButton btnRemoveTag = new JButton("");
      btnRemoveTag.setIcon(IconManager.LIST_REMOVE);
      btnRemoveTag.setMargin(new Insets(2, 2, 2, 2));
      btnRemoveTag.addActionListener(new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent e) {
          changed = true;
          setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
          String tag = (String) cbTags.getSelectedItem();
          for (Movie movie : moviesToEdit) {
            movie.removeFromTags(tag);
          }
          setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
        }
      });
      panelContent.add(btnRemoveTag, "8, 4");

      JLabel lblMovieSet = new JLabel(BUNDLE.getString("metatag.movieset")); //$NON-NLS-1$
      panelContent.add(lblMovieSet, "2, 6, right, default");

      cbMovieSet = new JComboBox();
      panelContent.add(cbMovieSet, "4, 6, fill, default");

      JButton btnSetMovieSet = new JButton("");
      btnSetMovieSet.setMargin(new Insets(2, 2, 2, 2));
      btnSetMovieSet.setIcon(IconManager.APPLY);
      btnSetMovieSet.addActionListener(new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent e) {
          changed = true;
          setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
          // movie set
          Object obj = cbMovieSet.getSelectedItem();
          for (Movie movie : moviesToEdit) {
            if (obj instanceof String) {
              movie.removeFromMovieSet();
              movie.setSortTitle("");
            }
            if (obj instanceof MovieSet) {
              MovieSet movieSet = (MovieSet) obj;

              if (movie.getMovieSet() != movieSet) {
                movie.removeFromMovieSet();
                movie.setMovieSet(movieSet);
                // movieSet.addMovie(movie);
                movieSet.insertMovie(movie);
              }

              // movie.setSortTitleFromMovieSet();
              // movie.saveToDb();
              movieSet.updateMovieSorttitle();
            }
          }
          setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
        }
      });
      panelContent.add(btnSetMovieSet, "6, 6");

      JLabel lblWatched = new JLabel(BUNDLE.getString("metatag.watched")); //$NON-NLS-1$
      panelContent.add(lblWatched, "2, 8, right, default");

      chckbxWatched = new JCheckBox("");
      panelContent.add(chckbxWatched, "4, 8");

      JButton btnWatched = new JButton("");
      btnWatched.setMargin(new Insets(2, 2, 2, 2));
      btnWatched.setIcon(IconManager.APPLY);
      btnWatched.addActionListener(new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent e) {
          changed = true;
          setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
          for (Movie movie : moviesToEdit) {
            movie.setWatched(chckbxWatched.isSelected());
          }
          setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
        }
      });
      panelContent.add(btnWatched, "6, 8");
    }

    {
      JPanel panelButtons = new JPanel();
      FlowLayout flowLayout = (FlowLayout) panelButtons.getLayout();
      flowLayout.setAlignment(FlowLayout.RIGHT);
      getContentPane().add(panelButtons, BorderLayout.SOUTH);

      JButton btnClose = new JButton(BUNDLE.getString("Button.close")); //$NON-NLS-1$
      btnClose.setIcon(IconManager.APPLY);
      btnClose.addActionListener(new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent arg0) {
          // rewrite movies, if anything changed
          if (changed) {
            setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
            for (Movie movie : moviesToEdit) {
              movie.saveToDb();
              movie.writeNFO();
            }
            setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
          }
          setVisible(false);
          dispose();
        }
      });
      panelButtons.add(btnClose);

      // add window listener to write changes (if the window close button "X" is
      // pressed)
      addWindowListener(new WindowAdapter() {
        @Override
        public void windowClosing(WindowEvent e) {
          // rewrite movies, if anything changed
          if (changed) {
            for (Movie movie : moviesToEdit) {
              movie.saveToDb();
              movie.writeNFO();
            }
          }
        }
      });
    }

    {
      cbMovieSet.addItem("");

      for (MovieSet movieSet : movieList.getSortedMovieSetList()) {
        cbMovieSet.addItem(movieSet);
      }

      moviesToEdit = movies;
    }
  }
}
