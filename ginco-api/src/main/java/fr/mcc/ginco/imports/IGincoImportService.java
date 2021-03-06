/**
 * Copyright or © or Copr. Ministère Français chargé de la Culture
 * et de la Communication (2013)
 * <p/>
 * contact.gincoculture_at_gouv.fr
 * <p/>
 * This software is a computer program whose purpose is to provide a thesaurus
 * management solution.
 * <p/>
 * This software is governed by the CeCILL license under French law and
 * abiding by the rules of distribution of free software. You can use,
 * modify and/ or redistribute the software under the terms of the CeCILL
 * license as circulated by CEA, CNRS and INRIA at the following URL
 * "http://www.cecill.info".
 * <p/>
 * As a counterpart to the access to the source code and rights to copy,
 * modify and redistribute granted by the license, users are provided only
 * with a limited warranty and the software's author, the holder of the
 * economic rights, and the successive licensors have only limited liability.
 * <p/>
 * In this respect, the user's attention is drawn to the risks associated
 * with loading, using, modifying and/or developing or reproducing the
 * software by the user in light of its specific status of free software,
 * that may mean that it is complicated to manipulate, and that also
 * therefore means that it is reserved for developers and experienced
 * professionals having in-depth computer knowledge. Users are therefore
 * encouraged to load and test the software's suitability as regards their
 * requirements in conditions enabling the security of their systemsand/or
 * data to be ensured and, more generally, to use and operate it in the
 * same conditions as regards security.
 * <p/>
 * The fact that you are presently reading this means that you have had
 * knowledge of the CeCILL license and that you accept its terms.
 */
package fr.mcc.ginco.imports;

import fr.mcc.ginco.beans.Alignment;
import fr.mcc.ginco.beans.Thesaurus;
import fr.mcc.ginco.beans.ThesaurusConcept;
import fr.mcc.ginco.exceptions.TechnicalException;

import java.io.File;
import java.util.Map;
import java.util.Set;


/**
 * Service provides function to use for Ginco import
 * by REST services.
 */
public interface IGincoImportService {

	/**
	 * This method import a Thesaurus from a Ginco custom format
	 *
	 * @param content
	 * @param fileName
	 * @param tempDir
	 * @return a map containing the imported Thesaurus and the banned alignments
	 * @throws TechnicalException
	 */
	Map<Thesaurus, Set<Alignment>> importGincoXmlThesaurusFile(String content, String fileName,
	                                                           File tempDir);

	/**
	 * This method import a concept branch from a Ginco custom format
	 *
	 * @param content
	 * @param fileName
	 * @param tempDir
	 * @param thesaurusId
	 * @return
	 */
	Map<ThesaurusConcept, Set<Alignment>> importGincoBranchXmlFile(String content, String fileName,
	                                                               File tempDir, String thesaurusId);

}
