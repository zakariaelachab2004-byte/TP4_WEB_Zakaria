package ma.emsi.elachab.tp4.tp4_web_zakaria.llm;

import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.google.genai.GoogleGenAiChatModel;

import dev.langchain4j.rag.DefaultRetrievalAugmentor;
import dev.langchain4j.rag.RetrievalAugmentor;
import dev.langchain4j.rag.content.retriever.ContentRetriever;
import dev.langchain4j.rag.query.router.LanguageModelQueryRouter;
import dev.langchain4j.rag.query.router.QueryRouter;
import dev.langchain4j.rag.query.transformer.CompressingQueryTransformer;

import dev.langchain4j.service.AiServices;

import jakarta.enterprise.context.Dependent;
import jakarta.inject.Inject;

import java.io.Serializable;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

@Dependent
public class LlmClient implements Serializable {

    private static final long serialVersionUID = 1L;

    private static final String ROLE_PAR_DEFAUT =
            "Tu es un assistant pédagogique utile et poli. " +
                    "Réponds en français en utilisant les documents fournis.";

    private String roleSysteme = ROLE_PAR_DEFAUT;

    @Inject
    private RagService ragService;

    private transient Assistant assistant;

    private void initialiserAssistant() {

        String apiKey = System.getenv("GEMINI_KEY");

        if (apiKey == null || apiKey.isBlank()) {
            throw new IllegalStateException(
                    "La variable GEMINI_KEY est absente."
            );
        }

        // 1. Modèle Gemini
        ChatModel model = GoogleGenAiChatModel.builder()
                .apiKey(apiKey)
                .modelName("gemini-3.5-flash-lite")
                .temperature(0.2)
                .logRequests(true)
                .logResponses(true)
                .build();

        // 2. Les récupérateurs des deux PDF
        ContentRetriever retrieverRag =
                ragService.getRetrieverRag();

        ContentRetriever retrieverLangchain4j =
                ragService.getRetrieverLangchain4j();

        // 3. Description des deux sources
        Map<ContentRetriever, String> descriptions =
                new LinkedHashMap<>();

        descriptions.put(
                retrieverRag,
                "Support de cours sur le RAG, " +
                        "le fine-tuning et les embeddings."
        );

        descriptions.put(
                retrieverLangchain4j,
                "Support de cours sur LangChain4j, " +
                        "ses API, ses composants et son utilisation."
        );

        // 4. Routage intelligent
        QueryRouter routeur =
                new LanguageModelQueryRouter(
                        model,
                        descriptions
                );

        // 5. Journalisation des sources
        QueryRouter routeurAvecLogs = query -> {

            Collection<ContentRetriever> selection =
                    routeur.route(query);

            System.out.println(
                    "[ROUTAGE] Question : " + query.text()
            );

            if (selection.contains(retrieverRag)) {
                System.out.println(
                        "[ROUTAGE] PDF sélectionné : rag.pdf"
                );
            }

            if (selection.contains(retrieverLangchain4j)) {
                System.out.println(
                        "[ROUTAGE] PDF sélectionné : langchain4j.pdf"
                );
            }

            return selection;
        };

        // 6. Configuration du RAG

        // Transformation des questions avec l'historique

        CompressingQueryTransformer queryTransformer =
                new CompressingQueryTransformer(model);

        RetrievalAugmentor augmentor =
                DefaultRetrievalAugmentor.builder()
                        .queryTransformer(query -> {

                            var requetesTransformees =
                                    queryTransformer.transform(query);

                            for (var requeteTransformee : requetesTransformees) {

                                System.out.println(
                                        "[TRANSFORMATION] Question originale : "
                                                + query.text()
                                );

                                System.out.println(
                                        "[TRANSFORMATION] Question reformulée : "
                                                + requeteTransformee.text()
                                );
                            }

                            return requetesTransformees;
                        })
                        .queryRouter(routeurAvecLogs)
                        .build();

        System.out.println(
                "[RAG] Transformation des questions activée."
        );


        System.out.println(
                "[RAG] Transformation des questions activée."
        );

        // 7. Assistant conversationnel
        assistant = AiServices.builder(Assistant.class)
                .chatModel(model)
                .chatMemory(
                        MessageWindowChatMemory.withMaxMessages(10)
                )
                .systemMessageProvider(id -> roleSysteme)
                .retrievalAugmentor(augmentor)
                .build();

        System.out.println(
                "[RAG] Assistant avec routage intelligent prêt."
        );
    }

    public void setSystemRole(String role) {
        if (role == null || role.isBlank()) {
            roleSysteme = ROLE_PAR_DEFAUT;
        } else {
            roleSysteme = role;
        }
    }

    public String envoyer(String question) {

        if (question == null || question.isBlank()) {
            return "";
        }

        if (assistant == null) {
            initialiserAssistant();
        }

        return assistant.chat(question);
    }

    public void nouveauChat() {
        assistant = null;
        roleSysteme = ROLE_PAR_DEFAUT;
    }
}
