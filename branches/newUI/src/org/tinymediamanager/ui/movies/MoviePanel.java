/*
 * Copyright 2012 - 2013 Manuel Laggner
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

import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.net.URL;
import java.util.ResourceBundle;

import javax.swing.Action;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.JToolBar;
import javax.swing.ListSelectionModel;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;

import org.gpl.JSplitButton.JSplitButton;
import org.gpl.JSplitButton.action.SplitButtonActionListener;
import org.jdesktop.beansbinding.AutoBinding;
import org.jdesktop.beansbinding.AutoBinding.UpdateStrategy;
import org.jdesktop.beansbinding.BeanProperty;
import org.jdesktop.beansbinding.Bindings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tinymediamanager.Globals;
import org.tinymediamanager.core.movie.Movie;
import org.tinymediamanager.core.movie.MovieList;
import org.tinymediamanager.ui.BorderCellRenderer;
import org.tinymediamanager.ui.IconHeaderRenderer;
import org.tinymediamanager.ui.MainWindow;
import org.tinymediamanager.ui.UTF8Control;
import org.tinymediamanager.ui.components.JSearchTextField;
import org.tinymediamanager.ui.components.ZebraJTable;
import org.tinymediamanager.ui.movies.actions.MovieBatchEditAction;
import org.tinymediamanager.ui.movies.actions.MovieEditAction;
import org.tinymediamanager.ui.movies.actions.MovieExportAction;
import org.tinymediamanager.ui.movies.actions.MovieMediaInformationAction;
import org.tinymediamanager.ui.movies.actions.MovieRemoveAction;
import org.tinymediamanager.ui.movies.actions.MovieRenameAction;
import org.tinymediamanager.ui.movies.actions.MovieRewriteNfoAction;
import org.tinymediamanager.ui.movies.actions.MovieSelectedScrapeAction;
import org.tinymediamanager.ui.movies.actions.MovieSelectedScrapeMetadataAction;
import org.tinymediamanager.ui.movies.actions.MovieSingleScrapeAction;
import org.tinymediamanager.ui.movies.actions.MovieUnscrapedScrapeAction;
import org.tinymediamanager.ui.movies.actions.MovieUpdateDatasourceAction;
import org.tinymediamanager.ui.movies.actions.MovieUpdateSingleDatasourceAction;

import ca.odell.glazedlists.FilterList;
import ca.odell.glazedlists.SortedList;
import ca.odell.glazedlists.matchers.MatcherEditor;
import ca.odell.glazedlists.swing.DefaultEventTableModel;
import ca.odell.glazedlists.swing.GlazedListsSwing;
import ca.odell.glazedlists.swing.TableComparatorChooser;
import ca.odell.glazedlists.swing.TextComponentMatcherEditor;

import com.jgoodies.forms.factories.FormFactory;
import com.jgoodies.forms.layout.ColumnSpec;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.RowSpec;

/**
 * The Class MoviePanel.
 * 
 * @author Manuel Laggner
 */
public class MoviePanel extends JPanel {

  private static final long             serialVersionUID             = 5783887949951797288L;
  private static final ResourceBundle   BUNDLE                       = ResourceBundle.getBundle("messages", new UTF8Control()); //$NON-NLS-1$
  private final static Logger           LOGGER                       = LoggerFactory.getLogger(MoviePanel.class);

  /** The movie list. */
  private MovieList                     movieList;

  /** The text field. */
  private JTextField                    textField;

  /** The table. */
  private JTable                        table;

  /** The action update data sources. */
  private final Action                  actionUpdateDataSources      = new MovieUpdateDatasourceAction(false);

  /** The action update data sources. */
  private final Action                  actionUpdateDataSources2     = new MovieUpdateDatasourceAction(true);

  /** The action scrape. */
  private final Action                  actionScrape                 = new MovieSingleScrapeAction(false);

  /** The action scrape. */
  private final Action                  actionScrape2                = new MovieSingleScrapeAction(true);

  /** The action edit movie. */
  private final Action                  actionEditMovie              = new MovieEditAction(false);

  /** The action edit movie. */
  private final Action                  actionEditMovie2             = new MovieEditAction(true);

  /** The action scrape unscraped movies. */
  private final Action                  actionScrapeUnscraped        = new MovieUnscrapedScrapeAction();

  /** The action scrape selected movies. */
  private final Action                  actionScrapeSelected         = new MovieSelectedScrapeAction();

  /** The action scrape metadata selected. */
  private final Action                  actionScrapeMetadataSelected = new MovieSelectedScrapeMetadataAction();

