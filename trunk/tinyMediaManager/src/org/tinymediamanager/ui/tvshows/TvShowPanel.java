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
package org.tinymediamanager.ui.tvshows;

import java.awt.CardLayout;
import java.awt.Graphics;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JToolBar;
import javax.swing.JTree;
import javax.swing.ScrollPaneConstants;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;

import org.apache.commons.lang3.StringUtils;
import org.gpl.JSplitButton.JSplitButton;
import org.gpl.JSplitButton.action.SplitButtonActionListener;
import org.jdesktop.beansbinding.AutoBinding;
import org.jdesktop.beansbinding.AutoBinding.UpdateStrategy;
import org.jdesktop.beansbinding.BeanProperty;
import org.jdesktop.beansbinding.Bindings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tinymediamanager.core.tvshow.TvShow;
import org.tinymediamanager.core.tvshow.TvShowEpisode;
import org.tinymediamanager.core.tvshow.TvShowList;
import org.tinymediamanager.core.tvshow.TvShowSeason;
import org.tinymediamanager.core.tvshow.tasks.TvShowUpdateDatasourceTask;
import org.tinymediamanager.ui.MainWindow;
import org.tinymediamanager.ui.TmmSwingWorker;
import org.tinymediamanager.ui.TreeUI;
import org.tinymediamanager.ui.UTF8Control;
import org.tinymediamanager.ui.components.ImageLabel;
import org.tinymediamanager.ui.components.ZebraJTree;
import org.tinymediamanager.ui.dialogs.ImageChooserDialog;
import org.tinymediamanager.ui.dialogs.ImageChooserDialog.ImageType;
import org.tinymediamanager.ui.tvshows.dialogs.TvShowChooserDialog;
import org.tinymediamanager.ui.tvshows.dialogs.TvShowEditorDialog;
import org.tinymediamanager.ui.tvshows.dialogs.TvShowEpisodeEditorDialog;

import com.jgoodies.forms.factories.FormFactory;
import com.jgoodies.forms.layout.ColumnSpec;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.RowSpec;
import com.jtattoo.plaf.AbstractLookAndFeel;
import com.jtattoo.plaf.JTattooUtilities;

/**
 * The Class TvShowPanel.
 * 
 * @author Manuel Laggner
 */
public class TvShowPanel extends JPanel {

  /** The Constant BUNDLE. */
  private static final ResourceBundle BUNDLE                    = ResourceBundle.getBundle("messages", new UTF8Control()); //$NON-NLS-1$

  /** The Constant serialVersionUID. */
  private static final long           serialVersionUID          = -1923811385292825136L;

  /** The logger. */
  private final static Logger         LOGGER                    = LoggerFactory.getLogger(TvShowPanel.class);

  /** The tree model. */
  private TvShowTreeModel             treeModel;

  /** The tv show selection model. */
  private TvShowSelectionModel        tvShowSelectionModel;

  /** The tv show episode selection model. */
  private TvShowEpisodeSelectionModel tvShowEpisodeSelectionModel;

  /** The tv show list. */
  private TvShowList                  tvShowList                = TvShowList.getInstance();

  /** The tree. */
  private JTree                       tree;
  // private TreeTable tree;

  /** The panel right. */
  private JPanel                      panelRight;

  /** The action update datasources. */
  private final Action                actionUpdateDatasources   = new UpdateDatasourcesAction(false);

  /** The action update datasources2. */
  private final Action                actionUpdateDatasources2  = new UpdateDatasourcesAction(true);

  /** The action scrape. */
  private final Action                actionScrape              = new SingleScrapeAction(false);

  /** The action scrape2. */
  private final Action                actionScrape2             = new SingleScrapeAction(true);

  /** The action edit. */
  private final Action                actionEdit                = new EditAction(false);

  /** The action edit2. */
  private final Action                actionEdit2               = new EditAction(true);

  /** The action change season poster. */
  private final Action                actionChangeSeasonPoster2 = new ChangeSeasonPosterAction(true);

  /** The width. */
  private int                         width                     = 0;

  /** The menu. */
  private JMenu                       menu;

