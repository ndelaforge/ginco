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
package fr.mcc.ginco.services;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;

import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.apache.solr.common.SolrInputDocument;
import org.apache.solr.common.params.ModifiableSolrParams;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import fr.mcc.ginco.beans.Note;
import fr.mcc.ginco.beans.ThesaurusConcept;
import fr.mcc.ginco.beans.ThesaurusTerm;
import fr.mcc.ginco.exceptions.BusinessException;
import fr.mcc.ginco.exceptions.TechnicalException;
import fr.mcc.ginco.log.Log;
import fr.mcc.ginco.solr.SearchResult;
import fr.mcc.ginco.solr.SearchResultList;
import fr.mcc.ginco.solr.SolrField;

/**
 * @author hufon
 *
 */

@Service("indexerService")
public class IndexerServiceImpl implements IIndexerService {

    @Log
    private Logger logger;

    @Inject
    @Named("thesaurusConceptService")
    private IThesaurusConceptService thesaurusConceptService;

    @Inject
    @Named("thesaurusTermService")
    private IThesaurusTermService thesaurusTermService;

    @Inject
    @Named("noteService")
    private INoteService noteService;

    @Value("${solr.url}")
    private String url;
    
    @Inject
    @Named("solrServer")
    private SolrServer solrServer;

    @Override
    public void removeTerm(ThesaurusTerm thesaurusTerm) throws TechnicalException {
        try {
            solrServer.deleteById(thesaurusTerm.getIdentifier());
        } catch (SolrServerException e) {
            throw new TechnicalException("Error executing query for removing Term from index!", e);
        } catch (IOException e) {
            throw new TechnicalException("IO error during executing query for removing Term from index!", e);
        }
    }
    
    private SearchResult getSearchResult(SolrDocument doc)
    {
    	SearchResult result = new SearchResult();
    	result.setIdentifier(doc.getFieldValue(SolrField.IDENTIFIER).toString());
    	result.setLexicalValue(doc.getFieldValue(SolrField.LEXICALVALUE).toString());
    	result.setType(doc.getFieldValue(SolrField.TYPE).toString());
    	result.setThesaurusId(doc.getFieldValue(SolrField.THESAURUSID).toString());
    	result.setThesaurusTitle(doc.getFieldValue(SolrField.THESAURUSTITLE).toString());
    	return result;
    }
    
    private SearchResultList getSearchResultList (SolrDocumentList list)
    {
    	SearchResultList results = new SearchResultList();
    	results.setNumFound(list.getNumFound());
        for (int i = 0; i < list.size(); ++i) {
        	results.add(getSearchResult(list.get(i)));
        }
        return results;
    }

    @Override
    public SearchResultList search(String request, int startIndex, int limit) throws SolrServerException {
        ModifiableSolrParams params = new ModifiableSolrParams();
        params.set("defType", "edismax");
        params.set("qf", SolrField.LEXICALVALUE+"^1.0 "+SolrField.NOTES+"^0.5");
        params.set("fl", "*,score");
        params.set("sort","score desc");
        params.set("q", request);
        params.set("start", startIndex);
        params.set("rows", limit);
        QueryResponse response = solrServer.query(params);
        SolrDocumentList solrresults = response.getResults();
        return getSearchResultList(solrresults);
    }

    @Override
    @Async
    public void removeConcept(ThesaurusConcept thesaurusConcept) throws TechnicalException {
        try {
        	solrServer.deleteById(thesaurusConcept.getIdentifier());
        } catch (SolrServerException e) {
            throw new TechnicalException("Error executing query for removing Concept from index!", e);
        } catch (IOException e) {
            throw new TechnicalException("IO error during executing query for removing Concept from index!", e);
        }
    }
    
    private SolrInputDocument convertConcept(ThesaurusConcept thesaurusConcept)
    {
    	SolrInputDocument doc = new SolrInputDocument();
        doc.addField(SolrField.THESAURUSID, thesaurusConcept.getThesaurusId());
        doc.addField(SolrField.THESAURUSTITLE, thesaurusConcept.getThesaurus().getTitle());
        doc.addField(SolrField.IDENTIFIER, thesaurusConcept.getIdentifier());
        doc.addField(SolrField.TYPE, ThesaurusConcept.class.getSimpleName());
        String prefLabel;
        try {
            prefLabel = thesaurusConceptService.getConceptPreferredTerm(thesaurusConcept.getIdentifier())
                    .getLexicalValue();
        } catch (BusinessException ex) {
            logger.warn(ex.getMessage());
            return null;
        }
        doc.addField(SolrField.LEXICALVALUE, prefLabel);
        List<Note> notes = noteService.getConceptNotePaginatedList(thesaurusConcept.getIdentifier(), 0, 0);
        for(Note note : notes ) {
        	doc.addField(SolrField.NOTES, note.getLexicalValue());
        }
        return doc;
    }

