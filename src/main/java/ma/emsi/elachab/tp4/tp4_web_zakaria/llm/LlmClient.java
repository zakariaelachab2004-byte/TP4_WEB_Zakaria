package ma.emsi.elachab.tp4.tp4_web_zakaria.llm;

import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.google.genai.GoogleGenAiChatModel;
import dev.langchain4j.service.AiServices;

import jakarta.enterprise.context.Dependent;

import java.io.Serializable;

@Dependent
public class LlmClient implements Serializable {

    private static final long serialVersionUID = 1L;

    private static final String ROLE_PAR_DEFAUT =
            "Tu es un assistant pédagogique utile et poli. " +
                    "Réponds en français.";

    private String roleSysteme = ROLE_PAR_DEFAUT;

    // Les objets techniques ne doivent pas être sérialisés par JSF.
    private transient Assistant assistant;

    public LlmClient() {
    }

    private void initialiserAssistant() {

        String apiKey = System.getenv("GEMINI_KEY");

        if (apiKey == null || apiKey.isBlank()) {
            throw new IllegalStateException(
                    "La variable GEMINI_KEY est absente."
            );
        }

        ChatModel model = GoogleGenAiChatModel.builder()
                .apiKey(apiKey)
                .modelName("gemini-3.5-flash-lite")
                .temperature(0.2)
                .build();

        assistant = AiServices.builder(Assistant.class)
                .chatModel(model)
                .chatMemory(
                        MessageWindowChatMemory.withMaxMessages(10)
                )
                .systemMessageProvider(id -> roleSysteme)
                .build();
    }

    public void setSystemRole(String role) {

        if (role == null || role.isBlank()) {
            this.roleSysteme = ROLE_PAR_DEFAUT;
        } else {
            this.roleSysteme = role;
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

        // Recréer l'assistant au prochain message
        // réinitialise également la mémoire conversationnelle.
        assistant = null;
        roleSysteme = ROLE_PAR_DEFAUT;
    }
}