  /** The lbl tv shows. */
  private JLabel                      lblTvShows;

  /** The lbl episodes. */
  private JLabel                      lblEpisodes;

  /**
   * Instantiates a new tv show panel.
   */
  public TvShowPanel() {
    super();

    treeModel = new TvShowTreeModel(tvShowList.getTvShows());
    tvShowSelectionModel = new TvShowSelectionModel();
    tvShowEpisodeSelectionModel = new TvShowEpisodeSelectionModel();

    // build menu
    menu = new JMenu(BUNDLE.getString("tmm.tvshows")); //$NON-NLS-1$
    JFrame mainFrame = MainWindow.getFrame();
    JMenuBar menuBar = mainFrame.getJMenuBar();
    menuBar.add(menu);

    setLayout(new FormLayout(
        new ColumnSpec[] { FormFactory.RELATED_GAP_COLSPEC, ColumnSpec.decode("default:grow"), FormFactory.RELATED_GAP_COLSPEC, }, new RowSpec[] {
            FormFactory.RELATED_GAP_ROWSPEC, RowSpec.decode("default:grow"), }));

    JSplitPane splitPane = new JSplitPane();
    splitPane.setContinuousLayout(true);
    add(splitPane, "2, 2, fill, fill");

    JPanel panelTvShowTree = new JPanel();
    splitPane.setLeftComponent(panelTvShowTree);
    panelTvShowTree.setLayout(new FormLayout(new ColumnSpec[] { FormFactory.RELATED_GAP_COLSPEC, ColumnSpec.decode("300px:grow"), }, new RowSpec[] {
        FormFactory.LINE_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC, FormFactory.RELATED_GAP_ROWSPEC, RowSpec.decode("3px:grow"),
        FormFactory.RELATED_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC, }));

    // tree = new TreeTable(treeModel);
    // JScrollPane scrollPane = ZebraJTable.createStripedJScrollPane(tree);
    JScrollPane scrollPane = new JScrollPane();
    scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
    panelTvShowTree.add(scrollPane, "2, 4, fill, fill");

    JToolBar toolBar = new JToolBar();
    toolBar.setRollover(true);
    toolBar.setFloatable(false);
    toolBar.setOpaque(false);
    panelTvShowTree.add(toolBar, "2, 2");

    toolBar.add(actionUpdateDatasources);
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

    // TODO create dropdown for split button
    // JPopupMenu popup = new JPopupMenu("popup");
    // JMenuItem item = new JMenuItem(actionScrape2);
    // popup.add(item);
    // item = new JMenuItem(actionScrapeUnscraped);
    // popup.add(item);
    // item = new JMenuItem(actionScrapeSelected);
    // popup.add(item);
    // buttonScrape.setPopupMenu(popup);
    toolBar.add(buttonScrape);
    toolBar.add(actionEdit);

    // TODO move to a correcter place
    tree = new ZebraJTree(treeModel) {
      private static final long serialVersionUID = 1L;

      public void paintComponent(Graphics g) {
        width = this.getWidth();
        super.paintComponent(g);
      }
    };

    // // TODO move to a correcter place
    TreeUI ui = new TreeUI() {
      protected void paintRow(Graphics g, Rectangle clipBounds, Insets insets, Rectangle bounds, TreePath path, int row, boolean isExpanded,
          boolean hasBeenExpanded, boolean isLeaf) {
        bounds.width = width - bounds.x;
        super.paintRow(g, clipBounds, insets, bounds, path, row, isExpanded, hasBeenExpanded, isLeaf);
      }

    };
    tree.setUI(ui);

    tree.setRootVisible(false);
    tree.setShowsRootHandles(true);
    // tree.getTree().setLargeModel(true);
    tree.setCellRenderer(new TvShowTreeCellRenderer());
    scrollPane.setViewportView(tree);

    JPanel panelHeader = new JPanel() {
      @Override
      public void paintComponent(Graphics g) {
        super.paintComponent(g);
        JTattooUtilities.fillHorGradient(g, AbstractLookAndFeel.getTheme().getColHeaderColors(), 0, 0, getWidth(), getHeight());
      }
    };
    scrollPane.setColumnHeaderView(panelHeader);
    panelHeader.setLayout(new FormLayout(new ColumnSpec[] { FormFactory.RELATED_GAP_COLSPEC, ColumnSpec.decode("default:grow"),
        FormFactory.RELATED_GAP_COLSPEC, ColumnSpec.decode("center:20px"), ColumnSpec.decode("center:20px"), },
        new RowSpec[] { FormFactory.DEFAULT_ROWSPEC, }));

    JLabel lblTvShowsColumn = new JLabel("Tv show");
    lblTvShowsColumn.setHorizontalAlignment(JLabel.CENTER);
    panelHeader.add(lblTvShowsColumn, "2, 1");

    JLabel lblNfoColumn = new JLabel("");
    lblNfoColumn.setHorizontalAlignment(JLabel.CENTER);
    lblNfoColumn.setIcon(new ImageIcon(TvShowPanel.class.getResource("/org/tinymediamanager/ui/images/Info.png")));
    panelHeader.add(lblNfoColumn, "4, 1");

    JLabel lblImageColumn = new JLabel("");
    lblImageColumn.setHorizontalAlignment(JLabel.CENTER);
    lblImageColumn.setIcon(new ImageIcon(TvShowPanel.class.getResource("/org/tinymediamanager/ui/images/Image.png")));
    panelHeader.add(lblImageColumn, "5, 1");

    JPanel panel = new JPanel();
    panelTvShowTree.add(panel, "2, 6, fill, fill");
    panel
        .setLayout(new FormLayout(new ColumnSpec[] { FormFactory.DEFAULT_COLSPEC, FormFactory.RELATED_GAP_COLSPEC, FormFactory.DEFAULT_COLSPEC,
            FormFactory.RELATED_GAP_COLSPEC, FormFactory.DEFAULT_COLSPEC, FormFactory.RELATED_GAP_COLSPEC, FormFactory.DEFAULT_COLSPEC,
            FormFactory.RELATED_GAP_COLSPEC, FormFactory.DEFAULT_COLSPEC, }, new RowSpec[] { FormFactory.LINE_GAP_ROWSPEC,
            FormFactory.DEFAULT_ROWSPEC, }));

    JLabel lblTvShowsT = new JLabel("TV shows:");
    panel.add(lblTvShowsT, "1, 2, fill, fill");

    lblTvShows = new JLabel("");
    panel.add(lblTvShows, "3, 2");

    JLabel labelSlash = new JLabel("/");
    panel.add(labelSlash, "5, 2");

    JLabel lblEpisodesT = new JLabel("Episodes:");
    panel.add(lblEpisodesT, "7, 2");

    lblEpisodes = new JLabel("");
    panel.add(lblEpisodes, "9, 2");

    panelRight = new JPanel();
    splitPane.setRightComponent(panelRight);
    panelRight.setLayout(new CardLayout(0, 0));

    JPanel panelTvShow = new TvShowInformationPanel(tvShowSelectionModel);
    panelRight.add(panelTvShow, "tvShow");

    JPanel panelTvShowEpisode = new TvShowEpisodeInformationPanel(tvShowEpisodeSelectionModel);
    panelRight.add(panelTvShowEpisode, "tvShowEpisode");

    tree.addTreeSelectionListener(new TreeSelectionListener() {
      @Override
      public void valueChanged(TreeSelectionEvent e) {
        DefaultMutableTreeNode node = (DefaultMutableTreeNode) tree.getLastSelectedPathComponent();
        if (node != null) {
          // click on a tv show
          if (node.getUserObject() instanceof TvShow) {
            TvShow tvShow = (TvShow) node.getUserObject();
            tvShowSelectionModel.setSelectedTvShow(tvShow);
            CardLayout cl = (CardLayout) (panelRight.getLayout());
            cl.show(panelRight, "tvShow");
          }

          // click on a season
          if (node.getUserObject() instanceof TvShowSeason) {
            TvShowSeason tvShowSeason = (TvShowSeason) node.getUserObject();
            // act as a click on a tv show if a season of an other tv show has been clicked
            if (tvShowSeason.getTvShow() != tvShowSelectionModel.getSelectedTvShow()) {
              tvShowSelectionModel.setSelectedTvShow(tvShowSeason.getTvShow());
              CardLayout cl = (CardLayout) (panelRight.getLayout());
              cl.show(panelRight, "tvShow");
            }
          }

          // click on an episode
          if (node.getUserObject() instanceof TvShowEpisode) {
            TvShowEpisode tvShowEpisode = (TvShowEpisode) node.getUserObject();
            tvShowEpisodeSelectionModel.setSelectedTvShowEpisode(tvShowEpisode);
            CardLayout cl = (CardLayout) (panelRight.getLayout());
            cl.show(panelRight, "tvShowEpisode");
          }
        }
        else {
          tvShowSelectionModel.setSelectedTvShow(null);
        }
      }
    });

    addComponentListener(new ComponentAdapter() {
      @Override
      public void componentHidden(ComponentEvent e) {
        menu.setVisible(false);
        super.componentHidden(e);
      }

      /*
       * (non-Javadoc)
       * 
       * @see java.awt.event.ComponentAdapter#componentShown(java.awt.event.ComponentEvent)
       */
      @Override
      public void componentShown(ComponentEvent e) {
        menu.setVisible(true);
        super.componentHidden(e);
      }
    });

    // further initializations
    init();
    initDataBindings();
  }