  /** The action rename. */
  private final Action                  actionRename                 = new MovieRenameAction(false);

  /** The action rename2. */
  private final Action                  actionRename2                = new MovieRenameAction(true);

  /** The action remove2. */
  private final Action                  actionRemove2                = new MovieRemoveAction();

  /** The action export. */
  private final Action                  actionExport                 = new MovieExportAction();

  private final Action                  actionRewriteNfo             = new MovieRewriteNfoAction();

  /** The panel movie count. */
  private JPanel                        panelMovieCount;

  /** The lbl movie count. */
  private JLabel                        lblMovieCount;

  /** The lbl movie count int. */
  private JLabel                        lblMovieCountTotal;

  /** The btn ren. */
  private JButton                       btnRen;

  /** The menu. */
  private JMenu                         menu;

  /** The movie table model. */
  private DefaultEventTableModel<Movie> movieTableModel;

  /** The movie selection model. */
  MovieSelectionModel                   movieSelectionModel;

  /** The sorted movies. */
  private SortedList<Movie>             sortedMovies;

  /** The text filtered movies. */
  private FilterList<Movie>             textFilteredMovies;

  /** The panel extended search. */
  private JPanel                        panelExtendedSearch;

  /** The lbl movie count of. */
  private JLabel                        lblMovieCountOf;

  /** The lbl movie count filtered. */
  private JLabel                        lblMovieCountFiltered;

  /** The split pane horizontal. */
  private JSplitPane                    splitPaneHorizontal;

  /** The panel right. */
  private MovieInformationPanel         panelRight;

  /** The btn media information. */
  private JButton                       btnMediaInformation;

  /** The action media information. */
  private final Action                  actionMediaInformation       = new MovieMediaInformationAction(false);

  /** The action media information2. */
  private final Action                  actionMediaInformation2      = new MovieMediaInformationAction(true);

  /** The action batch edit. */
  private final Action                  actionBatchEdit              = new MovieBatchEditAction();

