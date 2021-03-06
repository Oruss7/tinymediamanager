/*
 * Copyright 2012 - 2015 Manuel Laggner
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

package org.tinymediamanager.scraper.imdb;

import java.nio.charset.Charset;

import org.apache.commons.lang3.StringUtils;

/**
 * The Enum ImdbSiteDefinition.
 * 
 * @author Manuel Laggner
 */
public enum ImdbSiteDefinition {
  // www.imdb.com - international site
  IMDB_COM("http://www.imdb.com/", "UTF-8", "Tagline", "Genre", "Runtime", "Production Companies", "Writing credits", "Certification",
      "Release Date", "Produced by");

  private ImdbSiteDefinition(String site, String charsetName, String tagline, String genre, String runtime, String productionCompanies,
      String writers, String certification, String releaseDate, String producers) {
    this.site = site;
    if (StringUtils.isBlank(charsetName)) {
      this.charset = Charset.defaultCharset();
    }
    else {
      this.charset = Charset.forName(charsetName);
    }
    this.tagline = tagline;
    this.genre = genre;
    this.runtime = runtime;
    this.productionCompanies = productionCompanies;
    this.writer = writers;
    this.certification = certification;
    this.releaseDate = releaseDate;
    this.producers = producers;
  }

  private String  site;
  private Charset charset;
  private String  tagline;
  private String  genre;
  private String  runtime;
  private String  productionCompanies;
  private String  writer;
  private String  certification;
  private String  releaseDate;
  private String  producers;

  public String getSite() {
    return site;
  }

  public Charset getCharset() {
    return charset;
  }

  public String getTagline() {
    return tagline;
  }

  public String getGenre() {
    return genre;
  }

  public String getRuntime() {
    return runtime;
  }

  public String getProductionCompanies() {
    return productionCompanies;
  }

  public String getWriter() {
    return writer;
  }

  public String getCertification() {
    return certification;
  }

  public String getReleaseDate() {
    return releaseDate;
  }

  public String getProducers() {
    return producers;
  }

  /*
   * (non-Javadoc)
   * 
   * @see java.lang.Enum#toString()
   */
  public String toString() {
    return site;
  }
}

// IMDB_SITES.put("us", new ImdbSiteDefinition("http://www.imdb.com/",
// "ISO-8859-1", "Director|Directed by", "Cast", "Release Date", "Runtime",
// "Country", "Company", "Genre",
// "Quotes", "Plot", "Rated", "Certification", "Original Air Date",
// "Writer|Writing credits", "Taglines"));
//
// IMDB_SITES.put("fr", new ImdbSiteDefinition("http://www.imdb.fr/",
// "ISO-8859-1", "R&#xE9;alisateur|R&#xE9;alis&#xE9; par", "Ensemble",
// "Date de sortie", "Dur&#xE9;e", "Pays",
// "Soci&#xE9;t&#xE9;", "Genre", "Citation", "Intrigue", "Rated",
// "Classification", "Date de sortie", "Sc&#xE9;naristes|Sc&#xE9;naristes",
// "Taglines"));
//
// IMDB_SITES.put("es", new ImdbSiteDefinition("http://www.imdb.es/",
// "ISO-8859-1", "Director|Dirigida por", "Reparto", "Fecha de Estreno",
// "Duraci&#xF3;n", "Pa&#xED;s",
// "Compa&#xF1;&#xED;a", "G&#xE9;nero", "Quotes", "Trama", "Rated",
// "Clasificaci&#xF3;n", "Fecha de Estreno",
// "Escritores|Cr&#xE9;ditos del gui&#xF3;n", "Taglines"));
//
// IMDB_SITES.put("de", new ImdbSiteDefinition("http://www.imdb.de/",
// "ISO-8859-1", "Regisseur|Regie", "Besetzung", "Premierendatum",
// "L&#xE4;nge", "Land", "Firma", "Genre",
// "Quotes", "Handlung", "Rated", "Altersfreigabe", "Premierendatum",
// "Guionista|Buch", "Taglines"));
//
// IMDB_SITES.put("it", new ImdbSiteDefinition("http://www.imdb.it/",
// "ISO-8859-1", "Regista|Registi|Regia di", "Cast", "Data di uscita",
// "Durata", "Nazionalit&#xE0;",
// "Compagnia", "Genere", "Quotes", "Trama", "Rated", "Certification",
// "Data di uscita", "Sceneggiatore|Scritto da", "Taglines"));
//
// IMDB_SITES.put("pt", new ImdbSiteDefinition("http://www.imdb.pt/",
// "ISO-8859-1", "Diretor|Dirigido por", "Elenco", "Data de Lan&#xE7;amento",
// "Dura&#xE7;&#xE3;o", "Pa&#xED;s",
// "Companhia", "G&#xEA;nero", "Quotes", "Argumento", "Rated",
// "Certifica&#xE7;&#xE3;o", "Data de Lan&#xE7;amento",
// "Roteirista|Cr&#xE9;ditos como roteirista", "Taglines"));
//
// // Use this as a workaround for English speakers abroad who get localised
// // versions of imdb.com
// IMDB_SITES.put("labs", new ImdbSiteDefinition("http://akas.imdb.com/",
// "ISO-8859-1", "Director|Directors|Directed by", "Cast", "Release Date",
// "Runtime", "Country",
// "Production Co", "Genres", "Quotes", "Storyline", "Rated", "Certification",
// "Original Air Date", "Writer|Writers|Writing credits", "Taglines"));
//
//
// // Not 100% sure these are correct
// IMDB_SITES.put("it2", new ImdbSiteDefinition("http://www.imdb.it/",
// "ISO-8859-1", "Regista|Registi|Regia di", "Attori", "Data di uscita",
// "Durata", "Nazionalit&#xE0;",
// "Compagnia", "Genere", "Quotes", "Trama", "Rated", "Certification",
// "Data di uscita", "Sceneggiatore|Scritto da", "Taglines"));