  /**
   * Inits the.
   */
  private void init() {
    // build menu
    buildMenu();

  }

  /**
   * Builds the menu.
   */
  private void buildMenu() {
    // menu items
    menu.add(actionUpdateDatasources2);
    menu.addSeparator();

    JMenu menuScrape = new JMenu(BUNDLE.getString("Button.scrape"));
    menuScrape.add(actionScrape2);
    menu.add(menuScrape);

    JMenu menuEdit = new JMenu(BUNDLE.getString("Button.edit"));
    menuEdit.add(actionEdit2);
    menuEdit.add(actionChangeSeasonPoster2);
    menu.add(menuEdit);

    // menu.add(actionScrapeSelected);
    // menu.add(actionScrapeUnscraped);
    // menu.add(actionScrapeMetadataSelected);
    // menu.addSeparator();
    // menu.add(actionEditMovie2);
    // menu.add(actionBatchEdit);
    // menu.add(actionRename2);
    // menu.add(actionMediaInformation2);
    // menu.add(actionExport);
    // menu.addSeparator();
    // menu.add(actionRemove2);

    // popup menu
    JPopupMenu popupMenu = new JPopupMenu();
    popupMenu.add(actionScrape2);
    // popupMenu.add(actionScrapeSelected);
    // popupMenu.add(actionScrapeMetadataSelected);
    popupMenu.addSeparator();
    popupMenu.add(actionEdit2);
    popupMenu.add(actionChangeSeasonPoster2);
    // popupMenu.add(actionBatchEdit);
    // popupMenu.add(actionRename2);
    // popupMenu.add(actionMediaInformation2);
    // popupMenu.add(actionExport);
    // popupMenu.addSeparator();
    // popupMenu.add(actionRemove2);

    MouseListener popupListener = new PopupListener(popupMenu);
    tree.addMouseListener(popupListener);
  }

