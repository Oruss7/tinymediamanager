/*
 * Copyright 2012-2013 Manuel Laggner
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
package org.tinymediamanager.core.tvshow;

import javax.persistence.Entity;
import javax.persistence.Inheritance;

import org.tinymediamanager.core.MediaEntity;

/**
 * The Class TvShow.
 */
@Entity
@Inheritance(strategy = javax.persistence.InheritanceType.JOINED)
public class TvShow extends MediaEntity {

  @Override
  public String getNameForUi() {
    return name;
  }

}
