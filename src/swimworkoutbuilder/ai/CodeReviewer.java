package swimworkoutbuilder.ai;

import com.openai.client.OpenAIClient;
import com.openai.client.okhttp.OpenAIOkHttpClient;
import com.openai.models.ChatModel;
import com.openai.models.chat.completions.ChatCompletion;
import com.openai.models.chat.completions.ChatCompletionCreateParams;

public class CodeReviewer {
    private final OpenAIClient client = OpenAIOkHttpClient.fromEnv();

    public String review(String code, String question) {
        ChatCompletionCreateParams params = ChatCompletionCreateParams.builder()
                .model(ChatModel.GPT_4_1)
                .addUserMessage("Here is some Java code:\n\n" + code +
                        "\n\nQuestion: " + question)
                .build();

        ChatCompletion completion = client.chat().completions().create(params);
        return completion.choices().get(0).message().content().orElse("(no reply)");
    }

    public static void main(String[] args) {
        String code = "public class Hello { public static void main(String[] args){ System.out.println(\"Hi\"); } }";
        String question = "What’s wrong with this code?";
        System.out.println(new CodeReviewer().review(code, question));
    }
}