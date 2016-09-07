package LuceneExp

import org.apache.lucene.analysis.standard.StandardAnalyzer
import org.apache.lucene.document.Document
import org.apache.lucene.document.Field
import org.apache.lucene.document.FieldType
import org.apache.lucene.document.StringField
import org.apache.lucene.index.DirectoryReader
import org.apache.lucene.index.IndexOptions
import org.apache.lucene.index.IndexReader
import org.apache.lucene.index.IndexWriter
import org.apache.lucene.index.IndexWriterConfig
import org.apache.lucene.index.MultiFields
import org.apache.lucene.index.PostingsEnum
import org.apache.lucene.index.Terms
import org.apache.lucene.index.TermsEnum
import org.apache.lucene.queryparser.classic.QueryParser
import org.apache.lucene.search.IndexSearcher
import org.apache.lucene.search.Query
import org.apache.lucene.search.ScoreDoc
import org.apache.lucene.search.TopScoreDocCollector
import org.apache.lucene.store.Directory
import org.apache.lucene.store.RAMDirectory
import org.apache.lucene.util.BytesRef


//http://stackoverflow.com/questions/35809035/how-to-get-positions-from-a-document-term-vector-in-lucene

StandardAnalyzer analyzer2 = new StandardAnalyzer();

FieldType ft = new FieldType();
ft.setIndexOptions( IndexOptions.DOCS_AND_FREQS_AND_POSITIONS_AND_OFFSETS );
ft.setStoreTermVectors( true );
ft.setStoreTermVectorOffsets( true );
ft.setStoreTermVectorPayloads( true );
ft.setStoreTermVectorPositions( true );
ft.setTokenized( true );
//return ft;

// 1. create the index
Directory index2 = new RAMDirectory();


IndexWriterConfig config = new IndexWriterConfig( analyzer2);


IndexWriter w = new IndexWriter(index2, config);
addDoc(w, "zz2 Tintin: the crab with zz2 zz2 the golden claws", "1941", ft);
addDoc(w, "Tintin: the black island", "1938", ft);
addDoc(w, "Tintin: zz2 the land of black gold", "1950",ft );
addDoc(w, "Tintin: destination zz2 moon", "1953",ft);
addDoc(w, "Tintin: explorers on zz2 the moon", "1954",ft);
w.close();

// 2. query
String querystr =  "zz2";

// the "title" arg specifies the default field to use
// when no field is explicitly specified in the query.
Query q = new QueryParser( "title", analyzer2).parse(querystr);

// 3. search
int hitsPerPage = 10;
IndexReader reader = DirectoryReader.open(index2);
IndexSearcher searcher = new IndexSearcher(reader);
TopScoreDocCollector collector = TopScoreDocCollector.create(hitsPerPage);
searcher.search(q, collector);
ScoreDoc[] hits = collector.topDocs().scoreDocs;

// 4. display results
println "Found " + hits.length + " hits."

Bits liveDocs = MultiFields.getLiveDocs(reader);
for (int i=0; i<reader.maxDoc(); i++) {
	if (liveDocs != null && !liveDocs.get(i))
		continue;

	Document doc = reader.document(i);
	
	println "i $i doc $doc"
}


hits.each{
	int docId = it.doc;
	Document d = searcher.doc(docId);

	println(d.get("year") + "\t" + d.get("title"));
	//}
	println "******************************************"
	//}
//https://lucene.apache.org/core/6_2_0/core/index.html?org/apache/lucene/index/CheckIndex.Status.TermVectorStatus.html
	Terms tv = reader.getTermVector( docId, "title" );
	TermsEnum terms = tv.iterator();
	PostingsEnum p = null;

	def x = 0


	while( terms.next() != null ) {
		p = terms.postings( p, PostingsEnum.POSITIONS ); 
		x=0
		while( p.nextDoc() != PostingsEnum.NO_MORE_DOCS ) {
			x++
			int freq = p.freq(); 
			//println "x $x  docId $docId p.Postions "  + p.POSITIONS
			println "freq $freq docId $docId"
		//	for( int i = 0; i < freq; i++ ) {
			freq.times {
				int pos = p.nextPosition();   // Always returns -1!!!
				BytesRef data = p.getPayload();

				println "pos $pos it $it"
				//doStuff( freq, pos, data ); // Fails miserably, of course.
			}
		}
	}
}




// reader can only be closed when there
// is no need to access the documents any more.
reader.close();


def addDoc(IndexWriter w, String title, String isbn, FieldType ft) throws IOException {
	Document doc = new Document();

	Field ff = new Field("title", title, ft)
	//doc.add(new TextField("title", title, Field.Store.YES));
	doc.add(ff);

	// use a string field for year because we don't want it tokenized
	doc.add(new StringField("year", isbn, Field.Store.YES));
	w.addDocument(doc);
}