  /**
   * Create the panel.
   */
  public MoviePanel() {
    super();
    setOpaque(false);
    // load movielist
    LOGGER.debug("loading MovieList");
    movieList = MovieList.getInstance();
    sortedMovies = new SortedList<Movie>(GlazedListsSwing.swingThreadProxyList(movieList.getMovies()), new MovieComparator());
    sortedMovies.setMode(SortedList.AVOID_MOVING_ELEMENTS);

    // build menu
    menu = new JMenu(BUNDLE.getString("tmm.movies")); //$NON-NLS-1$
    JFrame mainFrame = MainWindow.getFrame();
    JMenuBar menuBar = mainFrame.getJMenuBar();
    menuBar.add(menu);

    setLayout(new FormLayout(
        new ColumnSpec[] { FormFactory.RELATED_GAP_COLSPEC, ColumnSpec.decode("default:grow"), FormFactory.RELATED_GAP_COLSPEC, }, new RowSpec[] {
            FormFactory.RELATED_GAP_ROWSPEC, RowSpec.decode("fill:default:grow"), }));

    splitPaneHorizontal = new JSplitPane();
    splitPaneHorizontal.setOpaque(false);
    splitPaneHorizontal.setContinuousLayout(true);
    add(splitPaneHorizontal, "2, 2, fill, fill");

    JPanel panelMovieList = new JPanel();
    panelMovieList.putClientProperty("class", "roundedPanel");
    splitPaneHorizontal.setLeftComponent(panelMovieList);
    panelMovieList
        .setLayout(new FormLayout(new ColumnSpec[] { FormFactory.RELATED_GAP_COLSPEC, ColumnSpec.decode("200px:grow"),
            ColumnSpec.decode("150px:grow"), }, new RowSpec[] { RowSpec.decode("26px"), FormFactory.RELATED_GAP_ROWSPEC,
            RowSpec.decode("fill:max(200px;default):grow"), FormFactory.RELATED_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC,
            FormFactory.DEFAULT_ROWSPEC, }));

    JToolBar toolBar = new JToolBar();
    toolBar.setRollover(true);
    toolBar.setFloatable(false);
    toolBar.setOpaque(false);
    panelMovieList.add(toolBar, "2, 1, left, fill");

    // udpate datasource
    // toolBar.add(actionUpdateDataSources);
    final JSplitButton buttonUpdateDatasource = new JSplitButton(new ImageIcon(getClass().getResource(
        "/org/tinymediamanager/ui/images/Folder-Sync.png")));
    // temp fix for size of the button
    buttonUpdateDatasource.setText("   ");
    buttonUpdateDatasource.setHorizontalAlignment(JButton.LEFT);
    // buttonScrape.setMargin(new Insets(2, 2, 2, 24));
    buttonUpdateDatasource.setSplitWidth(18);
    buttonUpdateDatasource.addSplitButtonActionListener(new SplitButtonActionListener() {
      public void buttonClicked(ActionEvent e) {
        actionUpdateDataSources.actionPerformed(e);
      }

      public void splitButtonClicked(ActionEvent e) {
        // build the popupmenu on the fly
        buttonUpdateDatasource.getPopupMenu().removeAll();
        JMenuItem item = new JMenuItem(actionUpdateDataSources2);
        buttonUpdateDatasource.getPopupMenu().add(item);
        buttonUpdateDatasource.getPopupMenu().addSeparator();
        for (String ds : Globals.settings.getMovieSettings().getMovieDataSource()) {
          buttonUpdateDatasource.getPopupMenu().add(new JMenuItem(new MovieUpdateSingleDatasourceAction(ds)));
        }

        buttonUpdateDatasource.getPopupMenu().pack();
      }
    });

    JPopupMenu popup = new JPopupMenu("popup");
    buttonUpdateDatasource.setPopupMenu(popup);
    toolBar.add(buttonUpdateDatasource);

    JSplitButton buttonScrape = new JSplitButton(new ImageIcon(getClass().getResource("/org/tinymediamanager/ui/images/Search.png")));
    // temp fix for size of the button
    buttonScrape.setText("   ");
    buttonScrape.setHorizontalAlignment(JButton.LEFT);
    // buttonScrape.setMargin(new Insets(2, 2, 2, 24));
    buttonScrape.setSplitWidth(18);

    // register for listener
    buttonScrape.addSplitButtonActionListener(new SplitButtonActionListener() {
      public void buttonClicked(ActionEvent e) {
        actionScrape.actionPerformed(e);
      }

      public void splitButtonClicked(ActionEvent e) {
      }
    });

    popup = new JPopupMenu("popup");
    JMenuItem item = new JMenuItem(actionScrape2);
    popup.add(item);
    item = new JMenuItem(actionScrapeUnscraped);
    popup.add(item);
    item = new JMenuItem(actionScrapeSelected);
    popup.add(item);
    buttonScrape.setPopupMenu(popup);
    toolBar.add(buttonScrape);

    toolBar.add(actionEditMovie);

    btnRen = new JButton("REN");
    btnRen.setAction(actionRename);
    toolBar.add(btnRen);

    btnMediaInformation = new JButton("MI");
    btnMediaInformation.setAction(actionMediaInformation);
    toolBar.add(btnMediaInformation);

    // textField = new JTextField();
    textField = new JSearchTextField();
    panelMovieList.add(textField, "3, 1, right, bottom");
    textField.setColumns(10);

    // table = new JTable();
    // build JTable

    MatcherEditor<Movie> textMatcherEditor = new TextComponentMatcherEditor<Movie>(textField, new MovieFilterator());
    MovieMatcherEditor movieMatcherEditor = new MovieMatcherEditor();
    FilterList<Movie> extendedFilteredMovies = new FilterList<Movie>(sortedMovies, movieMatcherEditor);
    textFilteredMovies = new FilterList<Movie>(extendedFilteredMovies, textMatcherEditor);
    movieSelectionModel = new MovieSelectionModel(sortedMovies, textFilteredMovies, movieMatcherEditor);
    movieTableModel = new DefaultEventTableModel<Movie>(GlazedListsSwing.swingThreadProxyList(textFilteredMovies), new MovieTableFormat());
    table = new ZebraJTable(movieTableModel);

    movieTableModel.addTableModelListener(new TableModelListener() {
      @Override
      public void tableChanged(TableModelEvent arg0) {
        lblMovieCountFiltered.setText(String.valueOf(movieTableModel.getRowCount()));
        // select first movie if nothing is selected
        ListSelectionModel selectionModel = table.getSelectionModel();
        if (selectionModel.isSelectionEmpty() && movieTableModel.getRowCount() > 0) {
          selectionModel.setSelectionInterval(0, 0);
        }
      }
    });

    // install and save the comparator on the Table
    movieSelectionModel.setTableComparatorChooser(TableComparatorChooser.install(table, sortedMovies, TableComparatorChooser.SINGLE_COLUMN));

    // table = new MyTable();
    table.setFont(new Font("Dialog", Font.PLAIN, 11));
    // scrollPane.setViewportView(table);

    // JScrollPane scrollPane = new JScrollPane(table);
    JScrollPane scrollPane = ZebraJTable.createStripedJScrollPane(table);
    panelMovieList.add(scrollPane, "2, 3, 2, 1, fill, fill");

    panelExtendedSearch = new MovieExtendedSearchPanel(movieSelectionModel);
    panelMovieList.add(panelExtendedSearch, "2, 5, 2, 1, fill, fill");

    JPanel panelStatus = new JPanel();
    panelMovieList.add(panelStatus, "2, 6, 2, 1");
    panelStatus.setLayout(new FormLayout(new ColumnSpec[] { FormFactory.RELATED_GAP_COLSPEC, ColumnSpec.decode("1px"),
        ColumnSpec.decode("146px:grow"), FormFactory.RELATED_GAP_COLSPEC, ColumnSpec.decode("default:grow"), }, new RowSpec[] { RowSpec
        .decode("fill:default:grow"), }));

    panelMovieCount = new JPanel();
    panelStatus.add(panelMovieCount, "3, 1, left, fill");

    lblMovieCount = new JLabel("Movies:");
    panelMovieCount.add(lblMovieCount);

    lblMovieCountFiltered = new JLabel("");
    panelMovieCount.add(lblMovieCountFiltered);

    lblMovieCountOf = new JLabel("of");
    panelMovieCount.add(lblMovieCountOf);

    lblMovieCountTotal = new JLabel("");
    panelMovieCount.add(lblMovieCountTotal);

    panelRight = new MovieInformationPanel(movieSelectionModel);
    splitPaneHorizontal.setRightComponent(panelRight);
    splitPaneHorizontal.setContinuousLayout(true);

    // beansbinding init
    initDataBindings();

    addComponentListener(new ComponentAdapter() {
      @Override
      public void componentHidden(ComponentEvent e) {
        menu.setVisible(false);
        super.componentHidden(e);
      }

      @Override
      public void componentShown(ComponentEvent e) {
        menu.setVisible(true);
        super.componentHidden(e);
      }
    });

    // further initializations
    init();
  }