    @Override
    @Async
    public void addConcept(ThesaurusConcept thesaurusConcept) throws TechnicalException {
        addConcepts(Arrays.asList(thesaurusConcept));
    }

    private void addConcepts(List<ThesaurusConcept> thesaurusConcepts) throws TechnicalException {
        for(ThesaurusConcept concept : thesaurusConcepts) {
        	SolrInputDocument doc = convertConcept(concept);
        	if (doc!=null) {
        		try {
        			logger.info("Indexing concept :"+concept.getIdentifier());
					solrServer.add(doc);
				} catch (SolrServerException e) {
					throw new TechnicalException("Error during adding to SOLR!", e);
				} catch (IOException e) {
					throw new TechnicalException("IO error!", e);
				}
        	}
        }
        try {
        	solrServer.commit();
        } catch (SolrServerException e) {
            throw new TechnicalException("Error during adding to SOLR!", e);
        } catch (IOException e) {
            throw new TechnicalException("IO error!", e);
        }
    }
    
    private void removeAllIndex(SolrServer solrCore) {
        try {
        	solrServer.deleteByQuery("*:*");
        	solrServer.commit();
        } catch (SolrServerException e) {
            throw new TechnicalException("Error executing query for clearing SOLR index!", e);
        } catch (IOException e) {
            throw new TechnicalException("IO error during executing query for clearing SOLR index!", e);
        }
    }

    @Override
    public void forceIndexing() throws TechnicalException{
        try {
            solrServer.deleteByQuery("*:*");
            solrServer.commit();
        } catch (SolrServerException e) {
            throw new TechnicalException("Error executing query for clearing SOLR index!", e);
        } catch (IOException e) {
            throw new TechnicalException("IO error during executing query for clearing SOLR index!", e);
        }
        removeAllIndex(solrServer);

        addConcepts(thesaurusConceptService.getAllConcepts());
        addTerms(thesaurusTermService.getAllTerms());
    }

    @Override
    @Async
    public void addTerm(ThesaurusTerm thesaurusTerm) throws TechnicalException {
    	try {
    		addTerms(Arrays.asList(thesaurusTerm));
    	} catch (TechnicalException ex)
    	{
    		logger.error(ex.getMessage(),ex.getCause().getMessage());
    		throw ex;
    	}
    }
    
    /**
     * Convert a Thesaurus Term into a SolrDocument
     * 
     * @param thesaurusTerm
     * @return SolrInputDocument
     */
    private SolrInputDocument convertTerm(ThesaurusTerm thesaurusTerm)
    {
    	SolrInputDocument doc = new SolrInputDocument();
        doc.addField(SolrField.THESAURUSID, thesaurusTerm.getThesaurusId());
        doc.addField(SolrField.THESAURUSTITLE, thesaurusTerm.getThesaurus().getTitle());
        doc.addField(SolrField.IDENTIFIER, thesaurusTerm.getIdentifier());
        doc.addField(SolrField.LEXICALVALUE, thesaurusTerm.getLexicalValue());
        doc.addField(SolrField.TYPE, ThesaurusTerm.class.getSimpleName());
        List<Note> notes = noteService.getTermNotePaginatedList(thesaurusTerm.getIdentifier(), 0, 0);
        for(Note note : notes ) {
        	 doc.addField(SolrField.NOTES, note.getLexicalValue());
        }
        return doc;
    }

    private void addTerms(List<ThesaurusTerm> thesaurusTerms) {
        try {
	        for(ThesaurusTerm term : thesaurusTerms) {
	        	logger.info("Indexing term :"+term.getIdentifier());
	        	SolrInputDocument doc = convertTerm(term);
	        	solrServer.add(doc);
	        }
	        solrServer.commit();
        } catch (SolrServerException e) {
            throw new TechnicalException("Error during adding to SOLR!", e);
        } catch (IOException e) {
            throw new TechnicalException("IO error!", e);
        }
    }
}