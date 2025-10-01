package swimworkoutbuilder.model.utils;

import com.openai.client.OpenAIClient;
import com.openai.client.okhttp.OpenAIOkHttpClient;
import com.openai.models.ChatModel;
import com.openai.models.chat.completions.ChatCompletion;
import com.openai.models.chat.completions.ChatCompletionCreateParams;

public class TestOpenAI {
    public static void main(String[] args) {
        String key = System.getenv("OPENAI_API_KEY");
        if (key == null || key.isBlank()) {
            System.err.println("❌ OPENAI_API_KEY not set. Add it to Run Config or ~/.zshrc");
            System.exit(2);
        }
        OpenAIClient client = OpenAIOkHttpClient.fromEnv();

        ChatCompletionCreateParams params = ChatCompletionCreateParams.builder()
                .model(ChatModel.GPT_4_1_MINI) // API name for “GPT-5 Mini” in ChatGPT
                .addUserMessage("Say hello from SwimWorkoutBuilder!")
                .build();

        ChatCompletion completion = client.chat().completions().create(params);
        System.out.println("🔹 OpenAI replied:");
        completion.choices().forEach(c -> c.message().content().ifPresent(System.out::println));
    }
}