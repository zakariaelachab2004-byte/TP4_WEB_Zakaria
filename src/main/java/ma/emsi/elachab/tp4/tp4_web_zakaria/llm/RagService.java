package ma.emsi.elachab.tp4.tp4_web_zakaria.llm;

import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.loader.ClassPathDocumentLoader;
import dev.langchain4j.data.document.parser.apache.tika.ApacheTikaDocumentParser;
import dev.langchain4j.data.document.splitter.DocumentSplitters;
import dev.langchain4j.data.segment.TextSegment;

import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.embedding.onnx.bgesmallenv15q.BgeSmallEnV15QuantizedEmbeddingModel;
import dev.langchain4j.rag.content.retriever.ContentRetriever;
import dev.langchain4j.rag.content.retriever.EmbeddingStoreContentRetriever;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.inmemory.InMemoryEmbeddingStore;

import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.List;

@ApplicationScoped
public class RagService {

    private ContentRetriever retrieverRag;
    private ContentRetriever retrieverLangchain4j;

    @PostConstruct
    public void initialiser() {

        EmbeddingModel embeddingModel =
                new BgeSmallEnV15QuantizedEmbeddingModel();

        retrieverRag = creerRetriever(
                "rag.pdf",
                embeddingModel
        );

        retrieverLangchain4j = creerRetriever(
                "langchain4j.pdf",
                embeddingModel
        );

        System.out.println(
                "[RAG] Les deux documents ont été indexés."
        );
    }

    private ContentRetriever creerRetriever(
            String fichier,
            EmbeddingModel embeddingModel) {

        Document document = ClassPathDocumentLoader.loadDocument(
                fichier,
                new ApacheTikaDocumentParser()
        );

        List<TextSegment> segments =
                DocumentSplitters.recursive(300, 30)
                        .split(document);

        EmbeddingStore<TextSegment> store =
                new InMemoryEmbeddingStore<>();

        store.addAll(
                embeddingModel.embedAll(segments).content(),
                segments
        );

        System.out.println(
                "[INDEXATION] " + fichier + " : " +
                        segments.size() + " segments"
        );

        return EmbeddingStoreContentRetriever.builder()
                .embeddingStore(store)
                .embeddingModel(embeddingModel)
                .maxResults(3)
                .minScore(0.5)
                .build();
    }

    public ContentRetriever getRetrieverRag() {
        return retrieverRag;
    }

    public ContentRetriever getRetrieverLangchain4j() {
        return retrieverLangchain4j;
    }
}