  private void buildMenu() {
    // menu items
    menu.add(actionUpdateDataSources2);

    JMenu menuScrape = new JMenu(BUNDLE.getString("Button.scrape"));
    menuScrape.add(actionScrape2);
    menuScrape.add(actionScrapeSelected);
    menuScrape.add(actionScrapeUnscraped);
    menuScrape.add(actionScrapeMetadataSelected);
    menu.add(menuScrape);

    JMenu menuEdit = new JMenu(BUNDLE.getString("Button.edit"));
    menuEdit.add(actionEditMovie2);
    menuEdit.add(actionBatchEdit);
    menuEdit.add(actionRename2);

    menu.add(menuEdit);
    menu.add(actionRewriteNfo);
    menu.addSeparator();
    menu.add(actionMediaInformation2);
    menu.add(actionExport);
    menu.add(actionRemove2);

    // popup menu
    JPopupMenu popupMenu = new JPopupMenu();
    popupMenu.add(actionScrape2);
    popupMenu.add(actionScrapeSelected);
    popupMenu.add(actionScrapeMetadataSelected);
    popupMenu.addSeparator();
    popupMenu.add(actionEditMovie2);
    popupMenu.add(actionBatchEdit);
    popupMenu.add(actionRewriteNfo);
    popupMenu.add(actionRename2);
    popupMenu.add(actionMediaInformation2);
    popupMenu.add(actionExport);
    popupMenu.addSeparator();
    popupMenu.add(actionRemove2);

    MouseListener popupListener = new PopupListener(popupMenu);
    table.addMouseListener(popupListener);
  }