  /**
   * Gets the selected tv shows.
   * 
   * @return the selected tv shows
   */
  private List<TvShow> getSelectedTvShows() {
    List<TvShow> selectedTvShows = new ArrayList<TvShow>();

    TreePath[] paths = tree.getSelectionPaths();

    // filter out all tv shows from the selection
    if (paths != null) {
      for (TreePath path : paths) {
        if (path.getPathCount() > 1) {
          DefaultMutableTreeNode node = (DefaultMutableTreeNode) path.getLastPathComponent();
          if (node.getUserObject() instanceof TvShow) {
            TvShow tvShow = (TvShow) node.getUserObject();
            selectedTvShows.add(tvShow);
          }
        }
      }
    }

    return selectedTvShows;
  }

  /**
   * Gets the selected objects.
   * 
   * @return the selected objects
   */
  private List<Object> getSelectedObjects() {
    List<Object> selectedObjects = new ArrayList<Object>();

    TreePath[] paths = tree.getSelectionPaths();

    // filter out all objects from the selection
    if (paths != null) {
      for (TreePath path : paths) {
        if (path.getPathCount() > 1) {
          DefaultMutableTreeNode node = (DefaultMutableTreeNode) path.getLastPathComponent();
          selectedObjects.add(node.getUserObject());
        }
      }
    }

    return selectedObjects;
  }

