package pl.net.bluesoft.rnd.processtool.plugins.osgi;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.Fieldable;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.*;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;
import org.aperteworkflow.search.ProcessInstanceSearchAttribute;
import org.aperteworkflow.search.ProcessInstanceSearchData;
import org.aperteworkflow.search.SearchProvider;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * User: POlszewski
 * Date: 2012-11-27
 * Time: 16:39
 */
class LuceneSearchService implements SearchProvider {
	private static final String AWF__ID = "__AWF__ID";
	private static final String AWF__TYPE = "__AWF__TYPE";
	private static final String AWF__ROLE = "__AWF__ROLE";
	private static final String AWF_RUNNING = "__AWF__running";
	private static final String AWF__ASSIGNEE = "__AWF__assignee";
	private static final String AWF__QUEUE = "__AWF__queue";

	private static final String PROCESS_INSTANCE = "PROCESS_INSTANCE";

	private static final int SEARCH_LIMIT = 1000;

	private String luceneDir;
	private Directory index;
	private IndexSearcher indexSearcher;
	private IndexReader indexReader;

	private Logger LOGGER;

	public LuceneSearchService(Logger logger) {
		this.LOGGER = logger;
	}

	public void initialize() {
		try {
			File path = new File(luceneDir);
			if (!path.exists()) {
				LOGGER.severe("Default lucene index directory: " + luceneDir + " not found, attempting to create...");
				if (!path.mkdir()) {
					LOGGER.severe("Failed to create Default lucene index directory: " + luceneDir);
				} else {
					LOGGER.severe("Created Default lucene index directory: " + luceneDir);
				}
			}
			try { if (indexSearcher != null) {
				indexSearcher.close();
			} } catch (Exception e) { LOGGER.log(Level.SEVERE, e.getMessage(), e); }
			try { if (indexReader != null) {
				indexReader.close();
			} } catch (Exception e) { LOGGER.log(Level.SEVERE, e.getMessage(), e); }
			try { if (index != null) {
				index.close();
			} } catch (Exception e) { LOGGER.log(Level.SEVERE, e.getMessage(), e); }
			index = FSDirectory.open(path);
			indexReader = IndexReader.open(index);
			indexSearcher = new IndexSearcher(indexReader);

		} catch (IOException e) {
			LOGGER.log(Level.SEVERE, e.getMessage(), e);
			throw new RuntimeException(e);
		}
	}

	@Override
	public void updateIndex(ProcessInstanceSearchData processInstanceSearchData) {
		Document doc = new Document();
		doc.add(new Field(AWF__ID,
				String.valueOf(processInstanceSearchData.getProcessInstanceId()),
				Field.Store.YES,Field.Index.NOT_ANALYZED));
		doc.add(new Field(AWF__TYPE, PROCESS_INSTANCE, Field.Store.YES, Field.Index.NOT_ANALYZED));
		for (ProcessInstanceSearchAttribute attr : processInstanceSearchData.getSearchAttributes()) {
			if (attr.getValue() != null && !attr.getValue().trim().isEmpty()) {
				Field field = new Field(attr.getName(),
						attr.isKeyword() ? attr.getValue().toLowerCase() : attr.getValue(),
						Field.Store.YES,
						attr.isKeyword() ? Field.Index.NOT_ANALYZED : Field.Index.ANALYZED);
				doc.add(field);
			}
		}
		updateIndex(doc);
	}