  /**
   * further initializations.
   */
  private void init() {
    // build menu
    buildMenu();

    // moviename column
    table.getColumnModel().getColumn(0).setCellRenderer(new BorderCellRenderer());

    // year column
    table.getTableHeader().getColumnModel().getColumn(1).setPreferredWidth(35);
    table.getTableHeader().getColumnModel().getColumn(1).setMinWidth(35);
    table.getTableHeader().getColumnModel().getColumn(1).setMaxWidth(50);

    // NFO column
    table.getTableHeader().getColumnModel().getColumn(2).setHeaderRenderer(new IconHeaderRenderer("NFO"));
    table.getTableHeader().getColumnModel().getColumn(2).setMaxWidth(20);
    URL imageURL = MainWindow.class.getResource("images/File.png");
    if (imageURL != null) {
      table.getColumnModel().getColumn(2).setHeaderValue(new ImageIcon(imageURL));
    }

    // Images column
    table.getTableHeader().getColumnModel().getColumn(3).setHeaderRenderer(new IconHeaderRenderer("Images"));
    table.getTableHeader().getColumnModel().getColumn(3).setMaxWidth(20);
    imageURL = null;
    imageURL = MainWindow.class.getResource("images/Image.png");
    if (imageURL != null) {
      table.getColumnModel().getColumn(3).setHeaderValue(new ImageIcon(imageURL));
    }

    // trailer column
    table.getTableHeader().getColumnModel().getColumn(4).setHeaderRenderer(new IconHeaderRenderer("Trailer"));
    table.getTableHeader().getColumnModel().getColumn(4).setMaxWidth(20);
    imageURL = null;
    imageURL = MainWindow.class.getResource("images/ClapBoard.png");
    if (imageURL != null) {
      table.getColumnModel().getColumn(4).setHeaderValue(new ImageIcon(imageURL));
    }

    // subtitles column
    table.getTableHeader().getColumnModel().getColumn(5).setHeaderRenderer(new IconHeaderRenderer("Subtitles"));
    table.getTableHeader().getColumnModel().getColumn(5).setMaxWidth(20);
    imageURL = null;
    imageURL = MainWindow.class.getResource("images/subtitle.png");
    if (imageURL != null) {
      table.getColumnModel().getColumn(5).setHeaderValue(new ImageIcon(imageURL));
    }

    table.setSelectionModel(movieSelectionModel.getSelectionModel());
    // selecting first movie at startup
    if (movieList.getMovies() != null && movieList.getMovies().size() > 0) {
      ListSelectionModel selectionModel = table.getSelectionModel();
      if (selectionModel.isSelectionEmpty()) {
        selectionModel.setSelectionInterval(0, 0);
      }
    }

    // initialize filteredCount
    lblMovieCountFiltered.setText(String.valueOf(movieTableModel.getRowCount()));
  }

  /**
   * The listener interface for receiving popup events. The class that is interested in processing a popup event implements this interface, and the
   * object created with that class is registered with a component using the component's <code>addPopupListener<code> method. When
   * the popup event occurs, that object's appropriate
   * method is invoked.
   * 
   * @see PopupEvent
   */
  private class PopupListener extends MouseAdapter {

    /** The popup. */
    private JPopupMenu popup;

    /**
     * Instantiates a new popup listener.
     * 
     * @param popupMenu
     *          the popup menu
     */
    PopupListener(JPopupMenu popupMenu) {
      popup = popupMenu;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.awt.event.MouseAdapter#mousePressed(java.awt.event.MouseEvent)
     */
    @Override
    public void mousePressed(MouseEvent e) {
      maybeShowPopup(e);
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.awt.event.MouseAdapter#mouseReleased(java.awt.event.MouseEvent)
     */
    @Override
    public void mouseReleased(MouseEvent e) {
      // if (table.getSelectedRow() != -1) {
      maybeShowPopup(e);
      // }
    }

    /**
     * Maybe show popup.
     * 
     * @param e
     *          the e
     */
    private void maybeShowPopup(MouseEvent e) {
      if (e.isPopupTrigger()) {
        boolean selected = false;
        // check the selected rows
        int row = table.rowAtPoint(e.getPoint());
        int[] selectedRows = table.getSelectedRows();
        for (int selectedRow : selectedRows) {
          if (selectedRow == row) {
            selected = true;
          }
        }

        // if the row, which has been right clicked is not selected - select it
        if (!selected) {
          table.getSelectionModel().setSelectionInterval(row, row);
        }

        popup.show(e.getComponent(), e.getX(), e.getY());
      }
    }
  }

  /**
   * Gets the split pane horizontal.
   * 
   * @return the split pane horizontal
   */
  public JSplitPane getSplitPaneHorizontal() {
    return splitPaneHorizontal;
  }

  /**
   * Inits the data bindings.
   */
  protected void initDataBindings() {
    BeanProperty<JLabel, String> jLabelBeanProperty = BeanProperty.create("text");
    //
    BeanProperty<MovieList, Integer> movieListBeanProperty = BeanProperty.create("movieCount");
    AutoBinding<MovieList, Integer, JLabel, String> autoBinding_20 = Bindings.createAutoBinding(UpdateStrategy.READ, movieList,
        movieListBeanProperty, lblMovieCountTotal, jLabelBeanProperty);
    autoBinding_20.bind();
    //
  }
}