  /**
   * The Class UpdateDatasourcesAction.
   * 
   * @author Manuel Laggner
   */
  private class UpdateDatasourcesAction extends AbstractAction {

    /** The Constant serialVersionUID. */
    private static final long serialVersionUID = 5704371143505653741L;

    /**
     * Instantiates a new update datasources action.
     * 
     * @param withTitle
     *          the with title
     */
    public UpdateDatasourcesAction(boolean withTitle) {
      if (withTitle) {
        putValue(NAME, BUNDLE.getString("update.datasource")); //$NON-NLS-1$
        putValue(LARGE_ICON_KEY, "");
      }
      else {
        putValue(LARGE_ICON_KEY, new ImageIcon(getClass().getResource("/org/tinymediamanager/ui/images/Folder-Sync.png")));
      }
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
     */
    public void actionPerformed(ActionEvent e) {
      TmmSwingWorker task = new TvShowUpdateDatasourceTask();
      if (!MainWindow.executeMainTask(task)) {
        JOptionPane.showMessageDialog(null, BUNDLE.getString("onlyoneoperation")); //$NON-NLS-1$
      }
    }
  }

  /**
   * The Class SingleScrapeAction.
   * 
   * @author Manuel Laggner
   */
  private class SingleScrapeAction extends AbstractAction {

    /** The Constant serialVersionUID. */
    private static final long serialVersionUID = 641704453374845709L;

    /**
     * Instantiates a new SingleScrapeAction.
     * 
     * @param withTitle
     *          the with title
     */
    public SingleScrapeAction(boolean withTitle) {
      if (withTitle) {
        putValue(NAME, BUNDLE.getString("tvshow.scrape.selected")); //$NON-NLS-1$
        putValue(LARGE_ICON_KEY, "");
      }
      else {
        putValue(LARGE_ICON_KEY, new ImageIcon(getClass().getResource("/org/tinymediamanager/ui/images/Search.png")));
        putValue(SHORT_DESCRIPTION, BUNDLE.getString("tvshow.scrape.selected")); //$NON-NLS-1$
      }
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
     */
    public void actionPerformed(ActionEvent e) {
      List<TvShow> selectedTvShows = getSelectedTvShows();

      for (TvShow tvShow : selectedTvShows) {
        // display tv show chooser
        TvShowChooserDialog chooser = new TvShowChooserDialog(tvShow, selectedTvShows.size() > 1 ? true : false);
        if (!chooser.showDialog()) {
          break;
        }
      }
    }
  }

  /**
   * The Class EditAction.
   * 
   * @author Manuel Laggner
   */
  private class EditAction extends AbstractAction {

    /** The Constant serialVersionUID. */
    private static final long serialVersionUID = -3911290901017607679L;

    /**
     * Instantiates a new edits the action.
     * 
     * @param withTitle
     *          the with title
     */
    public EditAction(boolean withTitle) {
      if (withTitle) {
        putValue(NAME, BUNDLE.getString("tvshow.edit")); //$NON-NLS-1$
        putValue(LARGE_ICON_KEY, "");
      }
      else {
        putValue(LARGE_ICON_KEY, new ImageIcon(getClass().getResource("/org/tinymediamanager/ui/images/Pencil.png")));
        putValue(SHORT_DESCRIPTION, BUNDLE.getString("tvshow.edit")); //$NON-NLS-1$
      }
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
     */
    public void actionPerformed(ActionEvent e) {
      List<Object> selectedObjects = getSelectedObjects();

      for (Object obj : selectedObjects) {
        // display tv show editor
        if (obj instanceof TvShow) {
          TvShow tvShow = (TvShow) obj;
          TvShowEditorDialog editor = new TvShowEditorDialog(tvShow, selectedObjects.size() > 1 ? true : false);
          if (!editor.showDialog()) {
            break;
          }
        }
        // display tv episode editor
        if (obj instanceof TvShowEpisode) {
          TvShowEpisode tvShowEpisode = (TvShowEpisode) obj;
          TvShowEpisodeEditorDialog editor = new TvShowEpisodeEditorDialog(tvShowEpisode, selectedObjects.size() > 1 ? true : false);
          if (!editor.showDialog()) {
            break;
          }
        }
      }
    }
  }

