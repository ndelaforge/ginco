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
package fr.mcc.ginco.tests.rest;

import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import junit.framework.Assert;

import org.apache.commons.lang3.StringEscapeUtils;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.slf4j.Logger;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import fr.mcc.ginco.beans.Thesaurus;
import fr.mcc.ginco.exceptions.BusinessException;
import fr.mcc.ginco.extjs.view.node.IThesaurusListNode;
import fr.mcc.ginco.extjs.view.utils.FolderGenerator;
import fr.mcc.ginco.log.Log;
import fr.mcc.ginco.rest.services.exceptions.BusinessExceptionMapper;
import fr.mcc.ginco.services.IThesaurusService;
import fr.mcc.ginco.tests.LoggerTestUtil;
import fr.mcc.ginco.utils.EncodedControl;

public class BusinessExceptionMapperTest {

	private BusinessExceptionMapper businessExceptionMapper = new BusinessExceptionMapper();

	@Before
	public void setUp() {
		MockitoAnnotations.initMocks(this);
		LoggerTestUtil.initLogger(businessExceptionMapper);
	}

	/**
	 * Test the getVocabularies method with empty values for non mandatory
	 * fields in Thesauruses objects
	 */
	@Test
	public final void testToResponse() {
		BusinessException be = new BusinessException("logMessage",
				"import-unable-to-write-temporary-file");
		Response response = businessExceptionMapper.toResponse(be);
		Assert.assertEquals(200, response.getStatus());
		String responseEntity = (String) response.getEntity();
		Assert.assertEquals(
				"{success:false, message: 'Impossible de stocker temporairement le fichier'}",
				responseEntity);

	}

}