	@Override
	public List<Long> searchProcesses(String query, Integer offset,
									  Integer limit, boolean onlyRunning, String[] userRoles,
									  String assignee, String... queues) {

		List<Document> results;
		List<Query> addQueries = new ArrayList<Query>();
		if (offset == null) {
			offset = null; // ROFL!!!
		}
		if (limit == null) {
			limit = SEARCH_LIMIT;
		}

		if (assignee != null) {
			addQueries.add(new TermQuery(new Term(AWF__ASSIGNEE, assignee)));
		}
		if (queues != null)
			for (String queue : queues) {
				addQueries.add(new TermQuery(new Term(AWF__QUEUE, queue)));
			}
		if (onlyRunning) {
			addQueries.add(new TermQuery(new Term(AWF_RUNNING, String
					.valueOf(true))));
		}

		if (userRoles != null) {
			BooleanQuery bq = new BooleanQuery();
			bq.add(new TermQuery(new Term(AWF__ROLE, "__AWF__ROLE_ALL"
					.toLowerCase())), BooleanClause.Occur.SHOULD);
			for (String roleName : userRoles) {
				bq.add(new TermQuery(new Term(AWF__ROLE, roleName.replace(' ',
						'_').toLowerCase())), BooleanClause.Occur.SHOULD);
			}
			addQueries.add(bq);
		}
		results = search(query, 0, SEARCH_LIMIT,
				addQueries.toArray(new Query[addQueries.size()]));
		// always check 1000 first results - larger limit means no sense and
		// Lucene provides the results
		// with no sort guarantees

		List<Long> res = new ArrayList<Long>(results.size());
		for (Document doc : results) {
			Fieldable fieldable = doc.getFieldable(AWF__ID);
			if (fieldable != null) {
				String s = fieldable.stringValue();
				if (s != null) {
					res.add(Long.parseLong(s));
				}
			}
		}
		Collections.sort(res);
		Collections.reverse(res);
		return res.subList(offset, Math.min(offset + limit, res.size()));
	}

	public List<Document> search(String query, int offset, int limit, Query... addQueries) {
		try {
			LOGGER.fine("Parsing lucene search query: " + query);
			QueryParser qp = new QueryParser(Version.LUCENE_35, "all", new StandardAnalyzer(Version.LUCENE_35));
			Query q = qp.parse(query);
			BooleanQuery bq = new BooleanQuery();
			bq.add(new TermQuery(new Term(AWF__TYPE, PROCESS_INSTANCE)), BooleanClause.Occur.MUST);
			for (Query qq : addQueries) {
				bq.add(qq, BooleanClause.Occur.MUST);
			}
			bq.add(q, BooleanClause.Occur.MUST);

			LOGGER.fine("Searching lucene index with query: " + bq.toString());
			TopDocs search = indexSearcher.search(bq, offset + limit);

			List<Document> results = new ArrayList<Document>(limit);
			LOGGER.fine("Total result count for query: " + bq.toString() + " is " + search.totalHits);
			for (int i = offset; i < offset+limit && i < search.totalHits; i++) {
				ScoreDoc scoreDoc = search.scoreDocs[i];
				results.add(indexSearcher.doc(scoreDoc.doc));
			}
			return results;
		} catch (Exception e) {
			LOGGER.log(Level.SEVERE, e.getMessage(), e);
			throw new RuntimeException(e);

		}
	}

	public synchronized void updateIndex(Document... docs) {
		try {
			//how awesome to force programmer to hardcode library version with no reasonable default
			IndexWriterConfig cfg = new IndexWriterConfig(Version.LUCENE_35, new StandardAnalyzer(Version.LUCENE_35));
			IndexWriter indexWriter = new IndexWriter(index, cfg);
			for (Document doc : docs) {
				LOGGER.fine("Updating index for document: " + doc.getFieldable(AWF__ID));
				indexWriter.deleteDocuments(new Term(AWF__ID, doc.getFieldable(AWF__ID).stringValue()));
				StringBuilder all = new StringBuilder();
				for (Fieldable f : doc.getFields()) {
					all.append(f.stringValue());
					all.append(' ');
				}
				LOGGER.fine("Updated field all for "+ doc.getFieldable(AWF__ID) + " with value: " + all);
				doc.add(new Field("all", all.toString(), Field.Store.NO, Field.Index.ANALYZED));
			}
			indexWriter.addDocuments(Arrays.asList(docs));
			LOGGER.fine("reindexing Lucene...");
			indexWriter.commit();
			indexWriter.close();
			LOGGER.fine("reindexing Lucene... DONE!");

			try { if (indexSearcher != null) {
				indexSearcher.close();
			} } catch (Exception e) { LOGGER.log(Level.SEVERE, e.getMessage(), e); }
			try { if (indexReader != null) {
				indexReader.close();
			} } catch (Exception e) { LOGGER.log(Level.SEVERE, e.getMessage(), e); }

			indexReader = IndexReader.open(index);
			indexSearcher = new IndexSearcher(indexReader);
			LOGGER.fine("reopened Lucene index handles");

		} catch (IOException e) {
			LOGGER.log(Level.SEVERE, e.getMessage(), e);
			throw new RuntimeException(e);
		}
	}

	public void setLuceneDir(String luceneDir) {
		this.luceneDir = luceneDir;
	}
}