  /**
   * The Class ChangeSeasonPosterAction.
   * 
   * @author Manuel Laggner
   */
  private class ChangeSeasonPosterAction extends AbstractAction {

    /** The Constant serialVersionUID. */
    private static final long serialVersionUID = 8356413227405772558L;

    /**
     * Instantiates a new change season poster action.
     * 
     * @param withTitle
     *          the with title
     */
    public ChangeSeasonPosterAction(boolean withTitle) {
      if (withTitle) {
        putValue(NAME, BUNDLE.getString("tvshow.changeseasonposter")); //$NON-NLS-1$
        putValue(LARGE_ICON_KEY, "");
      }
      else {
        putValue(LARGE_ICON_KEY, new ImageIcon(getClass().getResource("/org/tinymediamanager/ui/images/Pencil.png")));
        putValue(SHORT_DESCRIPTION, BUNDLE.getString("tvshow.changeseasonposter")); //$NON-NLS-1$
      }
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
     */
    public void actionPerformed(ActionEvent e) {
      List<Object> selectedObjects = getSelectedObjects();

      for (Object obj : selectedObjects) {
        // display image chooser
        if (obj instanceof TvShowSeason) {
          TvShowSeason season = (TvShowSeason) obj;
          ImageLabel imageLabel = new ImageLabel();
          ImageChooserDialog dialog = new ImageChooserDialog(season.getTvShow().getIds(), ImageType.SEASON, tvShowList.getArtworkProviders(),
              imageLabel, null, null);
          dialog.setLocationRelativeTo(MainWindow.getActiveInstance());
          dialog.setVisible(true);

          if (StringUtils.isNotBlank(imageLabel.getImageUrl())) {
            season.setPosterUrl(imageLabel.getImageUrl());
            season.getTvShow().writeSeasonPoster(season.getSeason());
          }
        }
      }
    }
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
        int row = tree.getClosestRowForLocation(e.getPoint().x, e.getPoint().y);

        TreePath[] paths = tree.getSelectionPaths();

        // filter out all objects from the selection
        if (paths != null) {
          for (TreePath path : paths) {
            if (path.getPathCount() > 1) {
              if (tree.getRowForPath(path) == row) {
                selected = true;
              }
            }
          }
        }

        // if the row, which has been right clicked is not selected - select it
        if (!selected) {
          tree.getSelectionModel().setSelectionPath(tree.getPathForRow(row));
        }

        popup.show(e.getComponent(), e.getX(), e.getY());
      }
    }
  }

  protected void initDataBindings() {
    BeanProperty<TvShowList, Integer> tvShowListBeanProperty = BeanProperty.create("tvShowCount");
    BeanProperty<JLabel, String> jLabelBeanProperty = BeanProperty.create("text");
    AutoBinding<TvShowList, Integer, JLabel, String> autoBinding = Bindings.createAutoBinding(UpdateStrategy.READ, tvShowList,
        tvShowListBeanProperty, lblTvShows, jLabelBeanProperty);
    autoBinding.bind();
    //
    BeanProperty<TvShowList, Integer> tvShowListBeanProperty_1 = BeanProperty.create("episodeCount");
    AutoBinding<TvShowList, Integer, JLabel, String> autoBinding_1 = Bindings.createAutoBinding(UpdateStrategy.READ, tvShowList,
        tvShowListBeanProperty_1, lblEpisodes, jLabelBeanProperty);
    autoBinding_1.bind();
  }
}
