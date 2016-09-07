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

StandardAnalyzer analyzer = new StandardAnalyzer();

FieldType ft = new FieldType();
ft.setIndexOptions( IndexOptions.DOCS_AND_FREQS_AND_POSITIONS );
ft.setStoreTermVectors( true );
//ft.setStoreTermVectorOffsets( true );
//ft.setStoreTermVectorPayloads( true );
ft.setStoreTermVectorPositions( true );
ft.setTokenized( true );
//return ft;

Directory indexDir = new RAMDirectory();
IndexWriterConfig config = new IndexWriterConfig( analyzer);
IndexWriter w = new IndexWriter(indexDir, config);

addDoc(w, "one fish two fish", ft);
addDoc(w, "red fish blue fish", ft);

w.close();

// 2. query
String querystr =  "word0";

// the "title" arg specifies the default field to use
// when no field is explicitly specified in the query.
Query q = new QueryParser( "contents", analyzer).parse(querystr);

// 3. search
int hitsPerPage = 10;
IndexReader reader = DirectoryReader.open(indexDir);
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
	println "**********************************"
	doc = reader.document(i);

	println "i $i doc $doc"
	//}

//	Terms terms2 = reader.getTermVector(i, "contents");
//	TermsEnum termsEnum = terms2.iterator();
//	BytesRef bytesRef = termsEnum.next();
//	while(bytesRef  != null){
//		System.out.println("BytesRef: " + bytesRef.utf8ToString());
//		System.out.println("docFreq: " + termsEnum.docFreq());
//		System.out.println("totalTermFreq: " + termsEnum.totalTermFreq());
//
//		bytesRef = termsEnum.next();
//	}


	//}
	//https://lucene.apache.org/core/6_2_0/core/index.html?org/apache/lucene/index/CheckIndex.Status.TermVectorStatus.html
	Terms tv = reader.getTermVector( i, "contents" );
	TermsEnum terms = tv.iterator();
	PostingsEnum p = null;

	def x = 0

	BytesRef br = terms.next();
	
	while(br != null ) {
		println "&&&&  ${br.utf8ToString()}"
		p = terms.postings( p, PostingsEnum.POSITIONS );
		x=0
		while( p.nextDoc() != PostingsEnum.NO_MORE_DOCS ) {
			x++
			int freq = p.freq();
			//println "x $x  docId $docId p.Postions "  + p.POSITIONS
			println "freq $freq docId $i"
			//	for( int i = 0; i < freq; i++ ) {
			freq.times {
				int pos = p.nextPosition();   // Always returns -1!!!
			//	BytesRef data = p.getPayload();
//def x2 = br.u
				println " pos $pos it $it"
				//doStuff( freq, pos, data ); // Fails miserably, of course.
			}
		}
		br = terms.next();
	}
}


// reader can only be closed when there
// is no need to access the documents any more.
reader.close();


def addDoc(IndexWriter w, String text, FieldType ft) throws IOException {
	Document doc = new Document();

	Field f = new Field("contents", text, ft)	
	doc.add(f);
	w.addDocument(doc);
}
