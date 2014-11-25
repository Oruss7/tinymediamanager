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
package org.tinymediamanager.ui.tvshows;

import javax.swing.Action;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;

import org.tinymediamanager.ui.ITmmUIModule;
import org.tinymediamanager.ui.MainWindow;
import org.tinymediamanager.ui.tvshows.settings.TvShowSettingsContainerPanel;

public class TvShowUIModule implements ITmmUIModule {
  private final static String        ID       = "tvShows";
  private static TvShowUIModule      instance = null;

  private final TvShowSelectionModel selectionModel;
  private final JPanel               settingsPanel;

  private TvShowUIModule() {
    // this will be used in v3
    // listPanel = new MoviePanel();
    // selectionModel = listPanel.movieSelectionModel;
    // detailPanel = new MovieInformationPanel(selectionModel);

    // createActions();
    // createPopupMenu();

    selectionModel = MainWindow.getActiveInstance().getTvShowPanel().tvShowSelectionModel;
    settingsPanel = new TvShowSettingsContainerPanel();
  }

  public static TvShowUIModule getInstance() {
    if (instance == null) {
      instance = new TvShowUIModule();
    }
    return instance;
  }

  @Override
  public String getModuleId() {
    return ID;
  }

  @Override
  public JPanel getTabPanel() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public String getTabTitle() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public JPanel getDetailPanel() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public Action getSearchAction() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public JPopupMenu getSearchMenu() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public Action getEditAction() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public JPopupMenu getEditMenu() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public Action getUpdateAction() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public JPopupMenu getUpdateMenu() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public Action getExportAction() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public JPopupMenu getExportMenu() {
    // TODO Auto-generated method stub
    return null;
  }

  public TvShowSelectionModel getSelectionModel() {
    return selectionModel;
  }

  @Override
  public JPanel getSettingsPanel() {
    return settingsPanel;
  }
}
