package ma.emsi.elachab.tp4.tp4_web_zakaria.jsf;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import ma.emsi.elachab.tp4.tp4_web_zakaria.llm.LlmClient;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Named("bb")
@ViewScoped
public class Bb implements Serializable {

    private String question;
    private String reponse;
    private String conversation = "";

    private String roleSysteme;
    private final List<String> rolesSysteme;

    private boolean roleSystemeChangeable = true;

    @Inject
    private LlmClient llmClient;

    public Bb() {

        rolesSysteme = new ArrayList<>();

        rolesSysteme.add(
                "Tu es un assistant utile et poli."
        );

        rolesSysteme.add(
                "Tu es un expert en intelligence artificielle."
        );

        rolesSysteme.add(
                "Tu es un assistant pédagogique qui répond " +
                        "à partir des documents fournis."
        );
        roleSysteme = rolesSysteme.get(0);
    }


    public String envoyer() {

        if (question == null || question.isBlank()) {
            return null;
        }

        try {

            if (roleSystemeChangeable) {
                llmClient.setSystemRole(roleSysteme);
            }

            roleSystemeChangeable = false;

            reponse = llmClient.envoyer(question);

            conversation += "Moi : " + question + "\n";
            conversation += "Assistant : " + reponse + "\n\n";

            question = "";

        } catch (Exception e) {

            reponse = "Erreur lors de la génération de la réponse.";
            // Pour le développement : journaliser l'exception
            e.printStackTrace();
        }

        return null;
    }

    public String nouveauChat() {

        llmClient.nouveauChat();

        question = "";
        reponse = "";
        conversation = "";

        roleSysteme = rolesSysteme.get(0);
        roleSystemeChangeable = true;

        return null;
    }

    public String getQuestion() {
        return question;
    }

    public void setQuestion(String question) {
        this.question = question;
    }

    public String getReponse() {
        return reponse;
    }

    public void setReponse(String reponse) {
        this.reponse = reponse;
    }

    public String getConversation() {
        return conversation;
    }

    public void setConversation(String conversation) {
        this.conversation = conversation;
    }

    public String getRoleSysteme() {
        return roleSysteme;
    }

    public void setRoleSysteme(String roleSysteme) {
        this.roleSysteme = roleSysteme;
    }

    public List<String> getRolesSysteme() {
        return rolesSysteme;
    }

    public boolean isRoleSystemeChangeable() {
        return roleSystemeChangeable;
    }

    public void setRoleSystemeChangeable(boolean value) {
        this.roleSystemeChangeable = value;
    }
}

