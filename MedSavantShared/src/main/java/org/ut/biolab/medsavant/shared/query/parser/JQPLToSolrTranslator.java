package org.ut.biolab.medsavant.shared.query.parser;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.solr.client.solrj.SolrQuery;
import org.ut.biolab.medsavant.shared.query.SimpleSolrQuery;
import org.ut.biolab.medsavant.shared.query.parser.analyzer.QueryAnalyzer;
import org.ut.biolab.medsavant.shared.query.parser.lexer.LexerException;
import org.ut.biolab.medsavant.shared.query.parser.node.Start;
import org.ut.biolab.medsavant.shared.query.parser.parser.ParserException;

import java.io.IOException;
import java.util.Map;

/**
 * Translate JPQL queries.
 */
public class JQPLToSolrTranslator {

    private QueryAnalyzer analyzer;

    private static final Log LOG = LogFactory.getLog(JQPLToSolrTranslator.class);

    public JQPLToSolrTranslator() {
        this.analyzer = new QueryAnalyzer(new QueryContext());
    }

    public JQPLToSolrTranslator(QueryAnalyzer analyzer) {
        this.analyzer = analyzer;
    }

    public SolrQuery translate(String input) {

        JPQLParser parser = new JPQLParser();
        SolrQuery solrQuery = null;
        try {
            Start tree = parser.parse(input);
            tree.apply(analyzer);

            solrQuery = analyzer.getSolrQuery();

        } catch (ParserException e) {
            LOG.error("Unable to parse string " + input, e);
        } catch (IOException e) {
            LOG.error("Unable to parse string " + input, e);
        } catch (LexerException e) {
            LOG.error("Unable to parse string " + input, e);
        }

        return solrQuery;
    }

    public QueryAnalyzer getAnalyzer() {
        return analyzer;
    }

    public void setAnalyzer(QueryAnalyzer analyzer) {
        this.analyzer = analyzer;
    }
}
